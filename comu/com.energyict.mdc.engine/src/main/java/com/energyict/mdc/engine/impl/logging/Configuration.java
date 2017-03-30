/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates the methods of your logging message interfaces
 * to tell the logging framework how to format your message.
 * You will use {n} to indicate where the value parameter "n"
 * should be replaced in the resulting message.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-05-09 (08:39)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Configuration {

    /**
     * The LogLevel that should be used when emitting
     * the log message associated with the method
     * that is annotated with the Configuration annotation.
     *
     * @return The LogLevel
     */
    public LogLevel logLevel () default LogLevel.INFO;

    /**
     * The String that will be used to actually log the message.
     * Parameters of your messsage will be injected into the
     * format in the same order as they are passed.
     * <p>
     *
     * @return The format String
     */
    public String format ();

}