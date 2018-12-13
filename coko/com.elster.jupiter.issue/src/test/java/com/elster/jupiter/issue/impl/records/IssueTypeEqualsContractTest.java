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
public class IssueTypeEqualsContractTest extends EqualsContractTest {

    @Mock
    DataModel dataModel;
    @Mock
    Thesaurus thesaurus;

    IssueTypeImpl issueType;

    @Override
    protected Object getInstanceA() {
        if (issueType == null) {
            issueType = new IssueTypeImpl(dataModel, thesaurus);
            issueType.init("key", null, "TST");
        }
        return issueType;
    }

    @Override
    protected Object getInstanceEqualToA() {
        IssueTypeImpl issueType = new IssueTypeImpl(dataModel, thesaurus);
        issueType.init("key", null, "TST");
        return issueType;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        IssueTypeImpl issueType = new IssueTypeImpl(dataModel, thesaurus);
        issueType.init("another key", null, "TST");
        return Collections.singletonList(issueType);
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
