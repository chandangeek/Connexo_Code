/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.bpm.impl.issue.datacollection;

import com.elster.jupiter.bpm.ProcessAssociationProvider;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.bpm.impl.InMemoryPersistence;
import com.energyict.mdc.bpm.impl.device.DeviceProcessAssociationProviderTest;

import java.sql.SQLException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by dragos on 2/26/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class IssueProcessAssociationProviderTest {
    private static InMemoryPersistence inMemoryPersistence;

    @BeforeClass
    public static void initialize() {
        inMemoryPersistence = InMemoryPersistence.defaultPersistence();
        inMemoryPersistence.initializeDatabase(DeviceProcessAssociationProviderTest.class.getSimpleName());
    }

    @AfterClass
    public static void cleanUpDataBase() throws SQLException {
        inMemoryPersistence.cleanUpDataBase();
    }

    @Test
    public void testProviderType() {
        ProcessAssociationProvider provider = inMemoryPersistence.getIssueAssociationProvider();
        assertEquals("datacollectionissue", provider.getType());
    }

    @Test
    public void testProviderPropertySpecNotAvailable() {
        ProcessAssociationProvider provider = inMemoryPersistence.getIssueAssociationProvider();
        assertFalse(provider.getPropertySpec("unavailable").isPresent());
    }

    @Test
    public void testProviderDeviceStatePropertySpec() {
        ProcessAssociationProvider provider = inMemoryPersistence.getIssueAssociationProvider();
        assertTrue(provider.getPropertySpec("issueReasons").isPresent());

        PropertySpec spec = provider.getPropertySpec("issueReasons").get();
        assertEquals("issueReasons", spec.getName());
        assertEquals("Issue reasons", spec.getDisplayName());
        assertTrue(spec.isRequired());
        assertTrue(spec.supportsMultiValues());
        assertTrue(spec.getPossibleValues().isExhaustive());
        assertFalse(spec.getPossibleValues().isEditable());

        List<IssueProcessAssociationProvider.IssueReasonInfo> values = spec.getPossibleValues().getAllValues();

        assertEquals("reason.connection.failed", values.get(0).getId());
        assertEquals("IssueReasonConnectionFailed", values.get(0).getName());

        assertEquals("reason.connection.setup.failed", values.get(1).getId());
        assertEquals("IssueReasonConnectionSetupFailed", values.get(1).getName());

        assertEquals("reason.failed.to.communicate", values.get(2).getId());
        assertEquals("IssueReasonFailedToCommunicate", values.get(2).getName());

        assertEquals("reason.power.outage", values.get(3).getId());
        assertEquals("IssueReasonPowerOutage", values.get(3).getName());

        assertEquals("reason.tyme.sync.failed", values.get(4).getId());
        assertEquals("IssueReasonSyncFailed", values.get(4).getName());

        assertEquals("reason.unknown.inbound.device", values.get(5).getId());
        assertEquals("IssueReasonUnknownInboundDevice", values.get(5).getName());

        assertEquals("reason.unknown.outbound.device", values.get(6).getId());
        assertEquals("IssueReasonUnknownOutboundDevice", values.get(6).getName());
    }
}
