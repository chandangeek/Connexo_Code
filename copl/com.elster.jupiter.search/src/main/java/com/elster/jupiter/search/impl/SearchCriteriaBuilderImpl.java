package com.elster.jupiter.search.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.search.SearchCriteria;
import com.elster.jupiter.search.SearchCriteriaService;

import static com.elster.jupiter.search.SearchCriteriaService.*;

public class SearchCriteriaBuilderImpl implements SearchCriteriaBuilder {

    protected final SearchCriteriaImpl underConstruction;
    private final DataModel dataModel;

    public SearchCriteriaBuilderImpl(DataModel dataModel, SearchCriteriaImpl searchCriteriaImpl){
        this.dataModel = dataModel;
        this.underConstruction = searchCriteriaImpl;
    }

    @Override
    public SearchCriteriaBuilder setId(long id) {
        underConstruction.setId(id);
        return this;
    }

    @Override
    public SearchCriteriaBuilder setName(String name) {
        underConstruction.setName(name);
        return this;
    }

    @Override
    public SearchCriteriaBuilder setUserName(String userName) {
        underConstruction.setUserName(userName);
        return this;
    }

    @Override
    public SearchCriteriaBuilder setCriteria(String criteria) {
        underConstruction.setCriteria(criteria);
        return this;
    }

    @Override
    public SearchCriteriaBuilder setDomain(String domain) {
        underConstruction.setDomain(domain);
        return this;
    }

    @Override
    public SearchCriteria complete() {
        this.underConstruction.save();
        return this.underConstruction;
    }

    @Override
    public SearchCriteria update() {
        this.underConstruction.update();
        return this.underConstruction;
    }

    @Override
    public SearchCriteria delete() {
        this.underConstruction.delete();
        return this.underConstruction;
    }
}
