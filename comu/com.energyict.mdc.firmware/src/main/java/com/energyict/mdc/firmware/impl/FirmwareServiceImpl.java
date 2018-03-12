/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

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
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.nls.TranslationKeyProvider;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.pki.CertificateWrapper;
import com.elster.jupiter.pki.SecurityAccessor;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.V10_4SimpleUpgrader;
import com.elster.jupiter.upgrade.V10_4_1SimpleUpgrader;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.DeviceFirmwareHistory;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.DevicesInFirmwareCampaignFilter;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignStatus;
import com.energyict.mdc.firmware.FirmwareManagementDeviceStatus;
import com.energyict.mdc.firmware.FirmwareManagementDeviceUtils;
import com.energyict.mdc.firmware.FirmwareManagementOptions;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.FirmwareVersionBuilder;
import com.energyict.mdc.firmware.FirmwareVersionFilter;
import com.energyict.mdc.firmware.PassiveFirmwareVersion;
import com.energyict.mdc.firmware.SecurityAccessorOnDeviceType;
import com.energyict.mdc.firmware.impl.search.PropertyTranslationKeys;
import com.energyict.mdc.firmware.security.Privileges;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessage;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.tasks.ComTask;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.upl.messages.DeviceMessageAttribute;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link FirmwareService} interface.
 *
 */
@Component(name = "com.energyict.mdc.firmware", service = {FirmwareService.class, MessageSeedProvider.class, TranslationKeyProvider.class}, property = "name=" + FirmwareService.COMPONENTNAME, immediate = true)
public class FirmwareServiceImpl implements FirmwareService, MessageSeedProvider, TranslationKeyProvider {

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
    private volatile PropertySpecService propertySpecService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile UpgradeService upgradeService;

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
                               UserService userService,
                               CommunicationTaskService communicationTaskService,
                               PropertySpecService propertySpecService,
                               UpgradeService upgradeService) {
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
        setPropertySpecService(propertySpecService);
        setUpgradeService(upgradeService);
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

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Override
    public Set<ProtocolSupportedFirmwareOptions> getSupportedFirmwareOptionsFor(DeviceType deviceType) {
        return deviceType.getDeviceProtocolPluggableClass()
                .map(deviceProtocolPluggableClass -> deviceProtocolPluggableClass.getDeviceProtocol().getSupportedMessages().stream()
                        .map(DeviceMessageSpec::getId)
                        .map(DeviceMessageId::from)
                        .map(this.deviceMessageSpecificationService::getProtocolSupportedFirmwareOptionFor)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    @Override
    public boolean imageIdentifierExpectedAtFirmwareUpload(DeviceType deviceType) {
        if (deviceType.getDeviceProtocolPluggableClass().isPresent()){
            DeviceProtocol deviceProtocol = deviceType.getDeviceProtocolPluggableClass().get().getDeviceProtocol();
            return deviceProtocol.getSupportedMessages().stream().map(DeviceMessageSpec::getId).map(DeviceMessageId::from).anyMatch(dmid -> this.deviceMessageSpecificationService.needsImageIdentifierAtFirmwareUpload(dmid));
        }
        return false;
    }
    @Override
    public boolean isResumeFirmwareUploadEnabled(DeviceType deviceType){
        if (deviceType.getDeviceProtocolPluggableClass().isPresent()){
            DeviceProtocol deviceProtocol = deviceType.getDeviceProtocolPluggableClass().get().getDeviceProtocol();
            return deviceProtocol.getSupportedMessages().stream().map(DeviceMessageSpec::getId).map(DeviceMessageId::from).anyMatch(dmid -> this.deviceMessageSpecificationService.canResumeFirmwareUpload(dmid));
        }
        return false;
    }

    @Override
    public Optional<com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec> defaultFirmwareVersionSpec() {
        return deviceMessageSpecificationService.findMessageSpecById(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE.dbValue());
    }

    @Override
    public Optional<DeviceMessageId> bestSuitableFirmwareUpgradeMessageId(DeviceType deviceType, ProtocolSupportedFirmwareOptions firmwareManagementOption,FirmwareVersion firmwareVersion){
        List<DeviceMessageId> deviceMessageIdList = getDeviceMessageIdList(deviceType, firmwareManagementOption);
        if (!deviceMessageIdList.isEmpty()) {
            if (firmwareVersion != null) {
                return deviceMessageIdList.stream()
                        .filter(deviceMessageId -> this.validateImageIdentifier(deviceMessageId, firmwareVersion.getImageIdentifier()))
                        .findFirst();
            } else {
                // If an image identifier message is present, get that first. See CXO-7821 for more details
                Optional<DeviceMessageId> messageIdOptional = deviceMessageIdList.stream()
                        .filter(DeviceMessageId.needsImageIdentifier()::contains)
                        .findFirst();
                return messageIdOptional.isPresent() ? messageIdOptional : deviceMessageIdList.stream().findFirst();
            }
        }
        return Optional.empty();
    }

    private boolean validateImageIdentifier(DeviceMessageId deviceMessageId, String imageIdentifier) {
        return (imageIdentifier != null) == DeviceMessageId.needsImageIdentifier().contains(deviceMessageId);
    }

    private List<DeviceMessageId> getDeviceMessageIdList(DeviceType deviceType, ProtocolSupportedFirmwareOptions firmwareManagementOption) {
        return deviceType.getDeviceProtocolPluggableClass()
                .map(deviceProtocolPluggableClass -> deviceProtocolPluggableClass.getDeviceProtocol().getSupportedMessages().stream()
                        .map(DeviceMessageSpec::getId)
                        .map(DeviceMessageId::from)
                        .collect(Collectors.toList())).orElse(Collections.emptyList())
                .stream()
                .filter(firmwareMessageCandidate -> {
                    Optional<ProtocolSupportedFirmwareOptions> firmwareOptionForCandidate = deviceMessageSpecificationService.getProtocolSupportedFirmwareOptionFor(firmwareMessageCandidate);
                    return firmwareOptionForCandidate.isPresent() && firmwareManagementOption.equals(firmwareOptionForCandidate.get());
                })
                .collect(Collectors.toList());
    }

    @Override
    public FirmwareVersionFilter filterForFirmwareVersion(DeviceType deviceType) {
        return new FirmwareVersionFilterImpl(deviceType);
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

    Finder<ActivatedFirmwareVersion> findActivatedFirmwareVersion(Condition condition) {
        return DefaultFinder.of(ActivatedFirmwareVersion.class, condition, dataModel);
    }

    Finder<PassiveFirmwareVersion> findPassiveFirmwareVersion(Condition condition) {
        return DefaultFinder.of(PassiveFirmwareVersion.class, condition, dataModel);
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
    public Optional<FirmwareVersion> findAndLockFirmwareVersionByIdAndVersion(long id, long version) {
        return dataModel.mapper(FirmwareVersion.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Optional<FirmwareVersion> getFirmwareVersionByVersionAndType(String version, FirmwareType firmwareType, DeviceType deviceType) {
        return dataModel.mapper(FirmwareVersion.class)
                .getUnique(new String[]{FirmwareVersionImpl.Fields.FIRMWAREVERSION.fieldName(), FirmwareVersionImpl.Fields.DEVICETYPE.fieldName(), FirmwareVersionImpl.Fields.FIRMWARETYPE
                        .fieldName()}, new Object[]{version, deviceType, firmwareType});
    }

    @Override
    public FirmwareVersionBuilder newFirmwareVersion(DeviceType deviceType, String firmwareVersion, FirmwareStatus status, FirmwareType type) {
        return new FirmwareVersionImpl.FirmwareVersionImplBuilder(dataModel.getInstance(FirmwareVersionImpl.class), deviceType, firmwareVersion, status, type);
    }

    public FirmwareVersionBuilder newFirmwareVersion(DeviceType deviceType, String firmwareVersion, FirmwareStatus status, FirmwareType type, String imageIdentifier) {
        return new FirmwareVersionImpl.FirmwareVersionImplBuilder(dataModel.getInstance(FirmwareVersionImpl.class), deviceType, firmwareVersion, status, type, imageIdentifier);
    }

    @Override
    public boolean isFirmwareVersionInUse(long firmwareVersionId) {
        Optional<FirmwareVersion> firmwareVersionRef = getFirmwareVersionById(firmwareVersionId);
        return firmwareVersionRef.isPresent()
                && !dataModel.query(ActivatedFirmwareVersion.class)
                .select(where(ActivatedFirmwareVersionImpl.Fields.FIRMWARE_VERSION.fieldName()).isEqualTo(firmwareVersionRef.get()), null, false, null, 1, 2)
                .isEmpty();
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
                .sorted(Comparator.comparing(DeviceType::getName))
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
    public Optional<FirmwareManagementOptions> findFirmwareManagementOptions(DeviceType deviceType) {
        return dataModel.mapper(FirmwareManagementOptions.class).getUnique(FirmwareVersionImpl.Fields.DEVICETYPE.fieldName(), deviceType);
    }

    @Override
    public Optional<FirmwareManagementOptions> findAndLockFirmwareManagementOptionsByIdAndVersion(DeviceType deviceType, long version) {
        return dataModel.mapper(FirmwareManagementOptions.class).lockObjectIfVersion(version, deviceType.getId());
    }

    @Override
    public Optional<FirmwareCampaign> getFirmwareCampaignById(long id) {
        return dataModel.mapper(FirmwareCampaign.class).getOptional(id);
    }

    @Override
    public Optional<FirmwareCampaign> findAndLockFirmwareCampaignByIdAndVersion(long id, long version) {
        return dataModel.mapper(FirmwareCampaign.class).lockObjectIfVersion(version, id);
    }

    @Override
    public Finder<FirmwareCampaign> getFirmwareCampaigns() {
        return DefaultFinder.of(FirmwareCampaign.class, Condition.TRUE, dataModel).sorted(FirmwareCampaignImpl.Fields.STARTED_ON.fieldName(), false);
    }

    @Override
    public List<FirmwareCampaign> findFirmwareCampaigns(DeviceType deviceType) {
        return this.dataModel.mapper(FirmwareCampaign.class).find(FirmwareCampaignImpl.Fields.DEVICE_TYPE.fieldName(), deviceType);
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

    @Override
    public DevicesInFirmwareCampaignFilter filterForDevicesInFirmwareCampaign() {
        return new DevicesInFirmwareCampaignFilterImpl(this, deviceService);
    }

    @Override
    public Finder<DeviceInFirmwareCampaign> getDevicesForFirmwareCampaign(DevicesInFirmwareCampaignFilter filter) {
        return DefaultFinder.of(DeviceInFirmwareCampaign.class, filter.getCondition(), dataModel);
    }

    public List<DeviceInFirmwareCampaignImpl> getDeviceInFirmwareCampaignsFor(Device device) {
        return dataModel.query(DeviceInFirmwareCampaignImpl.class, FirmwareCampaign.class, Device.class)
                .select(where(DeviceInFirmwareCampaignImpl.Fields.DEVICE.fieldName()).isEqualTo(device).and(
                        where(DeviceInFirmwareCampaignImpl.Fields.CAMPAIGN.fieldName() + "." + FirmwareCampaignImpl.Fields.STATUS.fieldName()).isNotEqual(FirmwareCampaignStatus.COMPLETE)));
    }

    @Override
    public Optional<FirmwareCampaign> getFirmwareCampaign(ComTaskExecution comTaskExecution) {
        Optional<FirmwareCampaignImpl> firmwareCampaign = getDeviceInFirmwareCampaignsFor(comTaskExecution.getDevice())
                .stream()
                .filter(DeviceInFirmwareCampaignImpl::hasNonFinalStatus)
                .map(DeviceInFirmwareCampaignImpl::getFirmwareCampaign)
                .findAny();
        return Optional.ofNullable(firmwareCampaign.orElse(null));
    }

    @Override
    public void cancelFirmwareCampaign(FirmwareCampaign firmwareCampaign) {
        ((FirmwareCampaignImpl) firmwareCampaign).cancel();
    }

    @Override
    public boolean cancelFirmwareUploadForDevice(Device device) {
        Optional<ComTaskExecution> fwComTaskExecution = getFirmwareComtaskExecution(device);
        return fwComTaskExecution.isPresent() && cancelFirmwareUpload(fwComTaskExecution);
    }

    public void resumeFirmwareUploadForDevice(Device device) {
            device.getMessagesByState(DeviceMessageStatus.PENDING)
                    .stream()
                    .filter(this::isItAFirmwareRelatedMessage)
                    .forEach(x-> this.resume(device, x));
    }

    private void resume(Device device, DeviceMessage deviceMessage){
        // "Cloning" the current message but with a resume attribute set to true
        Device.DeviceMessageBuilder deviceMessageBuilder = device
                .newDeviceMessage(deviceMessage.getDeviceMessageId())
                .setReleaseDate(deviceMessage.getReleaseDate())
                .setTrackingId(deviceMessage.getTrackingId());
        for (DeviceMessageAttribute attribute: deviceMessage.getAttributes()) {
            if (attribute.getName().equals("FirmwareDeviceMessage.upgrade.resume")){
                deviceMessageBuilder.addProperty("FirmwareDeviceMessage.upgrade.resume" , Boolean.TRUE);
            }else {
                deviceMessageBuilder.addProperty(attribute.getName(), attribute.getValue());
            }
        }
        // Persisting the clone
        deviceMessageBuilder.add();
        // revoking the previous message
        // deviceMessage.revoke();    ???? Revoking = Change status to DeviceMessageStatus.CANCELED ... can't we leave it as DeviceMessageStatus.FAILED
        // As revoking the message leads to canceling the deviceInFirmwareCampaign : see FirmwareCampaignHandler
    }


    @Override
    public boolean retryFirmwareUploadForDevice(DeviceInFirmwareCampaign deviceInFirmwareCampaign) {
        if (deviceInFirmwareCampaign.getFirmwareCampaign().getStatus() != FirmwareCampaignStatus.ONGOING) {
            throw RetryDeviceInFirmwareCampaignExceptions.invalidState(this.thesaurus);
        }
        // retry
        if (!deviceInFirmwareCampaign.getStatus().canTransitToStatus(FirmwareManagementDeviceStatus.UPLOAD_PENDING)) {
            throw RetryDeviceInFirmwareCampaignExceptions.transitionToPendingStateImpossible(this.thesaurus, deviceInFirmwareCampaign);
        }
        ((DeviceInFirmwareCampaignImpl) deviceInFirmwareCampaign).retryFirmwareProces();
        return true;
    }

    private Optional<ComTaskExecution> getFirmwareComtaskExecution(Device device) {
        Optional<ComTask> fwComTask = taskService.findFirmwareComTask();
        if (fwComTask.isPresent()) {
            ComTask firmwareComTask = fwComTask.get();
            return device.getComTaskExecutions().stream()
                    .filter(comTaskExecution -> comTaskExecution.getComTask().getId() == firmwareComTask.getId())
                    .findAny();
        } else {
            return Optional.empty();
        }
    }

    private boolean cancelFirmwareUpload(Optional<ComTaskExecution> fwComTaskExecution) {
        ComTaskExecution comTaskExecution1 = fwComTaskExecution.get();
        if (comTaskExecution1.getNextExecutionTimestamp() != null) {
            comTaskExecution1.schedule(null);
            cancelPendingFirmwareMessages(comTaskExecution1.getDevice());
            return true;
        } else {
            return false;
        }
    }

    private void cancelPendingFirmwareMessages(Device device) {
        device.getMessagesByState(DeviceMessageStatus.PENDING)
                .stream()
                .filter(this::isItAFirmwareRelatedMessage)
                .forEach(DeviceMessage::revoke);
    }

    @Override
    public Optional<DeviceInFirmwareCampaign> getDeviceInFirmwareCampaignsForDevice(FirmwareCampaign firmwareCampaign, Device device) {
        return dataModel.mapper(DeviceInFirmwareCampaign.class)
                .getUnique(DeviceInFirmwareCampaignImpl.Fields.CAMPAIGN.fieldName(), firmwareCampaign, DeviceInFirmwareCampaignImpl.Fields.DEVICE.fieldName(), device);
    }

    @Override
    public FirmwareManagementDeviceUtils getFirmwareManagementDeviceUtilsFor(Device device) {
        return dataModel.getInstance(FirmwareManagementDeviceUtilsImpl.class).initFor(device);
    }

    @Override
    public Finder<SecurityAccessorOnDeviceType> findSecurityAccessorForSignatureValidation(DeviceType deviceType, SecurityAccessor securityAccessor) {
        Condition deviceTypeCondition = where(SecurityAccessorOnDeviceTypeImpl.Fields.DEVICETYPE.fieldName()).isEqualTo(deviceType);
        Condition securityAccessorCondition = where(SecurityAccessorOnDeviceTypeImpl.Fields.SECACCESSOR.fieldName()).isEqualTo(securityAccessor);
        Condition condition = deviceTypeCondition.and(securityAccessorCondition);
        return DefaultFinder.of(SecurityAccessorOnDeviceType.class, condition, dataModel);
    }

    @Override
    public Finder<SecurityAccessorOnDeviceType> findSecurityAccessorForSignatureValidation(SecurityAccessor securityAccessor) {
        Condition securityAccessorCondition = where(SecurityAccessorOnDeviceTypeImpl.Fields.SECACCESSOR.fieldName()).isEqualTo(securityAccessor);
        return DefaultFinder.of(SecurityAccessorOnDeviceType.class, securityAccessorCondition, dataModel);
    }

    @Override
    public Finder<SecurityAccessorOnDeviceType> findSecurityAccessorForSignatureValidation(DeviceType deviceType) {
        Condition deviceTypeCondition = where(SecurityAccessorOnDeviceTypeImpl.Fields.DEVICETYPE.fieldName()).isEqualTo(deviceType);
        return DefaultFinder.of(SecurityAccessorOnDeviceType.class, deviceTypeCondition, dataModel);
    }

    @Override
    public void addSecurityAccessorForSignatureValidation(DeviceType deviceType, SecurityAccessor securityAccessor) {
        List<SecurityAccessorOnDeviceType> securityAccessorOnDeviceTypeList = findSecurityAccessorForSignatureValidation(deviceType, securityAccessor).find();
        if (securityAccessorOnDeviceTypeList.isEmpty()) {
            this.dataModel.getInstance(SecurityAccessorOnDeviceTypeImpl.class).init(deviceType, securityAccessor).save();
        }
    }

    @Override
    public void deleteSecurityAccessorForSignatureValidation(DeviceType deviceType, SecurityAccessor securityAccessor) {
        List<SecurityAccessorOnDeviceType> securityAccessorOnDeviceTypeList = findSecurityAccessorForSignatureValidation(deviceType, securityAccessor).find();
        securityAccessorOnDeviceTypeList.forEach(SecurityAccessorOnDeviceType::delete);
    }

    @Override
    public void validateFirmwareFileSignature(SecurityAccessor securityAccessor, byte[] firmwareFile) {
        if (securityAccessor.getActualValue().isPresent() && securityAccessor.getActualValue().get() instanceof CertificateWrapper) {
            CertificateWrapper certificateWrapper = (CertificateWrapper) securityAccessor.getActualValue().get();
            if (certificateWrapper.getCertificate().isPresent()) {
                X509Certificate x509Certificate = certificateWrapper.getCertificate().get();
                PublicKey publicKey = x509Certificate.getPublicKey();
            }
        }
    }

    @Override
    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    private boolean isItAFirmwareRelatedMessage(DeviceMessage deviceDeviceMessage) {
        return Stream.of(
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_ACTIVATE_IMMEDIATE,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_RESUME_OPTION_AND_TYPE_ACTIVATE_IMMEDIATE,
                DeviceMessageId.FIRMWARE_UPGRADE_ACTIVATE,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_AND_ACTIVATE_DATE,
                DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_VERSION_AND_ACTIVATE_DATE,
                DeviceMessageId.FIRMWARE_UPGRADE_URL_ACTIVATE_IMMEDIATE,
                DeviceMessageId.FIRMWARE_UPGRADE_URL_AND_ACTIVATE_DATE
        )
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
                    bind(PropertySpecService.class).toInstance(propertySpecService);
                }
            });
            upgradeService.register(
                    InstallIdentifier.identifier("MultiSense", FirmwareService.COMPONENTNAME),
                    dataModel,
                    Installer.class,
                    ImmutableMap.of(
                            version(10, 2), UpgraderV10_2.class,
                            version(10, 4), V10_4SimpleUpgrader.class,
                            version(10, 4, 1), V10_4_1SimpleUpgrader.class
                    ));
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

    @Reference
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
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
        keys.addAll(Arrays.asList(Privileges.values()));
        keys.addAll(Arrays.asList(PropertyTranslationKeys.values()));
        keys.addAll(Arrays.asList(TranslationKeys.values()));
        return keys;
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    public DataModel getDataModel() {
        return this.dataModel;
    }

    @Override
    public DeviceFirmwareHistory getFirmwareHistory(Device device) {
        return new DeviceFirmwareHistoryImpl(this, device);
    }

}
