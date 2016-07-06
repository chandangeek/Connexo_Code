package com.energyict.mdc.bpm.impl.device;

import com.elster.jupiter.bpm.ProcessAssociationProvider;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.bpm.impl.InMemoryPersistence;

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
        ProcessAssociationProvider provider = inMemoryPersistence.getDeviceAssociationProvider();

        assertTrue(provider.getPropertySpec("deviceStates").isPresent());

        PropertySpec spec = provider.getPropertySpec("deviceStates").get();
        assertEquals("deviceStates", spec.getName());
        assertEquals("Device states", spec.getDisplayName());
        assertTrue(spec.isRequired());
        assertTrue(spec.supportsMultiValues());
        assertTrue(spec.getPossibleValues().isExhaustive());
        assertFalse(spec.getPossibleValues().isEditable());

        List<DeviceProcessAssociationProvider.DeviceStateInfo> values = spec.getPossibleValues().getAllValues();

        assertEquals("dlc.default.active", values.get(0).getName());
        assertEquals("Standard device life cycle", values.get(0).getLifeCycleName());

        assertEquals("dlc.default.commissioning", values.get(1).getName());
        assertEquals("Standard device life cycle", values.get(1).getLifeCycleName());

        assertEquals("dlc.default.decommissioned", values.get(2).getName());
        assertEquals("Standard device life cycle", values.get(2).getLifeCycleName());

        assertEquals("dlc.default.inactive", values.get(3).getName());
        assertEquals("Standard device life cycle", values.get(3).getLifeCycleName());

        assertEquals("dlc.default.inStock", values.get(4).getName());
        assertEquals("Standard device life cycle", values.get(4).getLifeCycleName());

        assertEquals("dlc.default.removed", values.get(5).getName());
        assertEquals("Standard device life cycle", values.get(5).getLifeCycleName());
    }
}