/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl.action;

import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import java.util.List;

public class
FailedAction extends AbstractIssueAction {

    private static final String NAME = "FailedServiceCallAction";
    public static final String CLOSE_STATUS = NAME + ".status";
    public static final String COMMENT = NAME + ".comment";

    public FailedAction(){
        super(null, null, null);
    }

    public FailedAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(dataModel, thesaurus, propertySpecService);
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        return null;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return null;
    }
}
