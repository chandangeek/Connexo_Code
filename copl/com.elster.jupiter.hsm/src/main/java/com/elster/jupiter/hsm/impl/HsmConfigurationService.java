/*
 *
 *  * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 *
 */

package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.impl.config.HsmConfiguration;

import aQute.bnd.annotation.ProviderType;
import com.atos.worldline.jss.api.basecrypto.ChainingMode;
import com.atos.worldline.jss.api.basecrypto.PaddingAlgorithm;
import com.atos.worldline.jss.configuration.RawConfiguration;

import java.util.Collection;

@ProviderType
public interface HsmConfigurationService {

    HsmConfiguration getHsmConfiguration() throws HsmBaseException;

    Collection<String> getLabels() throws HsmBaseException;

    void reload();
}
