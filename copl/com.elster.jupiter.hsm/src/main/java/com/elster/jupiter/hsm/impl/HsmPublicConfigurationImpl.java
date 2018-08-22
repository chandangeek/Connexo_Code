package com.elster.jupiter.hsm.impl;

import com.elster.jupiter.hsm.HsmPublicConfiguration;
import com.elster.jupiter.hsm.model.config.HsmLabelConfiguration;
import com.elster.jupiter.hsm.model.HsmBaseException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collection;

@Component(name = "com.elster.jupiter.hsm.impl.HsmPublicConfigurationImpl", service = {HsmPublicConfiguration.class}, immediate = true, property = "name=" + HsmPublicConfiguration.COMPONENTNAME)
public class HsmPublicConfigurationImpl implements HsmPublicConfiguration {

    private volatile HsmConfigurationService hsmConfigurationService;

    @Override
    public Collection<HsmLabelConfiguration> labels() throws HsmBaseException {
        return hsmConfigurationService.getHsmConfiguration().getLabels();
    }

    @Reference
    public void setHsmConfigurationService(HsmConfigurationService hsmConfigurationService){
        this.hsmConfigurationService = hsmConfigurationService;
    }

}
