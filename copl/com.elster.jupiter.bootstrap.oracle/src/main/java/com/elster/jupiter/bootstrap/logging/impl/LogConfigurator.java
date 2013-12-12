package com.elster.jupiter.bootstrap.logging.impl;

import java.util.Map;
import java.util.logging.Logger;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

@Component (name="com.elster.jupiter.logging", immediate = true )
public class LogConfigurator  {

    private static final String FORMAT_KEY = "com.elster.jupiter.logging.format";
    private static final String DEFAULT_FORMAT = "%5$s";
    private volatile LogService logService;
    private volatile LogHandler handler;

    public LogConfigurator() {
    }
    
	@Reference
	public void setLogService(LogService logService) {
        this.logService = logService;
	}

    @Activate
    public void activate(Map<String, Object> props) {
        String format = DEFAULT_FORMAT;
        if (props != null && props.containsKey(FORMAT_KEY)) {
            format = (String) props.get(FORMAT_KEY);
        }
        handler = new LogHandler(logService, format);
        Logger.getLogger("").addHandler(handler);
    }

    @Deactivate
    public void deactivate() {
        Logger.getLogger("").removeHandler(handler);
    }

}
