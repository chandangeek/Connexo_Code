/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

/**
 * Add behavior to {@link ProtocolPluggableService} that is specific to server side components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-01 (14:40)
 */
public interface ServerProtocolPluggableService extends ProtocolPluggableService {

    /**
     * Registers the {@link CustomPropertySet} of the existing
     * {@link com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass} supported by the specified class name.
     *
     * @param javaClassName The fully qualified class name
     */
    void registerConnectionTypePluggableClassAsCustomPropertySet(String javaClassName);

    /**
     * Registers the {@link CustomPropertySet}s of the existing
     * {@link com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass}
     * supported by the specified class name.
     *
     * @param javaClassName The fully qualified class name
     */
    void registerDeviceProtocolPluggableClassAsCustomPropertySet(String javaClassName);

}