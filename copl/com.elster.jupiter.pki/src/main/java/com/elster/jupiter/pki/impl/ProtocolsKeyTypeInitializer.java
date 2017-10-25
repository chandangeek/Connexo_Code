/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.pki.SecurityManagementService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.stream.Stream;

@Component(name = "ProtocolsKeyTypeInitializer", immediate = true)
public class ProtocolsKeyTypeInitializer {
    private SecurityManagementService securityManagementService;
    private TransactionService transactionService;

    @Reference
    public void setSecurityManagementService(SecurityManagementService securityManagementService) {
        this.securityManagementService = securityManagementService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Activate
    public void activate() {
        try (TransactionContext context = transactionService.getContext()) {
            Stream.of(ProtocolKeyTypes.values()).forEach(kt -> securityManagementService.getKeyType(kt.getName()).orElseGet(()->kt.createKeyType(securityManagementService)));
            context.commit();
        }
    }
}
