/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl;

import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.json.JsonService;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.validation.ValidationService;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskReportService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskReportService;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;

import java.time.Clock;
import java.util.Map;

/**
 * Provides the model for all data that relates to {@link com.energyict.mdc.device.data.Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-09-30 (17:31)
 */
public interface DeviceDataModelService {

    DataModel dataModel();

    Thesaurus thesaurus();

    Clock clock();

    SchedulingService schedulingService();

    EngineConfigurationService engineConfigurationService();

    KpiService kpiService();

    TaskService taskService();

    ProtocolPluggableService protocolPluggableService();

    DeviceConfigurationService deviceConfigurationService();

    TransactionService getTransactionService();

    ServerConnectionTaskService connectionTaskService();

    ConnectionTaskReportService connectionTaskReportService();

    ServerCommunicationTaskService communicationTaskService();

    CommunicationTaskReportService communicationTaskReportService();

    ServerDeviceService deviceService();

    DataCollectionKpiService dataCollectionKpiService();

    BatchService batchService();

    MessageService messageService();

    DeviceMessageSpecificationService deviceMessageSpecificationService();

    ValidationService validationService();

    JsonService jsonService();

    void executeUpdate(SqlBuilder sqlBuilder);

    Map<TaskStatus, Long> fetchTaskStatusCounters(PreparedStatementProvider preparedStatementProvider);

    Map<Long, Map<TaskStatus, Long>> fetchTaskStatusBreakdown(PreparedStatementProvider builder);

    Map<TaskStatus, Long> addMissingTaskStatusCounters(Map<TaskStatus, Long> counters);

}