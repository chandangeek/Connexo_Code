/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.LicensedProtocol;

import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-10-09 (13:38)
 */
public abstract class PluggableClassRegistrar {

    private static final Logger LOGGER = Logger.getLogger(PluggableClassRegistrar.class.getName());

    protected String toLogMessage(LicensedProtocol licensedProtocol) {
        return "(code = " + licensedProtocol.getCode() + "; className = " + licensedProtocol.getClassName() + ")";
    }

    protected void logWarning(Supplier<String> supplier) {
        LOGGER.warning(supplier);
    }

    protected void logError(Supplier<String> supplier) {
        LOGGER.severe(supplier);
    }

    protected void factoryComponentMissing () {
        this.logWarning(() -> "Not all factory components registered, will retry later.");
    }

    protected void created (LicensedProtocol licensedProtocol) {
        LOGGER.fine(() -> "Created pluggable class for " + licensedProtocol.getClassName());
    }

    protected void created (PluggableClassDefinition definition) {
        LOGGER.fine(() -> "Created pluggable class for " + definition.getProtocolTypeClass().getSimpleName());
    }

    protected void completed (int count, String type) {
        LOGGER.fine(() -> "Completed registration of " + count + " " + type + " pluggable classes");
    }
    protected void creationFailed (LicensedProtocol licensedProtocol) {
        LOGGER.severe(() -> "Failure to register device protocol " + this.toLogMessage(licensedProtocol) + "see error message below:");
    }

    protected void alreadyExists (LicensedProtocol licensedProtocol) {
        LOGGER.fine(() -> "Skipping " + licensedProtocol.getClassName() + ": already exists");
    }

    protected void alreadyExists (PluggableClassDefinition definition) {
        LOGGER.fine(() -> "Skipping  " + definition.getProtocolTypeClass().getName() + ": already exists");
    }

    protected void handleCreationException(PluggableClassDefinition definition, Throwable e) {
        this.handleCreationException(definition.getProtocolTypeClass().getName(), e);
    }

    protected void handleCreationException(String className, Throwable e) {
        this.logError(() -> "Failed to create pluggable class for " + className + ", see stacktrace below ");
        try {
            LOGGER.log(Level.SEVERE, e, () -> "Failed to create pluggable class for " + className + ": " + e.getMessage());
        }
        catch (Exception ne) {
            logError(() -> "Failed to print stacktrace: " + ne.getClass().getName());
        }
    }

}