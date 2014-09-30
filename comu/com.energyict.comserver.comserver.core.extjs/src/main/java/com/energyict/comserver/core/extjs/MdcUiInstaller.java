package com.energyict.comserver.core.extjs;

import com.elster.jupiter.install.impl.UiInstallService;
import com.elster.jupiter.orm.callback.InstallService;
import org.osgi.service.component.annotations.Component;

@Component(name = "com.energyict.comserver.core.extjs", service = {InstallService.class},
        property = {"name=" + MdcUiInstaller.COMPONENT_NAME}, immediate = true)
public class MdcUiInstaller extends UiInstallService {

    public static final String COMPONENT_NAME = "MDC";

    public MdcUiInstaller() {
        super(COMPONENT_NAME, new Activator());
    }

}