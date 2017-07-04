/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.upl;

/**
 * Defines the function of device connection tasks.<br/>
 * Knowledge of these function(s) should allow to fully understand the purpose of the devices connection tasks.<br/>
 * An example would be the different flavors of BEACON 3100 connections:
 * <ul>
 * <li>One for connecting to the mirror logical device</li>
 * <li>One for connecting to the gateway logical device</li>
 * </ul>
 *
 * @author Stijn Vanhoorelbeke
 * @since 22.06.17 - 10:05
 */
public interface UPLConnectionFunction {

    /**
     * Provides a <b>unique</b> name for this ConnectionFunction.<br/>
     * Note that this name is part of the translation key for the ConnectionFunction,
     * so it is advised to not touch the name!
     *
     * @return the unique name for this ConnectionFunction
     */
    String getConnectionFunctionName();

    /**
     * Gets the <b>unique</b> ID of the ConnectionFunction
     *
     * @return the id of the ConnectionFunction
     */
    long getId();

}