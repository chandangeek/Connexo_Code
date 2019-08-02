package com.elster.jupiter.search.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.search.SearchCriteria;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SearchCriteriaImpl extends EntityImpl implements SearchCriteria {

    private long id;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 280, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String userName;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 1020, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String criteria;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 250, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 50, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String domain;

    private final Thesaurus thesaurus;

    @Inject
    public SearchCriteriaImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel);
        this.thesaurus = thesaurus;
    }

    public SearchCriteriaImpl init(long id, String name, String userName, String criteria, String domain) {
        this.id = id;
        this.name = name;
        this.criteria = criteria;
        this.domain = domain;
        this.userName = userName;
        return this;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setCriteria(String criteria) {
        this.criteria = criteria;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    @Override
    public String getUser() {
        return userName;
    }

    @Override
    public String getCriteria() {
        return criteria;
    }

    @Override
    public String getDomain() {
        return domain;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getId() {
        return id;
    }
}
