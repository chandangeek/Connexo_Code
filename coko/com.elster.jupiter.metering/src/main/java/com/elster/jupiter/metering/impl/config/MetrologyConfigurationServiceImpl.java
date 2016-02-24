package com.elster.jupiter.metering.impl.config;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MessageSeeds;
import com.elster.jupiter.metering.config.Formula;
import com.elster.jupiter.metering.config.MetrologyConfiguration;
import com.elster.jupiter.metering.config.MetrologyConfigurationService;
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
        this.install();
    }

    @Override
    public void install() {
        new Installer(this.eventService).install();
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
                        Privileges.RESOURCE_METROLOGY_CONFIG.getKey(),
                        Privileges.RESOURCE_METROLOGY_CONFIGURATION_DESCRIPTION.getKey(),
                        Arrays.asList(
                                Privileges.Constants.ADMINISTER_ANY_METROLOGY_CONFIGURATION,
                                Privileges.Constants.BROWSE_ANY_METROLOGY_CONFIGURATION)));
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
        return Arrays.asList(Privileges.values());
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
    public MetrologyConfiguration newMetrologyConfiguration(String name) {
        MetrologyConfigurationImpl metrologyConfiguration = this.getDataModel().getInstance(MetrologyConfigurationImpl.class).init(name);
        metrologyConfiguration.update();
        return metrologyConfiguration;
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
    public Formula newFormula(Formula.Mode mode, ExpressionNode node) {
        Formula formula = getDataModel().getInstance(FormulaImpl.class).init(mode, node);
        formula.save();
        return formula;
    }

    @Override
    public Optional<Formula> findFormula(long id) {
        return getDataModel().mapper(Formula.class).getOptional(id);
    }

}