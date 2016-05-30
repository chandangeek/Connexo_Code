package com.elster.jupiter.upgrade;

import com.elster.jupiter.orm.DataModelUpgrader;

import aQute.bnd.annotation.ConsumerType;

import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

@ConsumerType
public interface FullInstaller {

    void install(DataModelUpgrader dataModelUpgrader, Logger logger);

    default void doTry(String description, Runnable runnable, Logger logger) {
        try {
            runnable.run();
            logger.log(Level.SEVERE, "Success : " + description);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Failed  : " + description, e);
            throw e;
        }
    }

    default <T> T doTry(String description, Callable<T> callable, Logger logger) {
        try {
            T value = callable.call();
            logger.log(Level.SEVERE, "Success : " + description);
            return value;
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Failed  : " + description, e);
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed  : " + description, e);
            throw new RuntimeException(e);
        }
    }
}
