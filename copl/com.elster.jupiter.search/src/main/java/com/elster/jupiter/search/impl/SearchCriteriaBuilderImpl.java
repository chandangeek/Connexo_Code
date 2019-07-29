package com.elster.jupiter.search.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;

public class SearchCriteriaBuilderImpl implements SearchCriteriaService.SearchCriteriaBuilder {

    protected final SearchCriteriaImpl underConstruction;
    private final DataModel dataModel;

    public SearchCriteriaBuilderImpl(DataModel dataModel, SearchCriteriaImpl searchCriteria){
        this.dataModel = dataModel;
        this.underConstruction = searchCriteria;
    }

    @Override
    public SearchCriteriaService.SearchCriteriaBuilder setName(String name) {
        underConstruction.setName(name);
        return this;
    }

    @Override
    public SearchCriteriaService.SearchCriteriaBuilder setUserName(String userName) {
        underConstruction.setUserName(userName);
        return this;
    }

    @Override
    public SearchCriteriaService.SearchCriteriaBuilder setCriteria(String criteria) {
        underConstruction.setCriteria(criteria);
        return this;
    }

    @Override
    public SearchCriteriaService.SearchCriteriaBuilder setDomain(String domain) {
        underConstruction.setDomain(domain);
        return this;
    }

    @Override
    public SearchCriteria complete() {
        this.underConstruction.save();
        return this.underConstruction;
    }
}
