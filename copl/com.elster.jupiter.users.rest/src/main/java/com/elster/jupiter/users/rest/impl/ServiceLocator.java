package com.elster.jupiter.users.rest.impl;

import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;

public interface ServiceLocator {

    TransactionService getTransactionService();

    UserService getUserService();

    RestQueryService getRestQueryService();

}
