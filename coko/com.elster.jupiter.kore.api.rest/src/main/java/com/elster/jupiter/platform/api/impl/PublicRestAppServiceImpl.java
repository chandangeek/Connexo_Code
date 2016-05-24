package com.elster.jupiter.platform.api.impl;

import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.platform.api.impl.security.Privileges;
import com.elster.jupiter.users.ApplicationPrivilegesProvider;

import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component(name = "com.elster.jupiter.pulse.api.rest.app",
        service = {TranslationKeyProvider.class, ApplicationPrivilegesProvider.class},
        immediate = true)
public class PublicRestAppServiceImpl implements TranslationKeyProvider, ApplicationPrivilegesProvider {
    @Override
    public List<String> getApplicationPrivileges() {
        return Collections.singletonList(Privileges.Constants.PUBLIC_REST_API);
    }

    @Override
    public String getApplicationName() {
        return PublicRestApplication.APP_KEY;
    }

    @Override
    public String getComponentName() {
        return PublicRestApplication.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(Privileges.values());
    }
}
