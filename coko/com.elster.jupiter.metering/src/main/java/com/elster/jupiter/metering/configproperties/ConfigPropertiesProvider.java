/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering.configproperties;

import aQute.bnd.annotation.ProviderType;

import javax.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Map;

@ProviderType
public interface ConfigPropertiesProvider {

    String getScope();

    List<PropertiesInfo> getPropertyInfos();

    Map<String, Object> getPropertyValues();

    default boolean isValid(List<PropertiesInfo> properties, ConstraintValidatorContext context) {return true;};

    void update();

    void setProperty(String key, Object value);
}
