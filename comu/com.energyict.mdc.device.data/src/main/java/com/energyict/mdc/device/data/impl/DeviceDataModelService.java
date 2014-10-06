package com.energyict.mdc.device.data.impl;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.tasks.ServerCommunicationTaskService;
import com.energyict.mdc.device.data.impl.tasks.ServerConnectionTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTask;
import com.energyict.mdc.device.data.tasks.TaskStatus;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.scheduling.SchedulingService;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.time.Clock;

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

    public EngineModelService engineModelService();

    public ProtocolPluggableService protocolPluggableService();

    public DeviceConfigurationService deviceConfigurationService();

    public ServerConnectionTaskService connectionTaskService();

    public ServerCommunicationTaskService communicationTaskService();

    public ServerDeviceService deviceService();

    public void executeUpdate(SqlBuilder sqlBuilder);

    public void setOrUpdateDefaultConnectionTaskOnComTaskInDeviceTopology(Device device, ConnectionTask connectionTask);

    public Map<TaskStatus, Long> fetchTaskStatusCounters(ClauseAwareSqlBuilder builder);

    public Map<Long, Map<TaskStatus, Long>> fetchTaskStatusBreakdown(ClauseAwareSqlBuilder builder);

    public Map<TaskStatus, Long> addMissingTaskStatusCounters(Map<TaskStatus, Long> counters);

}