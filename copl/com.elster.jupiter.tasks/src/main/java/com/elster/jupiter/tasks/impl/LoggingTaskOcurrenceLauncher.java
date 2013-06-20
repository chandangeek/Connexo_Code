package com.elster.jupiter.tasks.impl;

import java.util.logging.Level;

public class LoggingTaskOcurrenceLauncher implements TaskOccurrenceLauncher {

    private final TaskOccurrenceLauncher launcher;
    private int c = 0;

    public LoggingTaskOcurrenceLauncher(TaskOccurrenceLauncher launcher) {
        this.launcher = launcher;
    }

    @Override
    public void run() { 
    	System.out.println("launching " + (++c));
        Bus.getLogger().info("entering TaskOcurrenceLauncher.run()");
        launcher.run();
        Bus.getLogger().info("exiting TaskOcurrenceLauncher.run()");
    }
}
