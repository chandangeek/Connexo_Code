/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.common.protocol.ConnectionTypePluggableClass;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
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
     * {@link ConnectionTypePluggableClass} supported by the specified class name.
     *
     * @param javaClassName The fully qualified class name
     */
    void registerConnectionTypePluggableClassAsCustomPropertySet(String javaClassName);

    /**
     * Registers the {@link CustomPropertySet}s of the existing
     * {@link DeviceProtocolPluggableClass}
     * supported by the specified class name.
     *
     * @param javaClassName The fully qualified class name
     */
    void registerDeviceProtocolPluggableClassAsCustomPropertySet(String javaClassName);

    /**
     * Returns the {@link Thesaurus} that contains all the information
     * that was provided by all of the registered protocols.
     *
     * @return The Thesaurus
     */
    Thesaurus protocolsThesaurus();

}