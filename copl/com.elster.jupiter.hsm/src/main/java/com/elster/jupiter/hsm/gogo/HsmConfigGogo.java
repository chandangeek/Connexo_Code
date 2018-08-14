package com.elster.jupiter.hsm.gogo;

import com.elster.jupiter.hsm.HsmConfigurationService;
import com.elster.jupiter.hsm.model.HsmBaseException;
import com.elster.jupiter.hsm.model.config.HsmConfiguration;
import com.elster.jupiter.hsm.model.config.HsmLabelConfiguration;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collection;

/**
 * This class is just for test purpose for the time being
 */
@Component(name = "com.elster.jupiter.hsm.gogo.HsmConfigGogo", service = {HsmConfigGogo.class}, property = {"name=" + "HSM" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=hsmConfigPrint"}, immediate = true)
public class HsmConfigGogo {

    private volatile HsmConfigurationService hsmConfigurationService;

    public void hsmConfigPrint() throws HsmBaseException {
        HsmConfiguration hsmConfiguration = hsmConfigurationService.getHsmConfiguration();
        System.out.println("JSS file:" + hsmConfiguration.getJssInitFile());
        Collection<HsmLabelConfiguration> labels = hsmConfiguration.getLabels();
        for (HsmLabelConfiguration label: labels){
            System.out.println("Configured label:" + label.toString());
        }

    }

    @Reference
    public void setHsmConfigurationService(HsmConfigurationService hsmConfigurationService) {
        this.hsmConfigurationService = hsmConfigurationService;
    }
}
