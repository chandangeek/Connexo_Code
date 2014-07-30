package com.energyict.mdc.dashboard;

/**
 * Provides services to dashboard-like information of the
 * {@link com.energyict.mdc.device.data.tasks.ConnectionTask}s
 * and/or {@link com.energyict.mdc.device.data.tasks.ComTaskExecution}s
 * that are configured and scheduled in the system.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-29 (17:28)
 */
public interface DashboardService {

    public ConnectionStatusOverview getConnectionStatusOverview();

    public ComTaskCompletionOverview getComTaskCompletionOverview();

    public ComPortPoolBreakdown getComPortPoolBreakdown();

    public ConnectionTypeBreakdown getConnectionTypeBreakdown();

    public DeviceTypeBreakdown getDeviceTypeBreakdown();

}