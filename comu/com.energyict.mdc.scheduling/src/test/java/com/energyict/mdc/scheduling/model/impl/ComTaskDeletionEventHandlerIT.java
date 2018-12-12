/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.scheduling.model.impl;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.elster.jupiter.devtools.persistence.test.rules.TransactionalRule;
import com.elster.jupiter.time.TemporalExpression;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.scheduling.model.ComSchedule;
import com.energyict.mdc.scheduling.model.ComScheduleBuilder;
import com.energyict.mdc.tasks.ComTask;

import java.time.Instant;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

/**
 * Integration test for the {@link ComTaskDeletionEventHandler} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-01-22 (13:54)
 */
public class ComTaskDeletionEventHandlerIT extends PersistenceTest {

    @Rule
    public TestRule transactionalRule = new TransactionalRule(inMemoryPersistence.getTransactionService());

    @Before
    public void registerEventHandlers () {
        inMemoryPersistence.registerEventHandlers();
    }

    @After
    public void unregisterEventHandlers () {
        inMemoryPersistence.unregisterEventHandlers();
    }

    @Test(expected = VetoDeleteComTaskException.class)
    @Transactional
    public void testCannotDeleteComTaskWhenUsedByComSchedule() throws Exception {
        // Create ComTask
        ComTask comTask = inMemoryPersistence.getTaskService().newComTask("Simple task");
        comTask.createStatusInformationTask();
        comTask.save();

        // Use the ComTask in a ComSchedule
        ComScheduleBuilder builder =
                inMemoryPersistence
                        .getSchedulingService()
                        .newComSchedule(
                                "testCannotDeleteComTaskWhenUsedByComSchedule",
                                new TemporalExpression(TimeDuration.minutes(15)),
                                Instant.now());
        ComSchedule comSchedule = builder.addComTask(comTask).build();

        // Business method
        comTask.delete();

        // Asserts: see expected exception rule
    }

}