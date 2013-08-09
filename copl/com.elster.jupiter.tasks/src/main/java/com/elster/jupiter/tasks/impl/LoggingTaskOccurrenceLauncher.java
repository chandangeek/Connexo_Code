package com.elster.jupiter.tasks.impl;

public class LoggingTaskOccurrenceLauncher implements TaskOccurrenceLauncher {

    private final TaskOccurrenceLauncher launcher;
    private int c = 0;

    public LoggingTaskOccurrenceLauncher(TaskOccurrenceLauncher launcher) {
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
