package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.users.ApplicationPrivilegesProvider;
import com.energyict.mdc.multisense.api.security.Privileges;
import org.osgi.application.ApplicationServiceListener;
import org.osgi.service.component.annotations.Component;

import java.util.Collections;
import java.util.List;

@Component(
        name = "com.energyict.mdc.api.rest.app",
        service = {ApplicationPrivilegesProvider.class},
        immediate = true)
public class PublicRestAppServiceImpl implements ApplicationPrivilegesProvider {
    @Override
    public List<String> getApplicationPrivileges() {
        return Collections.singletonList(Privileges.PUBLIC_REST_API);
    }

    @Override
    public String getApplicationName() {
        return PublicRestApplication.APP_KEY;
    }

}
