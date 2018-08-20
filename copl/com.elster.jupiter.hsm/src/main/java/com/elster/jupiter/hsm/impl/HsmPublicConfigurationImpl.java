package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.HsmPublicConfiguration;
import com.elster.jupiter.hsm.impl.config.HsmConfiguration;
import com.elster.jupiter.hsm.impl.config.HsmLabelConfiguration;
import com.elster.jupiter.hsm.model.HsmBaseException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collection;

@Component(name = "com.elster.jupiter.hsm.impl.HsmPublicConfigurationImpl", service = {HsmPublicConfiguration.class}, immediate = true, property = "name=" + HsmPublicConfiguration.COMPONENTNAME)
public class HsmPublicConfigurationImpl implements HsmPublicConfiguration {

    private volatile HsmConfiguration hsmConfiguration;

    @Override
    public Collection<HsmLabelConfiguration> labels() throws HsmBaseException {
        return hsmConfiguration.getLabels();
    }

    @Reference
    public void setHsmConfiguration(HsmConfiguration hsmConfiguration){
        this.hsmConfiguration = hsmConfiguration;
    }

}
