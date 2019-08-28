package com.elster.jupiter.search.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.RestQueryService;
import com.elster.jupiter.search.SearchCriteriaService;
import com.elster.jupiter.transaction.TransactionService;

import javax.inject.Inject;

public class BaseResource {

    private RestQueryService queryService;
    private TransactionService transactionService;

    private SearchCriteriaService searchCriteriaService;


    public BaseResource(  RestQueryService restQueryService,
                          SearchCriteriaService searchCriteriaService,
                          TransactionService transactionService
                        ) {
        this.queryService = restQueryService;
        this.searchCriteriaService = searchCriteriaService;
        this.transactionService = transactionService;
    }

    protected RestQueryService getQueryService() {
        return queryService;
    }

    protected SearchCriteriaService getSearchCriteriaService() {
        return searchCriteriaService;
    }

    protected TransactionService getTransactionService() {
        return transactionService;
    }


}
