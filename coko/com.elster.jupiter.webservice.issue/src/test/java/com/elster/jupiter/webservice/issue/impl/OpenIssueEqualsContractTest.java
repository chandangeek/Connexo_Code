/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.impl.NlsModule;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.webservice.issue.impl.entity.WebServiceOpenIssueImpl;

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
    private DataModel dataModel;
    @Mock
    private IssueService issueService;
    private Thesaurus thesaurus = NlsModule.FakeThesaurus.INSTANCE;

    private OpenIssueImpl baseIssue;
    private WebServiceOpenIssueImpl webServiceIssue;

    @Override
    protected Object getInstanceA() {
        if (webServiceIssue == null) {
            baseIssue = new OpenIssueImpl(dataModel, issueService, Clock.systemDefaultZone(), thesaurus);
            baseIssue.setId(ID);
            webServiceIssue = new WebServiceOpenIssueImpl(dataModel);
            webServiceIssue.setIssue(baseIssue);
        }
        return webServiceIssue;
    }

    @Override
    protected Object getInstanceEqualToA() {
        OpenIssueImpl baseIssue = new OpenIssueImpl(dataModel, issueService, Clock.systemDefaultZone(), thesaurus);
        baseIssue.setId(ID);
        WebServiceOpenIssueImpl issueDataValidation = new WebServiceOpenIssueImpl(dataModel);
        issueDataValidation.setIssue(baseIssue);
        return issueDataValidation;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        OpenIssueImpl baseIssue = new OpenIssueImpl(dataModel, issueService, Clock.systemDefaultZone(), thesaurus);
        baseIssue.setId(OTHER_ID);
        WebServiceOpenIssueImpl issueDataValidation = new WebServiceOpenIssueImpl(dataModel);
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
