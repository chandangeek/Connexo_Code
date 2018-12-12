/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import static org.assertj.core.api.Assertions.assertThat;

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
        assertThat(provider.getType()).isEqualTo("device");
    }

    @Test
    public void testProviderPropertySpecNotAvailable() {
        ProcessAssociationProvider provider = inMemoryPersistence.getDeviceAssociationProvider();
        assertThat(provider.getPropertySpec("unavailable").isPresent()).isFalse();
    }

    @Test
    public void testProviderDeviceStatePropertySpec() {
        ProcessAssociationProvider provider = inMemoryPersistence.getDeviceAssociationProvider();

        assertThat(provider.getPropertySpec("deviceStates")).isPresent();

        PropertySpec spec = provider.getPropertySpec("deviceStates").get();
        assertThat(spec.getName()).isEqualTo("deviceStates");
        assertThat(spec.getDisplayName()).isEqualTo("Device states");
        assertThat(spec.isRequired()).isTrue();
        assertThat(spec.supportsMultiValues()).isTrue();
        assertThat(spec.getPossibleValues().isExhaustive()).isTrue();
        assertThat(spec.getPossibleValues().isEditable()).isFalse();

        List<DeviceProcessAssociationProvider.DeviceStateInfo> values = spec.getPossibleValues().getAllValues();

        assertThat(values.get(0).getName()).isEqualTo("Active");
        assertThat(values.get(0).getLifeCycleName()).isEqualTo("Standard device life cycle");

        assertThat(values.get(1).getName()).isEqualTo("Commissioning");
        assertThat(values.get(1).getLifeCycleName()).isEqualTo("Standard device life cycle");

        assertThat(values.get(2).getName()).isEqualTo("Decommissioned");
        assertThat(values.get(2).getLifeCycleName()).isEqualTo("Standard device life cycle");

        assertThat(values.get(3).getName()).isEqualTo("In stock");
        assertThat(values.get(3).getLifeCycleName()).isEqualTo("Standard device life cycle");

        assertThat(values.get(4).getName()).isEqualTo("Inactive");
        assertThat(values.get(4).getLifeCycleName()).isEqualTo("Standard device life cycle");

        assertThat(values.get(5).getName()).isEqualTo("Removed");
        assertThat(values.get(5).getLifeCycleName()).isEqualTo("Standard device life cycle");
    }

}