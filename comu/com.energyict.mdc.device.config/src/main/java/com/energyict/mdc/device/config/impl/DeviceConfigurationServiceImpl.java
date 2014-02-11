package com.energyict.mdc.device.config.impl;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.Provider;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.ListOperator;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.config.ChannelSpec;
import com.energyict.mdc.device.config.ChannelSpecLinkType;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.config.LoadProfileSpec;
import com.energyict.mdc.device.config.LoadProfileType;
import com.energyict.mdc.device.config.LogBookSpec;
import com.energyict.mdc.device.config.LogBookType;
import com.energyict.mdc.device.config.Phenomenon;
import com.energyict.mdc.device.config.ProductSpec;
import com.energyict.mdc.device.config.RegisterGroup;
import com.energyict.mdc.device.config.RegisterMapping;
import com.energyict.mdc.device.config.RegisterSpec;
import com.google.inject.AbstractModule;
import com.google.inject.Module;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import java.util.List;

/**
 * Provides an implementation for the {@link DeviceConfigurationService} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-01-30 (15:38)
 */
@Component(name="com.energyict.mdc.device.config", service = {DeviceConfigurationService.class, InstallService.class})
public class DeviceConfigurationServiceImpl implements DeviceConfigurationService, InstallService {

    private Provider<LoadProfileTypeImpl> loadProfileTypeProvider;
    private Provider<PhenomenonImpl> phenomenonProvider;
    private volatile DataModel dataModel;
    private volatile EventService eventService;
    private volatile Thesaurus thesaurus;

    public DeviceConfigurationServiceImpl() {
        super();
    }

    @Inject
    public DeviceConfigurationServiceImpl(OrmService ormService, EventService eventService, NlsService nlsService,
                                          Provider<LoadProfileTypeImpl> loadProfileTypeProvider,
                                          Provider<PhenomenonImpl> phenomenonProvider) {
        this();
        this.loadProfileTypeProvider = loadProfileTypeProvider;
        this.phenomenonProvider = phenomenonProvider;
        this.setOrmService(ormService);
        this.setEventService(eventService);
        this.setNlsService(nlsService);
        this.activate();
        if (!this.dataModel.isInstalled()) {
            this.install();
        }
    }

    @Override
    public List<DeviceType> findAllDeviceTypes() {
        return this.getDataModel().mapper(DeviceType.class).find();
    }

    @Override
    public ProductSpec findProductSpec(long id) {
        return this.getDataModel().mapper(ProductSpec.class).getUnique("id", id).orNull();
    }

    @Override
    public List<ProductSpec> findAllProductSpecs() {
        return this.getDataModel().mapper(ProductSpec.class).find();
    }

    @Override
    public ProductSpec newProductSpec(ReadingType readingType) {
        return ProductSpecImpl.from(this.getDataModel(), readingType);
    }

    @Override
    public List<RegisterMapping> findAllRegisterMappings() {
        return this.getDataModel().mapper(RegisterMapping.class).find();
    }

    @Override
    public RegisterMapping findRegisterMapping(long id) {
        return this.getDataModel().mapper((RegisterMapping.class)).getUnique("id", id).orNull();
    }

    @Override
    public RegisterMapping findRegisterMappingByName(String name) {
        return this.getDataModel().mapper((RegisterMapping.class)).getUnique("name", name).orNull();
    }

    @Override
    public RegisterMapping findRegisterMappingByObisCodeAndProductSpec(ObisCode obisCode, ProductSpec productSpec) {
        return this.getDataModel().mapper((RegisterMapping.class)).getUnique("obisCodeString", obisCode.toString(), "productSpec", productSpec).orNull();
    }

    @Override
    public RegisterMapping newRegisterMapping(String name, ObisCode obisCode, ProductSpec productSpec) {
        return RegisterMappingImpl.from(this.getDataModel(), name, obisCode, productSpec);
    }

    @Override
    public List<RegisterGroup> findAllRegisterGroups() {
        return this.getDataModel().mapper(RegisterGroup.class).find();
    }

    @Override
    public RegisterGroup findRegisterGroup(long id) {
        return this.getDataModel().mapper(RegisterGroup.class).getUnique("id", id).orNull();
    }

    @Override
    public RegisterGroup newRegisterGroup(String name) {
        return RegisterGroupImpl.from(this.getDataModel(), name);
    }

    @Override
    public List<LoadProfileType> findAllLoadProfileTypes() {
        return this.getDataModel().mapper(LoadProfileType.class).find();
    }

    @Override
    public LoadProfileType newLoadProfileType(String name, ObisCode obisCode, TimeDuration interval) {
        return LoadProfileTypeImpl.from(this.getDataModel(), name, obisCode, interval);
    }

    @Override
    public List<LogBookType> findAllLogBookTypes() {
        return this.getDataModel().mapper(LogBookType.class).find();
    }

    @Override
    public LogBookType newLogBookType(String name, ObisCode obisCode) {
        return LogBookTypeImpl.from(this.getDataModel(), name, obisCode);
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
    public List<RegisterMapping> findRegisterMappingByDeviceType(int deviceTypeId) {
        Condition condition = Where.where("deviceType").isEqualTo(findDeviceType(deviceTypeId));
        return this.getDataModel().query(RegisterMapping.class, DeviceTypeRegisterMappingUsage.class).select(condition);
    }

    @Override
    public List<RegisterSpec> findRegisterSpecsByRegisterMappings(List<RegisterMapping> mappings) {
        Condition condition = ListOperator.IN.contains("registerMapping", mappings);
        return this.getDataModel().query(RegisterSpec.class, RegisterMapping.class).select(condition);
    }

    @Override
    public DeviceType findDeviceType(long deviceTypeId) {
        return this.getDataModel().mapper((DeviceType.class)).getUnique("id", deviceTypeId).orNull();
    }

    @Override
    public List<RegisterSpec> findRegisterSpecsByDeviceTypeAndRegisterMapping(DeviceType deviceType, RegisterMapping registerMapping) {
        Condition condition = Where.where("deviceType").isEqualTo(deviceType).and(Where.where("registerMapping").isEqualTo(registerMapping));
        return this.getDataModel().query(RegisterSpec.class, RegisterMapping.class, DeviceTypeRegisterMappingUsage.class).select(condition);
    }

    @Override
    public List<RegisterSpec> findRegisterSpecsByRegisterMapping(long registerMappingId) {
        RegisterMapping registerMapping = findRegisterMapping(registerMappingId);
        return this.getDataModel().mapper(RegisterSpec.class).find("registerMapping", registerMapping);
    }

    @Override
    public List<RegisterSpec> findRegisterSpecsByDeviceConfiguration(DeviceConfiguration deviceConfig) {
        return this.getDataModel().mapper(RegisterSpec.class).find("deviceConfig", deviceConfig);
    }

    @Override
    public List<RegisterSpec> findRegisterSpecsByChannelSpecAndLinkType(ChannelSpec channelSpec, ChannelSpecLinkType linkType) {
        Condition condition = Where.where("linkedChannelSpec").isEqualTo(channelSpec).and(Where.where("channelSpecLinkType").isEqualTo(linkType));
        return this.getDataModel().query(RegisterSpec.class, ChannelSpec.class).select(condition);
    }

    @Override
    public List<RegisterSpec> findRegisterSpecsByDeviceConfigurationAndRegisterMapping(long deviceConfigId, long registerMappingId) {
        DeviceConfiguration deviceConfiguration = findDeviceConfiguration(deviceConfigId);
        RegisterMapping registerMapping = findRegisterMapping(registerMappingId);
        Condition condition = Where.where("deviceConfig").isEqualTo(deviceConfiguration).and(Where.where("registerMapping").isEqualTo(registerMapping));
        return this.getDataModel().query(RegisterSpec.class, DeviceConfiguration.class, RegisterMapping.class).select(condition);
    }

    @Override
    public List<ChannelSpec> findChannelSpecsForLoadProfileSpec(LoadProfileSpec loadProfileSpec) {
        return this.getDataModel().mapper(ChannelSpec.class).find("loadProfileSpec", loadProfileSpec);
    }

    @Override
    public LoadProfileType findLoadProfileType(long loadProfileTypeId) {
        return this.getDataModel().mapper(LoadProfileType.class).getUnique("id", loadProfileTypeId).orNull();
    }

    @Override
    public LoadProfileSpec findLoadProfileSpec(int loadProfileSpecId) {
        return this.getDataModel().mapper(LoadProfileSpec.class).getUnique("id", loadProfileSpecId).orNull();
    }

    @Override
    public List<LoadProfileSpec> findLoadProfileSpecsByDeviceConfig(DeviceConfiguration deviceConfiguration) {
        return this.getDataModel().mapper(LoadProfileSpec.class).find("deviceConfiguration", deviceConfiguration);
    }

    @Override
    public LoadProfileSpec findLoadProfileSpecsByDeviceConfigAndLoadProfileType(DeviceConfiguration deviceConfig, LoadProfileType loadProfileType) {
        return this.getDataModel().mapper(LoadProfileSpec.class).getUnique("deviceConfiguration", deviceConfig, "loadProfileType", loadProfileType).orNull();
    }

    @Override
    public LogBookType findLogBookType(long logBookTypeId) {
        return this.getDataModel().mapper(LogBookType.class).getUnique("id", logBookTypeId).orNull();
    }

    @Override
    public LogBookSpec findLogBookSpec(long logBookSpecId) {
        return this.getDataModel().mapper(LogBookSpec.class).getUnique("id", logBookSpecId).orNull();
    }

    @Override
    public List<LogBookSpec> findLogBookSpecsByDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        return this.getDataModel().mapper(LogBookSpec.class).find("deviceConfiguration", deviceConfiguration);
    }

    @Override
    public LogBookSpec findLogBookSpecByDeviceConfigAndLogBookType(DeviceConfiguration deviceConfig, LogBookType logBookType) {
        return this.getDataModel().mapper(LogBookSpec.class).getUnique("deviceConfiguration", deviceConfig, "logBookType", logBookType).orNull();
    }

    @Override
    public boolean isPhenomenonInUse(Phenomenon phenomenon) {
        return this.getDataModel().mapper(ChannelSpec.class).find("phenomenon", phenomenon).size() > 0;
    }

    @Override
    public Phenomenon findPhenomenon(int phenomenonId) {
        return  this.getDataModel().mapper(Phenomenon.class).getUnique("id", phenomenonId).orNull();
    }

    @Override
    public Phenomenon newPhenomenon(String name, Unit unit) {
        return this.phenomenonProvider.get().initialize(name, unit);
    }

    @Override
    public Phenomenon findPhenomenonByNameAndUnit(String name, String unit) {
        return this.getDataModel().mapper(Phenomenon.class).getUnique("name", name, "unitString", unit).orNull();
    }

    @Override
    public List<Phenomenon> findPhenomenonByEdiCode(String ediCode) {
        return this.getDataModel().mapper(Phenomenon.class).find("ediCode", ediCode);
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
    public List<ChannelSpec> findChannelSpecsByDeviceConfiguration(DeviceConfiguration deviceConfiguration) {
        return this.getDataModel().mapper(ChannelSpec.class).find("deviceConfiguration", deviceConfiguration);
    }

    @Override
    public List<ChannelSpec> findChannelSpecsByDeviceConfigurationAndRegisterMapping(DeviceConfiguration deviceConfiguration, RegisterMapping registerMapping) {
        return this.getDataModel().mapper(ChannelSpec.class).find("deviceConfiguration", deviceConfiguration, "registerMapping", registerMapping);
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel(COMPONENTNAME, "DeviceType and configurations");
        for (TableSpecs tableSpecs : TableSpecs.values()) {
            tableSpecs.addTo(dataModel);
        }
        this.dataModel = dataModel;
    }

    public DataModel getDataModel() {
        return dataModel;
    }

    public EventService getEventService() {
        return eventService;
    }

    @Reference
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(COMPONENTNAME, Layer.DOMAIN);
    }

    private Module getModule() {
        return new AbstractModule() {
            @Override
            public void configure() {
                bind(DataModel.class).toInstance(dataModel);
                bind(EventService.class).toInstance(eventService);
                bind(Thesaurus.class).toInstance(thesaurus);
            }
        };
    }

    @Activate
    public void activate() {
        this.dataModel.register(this.getModule());
    }

    @Override
    public void install() {
        new Installer(this.dataModel, this.eventService, this.thesaurus).install(true, true, true);
    }

}