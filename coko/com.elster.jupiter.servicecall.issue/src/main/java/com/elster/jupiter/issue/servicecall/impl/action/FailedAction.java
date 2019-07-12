/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.impl.action;

import com.elster.jupiter.issue.servicecall.impl.i18n.TranslationKeys;
import com.elster.jupiter.issue.share.AbstractIssueAction;
import com.elster.jupiter.issue.share.IssueActionResult;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class FailedAction extends AbstractIssueAction {

    private static final String NAME = "FailedServiceCallAction";
    public static final String CLOSE_STATUS = NAME + ".status";
    public static final String COMMENT = NAME + ".comment";

    @Inject
    public FailedAction(DataModel dataModel, Thesaurus thesaurus, PropertySpecService propertySpecService) {
        super(dataModel, thesaurus, propertySpecService);
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(TranslationKeys.SERVICE_CALL_ISSUE_FAILED_REASON).format();
    }

    @Override
    public IssueActionResult execute(Issue issue) {
        IssueActionResult.DefaultActionResult result = new IssueActionResult.DefaultActionResult();
        //todo
        return result;
    }

    @Override
    public boolean isApplicable(Issue issue) {
        return false;
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.emptyList();
    }
}
