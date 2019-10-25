/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.share;

import aQute.bnd.annotation.ConsumerType;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.security.Privileges;
import com.elster.jupiter.issue.share.entity.ActionType;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.HasValidProperties;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.users.User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@ConsumerType
@HasValidProperties(requiredPropertyMissingMessage = "{" + MessageSeeds.Keys.PROPERTY_MISSING + "}",
        propertyNotInSpecMessage = "{" + MessageSeeds.Keys.PROPERTY_NOT_IN_PROPERTYSPECS + "}")
public abstract class AbstractIssueAction implements IssueAction {

    private final DataModel dataModel;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;

    protected IssueType issueType;
    protected IssueReason issueReason;

    protected Map<String, Object> properties = new HashMap<>();

    protected AbstractIssueAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService) {
        this.dataModel = dataModel;
        this.thesaurus = thesaurus;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public boolean isApplicable(Issue issue) {
        return issue != null;
    }

    @Override
    public boolean isApplicable(String reasonName) {
        return true;
    }

    @Override
    public boolean isApplicableForUser(User user) {
        return user.getPrivileges().stream().anyMatch(p -> Privileges.Constants.ACTION_ISSUE.equals(p.getName()));
    }

    @Override
    public IssueAction initAndValidate(Map<String, Object> properties) {
        this.properties = properties;
        Save.CREATE.validate(dataModel, this);
        return this;
    }

    @Override
    public Map<String, Object> getProperties() {
        return Collections.unmodifiableMap(this.properties);
    }

    @Override
    public IssueAction setIssueType(final IssueType issueType) {
        this.issueType = issueType;
        return this;
    }

    @Override
    public IssueAction setIssueReason(final IssueReason issueReason) {
        this.issueReason = issueReason;
        return this;
    }

    @Override
    public long getActionType() {
        return ActionType.ACTION.getValue();
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    protected Thesaurus getThesaurus() {
        return thesaurus;
    }
}
