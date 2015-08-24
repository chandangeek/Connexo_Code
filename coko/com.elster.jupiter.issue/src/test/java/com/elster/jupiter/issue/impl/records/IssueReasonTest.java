package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.impl.service.BaseTest;
import com.elster.jupiter.issue.share.entity.IssueReason;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.NotUniqueKeyException;
import com.elster.jupiter.transaction.TransactionContext;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class IssueReasonTest extends BaseTest{

    private IssueType getDefaultIssueType(){
        return getIssueService().findIssueType(ISSUE_DEFAULT_TYPE_UUID).get();
    }

    @Test
    @Transactional
    public void checkCreationWithTheSameTranslation(){
        getIssueService().createReason("some.reason.key", getDefaultIssueType(), MESSAGE_SEED_DEFAULT_TRANSLATION, MESSAGE_SEED_DEFAULT_TRANSLATION);
        getIssueService().createReason("another.reason.key", getDefaultIssueType(), MESSAGE_SEED_DEFAULT_TRANSLATION, MESSAGE_SEED_DEFAULT_TRANSLATION);
    }

    @Test(expected = NotUniqueKeyException.class)
    @Transactional
    public void checkCreationWithTheSameKey(){
        getIssueService().createReason("reason.key.one", getDefaultIssueType(), MESSAGE_SEED_DEFAULT_TRANSLATION, MESSAGE_SEED_DEFAULT_TRANSLATION);
        getIssueService().createReason("reason.key.one", getDefaultIssueType(), MESSAGE_SEED_DEFAULT_TRANSLATION, MESSAGE_SEED_DEFAULT_TRANSLATION);
    }

    @Test
    public void checkReasonDeletion(){
        String key = "reason.key.for.deletion";
        try (TransactionContext context = getContext()) {
            getIssueService().createReason(key, getDefaultIssueType(), MESSAGE_SEED_DEFAULT_TRANSLATION, MESSAGE_SEED_DEFAULT_TRANSLATION);
            context.commit();
        }
        try (TransactionContext context = getContext()) {
            getIssueService().findReason(key).get().delete();
            context.commit();
        }
        Optional<IssueReason> reasonRef = getIssueService().findReason(key);
        assertThat(reasonRef.isPresent()).isFalse();
    }

    @Test(expected = IllegalArgumentException.class)
    @Transactional
    public void checkCreationWithNullTranslation(){
        getIssueService().createReason("reason.with.null.translation", getDefaultIssueType(), null, null);
    }

    @Test
    public void checkCreation(){
        String key = "reason.key.normal";
        try (TransactionContext context = getContext()) {
            getIssueService().createReason(key, getDefaultIssueType(), MESSAGE_SEED_DEFAULT_TRANSLATION, MESSAGE_SEED_DEFAULT_TRANSLATION);
            context.commit();
        }
        Optional<IssueReason> reasonRef = getIssueService().findReason(key);
        assertThat(reasonRef.isPresent()).isTrue();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    public void checkKeyValidation(){
        getDataModel().getInstance(IssueReasonImpl.class).init(null, getDefaultIssueType(), MESSAGE_SEED_DEFAULT_TRANSLATION, MESSAGE_SEED_DEFAULT_TRANSLATION).save();
    }


    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_80 + "}")
    public void checkLongKeyValidation(){
        StringBuilder key = new StringBuilder();
        for (int i=0; i < 90; i++){
            key.append("q");
        }
        getDataModel().getInstance(IssueReasonImpl.class).init(key.toString(), getDefaultIssueType(), MESSAGE_SEED_DEFAULT_TRANSLATION, MESSAGE_SEED_DEFAULT_TRANSLATION).save();
    }


    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}", property = "translationKey", strict = false)
    public void checkTranslationValidation(){
        getDataModel().getInstance(IssueReasonImpl.class).init("reason.validation", getDefaultIssueType(), null, null).save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_SIZE_BETWEEN_1_AND_200 + "}", property = "defaultName", strict = false)
    public void checkDefaultNameValidation(){
        getDataModel().getInstance(IssueReasonImpl.class).init("reason.validation", getDefaultIssueType(), null, null).save();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}", property = "issueType", strict = true)
    public void checkIssueTypeValidation(){
        getDataModel().getInstance(IssueReasonImpl.class).init("reason.validation", null, MESSAGE_SEED_DEFAULT_TRANSLATION, MESSAGE_SEED_DEFAULT_TRANSLATION).save();
    }
}
