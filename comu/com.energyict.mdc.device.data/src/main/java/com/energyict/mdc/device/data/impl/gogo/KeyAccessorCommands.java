/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data.impl.gogo;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PassphraseWrapper;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.PlaintextPrivateKeyWrapper;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.gogo.MysqlPrint;
import com.energyict.mdc.device.data.CertificateAccessor;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.KeyAccessor;
import com.energyict.mdc.device.data.impl.pki.SymmetricKeyAccessorImpl;

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
                "osgi.command.function=clearTemp",
                "osgi.command.function=createEventType",
                "osgi.command.function=setAccessorPassword"

        },
        immediate = true)
public class KeyAccessorCommands {
    public static final MysqlPrint MYSQL_PRINT = new MysqlPrint();

    private volatile DeviceService deviceService;
    private volatile PkiService pkiService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile EventService eventService;

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
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

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public void keyAccessors() {
        System.out.println("Usage: keyAccessors <device name>");
        System.out.println("       List all known key accessors for a certain device with information about their values");
        System.out.println("e.g. : keyAccessors ABCD0001");
    }

    public void keyAccessors(String deviceName) throws InvalidKeyException {
        Device device = deviceService.findDeviceByName(deviceName)
                .orElseThrow(() -> new RuntimeException("No such device"));
        List<List<?>> collection = new ArrayList<>();
        for (KeyAccessorType keyAccessorType: device.getDeviceType().getKeyAccessorTypes()) {
            Optional<KeyAccessor> keyAccessor = device.getKeyAccessor(keyAccessorType);
            String actualExtraValue = "";
            String tempExtraValue = "";
            if (keyAccessor.isPresent()) {
                if (keyAccessor.get() instanceof CertificateAccessor) {
                    CertificateAccessor certificateAccessor = (CertificateAccessor) keyAccessor.get();
                    if (certificateAccessor.getActualValue() instanceof ClientCertificateWrapper) {
                        ClientCertificateWrapper actualValue = (ClientCertificateWrapper) certificateAccessor.getActualValue();
                        actualExtraValue = toString(actualExtraValue, actualValue);
                    }
                    if (certificateAccessor.getTempValue().isPresent() && certificateAccessor.getTempValue().get() instanceof ClientCertificateWrapper) {
                        ClientCertificateWrapper tempValue = (ClientCertificateWrapper) certificateAccessor.getTempValue().get();
                        tempExtraValue = toString(tempExtraValue, tempValue);
                    }
                } else if (keyAccessor.get() instanceof SymmetricKeyAccessorImpl) {
                    SymmetricKeyAccessorImpl symmetricKeyAccessor = (SymmetricKeyAccessorImpl) keyAccessor.get();
                    if (symmetricKeyAccessor.getActualValue() instanceof PlaintextSymmetricKey) {
                        PlaintextSymmetricKey actualValue = (PlaintextSymmetricKey) symmetricKeyAccessor.getActualValue();
                        if (actualValue.getKey().isPresent()) {
                            actualExtraValue += actualValue.getKey().get().getAlgorithm();
                        }
                    }
                    if (symmetricKeyAccessor.getTempValue().isPresent() && symmetricKeyAccessor.getTempValue().get() instanceof PlaintextSymmetricKey) {
                        PlaintextSymmetricKey tempValue = (PlaintextSymmetricKey) symmetricKeyAccessor.getTempValue().get();
                        if (tempValue.getKey().isPresent()) {
                            tempExtraValue += tempValue.getKey().get().getAlgorithm();
                        }
                    }
                }
            }
            collection.add(Arrays.asList(keyAccessorType.getName(),
                    keyAccessorType.getKeyType().getName(),
                    keyAccessor.isPresent() && keyAccessor.get().getActualValue()!=null ? (actualExtraValue.isEmpty()?"Accessor present":actualExtraValue):"",
                    keyAccessor.isPresent() && keyAccessor.get().getTempValue().isPresent() ? (tempExtraValue.isEmpty()?"Accessor present":tempExtraValue):""
            ));
        }

        MYSQL_PRINT.printTableWithHeader(Arrays.asList("Key accessor type", "Key type","Current value", "Temp value"), collection);
    }

    private String toString(String extraValue, ClientCertificateWrapper value) throws InvalidKeyException {
        if (value.getCertificate().isPresent()) {
            extraValue += "X.509";
        } else if (value.getCSR().isPresent()) {
            extraValue += "CSR";
        }
        PrivateKey privateKey = value.getPrivateKeyWrapper().getPrivateKey();
        if (privateKey != null) {
            extraValue += " + PK";
        }
        return extraValue;
    }

    public void importCertificateWithKey() {
        System.out.println("Usage: importCertificateWithKey <device name> <cert accessor type name> <pkcs#12 file>  <password> <alias>");
        System.out.println("e.g. : importCertificateWithKey ABC123 \"TLS SUITE 2\" tls.pkcs12 foo123 mycert");
    }

    public void importCertificateWithKey(String deviceName, String certKatName, String pkcs12Name, String pkcs12Password, String alias)
            throws KeyStoreException, IOException, CertificateException,
                        NoSuchAlgorithmException, UnrecoverableKeyException {

        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            Device device = deviceService.findDeviceByName(deviceName)
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
            ClientCertificateWrapper clientCertificateWrapper = pkiService.newClientCertificateWrapper(certKeyAccessorType.getKeyType(), certKeyAccessorType.getKeyEncryptionMethod()).alias(alias).add();
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

    public void setAccessorPassword() {
        System.out.println("Usage: setAccessorPassword <device name> <key accessor type name> <cleartext password>");
        System.out.println("e.g. : setAccessorPassword Device0001 DlmsPhrase ABCD1234");
    }

    public void setAccessorPassword(String deviceName, String keyAccessorTypeName, String cleartextPassword) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            Device device = deviceService.findDeviceByName(deviceName)
                    .orElseThrow(() -> new RuntimeException("No such device"));
            KeyAccessorType keyAccessorType = device.getDeviceType()
                    .getKeyAccessorTypes()
                    .stream()
                    .filter(kat -> kat.getName().equals(keyAccessorTypeName))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("No such key accessor type on the device type: " + keyAccessorTypeName));

            Optional<KeyAccessor> keyAccessor = device.getKeyAccessor(keyAccessorType);
            if (keyAccessor.isPresent()) {
                PlaintextPassphrase actualValue = (PlaintextPassphrase) keyAccessor.get().getActualValue();
                actualValue.setPassphrase(cleartextPassword);
            } else {
                PassphraseWrapper passphraseWrapper = pkiService.newPassphraseWrapper(keyAccessorType);
                ((PlaintextPassphrase)passphraseWrapper).setPassphrase(cleartextPassword);
                KeyAccessor newKeyAccessor = device.newKeyAccessor(keyAccessorType);
                newKeyAccessor.setActualValue(passphraseWrapper);
                newKeyAccessor.save();
            }
            context.commit();
        }
    }

    public void importSymmetricKey() {
        System.out.println("Usage: importSymmetricKey <device name> <key accessor type name> <keystore file>  <key store password> <alias>");
        System.out.println("e.g. : importSymmetricKey A1BC MK aes128.jks foo123 mk");
    }

    public void importSymmetricKey(String deviceName, String keyAccessTypeName, String keyStoreName, String keyStorePassword, String alias)
            throws KeyStoreException, IOException, CertificateException,
                        NoSuchAlgorithmException, UnrecoverableKeyException {

        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            Device device = deviceService.findDeviceByName(deviceName)
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
            Optional<KeyAccessor> keyAccessorOptional = device.getKeyAccessor(keyAccessorType);
            KeyAccessor<SymmetricKeyWrapper> keyAccessor;
            if (keyAccessorOptional.isPresent()) {
                if (keyAccessorOptional.get().getActualValue()!=null) {
                    ((SymmetricKeyWrapper)keyAccessorOptional.get().getActualValue()).delete();
                }
                keyAccessor = keyAccessorOptional.get();
            } else {
                keyAccessor = device.newKeyAccessor(keyAccessorType);
            }
            keyAccessor.setActualValue(symmetricKeyWrapper);
            keyAccessor.save();
            context.commit();
        }
    }

    public void generateCSR() {
        System.out.println("Usage: generateCSR <device name> <cert accessor type name> <alias> <CommonName>");
        System.out.println("e.g. : generateCSR AB1 \"TLS SUITE 1\" comserver \"Comserver TLS\"");
    }

    public void generateCSR(String deviceName, String certKatName, String alias, String cn) throws
            NoSuchAlgorithmException {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            Device device = deviceService.findDeviceByName(deviceName)
                    .orElseThrow(() -> new RuntimeException("No such device"));
            KeyAccessorType certKeyAccessorType = device.getDeviceType()
                    .getKeyAccessorTypes()
                    .stream()
                    .filter(kat -> kat.getName().equals(certKatName))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("No such key accessor type on the device type: " + certKatName));

            ClientCertificateWrapper clientCertificateWrapper = pkiService.newClientCertificateWrapper(certKeyAccessorType.getKeyType(), certKeyAccessorType.getKeyEncryptionMethod()).alias(alias).add();
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
        System.out.println("usage: renew <device name> <key acccessor type name>");
        System.out.println("e.g.: renew 1001 MK");
    }

    public void renew(String deviceName, String keyAccessorTypeName) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            Device device = deviceService.findDeviceByName(deviceName)
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
        System.out.println("Usage: swap <device name> <key accessor type name>");
        System.out.println("e.g. : swap 1001 MK");
    }

    public void swap(String deviceName, String keyAccessorTypeName) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            Device device = deviceService.findDeviceByName(deviceName)
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
        System.out.println("usage: clearTemp <device name> <key accessor type name>");
        System.out.println("e.g. : clearTemp 1001 MK");
    }

    public void clearTemp(String deviceName, String keyAccessorTypeName) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            Device device = deviceService.findDeviceByName(deviceName)
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
            context.commit();
        }
    }

    public void createEventType() {
        System.out.println("Usage: createEventType <name> <componentName> <topic>");
        System.out.println("E.g. : createEventType TRUSTSTORE_VALIDATE_DELETE PKI com/elster/jupiter/pki/truststore/DELETED");
    }

    public void createEventType(String name, String componentName, String topic) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            eventService.buildEventTypeWithTopic(topic)
                    .name(name)
                    .component(componentName)
                    .category("Crud")
                    .scope("System")
                    .withProperty("id", ValueType.LONG, "id")
                    .create();
            context.commit();
        }
    }
}
