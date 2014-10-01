package com.elster.jupiter.users.extjs;

import com.elster.jupiter.install.UiInstallService;
import com.elster.jupiter.orm.callback.InstallService;
import org.osgi.service.component.annotations.Component;

@Component(name = "com.elster.jupiter.users.extjs", service = {InstallService.class},
        property = {"name=" + UsrUiInstaller.COMPONENT_NAME}, immediate = true)
public class UsrUiInstaller extends UiInstallService {

    public static final String COMPONENT_NAME = "USR";

    public UsrUiInstaller() {
        super(COMPONENT_NAME, new Activator());
    }

}