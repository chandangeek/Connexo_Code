/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.device.lifecycle.impl;

import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.lifecycle.ExecutableMicroCheck;
import com.energyict.mdc.device.lifecycle.config.MicroCheck;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.ActiveConnectionAvailable;
import com.energyict.mdc.device.lifecycle.impl.micro.checks.DeviceMicroCheckFactoryImpl;

import java.util.Optional;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
    @Mock
    private DataModel dataModel;

    @Before
    public void initializeMocks() {
        when(dataModel.getInstance(any(Class.class))).thenAnswer(invocationOnMock -> mockMicroCheck(invocationOnMock.getArgumentAt(0, Class.class)));
    }

    @Test
    public void testAllChecks() {
        DeviceMicroCheckFactoryImpl factory = this.getTestInstance();
        Set<? extends MicroCheck> microChecks = factory.getAllChecks();

        verify(dataModel, times(19)).getInstance(any());
        assertThat(microChecks).hasSize(19);
    }

    @Test
    public void testOneCheck() {
        DeviceMicroCheckFactoryImpl factory = this.getTestInstance();
        Optional<ExecutableMicroCheck> serverMicroCheck = factory.from(ActiveConnectionAvailable.class.getSimpleName())
                .map(ExecutableMicroCheck.class::cast);

        verify(dataModel).getInstance(ActiveConnectionAvailable.class);
        assertThat(serverMicroCheck).isPresent();
        assertThat(serverMicroCheck.get().getKey()).isEqualTo(ActiveConnectionAvailable.class.getSimpleName());
    }

    private DeviceMicroCheckFactoryImpl getTestInstance() {
        return new DeviceMicroCheckFactoryImpl(dataModel);
    }

    private ExecutableMicroCheck mockMicroCheck(Class checkClass) {
        ExecutableMicroCheck microCheck = mock(ExecutableMicroCheck.class);
        when(microCheck.getKey()).thenReturn(checkClass.getSimpleName());
        return microCheck;
    }
}
