package com.energyict.mdc.engine.impl.scheduling;

import com.energyict.mdc.engine.impl.core.ServerProcessStatus;
import org.junit.*;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link VoidTimeOutMonitor} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-03 (14:39)
 */
public class VoidTimeOutMonitorTest {

    @Test
    public void testGetStatusAfterConstruction () {
        // Business method
        VoidTimeOutMonitor timeOutMonitor = new VoidTimeOutMonitor();

        // Asserts
        assertThat(timeOutMonitor.getStatus()).isEqualTo(ServerProcessStatus.STARTING);
    }

    @Test
    public void testStart () {
        VoidTimeOutMonitor timeOutMonitor = new VoidTimeOutMonitor();

        // Business method
        timeOutMonitor.start();

        // Asserts
        assertThat(timeOutMonitor.getStatus()).isEqualTo(ServerProcessStatus.STARTED);
    }

    @Test
    public void testShutdown () {
        VoidTimeOutMonitor timeOutMonitor = new VoidTimeOutMonitor();

        // Business method
        timeOutMonitor.shutdown();

        // Asserts
        assertThat(timeOutMonitor.getStatus()).isEqualTo(ServerProcessStatus.SHUTDOWN);
    }

}