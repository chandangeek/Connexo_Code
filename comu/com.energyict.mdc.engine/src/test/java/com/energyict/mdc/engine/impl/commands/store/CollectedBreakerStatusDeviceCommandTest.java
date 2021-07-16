/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.upl.meterdata.BreakerStatus;
import com.energyict.mdc.upl.meterdata.CollectedBreakerStatus;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.elster.jupiter.devtools.tests.assertions.JupiterAssertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author sva
 * @since 7/04/2016 - 16:24
 */
@RunWith(MockitoJUnitRunner.class)
public class CollectedBreakerStatusDeviceCommandTest {

    @Mock
    private ComTaskExecution comTaskExecution;
    @Mock
    private DeviceCommand.ServiceProvider serviceProvider;
    @Mock
    public ComServerDAO comServerDAO;

    @Test
    public void handOverToComServerDAOTest() {
        CollectedBreakerStatus collectedBreakerStatus = getSafeMockedCollectedBreakerStatus();
        CollectedBreakerStatusDeviceCommand collectedBreakerStatusDeviceCommand = new CollectedBreakerStatusDeviceCommand(serviceProvider, collectedBreakerStatus, comTaskExecution);

        // Business method
        collectedBreakerStatusDeviceCommand.doExecute(comServerDAO);

        //asserts
        verify(comServerDAO).updateBreakerStatus(collectedBreakerStatus, true, true);
    }

    @Test
    public void testToJournalMessageDescription() throws Exception {
        CollectedBreakerStatus collectedBreakerStatus = getSafeMockedCollectedBreakerStatus();
        CollectedBreakerStatusDeviceCommand collectedBreakerStatusDeviceCommand = new CollectedBreakerStatusDeviceCommand(serviceProvider, collectedBreakerStatus, comTaskExecution);

        // Business method
        String journalMessageDescription = collectedBreakerStatusDeviceCommand.toJournalMessageDescription(ComServer.LogLevel.INFO);

        // Asserts
        assertThat(journalMessageDescription).contains("Collected breaker status");
        assertThat(journalMessageDescription).contains("disconnected");
    }

    public CollectedBreakerStatus getSafeMockedCollectedBreakerStatus() {
        CollectedBreakerStatus collectedBreakerStatus = mock(CollectedBreakerStatus.class);
        when(collectedBreakerStatus.getBreakerStatus()).thenReturn(Optional.of(BreakerStatus.DISCONNECTED));
        return collectedBreakerStatus;
    }
}