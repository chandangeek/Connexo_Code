package com.elster.jupiter.bpm.extjs;

import com.elster.jupiter.install.impl.UiInstallService;
import com.elster.jupiter.orm.callback.InstallService;
import org.osgi.service.component.annotations.Component;

@Component(name = "com.elster.jupiter.bpm.extjs", service = {InstallService.class},
        property = {"name=" + BpmUiInstaller.COMPONENT_NAME}, immediate = true)
public class BpmUiInstaller extends UiInstallService {

    public static final String COMPONENT_NAME = "BPM";

    public BpmUiInstaller() {
        super(COMPONENT_NAME, new Activator());
    }

}