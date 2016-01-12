package com.energyict.mdc.device.data.impl.tasks.report;

import com.energyict.mdc.device.data.impl.PersistenceIntegrationTest;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;

import org.junit.*;

/**
 * Integration test of the {@link ConnectionTaskReportServiceImpl} component
 * that only asserts that no sql exceptions occur.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-25 (09:32)
 */
public class ConnectionTaskReportServiceImplIT extends PersistenceIntegrationTest {

    @Transactional
    @Test
    public void getConnectionTaskLastComSessionSuccessIndicatorCountDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getConnectionTaskReportService().getConnectionTaskLastComSessionSuccessIndicatorCount();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void countConnectionTasksLastComSessionsWithAtLeastOneFailedTaskDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getConnectionTaskReportService().countConnectionTasksLastComSessionsWithAtLeastOneFailedTask();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void countWaitingConnectionTasksLastComSessionsWithAtLeastOneFailedTaskDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getConnectionTaskReportService().countWaitingConnectionTasksLastComSessionsWithAtLeastOneFailedTask();

        // Asserts: should not cause any SQLExceptions
    }

}