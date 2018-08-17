package com.elster.jupiter.hsm;

import com.elster.jupiter.hsm.impl.config.HsmLabelConfiguration;
import com.elster.jupiter.hsm.model.HsmBaseException;

import java.util.Collection;

public interface HsmPublicConfiguration {

    /**
     *
     * @return list of labels configured in HSM bundle and hopefully HSM as well.
     */
    Collection<HsmLabelConfiguration> labels() throws HsmBaseException;

}
