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

    //private SearchCriteriaService searchCriteriaService;

    private Thesaurus thesaurus;

    public BaseResource() {
    }


    @Inject
    public void setQueryService(RestQueryService queryService) {
        this.queryService = queryService;
    }

    protected RestQueryService getQueryService() {
        return queryService;
    }

    @Inject
    public void setSearchCriteriaService(SearchCriteriaService  searchCriteriaService) {
        this.searchCriteriaService = searchCriteriaService;
    }

    protected SearchCriteriaService getSearchCriteriaService() {
        return searchCriteriaService;
    }

    @Inject
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    protected TransactionService getTransactionService() {
        return transactionService;
    }



    @Inject
    public void setThesaurus(Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }

}
