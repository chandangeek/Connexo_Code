package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.Privilege;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.common.interval.Phenomenon;
import com.energyict.mdc.common.services.DefaultFinder;
import com.energyict.mdc.common.services.Finder;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.ChannelSpecLinkType;
import com.energyict.mdc.device.config.DeviceCommunicationConfiguration;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceSecurityUserAction;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.NextExecutionSpecs;
import com.energyict.mdc.device.config.PartialConnectionTask;
import com.energyict.mdc.device.config.ProtocolDialectConfigurationProperties;
import com.energyict.mdc.device.config.RegisterSpec;
import com.energyict.mdc.device.config.SecurityPropertySet;
import com.energyict.mdc.device.config.TemporalExpression;
import com.energyict.mdc.engine.model.ComPortPool;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.masterdata.LoadProfileType;
import com.energyict.mdc.masterdata.LogBookType;
import com.energyict.mdc.masterdata.MasterDataService;
import com.energyict.mdc.masterdata.RegisterMapping;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.DeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.google.common.base.Optional;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link DeviceConfigurationService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:38)
 */
@Component(name="com.energyict.mdc.device.config", service = {DeviceConfigurationService.class, InstallService.class}, property = "name=" + DeviceConfigurationService.COMPONENTNAME, immediate = true)
public class DeviceConfigurationServiceImpl implements ServerDeviceConfigurationService, InstallService {

    private volatile ProtocolPluggableService protocolPluggableService;

    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile Thesaurus thesaurus;
    private volatile MeteringService meteringService;
    private volatile MdcReadingTypeUtilService readingTypeUtilService;
    private volatile EngineModelService engineModelService;
    private volatile MasterDataService masterDataService;
    private volatile UserService userService;

    private final Map<DeviceSecurityUserAction, Privilege> privileges = new EnumMap<>(DeviceSecurityUserAction.class);

    public DeviceConfigurationServiceImpl() {
        super();
    }

    @Inject
    public DeviceConfigurationServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService, MeteringService meteringService, MdcReadingTypeUtilService mdcReadingTypeUtilService, UserService userService, ProtocolPluggableService protocolPluggableService, EngineModelService engineModelService, MasterDataService masterDataService) {
        this(ormService, eventService, nlsService, meteringService, mdcReadingTypeUtilService, protocolPluggableService, userService, engineModelService, masterDataService, false);
    }

    public DeviceConfigurationServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService, MeteringService meteringService, MdcReadingTypeUtilService mdcReadingTypeUtilService, ProtocolPluggableService protocolPluggableService, UserService userService, EngineModelService engineModelService, MasterDataService masterDataService, boolean createMasterData) {
        this();
        this.setOrmService(ormService);
        this.setUserService(userService);
        this.setEventService(eventService);
        this.setNlsService(nlsService);
        this.setMeteringService(meteringService);
        this.setProtocolPluggableService(protocolPluggableService);
        this.setReadingTypeUtilService(mdcReadingTypeUtilService);
        this.setEngineModelService(engineModelService);
        this.setMasterDataService(this.masterDataService);
        this.activate();
        if (!this.dataModel.isInstalled()) {
            this.install(true);
        }
    }

    @Override
    public Finder<DeviceType> findAllDeviceTypes() {
        return DefaultFinder.of(DeviceType.class, this.getDataModel()).defaultSortColumn("lower(name)");
    }

    @Override
    public DeviceType newDeviceType(String name, DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        return DeviceTypeImpl.from(this.getDataModel(), name, deviceProtocolPluggableClass);
    }

    @Override
    public DeviceType findDeviceType(long deviceTypeId) {
        return this.getDataModel().mapper((DeviceType.class)).getUnique("id", deviceTypeId).orNull();
    }

    @Override
    public DeviceType findDeviceTypeByName(String name) {
        return this.getDataModel().mapper((DeviceType.class)).getUnique("name", name).orNull();
    }

    @Override
    public DeviceConfiguration findDeviceConfiguration(long deviceConfigId) {
        return this.getDataModel().mapper((DeviceConfiguration.class)).getUnique("id", deviceConfigId).orNull();
    }

    @Override
    public ChannelSpec findChannelSpec(long channelSpecId) {
        return this.getDataModel().mapper((ChannelSpec.class)).getUnique("id", channelSpecId).orNull();
    }

    @Override
    public RegisterSpec findRegisterSpec(long id) {
        return this.getDataModel().mapper((RegisterSpec.class)).getUnique("id", id).orNull();
    }

    @Override
    public List<RegisterSpec> findActiveRegisterSpecsByDeviceTypeAndRegisterMapping(DeviceType deviceType, RegisterMapping registerMapping) {
        Condition condition = where("deviceConfig.deviceType").isEqualTo(deviceType).
                and(where("registerMapping").isEqualTo(registerMapping)).
                and(where("deviceConfig.active").isEqualTo(Boolean.TRUE));
        return this.getDataModel().query(RegisterSpec.class, DeviceConfiguration.class).select(condition);
    }

    @Override
    public List<RegisterSpec> findInactiveRegisterSpecsByDeviceTypeAndRegisterMapping(DeviceType deviceType, RegisterMapping registerMapping) {
        Condition condition = where("deviceConfig.deviceType").isEqualTo(deviceType).
                and(where("registerMapping").isEqualTo(registerMapping)).
                and(where("deviceConfig.active").isEqualTo(Boolean.FALSE));
        return this.getDataModel().query(RegisterSpec.class, DeviceConfiguration.class).select(condition);
    }

    @Override
    public List<RegisterSpec> findRegisterSpecsByRegisterMapping(RegisterMapping registerMapping) {
        return this.getDataModel().mapper(RegisterSpec.class).find("registerMapping", registerMapping);
    }

    @Override
    public List<RegisterSpec> findRegisterSpecsByChannelSpecAndLinkType(ChannelSpec channelSpec, ChannelSpecLinkType linkType) {
        return this.getDataModel().mapper(RegisterSpec.class).find("linkedChannelSpec", channelSpec, "channelSpecLinkType", linkType);
    }

    @Override
    public List<ChannelSpec> findChannelSpecsForLoadProfileSpec(LoadProfileSpec loadProfileSpec) {
        return this.getDataModel().mapper(ChannelSpec.class).find("loadProfileSpec", loadProfileSpec);
    }

    @Override
    public LoadProfileSpec findLoadProfileSpec(int loadProfileSpecId) {
        return this.getDataModel().mapper(LoadProfileSpec.class).getUnique("id", loadProfileSpecId).orNull();
    }

    @Override
    public LoadProfileSpec findLoadProfileSpecsByDeviceConfigAndLoadProfileType(DeviceConfiguration deviceConfig, LoadProfileType loadProfileType) {
        return this.getDataModel().mapper(LoadProfileSpec.class).getUnique("deviceConfiguration", deviceConfig, "loadProfileType", loadProfileType).orNull();
    }

    @Override
    public List<LoadProfileSpec> findLoadProfileSpecsByLoadProfileType(LoadProfileType loadProfileType) {
        return this.getDataModel().mapper(LoadProfileSpec.class).find("loadProfileType", loadProfileType);
    }

    @Override
    public LogBookSpec findLogBookSpec(long logBookSpecId) {
        return this.getDataModel().mapper(LogBookSpec.class).getUnique("id", logBookSpecId).orNull();
    }

    @Override
    public ChannelSpec findChannelSpecForLoadProfileSpecAndRegisterMapping(LoadProfileSpec loadProfileSpec, RegisterMapping registerMapping) {
        return this.getDataModel().mapper(ChannelSpec.class).getUnique("loadProfileSpec", loadProfileSpec, "registerMapping", registerMapping).orNull();
    }

    @Override
    public ChannelSpec findChannelSpecByDeviceConfigurationAndName(DeviceConfiguration deviceConfiguration, String name) {
        return this.getDataModel().mapper(ChannelSpec.class).getUnique("deviceConfiguration", deviceConfiguration, "name", name).orNull();
    }

    @Override
    public List<DeviceConfiguration> findDeviceConfigurationsByDeviceType(DeviceType deviceType) {
        return this.getDataModel().mapper(DeviceConfiguration.class).find("deviceType", deviceType);
    }

    @Override
    public List<DeviceType> findDeviceTypesWithDeviceProtocol(DeviceProtocolPluggableClass deviceProtocolPluggableClass) {
        return this.getDataModel().mapper(DeviceType.class).find("deviceProtocolPluggableClass", deviceProtocolPluggableClass);
    }

    @Override
    public List<DeviceConfiguration> findDeviceConfigurationsUsingLoadProfileType(LoadProfileType loadProfileType) {
        return this.getDataModel().
                    query(DeviceConfiguration.class, LoadProfileSpec.class).
                    select(where("loadProfileSpecs.loadProfileType").isEqualTo(loadProfileType));
    }

    @Override
    public List<ChannelSpec> findChannelSpecsForRegisterMapping(RegisterMapping registerMapping) {
        return this.getDataModel().mapper(ChannelSpec.class).find("registerMapping", registerMapping);
    }

    @Override
    public List<ChannelSpec> findChannelSpecsForRegisterMappingInLoadProfileType(RegisterMapping registerMapping, LoadProfileType loadProfileType) {
        return this.getDataModel().
                query(ChannelSpec.class, LoadProfileSpec.class).
                select(where("registerMapping").isEqualTo(registerMapping).
                   and(where("loadProfileSpec.loadProfileType").isEqualTo(loadProfileType))
                );
    }

    @Override
    public List<DeviceType> findDeviceTypesUsingLogBookType(LogBookType logBookType) {
        return this.getDataModel().
                query(DeviceType.class, DeviceTypeLogBookTypeUsage.class).
                select(where("logBookTypeUsages.logBookType").isEqualTo(logBookType));
    }

    @Override
    public List<DeviceType> findDeviceTypesUsingRegisterMapping(RegisterMapping registerMapping) {
        return this.getDataModel().
                query(DeviceType.class, DeviceTypeRegisterMappingUsage.class).
                select(where("registerMappingUsages.registerMapping").isEqualTo(registerMapping));
    }

    @Override
    public List<DeviceType> findDeviceTypesUsingLoadProfileType(LoadProfileType loadProfileType) {
        return this.getDataModel().
                query(DeviceType.class, DeviceTypeLoadProfileTypeUsage.class).
                select(where("loadProfileTypeUsages.loadProfileType").isEqualTo(loadProfileType));
    }

    @Override
    public List<DeviceConfiguration> findDeviceConfigurationsUsingLogBookType(LogBookType logBookType) {
        return this.getDataModel().
                query(DeviceConfiguration.class, LogBookSpec.class).
                select(where("logBookSpecs.logBookType").isEqualTo(logBookType));
    }

    @Override
    public List<DeviceConfiguration> findDeviceConfigurationsUsingRegisterMapping(RegisterMapping registerMapping) {
        return this.getDataModel().
                query(DeviceConfiguration.class, ChannelSpec.class, RegisterSpec.class).
                select(   where("channelSpecs.registerMapping").isEqualTo(registerMapping).
                       or(where("registerSpecs.registerMapping").isEqualTo(registerMapping)));
    }

    @Override
    public boolean isRegisterMappingUsedByDeviceType(RegisterMapping registerMapping) {
        return !this.getDataModel().
                query(DeviceTypeRegisterMappingUsage.class).select(where("registerMapping").isEqualTo(registerMapping)).isEmpty();
    }

    @Override
    public Finder<DeviceConfiguration> findDeviceConfigurationsUsingDeviceType(DeviceType deviceType) {
        return DefaultFinder.of(DeviceConfiguration.class, where("deviceType").isEqualTo(deviceType), this.getDataModel()).defaultSortColumn("lower(name)");
    }

    @Override
    public DeviceCommunicationConfiguration findDeviceCommunicationConfiguration(long id) {
        return dataModel.mapper(DeviceCommunicationConfiguration.class).getOptional(id).orNull();
    }

    @Override
    public DeviceCommunicationConfiguration findDeviceCommunicationConfigurationFor(DeviceConfiguration deviceConfiguration) {
        List<DeviceCommunicationConfiguration> configurations = DefaultFinder.of(DeviceCommunicationConfiguration.class, where("deviceConfiguration").isEqualTo(deviceConfiguration), dataModel).find();
        return configurations.isEmpty() ? null : configurations.get(0);
    }

    @Override
    public DeviceCommunicationConfiguration newDeviceCommunicationConfiguration(DeviceConfiguration deviceConfiguration) {
        return DeviceCommunicationConfigurationImpl.from(dataModel, deviceConfiguration);
    }

    @Override
    public NextExecutionSpecs newNextExecutionSpecs(TemporalExpression temporalExpression) {
        return new NextExecutionSpecsImpl(this.dataModel, this.eventService, this.thesaurus).initialize(temporalExpression);
    }

    @Override
    public NextExecutionSpecs findNextExecutionSpecs(long id) {
        return this.dataModel.mapper(NextExecutionSpecs.class).getUnique("id", id).orNull();
    }

    @Override
    public Optional<PartialConnectionTask> getPartialConnectionTask(long id) {
        return dataModel.mapper(PartialConnectionTask.class).getOptional(id);
    }

    @Override
    public List<PartialConnectionTask> findByConnectionTypePluggableClass(ConnectionTypePluggableClass connectionTypePluggableClass) {
        return dataModel.query(PartialConnectionTask.class).select(where("pluggableClass").isEqualTo(connectionTypePluggableClass));
    }

    @Override
    public List<PartialConnectionTask> findByComPortPool(ComPortPool comPortPool) {
        return dataModel.query(PartialConnectionTask.class).select(where("comPortPool").isEqualTo(comPortPool));
    }

    @Override
    public Optional<ProtocolDialectConfigurationProperties> getProtocolDialectConfigurationProperties(long id) {
        return dataModel.mapper(ProtocolDialectConfigurationProperties.class).getOptional(id);
    }

    @Override
    public boolean isPhenomenonInUse(Phenomenon phenomenon) {
        return !this.getDataModel().mapper(ChannelSpec.class).find("phenomenon", phenomenon).isEmpty();
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(COMPONENTNAME, "DeviceType and configurations");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        this.dataModel = dataModel;
    }

    DataModel getDataModel() {
        return dataModel;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
    }

    Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Reference
    public void setReadingTypeUtilService(MdcReadingTypeUtilService readingTypeUtilService) {
        this.readingTypeUtilService = readingTypeUtilService;
    }

    @Reference
    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
        initPrivileges();
    }

    private void initPrivileges() {
        privileges.clear();
        for (Privilege privilege : userService.getPrivileges()) {
            if (COMPONENTNAME.equals(privilege.getComponentName())) {
                Optional<DeviceSecurityUserAction> found = DeviceSecurityUserAction.forName(privilege.getName());
                if (found.isPresent()) {
                    privileges.put(found.get(), privilege);
                }
            }
        }
    }

    Optional<Privilege> findPrivilege(DeviceSecurityUserAction userAction) {
        return Optional.fromNullable(privileges.get(userAction));
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DeviceConfigurationService.class).toInstance(DeviceConfigurationServiceImpl.this);
                bind(ProtocolPluggableService.class).toInstance(protocolPluggableService);
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(Thesaurus.class).toInstance(thesaurus);
                bind(MdcReadingTypeUtilService.class).toInstance(readingTypeUtilService);
                bind(MeteringService.class).toInstance(meteringService);
                bind(EngineModelService.class).toInstance(engineModelService);
                bind(UserService.class).toInstance(userService);
            }
        };
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

    @Override
    public void install() {
        this.install(false);
    }

    private void install(boolean exeuteDdl) {
        new Installer(this.dataModel, this.eventService, this.thesaurus, userService).install(exeuteDdl, true);
        initPrivileges();
    }

    @Reference
    public void setEngineModelService(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

    @Reference
    public void setMasterDataService(MasterDataService masterDataService) {
        this.masterDataService = masterDataService;
    }

    @Override
    public Optional<SecurityPropertySet> findSecurityPropertySet(long id) {
        return dataModel.mapper(SecurityPropertySet.class).getOptional(id);
    }

    @Override
    public List<SecurityPropertySet> findAllSecurityPropertySets() {
        return dataModel.mapper(SecurityPropertySet.class).find();
    }
}