/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.insight.issue.datavalidation.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.issue.impl.records.HistoricalIssueImpl;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.insight.issue.datavalidation.UsagePointIssueDataValidationService;
import com.elster.insight.issue.datavalidation.impl.entity.UsagePointHistoricalIssueDataValidationImpl;

import java.time.Clock;
import java.util.Collections;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
@Ignore
@RunWith(MockitoJUnitRunner.class)
public class HistoricalIssueEqualsContractTest extends EqualsContractTest {

    private static final long ID = 651L;
    private static final long OTHER_ID = 426294L;

    @Mock
    DataModel dataModel;
    @Mock
    IssueService issueService;
    @Mock
    UsagePointIssueDataValidationService usagePointIssueDataValidationService;
    @Mock
    Thesaurus thesaurus;

    HistoricalIssueImpl baseIssue;
    UsagePointHistoricalIssueDataValidationImpl issueDataValidation;

    @Override
    protected Object getInstanceA() {
        if (issueDataValidation == null) {
            baseIssue = new HistoricalIssueImpl(dataModel, issueService, Clock.systemDefaultZone(), thesaurus);
            baseIssue.setId(ID);
            issueDataValidation = new UsagePointHistoricalIssueDataValidationImpl(dataModel, usagePointIssueDataValidationService);
            issueDataValidation.setIssue(baseIssue);
        }
        return issueDataValidation;
    }

    @Override
    protected Object getInstanceEqualToA() {
        HistoricalIssueImpl baseIssue = new HistoricalIssueImpl(dataModel, issueService, Clock.systemDefaultZone(), thesaurus);
        baseIssue.setId(ID);
        UsagePointHistoricalIssueDataValidationImpl issueDataValidation = new UsagePointHistoricalIssueDataValidationImpl(dataModel, usagePointIssueDataValidationService);
        issueDataValidation.setIssue(baseIssue);
        return issueDataValidation;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        HistoricalIssueImpl baseIssue = new HistoricalIssueImpl(dataModel, issueService, Clock.systemDefaultZone(), thesaurus);
        baseIssue.setId(OTHER_ID);
        UsagePointHistoricalIssueDataValidationImpl issueDataValidation = new UsagePointHistoricalIssueDataValidationImpl(dataModel, usagePointIssueDataValidationService);
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
