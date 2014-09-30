package com.elster.jupiter.issue.extjs;

import com.elster.jupiter.install.impl.UiInstallService;
import com.elster.jupiter.orm.callback.InstallService;
import org.osgi.service.component.annotations.Component;

@Component(name = "com.elster.jupiter.issue.extjs", service = InstallService.class,
        property = "name=" + IsuUiInstaller.COMPONENT_NAME, immediate = true)
public class IsuUiInstaller extends UiInstallService {

    public static final String COMPONENT_NAME = "ISU";

    public IsuUiInstaller() {
        super(COMPONENT_NAME, new Activator());
    }

}