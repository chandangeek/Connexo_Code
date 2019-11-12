/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.metering.configproperties.ConfigPropertiesProvider;

import java.util.Optional;

public interface ConfigPropertiesService {

    Optional<ConfigPropertiesProvider> findConfigFroperties(String scope);

}
