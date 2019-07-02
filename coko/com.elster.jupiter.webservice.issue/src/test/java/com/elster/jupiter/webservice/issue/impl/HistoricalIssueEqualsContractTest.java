/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.issue.impl.records.HistoricalIssueImpl;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.webservice.issue.impl.entity.WebServiceHistoricalIssueImpl;

import java.time.Clock;
import java.util.Collections;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HistoricalIssueEqualsContractTest extends EqualsContractTest {

    private static final long ID = 651L;
    private static final long OTHER_ID = 426294L;

    @Mock
    private DataModel dataModel;
    @Mock
    private IssueService issueService;
    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;

    private HistoricalIssueImpl baseIssue;
    private WebServiceHistoricalIssueImpl webServiceIssue;

    @Override
    protected Object getInstanceA() {
        if (webServiceIssue == null) {
            baseIssue = new HistoricalIssueImpl(dataModel, issueService, Clock.systemDefaultZone(), thesaurus);
            baseIssue.setId(ID);
            webServiceIssue = new WebServiceHistoricalIssueImpl(dataModel);
            webServiceIssue.setIssue(baseIssue);
        }
        return webServiceIssue;
    }

    @Override
    protected Object getInstanceEqualToA() {
        HistoricalIssueImpl baseIssue = new HistoricalIssueImpl(dataModel, issueService, Clock.systemDefaultZone(), thesaurus);
        baseIssue.setId(ID);
        WebServiceHistoricalIssueImpl issueDataValidation = new WebServiceHistoricalIssueImpl(dataModel);
        issueDataValidation.setIssue(baseIssue);
        return issueDataValidation;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        HistoricalIssueImpl baseIssue = new HistoricalIssueImpl(dataModel, issueService, Clock.systemDefaultZone(), thesaurus);
        baseIssue.setId(OTHER_ID);
        WebServiceHistoricalIssueImpl issueDataValidation = new WebServiceHistoricalIssueImpl(dataModel);
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
