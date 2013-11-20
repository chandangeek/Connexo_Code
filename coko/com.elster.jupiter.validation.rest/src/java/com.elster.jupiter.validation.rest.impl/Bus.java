package com.elster.jupiter.validation.rest.impl;

import com.elster.jupiter.validation.ValidationService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;

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

}
