package com.elster.jupiter.config.extjs;

import com.elster.jupiter.install.UiInstallService;
import com.elster.jupiter.orm.callback.InstallService;
import org.osgi.service.component.annotations.Component;

@Component(name = "com.elster.jupiter.config.extjs", service = {InstallService.class},
        property = {"name=" + CfgUiInstaller.COMPONENT_NAME}, immediate = true)
public class CfgUiInstaller extends UiInstallService {

    public static final String COMPONENT_NAME = "CFG";

    public CfgUiInstaller() {
        super(COMPONENT_NAME, new Activator());
    }

}