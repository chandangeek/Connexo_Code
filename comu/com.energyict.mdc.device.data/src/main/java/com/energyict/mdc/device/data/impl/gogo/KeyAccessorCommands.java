/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data.impl.gogo;

import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.PlaintextPrivateKeyWrapper;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.gogo.MysqlPrint;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.CertificateAccessor;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.KeyAccessor;

import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 1/31/17.
 */
@Component(name = "KeyAccessorCommands",
        service = KeyAccessorCommands.class,
        property = {
                "osgi.command.scope=ka",
                "osgi.command.function=keyAccessors",
                "osgi.command.function=importCertificateWithKey",
                "osgi.command.function=generateCSR",
                "osgi.command.function=truststores",
                "osgi.command.function=createTrustStore",
                "osgi.command.function=deleteTrustStore",
                "osgi.command.function=importSymmetricKey",
                "osgi.command.function=renew",
                "osgi.command.function=swap",
                "osgi.command.function=clearTemp"

        },
        immediate = true)
public class KeyAccessorCommands {
    public static final MysqlPrint MYSQL_PRINT = new MysqlPrint();

    private DeviceConfigurationService deviceConfigurationService;
    private DeviceService deviceService;
    private PkiService pkiService;
    private TransactionService transactionService;
    private ThreadPrincipalService threadPrincipalService;

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setPkiService(PkiService pkiService) {
        this.pkiService = pkiService;
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void keyAccessors() {
        System.out.println("Usage: keyAccessors <device id>");
        System.out.println("       List all known key accessors for a certain device");
        System.out.println("e.g. : keyAccessors 123");
    }

    public void keyAccessors(long deviceId) throws InvalidKeyException {
        Device device = deviceService.findDeviceById(deviceId)
                .orElseThrow(() -> new RuntimeException("No such device"));
        List<List<?>> collection = new ArrayList<>();
        for (KeyAccessorType keyAccessorType: device.getDeviceType().getKeyAccessorTypes()) {
            Optional<KeyAccessor> keyAccessor = device.getKeyAccessor(keyAccessorType);
            String extraValue = "";
            if (keyAccessor.isPresent() && keyAccessor.get() instanceof CertificateAccessor) {
                CertificateAccessor certificateAccessor = (CertificateAccessor) keyAccessor.get();
                if (certificateAccessor.getActualValue() instanceof ClientCertificateWrapper) {
                    ClientCertificateWrapper actualValue = (ClientCertificateWrapper) certificateAccessor.getActualValue();
                    if (actualValue.getCertificate().isPresent()) {
                        extraValue+="X.509";
                    } else if (actualValue.getCSR().isPresent()) {
                        extraValue+="CSR";
                    }
                    PrivateKey privateKey = actualValue.getPrivateKeyWrapper().getPrivateKey();
                    if (privateKey != null) {
                        extraValue+=" + PK";
                    }
                }
            }
            collection.add(Arrays.asList(keyAccessorType.getName(),
                    keyAccessorType.getKeyType().getName(),
                    keyAccessor.isPresent() && keyAccessor.get().getActualValue()!=null ? (extraValue.isEmpty()?"Present":extraValue):"",
                    keyAccessor.isPresent() && keyAccessor.get().getTempValue().isPresent() ? "Present":""
            ));
        }

        MYSQL_PRINT.printTableWithHeader(Arrays.asList("Key accessor type", "Key type","Current value", "Temp value"), collection);
    }

    public void importCertificateWithKey() {
        System.out.println("Usage: importCertificateWithKey <device id> <cert accessor type name> <pkcs#12 file>  <password> <alias>");
        System.out.println("e.g. : importCertificateWithKey 1 \"TLS SUITE 2\" tls.pkcs12 foo123 mycert");
    }

    public void importCertificateWithKey(long deviceId, String certKatName, String pkcs12Name, String pkcs12Password, String alias)
            throws KeyStoreException, IOException, CertificateException,
                        NoSuchAlgorithmException, UnrecoverableKeyException {

        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            Device device = deviceService.findDeviceById(deviceId)
                    .orElseThrow(() -> new RuntimeException("No such device"));
            KeyAccessorType certKeyAccessorType = device.getDeviceType()
                    .getKeyAccessorTypes()
                    .stream()
                    .filter(kat -> kat.getName().equals(certKatName))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("No such key accessor type on the device type: "+certKatName));

            KeyStore pkcs12 = KeyStore.getInstance("pkcs12");
            pkcs12.load(new FileInputStream(pkcs12Name), pkcs12Password.toCharArray());
            Certificate certificate = pkcs12.getCertificate(alias);
            if (certificate==null) {
                throw new RuntimeException("The keystore does not contain a certificate with alias "+alias);
            }
            Key key = pkcs12.getKey(alias, pkcs12Password.toCharArray());
            if (key==null) {
                throw new RuntimeException("The keystore does not contain a key with alias "+alias);
            }
            ClientCertificateWrapper clientCertificateWrapper = pkiService.newClientCertificateWrapper(certKeyAccessorType);
            clientCertificateWrapper.setAlias(alias);
            clientCertificateWrapper.setCertificate((X509Certificate) certificate);
            clientCertificateWrapper.setCertificate((X509Certificate) certificate);
            PlaintextPrivateKeyWrapper privateKeyWrapper = (PlaintextPrivateKeyWrapper) clientCertificateWrapper.getPrivateKeyWrapper();
            privateKeyWrapper.setPrivateKey((PrivateKey) key);
            privateKeyWrapper.save();

            KeyAccessor keyAccessor = device.getKeyAccessor(certKeyAccessorType)
                    .orElseGet(()->device.newKeyAccessor(certKeyAccessorType));
            keyAccessor.setActualValue(clientCertificateWrapper);
            keyAccessor.save();
            context.commit();
        }
    }

    public void importSymmetricKey() {
        System.out.println("Usage: importSymmetricKey <device id> <key accessor type name> <keystore file>  <key store password> <alias>");
        System.out.println("e.g. : importSymmetricKey 1 MK aes128.jks foo123 mk");
    }

    public void importSymmetricKey(long deviceId, String keyAccessTypeName, String keyStoreName, String keyStorePassword, String alias)
            throws KeyStoreException, IOException, CertificateException,
                        NoSuchAlgorithmException, UnrecoverableKeyException {

        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            Device device = deviceService.findDeviceById(deviceId)
                    .orElseThrow(() -> new RuntimeException("No such device"));
            KeyAccessorType keyAccessorType = device.getDeviceType()
                    .getKeyAccessorTypes()
                    .stream()
                    .filter(kat -> kat.getName().equals(keyAccessTypeName))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("No such key accessor type on the device type: "+keyAccessTypeName));

            KeyStore keyStore = KeyStore.getInstance("JCEKS");
            keyStore.load(new FileInputStream(keyStoreName), keyStorePassword.toCharArray());
            Key key = keyStore.getKey(alias, keyStorePassword.toCharArray());
            if (key==null) {
                throw new RuntimeException("The keystore does not contain a key with alias "+alias);
            }
            SymmetricKeyWrapper symmetricKeyWrapper = pkiService.newSymmetricKeyWrapper(keyAccessorType);
            ((PlaintextSymmetricKey)symmetricKeyWrapper).setKey(new SecretKeySpec(key.getEncoded(), key.getAlgorithm()));

            KeyAccessor keyAccessor = device.getKeyAccessor(keyAccessorType)
                    .orElseGet(()->device.newKeyAccessor(keyAccessorType));
            keyAccessor.setActualValue(symmetricKeyWrapper);
            keyAccessor.save();
            context.commit();
        }
    }

    public void generateCSR() {
        System.out.println("Usage: generateCSR <device id> <cert accessor type name> <alias> <CommonName>");
        System.out.println("e.g. : generateCSR 1 \"TLS SUITE 1\" comserver \"Comserver TLS\"");
    }

    public void generateCSR(long deviceId, String certKatName, String alias, String cn) throws
            NoSuchAlgorithmException {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            Device device = deviceService.findDeviceById(deviceId)
                    .orElseThrow(() -> new RuntimeException("No such device"));
            KeyAccessorType certKeyAccessorType = device.getDeviceType()
                    .getKeyAccessorTypes()
                    .stream()
                    .filter(kat -> kat.getName().equals(certKatName))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("No such key accessor type on the device type: " + certKatName));

            ClientCertificateWrapper clientCertificateWrapper = pkiService.newClientCertificateWrapper(certKeyAccessorType);
            clientCertificateWrapper.getPrivateKeyWrapper().generateValue();

            X500NameBuilder x500NameBuilder = new X500NameBuilder();
            x500NameBuilder.addRDN(BCStyle.CN, cn);
            PKCS10CertificationRequest pkcs10CertificationRequest = clientCertificateWrapper.getPrivateKeyWrapper()
                    .generateCSR(x500NameBuilder.build(), certKeyAccessorType.getKeyType().getSignatureAlgorithm());
            clientCertificateWrapper.setCSR(pkcs10CertificationRequest);
            clientCertificateWrapper.save();
            context.commit();
        } catch (Exception e) {
            System.err.println("Failed to create CSR: "+e.getMessage());
        }
    }

    public void renew() {
        System.out.println("Trigger renew for a key accessor type on a device");
        System.out.println("e.g.: renew 1001 MK");
    }

    public void renew(long deviceId, String keyAccessorTypeName) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            Device device = deviceService.findDeviceById(deviceId)
                    .orElseThrow(() -> new RuntimeException("No such device"));
            KeyAccessorType keyAccessorType = device.getDeviceType()
                    .getKeyAccessorTypes()
                    .stream()
                    .filter(kat -> kat.getName().equals(keyAccessorTypeName))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("No such key accessor type on the device type: " + keyAccessorTypeName));

            KeyAccessor keyAccessor = device.getKeyAccessor(keyAccessorType)
                    .orElseThrow(() -> new RuntimeException("No key accessor for key accessor type " + keyAccessorTypeName));

            keyAccessor.renew();

            context.commit();
        } catch (Exception e) {
            System.err.println("Failed to renew value: "+e.getMessage());
        }
    }

    public void swap() {
        System.out.println("Swap actual and temp values on a device for a key accessor type");
        System.out.println("e.g.: swap 1001 MK");
    }

    public void swap(long deviceId, String keyAccessorTypeName) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            Device device = deviceService.findDeviceById(deviceId)
                    .orElseThrow(() -> new RuntimeException("No such device"));
            KeyAccessorType keyAccessorType = device.getDeviceType()
                    .getKeyAccessorTypes()
                    .stream()
                    .filter(kat -> kat.getName().equals(keyAccessorTypeName))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("No such key accessor type on the device type: " + keyAccessorTypeName));

            KeyAccessor keyAccessor = device.getKeyAccessor(keyAccessorType)
                    .orElseThrow(() -> new RuntimeException("No key accessor for key accessor type " + keyAccessorTypeName));

            keyAccessor.swapValues();

            context.commit();
        } catch (Exception e) {
            System.err.println("Failed to swap values: "+e.getMessage());
        }
    }

    public void clearTemp() {
        System.out.println("Clears the temp value on a device for a key accessor type");
        System.out.println("e.g.: clearTemp 1001 MK");
    }

    public void clearTemp(long deviceId, String keyAccessorTypeName) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            Device device = deviceService.findDeviceById(deviceId)
                    .orElseThrow(() -> new RuntimeException("No such device"));
            KeyAccessorType keyAccessorType = device.getDeviceType()
                    .getKeyAccessorTypes()
                    .stream()
                    .filter(kat -> kat.getName().equals(keyAccessorTypeName))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("No such key accessor type on the device type: " + keyAccessorTypeName));

            KeyAccessor keyAccessor = device.getKeyAccessor(keyAccessorType)
                    .orElseThrow(() -> new RuntimeException("No key accessor for key accessor type " + keyAccessorTypeName));

            keyAccessor.clearTempValue();

            context.commit();
        } catch (Exception e) {
            System.err.println("Failed to clear temp value: "+e.getMessage());
        }
    }

    public void trustStores() {
        List<List<?>> lists = pkiService.getAllTrustStores()
                .stream()
                .map(ts -> Arrays.asList(ts.getName(), ts.getCertificates().size()))
                .collect(toList());
        MYSQL_PRINT.printTableWithHeader(Arrays.asList("Name", "# Certificates"), lists);
    }

    public void createTrustStore() {
        System.out.println("Usage: createTrustStore <name>");
        System.out.println("E.g. : createTrustStore \"DLMS main\"");
    }

    public void createTrustStore(String name) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            pkiService.newTrustStore(name).description("Created by GoGo command").add();
            context.commit();
        }
    }

    public void deleteTrustStore() {
        System.out.println("Usage: deleteTrustStore <name>");
        System.out.println("E.g. : deleteTrustStore \"DLMS main\"");
    }

    public void deleteTrustStore(String name) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            TrustStore trustStore = pkiService.findTrustStore(name)
                    .orElseThrow(() -> new RuntimeException("No such trust store"));
            trustStore.delete();
        }
    }
}
