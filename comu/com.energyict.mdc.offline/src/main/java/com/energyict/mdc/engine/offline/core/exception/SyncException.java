package com.energyict.mdc.engine.offline.core.exception;

/**
 * Copyrights EnergyICT
 * <p/>
 * Exception indicating something went wrong before/during/after sync'ing (requesting/responding) a business object
 *
 * @author khe
 * @since 29/08/2014 - 14:07
 */
public class SyncException extends Exception {

    public SyncException(String message) {
        super(message);
    }
}