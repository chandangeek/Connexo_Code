/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl.gogo;

import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.gogo.MysqlPrint;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Component(name = "com.elster.jupiter.pki.gogo.impl.PkiGogoCommand",
        service = PkiGogoCommand.class,
        property = {"osgi.command.scope=pki",
                "osgi.command.function=keytypes",
                "osgi.command.function=certificateStore",
                "osgi.command.function=deleteCertificate"
        },
        immediate = true)
public class PkiGogoCommand {
    public static final MysqlPrint MYSQL_PRINT = new MysqlPrint();

    private volatile SecurityManagementService securityManagementService;
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
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    public void keytypes() {
        List<List<?>> collect = securityManagementService.findAllKeyTypes()
                .stream()
                .map(keytype -> Arrays.asList(keytype.getId(), keytype.getName(), keytype.getCryptographicType().name(), keytype.getKeyAlgorithm()))
                .collect(toList());
        collect.add(0, Arrays.asList("id", "name", "type", "algorithm"));
        MYSQL_PRINT.printTableWithHeader(collect);
    }

    public void certificateStore() {
        List<List<?>> certs = securityManagementService.findAllCertificates()
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

            securityManagementService.findCertificateWrapper(alias)
                    .orElseThrow(() -> new IllegalArgumentException("No such certificate"))
                    .delete();
            context.commit();
        }
    }

}

