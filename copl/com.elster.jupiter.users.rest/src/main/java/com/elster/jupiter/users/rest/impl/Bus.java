package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;

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

    public static UserService getUserService() {
        return locator.getUserService();
    }

}
