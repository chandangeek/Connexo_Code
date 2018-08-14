/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.impl.config.HsmConfiguration;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface HsmConfigurationService {

    HsmConfiguration getHsmConfiguration() throws HsmBaseException;


}
