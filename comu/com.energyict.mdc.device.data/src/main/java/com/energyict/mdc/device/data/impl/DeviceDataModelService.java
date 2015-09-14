package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.BatchService;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;
import com.energyict.mdc.device.data.kpi.DataCollectionKpiService;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;

import com.elster.jupiter.kpi.KpiService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.tasks.TaskService;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.time.Clock;
import java.util.Map;

/**
 * Provides the model for all data that relates to {@link com.energyict.mdc.device.data.Device}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-09-30 (17:31)
 */
public interface DeviceDataModelService {

    public DataModel dataModel();

    public Thesaurus thesaurus();

    public Clock clock();

    public SchedulingService schedulingService();

    public EngineConfigurationService engineConfigurationService();

    public KpiService kpiService();

    public TaskService taskService();

    public ProtocolPluggableService protocolPluggableService();

    public DeviceConfigurationService deviceConfigurationService();

    public ServerConnectionTaskService connectionTaskService();

    public ServerCommunicationTaskService communicationTaskService();

    public ServerDeviceService deviceService();

    public DataCollectionKpiService dataCollectionKpiService();

    public BatchService batchService();

    public void executeUpdate(SqlBuilder sqlBuilder);

    public Map<TaskStatus, Long> fetchTaskStatusCounters(PreparedStatementProvider preparedStatementProvider);

    public Map<Long, Map<TaskStatus, Long>> fetchTaskStatusBreakdown(PreparedStatementProvider builder);

    public Map<TaskStatus, Long> addMissingTaskStatusCounters(Map<TaskStatus, Long> counters);

}