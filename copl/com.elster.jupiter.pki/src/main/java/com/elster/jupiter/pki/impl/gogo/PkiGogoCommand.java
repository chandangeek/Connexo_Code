/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.gogo;

import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.KeypairWrapper;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.PlaintextPrivateKeyWrapper;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.gogo.MysqlPrint;

import com.google.common.io.ByteStreams;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component(name = "com.elster.jupiter.pki.gogo.impl.PkiGogoCommand",
        service = PkiGogoCommand.class,
        property = {"osgi.command.scope=pki",
                "osgi.command.function=keytypes",
                "osgi.command.function=certificateStore",
                "osgi.command.function=deleteCertificate",
                "osgi.command.function=keypairs",
                "osgi.command.function=deleteKeypair",
                "osgi.command.function=generateKeypair",
                "osgi.command.function=importPublicKey",
                "osgi.command.function=importKeypair",

        },
        immediate = true)
public class PkiGogoCommand {
    public static final MysqlPrint MYSQL_PRINT = new MysqlPrint();

    private volatile PkiService pkiService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;

    public PkiGogoCommand() {
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setPkiService(PkiService pkiService) {
        this.pkiService = pkiService;
    }

    public void keytypes() {
        List<List<?>> collect = pkiService.findAllKeyTypes()
                .stream()
                .sorted(Comparator.comparing(KeyType::getId))
                .map(keytype -> Arrays.asList(keytype.getId(), keytype.getName(), keytype.getCryptographicType().name(), keytype.getKeyAlgorithm()))
                .collect(toList());
        collect.add(0, Arrays.asList("id", "name", "type", "algorithm"));
        MYSQL_PRINT.printTableWithHeader(collect);
    }

    public void certificateStore() {
        List<List<?>> certs = pkiService.findAllCertificates()
                .stream()
                .map(cert -> Arrays.asList(cert.getAlias(), cert.getCertificate().isPresent()))
                .collect(toList());
        MYSQL_PRINT.printTableWithHeader(Arrays.asList("Alias", "Certificate"), certs);
    }

    public void deleteCertificate() {
        System.out.println("Delete a certificate, identified by alias");
        System.out.println("usage: deleteCertificate <alias>");
        System.out.println("e.g. : deleteCertificate \"TLS 1\"");
    }

    public void deleteCertificate(String alias) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {

            pkiService.findCertificateWrapper(alias)
                    .orElseThrow(() -> new IllegalArgumentException("No such certificate"))
                    .delete();
            context.commit();
        }
    }

    public void keypairs() {
        List<List<?>> lists = pkiService.findAllKeypairs()
                .stream()
                .sorted(Comparator.comparing(KeypairWrapper::getAlias))
                .map(keypair -> Arrays.asList(keypair.getId(), keypair.getAlias(), keypair.getKeyType()
                        .getKeyAlgorithm(), keypair.getPublicKey().isPresent(), keypair.hasPrivateKey()))
                .collect(toList());
        MYSQL_PRINT.printTableWithHeader(Arrays.asList("id", "Alias", "KeyType", "PublicKey", "PrivateKey"), lists);
    }

    public void generateKeypair() {
        System.out.println("usage: createKeypair <alias> <keytype>");
    }

    public void generateKeypair(String alias, int keyTypeId) {
        threadPrincipalService.set(() -> "Console");
        KeyType keyType = pkiService.getKeyType(keyTypeId)
                .orElseThrow(() -> new IllegalArgumentException("No key type with id " + keyTypeId));

        try (TransactionContext context = transactionService.getContext()) {
            KeypairWrapper keypairWrapper = pkiService.newKeypairWrapper(alias, keyType, "DataVault");
            keypairWrapper.generateValue();
            context.commit();
        }
    }

    public void deleteKeypair() {
        System.out.println("usage: deleteKeypair <id>");
    }

    public void deleteKeypair(int keyPairId) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            pkiService.findKeypairWrapper(keyPairId)
                    .orElseThrow(() -> new IllegalArgumentException("No keypair with id " + keyPairId))
                    .delete();
            context.commit();
        }
    }

    public void importPublicKey() {
        System.out.println("usage: importPublicKey <alias> <keyTypeId> <file>");
    }

    public void importPublicKey(String alias, Long keyTypeId, String file) {
        threadPrincipalService.set(() -> "Console");
        KeyType keyType = pkiService.getKeyType(keyTypeId)
                .orElseThrow(() -> new IllegalArgumentException("No key type with id " + keyTypeId));
        try (TransactionContext context = transactionService.getContext()) {
            KeypairWrapper keypairWrapper = pkiService.newPublicKeyWrapper(alias, keyType);
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bytes = ByteStreams.toByteArray(fileInputStream);
            keypairWrapper.setPublicKey(bytes);
            keypairWrapper.save();
            context.commit();
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("No such file: "+file);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading file: "+file+" : "+e);
        }
    }

    public void importKeypair() {
        System.out.println("usage: importKeypair <alias> <keyTypeId> <private key file (DER)>");
    }

    public void importKeypair(String alias, Long keyTypeId, String privateKeyFile) {
        threadPrincipalService.set(() -> "Console");
        KeyType keyType = pkiService.getKeyType(keyTypeId)
                .orElseThrow(() -> new IllegalArgumentException("No key type with id " + keyTypeId));
        try (TransactionContext context = transactionService.getContext()) {
            KeypairWrapper keypairWrapper = pkiService.newKeypairWrapper(alias, keyType, "DataVault");
            FileInputStream fileInputStream = new FileInputStream(privateKeyFile);
            byte[] privateKeyBytes = ByteStreams.toByteArray(fileInputStream);
            PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(keyType.getKeyAlgorithm());
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);
            PlaintextPrivateKeyWrapper privateKeyWrapper = (PlaintextPrivateKeyWrapper) keypairWrapper.getPrivateKeyWrapper().get();
            privateKeyWrapper.setPrivateKey(privateKey);
            privateKeyWrapper.save();
            keypairWrapper.setPublicKey(privateKeyWrapper.getPublicKey());
            keypairWrapper.save();
            context.commit();
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("No such file: "+privateKeyFile);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading file: "+privateKeyFile+" : "+e);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("No such algorithm: "+e);
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException("Invalid key spec: "+e);
        }
    }

}

