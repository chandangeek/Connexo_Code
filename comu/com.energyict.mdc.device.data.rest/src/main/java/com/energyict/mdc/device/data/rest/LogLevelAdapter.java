package com.energyict.mdc.device.data.rest;


import com.energyict.mdc.common.rest.MapBasedXmlAdapter;
import com.energyict.mdc.engine.model.ComServer;

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
