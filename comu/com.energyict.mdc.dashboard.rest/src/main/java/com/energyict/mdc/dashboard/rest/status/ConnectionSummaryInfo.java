package com.energyict.mdc.dashboard.rest.status;

/**
 * JSON representation of connection summary
 * JP-4278
 * Created by bvn on 7/29/14.
 */
public class ConnectionSummaryInfo {
    public int activeConnections;
    public int successfulConnections;
    public int pendingConnections;
    public int failedConnections;
    public int connectionsWithAllTasksSuccessful;
    public int connectionsWithFailingTasks;
}
