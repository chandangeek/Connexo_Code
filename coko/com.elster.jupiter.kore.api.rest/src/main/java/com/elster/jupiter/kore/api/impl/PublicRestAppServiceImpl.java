package com.elster.jupiter.kore.api.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.kore.api.impl.servicecall.UsagePointCommandCustomPropertySet;
import com.elster.jupiter.kore.api.security.Privileges;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.ApplicationPrivilegesProvider;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.elster.jupiter.orm.Version.version;

@Component(name = "com.elster.jupiter.pulse.api.rest.app",
        service = {ApplicationPrivilegesProvider.class, TranslationKeyProvider.class, MessageSeedProvider.class},
        immediate = true)
public class PublicRestAppServiceImpl implements ApplicationPrivilegesProvider, MessageSeedProvider, TranslationKeyProvider {

    public static final String COMPONENT_NAME = "PRA";

    static final String APP_KEY = "SYS";

    private volatile Thesaurus thesaurus;
    private volatile UpgradeService upgradeService;
    private volatile ServiceCallService serviceCallService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile OrmService ormService;
    private volatile MessageService messageService;
    private volatile PropertySpecService propertySpecService;
    private volatile UserService userService;

    @Activate
    public void activate() {
        DataModel dataModel = upgradeService.newNonOrmDataModel();
        InstallIdentifier identifier = InstallIdentifier.identifier("Insight", "PRA");
        if (upgradeService.isInstalled(identifier, version(1, 0))) {
            this.registerCustomPropertySets();
        }

        dataModel.register(new AbstractModule() {
            @Override
            protected void configure() {
                bind(ServiceCallService.class).toInstance(serviceCallService);
                bind(CustomPropertySetService.class).toInstance(customPropertySetService);
                bind(MessageService.class).toInstance(messageService);
                bind(PropertySpecService.class).toInstance(propertySpecService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(OrmService.class).toInstance(ormService);
                bind(UserService.class).toInstance(userService);
            }
        });

        upgradeService.register(
                identifier,
                dataModel,
                Installer.class,
                ImmutableMap.of(
                        version(10, 2), UpgraderV10_2.class,
                        version(10, 3), UpgraderV10_3.class
                ));
    }

    private void registerCustomPropertySets() {
        customPropertySetService.addCustomPropertySet(new UsagePointCommandCustomPropertySet(propertySpecService, thesaurus));
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENT_NAME, Layer.REST);
    }

    @Override
    public List<String> getApplicationPrivileges() {
        return Collections.singletonList(Privileges.Constants.PUBLIC_REST_API);
    }

    @Override
    public String getApplicationName() {
        return APP_KEY;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.REST;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> translationKeys = new ArrayList<>();
        translationKeys.addAll(Arrays.asList(com.elster.jupiter.kore.api.impl.TranslationKeys.values()));
        translationKeys.addAll(Arrays.asList(com.elster.jupiter.kore.api.impl.servicecall.TranslationKeys.values()));
        translationKeys.addAll(Arrays.asList(Privileges.values()));
        return translationKeys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }
}
