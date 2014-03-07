package com.elster.jupiter.issue.tests;


import com.elster.jupiter.issue.share.entity.HistoricalIssue;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

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

            reas = new IssueReason();
            reas.setName("name2");
            List<IssueReason> list = getIssueMainService().search(reas);
            assertThat(list).isNotNull();
            assertThat(list.size()).isEqualTo(1);

            reason2 = getIssueMainService().delete(reason2.get());
            assertThat(reason2.isPresent()).isTrue();
            reason2 = getIssueMainService().get(IssueReason.class, reason2.get().getId());
            assertThat(reason2.isPresent()).isFalse();


            HistoricalIssue hist = new HistoricalIssue();
            hist.setStatus(getIssueMainService().get(IssueStatus.class, 1).get());
            hist.setReason(getIssueMainService().get(IssueReason.class, 1).get());
            hist.setDueDate(new UtcInstant(5L));
            getIssueMainService().save(hist);

            Optional<HistoricalIssue> history = getIssueMainService().get(HistoricalIssue.class, 1);
            assertThat(history.isPresent()).isTrue();
            assertThat(history.get().getDueDate().getTime()).isEqualTo(5L);

        }
    }


}

