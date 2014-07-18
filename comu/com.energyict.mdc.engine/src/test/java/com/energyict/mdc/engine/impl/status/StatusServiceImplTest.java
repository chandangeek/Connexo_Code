package com.energyict.mdc.engine.impl.status;

import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.engine.status.ComServerStatus;

import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.impl.DefaultClock;
import com.google.common.base.Optional;

import org.junit.*;
import org.junit.runner.*;
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
    private EngineModelService engineModelService;
    private Clock clock = new DefaultClock();

    @Test
    public void testStatusWhenComServerWithSystemNameDoesNotExist () {
        when(this.engineModelService.findComServerBySystemName()).thenReturn(Optional.<ComServer>absent());
        StatusServiceImpl statusService = this.newStatusService();

        // Business method
        ComServerStatus status = statusService.getStatus();

        // Asserts
        verify(this.engineModelService).findComServerBySystemName();
        verifyNoMoreInteractions(this.managementBeanFactory);
        assertThat(status).isNotNull();
        assertThat(status.isRunning()).isFalse();
        assertThat(status.isBlocked()).isFalse();
        assertThat(status.getBlockTime()).isNull();
    }

    @Test
    public void testStatusOffNonRunningOnlineComServer () {
        when(this.engineModelService.findComServerBySystemName()).thenReturn(Optional.<ComServer>absent());
        StatusServiceImpl statusService = this.newStatusService();

        // Business method
        ComServerStatus status = statusService.getStatus();

        // Asserts
        verify(this.engineModelService).findComServerBySystemName();
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
        service.setEngineModelService(this.engineModelService);
        return service;
    }

}