package com.elster.jupiter.validation.impl;

import com.elster.jupiter.orm.cache.TypeCache;
import com.elster.jupiter.util.time.UtcInstant;
import com.elster.jupiter.validation.ValidationRuleSet;

import java.util.Objects;

public final class ValidationRuleSetImpl implements ValidationRuleSet {

    private long id;
    private String mRID;
    private String name;
    private String aliasName;
    private String description;

    private long version;
    private UtcInstant createTime;
    private UtcInstant modTime;
    private String userName;

    private ValidationRuleSetImpl() {
        // for persistence
    }

    public ValidationRuleSetImpl(String name) {
        this.name = name;
    }


    @Override
    public String getAliasName() {
        return aliasName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String getMRID() {
        return mRID;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public void setMRID(String mRID) {
        this.mRID = mRID;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public long getVersion() {
        return version;
    }

    UtcInstant getCreateTime() {
        return createTime;
    }

    UtcInstant getModTime() {
        return modTime;
    }

    String getUserName() {
        return userName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ValidationRuleSet)) {
            return false;
        }

        ValidationRuleSet validationRuleSet = (ValidationRuleSet) o;

        return id == validationRuleSet.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public void save() {
        if (getId() == 0) {
            validationRuleSetFactory().persist(this);
            Bus.getEventService().postEvent(EventType.VALIDATIONRULESET_CREATED.topic(), this);
        } else {
            validationRuleSetFactory().update(this);
            Bus.getEventService().postEvent(EventType.VALIDATIONRULESET_UPDATED.topic(), this);
        }
    }

    @Override
    public void delete() {
        validationRuleSetFactory().remove(this);
        Bus.getEventService().postEvent(EventType.VALIDATIONRULESET_DELETED.topic(), this);
    }

    private TypeCache<ValidationRuleSet> validationRuleSetFactory() {
        return Bus.getOrmClient().getValidationRuleSetFactory();
    }


}
