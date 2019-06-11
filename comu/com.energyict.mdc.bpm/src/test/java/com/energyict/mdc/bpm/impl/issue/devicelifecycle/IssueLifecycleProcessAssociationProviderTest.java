/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.bpm.impl.issue.devicelifecycle;

import com.elster.jupiter.bpm.ProcessAssociationProvider;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.bpm.impl.InMemoryPersistence;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by Laurentia on 3/29/2019.
 */
@RunWith(MockitoJUnitRunner.class)
public class IssueLifecycleProcessAssociationProviderTest {
    private static InMemoryPersistence inMemoryPersistence;

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = InMemoryPersistence.defaultPersistence();
        inMemoryPersistence.initializeDatabase(IssueLifecycleProcessAssociationProvider.class.getSimpleName());
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    @Test
    public void testProviderType() {
        ProcessAssociationProvider provider = inMemoryPersistence.getLifecycleAssociationProvider();
        assertEquals("devicelifecycleissue", provider.getType());
    }

    @Test
    public void testProviderPropertySpecNotAvailable() {
        ProcessAssociationProvider provider = inMemoryPersistence.getLifecycleAssociationProvider();
        assertFalse(provider.getPropertySpec("unavailable").isPresent());
    }

    @Test
    public void testProviderDeviceStatePropertySpec() {
        ProcessAssociationProvider provider = inMemoryPersistence.getLifecycleAssociationProvider();
        assertTrue(provider.getPropertySpec("issueReasons").isPresent());

        PropertySpec spec = provider.getPropertySpec("issueReasons").get();
        assertEquals("issueReasons", spec.getName());
        assertEquals("Issue reasons", spec.getDisplayName());
        assertTrue(spec.isRequired());
        assertTrue(spec.supportsMultiValues());
        assertTrue(spec.getPossibleValues().isExhaustive());
        assertFalse(spec.getPossibleValues().isEditable());

        List<IssueLifecycleProcessAssociationProvider.IssueReasonInfo> values = spec.getPossibleValues().getAllValues();

        assertEquals("reason.device.lifecycle.transition.failure", values.get(0).getId());
        assertEquals("DeviceLifecycleIssueReason", values.get(0).getName());
    }
}
