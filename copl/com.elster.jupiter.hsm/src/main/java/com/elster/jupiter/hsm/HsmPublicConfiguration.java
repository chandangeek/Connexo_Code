package com.elster.jupiter.hsm;

import com.elster.jupiter.hsm.impl.config.HsmLabelConfiguration;
import com.elster.jupiter.hsm.model.HsmBaseException;

import aQute.bnd.annotation.ProviderType;

import java.util.Collection;

@ProviderType
public interface HsmPublicConfiguration {

    String COMPONENTNAME = "HsmPublicConfigurationImpl";

    /**
     *
     * @return list of labels configured in HSM bundle and hopefully HSM as well.
     */
    Collection<HsmLabelConfiguration> labels() throws HsmBaseException;

}
