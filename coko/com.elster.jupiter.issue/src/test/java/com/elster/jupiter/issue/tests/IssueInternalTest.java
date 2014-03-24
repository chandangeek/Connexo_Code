package com.elster.jupiter.issue.tests;


import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.transaction.TransactionContext;
import com.google.common.base.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class IssueInternalTest extends BaseTest{
    @Test
    public void test(){
        try (TransactionContext context = getTransactionService().getContext()) {
            String newReason = "ReasonName";
            String newTopic = "ReasonTopic";

            IssueReason reas = new IssueReason();
            reas.setName(newReason);
            reas.setTopic(newTopic);
            Optional<IssueReason> reason = getIssueMainService().save(reas);
            Optional<IssueReason> reason2 = getIssueMainService().get(IssueReason.class, reason.get().getId());
            assertThat(reason2.isPresent()).isTrue();
            assertThat(reason2.get().getName()).isEqualTo(newReason);

            reason2 = getIssueMainService().delete(IssueReason.class, reason.get().getId());
            assertThat(reason2.isPresent()).isTrue();

            reason2 = getIssueMainService().get(IssueReason.class, reason.get().getId());
            assertThat(reason2.isPresent()).isFalse();

            reas = new IssueReason();
            reas.setName(newReason);
            reas.setTopic(newTopic);
            reason2 = getIssueMainService().save(reas);
            assertThat(reason2.isPresent()).isTrue();
            assertThat(reason2.get().getName()).isEqualTo(newReason);

            reason2 = getIssueMainService().get(IssueReason.class, reason2.get().getId());
            assertThat(reason2.isPresent()).isTrue();
            assertThat(reason2.get().getName()).isEqualTo(newReason);


            reas.setName("Name2");
            reason2 = getIssueMainService().update(reas);
            assertThat(reason2.isPresent()).isTrue();
            assertThat(reason2.get().getName()).isEqualTo("Name2");
            reason2 = getIssueMainService().get(IssueReason.class, reason2.get().getId());
            assertThat(reason2.isPresent()).isTrue();
            assertThat(reason2.get().getName()).isEqualTo("Name2");
        }
    }


}

