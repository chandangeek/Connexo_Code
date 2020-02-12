/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.response;

import java.util.Map;

public class SystemInfo {
    public String jre;
    public String jvm;
    public String javaHome;
    public String javaClassPath;
    public String osName;
    public String osArch;
    public String timeZone;
    public int numberOfProcessors;
    public long totalMemory;
    public long freeMemory;
    public long usedMemory;
    public long lastStartedTime;
    public long serverUptime;
    public String dbConnectionUrl;
    public String dbUser;
    public String dbMaxConnectionsNumber;
    public String dbMaxStatementsPerRequest;
    public Map<String, String> environmentParameters;
    public Map<String, Map<String, String>> trustStoreContent;
}
