package com.energyict.mdc.dashboard.rest.status.impl;

/**
 * JSON representation of connection summary
 * JP-4278
 * Created by bvn on 7/29/14.
 */
public class ConnectionSummaryInfo {
    public long activeConnections;
    public long successfulConnections;
    public long pendingConnections;
    public long failedConnections;
    public long connectionsWithAllTasksSuccessful;
    public long connectionsWithFailingTasks;

    public static ConnectionSummaryInfo from(ConnectionSummaryData connectionSummaryData) {
        ConnectionSummaryInfo info = new ConnectionSummaryInfo();
        info.activeConnections=connectionSummaryData.getTotal();
        info.successfulConnections=connectionSummaryData.getSuccess();
        info.connectionsWithAllTasksSuccessful=connectionSummaryData.getAllTasksSuccessful();
        info.connectionsWithFailingTasks=connectionSummaryData.getAtLeastOneTaskFailed();
        info.failedConnections=connectionSummaryData.getFailed();
        info.pendingConnections=connectionSummaryData.getPending();
        return info;
    }
}
