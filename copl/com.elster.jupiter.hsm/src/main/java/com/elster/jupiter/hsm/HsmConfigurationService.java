/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.hsm;

import com.elster.jupiter.hsm.model.configuration.HsmConfiguration;

import aQute.bnd.annotation.ProviderType;
import com.atos.worldline.jss.api.custom.energy.ProtectedSessionKeyCapability;

@ProviderType
public interface HsmConfigurationService {

    HsmConfiguration getHsmConfiguration();


}
