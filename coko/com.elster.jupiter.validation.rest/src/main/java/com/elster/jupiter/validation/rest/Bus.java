package com.elster.jupiter.validation.rest;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.validation.rest.impl.ServiceLocator;

enum Bus {
    ;

    private static volatile ServiceLocator locator;

    static void setServiceLocator(ServiceLocator locator) {
        Bus.locator = locator;
    }

    public static RestQueryService getRestQueryService() {
        return locator.getRestQueryService();
    }

    public static TransactionService getTransactionService() {
        return locator.getTransactionService();
    }

    public static ValidationService getValidationService() {
        return locator.getValidationService();
    }

    public static MeteringService getMeteringService() {
        return locator.getMeteringService();
    }

}
