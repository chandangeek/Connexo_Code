/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.devtools.tests.EqualsContractTest;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.DataModel;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class IssueReasonEqualsContractTest extends EqualsContractTest {

    @Mock
    DataModel dataModel;
    @Mock
    Thesaurus thesaurus;

    IssueReasonImpl issueReason;

    @Override
    protected Object getInstanceA() {
        if (issueReason == null) {
            issueReason = new IssueReasonImpl(dataModel, thesaurus);
            issueReason.init("key", mock(IssueType.class), mock(TranslationKey.class), mock(TranslationKey.class));
        }
        return issueReason;
    }

    @Override
    protected Object getInstanceEqualToA() {
        IssueReasonImpl issueReason = new IssueReasonImpl(dataModel, thesaurus);
        issueReason.init("key", mock(IssueType.class), mock(TranslationKey.class), mock(TranslationKey.class));
        return issueReason;
    }

    @Override
    protected Iterable<?> getInstancesNotEqualToA() {
        IssueReasonImpl issueReason = new IssueReasonImpl(dataModel, thesaurus);
        issueReason.init("another key", mock(IssueType.class), mock(TranslationKey.class), mock(TranslationKey.class));
        return Collections.singletonList(issueReason);
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
