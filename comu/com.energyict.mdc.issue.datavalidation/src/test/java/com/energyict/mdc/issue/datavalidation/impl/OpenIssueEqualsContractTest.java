/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datavalidation.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.issue.datavalidation.IssueDataValidationService;
import com.energyict.mdc.issue.datavalidation.impl.entity.OpenIssueDataValidationImpl;

import java.time.Clock;
import java.util.Collections;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OpenIssueEqualsContractTest extends EqualsContractTest {

    private static final long ID = 651L;
    private static final long OTHER_ID = 426294L;

    @Mock
    DataModel dataModel;
    @Mock
    IssueService issueService;
    @Mock
    IssueDataValidationService issueDataValidationService;

    OpenIssueImpl baseIssue;
    OpenIssueDataValidationImpl issueDataValidation;

    @Override
    protected Object getInstanceA() {
        if (issueDataValidation == null) {
            baseIssue = new OpenIssueImpl(dataModel, issueService, Clock.systemDefaultZone());
            baseIssue.setId(ID);
            issueDataValidation = new OpenIssueDataValidationImpl(dataModel, issueDataValidationService);
            issueDataValidation.setIssue(baseIssue);
        }
        return issueDataValidation;
    }

    @Override
    protected Object getInstanceEqualToA() {
        OpenIssueImpl baseIssue = new OpenIssueImpl(dataModel, issueService, Clock.systemDefaultZone());
        baseIssue.setId(ID);
        OpenIssueDataValidationImpl issueDataValidation = new OpenIssueDataValidationImpl(dataModel, issueDataValidationService);
        issueDataValidation.setIssue(baseIssue);
        return issueDataValidation;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        OpenIssueImpl baseIssue = new OpenIssueImpl(dataModel, issueService, Clock.systemDefaultZone());
        baseIssue.setId(OTHER_ID);
        OpenIssueDataValidationImpl issueDataValidation = new OpenIssueDataValidationImpl(dataModel, issueDataValidationService);
        issueDataValidation.setIssue(baseIssue);
        return Collections.singletonList(issueDataValidation);
    }

    @Override
    protected boolean canBeSubclassed() {
        return false;
    }

    @Override
    protected Object getInstanceOfSubclassEqualToA() {
        return null;
    }
}
