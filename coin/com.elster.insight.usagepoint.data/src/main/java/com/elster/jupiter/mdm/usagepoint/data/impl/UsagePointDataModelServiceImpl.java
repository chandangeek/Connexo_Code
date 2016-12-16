package com.elster.jupiter.mdm.usagepoint.data.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.mdm.usagepoint.config.UsagePointConfigurationService;
import com.elster.jupiter.mdm.usagepoint.data.ChannelDataValidationSummaryFlag;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataCompletionService;
import com.elster.jupiter.mdm.usagepoint.data.UsagePointDataModelService;
import com.elster.jupiter.mdm.usagepoint.data.exceptions.MessageSeeds;
import com.elster.jupiter.mdm.usagepoint.data.favorites.FavoritesService;
import com.elster.jupiter.mdm.usagepoint.data.impl.favorites.FavoritesServiceImpl;
import com.elster.jupiter.mdm.usagepoint.data.security.Privileges;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.ValidationService;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.MessageInterpolator;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
@Component(
        name = "com.elster.jupiter.mdm.usagepoint.data.impl.UsagePointDataModelServiceImpl",
        service = {UsagePointDataModelService.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        property = {"name=" + UsagePointDataModelService.COMPONENT_NAME},
        immediate = true)
public class UsagePointDataModelServiceImpl implements UsagePointDataModelService, MessageSeedProvider, TranslationKeyProvider {
    private volatile DataModel dataModel;
    private volatile Clock clock;
    private volatile Thesaurus thesaurus;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile MeteringService meteringService;
    private volatile ValidationService validationService;
    private volatile UsagePointConfigurationService usagePointConfigurationService;
    private volatile UpgradeService upgradeService;
    private volatile MessageService messageService;
    private volatile UserService userService;
    private volatile ThreadPrincipalService threadPrincipalService;

    private UsagePointDataCompletionService usagePointDataCompletionService;
    private FavoritesService favoritesService;

    private List<ServiceRegistration> serviceRegistrations = new ArrayList<>();

    @SuppressWarnings("unused")
    public UsagePointDataModelServiceImpl() {
        // OSGI
    }

    @Inject
    public UsagePointDataModelServiceImpl(BundleContext bundleContext,
                                          Clock clock,
                                          MeteringService meteringService,
                                          ValidationService validationService,
                                          NlsService nlsService,
                                          CustomPropertySetService customPropertySetService,
                                          UsagePointConfigurationService usagePointConfigurationService,
                                          UpgradeService upgradeService,
                                          UserService userService,
                                          OrmService ormService,
                                          MessageService messageService,
                                          ThreadPrincipalService threadPrincipalService) {
        setClock(clock);
        setMeteringService(meteringService);
        setValidationService(validationService);
        setNlsService(nlsService);
        setCustomPropertySetService(customPropertySetService);
        setUsagePointConfigurationService(usagePointConfigurationService);
        setUpgradeService(upgradeService);
        setUserService(userService);
        setOrmService(ormService);
        setMessageService(messageService);
        setThreadPrincipalService(threadPrincipalService);
        activate(bundleContext);
    }

    Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(Clock.class).toInstance(clock);
                bind(CustomPropertySetService.class).toInstance(customPropertySetService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MessageInterpolator.class).toInstance(thesaurus);
                bind(UsagePointDataModelService.class).toInstance(UsagePointDataModelServiceImpl.this);
                bind(UsagePointDataCompletionService.class).toInstance(usagePointDataCompletionService);
                bind(FavoritesService.class).toInstance(favoritesService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(ValidationService.class).toInstance(validationService);
                bind(UpgradeService.class).toInstance(upgradeService);
                bind(UserService.class).toInstance(userService);
                bind(UsagePointConfigurationService.class).toInstance(usagePointConfigurationService);
                bind(MessageService.class).toInstance(messageService);
            }
        };
    }

    @Activate
    public void activate(BundleContext bundleContext) {
        createServices();
        dataModel.register(getModule());
        upgradeService.register(
                InstallIdentifier.identifier("Insight", getComponentName()),
                dataModel,
                Installer.class,
                ImmutableMap.of(UpgraderV10_3.VERSION, UpgraderV10_3.class)
        );
        registerServices(bundleContext);
    }

    @Deactivate
    public void stop() {
        serviceRegistrations.forEach(ServiceRegistration::unregister);
    }

    private void createServices() {
        usagePointDataCompletionService = new UsagePointDataCompletionServiceImpl(this, validationService);
        favoritesService = new FavoritesServiceImpl(this, threadPrincipalService);
    }

    private void registerServices(BundleContext bundleContext) {
        registerService(bundleContext, UsagePointDataCompletionService.class, usagePointDataCompletionService);
        registerService(bundleContext, FavoritesService.class, favoritesService);
    }

    private <T> void registerService(BundleContext bundleContext, Class<T> serviceClass, T service) {
        serviceRegistrations.add(bundleContext.registerService(serviceClass, service, null));
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(COMPONENT_NAME, "Usage point data");
        Arrays.stream(TableSpecs.values()).forEach(spec -> spec.addTo(dataModel));
        this.dataModel = dataModel;
    }

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(UsagePointDataModelService.COMPONENT_NAME, Layer.DOMAIN);
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setValidationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    @Reference
    public void setUsagePointConfigurationService(UsagePointConfigurationService usagePointConfigurationService) {
        this.usagePointConfigurationService = usagePointConfigurationService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Override
    public String getComponentName() {
        return UsagePointDataModelService.COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Stream.of(
                Arrays.stream(ChannelDataValidationSummaryFlag.values()),
                Arrays.stream(Subscribers.values()),
                Arrays.stream(Privileges.values()))
                .flatMap(Function.identity())
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public Clock clock() {
        return clock;
    }

    @Override
    public DataModel dataModel() {
        return dataModel;
    }

    @Override
    public Thesaurus thesaurus() {
        return thesaurus;
    }
}
