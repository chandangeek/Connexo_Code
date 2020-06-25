/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.gogo;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.pki.CaService;
import com.elster.jupiter.pki.CertificateAuthoritySearchFilter;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.KeypairWrapper;
import com.elster.jupiter.pki.PlaintextPrivateKeyWrapper;
import com.elster.jupiter.pki.RevokeStatus;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.pki.impl.importers.csr.CertificateExportProcessor;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.gogo.MysqlPrint;

import com.google.common.io.ByteStreams;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

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
                "osgi.command.function=exportPublicKey",
                "osgi.command.function=importKeypair",
                "osgi.command.function=getPkiCaNames",
                "osgi.command.function=getPkiInfo",
                "osgi.command.function=revokeCertificate",
                "osgi.command.function=checkRevocationStatus",
                "osgi.command.function=getLatestCRL",
                "osgi.command.function=importSuperadmin",
                "osgi.command.function=printTrustedCertificates",
                "osgi.command.function=signFile"
        },
        immediate = true)
public class PkiGogoCommand {
    public static final MysqlPrint MYSQL_PRINT = new MysqlPrint();

    private volatile SecurityManagementService securityManagementService;
    private volatile CaService caService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;
    private volatile Thesaurus thesaurus;

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
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Reference
    public void setCaService(CaService caService) {
        this.caService = caService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(SecurityManagementService.COMPONENTNAME, Layer.DOMAIN);
    }

    public void keytypes() {
        List<List<?>> collect = securityManagementService.findAllKeyTypes()
                .stream()
                .sorted(Comparator.comparing(KeyType::getId))
                .map(keytype -> Arrays.asList(keytype.getId(), keytype.getName(), keytype.getCryptographicType().name(), keytype.getKeyAlgorithm()))
                .collect(toList());
        collect.add(0, Arrays.asList("id", "name", "type", "algorithm"));
        MYSQL_PRINT.printTableWithHeader(collect);
    }

    public void certificateStore() {
        List<List<?>> certs = securityManagementService.findAllCertificates().stream()
                .map(cert -> Arrays.asList(cert.getAlias(), cert.getCertificate().isPresent())).collect(toList());
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

            securityManagementService.findCertificateWrapper(alias).orElseThrow(() -> new IllegalArgumentException("No such certificate"))
                    .delete();
            context.commit();
        }
    }

    public void getPkiCaNames() {
        List<List<?>> collect = new ArrayList<>();
        collect.add(0, caService.getPkiCaNames());
        MYSQL_PRINT.printTableWithHeader(collect);
    }

    public void getPkiInfo() {
        String result = caService.getPkiInfo();
        System.out.println(result);
    }

    private String readInput() throws IOException {
        return new BufferedReader(new InputStreamReader(System.in)).readLine();
    }

    private boolean analyseResponse(String response) {
        String[] ok = {"", "y", "yes"};
        return Stream.of(ok).anyMatch(option -> option.equalsIgnoreCase(response));
    }

    private boolean analyseRevocationReason(String reason) {
        String[] ok = {"0", "1", "2", "3", "4", "5", "6", "8", "9", "10"};
        return Stream.of(ok).anyMatch(option -> option.equalsIgnoreCase(reason));
    }

    private void printRevocationReason() {
        StringBuilder sb = new StringBuilder()
                .append("Reasons to revoke a certificate according to RFC 5280 p69:")
                .append('\n')
                .append("unspecified (0), ")
                .append("keyCompromise (1), ")
                .append("CACompromise (2), ")
                .append("affiliationChanged (3)")
                .append('\n')
                .append("superseded (4), ")
                .append("cessationOfOperation (5), ")
                .append("certificateHold (6), ")
                .append("removeFromCRL (8)")
                .append('\n')
                .append("privilegeWithdrawn (9), ")
                .append("AACompromise (10)");
        System.out.println(sb);
    }

    public void revokeCertificate() {
        System.out.println("Revokes certificate");
        System.out.println("usage: revokeCertificate <certificate s/n> <issuer DN> <revocation reason [0-6, 8-10]>");
        printRevocationReason();
    }

    public void revokeCertificate(String serialNumber, String issuerDN, String reason) {
        BigInteger sn;
        Integer r;
        List<List<?>> collect = new ArrayList<>();
        CertificateAuthoritySearchFilter certificateSearchFilter = new CertificateAuthoritySearchFilter();
        try {
            sn = new BigInteger(serialNumber);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Specify valid serial number");
        }
        try {
            r = Integer.parseInt(reason);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Specify valid revocation reason");
        }
        if (RevokeStatus.fromValue(r) == null) {
            throw new IllegalArgumentException("Specify valid revocation reason");
        }
        certificateSearchFilter.setSerialNumber(sn);
        certificateSearchFilter.setIssuerDN(issuerDN);
        collect.clear();
        collect.add(0, Arrays.asList("Revoking certificate with reason " + r));
        MYSQL_PRINT.printTable(collect);
        caService.revokeCertificate(certificateSearchFilter, r);
    }


    public void checkRevocationStatus() {
        System.out.println("Checks certificate revocation status");
        System.out.println("usage: checkRevocationStatus <certificate s/n> <issuer DN>");
    }

    public void checkRevocationStatus(String serialNumber, String issuerDN) {
        BigInteger sn;
        List<List<?>> collect = new ArrayList<>();
        CertificateAuthoritySearchFilter certificateSearchFilter = new CertificateAuthoritySearchFilter();
        try {
            sn = new BigInteger(serialNumber);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Specify valid serial number");
        }
        certificateSearchFilter.setSerialNumber(sn);
        certificateSearchFilter.setIssuerDN(issuerDN);
        collect.clear();
        collect.add(0, Arrays.asList("Checking certificate revocation status"));
        MYSQL_PRINT.printTable(collect);
        RevokeStatus revokeStatus = caService.checkRevocationStatus(certificateSearchFilter);
        collect.clear();
        collect.add(0, Arrays.asList("Received revocation status"));
        collect.add(1, Arrays.asList(revokeStatus));
        MYSQL_PRINT.printTable(collect);
    }


    public void getLatestCRL() {
        System.out.println("Usage: getlatestcrl <caName> <true|false>");
        System.out.println("CRL: getlatestcrl <caName> false");
        System.out.println("Delta CRL: getlatestcrl <caName> true");
    }

    public void getLatestCRL(String caName, String delta) {
        try {
            boolean deltaFlag = Boolean.parseBoolean(delta);
            Optional<X509CRL> LatestCRL = (deltaFlag == false) ? caService.getLatestCRL(caName) : caService.getLatestDeltaCRL(caName);
            if (LatestCRL.isPresent()) {
                X509CRL crl = LatestCRL.get();
                System.out.println("crl type=" + crl.getType() + '\n' + "issuer distinguished name = " + crl.getIssuerX500Principal() + '\n'
                        + "signature algorithm = " + crl.getSigAlgName() + '\n');
                if (crl.getRevokedCertificates() != null && !crl.getRevokedCertificates().isEmpty()) {
                    System.out.println("Certificates s/n, revocation date, revocation reason = " + crl.getRevokedCertificates().stream()
                            .map(crlEntry -> Arrays
                                    .asList(crlEntry.getSerialNumber(), crlEntry.getRevocationDate(), crlEntry.getRevocationReason()))
                            .collect(toList()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void importSuperadmin() {
        System.out.println("Imports superadmin client certificate and private key");
        System.out.println("Usage: importSuperadmin <pkcs#12 file> <password> <alias>");
    }

    public void importSuperadmin(String pkcs12Name, String pkcs12Password, String pkcs12Alias) {
        String name = Optional.of(pkcs12Name).orElseThrow(() -> new IllegalArgumentException("Specify valid file name"));
        String password = Optional.of(pkcs12Password).orElseThrow(() -> new IllegalArgumentException("Specify valid password"));
        String alias = Optional.of(pkcs12Alias).orElseThrow(() -> new IllegalArgumentException("Specify valid alias"));

        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext();
             FileInputStream inputStream = new FileInputStream(name)) {
            KeyStore pkcs12 = KeyStore.getInstance("pkcs12");
            pkcs12.load(inputStream, password.toCharArray());
            Certificate certificate = pkcs12.getCertificate(alias);
            if (certificate == null) {
                throw new IllegalArgumentException("The keystore does not contain a certificate with alias " + alias);
            }
            Key key = pkcs12.getKey(alias, pkcs12Password.toCharArray());
            if (key == null) {
                throw new IllegalArgumentException("The keystore does not contain a key with alias " + alias);
            }
            KeyType certificateType = securityManagementService.getKeyType("TLS-RSA-2048")
                    .orElseGet(() -> securityManagementService.newClientCertificateType("TLS-RSA-2048", "SHA256withRSA").RSA().keySize(2048).add());
            ClientCertificateWrapper clientCertificateWrapper = securityManagementService
                    .newClientCertificateWrapper(certificateType, "DataVault").alias(alias).add();
            clientCertificateWrapper.setCertificate((X509Certificate) certificate, Optional.empty());
            PlaintextPrivateKeyWrapper privateKeyWrapper = (PlaintextPrivateKeyWrapper) clientCertificateWrapper.getPrivateKeyWrapper();
            privateKeyWrapper.setPrivateKey((PrivateKey) key);
            privateKeyWrapper.save();
            context.commit();
        } catch (KeyStoreException | UnrecoverableKeyException | CertificateException | NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
    }

    public void keypairs() {
        List<List<?>> lists = securityManagementService.findAllKeypairs()
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
        KeyType keyType = securityManagementService.getKeyType(keyTypeId)
                .orElseThrow(() -> new IllegalArgumentException("No key type with id " + keyTypeId));

        try (TransactionContext context = transactionService.getContext()) {
            KeypairWrapper keypairWrapper = securityManagementService.newKeypairWrapper(alias, keyType, "DataVault");
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
            securityManagementService.findKeypairWrapper(keyPairId)
                    .orElseThrow(() -> new IllegalArgumentException("No keypair with id " + keyPairId))
                    .delete();
            context.commit();
        }
    }

    public void exportPublicKey() {
        System.out.println("Exports the public key (x509 encoded) to file");
        System.out.println("usage: importPublicKey <alias>");
    }

    public void exportPublicKey(String alias) throws IOException {
        KeypairWrapper keypairWrapper = securityManagementService.findKeypairWrapper(alias)
                .orElseThrow(() -> new IllegalArgumentException("No such keypair"));
        FileOutputStream fileOutputStream = null;
        try {
            String filename = alias + ".der";
            fileOutputStream = new FileOutputStream(filename);
            fileOutputStream.write(keypairWrapper.getPublicKey().get().getEncoded());
            fileOutputStream.flush();
            System.out.println("Wrote " + filename);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }

    public void importPublicKey() {
        System.out.println("Imports an existing public key (x509 encoded) into a new keypair wrapper");
        System.out.println("usage: importPublicKey <alias> <keyTypeId> <file>");
    }

    public void importPublicKey(String alias, Long keyTypeId, String file) {
        try(FileInputStream fileInputStream = new FileInputStream(file)) {
            threadPrincipalService.set(() -> "Console");
            KeyType keyType = securityManagementService.getKeyType(keyTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("No key type with id " + keyTypeId));
            try (TransactionContext context = transactionService.getContext()) {
                KeypairWrapper keypairWrapper = securityManagementService.newKeypairWrapper(alias, keyType, "DataVault");
                byte[] bytes = ByteStreams.toByteArray(fileInputStream);
                keypairWrapper.setPublicKey(bytes);
                keypairWrapper.save();
                context.commit();
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException("No such file: " + file);
            } catch (IOException e) {
                throw new IllegalArgumentException("Error reading file: " + file + " : " + e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void importKeypair() {
        System.out.println("Imports an existing keypair (pkcs8) into a new keypair wrapper");
        System.out.println("usage: importKeypair <alias> <keyTypeId> <private key file (DER)>");
    }

    public void importKeypair(String alias, Long keyTypeId, String privateKeyFile) {
        try(FileInputStream fileInputStream = new FileInputStream(privateKeyFile)) {
            threadPrincipalService.set(() -> "Console");
            KeyType keyType = securityManagementService.getKeyType(keyTypeId)
                    .orElseThrow(() -> new IllegalArgumentException("No key type with id " + keyTypeId));
            try (TransactionContext context = transactionService.getContext()) {
                KeypairWrapper keypairWrapper = securityManagementService.newKeypairWrapper(alias, keyType, "DataVault");

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
                throw new IllegalArgumentException("No such file: " + privateKeyFile);
            } catch (IOException e) {
                throw new IllegalArgumentException("Error reading file: " + privateKeyFile + " : " + e);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalArgumentException("No such algorithm: " + e);
            } catch (InvalidKeySpecException e) {
                throw new IllegalArgumentException("Invalid key spec: " + e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void printTrustedCertificates() {
        List<List<?>> collect = new ArrayList<>();
        List<TrustStore> trustStores = securityManagementService.getAllTrustStores();
        for (TrustStore trustStore : trustStores) {
            collect.clear();
            collect.add(0, Arrays.asList(trustStore.getName()));
            List<String> certs = trustStore.getCertificates()
                    .stream()
                    .map(trustedCertificate -> "Alias: " + trustedCertificate.getAlias() + " s/n: " + trustedCertificate.getCertificate().get().getSerialNumber())
                    .collect(toList());
            collect.add(1, certs);
            MYSQL_PRINT.printTable(collect);
        }
    }

    public void signFile() {
        System.out.println("Signs a given file with a given certificate.");
        System.out.println("usage: signFile <alias> <path_to_file>");
        System.out.println("The signed content is exported as '<path_to_file>.signed'.");
    }

    public void signFile(String alias, String path) {
        CertificateWrapper certificateWrapper = securityManagementService.findCertificateWrapper(alias)
                .orElseThrow(() -> new IllegalArgumentException("No such certificate: " + alias));
        byte[] bytes;
        try (InputStream input = new FileInputStream(path)) {
            bytes = ByteStreams.toByteArray(input);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException("No such file: " + path);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading file: " + path + " : " + e);
        }
        byte[] signature = CertificateExportProcessor.getSignature(certificateWrapper, bytes, thesaurus);
        String outputPath = path + ".signed";
        try (OutputStream output = new FileOutputStream(outputPath, true)) {
            output.write(bytes);
            output.write(signature);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error writing file: " + outputPath + " : " + e);
        }
    }
}

