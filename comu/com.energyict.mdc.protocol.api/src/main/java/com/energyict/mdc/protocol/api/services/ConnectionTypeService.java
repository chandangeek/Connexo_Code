/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.services;

import com.energyict.mdc.common.protocol.ConnectionType;
import com.energyict.mdc.pluggable.PluggableClassDefinition;

import java.util.Collection;

/**
 * OSGI Service wrapper for a {@link ConnectionType}.
 * <p>
 *
 * Date: 06/11/13
 * Time: 11:01
 */
public interface ConnectionTypeService {

    ConnectionType createConnectionType(String javaClassName);

    Collection<PluggableClassDefinition> getExistingConnectionTypePluggableClasses();

}