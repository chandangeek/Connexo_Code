package com.energyict.mdc.engine.impl.core.logging;

import com.energyict.protocol.exceptions.ConnectionException;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a composite implementation for the {@link ComPortConnectionLogger} interface
 * effectively delegating all log methods to a set of ComPortConnectionLoggers.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-01-11 (14:04)
 */
public class CompositeComPortConnectionLogger implements ComPortConnectionLogger {

    private List<ComPortConnectionLogger> loggers = new ArrayList<>();

    public void add(ComPortConnectionLogger logger) {
        this.loggers.add(logger);
    }

    @Override
    public String getLoggingCategoryName() {
        return this.getClass().getPackage().getName();
    }

    @Override
    public void startingTask(String comPortThreadName, String comTaskName) {
        for (ComPortConnectionLogger logger : this.loggers) {
            logger.startingTask(comPortThreadName, comTaskName);
        }
    }

    @Override
    public void connectionEstablished(String comPortThreadName, String comPortName) {
        for (ComPortConnectionLogger logger : this.loggers) {
            logger.connectionEstablished(comPortThreadName, comPortName);
        }
    }

    @Override
    public void cannotEstablishConnection(ConnectionException e, String comPortThreadName) {
        for (ComPortConnectionLogger logger : this.loggers) {
            logger.cannotEstablishConnection(e, comPortThreadName);
        }
    }

    @Override
    public void completingTask(String comPortThreadName, String comTaskName) {
        for (ComPortConnectionLogger logger : this.loggers) {
            logger.completingTask(comPortThreadName, comTaskName);
        }
    }

    @Override
    public void taskExecutionFailed(Throwable e, String comPortThreadName, String comTaskName) {
        for (ComPortConnectionLogger logger : this.loggers) {
            logger.taskExecutionFailed(e, comPortThreadName, comTaskName);
        }
    }

    @Override
    public void taskExecutionFailed(Throwable e, String comPortThreadName, String comTaskName, String device) {
        for (ComPortConnectionLogger logger : this.loggers) {
            logger.taskExecutionFailed(e, comPortThreadName, comTaskName, device);
        }
    }

    @Override
    public void taskExecutionFailedDueToProblems(String comPortThreadName, String comTaskName, String device) {
        for (ComPortConnectionLogger logger : this.loggers) {
            logger.taskExecutionFailedDueToProblems(comPortThreadName, comTaskName, device);
        }
    }

    @Override
    public void taskExecutionFailedDueToProblems(String comPortThreadName, String comTaskName) {
        for (ComPortConnectionLogger logger : this.loggers) {
            logger.taskExecutionFailedDueToProblems(comPortThreadName, comTaskName);
        }
    }

    @Override
    public void reschedulingTask(String comPortThreadName, String comTaskName) {
        for (ComPortConnectionLogger logger : this.loggers) {
            logger.reschedulingTask(comPortThreadName, comTaskName);
        }
    }

}