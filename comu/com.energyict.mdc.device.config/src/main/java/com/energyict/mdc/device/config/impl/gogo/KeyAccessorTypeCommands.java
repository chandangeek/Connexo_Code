/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.energyict.mdc.device.config.impl.gogo;

import com.elster.jupiter.pki.KeyAccessorType;
import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.gogo.MysqlPrint;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 1/31/17.
 */
@Component(name = "KeyAccessorTypeCommands",
        service = KeyAccessorTypeCommands.class,
        property = {
                "osgi.command.scope=kat",
                "osgi.command.function=keyAccessorTypes",
                "osgi.command.function=createKeyAccessorType",
                "osgi.command.function=createCertificateAccessorType"
        },
        immediate = true)
public class KeyAccessorTypeCommands {
    public static final MysqlPrint MYSQL_PRINT = new MysqlPrint();

    private DeviceConfigurationService deviceConfigurationService;
    private SecurityManagementService securityManagementService;
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
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void keyAccessorTypes() {
        System.out.println("Usage: keyAccessorTypes <device type id>");
        System.out.println("Eg.  : keyAccessorTypes 153");
    }

    public void keyAccessorTypes(Long deviceTypeId) {
        DeviceType deviceType = deviceConfigurationService.findDeviceType(deviceTypeId)
                .orElseThrow(() -> new RuntimeException("No such device type"));
        List<List<?>> kats = deviceType.getKeyAccessorTypes()
                .stream()
                .map(kat -> Arrays.asList(kat.getName(), kat.getKeyType().getName(), kat.getDuration(), kat.getKeyEncryptionMethod(), kat.getTrustStore().isPresent()?kat.getTrustStore().get().getName():""))
                .collect(toList());
        kats.add(0, Arrays.asList("Name", "Key type", "Duration", "Encryption method", "Trust store"));
        MYSQL_PRINT.printTableWithHeader(kats);
    }

    public void createKeyAccessorType() {
        System.out.println("Usage: createKeyAccessorTypes <name> <device type id> <key type name> <encryption method> <trust store> <duration in days>");
        System.out.println("Eg.  : createKeyAccessorTypes GUAK 153 AES128 SSM 365");
    }

    public void createKeyAccessorType(String name, long deviceTypeId, String keyTypeName, String keyEncryptionMethod, Integer ... duration) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.findDeviceType(deviceTypeId)
                    .orElseThrow(() -> new RuntimeException("No such device type"));
            KeyType keyType = securityManagementService.getKeyType(keyTypeName)
                    .orElseThrow(() -> new RuntimeException("No such key type"));
            KeyAccessorType.Builder builder = deviceType.addKeyAccessorType(name, keyType)
                    .keyEncryptionMethod(keyEncryptionMethod)
                    .description("Created by gogo command")
                    .duration(TimeDuration.days(duration[0]));
            builder.add();
            context.commit();
        }
    }

    public void createCertificateAccessorType() {
        System.out.println("Usage: createCertificateAccessorTypes <name> <device type id> <key type name> <trust store name> <key encryption method>");
        System.out.println("Eg.  : createCertificateAccessorTypes TLS 153 TLSClient DataVault");
    }

    public void createCertificateAccessorType(String name, long deviceTypeId, String keyTypeName, String trustStoreName, String keyEncryptionMethod) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            DeviceType deviceType = deviceConfigurationService.findDeviceType(deviceTypeId)
                    .orElseThrow(() -> new RuntimeException("No such device type"));
            KeyType keyType = securityManagementService.getKeyType(keyTypeName)
                    .orElseThrow(() -> new RuntimeException("No such key type"));
            TrustStore trustStore = securityManagementService.findTrustStore(trustStoreName)
                    .orElseThrow(() -> new RuntimeException("No such trust store"));
            KeyAccessorType.Builder builder = deviceType.addKeyAccessorType(name, keyType)
                    .trustStore(trustStore)
                    .keyEncryptionMethod(keyEncryptionMethod)
                    .description("Created by gogo command");
            builder.add();
            context.commit();
        }
    }
}
