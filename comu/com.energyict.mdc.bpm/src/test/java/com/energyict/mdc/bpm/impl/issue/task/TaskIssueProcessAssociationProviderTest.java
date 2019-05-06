/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.bpm.impl.issue.task;

import com.elster.jupiter.bpm.ProcessAssociationProvider;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.bpm.impl.InMemoryPersistence;
import com.energyict.mdc.bpm.impl.issue.devicelifecycle.IssueLifecycleProcessAssociationProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Laurentia on 4/24/2019.
 */
@RunWith(MockitoJUnitRunner.class)
public class TaskIssueProcessAssociationProviderTest {
    private static InMemoryPersistence inMemoryPersistence;

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = InMemoryPersistence.defaultPersistence();
        inMemoryPersistence.initializeDatabase(TaskIssueProcessAssociationProvider.class.getSimpleName());
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    @Test
    public void testProviderType() {
        ProcessAssociationProvider provider = inMemoryPersistence.getTaskAssociationProvider();
        assertEquals("taskissue", provider.getType());
    }

    @Test
    public void testProviderPropertySpecNotAvailable() {
        ProcessAssociationProvider provider = inMemoryPersistence.getTaskAssociationProvider();
        assertFalse(provider.getPropertySpec("unavailable").isPresent());
    }

    @Test
    public void testProviderDeviceStatePropertySpec() {
        ProcessAssociationProvider provider = inMemoryPersistence.getTaskAssociationProvider();
        assertTrue(provider.getPropertySpec("issueReasons").isPresent());

        PropertySpec spec = provider.getPropertySpec("issueReasons").get();
        assertEquals("issueReasons", spec.getName());
        assertEquals("Issue reasons", spec.getDisplayName());
        assertTrue(spec.isRequired());
        assertTrue(spec.supportsMultiValues());
        assertTrue(spec.getPossibleValues().isExhaustive());
        assertFalse(spec.getPossibleValues().isEditable());

        List<TaskIssueProcessAssociationProvider.IssueReasonInfo> values = spec.getPossibleValues().getAllValues();
         assertEquals("reason.task.failed", values.get(0).getId());
         assertEquals("TaskFailed", values.get(0).getName());

    }
}
