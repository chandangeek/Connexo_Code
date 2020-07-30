/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.response;

import java.util.Map;

public class SystemInfo {

    public String osName;
    public String osArch;
    public String timeZone;
    public long totalMemory;
    public long freeMemory;
    public long usedMemory;
    public long lastStartedTime;
    public long serverUptime;
    public Map<String, String> environmentParameters;
}
