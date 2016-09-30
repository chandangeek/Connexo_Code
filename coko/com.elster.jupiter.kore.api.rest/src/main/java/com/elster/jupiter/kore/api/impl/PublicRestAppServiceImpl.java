package com.elster.jupiter.kore.api.impl;

import com.elster.jupiter.kore.api.impl.security.Privileges;
import com.elster.jupiter.kore.api.impl.servicecall.TranslationKeys;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.users.ApplicationPrivilegesProvider;

import org.osgi.service.component.annotations.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        return
            Stream
                .concat(
                    Stream.of(Privileges.values()),
                    Stream.of(TranslationKeys.values()))
                .collect(Collectors.toList());
    }

}