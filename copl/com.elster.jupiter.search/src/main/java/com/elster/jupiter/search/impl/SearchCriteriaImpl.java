package com.elster.jupiter.search.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SearchCriteriaImpl extends EntityImpl implements SearchCriteria {
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 280, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String userName;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 280, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String criteria;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 250, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;

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

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    @Size(min = 1, max = 3, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String domain;

    private final Thesaurus thesaurus;
    @Inject
    public SearchCriteriaImpl(DataModel dataModel, Thesaurus thesaurus) {
        super(dataModel);
        this.thesaurus = thesaurus;
    }
    public SearchCriteriaImpl init(String name, String userName, String criteria, String domain) {
        this.name = name;
        this.criteria = criteria;
        this.domain = domain;
        this.userName = userName;
        return this;
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
}
