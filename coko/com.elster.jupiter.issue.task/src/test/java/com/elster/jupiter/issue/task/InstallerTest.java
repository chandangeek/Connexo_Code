/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.conditions.Condition;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class InstallerTest extends BaseTest {
    private static final int DEFAULT_REASON_COUNT = 1;
    
    @Test
    public void testDefaultReasons(){
        try (TransactionContext context = getContext()) {
            Query<IssueReason> query = getIssueService().query(IssueReason.class);
            List<IssueReason> dafaultReasons = query.select(Condition.TRUE);
            assertThat(dafaultReasons).hasSize(DEFAULT_REASON_COUNT);
        }
    }
}
