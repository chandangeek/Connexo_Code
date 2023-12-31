/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest;


import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.rest.MapBasedXmlAdapter;

public class LogLevelAdapter extends MapBasedXmlAdapter<ComServer.LogLevel> {

    public LogLevelAdapter() {
        register("", null);
        register("Error", ComServer.LogLevel.ERROR);
        register("Warning", ComServer.LogLevel.WARN);
        register("Information", ComServer.LogLevel.INFO);
        register("Debug", ComServer.LogLevel.DEBUG);
        register("Trace", ComServer.LogLevel.TRACE);
    }
}
