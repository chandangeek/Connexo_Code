package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.impl.records.IssueStatusImpl;
import com.elster.jupiter.issue.impl.service.BaseTest;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.entity.NotUniqueKeyException;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.util.conditions.Condition;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class IssueStatusTest extends BaseTest{
    private static final int DEFAULT_STATUS_COUNT = 4;

    @Test
    public void checkDefaultStatuses(){
        Query<IssueStatus> issueQuery = getIssueService().query(IssueStatus.class);
        List<IssueStatus> issueList = issueQuery.select(Condition.TRUE);
        assertThat(issueList).hasSize(DEFAULT_STATUS_COUNT);

        Optional<IssueStatus> statusRef = getIssueService().findStatus(IssueStatus.OPEN);
        assertThat(statusRef.isPresent()).isTrue();
        IssueStatus status = statusRef.get();
        assertThat(status.getKey()).isEqualTo(IssueStatus.OPEN);
        assertThat(status.getName()).isEqualTo("Open");
        assertThat(status.isHistorical()).isFalse();
    }

    @Test
    @Transactional
    public void checkCreationWithTheSameTranslation(){
        getIssueService().createStatus("some.status.key", false, MessageSeeds.ISSUE_STATUS_OPEN);
        getIssueService().createStatus("another.status.key", false, MessageSeeds.ISSUE_STATUS_OPEN);
    }

    @Test(expected = NotUniqueKeyException.class)
    @Transactional
    public void checkCreationWithTheSameKey(){
        getIssueService().createStatus("status.key.one", false, MessageSeeds.ISSUE_STATUS_OPEN);
        getIssueService().createStatus("status.key.one", true, MessageSeeds.ISSUE_STATUS_OPEN);
    }

    @Test
    public void checkStatusDeletion(){
        String key = "status.key.for.deletion";
        try (TransactionContext context = getContext()) {
            getIssueService().createStatus(key, false, MessageSeeds.ISSUE_STATUS_OPEN);
            context.commit();
        }
        try (TransactionContext context = getContext()) {
            getIssueService().findStatus(key).get().delete();
            context.commit();
        }
        Optional<IssueStatus> statusRef = getIssueService().findStatus(key);
        assertThat(statusRef.isPresent()).isFalse();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    public void checkCreationWithNullTranslation(){
        getIssueService().createStatus("status.with.null.translation", false, null);
    }

    @Test
    public void checkCreation(){
        String key = "status.key.normal";
        try (TransactionContext context = getContext()) {
            getIssueService().createStatus(key, false, MessageSeeds.ISSUE_STATUS_OPEN);
            context.commit();
        }
        Optional<IssueStatus> statusRef = getIssueService().findStatus(key);
        assertThat(statusRef.isPresent()).isTrue();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    public void checkKeyValidation(){
        getDataModel().getInstance(IssueStatusImpl.class).init(null, false, MessageSeeds.ISSUE_STATUS_OPEN).save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    public void checkLongKeyValidation(){
        StringBuilder key = new StringBuilder();
        for (int i=0; i < 90; i++){
            key.append("q");
        }
        getDataModel().getInstance(IssueStatusImpl.class).init(key.toString(), false, MessageSeeds.ISSUE_STATUS_OPEN).save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}", property = "translationKey", strict = false)
    public void checkTranslationValidation(){
        getDataModel().getInstance(IssueStatusImpl.class).init("status.validation", false, null).save();
    }
}
