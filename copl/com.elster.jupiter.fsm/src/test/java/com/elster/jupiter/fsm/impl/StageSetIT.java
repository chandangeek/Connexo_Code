/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm.impl;

import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolation;
import com.elster.jupiter.devtools.persistence.test.rules.ExpectedConstraintViolationRule;
import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.fsm.MessageSeeds;
import com.elster.jupiter.fsm.StageSet;
import com.elster.jupiter.fsm.StageSetBuilder;
import com.elster.jupiter.transaction.TransactionService;
import com.google.common.base.Strings;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import java.sql.SQLException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for the {@link FiniteStateMachineImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-03-02 (17:12)
 */
public class StageSetIT {

    private static InMemoryPersistence inMemoryPersistence;
    @Rule
    public TestRule transactionalRule = new TransactionalRule(getTransactionService());
    @Rule
    public TestRule expectedConstraintViolationRule = new ExpectedConstraintViolationRule();


    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = InMemoryPersistence.defaultPersistence();
        inMemoryPersistence.initializeDatabase(StageSetIT.class.getSimpleName());
    }

    public static TransactionService getTransactionService() {
        return inMemoryPersistence.getTransactionService();
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "name")
    @Test
    public void stageSetCannotHaveANullName() {
        StageSetBuilder stageSetBuilder = this.getTestService().newStageSet(null);
        stageSetBuilder.stage("stage1").add();
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}", property = "name")
    @Test
    public void stageSetCannotHaveAnEmptyName() {
        StageSetBuilder stageSetBuilder = this.getTestService().newStageSet("");
        stageSetBuilder.stage("stage1").add();
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}", property = "name")
    @Test
    public void stageSetCannotHaveAnExtremelyLongName() {
        StageSetBuilder builder = this.getTestService().newStageSet(Strings.repeat("Too long", 100));

        builder.stage("stage1").add();
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.AT_LEAST_ONE_STAGE + "}", strict = false)
    @Test
    public void stageSetMustHaveAtLeastOneStage() {
        StageSetBuilder builder = this.getTestService().newStageSet("stageSetAtLeastOneStage");

        builder.add();
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.UNIQUE_STAGE_SET_NAME + "}")
    @Test
    public void createDuplicateStageSet() {
        String expectedName = "notUnique";
        StageSetBuilder builder = this.getTestService().newStageSet(expectedName);
        builder.stage("stage1").add();

        StageSetBuilder builder2 = this.getTestService().newStageSet(expectedName);
        builder2.stage("stage1").add();
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    @Test
    public void addStageWithNullName() {
        String expectedName = "addStageWithNullName";
        StageSetBuilder builder = this.getTestService().newStageSet(expectedName);
        builder.stage(null).add();
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.CAN_NOT_BE_EMPTY + "}")
    @Test
    public void addStageWithEmptyName() {
        String expectedName = "addStageWithEmptyName";
        StageSetBuilder builder = this.getTestService().newStageSet(expectedName);
        builder.stage("").add();

    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @Test
    public void addStageWithNameThatIsTooLong() {
        String expectedName = "addStageNameTooLong";
        StageSetBuilder builder = this.getTestService().newStageSet(expectedName);
        builder.stage(Strings.repeat("Too long", 100)).add();
    }

    @Transactional
    @ExpectedConstraintViolation(messageId = "{" + MessageSeeds.Keys.NO_DUPLICATE_STAGE_NAME + "}")
    @Test
    public void addStagesWithSameName() {
        String expectedName = "addStagesWithSameName";
        StageSetBuilder builder = this.getTestService().newStageSet(expectedName);
        builder.stage("Stage1").stage("Stage1").add();
    }

    @Transactional
    @Test
    public void successfulAdd() {
        String name = "ANameForAStageSet";
        StageSetBuilder builder = this.getTestService().newStageSet(name);
        builder.stage("Stage1").stage("Stage2").stage("Stage3").add();

        Optional<StageSet> stageSetOptional = getTestService().findStageSetByName(name);
        assertThat(stageSetOptional).isPresent();
        StageSet stageSet = stageSetOptional.get();
        assertThat(stageSet.getName()).isEqualTo(name);
        assertThat(stageSet.getStages()).hasSize(3);
    }

    private FiniteStateMachineServiceImpl getTestService() {
        return inMemoryPersistence.getFiniteStateMachineService();
    }
}