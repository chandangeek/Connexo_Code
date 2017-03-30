/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api;

/**
 * Provides a connection to a physical device from the properties
 * provide by its {@link ConnectionType type}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-12 (13:14)
 */
public interface ConnectionProvider {

    ConnectionType getType();

}