/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryService;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.messaging.MessageService;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.LocalizedException;
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
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallCancellationHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.upgrade.InstallIdentifier;
import com.elster.jupiter.upgrade.UpgradeService;
import com.elster.jupiter.upgrade.V10_4SimpleUpgrader;
import com.elster.jupiter.upgrade.V10_4_1SimpleUpgrader;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.collections.KPermutation;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Order;
import com.elster.jupiter.util.conditions.Where;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.util.streams.Functions;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.common.protocol.DeviceMessage;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.protocol.DeviceProtocolPluggableClass;
import com.energyict.mdc.common.tasks.ComTask;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.common.tasks.ConnectionTask;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.CommunicationTaskService;
import com.energyict.mdc.device.data.tasks.ConnectionTaskService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.DeviceFirmwareHistory;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignManagementOptions;
import com.energyict.mdc.firmware.FirmwareCampaignService;
import com.energyict.mdc.firmware.FirmwareCampaignVersionStateShapshot;
import com.energyict.mdc.firmware.FirmwareCheck;
import com.energyict.mdc.firmware.FirmwareCheckManagementOption;
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
import com.energyict.mdc.firmware.impl.campaign.FirmwareCampaignCustomPropertySet;
import com.energyict.mdc.firmware.impl.campaign.FirmwareCampaignDomainExtension;
import com.energyict.mdc.firmware.impl.campaign.FirmwareCampaignItemCustomPropertySet;
import com.energyict.mdc.firmware.impl.campaign.FirmwareCampaignItemDomainExtension;
import com.energyict.mdc.firmware.impl.campaign.FirmwareCampaignItemServiceCallHandler;
import com.energyict.mdc.firmware.impl.campaign.FirmwareCampaignServiceCallHandler;
import com.energyict.mdc.firmware.impl.campaign.FirmwareCampaignServiceImpl;
import com.energyict.mdc.firmware.impl.search.PropertyTranslationKeys;
import com.energyict.mdc.firmware.security.Privileges;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.tasks.TaskService;
import com.energyict.mdc.upl.messages.DeviceMessageAttribute;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

import javax.inject.Inject;
import javax.validation.MessageInterpolator;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.elster.jupiter.orm.Version.version;
import static com.elster.jupiter.util.conditions.Where.where;

/**
 * Provides an implementation for the {@link FirmwareService} interface.
 */
@Component(name = "com.energyict.mdc.firmware", service = {FirmwareService.class, MessageSeedProvider.class, TranslationKeyProvider.class, ServiceCallCancellationHandler.class}, property = "name=" + FirmwareService.COMPONENTNAME, immediate = true)
public class FirmwareServiceImpl implements FirmwareService, MessageSeedProvider, TranslationKeyProvider, ServiceCallCancellationHandler {
    /*
     * {@link com.energyict.protocolimplv2.messages.DeviceMessageCategories#FIRMWARE}
     */
    private static final int FIRMWARE_DEVICE_MESSAGE_CATEGORY_ID = 9;
    private volatile DeviceMessageSpecificationService deviceMessageSpecificationService;
    private volatile DeviceMessageService deviceMessageService;
    private volatile DataModel dataModel;
    private volatile Thesaurus thesaurus;
    private volatile QueryService queryService;
    private volatile DeviceService deviceService;
    private volatile EventService eventService;
    private volatile TaskService taskService;
    private volatile MessageService messageService;
    private volatile UserService userService;
    private volatile CommunicationTaskService communicationTaskService;
    private volatile PropertySpecService propertySpecService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile UpgradeService upgradeService;
    private volatile TopologyService topologyService;
    private volatile FirmwareCampaignServiceImpl firmwareCampaignService;
    private volatile ServiceCallService serviceCallService;
    private volatile OrmService ormService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile TransactionService transactionService;
    private volatile RegisteredCustomPropertySet registeredCustomPropertySet;
    private volatile DeviceConfigurationService deviceConfigurationService;
    private volatile ConnectionTaskService connectionTaskService;

    private final List<CustomPropertySet> customPropertySets = new ArrayList<>();
    private final List<FirmwareCheck> firmwareChecks = new CopyOnWriteArrayList<>();


    // For OSGI
    public FirmwareServiceImpl() {
        super();
    }

    @Inject
    public FirmwareServiceImpl(OrmService ormService,
                               NlsService nlsService,
                               QueryService queryService,
                               DeviceMessageSpecificationService deviceMessageSpecificationService,
                               DeviceMessageService deviceMessageService,
                               DeviceService deviceService,
                               EventService eventService,
                               TaskService taskService,
                               MessageService messageService,
                               UserService userService,
                               CommunicationTaskService communicationTaskService,
                               PropertySpecService propertySpecService,
                               UpgradeService upgradeService,
                               TopologyService topologyService,
                               ServiceCallService serviceCallService,
                               CustomPropertySetService customPropertySetService,
                               BundleContext bundleContext,
                               TransactionService transactionService,
                               MeteringGroupsService meteringGroupsService,
                               DeviceConfigurationService deviceConfigurationService,
                               ConnectionTaskService connectionTaskService) {
        this();
        setOrmService(ormService);
        setNlsService(nlsService);
        setQueryService(queryService);
        setDeviceServices(deviceService);
        setDeviceMessageSpecificationService(deviceMessageSpecificationService);
        setDeviceMessageService(deviceMessageService);
        setEventService(eventService);
        setTaskService(taskService);
        setMessageService(messageService);
        setUserService(userService);
        setCommunicationTaskService(communicationTaskService);
        setPropertySpecService(propertySpecService);
        setUpgradeService(upgradeService);
        setTopologyService(topologyService);
        setServiceCallService(serviceCallService);
        setCustomPropertySetService(customPropertySetService);
        setTransactionService(transactionService);
        setMeteringGroupsService(meteringGroupsService);
        setDeviceConfigurationService(deviceConfigurationService);
        setConnectionTaskService(connectionTaskService);
        activate(bundleContext);
    }

    @Reference
    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setDeviceMessageSpecificationService(DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    @Reference
    public void setDeviceMessageService(DeviceMessageService deviceMessageService) {
        this.deviceMessageService = deviceMessageService;
    }

    @Reference
    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    @Reference
    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    @Reference
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Reference
    public void setUpgradeService(UpgradeService upgradeService) {
        this.upgradeService = upgradeService;
    }

    @Reference
    public void setTopologyService(TopologyService topologyService) {
        this.topologyService = topologyService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public Set<ProtocolSupportedFirmwareOptions> getSupportedFirmwareOptionsFor(DeviceType deviceType) {
        return deviceType.getDeviceProtocolPluggableClass()
                .map(deviceProtocolPluggableClass -> deviceProtocolPluggableClass.getDeviceProtocol().getSupportedMessages().stream()
                        .filter(spec -> spec.getCategory().getId() == FIRMWARE_DEVICE_MESSAGE_CATEGORY_ID)
                        .map(DeviceMessageSpec::getId)
                        .map(DeviceMessageId::find)
                        .flatMap(Functions.asStream())
                        .map(this.deviceMessageSpecificationService::getProtocolSupportedFirmwareOptionFor)
                        .flatMap(Functions.asStream())
                        .collect(Collectors.toSet()))
                .orElse(Collections.emptySet());
    }

    public boolean isFirmwareCheckActivated(DeviceType deviceType, FirmwareCheckManagementOption checkManagementOption) {
        return findFirmwareManagementOptions(deviceType)
                .filter(firmwareManagementOptions -> firmwareManagementOptions.isActivated(checkManagementOption))
                .isPresent();
    }

    public boolean isFirmwareTypeSupported(DeviceType deviceType, FirmwareType firmwareType) {
        switch (firmwareType) {
            case METER:
                return true;
            case COMMUNICATION:
                return deviceType.getDeviceProtocolPluggableClass()
                        .map(DeviceProtocolPluggableClass::getDeviceProtocol)
                        .filter(DeviceProtocol::supportsCommunicationFirmwareVersion)
                        .isPresent();
            case CA_CONFIG_IMAGE:
                return deviceType.getDeviceProtocolPluggableClass()
                        .map(DeviceProtocolPluggableClass::getDeviceProtocol)
                        .filter(DeviceProtocol::supportsCaConfigImageVersion)
                        .isPresent();
            case AUXILIARY:
                return deviceType.getDeviceProtocolPluggableClass()
                        .map(DeviceProtocolPluggableClass::getDeviceProtocol)
                        .filter(DeviceProtocol::supportsAuxiliaryFirmwareVersion)
                        .isPresent();
            default:
                throw new IllegalArgumentException("Unknown firmware type!");
        }
    }

    @Override
    public EnumSet<FirmwareType> getSupportedFirmwareTypes(DeviceType deviceType) {
        return Arrays.stream(FirmwareType.values())
                .filter(firmwareType -> isFirmwareTypeSupported(deviceType, firmwareType))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(FirmwareType.class)));
    }

    @Override
    public boolean imageIdentifierExpectedAtFirmwareUpload(DeviceType deviceType) {
        if (deviceType.getDeviceProtocolPluggableClass().isPresent()) {
            DeviceProtocol deviceProtocol = deviceType.getDeviceProtocolPluggableClass().get().getDeviceProtocol();
            return deviceProtocol.getSupportedMessages()
                    .stream()
                    .map(DeviceMessageSpec::getId)
                    .filter(id -> DeviceMessageId.find(id).isPresent())
                    .map(DeviceMessageId::from)
                    .anyMatch(dmid -> this.deviceMessageSpecificationService.needsImageIdentifierAtFirmwareUpload(dmid));
        }
        return false;
    }

    @Override
    public boolean isResumeFirmwareUploadEnabled(DeviceType deviceType) {
        if (deviceType.getDeviceProtocolPluggableClass().isPresent()) {
            DeviceProtocol deviceProtocol = deviceType.getDeviceProtocolPluggableClass().get().getDeviceProtocol();
            return deviceProtocol.getSupportedMessages()
                    .stream()
                    .map(DeviceMessageSpec::getId)
                    .filter(id -> DeviceMessageId.find(id).isPresent())
                    .map(DeviceMessageId::from)
                    .anyMatch(dmid -> this.deviceMessageSpecificationService.canResumeFirmwareUpload(dmid));
        }
        return false;
    }

    @Override
    public Optional<com.energyict.mdc.common.protocol.DeviceMessageSpec> defaultFirmwareVersionSpec() {
        // TODO: check usages of this method and get rid of ones that use this insecurely, i.e. find the exact message spec for every case instead of basing on the default one.
        return deviceMessageSpecificationService.findMessageSpecById(DeviceMessageId.FIRMWARE_UPGRADE_WITH_USER_FILE_ACTIVATE_IMMEDIATE.dbValue());
    }

    @Override
    public Optional<DeviceMessageId> bestSuitableFirmwareUpgradeMessageId(DeviceType deviceType, ProtocolSupportedFirmwareOptions firmwareManagementOption, FirmwareVersion firmwareVersion) {
        List<DeviceMessageId> deviceMessageIdList = getDeviceMessageIdList(deviceType, firmwareManagementOption);
        if (!deviceMessageIdList.isEmpty()) {
            if (firmwareVersion != null) {
                return deviceMessageIdList.stream()
                        .filter(deviceMessageId -> this.validateImageIdentifier(deviceMessageId, firmwareVersion.getImageIdentifier()))
                        .findFirst()
                        .map(Optional::of)
                        .orElseGet(deviceMessageIdList.stream()::findFirst);
            } else {
                // If an image identifier message is present, get that first. See CXO-7821 for more details
                return deviceMessageIdList.stream()
                        .filter(deviceMessageSpecificationService::needsImageIdentifierAtFirmwareUpload)
                        .findFirst()
                        .map(Optional::of)
                        .orElseGet(deviceMessageIdList.stream()::findFirst);
            }
        }
        return Optional.empty();
    }

    private boolean validateImageIdentifier(DeviceMessageId deviceMessageId, String imageIdentifier) {
        return (imageIdentifier != null) == deviceMessageSpecificationService.needsImageIdentifierAtFirmwareUpload(deviceMessageId);
    }

    private List<DeviceMessageId> getDeviceMessageIdList(DeviceType deviceType, ProtocolSupportedFirmwareOptions firmwareManagementOption) {
        return deviceType.getDeviceProtocolPluggableClass()
                .map(deviceProtocolPluggableClass -> deviceProtocolPluggableClass.getDeviceProtocol().getSupportedMessages().stream()
                        .map(DeviceMessageSpec::getId)
                        .filter(id -> DeviceMessageId.find(id).isPresent())
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
            condition = condition.and(where(FirmwareVersionImpl.Fields.FIRMWARESTATUS.fieldName()).in(filter.getFirmwareStatuses()));
        }
        if (!filter.getFirmwareVersions().isEmpty()) {
            condition = condition.and(where(FirmwareVersionImpl.Fields.FIRMWAREVERSION.fieldName()).in(filter.getFirmwareVersions()));
        }
        if (!filter.getFirmwareTypes().isEmpty()) {
            condition = condition.and(where(FirmwareVersionImpl.Fields.FIRMWARETYPE.fieldName()).in(filter.getFirmwareTypes()));
        }
        if (!Range.<Integer>all().equals(filter.getRankRange())) {
            condition = condition.and(where(FirmwareVersionImpl.Fields.RANK.fieldName()).in(filter.getRankRange()));
        }
        return DefaultFinder.of(FirmwareVersion.class, condition, dataModel).sorted(FirmwareVersionImpl.Fields.RANK.fieldName(), false);
    }

    Finder<ActivatedFirmwareVersion> findActivatedFirmwareVersion(Condition condition) {
        return DefaultFinder.of(ActivatedFirmwareVersion.class, condition, dataModel);
    }

    Finder<PassiveFirmwareVersion> findPassiveFirmwareVersion(Condition condition) {
        return DefaultFinder.of(PassiveFirmwareVersion.class, condition, dataModel);
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

    @Override
    public FirmwareVersionBuilder newFirmwareVersion(DeviceType deviceType, String firmwareVersion, FirmwareStatus status, FirmwareType type, String imageIdentifier) {
        return new FirmwareVersionImpl.FirmwareVersionImplBuilder(dataModel.getInstance(FirmwareVersionImpl.class), deviceType, firmwareVersion, status, type, imageIdentifier);
    }

    @Override
    public boolean isFirmwareVersionInUse(long firmwareVersionId) {
        Optional<FirmwareVersion> firmwareVersionRef = getFirmwareVersionById(firmwareVersionId);
        return firmwareVersionRef.isPresent()
                && !dataModel.query(ActivatedFirmwareVersion.class)
                .select(where(ActivatedFirmwareVersionImpl.Fields.FIRMWARE_VERSION.fieldName()).isEqualTo(firmwareVersionRef.get()), null, false, null, 1, 1)
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
    public FirmwareCampaignManagementOptions newFirmwareCampaignCheckManagementOptions(FirmwareCampaign firmwareCampaign) {
        return dataModel.getInstance(FirmwareCampaignManagementOptionsImpl.class).init(firmwareCampaign);
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
        // Current firmware version is not in the list (only for non-beacon devices)
        Optional<ActivatedFirmwareVersion> activeFirmwareVersion = getActiveFirmwareVersion(device, firmwareType);
        if ((activeFirmwareVersion.isPresent()) && !(isBeaconType(device))) {
            condition = condition.and(where(FirmwareVersionImpl.Fields.FIRMWAREVERSION.fieldName())
                    .isNotEqual(activeFirmwareVersion.get().getFirmwareVersion().getFirmwareVersion()));
        }
        // And only with specified firmware type
        if (firmwareType != null) {
            condition = condition.and(where(FirmwareVersionImpl.Fields.FIRMWARETYPE.fieldName()).isEqualTo(firmwareType));
        }
        return dataModel.mapper(FirmwareVersion.class).select(condition, Order.descending(FirmwareVersionImpl.Fields.RANK.fieldName()));
    }

    private boolean isBeaconType(Device device) {
        return device.getDeviceType().getDeviceProtocolPluggableClass().get().getName().contains("Elster EnergyICT Beacon3100 G3 DLMS");
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
    public Optional<FirmwareCampaignManagementOptions> findFirmwareCampaignCheckManagementOptions(FirmwareCampaign firmwareCampaign) {
        return dataModel.mapper(FirmwareCampaignManagementOptions.class).getUnique(FirmwareCampaignManagementOptionsImpl.Fields.FWRCAMPAIGN.fieldName(), firmwareCampaign);
    }

    @Override
    public void createFirmwareCampaignVersionStateSnapshot(FirmwareCampaign firmwareCampaign, FirmwareVersion foundFirmware) {
        dataModel.getInstance(FirmwareCampaignVersionSnapshotImpl.class).init(firmwareCampaign, foundFirmware).save();
    }

    @Override
    public List<FirmwareCampaignVersionStateShapshot> findFirmwareCampaignVersionStateSnapshots(FirmwareCampaign firmwareCampaign) {
        return dataModel.query(FirmwareCampaignVersionStateShapshot.class).select(where("firmwareCampaign").isEqualTo(firmwareCampaign));
    }

    @Override
    public Optional<FirmwareManagementOptions> findAndLockFirmwareManagementOptionsByIdAndVersion(DeviceType deviceType, long version) {
        return dataModel.mapper(FirmwareManagementOptions.class).lockObjectIfVersion(version, deviceType.getId());
    }

    @Override
    public void cancelFirmwareUploadForDevice(Device device) {
        getFirmwareComtaskExecution(device)
                .ifPresent(this::cancelFirmwareUpload);
    }

    public void resumeFirmwareUploadForDevice(Device device) {
        device.getMessagesByState(DeviceMessageStatus.PENDING)
                .stream()
                .filter(deviceMessage -> deviceMessage.getSpecification().getCategory().getId() == FIRMWARE_DEVICE_MESSAGE_CATEGORY_ID)
                .forEach(deviceMessage -> {
                    this.resume(device, deviceMessage);
                    deviceMessage.updateDeviceMessageStatus(DeviceMessageStatus.CANCELED);
                });
    }

    private void resume(Device device, DeviceMessage deviceMessage) {
        // "Cloning" the current message but with a resume attribute set to true
        Device.DeviceMessageBuilder deviceMessageBuilder = device
                .newDeviceMessage(deviceMessage.getDeviceMessageId())
                .setReleaseDate(deviceMessage.getReleaseDate())
                .setTrackingId(deviceMessage.getTrackingId());
        for (DeviceMessageAttribute attribute : deviceMessage.getAttributes()) {
            if (attribute.getName().equals("FirmwareDeviceMessage.upgrade.resume")) {
                deviceMessageBuilder.addProperty("FirmwareDeviceMessage.upgrade.resume", Boolean.TRUE);
            } else {
                deviceMessageBuilder.addProperty(attribute.getName(), attribute.getValue());
            }
        }
        // Persisting the clone
        deviceMessageBuilder.add();
        // revoking the previous message
        // deviceMessage.revoke();    ???? Revoking = Change status to DeviceMessageStatus.CANCELED ... can't we leave it as DeviceMessageStatus.FAILED
        // As revoking the message leads to canceling the deviceInFirmwareCampaign : see FirmwareCampaignHandler
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

    private void cancelFirmwareUpload(ComTaskExecution fwComTaskExecution) {
        long comTaskId = fwComTaskExecution.getId();
        Optional<ConnectionTask> ct = connectionTaskService.findAndLockConnectionTaskById(fwComTaskExecution.getConnectionTaskId());
        fwComTaskExecution = communicationTaskService.findAndLockComTaskExecutionById(comTaskId)
                .orElseThrow(() -> new IllegalStateException("ComTaskExecution with id: " + comTaskId + " not found."));
        if (fwComTaskExecution.getNextExecutionTimestamp() != null) {
            fwComTaskExecution.schedule(null);
            fwComTaskExecution.updateNextExecutionTimestamp();
        }
        cancelPendingFirmwareMessages(fwComTaskExecution.getDevice());
    }

    private void cancelPendingFirmwareMessages(Device device) {
        device.getMessagesByState(DeviceMessageStatus.PENDING)
                .stream()
                .filter(deviceMessage -> deviceMessage.getSpecification().getCategory().getId() == FIRMWARE_DEVICE_MESSAGE_CATEGORY_ID)
                .forEach(DeviceMessage::revoke);
    }

    @Override
    public FirmwareManagementDeviceUtils getFirmwareManagementDeviceUtilsFor(Device device) {
        return getFirmwareManagementDeviceUtilsFor(device, false);
    }

    @Override
    public FirmwareManagementDeviceUtils getFirmwareManagementDeviceUtilsFor(Device device, boolean onlyLastMessagePerFirmwareType) {
        return dataModel.getInstance(FirmwareManagementDeviceUtilsImpl.class).initFor(device, onlyLastMessagePerFirmwareType);
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
    public void validateFirmwareFileSignature(DeviceType deviceType, SecurityAccessor securityAccessor, File firmwareFile) {
        Optional<DeviceProtocolPluggableClass> protocol = deviceType.getDeviceProtocolPluggableClass();
        if (protocol.isPresent() && securityAccessor.getActualValue().isPresent() && securityAccessor.getActualValue().get() instanceof CertificateWrapper) {
            CertificateWrapper certificateWrapper = (CertificateWrapper) securityAccessor.getActualValue().get();
            if (certificateWrapper.getCertificate().isPresent()) {
                X509Certificate x509Certificate = certificateWrapper.getCertificate().get();
                PublicKey publicKey = x509Certificate.getPublicKey();
                try {
                    if (protocol.get().getDeviceProtocol().firmwareSignatureCheckSupported()
                            && !protocol.get().getDeviceProtocol().verifyFirmwareSignature(firmwareFile, publicKey)) {
                        throw new SignatureValidationFailedException(thesaurus, MessageSeeds.SIGNATURE_VALIDATION_FAILED);
                    }
                } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException | IOException e) {
                    throw new SignatureValidationFailedException(thesaurus, MessageSeeds.SIGNATURE_VALIDATION_FAILED);
                }
            }
        }
    }

    @Override
    public Thesaurus getThesaurus() {
        return thesaurus;
    }

    @Activate
    public final void activate(BundleContext bundleContext) {
        try {
            customPropertySets.add(new FirmwareCampaignCustomPropertySet(thesaurus, propertySpecService, this));
            customPropertySets.add(new FirmwareCampaignItemCustomPropertySet(thesaurus, propertySpecService, this));
            customPropertySets.forEach(customPropertySetService::addCustomPropertySet);
            dataModel = ormService.newDataModel(FirmwareService.COMPONENTNAME, "Firmware configuration");
            for (TableSpecs spec : TableSpecs.values()) {
                spec.addTo(dataModel);
            }
            dataModel.register(new AbstractModule() {
                @Override
                protected void configure() {
                    bind(DataModel.class).toInstance(dataModel);
                    bind(FirmwareServiceImpl.class).toInstance(FirmwareServiceImpl.this);
                    bind(FirmwareService.class).toInstance(FirmwareServiceImpl.this);
                    bind(MessageInterpolator.class).toInstance(thesaurus);
                    bind(Thesaurus.class).toInstance(thesaurus);
                    bind(QueryService.class).toInstance(queryService);
                    bind(DeviceMessageSpecificationService.class).toInstance(deviceMessageSpecificationService);
                    bind(DeviceMessageService.class).toInstance(deviceMessageService);
                    bind(DeviceService.class).toInstance(deviceService);
                    bind(EventService.class).toInstance(eventService);
                    bind(TaskService.class).toInstance(taskService);
                    bind(MessageService.class).toInstance(messageService);
                    bind(UserService.class).toInstance(userService);
                    bind(PropertySpecService.class).toInstance(propertySpecService);
                    bind(CommunicationTaskService.class).toInstance(communicationTaskService);
                    bind(ConnectionTaskService.class).toInstance(connectionTaskService);
                    bind(TopologyService.class).toInstance(topologyService);
                    bind(ServiceCallService.class).toInstance(serviceCallService);
                    bind(OrmService.class).toInstance(ormService);
                    bind(MeteringGroupsService.class).toInstance(meteringGroupsService);
                    bind(CustomPropertySetService.class).toInstance(customPropertySetService);
                    bind(TransactionService.class).toInstance(transactionService);
                    bind(DeviceConfigurationService.class).toInstance(deviceConfigurationService);
                    bind(FirmwareCampaignService.class).to(FirmwareCampaignServiceImpl.class).in(Scopes.SINGLETON);

                }
            });
            registeredCustomPropertySet = customPropertySetService.findActiveCustomPropertySet(FirmwareCampaignCustomPropertySet.CUSTOM_PROPERTY_SET_ID).get();
            firmwareCampaignService = dataModel.getInstance(FirmwareCampaignServiceImpl.class);
            upgradeService.register(
                    InstallIdentifier.identifier("MultiSense", FirmwareService.COMPONENTNAME),
                    dataModel,
                    Installer.class,
                    ImmutableMap.of(
                            version(10, 2), UpgraderV10_2.class,
                            version(10, 4), V10_4SimpleUpgrader.class,
                            version(10, 4, 1), V10_4_1SimpleUpgrader.class,
                            version(10, 6), UpgraderV10_6.class,
                            version(10, 7), UpgraderV10_7.class
                    ));
            addFirmwareCheck(dataModel.getInstance(StatusOfTargetFirmwareCheck.class));
            addFirmwareCheck(dataModel.getInstance(NoGhostFirmwareCheck.class));
            addFirmwareCheck(dataModel.getInstance(MinimumLevelFirmwareCheck.class));
            addFirmwareCheck(dataModel.getInstance(NoDowngradeFirmwareCheck.class));
            addFirmwareCheck(dataModel.getInstance(MasterHasLatestFirmwareCheck.class));
            firmwareCampaignService.activate(bundleContext);
        } catch (RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Deactivate
    public final void deactivate() {
        customPropertySets.forEach(customPropertySetService::removeCustomPropertySet);
        customPropertySets.clear();
        firmwareChecks.clear();
        firmwareCampaignService.deactivate();
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
        this.ormService = ormService;
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
        return Stream.of(
                        Privileges.values(),
                        PropertyTranslationKeys.values(),
                        TranslationKeys.values(),
                        FirmwareType.values(),
                        new TranslationKey[]{FirmwareCheck.CHECK_PREFIX},
                        FirmwareCheckTranslationKeys.values(),
                        FirmwareStatusTranslationKeys.values()
                )
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<MessageSeed> getSeeds() {
        return Arrays.asList(MessageSeeds.values());
    }

    public DataModel getDataModel() {
        return this.dataModel;
    }

    public OrmService getOrmService() {
        return this.ormService;
    }

    @Override
    public DeviceFirmwareHistory getFirmwareHistory(Device device) {
        return new DeviceFirmwareHistoryImpl(this, device);
    }

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    public void addFirmwareCheck(FirmwareCheck firmwareCheck) {
        firmwareChecks.add(firmwareCheck);
    }

    public void removeFirmwareCheck(FirmwareCheck firmwareCheck) {
        firmwareChecks.remove(firmwareCheck);
    }

    @Override
    public Stream<FirmwareCheck> getFirmwareChecks() {
        return firmwareChecks.stream();
    }

    public Optional<FirmwareVersion> getMaximumFirmware(DeviceType deviceType, Set<FirmwareType> accountedTypes, Set<FirmwareStatus> accountedStatuses) {
        return dataModel.stream(FirmwareVersion.class)
                .filter(where(FirmwareVersionImpl.Fields.DEVICETYPE.fieldName()).isEqualTo(deviceType))
                .filter(where(FirmwareVersionImpl.Fields.FIRMWARETYPE.fieldName()).in(ImmutableList.copyOf(accountedTypes)))
                .filter(where(FirmwareVersionImpl.Fields.FIRMWARESTATUS.fieldName()).in(ImmutableList.copyOf(accountedStatuses)))
                .max(FirmwareVersionImpl.Fields.RANK.fieldName());
    }

    @Override
    public List<FirmwareVersionImpl> getOrderedFirmwareVersions(DeviceType deviceType) {
        return dataModel.stream(FirmwareVersionImpl.class)
                .filter(where(FirmwareVersionImpl.Fields.DEVICETYPE.fieldName()).isEqualTo(deviceType))
                .sorted(Order.descending(FirmwareVersionImpl.Fields.RANK.fieldName()))
                .select();
    }

    @Override
    public void reorderFirmwareVersions(DeviceType deviceType, KPermutation kPermutation) {
        List<FirmwareVersionImpl> currentFirmwaresOrder = getOrderedFirmwareVersions(deviceType);
        if (!kPermutation.isPermutation(currentFirmwaresOrder)) {
            throw new LocalizedException(thesaurus, MessageSeeds.INVALID_FIRMWARE_VERSIONS_PERMUTATION) {
            };
        }
        List<FirmwareVersionImpl> target = kPermutation.perform(currentFirmwaresOrder);
        int size = target.size();
        List<FirmwareVersionImpl> toUpdate = new ArrayList<>();
        for (int i = 0; i < size; ++i) {
            int rank = size - i;
            FirmwareVersionImpl item = target.get(i);
            if (rank != item.getRank()) {
                item.setRank(rank);
                toUpdate.add(item);
            }
        }
        dataModel.mapper(FirmwareVersionImpl.class).update(toUpdate, FirmwareVersionImpl.Fields.RANK.fieldName());
        // need to validate the operation after changing ranks because objects are re-read from DB for validation
        dataModel.getInstance(FirmwareDependenciesValidator.class).validateDependencyRanks(toUpdate);
    }

    @Override
    public FirmwareCampaignServiceImpl getFirmwareCampaignService() {
        return firmwareCampaignService;
    }

    @Override
    public Optional<com.energyict.mdc.common.protocol.DeviceMessageSpec> getFirmwareMessageSpec(DeviceType deviceType, ProtocolSupportedFirmwareOptions firmwareManagementOptions,
                                                                                                FirmwareVersion firmwareVersion) {
        Optional<DeviceMessageId> firmwareMessageId = getFirmwareMessageId(deviceType, firmwareManagementOptions, firmwareVersion);
        if (firmwareMessageId.isPresent()) {
            return deviceMessageSpecificationService.findMessageSpecById(firmwareMessageId.get().dbValue());
        }
        return Optional.empty();
    }

    public Optional<DeviceMessageId> getFirmwareMessageId(DeviceType deviceType, ProtocolSupportedFirmwareOptions firmwareManagementOptions, FirmwareVersion firmwareVersion) {
        if (deviceType.getDeviceProtocolPluggableClass().isPresent() && firmwareManagementOptions != null) {
            return bestSuitableFirmwareUpgradeMessageId(deviceType, firmwareManagementOptions, firmwareVersion);
        }
        return Optional.empty();
    }

    public RegisteredCustomPropertySet getRegisteredCustomPropertySet() {
        return registeredCustomPropertySet;
    }

    @Override
    public List<ServiceCallType> getTypes() {
        return Arrays.asList(serviceCallService.findServiceCallType(FirmwareCampaignServiceCallHandler.NAME, FirmwareCampaignServiceCallHandler.VERSION)
                        .orElseThrow(() -> new IllegalStateException("Service call type not found.")),
                serviceCallService.findServiceCallType(FirmwareCampaignItemServiceCallHandler.NAME, FirmwareCampaignItemServiceCallHandler.VERSION)
                        .orElseThrow(() -> new IllegalStateException("Service call type not found.")));
    }

    @Override
    public void cancel(ServiceCall serviceCall) {
        if (serviceCall.getParent().isPresent()) {
            serviceCall.getExtension(FirmwareCampaignItemDomainExtension.class).get().cancel(false);
        } else {
            serviceCall.getExtension(FirmwareCampaignDomainExtension.class).get().cancel();
        }
    }

    @Override
    public String getLocalizedFirmwareStatus(FirmwareStatus firmwareStatus) {
        return FirmwareStatusTranslationKeys.translationFor(firmwareStatus, thesaurus);
    }
}
