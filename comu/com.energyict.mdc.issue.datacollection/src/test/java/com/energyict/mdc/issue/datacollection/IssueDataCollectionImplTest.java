package com.energyict.mdc.issue.datacollection;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.issue.impl.records.OpenIssueImpl;
import com.elster.jupiter.issue.share.Priority;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.OpenIssue;
import com.elster.jupiter.transaction.TransactionContext;
import com.energyict.mdc.issue.datacollection.entity.HistoricalIssueDataCollection;
import com.energyict.mdc.issue.datacollection.entity.OpenIssueDataCollection;
import com.energyict.mdc.issue.datacollection.impl.records.OpenIssueDataCollectionImpl;
import com.energyict.mdc.issue.datacollection.impl.ModuleConstants;

import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class IssueDataCollectionImplTest extends BaseTest {

    @Test
    @ExpectedConstraintViolation(messageId = "{com.elster.jupiter.orm.associations.isPresent}", property = "baseIssue")
    public void testIDCCreationWithoutBaseIssue() {
        try (TransactionContext context = getContext()) {
            OpenIssueDataCollectionImpl dcIssue = getDataModel().getInstance(OpenIssueDataCollectionImpl.class);
            dcIssue.save();
        }
    }

    @Test
    public void testIDCSuccessfullCreation() {
        try (TransactionContext context = getContext()) {
            CreationRule rule = getCreationRule("testIDCSuccessfullCreation", ModuleConstants.REASON_UNKNOWN_INBOUND_DEVICE);
            Issue baseIssue = createBaseIssue(rule);
            OpenIssueDataCollectionImpl dcIssue = getDataModel().getInstance(OpenIssueDataCollectionImpl.class);
            dcIssue.setIssue((OpenIssue) baseIssue);
            dcIssue.save();
        }
    }

    @Test
    public void testIDCCloseOperation() {
        OpenIssueDataCollectionImpl dcIssue = null;
        try (TransactionContext context = getContext()) {
            CreationRule rule = getCreationRule("testIDCCloseOperation", ModuleConstants.REASON_UNKNOWN_INBOUND_DEVICE);
            Issue baseIssue = createBaseIssue(rule);
            dcIssue = getDataModel().getInstance(OpenIssueDataCollectionImpl.class);
            dcIssue.setIssue((OpenIssue) baseIssue);
            dcIssue.setDeviceMRID("001234");
            dcIssue.save();
            context.commit();
        }
        try (TransactionContext context = getContext()) {
            HistoricalIssueDataCollection closed = dcIssue.close(getIssueService().findStatus(IssueStatus.RESOLVED).get());
            assertThat(closed.getId()).isEqualTo(dcIssue.getId());
            assertThat(closed.getDeviceMRID()).isEqualTo(dcIssue.getDeviceMRID());
            assertThat(closed.getCommunicationTask().orElse(null)).isEqualTo(dcIssue.getCommunicationTask().orElse(null));
            assertThat(closed.getConnectionTask().orElse(null)).isEqualTo(dcIssue.getConnectionTask().orElse(null));
            assertThat(closed.getReason().getKey()).isEqualTo(ModuleConstants.REASON_UNKNOWN_INBOUND_DEVICE);

            Optional<OpenIssueDataCollection> openIssueRef = getIssueDataCollectionService().findOpenIssue(dcIssue.getId());
            assertThat(openIssueRef.isPresent()).isFalse();

            Optional<HistoricalIssueDataCollection> historicalIssueRef = getIssueDataCollectionService().findHistoricalIssue(dcIssue.getId());
            assertThat(historicalIssueRef.isPresent()).isTrue();
        }
    }

    protected Issue createBaseIssue(CreationRule rule) {
        OpenIssueImpl baseIssue = getIssueDataModel().getInstance(OpenIssueImpl.class);
        baseIssue.setStatus(getIssueService().findStatus(IssueStatus.OPEN).get());
        baseIssue.setReason(rule.getReason());
        baseIssue.setRule(rule);
        baseIssue.setPriority(Priority.DEFAULT);
        baseIssue.save();
        return baseIssue;
    }
}
