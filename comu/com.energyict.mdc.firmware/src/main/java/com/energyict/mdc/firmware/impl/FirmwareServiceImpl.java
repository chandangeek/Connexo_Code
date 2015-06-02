package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.Query;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareManagementOptions;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.FirmwareVersionFilter;
import com.energyict.mdc.firmware.PassiveFirmwareVersion;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;


/**
 * Copyrights EnergyICT
 * Date: 3/5/15
 * Time: 10:33 AM
 */
@Component(name = "com.energyict.mdc.firmware", service = {FirmwareService.class, InstallService.class, ReferencePropertySpecFinderProvider.class, TranslationKeyProvider.class}, property = "name=" + FirmwareService.COMPONENTNAME, immediate = true)
public class FirmwareServiceImpl implements FirmwareService, InstallService, TranslationKeyProvider {

    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile QueryService queryService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceService deviceService;
    private volatile EventService eventService;

    // For OSGI
    public FirmwareServiceImpl() {
    }

    @Inject
    public FirmwareServiceImpl(OrmService ormService, NlsService nlsService, QueryService queryService, DeviceConfigurationService deviceConfigurationService, DeviceMessageSpecificationService deviceMessageSpecificationService, DeviceService deviceService, EventService eventService) {
        this.deviceService = deviceService;
        setOrmService(ormService);
        setNlsService(nlsService);
        setQueryService(queryService);
        setDeviceConfigurationService(deviceConfigurationService);
        setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        setEventService(eventService);
        if (!dataModel.isInstalled()) {
            install();
        }
        activate();
    }

    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Override
    public Set<ProtocolSupportedFirmwareOptions> getSupportedFirmwareOptionsFor(DeviceType deviceType) {
        return deviceType.getDeviceProtocolPluggableClass().getDeviceProtocol().getSupportedMessages().stream().
                map(this.deviceMessageSpecificationService::getProtocolSupportedFirmwareOptionFor)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toCollection(HashSet::new));
    }

    @Override
    public Query<? extends FirmwareVersion> getFirmwareVersionQuery() {
        return queryService.wrap(dataModel.query(FirmwareVersion.class));
    }

    @Override
    public Finder<FirmwareVersion> findAllFirmwareVersions(FirmwareVersionFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Please provide a filter with preset device type");
        }
        Condition condition = where(FirmwareVersionImpl.Fields.DEVICETYPE.fieldName()).isEqualTo(filter.getDeviceType());
        if (!filter.getFirmwareStatuses().isEmpty()) {
            condition = condition.and(createMultipleConditions(filter.getFirmwareStatuses(), FirmwareVersionImpl.Fields.FIRMWARESTATUS.fieldName()));
        }
        if (!filter.getFirmwareStatuses().isEmpty()) {
            condition = condition.and(createMultipleConditions(filter.getFirmwareVersions(), FirmwareVersionImpl.Fields.FIRMWAREVERSION.fieldName()));
        }
        if (!filter.getFirmwareTypes().isEmpty()) {
            condition = condition.and(createMultipleConditions(filter.getFirmwareTypes(), FirmwareVersionImpl.Fields.FIRMWARETYPE.fieldName()));
        }

        return DefaultFinder.of(FirmwareVersion.class, condition, dataModel).sorted("lower(firmwareVersion)", false);
    }

    private <T> Condition createMultipleConditions(List<T> params, String conditionField) {
        Condition condition = Condition.FALSE;
        for (T value : params) {
            condition = condition.or(where(conditionField).isEqualTo(value));
        }
        return condition;
    }

    @Override
    public Optional<FirmwareVersion> getFirmwareVersionById(long id) {
        return dataModel.mapper(FirmwareVersion.class).getUnique("id", id);
    }

    @Override
    public Optional<FirmwareVersion> getFirmwareVersionByVersion(String version, DeviceType deviceType) {
        return dataModel.mapper(FirmwareVersion.class).getUnique(FirmwareVersionImpl.Fields.FIRMWAREVERSION.fieldName(), version, FirmwareVersionImpl.Fields.DEVICETYPE.fieldName(), deviceType);
    }

    @Override
    public FirmwareVersion newFirmwareVersion(DeviceType deviceType, String firmwareVersion, FirmwareStatus status, FirmwareType type) {
        return FirmwareVersionImpl.from(dataModel, deviceType, firmwareVersion, status, type);
    }

    @Override
    public void saveFirmwareVersion(FirmwareVersion firmwareVersion) {
        FirmwareVersionImpl.class.cast(firmwareVersion).save();
    }

    @Override
    public void deprecateFirmwareVersion(FirmwareVersion firmwareVersion) {
        FirmwareVersionImpl.class.cast(firmwareVersion).deprecate();
    }

    @Override
    public boolean isFirmwareVersionInUse(long firmwareVersionId) {
        // TODO
        return false;
    }

    @Override
     public FirmwareManagementOptions getFirmwareManagementOptions(DeviceType deviceType) {
        Optional<FirmwareManagementOptions> ref = dataModel.mapper(FirmwareManagementOptions.class).getUnique(FirmwareVersionImpl.Fields.DEVICETYPE.fieldName(), deviceType);
        return ref.isPresent() ? ref.get() : FirmwareManagementOptionsImpl.from(dataModel, deviceType);
    }

    @Override
    public void saveFirmwareManagementOptions(FirmwareManagementOptions firmwareOptions) {
        FirmwareManagementOptionsImpl.class.cast(firmwareOptions).save();
    }

    @Override
    public Set<ProtocolSupportedFirmwareOptions> getAllowedFirmwareManagementOptionsFor(DeviceType deviceType) {
        Set<ProtocolSupportedFirmwareOptions> set = new LinkedHashSet<>();
        Optional<FirmwareManagementOptions> ref = dataModel.mapper(FirmwareManagementOptions.class).getUnique(FirmwareVersionImpl.Fields.DEVICETYPE.fieldName(), deviceType);
         if (ref.isPresent()) {
             FirmwareManagementOptions options = ref.get();
              set = options.getOptions();
         }
        return set;
    }

    @Override
    public List<FirmwareVersion> getAllUpgradableFirmwareVersionsFor(Device device) {
        // Only final and test versions are displayed in the list
        Condition condition = where(FirmwareVersionImpl.Fields.FIRMWARESTATUS.fieldName()).isEqualTo(FirmwareStatus.FINAL)
                .or(where(FirmwareVersionImpl.Fields.FIRMWARESTATUS.fieldName()).isEqualTo(FirmwareStatus.TEST))
                .and(where(FirmwareVersionImpl.Fields.DEVICETYPE.fieldName()).isEqualTo(device.getDeviceType()));
        // Current firmware version is not in the list
        Optional<ActivatedFirmwareVersion> comActiveVer = getCurrentCommunicationFirmwareVersionFor(device);
        if (comActiveVer.isPresent()){
            condition = condition.and(where(FirmwareVersionImpl.Fields.FIRMWAREVERSION.fieldName())
                    .isNotEqual(comActiveVer.get().getFirmwareVersion().getFirmwareVersion()));
        }
        Optional<ActivatedFirmwareVersion> meterActiveVer = getCurrentMeterFirmwareVersionFor(device);
        if (meterActiveVer.isPresent()){
            condition = condition.and(where(FirmwareVersionImpl.Fields.FIRMWAREVERSION.fieldName())
                    .isNotEqual(meterActiveVer.get().getFirmwareVersion().getFirmwareVersion()));
        }
        return dataModel.mapper(FirmwareVersion.class).select(condition, Order.descending("lower(firmwareVersion)"));
    }

    private Condition getCurrentFirmwareVersionFor(Device device){
        return where("device").isEqualTo(device).and(Where.where("interval").isEffective(Instant.now()));
    }

    @Override
    public Optional<ActivatedFirmwareVersion> getCurrentMeterFirmwareVersionFor(Device device) {
        QueryExecutor<ActivatedFirmwareVersion> activeFirmwareVersionQuery = dataModel.query(ActivatedFirmwareVersion.class, FirmwareVersion.class);
        activeFirmwareVersionQuery.setRestriction(where("firmwareVersion.firmwareType").isEqualTo(FirmwareType.METER));
        return activeFirmwareVersionQuery
                .select(getCurrentFirmwareVersionFor(device))
                .stream()
                .findFirst();
    }

    @Override
    public Optional<ActivatedFirmwareVersion> getCurrentCommunicationFirmwareVersionFor(Device device) {
        QueryExecutor<ActivatedFirmwareVersion> activeFirmwareVersionQuery = dataModel.query(ActivatedFirmwareVersion.class, FirmwareVersion.class);
        activeFirmwareVersionQuery.setRestriction(where("firmwareVersion.firmwareType").isEqualTo(FirmwareType.COMMUNICATION));
        return activeFirmwareVersionQuery
                .select(getCurrentFirmwareVersionFor(device))
                .stream()
                .findFirst();
    }

    @Override
    public ActivatedFirmwareVersion newActivatedFirmwareVersionFrom(Device device, FirmwareVersion firmwareVersion, Interval interval) {
        return ActivatedFirmwareVersionImpl.from(dataModel, device, firmwareVersion, interval);
    }

    @Override
    public void saveActivatedFirmwareVersion(ActivatedFirmwareVersion activatedFirmwareVersion) {
        ActivatedFirmwareVersionImpl.class.cast(activatedFirmwareVersion).save();
    }

    @Override
    public PassiveFirmwareVersion newPassiveFirmwareVersionFrom(Device device, FirmwareVersion firmwareVersion, Interval interval) {
        return PassiveFirmwareVersionImpl.from(dataModel, device, firmwareVersion, interval);
    }

    @Override
    public void savePassiveFirmwareVersion(PassiveFirmwareVersion passiveFirmwareVersion) {
        PassiveFirmwareVersionImpl.class.cast(passiveFirmwareVersion).save();
    }

    @Override
    public Optional<FirmwareManagementOptions> findFirmwareManagementOptionsByDeviceType(DeviceType deviceType) {
        return dataModel.mapper(FirmwareManagementOptions.class).getUnique("deviceType", deviceType);
    }

    @Activate
    public final void activate() {
        try {
            dataModel.register(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(DataModel.class).toInstance(dataModel);
                    bind(FirmwareService.class).toInstance(FirmwareServiceImpl.this);
                    bind(MessageInterpolator.class).toInstance(thesaurus);
                    bind(Thesaurus.class).toInstance(thesaurus);
                    bind(QueryService.class).toInstance(queryService);
                    bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
                    bind(DeviceMessageSpecificationService.class).toInstance(deviceMessageSpecificationService);
                    bind(DeviceService.class).toInstance(deviceService);
                    bind(EventService.class).toInstance(eventService);
                }
            });
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Deactivate
    public final void deactivate() {
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setDeviceServices(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setNlsService(NlsService nlsService) {
        this.thesaurus = nlsService.getThesaurus(FirmwareService.COMPONENTNAME, Layer.DOMAIN);
    }

    @Reference
    public void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        dataModel = ormService.newDataModel(FirmwareService.COMPONENTNAME, "Firmware configuration");
        for (TableSpecs spec : TableSpecs.values()) {
            spec.addTo(dataModel);
        }
    }

    @Reference
    public void setEventService(EventService eventService){
        this.eventService = eventService;
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(OrmService.COMPONENTNAME, NlsService.COMPONENTNAME, DeviceConfigurationService.COMPONENTNAME, DeviceMessageSpecificationService.COMPONENT_NAME, DeviceDataServices.COMPONENT_NAME);
    }

    @Override
    public void install() {
        Installer installer = new Installer(dataModel, thesaurus, eventService);
        installer.install();
    }

    @Override
    public String getComponentName() {
        return FirmwareService.COMPONENTNAME;
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<TranslationKey> getKeys() {
        List<TranslationKey> keys = new ArrayList<>();
        for (MessageSeeds messageSeed : MessageSeeds.values()) {
            keys.add(messageSeed);
        }
        return keys;
    }

    @Override
    public List<CanFindByLongPrimaryKey<? extends HasId>> finders() {
        ArrayList<CanFindByLongPrimaryKey<? extends HasId>> canFindByLongPrimaryKeys = new ArrayList<>();
        canFindByLongPrimaryKeys.add(new FirmwareVersionFinder());
        return canFindByLongPrimaryKeys;
    }

    public DataModel getDataModel() {
        return this.dataModel;
    }

    private class FirmwareVersionFinder implements CanFindByLongPrimaryKey<FirmwareVersion> {

        @Override
        public FactoryIds factoryId() {
            return FactoryIds.FIRMWAREVERSION;
        }

        @Override
        public Class<FirmwareVersion> valueDomain() {
            return FirmwareVersion.class;
        }

        @Override
        public Optional<FirmwareVersion> findByPrimaryKey(long id) {
            return getFirmwareVersionById(id);
        }

    }
}
