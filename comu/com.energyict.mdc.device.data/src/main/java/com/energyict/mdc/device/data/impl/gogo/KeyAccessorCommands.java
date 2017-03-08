/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.data.impl.gogo;

import com.elster.jupiter.pki.ClientCertificateWrapper;
import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.pki.PlaintextPrivateKeyWrapper;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.gogo.MysqlPrint;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.KeyAccessor;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.FileInputStream;
import java.io.IOException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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
                "osgi.command.function=generateCSR"
        },
        immediate = true)
public class KeyAccessorCommands {
    public static final MysqlPrint MYSQL_PRINT = new MysqlPrint();

    private DeviceConfigurationService deviceConfigurationService;
    private DeviceService deviceService;
    private PkiService pkiService;
    private TransactionService transactionService;

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
    public void keyAccessors(long deviceId) {
        Device device = deviceService.findDeviceById(deviceId)
                .orElseThrow(() -> new RuntimeException("No such device"));
        List<List<?>> collect = device.getDeviceType()
                .getKeyAccessorTypes()
                .stream()
                .map(kat -> {
                    Optional<KeyAccessor> keyAccessor = device.getKeyAccessor(kat);
                    return Arrays.asList(kat.getName(),
                            kat.getKeyType().getName(),
                            keyAccessor.isPresent() && keyAccessor.get().getActualValue()!=null ? "Present":"",
                            keyAccessor.isPresent() && keyAccessor.get().getTempValue().isPresent() ? "Present":""
                    );
                })
                .collect(toList());

        MYSQL_PRINT.printTableWithHeader(Arrays.asList("Key accessor type", "Key type","Current value", "Temp value"), collect);
    }

    public void importCertificateWithKey() {
        System.out.println("Usage: importCertificateWithKey <device id> <cert accessor type name> <private key accessor type name> <pkcs#12 file>  <password> alias");
        System.out.println("e.g. : importCertificateWithKey 1 \"TLS\" \"RSA\" tls.pkcs12 foo123 mycert");
    }

    public void importCertificateWithKey(long deviceId, String certKatName, String keyKatName, String pkcs12Name, String pkcs12Password, String alias) throws
            KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
        try (TransactionContext context = transactionService.getContext()) {
            Device device = deviceService.findDeviceById(deviceId)
                    .orElseThrow(() -> new RuntimeException("No such device"));
            KeyAccessorType certKeyAccessorType = device.getDeviceType()
                    .getKeyAccessorTypes()
                    .stream()
                    .filter(kat -> kat.getName().equals(certKatName))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("No such key accessor type on the device type: "+certKatName));
            KeyAccessorType keyKeyAccessorType = device.getDeviceType()
                    .getKeyAccessorTypes()
                    .stream()
                    .filter(kat -> kat.getName().equals(keyKatName))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("No such key accessor type on the device type: "+keyKatName));

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
            ClientCertificateWrapper clientCertificateWrapper = pkiService.newClientCertificateWrapper(alias, certKeyAccessorType, keyKeyAccessorType);
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

    public void generateCSR() {
        System.out.println("Usage: generateCSR <device id> <cert accessor type name> <private key accessor type name> <alias> <CommonName>");
        System.out.println("e.g. : generateCSR 1 \"TLS\" \"RSA\" comserver \"Comserver TLS\"");
    }

    public void generateCSR(long deviceId, String certKatName, String keyKatName, String alias, String cn) {
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
            KeyAccessorType keyKeyAccessorType = device.getDeviceType()
                    .getKeyAccessorTypes()
                    .stream()
                    .filter(kat -> kat.getName().equals(keyKatName))
                    .findAny()
                    .orElseThrow(() -> new RuntimeException("No such key accessor type on the device type: "+keyKatName));

            ClientCertificateWrapper clientCertificateWrapper = pkiService.newClientCertificateWrapper(alias, certKeyAccessorType, keyKeyAccessorType);
            clientCertificateWrapper.generateValue();


        }
}
