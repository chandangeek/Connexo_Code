/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class IssueStatusEqualsContractTest extends EqualsContractTest {

    @Mock
    DataModel dataModel;
    @Mock
    Thesaurus thesaurus;

    IssueStatusImpl issueStatus;

    @Override
    protected Object getInstanceA() {
        if (issueStatus == null) {
            issueStatus = new IssueStatusImpl(dataModel, thesaurus);
            issueStatus.init("key", false, null);
        }
        return issueStatus;
    }

    @Override
    protected Object getInstanceEqualToA() {
        IssueStatusImpl issueStatus = new IssueStatusImpl(dataModel, thesaurus);
        issueStatus.init("key", false, null);
        return issueStatus;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        IssueStatusImpl issueStatus = new IssueStatusImpl(dataModel, thesaurus);
        issueStatus.init("another key", false, null);
        return Collections.singletonList(issueStatus);
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
