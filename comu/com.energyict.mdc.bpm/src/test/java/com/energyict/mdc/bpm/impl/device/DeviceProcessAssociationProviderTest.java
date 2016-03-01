package com.energyict.mdc.bpm.impl.device;

import com.elster.jupiter.bpm.ProcessAssociationProvider;
import com.elster.jupiter.properties.PropertySpec;

import java.sql.SQLException;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by dragos on 2/26/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceProcessAssociationProviderTest {
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
        ProcessAssociationProvider provider = inMemoryPersistence.getDeviceAssociationProvider();
        assertEquals("device", provider.getType());
    }

    @Test
    public void testProviderPropertySpecNotAvailable() {
        ProcessAssociationProvider provider = inMemoryPersistence.getDeviceAssociationProvider();
        assertFalse(provider.getPropertySpec("unavailable").isPresent());
    }

    @Test
    public void testProviderDeviceStatePropertySpec() {
        /*ProcessAssociationProvider provider = inMemoryPersistence.getDeviceAssociationProvider();
        assertTrue(provider.getPropertySpec("deviceStates").isPresent());

        PropertySpec spec = provider.getPropertySpec("deviceStates").get();
        assertEquals("deviceStates", spec.getName());
        assertEquals("Device states", spec.getDisplayName());
        assertTrue(spec.isRequired());
        assertTrue(spec.supportsMultiValues());
        assertTrue(spec.getPossibleValues().isExhaustive());
        assertFalse(spec.getPossibleValues().isEditable());

        List<DeviceProcessAssociationProvider.DeviceStateInfo> values = spec.getPossibleValues().getAllValues();

        assertEquals(1L, (long)values.get(0).getId());
        assertEquals("Removed", values.get(0).getName());
        assertEquals(1L, (long)values.get(0).getLifeCycleId());
        assertEquals("Standard device life cycle", values.get(0).getLifeCycleName());

        assertEquals(2L, (long)values.get(1).getId());
        assertEquals("Decommissioned", values.get(1).getName());
        assertEquals(1L, (long)values.get(1).getLifeCycleId());
        assertEquals("Standard device life cycle", values.get(1).getLifeCycleName());

        assertEquals(3L, (long)values.get(2).getId());
        assertEquals("Active", values.get(2).getName());
        assertEquals(1L, (long)values.get(2).getLifeCycleId());
        assertEquals("Standard device life cycle", values.get(2).getLifeCycleName());

        assertEquals(4L, (long)values.get(3).getId());
        assertEquals("Inactive", values.get(3).getName());
        assertEquals(1L, (long)values.get(3).getLifeCycleId());
        assertEquals("Standard device life cycle", values.get(3).getLifeCycleName());

        assertEquals(5L, (long)values.get(4).getId());
        assertEquals("Comissioning", values.get(4).getName());
        assertEquals(1L, (long)values.get(4).getLifeCycleId());
        assertEquals("Standard device life cycle", values.get(4).getLifeCycleName());

        assertEquals(6L, (long)values.get(5).getId());
        assertEquals("In stock", values.get(5).getName());
        assertEquals(1L, (long)values.get(5).getLifeCycleId());
        assertEquals("Standard device life cycle", values.get(5).getLifeCycleName());*/
    }
}
