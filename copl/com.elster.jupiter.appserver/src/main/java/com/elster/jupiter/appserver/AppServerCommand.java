package com.elster.jupiter.appserver;

import java.util.Properties;

public class AppServerCommand {

    private Command command;

    private Properties properties = new Properties();

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
        return new Properties(properties);
    }

    public Command getCommand() {
        return command;
    }
}
