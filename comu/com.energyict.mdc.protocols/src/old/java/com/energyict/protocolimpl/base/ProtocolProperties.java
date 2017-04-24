/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.base;

import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;

import java.util.List;
import java.util.Properties;

public interface ProtocolProperties {

    List<String> getOptionalKeys();

    List<String> getRequiredKeys();

    void addProperties(Properties properties);

    void validateProperties() throws MissingPropertyException, InvalidPropertyException;

}
