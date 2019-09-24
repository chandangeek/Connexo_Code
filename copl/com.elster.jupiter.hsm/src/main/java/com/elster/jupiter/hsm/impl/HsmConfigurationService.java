/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.impl.config.HsmConfiguration;
import com.elster.jupiter.hsm.model.HsmNotConfiguredException;

import aQute.bnd.annotation.ProviderType;

import java.util.Collection;

@ProviderType
public interface HsmConfigurationService {

    HsmConfiguration getHsmConfiguration() throws HsmNotConfiguredException;

    Collection<String> getLabels() throws HsmNotConfiguredException;

    void reload();
}
