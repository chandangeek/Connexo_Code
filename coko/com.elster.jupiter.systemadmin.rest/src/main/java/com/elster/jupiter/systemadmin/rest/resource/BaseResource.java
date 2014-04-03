package com.elster.jupiter.systemadmin.rest.resource;


import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.systemadmin.LicensingService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;

import javax.inject.Inject;

public abstract class BaseResource {
    public static int UNPROCESSIBLE_ENTITY = 422;

    private RestQueryService queryService;
    private TransactionService transactionService;

    private LicensingService licensingService;
    private UserService userService;

    public BaseResource(){
    }

    protected RestQueryService getQueryService() {
        return queryService;
    }

    @Inject
    public void setQueryService(RestQueryService queryService) {
        this.queryService = queryService;
    }

    @Inject
    public void setLicensingService(LicensingService licensingService) {
        this.licensingService = licensingService;
    }
    protected LicensingService getLicensingService() {
        return licensingService;
    }

    @Inject
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
    protected TransactionService getTransactionService() {
        return transactionService;
    }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
    protected UserService getUserService() {
        return userService;
    }
}
