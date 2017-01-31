/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.appserver;

import java.util.Properties;

public class AppServerCommand {

    private Command command;

    private Properties properties = new Properties();

    @SuppressWarnings("unused")
	private AppServerCommand() {
    	
    }
    
    public AppServerCommand(Command command) {
        this.command = command;
    }

    public AppServerCommand(Command command, Properties properties) {
        this.command = command;
        this.properties.putAll(properties);
    }

    public Properties getProperties() {
        Properties props = new Properties();
        props.putAll(this.properties);
        return props;
    }

    public Command getCommand() {
        return command;
    }
}
