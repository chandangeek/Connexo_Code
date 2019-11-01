/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap.oracle.impl;

public class ConnectionProperties {
    String poolProvider;
    String jdbcUrl;
    String jdbcUser;
    String jdbcPassword;
    String keyFile;
    int maxLimit;
    int maxStatementsLimit;
    int connectionWaitTimeout;
    int inactivityTimeout;
    int abandonedConnectionsTimeout;
    int timeToLive;
    int maxConnectionReuseTime;
    String onsNodes;
}
