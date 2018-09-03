package com.elster.jupiter.hsm;

import com.elster.jupiter.hsm.model.config.HsmLabelConfiguration;
import com.elster.jupiter.hsm.model.HsmBaseException;

import aQute.bnd.annotation.ProviderType;

import java.util.Collection;

@ProviderType
public interface HsmPublicConfiguration {

    String COMPONENTNAME = "HsmPublicConfigurationImpl";

    /**
     *
     * @return list of getLabels configured in HSM (response is based on JSON file used to init JSS).
     */
    Collection<String> labels() throws HsmBaseException;

}
