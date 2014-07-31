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
}
