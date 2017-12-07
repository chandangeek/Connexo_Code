/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.tasks.report;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;
import com.energyict.mdc.device.data.impl.PersistenceIntegrationTest;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Integration test of the {@link ConnectionTaskReportServiceImpl} component
 * that only asserts that no sql exceptions occur.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-25 (09:32)
 */
@Ignore // all tests moved to ConnectionTaskReportServiceImplOracleSpecificIT
public class ConnectionTaskReportServiceImplIT extends PersistenceIntegrationTest {

    @Transactional
    @Test
    @Ignore // moved to ConnectionTaskReportServiceImplOracleSpecificIT
    public void countConnectionTasksLastComSessionsWithAtLeastOneFailedTaskDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getConnectionTaskReportService().countConnectionTasksLastComSessionsWithAtLeastOneFailedTask();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    @Ignore // moved to ConnectionTaskReportServiceImplOracleSpecificIT
    public void countWaitingConnectionTasksLastComSessionsWithAtLeastOneFailedTaskDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getConnectionTaskReportService().countWaitingConnectionTasksLastComSessionsWithAtLeastOneFailedTask();

        // Asserts: should not cause any SQLExceptions
    }

}