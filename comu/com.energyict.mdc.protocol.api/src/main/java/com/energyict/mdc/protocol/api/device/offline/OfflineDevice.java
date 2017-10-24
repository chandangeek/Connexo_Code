/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.device.offline;

import com.elster.jupiter.orm.MacException;
import com.elster.jupiter.pki.SecurityAccessorType;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.api.security.SecurityCustomPropertySet;
import com.energyict.mdc.upl.offline.Offline;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents an Offline version of a physical device which should contain all
 * necessary information needed to perform protocolTasks without the need to go to the database.
 *
 * @author gna
 * @since 11/04/12 - 10:01
 */
public interface OfflineDevice extends Offline, com.energyict.mdc.upl.offline.OfflineDevice {

    /**
     * Returns the {@link DeviceProtocolPluggableClass} configured for this device.
     *
     * @return The DeviceProtocolPluggableClass
     */
    DeviceProtocolPluggableClass getDeviceProtocolPluggableClass();

    Optional<MacException> getMacException();

    /**
     * Get a list of all offlineKeyAccessors
     *
     * @return a list of offlineKeyAccessors
     */
    List<OfflineKeyAccessor> getAllOfflineKeyAccessors();

    /**
     * Gets a mapping of all security property set attributes to corresponding {@link SecurityAccessorType} name
     * - represented as TypedProperties - for all of the {@link SecurityCustomPropertySet}s
     *
     * @return the mapping of all security property set attributes to corresponding KeyAccessorType name for all of the SecurityPropertySets
     */
    Map<String, TypedProperties> getSecurityPropertySetAttributeToKeyAccessorTypeMapping();

}