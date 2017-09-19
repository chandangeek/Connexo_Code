/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.pki.impl;

import com.elster.jupiter.pki.PkiService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.stream.Stream;

@Component(name = "ProtocolsKeyTypeInitializer", immediate = true)
public class ProtocolsSecurityAccessorTypesInitializer {
    private PkiService pkiService;
    private TransactionService transactionService;

    @Reference
    public void setPkiService(PkiService pkiService) {
        this.pkiService = pkiService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Activate
    public void activate() {
        try (TransactionContext context = transactionService.getContext()) {
            Stream.of(ProtocolKeyTypes.values()).forEach(kt -> pkiService.getKeyType(kt.getName()).orElseGet(()->kt.createKeyType(pkiService)));
            context.commit();
        }
    }
}
