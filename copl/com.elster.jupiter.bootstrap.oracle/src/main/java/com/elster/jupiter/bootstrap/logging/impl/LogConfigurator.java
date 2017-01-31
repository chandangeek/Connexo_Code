/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bootstrap.logging.impl;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.log.LogService;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component (name="com.elster.jupiter.logging", immediate = true )
public class LogConfigurator  {

    private static final String FORMAT_KEY = "com.elster.jupiter.logging.format";
    private static final String LOG_LEVEL = "com.elster.jupiter.logging.root.loglevel";
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
        Level level = Level.WARNING;
        if (props != null && props.containsKey(LOG_LEVEL)) {
            level = Level.parse((String) (props.get(LOG_LEVEL)));
        }
        handler = new LogHandler(logService, format);
        handler.setLevel(level);
        Logger.getLogger("").addHandler(handler);        
    }

    @Deactivate
    public void deactivate() {
        Logger.getLogger("").removeHandler(handler);
    }

}
