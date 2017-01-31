/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.api.services;

import com.energyict.mdc.pluggable.PluggableClassDefinition;
import com.energyict.mdc.protocol.api.ConnectionType;

import java.util.Collection;

public interface ConnectionTypeService {

    public ConnectionType createConnectionType(String javaClassName);

    public Collection<PluggableClassDefinition> getExistingConnectionTypePluggableClasses();

}