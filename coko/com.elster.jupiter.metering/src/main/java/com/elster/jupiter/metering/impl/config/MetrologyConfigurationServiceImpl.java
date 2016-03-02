package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.config.MeterRole;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationBuilder;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
import com.elster.jupiter.metering.impl.DefaultTranslationKey;
import com.elster.jupiter.metering.impl.ServerMeteringService;
import com.elster.jupiter.metering.security.Privileges;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Provides an implementation for the {@link MetrologyConfigurationService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-02-15 (13:20)
 */
@Component(
        name = "com.elster.jupiter.metering.config.MetrologyConfigurationServiceImpl",
        service = {MetrologyConfigurationService.class, InstallService.class, PrivilegesProvider.class, MessageSeedProvider.class, TranslationKeyProvider.class},
        property = {"name=" + MetrologyConfigurationService.COMPONENT_NAME},
        immediate = false)
public class MetrologyConfigurationServiceImpl implements MetrologyConfigurationService, InstallService, PrivilegesProvider, MessageSeedProvider, TranslationKeyProvider {

    private volatile ServerMeteringService meteringService;
    private volatile EventService eventService;
    private volatile UserService userService;

    // For OSGi purposes
    public MetrologyConfigurationServiceImpl() {
        super();
    }

    @Inject
    public MetrologyConfigurationServiceImpl(ServerMeteringService meteringService, EventService eventService, UserService userService) {
        this();
        setMeteringService(meteringService);
        setEventService(eventService);
        setUserService(userService);
    }

    @Override
    public void install() {
        new Installer(this.meteringService, this).install();
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList("ORM", "EVT", "MTR", "VAL", NlsService.COMPONENTNAME, CustomPropertySetService.COMPONENT_NAME);
    }

    @Override
    public String getModuleName() {
        return this.getComponentName();
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(
                userService.createModuleResourceWithPrivileges(
                        getModuleName(),
                        DefaultTranslationKey.RESOURCE_METROLOGY_CONFIGURATION.getKey(),
                        DefaultTranslationKey.RESOURCE_METROLOGY_CONFIGURATION_DESCRIPTION.getKey(),
                        Arrays.asList(
                                Privileges.Constants.ADMINISTER_METROLOGY_CONFIGURATION,
                                Privileges.Constants.VIEW_METROLOGY_CONFIGURATION)));
        return resources;
    }

    @Override
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> translationKeys = new ArrayList<>();
        translationKeys.addAll(Arrays.asList(Privileges.values()));
        return translationKeys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Reference
    public void setMeteringService(ServerMeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    DataModel getDataModel() {
        return this.meteringService.getDataModel();
    }

    @Override
    public MetrologyConfigurationBuilder newMetrologyConfiguration(String name, ServiceCategory serviceCategory) {
        MetrologyConfigurationBuilderImpl builder = new MetrologyConfigurationBuilderImpl(getDataModel());
        builder.init(name, serviceCategory);
        return builder;
    }

    @Override
    public Optional<MetrologyConfiguration> findMetrologyConfiguration(long id) {
        return this.getDataModel().mapper(MetrologyConfiguration.class).getUnique("id", id);
    }

    @Override
    public Optional<MetrologyConfiguration> findAndLockMetrologyConfiguration(long id, long version) {
        return this.getDataModel().mapper(MetrologyConfiguration.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<MetrologyConfiguration> findMetrologyConfiguration(String name) {
        return this.getDataModel().mapper(MetrologyConfiguration.class).getUnique("name", name);
    }

    @Override
    public List<MetrologyConfiguration> findAllMetrologyConfigurations() {
        return DefaultFinder.of(MetrologyConfiguration.class, this.getDataModel()).defaultSortColumn("lower(name)").find();
    }

    @Override
    public boolean isInUse(MetrologyConfiguration metrologyConfiguration) {
        Condition condition = Where.where("metrologyConfiguration").isEqualTo(metrologyConfiguration);
        List<UsagePointMetrologyConfiguration> atLeastOneUsagePoint = this.getDataModel().query(UsagePointMetrologyConfiguration.class).select(condition, new Order[0], false, new String[0], 1, 1);
        return !atLeastOneUsagePoint.isEmpty();
    }

    @Override
    public MeterRole newMeterRole(TranslationKey name) {
        MeterRoleImpl meterRole = getDataModel().getInstance(MeterRoleImpl.class).init(name.getKey());
        Save.CREATE.save(getDataModel(), meterRole);
        return meterRole;
    }

    @Override
    public Optional<MeterRole> findMeterRole(String name) {
        return getDataModel().mapper(MeterRole.class).getUnique(MeterRoleImpl.Fields.NAME.fieldName(), name);
    }
}