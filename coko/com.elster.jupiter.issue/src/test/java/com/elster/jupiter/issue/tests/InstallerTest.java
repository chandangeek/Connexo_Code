package com.elster.jupiter.issue.tests;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.conditions.Condition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(MockitoJUnitRunner.class)
public class InstallerTest extends BaseTest {
    private static final int DEFAULT_STATUS_COUNT = 3;

    @Test
    public void testDefaultStatuses(){
        try (TransactionContext context = getContext()) {
            Query<IssueStatus> query = getIssueService().query(IssueStatus.class);
            List<IssueStatus> defaultStatuses = query.select(Condition.TRUE);
            assertThat(defaultStatuses).hasSize(DEFAULT_STATUS_COUNT);
        }
    }
}
