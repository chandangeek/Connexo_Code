/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest;

import com.elster.jupiter.pki.TrustStore;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyType;
import com.energyict.mdc.pluggable.rest.PropertyDefaultValuesProvider;

import aQute.bnd.annotation.ProviderType;

import java.util.List;

@ProviderType
public interface TrustStoreValuesProvider extends PropertyDefaultValuesProvider {
    List<TrustStore> getPropertyPossibleValues(PropertySpec propertySpec, PropertyType propertyType);
}
