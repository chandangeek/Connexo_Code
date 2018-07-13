package com.elster.jupiter.hsm.gogo;

import com.elster.jupiter.hsm.impl.HsmConfigurationServiceImpl;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(name = "com.elster.jupiter.hsm.gogo.HsmConfigurationServiceGogo", service = {HsmConfigurationServiceGogo.class}, property = {"name=" + "HSM" + ".console", "osgi.command.scope=jupiter", "osgi.command.function=jssInit"}, immediate = true)
public class HsmConfigurationServiceGogo {

    private HsmConfigurationServiceImpl hsmCfgService;

    public void jssInit(String file) {
        this.hsmCfgService.init(file);
    }

    @Reference
    public void setHsmCfgService(HsmConfigurationServiceImpl hsmConfigService){
        this.hsmCfgService = hsmConfigService;
    }

}
