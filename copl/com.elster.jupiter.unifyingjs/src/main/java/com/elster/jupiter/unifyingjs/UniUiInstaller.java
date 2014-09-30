package com.elster.jupiter.unifyingjs;

import com.elster.jupiter.install.UiInstallService;
import com.elster.jupiter.orm.callback.InstallService;
import org.osgi.service.component.annotations.Component;

@Component(name = "com.elster.jupiter.unifyingjs", service = {InstallService.class},
        property = {"name=" + UniUiInstaller.COMPONENT_NAME}, immediate = true)
public class UniUiInstaller extends UiInstallService {

    public static final String COMPONENT_NAME = "UNI";

    public UniUiInstaller() {
        super(COMPONENT_NAME, new Activator());
    }

}