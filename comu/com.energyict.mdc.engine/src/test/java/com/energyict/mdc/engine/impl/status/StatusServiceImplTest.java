/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.status;

import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.engine.status.ComServerStatus;

import java.time.Clock;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link StatusServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-19 (13:49)
 */
@RunWith(MockitoJUnitRunner.class)
public class StatusServiceImplTest {

    @Mock
    private ManagementBeanFactory managementBeanFactory;
    @Mock
    private EngineConfigurationService engineConfigurationService;
    private Clock clock = Clock.systemDefaultZone();

    @Test
    public void testStatusWhenComServerWithSystemNameDoesNotExist () {
        when(this.engineConfigurationService.findComServerBySystemName()).thenReturn(Optional.empty());
        StatusServiceImpl statusService = this.newStatusService();

        // Business method
        ComServerStatus status = statusService.getStatus();

        // Asserts
        verify(this.engineConfigurationService).findComServerBySystemName();
        verifyNoMoreInteractions(this.managementBeanFactory);
        assertThat(status).isNotNull();
        assertThat(status.isRunning()).isFalse();
        assertThat(status.isBlocked()).isFalse();
        assertThat(status.getBlockTime()).isNull();
    }

    @Test
    public void testStatusOffNonRunningOnlineComServer () {
        when(this.engineConfigurationService.findComServerBySystemName()).thenReturn(Optional.empty());
        StatusServiceImpl statusService = this.newStatusService();

        // Business method
        ComServerStatus status = statusService.getStatus();

        // Asserts
        verify(this.engineConfigurationService).findComServerBySystemName();
        verifyNoMoreInteractions(this.managementBeanFactory);
        assertThat(status).isNotNull();
        assertThat(status.isRunning()).isFalse();
        assertThat(status.isBlocked()).isFalse();
        assertThat(status.getBlockTime()).isNull();
    }

    private StatusServiceImpl newStatusService () {
        StatusServiceImpl service = new StatusServiceImpl();
        service.setClock(this.clock);
        service.setManagementBeanFactory(this.managementBeanFactory);
        service.setEngineConfigurationService(this.engineConfigurationService);
        return service;
    }

}