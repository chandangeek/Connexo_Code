package com.elster.jupiter.systemadmin.rest.imp.resource;


import com.elster.jupiter.license.LicenseService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.json.JsonService;

import javax.inject.Inject;

public abstract class BaseResource {
    public static int UNPROCESSIBLE_ENTITY = 422;

    private RestQueryService queryService;
    private TransactionService transactionService;

    private LicenseService licenseService;
    private UserService userService;
    private NlsService nlsService;
    private JsonService jsonService;

    public BaseResource() {
    }

    protected RestQueryService getQueryService() {
        return queryService;
    }

    @Inject
    public void setQueryService(RestQueryService queryService) {
        this.queryService = queryService;
    }

    @Inject
    public void setLicenseService(LicenseService licenseService) {
        this.licenseService = licenseService;
    }

    protected LicenseService getLicenseService() {
        return licenseService;
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

    @Inject
    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
    }

    protected NlsService getNlsService() {
        return nlsService;
    }


    @Inject
    public void setJsonService(JsonService jsonService) {
        this.jsonService = jsonService;
    }

    protected JsonService getJsonService() {
        return jsonService;
    }
}
