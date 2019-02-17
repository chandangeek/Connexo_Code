/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.lifecycle.DeviceLifeCycleService;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.DeviceMicroCheckFactoryImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceMicroCheckFactoryImpl} component.
 */
@RunWith(MockitoJUnitRunner.class)
public class MicroCheckFactoryImplTest {

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private NlsService nlsService;

    @Before
    public void initializeMocks() {
        when(this.nlsService.getThesaurus(DeviceLifeCycleService.COMPONENT_NAME, Layer.DOMAIN)).thenReturn(this.thesaurus);
    }

    @Test
    public void constructorExtractsThesaurus() {
        // Business method
        this.getTestInstance();

        // Asserts
        verify(this.nlsService).getThesaurus(DeviceLifeCycleService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Test
    public void allMicroChecksAreCovered() {
        DeviceMicroCheckFactoryImpl factory = this.getTestInstance();

        for (MicroCheck microCheck : MicroCheck.values()) {
            // Business method
            ServerMicroCheck serverMicroCheck = factory.from(microCheck);

            // Asserts
            assertThat(serverMicroCheck).as("MicroCheckFactoryImpl returns null for " + microCheck).isNotNull();
        }
    }

    private DeviceMicroCheckFactoryImpl getTestInstance() {
        return new DeviceMicroCheckFactoryImpl();
    }
}