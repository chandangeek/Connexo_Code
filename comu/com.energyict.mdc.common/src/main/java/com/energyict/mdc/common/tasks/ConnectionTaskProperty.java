/*
 * Copyright (c) 2019  by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.tasks;

import com.energyict.mdc.common.pluggable.PluggableClassUsageProperty;
import com.energyict.mdc.common.protocol.ConnectionProperty;
import com.energyict.mdc.common.protocol.ConnectionType;

import aQute.bnd.annotation.ConsumerType;

/**
 * Holds the value of a property of a {@link ConnectionType}.
 * The values of properties are versioned over time
 * so a ConnectionTaskProperty has a activity period during
 * which the property was active.
 * The versioning is only used to keep a history of the
 * previous values and therefore it will not be
 * possible to change values in the past.
 * Setting properties will therefore only affect the
 * actual or current situation and the new values
 * will be active from the current time until forever
 * or until new values are provided.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-04-11 (15:09)
 */
@ConsumerType
public interface ConnectionTaskProperty extends ConnectionProperty, PluggableClassUsageProperty<ConnectionType> {

    ConnectionTask getConnectionTask();
}