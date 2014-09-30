package com.elster.jupiter.systemadmin.extjs;

import com.elster.jupiter.install.UiInstallService;
import com.elster.jupiter.orm.callback.InstallService;
import org.osgi.service.component.annotations.Component;

@Component(name = "com.elster.jupiter.systemadmin.extjs", service = {InstallService.class},
        property = {"name=" + SamUiInstaller.COMPONENT_NAME}, immediate = true)
public class SamUiInstaller extends UiInstallService {

    public static final String COMPONENT_NAME = "SAM";

    public SamUiInstaller() {
        super(COMPONENT_NAME, new Activator());
    }

}