/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.pki.impl.gogo;

import com.elster.jupiter.pki.KeyType;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.gogo.MysqlPrint;

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

    private SecurityManagementService securityManagementService;
    private TransactionService transactionService;
    private ThreadPrincipalService threadPrincipalService;

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
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
        List<List<?>> kats = securityManagementService.getSecurityAccessorTypes()
                .stream()
                .map(kat -> Arrays.asList(kat.getName(), kat.getKeyType().getName(), kat.getDuration(), kat.getKeyEncryptionMethod(), kat.getTrustStore().isPresent()?kat.getTrustStore().get().getName():""))
                .collect(toList());
        kats.add(0, Arrays.asList("Name", "Key type", "Duration", "Encryption method", "Trust store"));
        MYSQL_PRINT.printTableWithHeader(kats);
    }

    public void createKeyAccessorType() {
        System.out.println("Usage: createKeyAccessorTypes <name> <key type name> <encryption method> <trust store> <duration in days>");
        System.out.println("Eg.  : createKeyAccessorTypes GUAK AES128 SSM 365");
    }

    public void createKeyAccessorType(String name, String keyTypeName, String keyEncryptionMethod, Integer ... duration) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            KeyType keyType = securityManagementService.getKeyType(keyTypeName)
                    .orElseThrow(() -> new RuntimeException("No such key type"));
            SecurityAccessorType.Builder builder = securityManagementService.addSecurityAccessorType(name, keyType)
                    .keyEncryptionMethod(keyEncryptionMethod)
                    .purpose(SecurityAccessorType.Purpose.DEVICE_OPERATIONS)
                    .description("Created by gogo command")
                    .duration(TimeDuration.days(duration[0]));
            builder.add();
            context.commit();
        }
    }

    public void createCertificateAccessorType() {
        System.out.println("Usage: createCertificateAccessorTypes <name> <key type name> <trust store name> <key encryption method> <purpose>");
        System.out.println("Eg.  : createCertificateAccessorTypes TLS TLSClient DataVault DEVICE_OPERATIONS");
    }

    public void createCertificateAccessorType(String name, String keyTypeName, String trustStoreName, String keyEncryptionMethod, String purpose) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            KeyType keyType = securityManagementService.getKeyType(keyTypeName)
                    .orElseThrow(() -> new RuntimeException("No such key type"));
            TrustStore trustStore = securityManagementService.findTrustStore(trustStoreName)
                    .orElseThrow(() -> new RuntimeException("No such trust store"));
            SecurityAccessorType.Builder builder = securityManagementService.addSecurityAccessorType(name, keyType)
                    .trustStore(trustStore)
                    .keyEncryptionMethod(keyEncryptionMethod)
                    .purpose(SecurityAccessorType.Purpose.valueOf(purpose))
                    .description("Created by gogo command");
            builder.add();
            context.commit();
        }
    }
}
