package com.elster.jupiter.parties.rest;

import com.elster.jupiter.parties.PartyService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;

enum Bus {
    ;
    private static ServiceLocator serviceLocator;

    public static void setServiceLocator(ServiceLocator serviceLocator) {
        Bus.serviceLocator = serviceLocator;
    }

    public static RestQueryService getQueryService() {
        return serviceLocator.getQueryService();
    }

    public static TransactionService getTransactionService() {
        return serviceLocator.getTransactionService();
    }

    public static PartyService getPartyService() {
        return serviceLocator.getPartyService();
    }
}
