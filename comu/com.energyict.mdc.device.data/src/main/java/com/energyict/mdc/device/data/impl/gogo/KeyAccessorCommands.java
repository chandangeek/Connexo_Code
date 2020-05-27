/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data.impl.gogo;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.events.ValueType;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.PassphraseWrapper;
import com.elster.jupiter.pki.PlaintextPassphrase;
import com.elster.jupiter.pki.PlaintextPrivateKeyWrapper;
import com.elster.jupiter.pki.PlaintextSymmetricKey;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.SecurityValueWrapper;
import com.elster.jupiter.pki.SymmetricKeyWrapper;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.gogo.MysqlPrint;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.device.data.SecurityAccessor;
import com.energyict.mdc.device.data.CertificateAccessor;
import com.energyict.mdc.device.data.CertificateRenewalService;
import com.energyict.mdc.device.data.CrlRequestService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.KeyRenewalService;
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
                "osgi.command.function=importDeviceCertificateWithKey",
                "osgi.command.function=importDefaultCertificateWithKey",
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
                "osgi.command.function=setAccessorPassword",
                "osgi.command.function=runCertificateRenewalTask",
                "osgi.command.function=runKeyRenewalTask",
                "osgi.command.function=runCrlRequestTask"
        },
        immediate = true)
public class KeyAccessorCommands {
    public static final MysqlPrint MYSQL_PRINT = new MysqlPrint();

    private volatile DeviceService deviceService;
    private volatile SecurityManagementService securityManagementService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile EventService eventService;
    private volatile CertificateRenewalService certificateRenewalService;
    private volatile KeyRenewalService keyRenewalService;
    private volatile CrlRequestService crlRequestService;

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
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

    @Reference
    public void setCertificateRenewalService(CertificateRenewalService certificateRenewalService) {
        this.certificateRenewalService = certificateRenewalService;
    }

    @Reference
    public void setKeyRenewalService(KeyRenewalService keyRenewalService) {
        this.keyRenewalService = keyRenewalService;
    }

    @Reference
    public void setCrlRequestService(CrlRequestService crlRequestService) {
        this.crlRequestService = crlRequestService;
    }

    public void keyAccessors() {
        System.out.println("Usage: keyAccessors <device name>");
        System.out.println("       List all known security accessors for a certain device with information about their values");
        System.out.println("e.g. : keyAccessors ABCD0001");
    }

    public void keyAccessors(String deviceName) throws InvalidKeyException {
        Device device = deviceService.findDeviceByName(deviceName)
                .orElseThrow(() -> new RuntimeException("No such device"));
        List<List<?>> collection = new ArrayList<>();
        for (SecurityAccessorType securityAccessorType : device.getDeviceType().getSecurityAccessorTypes()) {
            Optional<SecurityAccessor> keyAccessor = device.getSecurityAccessor(securityAccessorType);
            String actualExtraValue = "";
            String tempExtraValue = "";
            if (keyAccessor.isPresent()) {
                if (keyAccessor.get() instanceof CertificateAccessor) {
                    CertificateAccessor certificateAccessor = (CertificateAccessor) keyAccessor.get();
                    if (certificateAccessor.getActualPassphraseWrapperReference().isPresent() && certificateAccessor.getActualPassphraseWrapperReference().get() instanceof ClientCertificateWrapper) {
                        ClientCertificateWrapper actualValue = (ClientCertificateWrapper) certificateAccessor.getActualPassphraseWrapperReference().get();
                        actualExtraValue = toString(actualExtraValue, actualValue);
                    }
                    if (certificateAccessor.getTempValue().isPresent() && certificateAccessor.getTempValue().get() instanceof ClientCertificateWrapper) {
                        ClientCertificateWrapper tempValue = (ClientCertificateWrapper) certificateAccessor.getTempValue().get();
                        tempExtraValue = toString(tempExtraValue, tempValue);
                    }
                } else if (keyAccessor.get() instanceof SymmetricKeyAccessorImpl) {
                    SymmetricKeyAccessorImpl symmetricKeyAccessor = (SymmetricKeyAccessorImpl) keyAccessor.get();
                    if (symmetricKeyAccessor.getActualPassphraseWrapperReference().isPresent() && symmetricKeyAccessor.getActualPassphraseWrapperReference().get() instanceof PlaintextSymmetricKey) {
                        PlaintextSymmetricKey actualValue = (PlaintextSymmetricKey) symmetricKeyAccessor.getActualPassphraseWrapperReference().get();
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
            collection.add(Arrays.asList(securityAccessorType.getName(),
                    securityAccessorType.getKeyType().getName(),
                    keyAccessor.isPresent() && keyAccessor.get().getActualPassphraseWrapperReference().isPresent() ? (actualExtraValue.isEmpty() ? "Accessor present" : actualExtraValue) : "",
                    keyAccessor.isPresent() && keyAccessor.get().getTempValue().isPresent() ? (tempExtraValue.isEmpty() ? "Accessor present" : tempExtraValue) : "",
                    securityAccessorType.isManagedCentrally() ? "Yes" : "No"
            ));
        }

        MYSQL_PRINT.printTableWithHeader(Arrays.asList("Security accessor type", "Key type", "Current value", "Temp value", "Managed centrally"), collection);
    }

    private String toString(String extraValue, ClientCertificateWrapper value) throws InvalidKeyException {
        if (value.getCertificate().isPresent()) {
            extraValue += "X.509";
        } else if (value.getCSR().isPresent()) {
            extraValue += "CSR";
        }
        if (value.getPrivateKeyWrapper().getPrivateKey().isPresent()) {
            extraValue += " + PK";
        }
        return extraValue;
    }

    public void importCertificateWithKey() {
        System.out.println("Usage: importCertificateWithKey <certstore alias> <pkcs#12 file>  <password> <alias>");
        System.out.println("e.g. : importCertificateWithKey 'MDC' tls.pkcs12 foo123 mycert");
    }

    public void importCertificateWithKey(String certificateAlias, String pkcs12Name, String pkcs12Password, String alias)
            throws KeyStoreException, IOException, CertificateException,
            NoSuchAlgorithmException, UnrecoverableKeyException {

        threadPrincipalService.set(() -> "Console");
        //TODO refactor to avoid code duplication
        try (TransactionContext context = transactionService.getContext();
             FileInputStream pksInputStream = new FileInputStream(pkcs12Name)) {
            KeyType keyType = securityManagementService.getKeyType("RSA 1024")
                    .orElseThrow(() -> new RuntimeException("No such key type: RSA 1024"));

            KeyStore pkcs12 = KeyStore.getInstance("pkcs12");
            pkcs12.load(pksInputStream, pkcs12Password.toCharArray());
            Certificate certificate = pkcs12.getCertificate(alias);
            if (certificate == null) {
                throw new RuntimeException("The keystore does not contain a certificate with alias " + alias);
            }
            Key key = pkcs12.getKey(alias, pkcs12Password.toCharArray());
            if (key == null) {
                throw new RuntimeException("The keystore does not contain a key with alias " + alias);
            }
            ClientCertificateWrapper clientCertificateWrapper = securityManagementService.newClientCertificateWrapper(keyType, "DataVault")
                    .alias(certificateAlias).add();
            clientCertificateWrapper.setCertificate((X509Certificate) certificate, Optional.empty());
            PlaintextPrivateKeyWrapper privateKeyWrapper = (PlaintextPrivateKeyWrapper) clientCertificateWrapper.getPrivateKeyWrapper();
            privateKeyWrapper.setPrivateKey((PrivateKey) key);
            privateKeyWrapper.save();

            context.commit();
        }
    }

    public void importDefaultCertificateWithKey() {
        System.out.println("Usage: importDefaultCertificateWithKey <cert accessor type name> <pkcs#12 file>  <password> <alias>");
        System.out.println("e.g. : importDefaultCertificateWithKey \"TLS SUITE 2\" tls.pkcs12 foo123 mycert");
    }

    public void importDefaultCertificateWithKey(String certKatName, String pkcs12Name, String pkcs12Password, String alias)
            throws KeyStoreException, IOException, CertificateException,
            NoSuchAlgorithmException, UnrecoverableKeyException {

        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext();
             FileInputStream pksInputStream = new FileInputStream(pkcs12Name)) {
            SecurityAccessorType certSecurityAccessorType = securityManagementService
                    .findSecurityAccessorTypeByName(certKatName)
                    .orElseThrow(() -> new RuntimeException("No such security accessor type: " + certKatName));

            KeyStore pkcs12 = KeyStore.getInstance("pkcs12");
            pkcs12.load(pksInputStream, pkcs12Password.toCharArray());
            Certificate certificate = pkcs12.getCertificate(alias);
            if (certificate == null) {
                throw new RuntimeException("The keystore does not contain a certificate with alias " + alias);
            }
            Key key = pkcs12.getKey(alias, pkcs12Password.toCharArray());
            if (key == null) {
                throw new RuntimeException("The keystore does not contain a key with alias " + alias);
            }
            ClientCertificateWrapper clientCertificateWrapper = securityManagementService.newClientCertificateWrapper(certSecurityAccessorType
                    .getKeyType(), certSecurityAccessorType.getKeyEncryptionMethod()).alias(alias).add();
            clientCertificateWrapper.setCertificate((X509Certificate) certificate, Optional.empty());
            PlaintextPrivateKeyWrapper privateKeyWrapper = (PlaintextPrivateKeyWrapper) clientCertificateWrapper.getPrivateKeyWrapper();
            privateKeyWrapper.setPrivateKey((PrivateKey) key);
            privateKeyWrapper.save();

            Optional<com.elster.jupiter.pki.SecurityAccessor<CertificateWrapper>> securityAccessor = securityManagementService.getDefaultValues(certSecurityAccessorType)
                    .map(sa -> (com.elster.jupiter.pki.SecurityAccessor<CertificateWrapper>) sa);
            if (securityAccessor.isPresent()) {
                securityAccessor.get().setActualPassphraseWrapperReference(clientCertificateWrapper);
                securityAccessor.get().save();
            } else {
                securityManagementService.setDefaultValues(certSecurityAccessorType, clientCertificateWrapper, null);
            }
            context.commit();
        }
    }

    public void importDeviceCertificateWithKey() {
        System.out.println("Usage: importDeviceCertificateWithKey <device name> <cert accessor type name> <pkcs#12 file>  <password> <alias>");
        System.out.println("e.g. : importDeviceCertificateWithKey ABC123 \"TLS SUITE 2\" tls.pkcs12 foo123 mycert");
    }

    public void importDeviceCertificateWithKey(String deviceName, String certKatName, String pkcs12Name, String pkcs12Password, String alias)
            throws KeyStoreException, IOException, CertificateException,
            NoSuchAlgorithmException, UnrecoverableKeyException {

        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext();
             FileInputStream pkcsInputStream = new FileInputStream(pkcs12Name)) {
            Device device = deviceService.findDeviceByName(deviceName)
                    .orElseThrow(() -> new RuntimeException("No such device"));
            SecurityAccessorType certSecurityAccessorType = device.getDeviceType()
                    .getSecurityAccessorTypes()
                    .stream()
                    .filter(kat -> kat.getName().equals(certKatName))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("No such security accessor type on the device type: " + certKatName));

            KeyStore pkcs12 = KeyStore.getInstance("pkcs12");
            pkcs12.load(pkcsInputStream, pkcs12Password.toCharArray());
            Certificate certificate = pkcs12.getCertificate(alias);
            if (certificate == null) {
                throw new RuntimeException("The keystore does not contain a certificate with alias " + alias);
            }
            Key key = pkcs12.getKey(alias, pkcs12Password.toCharArray());
            if (key == null) {
                throw new RuntimeException("The keystore does not contain a key with alias " + alias);
            }
            ClientCertificateWrapper clientCertificateWrapper = securityManagementService.newClientCertificateWrapper(certSecurityAccessorType
                    .getKeyType(), certSecurityAccessorType.getKeyEncryptionMethod()).alias(alias).add();
            clientCertificateWrapper.setCertificate((X509Certificate) certificate, Optional.empty());
            PlaintextPrivateKeyWrapper privateKeyWrapper = (PlaintextPrivateKeyWrapper) clientCertificateWrapper.getPrivateKeyWrapper();
            privateKeyWrapper.setPrivateKey((PrivateKey) key);
            privateKeyWrapper.save();

            SecurityAccessor securityAccessor = device.getSecurityAccessor(certSecurityAccessorType)
                    .orElseGet(() -> device.newSecurityAccessor(certSecurityAccessorType));
            securityAccessor.setActualPassphraseWrapperReference(clientCertificateWrapper);
            securityAccessor.save();
            context.commit();
        }
    }

    public void setAccessorPassword() {
        System.out.println("Usage: setAccessorPassword <device name> <security accessor type name> <cleartext password>");
        System.out.println("e.g. : setAccessorPassword Device0001 DlmsPhrase ABCD1234");
    }

    public void setAccessorPassword(String deviceName, String keyAccessorTypeName, String cleartextPassword) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            Device device = deviceService.findDeviceByName(deviceName)
                    .orElseThrow(() -> new RuntimeException("No such device"));
            SecurityAccessorType securityAccessorType = device.getDeviceType()
                    .getSecurityAccessorTypes()
                    .stream()
                    .filter(kat -> kat.getName().equals(keyAccessorTypeName))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("No such security accessor type on the device type: " + keyAccessorTypeName));

            SecurityAccessor securityAccessor = device.getSecurityAccessor(securityAccessorType).orElseGet(() -> device.newSecurityAccessor(securityAccessorType));
            if (securityAccessor.getActualPassphraseWrapperReference().isPresent()) {
                PlaintextPassphrase actualValue = (PlaintextPassphrase) securityAccessor.getActualPassphraseWrapperReference().get();
                actualValue.setEncryptedPassphrase(cleartextPassword);
            } else {
                PassphraseWrapper passphraseWrapper = securityManagementService.newPassphraseWrapper(securityAccessorType);
                ((PlaintextPassphrase) passphraseWrapper).setEncryptedPassphrase(cleartextPassword);
                securityAccessor.setActualPassphraseWrapperReference(passphraseWrapper);
                securityAccessor.save();
            }
            context.commit();
        }
    }

    public void importSymmetricKey() {
        System.out.println("Usage: importSymmetricKey <device name> <security accessor type name> <keystore file>  <key store password> <alias>");
        System.out.println("e.g. : importSymmetricKey A1BC MK aes128.jks foo123 mk");
    }

    public void importSymmetricKey(String deviceName, String keyAccessTypeName, String keyStoreName, String keyStorePassword, String alias)
            throws KeyStoreException, IOException, CertificateException,
            NoSuchAlgorithmException, UnrecoverableKeyException {

        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext();
             FileInputStream fileInputStream = new FileInputStream(keyStoreName)) {
            Device device = deviceService.findDeviceByName(deviceName)
                    .orElseThrow(() -> new RuntimeException("No such device"));
            SecurityAccessorType securityAccessorType = device.getDeviceType()
                    .getSecurityAccessorTypes()
                    .stream()
                    .filter(kat -> kat.getName().equals(keyAccessTypeName))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("No such security accessor type on the device type: " + keyAccessTypeName));

            KeyStore keyStore = KeyStore.getInstance("JCEKS");
            keyStore.load(fileInputStream, keyStorePassword.toCharArray());
            Key key = keyStore.getKey(alias, keyStorePassword.toCharArray());
            if (key == null) {
                throw new RuntimeException("The keystore does not contain a key with alias " + alias);
            }

            SymmetricKeyWrapper symmetricKeyWrapper = securityManagementService.newSymmetricKeyWrapper(securityAccessorType);
            ((PlaintextSymmetricKey) symmetricKeyWrapper).setKey(new SecretKeySpec(key.getEncoded(), key.getAlgorithm()));
            Optional<SecurityAccessor> keyAccessorOptional = device.getSecurityAccessor(securityAccessorType);
            SecurityAccessor<SymmetricKeyWrapper> securityAccessor;
            if (keyAccessorOptional.isPresent()) {
                if (keyAccessorOptional.get().getActualPassphraseWrapperReference().isPresent()) {
                    ((SymmetricKeyWrapper) keyAccessorOptional.get().getActualPassphraseWrapperReference().get()).delete();
                }
                securityAccessor = keyAccessorOptional.get();
            } else {
                securityAccessor = device.newSecurityAccessor(securityAccessorType);
            }
            securityAccessor.setActualPassphraseWrapperReference(symmetricKeyWrapper);
            securityAccessor.save();
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
            SecurityAccessorType certSecurityAccessorType = device.getDeviceType()
                    .getSecurityAccessorTypes()
                    .stream()
                    .filter(kat -> kat.getName().equals(certKatName))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("No such security accessor type on the device type: " + certKatName));

            ClientCertificateWrapper clientCertificateWrapper = securityManagementService.newClientCertificateWrapper(certSecurityAccessorType
                    .getKeyType(), certSecurityAccessorType.getKeyEncryptionMethod()).alias(alias).add();
            clientCertificateWrapper.getPrivateKeyWrapper().generateValue();

            X500NameBuilder x500NameBuilder = new X500NameBuilder();
            x500NameBuilder.addRDN(BCStyle.CN, cn);
            PKCS10CertificationRequest pkcs10CertificationRequest = clientCertificateWrapper.getPrivateKeyWrapper()
                    .generateCSR(x500NameBuilder.build(), certSecurityAccessorType.getKeyType().getSignatureAlgorithm());
            clientCertificateWrapper.setCSR(pkcs10CertificationRequest, certSecurityAccessorType.getKeyType().getKeyUsages(), certSecurityAccessorType.getKeyType().getExtendedKeyUsages());
            clientCertificateWrapper.save();
            context.commit();
        } catch (Exception e) {
            System.err.println("Failed to create CSR: " + e.getMessage());
        }
    }

    public void renew() {
        System.out.println("Trigger renew for a security accessor on a device / or security accessor default value");
        System.out.println("usage: renew <device name> <key accessor type name>");
        System.out.println("or:    renew <key accessor type name>");
        System.out.println("e.g.:  renew 1001 MK");
    }

    public void renew(String keyAccessorTypeName) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            SecurityAccessorType securityAccessorType = securityManagementService.findSecurityAccessorTypeByName(keyAccessorTypeName)
                    .orElseThrow(() -> new RuntimeException("No such security accessor type: " + keyAccessorTypeName));

            com.elster.jupiter.pki.SecurityAccessor<? extends SecurityValueWrapper> securityAccessor = securityManagementService.getDefaultValues(securityAccessorType)
                    .orElseThrow(() -> new RuntimeException("No default security accessor for security accessor type " + keyAccessorTypeName));

            securityAccessor.renew();

            context.commit();
        } catch (Exception e) {
            System.err.println("Failed to renew value: " + e.getMessage());
        }
    }

    public void renew(String deviceName, String keyAccessorTypeName) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            Device device = deviceService.findDeviceByName(deviceName)
                    .orElseThrow(() -> new RuntimeException("No such device"));
            SecurityAccessorType securityAccessorType = device.getDeviceType()
                    .getSecurityAccessorTypes()
                    .stream()
                    .filter(kat -> kat.getName().equals(keyAccessorTypeName))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("No such security accessor type on the device type: " + keyAccessorTypeName));

            SecurityAccessor securityAccessor = device.getSecurityAccessor(securityAccessorType)
                    .orElseThrow(() -> new RuntimeException("No security accessor for security accessor type " + keyAccessorTypeName));

            securityAccessor.renew();

            context.commit();
        } catch (Exception e) {
            System.err.println("Failed to renew value: " + e.getMessage());
        }
    }

    public void swap() {
        System.out.println("Swap actual and temp values for a security accessor on a device / or security accessor default value");
        System.out.println("Usage: swap <device name> <security accessor type name>");
        System.out.println("or:    swap <security accessor type name>");
        System.out.println("e.g.:  swap 1001 MK");
    }

    public void swap(String keyAccessorTypeName) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            SecurityAccessorType securityAccessorType = securityManagementService.findSecurityAccessorTypeByName(keyAccessorTypeName)
                    .orElseThrow(() -> new RuntimeException("No such security accessor type: " + keyAccessorTypeName));

            com.elster.jupiter.pki.SecurityAccessor<? extends SecurityValueWrapper> securityAccessor = securityManagementService.getDefaultValues(securityAccessorType)
                    .orElseThrow(() -> new RuntimeException("No default security accessor for security accessor type " + keyAccessorTypeName));

            securityAccessor.swapValues();

            context.commit();
        } catch (Exception e) {
            System.err.println("Failed to swap values: " + e.getMessage());
        }
    }

    public void swap(String deviceName, String keyAccessorTypeName) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            Device device = deviceService.findDeviceByName(deviceName)
                    .orElseThrow(() -> new RuntimeException("No such device"));
            SecurityAccessorType securityAccessorType = device.getDeviceType()
                    .getSecurityAccessorTypes()
                    .stream()
                    .filter(kat -> kat.getName().equals(keyAccessorTypeName))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("No such security accessor type on the device type: " + keyAccessorTypeName));

            SecurityAccessor securityAccessor = device.getSecurityAccessor(securityAccessorType)
                    .orElseThrow(() -> new RuntimeException("No security accessor for security accessor type " + keyAccessorTypeName));

            securityAccessor.swapValues();

            context.commit();
        } catch (Exception e) {
            System.err.println("Failed to swap values: " + e.getMessage());
        }
    }

    public void clearTemp() {
        System.out.println("Clears the temp value for a security accessor on a device / or security accessor default value");
        System.out.println("usage: clearTemp <device name> <security accessor type name>");
        System.out.println("or:    clearTemp <security accessor type name>");
        System.out.println("e.g.:  clearTemp 1001 MK");
    }

    public void clearTemp(String keyAccessorTypeName) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            SecurityAccessorType securityAccessorType = securityManagementService.findSecurityAccessorTypeByName(keyAccessorTypeName)
                    .orElseThrow(() -> new RuntimeException("No such security accessor type: " + keyAccessorTypeName));

            com.elster.jupiter.pki.SecurityAccessor<? extends SecurityValueWrapper> securityAccessor = securityManagementService.getDefaultValues(securityAccessorType)
                    .orElseThrow(() -> new RuntimeException("No default security accessor for security accessor type " + keyAccessorTypeName));

            securityAccessor.clearTempValue();

            context.commit();
        } catch (Exception e) {
            System.err.println("Failed to clear temp value: " + e.getMessage());
        }
    }

    public void clearTemp(String deviceName, String keyAccessorTypeName) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            Device device = deviceService.findDeviceByName(deviceName)
                    .orElseThrow(() -> new RuntimeException("No such device"));
            SecurityAccessorType securityAccessorType = device.getDeviceType()
                    .getSecurityAccessorTypes()
                    .stream()
                    .filter(kat -> kat.getName().equals(keyAccessorTypeName))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("No such security accessor type on the device type: " + keyAccessorTypeName));

            SecurityAccessor securityAccessor = device.getSecurityAccessor(securityAccessorType)
                    .orElseThrow(() -> new RuntimeException("No security accessor for security accessor type " + keyAccessorTypeName));

            securityAccessor.clearTempValue();

            context.commit();
        } catch (Exception e) {
            System.err.println("Failed to clear temp value: " + e.getMessage());
        }
    }

    public void trustStores() {
        List<List<?>> lists = securityManagementService.getAllTrustStores()
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
            securityManagementService.newTrustStore(name).description("Created by GoGo command").add();
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
            TrustStore trustStore = securityManagementService.findTrustStore(name)
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

    public void runCertificateRenewalTask() {
        threadPrincipalService.set(() -> "Console");
        try {
            transactionService.execute(() -> {
                certificateRenewalService.runNow();
                return null;
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
        System.out.println("Device certificate renewal task has been started.");
    }

    public void runKeyRenewalTask() {
        threadPrincipalService.set(() -> "Console");
        try {
            transactionService.execute(() -> {
                keyRenewalService.runNow();
                return null;
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
        System.out.println("Device key renewal task has been started.");
    }

    public void runCrlRequestTask() {
        threadPrincipalService.set(() -> "Console");
        try {
            transactionService.execute(() -> {
                crlRequestService.runNow();
                return null;
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            threadPrincipalService.clear();
        }
        System.out.println("Crl request task has been started.");
    }
}
