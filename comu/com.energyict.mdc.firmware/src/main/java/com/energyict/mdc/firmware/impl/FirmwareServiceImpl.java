package com.energyict.mdc.firmware.impl;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.common.HasId;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.firmware.*;
import com.energyict.mdc.firmware.security.Privileges;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.MessageSeedProvider;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.callback.InstallService;
import com.elster.jupiter.users.PrivilegesProvider;
import com.elster.jupiter.users.ResourceDefinition;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.time.Interval;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.util.conditions.Where.where;


/**
 * Provides an implementation for the {@link FirmwareService} interface.
 *
 * Copyrights EnergyICT
 * Date: 3/5/15
 * Time: 10:33 AM
 */
@Component(name = "com.energyict.mdc.firmware", service = {FirmwareService.class, InstallService.class, ReferencePropertySpecFinderProvider.class, MessageSeedProvider.class, PrivilegesProvider.class}, property = "name=" + FirmwareService.COMPONENTNAME, immediate = true)
public class FirmwareServiceImpl implements FirmwareService, InstallService, MessageSeedProvider, PrivilegesProvider {

    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile QueryService queryService;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile DeviceService deviceService;
    private volatile EventService eventService;
    private volatile TaskService taskService;
    private volatile MessageService messageService;
    private volatile UserService userService;
    private volatile CommunicationTaskService communicationTaskService;

    // For OSGI
    public FirmwareServiceImpl() {
        super();
    }

    @Inject
    public FirmwareServiceImpl(OrmService ormService,
                               NlsService nlsService,
                               QueryService queryService,
                               DeviceConfigurationService deviceConfigurationService,
                               DeviceMessageSpecificationService deviceMessageSpecificationService,
                               DeviceService deviceService,
                               EventService eventService,
                               TaskService taskService,
                               MessageService messageService,
                               UserService userService, CommunicationTaskService communicationTaskService) {
        this();
        setOrmService(ormService);
        setNlsService(nlsService);
        setQueryService(queryService);
        setDeviceServices(deviceService);
        setDeviceConfigurationService(deviceConfigurationService);
        setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        setEventService(eventService);
        setTaskService(taskService);
        setMessageService(messageService);
        setUserService(userService);
        setCommunicationTaskService(communicationTaskService);
        if (!dataModel.isInstalled()) {
            install();
        }
        activate();
    }

    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
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
    public Finder<FirmwareVersion> findAllFirmwareVersions(FirmwareVersionFilter filter) {
        if (filter == null) {
            throw new IllegalArgumentException("Please provide a filter with preset device type");
        }
        Condition condition = where(FirmwareVersionImpl.Fields.DEVICETYPE.fieldName()).isEqualTo(filter.getDeviceType());
        if (!filter.getFirmwareStatuses().isEmpty()) {
            condition = condition.and(createMultipleConditions(filter.getFirmwareStatuses(), FirmwareVersionImpl.Fields.FIRMWARESTATUS.fieldName()));
        }
        if (!filter.getFirmwareVersions().isEmpty()) {
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
    public Optional<FirmwareVersion> getFirmwareVersionByVersionAndType(String version, FirmwareType firmwareType, DeviceType deviceType) {
        return dataModel.mapper(FirmwareVersion.class).getUnique(new String[]{FirmwareVersionImpl.Fields.FIRMWAREVERSION.fieldName(), FirmwareVersionImpl.Fields.DEVICETYPE.fieldName(), FirmwareVersionImpl.Fields.FIRMWARETYPE
                .fieldName()}, new Object[]{version, deviceType, firmwareType});
    }

    @Override
    public FirmwareVersion newFirmwareVersion(DeviceType deviceType, String firmwareVersion, FirmwareStatus status, FirmwareType type) {
        return FirmwareVersionImpl.from(dataModel, deviceType, firmwareVersion, status, type);
    }

    @Override
    public boolean isFirmwareVersionInUse(long firmwareVersionId) {
        Optional<FirmwareVersion> firmwareVersionRef = getFirmwareVersionById(firmwareVersionId);
        if (firmwareVersionRef.isPresent()) {
            return !dataModel.query(ActivatedFirmwareVersion.class)
                    .select(where(ActivatedFirmwareVersionImpl.Fields.FIRMWARE_VERSION.fieldName()).isEqualTo(firmwareVersionRef.get()), null, false, null, 1, 2)
                    .isEmpty();
        }
        return false;
    }

    @Override
    public Optional<FirmwareManagementOptions> getFirmwareManagementOptions(DeviceType deviceType) {
        return dataModel.mapper(FirmwareManagementOptions.class).getUnique(FirmwareVersionImpl.Fields.DEVICETYPE.fieldName(), deviceType);
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
    public FirmwareManagementOptions newFirmwareManagementOptions(DeviceType deviceType) {
        return dataModel.getInstance(FirmwareManagementOptionsImpl.class).init(deviceType);
    }

    @Override
    public List<DeviceType> getDeviceTypesWhichSupportFirmwareManagement() {
        Condition condition = where(FirmwareManagementOptionsImpl.Fields.ACTIVATEONDATE.fieldName()).isEqualTo(true)
                .or(where(FirmwareManagementOptionsImpl.Fields.ACTIVATE.fieldName()).isEqualTo(true))
                .or(where(FirmwareManagementOptionsImpl.Fields.INSTALL.fieldName()).isEqualTo(true));
        return dataModel.query(FirmwareManagementOptionsImpl.class, DeviceType.class).select(condition).stream()
                .map(FirmwareManagementOptionsImpl::getDeviceType)
                .sorted((dt1, dt2) -> dt1.getName().compareTo(dt2.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public List<FirmwareVersion> getAllUpgradableFirmwareVersionsFor(Device device, FirmwareType firmwareType) {
        // Only final and test versions are displayed in the list
        Condition condition = where(FirmwareVersionImpl.Fields.FIRMWARESTATUS.fieldName()).isEqualTo(FirmwareStatus.FINAL)
                .or(where(FirmwareVersionImpl.Fields.FIRMWARESTATUS.fieldName()).isEqualTo(FirmwareStatus.TEST))
                .and(where(FirmwareVersionImpl.Fields.DEVICETYPE.fieldName()).isEqualTo(device.getDeviceType()));
        // Current firmware version is not in the list
        Optional<ActivatedFirmwareVersion> activeFirmwareVersion = getActiveFirmwareVersion(device, firmwareType);
        if (activeFirmwareVersion.isPresent()) {
            condition = condition.and(where(FirmwareVersionImpl.Fields.FIRMWAREVERSION.fieldName())
                    .isNotEqual(activeFirmwareVersion.get().getFirmwareVersion().getFirmwareVersion()));
        }
        // And only with specified firmware type
        if (firmwareType != null) {
            condition = condition.and(where(FirmwareVersionImpl.Fields.FIRMWARETYPE.fieldName()).isEqualTo(firmwareType));
        }
        return dataModel.mapper(FirmwareVersion.class).select(condition, Order.descending("lower(firmwareVersion)"));
    }

    @Override
    public Optional<ActivatedFirmwareVersion> getActiveFirmwareVersion(Device device, FirmwareType firmwareType) {
        QueryExecutor<ActivatedFirmwareVersion> activeFirmwareVersionQuery = dataModel.query(ActivatedFirmwareVersion.class, FirmwareVersion.class);
        activeFirmwareVersionQuery.setRestriction(where("firmwareVersion.firmwareType").isEqualTo(firmwareType));
        return activeFirmwareVersionQuery
                .select(where("device").isEqualTo(device).and(Where.where("interval").isEffective()))
                .stream()
                .findFirst();
    }

    @Override
    public ActivatedFirmwareVersion newActivatedFirmwareVersionFrom(Device device, FirmwareVersion firmwareVersion, Interval interval) {
        return ActivatedFirmwareVersionImpl.from(dataModel, device, firmwareVersion, interval);
    }

    @Override
    public PassiveFirmwareVersion newPassiveFirmwareVersionFrom(Device device, FirmwareVersion firmwareVersion, Interval interval) {
        return PassiveFirmwareVersionImpl.from(dataModel, device, firmwareVersion, interval);
    }

    @Override
    public Optional<FirmwareManagementOptions> findFirmwareManagementOptionsByDeviceType(DeviceType deviceType) {
        return dataModel.mapper(FirmwareManagementOptions.class).getUnique("deviceType", deviceType);
    }

    @Override
    public Optional<FirmwareCampaign> getFirmwareCampaignById(long id) {
        return dataModel.mapper(FirmwareCampaign.class).getOptional(id);
    }

    @Override
    public Finder<FirmwareCampaign> getFirmwareCampaigns() {
        return DefaultFinder.of(FirmwareCampaign.class, Condition.TRUE, dataModel).sorted(FirmwareCampaignImpl.Fields.STARTED_ON.fieldName(), false);
    }

    @Override
    public FirmwareCampaign newFirmwareCampaign(DeviceType deviceType, EndDeviceGroup endDeviceGroup) {
        return dataModel.getInstance(FirmwareCampaignImpl.class).init(deviceType, endDeviceGroup);
    }

    @Override
    public Finder<DeviceInFirmwareCampaign> getDevicesForFirmwareCampaign(FirmwareCampaign firmwareCampaign) {
        Condition condition = where(DeviceInFirmwareCampaignImpl.Fields.CAMPAIGN.fieldName()).isEqualTo(firmwareCampaign);
        return DefaultFinder.of(DeviceInFirmwareCampaign.class, condition, dataModel);
    }

    public List<DeviceInFirmwareCampaignImpl> getDeviceInFirmwareCampaignsFor(Device device) {
        return dataModel.query(DeviceInFirmwareCampaignImpl.class, FirmwareCampaign.class, Device.class)
                .select(where(DeviceInFirmwareCampaignImpl.Fields.DEVICE.fieldName()).isEqualTo(device).and(
                        where(DeviceInFirmwareCampaignImpl.Fields.CAMPAIGN.fieldName() + "." + FirmwareCampaignImpl.Fields.STATUS.fieldName()).isNotEqual(FirmwareCampaignStatus.COMPLETE)));
    }

    @Override
    public void cancelFirmwareCampaign(FirmwareCampaign firmwareCampaign) {
        ((FirmwareCampaignImpl) firmwareCampaign).cancel();
    }

    @Override
    public boolean cancelFirmwareUploadForDevice(Device device) {
        Optional<ComTask> fwComTask = taskService.findFirmwareComTask();
        if (fwComTask.isPresent()) {
            ComTask firmwareComTask = fwComTask.get();
            Optional<ComTaskExecution> fwComTaskExecution = device.getComTaskExecutions().stream()
                    .filter(comTaskExecution -> comTaskExecution.getComTasks().stream()
                            .filter(comTask -> comTask.getId() == firmwareComTask.getId())
                            .findAny()
                            .isPresent())
                    .findAny();
            return fwComTaskExecution.isPresent() && cancelFirmwareFirmwareUpload(device, fwComTaskExecution);
        } else {
            return false;
        }
    }

    private boolean cancelFirmwareFirmwareUpload(Device device, Optional<ComTaskExecution> fwComTaskExecution) {
        ComTaskExecution comTaskExecution1 = fwComTaskExecution.get();
        if (communicationTaskService.isComTaskStillPending(comTaskExecution1.getId())) {
            comTaskExecution1.putOnHold();
            cancelPendingFirmwareMessages(device);
            return true;
        } else {
            return false;
        }
    }

    private void cancelPendingFirmwareMessages(Device device) {
        device.getMessagesByState(DeviceMessageStatus.PENDING)
                .stream()
                .filter(this::isItAFirmwareRelatedMessage)
                .forEach(deviceDeviceMessage -> {
                    deviceDeviceMessage.revoke();
                    deviceDeviceMessage.save();
                });
    }

    @Override
    public Optional<DeviceInFirmwareCampaign> getDeviceInFirmwareCampaignsForDevice(FirmwareCampaign firmwareCampaign, Device device) {
        return dataModel.mapper(DeviceInFirmwareCampaign.class).getUnique(DeviceInFirmwareCampaignImpl.Fields.CAMPAIGN.fieldName(), firmwareCampaign, DeviceInFirmwareCampaignImpl.Fields.DEVICE.fieldName(), device);
    }

    @Override
    public FirmwareManagementDeviceUtils getFirmwareManagementDeviceUtilsFor(Device device) {
        return dataModel.getInstance(FirmwareManagementDeviceUtilsImpl.class).initFor(device);
    }

    private boolean isItAFirmwareRelatedMessage(DeviceMessage<Device> deviceDeviceMessage) {
        return Stream.of(
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_LATER,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_ACTIVATE_IMMEDIATE,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE_ACTIVATE_IMMEDIATE,
                DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_VERSION_AND_ACTIVATE_DATE,
                DeviceMessageId.FIRMWARE_UPGRADE_URL_ACTIVATE_IMMEDIATE,
                DeviceMessageId.FIRMWARE_UPGRADE_URL_AND_ACTIVATE_DATE,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE)
                .filter(firmwareDeviceMessage -> firmwareDeviceMessage.equals(deviceDeviceMessage.getDeviceMessageId()))
                .findAny().isPresent();
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
                    bind(TaskService.class).toInstance(taskService);
                    bind(MessageService.class).toInstance(messageService);
                    bind(UserService.class).toInstance(userService);
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
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    @Reference
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Reference
    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public List<String> getPrerequisiteModules() {
        return Arrays.asList(OrmService.COMPONENTNAME,
                NlsService.COMPONENTNAME,
                DeviceConfigurationService.COMPONENTNAME,
                DeviceMessageSpecificationService.COMPONENT_NAME,
                DeviceDataServices.COMPONENT_NAME,
                EventService.COMPONENTNAME,
                TaskService.COMPONENT_NAME,
                MessageService.COMPONENTNAME,
                UserService.COMPONENTNAME,
                MeteringGroupsService.COMPONENTNAME
        );
    }

    @Override
    public void install() {
        Installer installer = new Installer(dataModel, eventService, messageService, userService);
        installer.install();
    }

    @Override
    public Layer getLayer() {
        return Layer.DOMAIN;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    @Override
    public List<CanFindByLongPrimaryKey<? extends HasId>> finders() {
        List<CanFindByLongPrimaryKey<? extends HasId>> canFindByLongPrimaryKeys = new ArrayList<>();
        canFindByLongPrimaryKeys.add(new FirmwareVersionFinder());
        return canFindByLongPrimaryKeys;
    }

    public DataModel getDataModel() {
        return this.dataModel;
    }

    @Override
    public String getModuleName() {
        return FirmwareService.COMPONENTNAME;
    }

    @Override
    public List<ResourceDefinition> getModuleResources() {
        List<ResourceDefinition> resources = new ArrayList<>();
        resources.add(userService.createModuleResourceWithPrivileges(FirmwareService.COMPONENTNAME, "firmware.campaigns", "firmware.campaigns.description",
                Arrays.asList(
                        Privileges.VIEW_FIRMWARE_CAMPAIGN, Privileges.ADMINISTRATE_FIRMWARE_CAMPAIGN)));
        return resources;
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
