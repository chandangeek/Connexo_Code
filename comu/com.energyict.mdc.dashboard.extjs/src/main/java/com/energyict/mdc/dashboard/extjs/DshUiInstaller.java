package com.energyict.mdc.dashboard.extjs;

import com.elster.jupiter.install.impl.UiInstallService;
import com.elster.jupiter.orm.callback.InstallService;
import org.osgi.service.component.annotations.Component;

@Component(name = "com.energyict.mdc.dashboard.extjs", service = InstallService.class,
        property = "name=" + DshUiInstaller.COMPONENT_NAME, immediate = true)
public class DshUiInstaller extends UiInstallService {

    public static final String COMPONENT_NAME = "DSH";

    public DshUiInstaller() {
        super(COMPONENT_NAME, new Activator());
    }

}