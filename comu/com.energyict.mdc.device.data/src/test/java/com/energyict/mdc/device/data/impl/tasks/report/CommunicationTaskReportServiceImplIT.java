package com.energyict.mdc.device.data.impl.tasks.report;

import com.energyict.mdc.device.data.impl.PersistenceIntegrationTest;

import com.elster.jupiter.devtools.persistence.test.rules.Transactional;

import org.junit.*;

/**
 * Integration test of the {@link CommunicationTaskReportServiceImpl} component
 * that only asserts that no sql exceptions occur.
 * Any failures that are reported here that relate to oracle specific
 * syntax should be moved to {@link CommunicationTaskReportServiceImplOracleSpecificIT}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-08-25 (09:32)
 */
public class CommunicationTaskReportServiceImplIT extends PersistenceIntegrationTest {

    @Transactional
    @Test
    public void getComTaskLastComSessionHighestPriorityCompletionCodeCountDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getCommunicationTaskReportService().getComTaskLastComSessionHighestPriorityCompletionCodeCount();

        // Asserts: should not cause any SQLExceptions
    }

    @Transactional
    @Test
    public void getComTasksDeviceTypeHeatMapDoesNotProduceSQLExceptions() {
        // Business method
        inMemoryPersistence.getCommunicationTaskReportService().getComTasksDeviceTypeHeatMap();

        // Asserts: should not cause any SQLExceptions
    }

}