/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.issue.impl.module.MessageSeeds;
import com.elster.jupiter.issue.impl.service.BaseTest;
import com.elster.jupiter.issue.share.entity.IssueType;
import com.elster.jupiter.issue.share.entity.NotUniqueKeyException;
import com.elster.jupiter.transaction.TransactionContext;

import junit.framework.*;

import java.util.Optional;

import org.junit.*;
import org.junit.Test;
import org.junit.runner.*;
import org.mockito.runners.MockitoJUnitRunner;

import static junit.framework.TestCase.assertEquals;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class IssueTypeTest extends BaseTest {

    @Test
    @Transactional
    public void checkCreationWithTheSameTranslation(){
        getIssueService().createIssueType("some.type.key", MESSAGE_SEED_DEFAULT_TRANSLATION, "TST");
        getIssueService().createIssueType("another.type.key", MESSAGE_SEED_DEFAULT_TRANSLATION, "TST");
    }

    @Test(expected = NotUniqueKeyException.class)
    @Transactional
    public void checkCreationWithTheSameKey(){
        getIssueService().createIssueType("type.key.one", MESSAGE_SEED_DEFAULT_TRANSLATION, "TST");
        getIssueService().createIssueType("type.key.one", MESSAGE_SEED_DEFAULT_TRANSLATION, "TST");
    }

    @Test
    public void checkIssueTypeDeletion(){
        String key = "type.key.for.deletion";
        try (TransactionContext context = getContext()) {
            getIssueService().createIssueType(key, MESSAGE_SEED_DEFAULT_TRANSLATION, "TST");
            context.commit();
        }
        try (TransactionContext context = getContext()) {
            getIssueService().findIssueType(key).get().delete();
            context.commit();
        }
        Optional<IssueType> typeRef = getIssueService().findIssueType(key);
        assertThat(typeRef.isPresent()).isFalse();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    public void checkCreationWithNullTranslation(){
        getIssueService().createIssueType("type.with.null.translation", null, "TST");
    }

    @Test
    public void checkCreation(){
        String key = "type.key.normal";
        try (TransactionContext context = getContext()) {
            getIssueService().createIssueType(key, MESSAGE_SEED_DEFAULT_TRANSLATION, "TST");
            context.commit();
        }
        Optional<IssueType> typeRef = getIssueService().findIssueType(key);
        assertThat(typeRef.isPresent()).isTrue();
        assertEquals("TST", typeRef.get().getPrefix());
        assertEquals(MESSAGE_SEED_DEFAULT_TRANSLATION.getKey(), typeRef.get().getName());
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    public void checkKeyValidation(){
        getDataModel().getInstance(IssueTypeImpl.class).init(null, MESSAGE_SEED_DEFAULT_TRANSLATION, "TST").update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    public void checkLongKeyValidation(){
        StringBuilder key = new StringBuilder();
        for (int i=0; i < 90; i++){
            key.append("q");
        }
        getDataModel().getInstance(IssueTypeImpl.class)
                .init(key.toString(), MESSAGE_SEED_DEFAULT_TRANSLATION, "TST")
                .update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}", property = "translationKey", strict = false)
    public void checkTranslationValidation(){
        getDataModel().getInstance(IssueTypeImpl.class).init("type.validation", null, "TST").update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_CAN_NOT_BE_EMPTY + "}")
    public void checkPrefixValidation() {
        getDataModel().getInstance(IssueTypeImpl.class)
                .init("type.validation", MESSAGE_SEED_DEFAULT_TRANSLATION, null)
                .update();
    }

    @Test
    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    public void checkLongPrefixValidation() {
        getDataModel().getInstance(IssueTypeImpl.class)
                .init("type.validation", MESSAGE_SEED_DEFAULT_TRANSLATION, "TSTX")
                .update();
    }
}
