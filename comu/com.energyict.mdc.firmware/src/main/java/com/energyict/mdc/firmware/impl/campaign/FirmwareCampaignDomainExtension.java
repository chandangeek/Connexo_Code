/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */
package com.energyict.mdc.firmware.impl.campaign;

import com.elster.jupiter.cps.AbstractPersistentDomainExtension;
import com.elster.jupiter.cps.CustomPropertySetValues;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.callback.PersistenceAware;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.time.TimeDuration;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Where;
import com.energyict.mdc.common.ComWindow;
import com.energyict.mdc.common.device.config.ConnectionStrategy;
import com.energyict.mdc.common.device.config.DeviceType;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.common.protocol.DeviceMessageId;
import com.energyict.mdc.common.protocol.DeviceMessageSpec;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignProperty;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.firmware.impl.EventType;
import com.energyict.mdc.firmware.impl.FirmwareServiceImpl;
import com.energyict.mdc.firmware.impl.HasUniqueName;
import com.energyict.mdc.firmware.impl.MessageSeeds;
import com.energyict.mdc.firmware.impl.UniqueName;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.firmware.BaseFirmwareVersion;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@UniqueName(groups = {Save.Create.class, Save.Update.class})

public class FirmwareCampaignDomainExtension extends AbstractPersistentDomainExtension implements PersistentDomainExtension<ServiceCall>, FirmwareCampaign, HasUniqueName, PersistenceAware {


    public enum FieldNames {
        DOMAIN("serviceCall", "service_call"),
        NAME("name", "name"),
        DEVICE_TYPE("deviceType", "device_type"),
        DEVICE_GROUP("deviceGroup", "device_group"),
        MANAGEMENT_OPTION("managementOption", "management_option"),
        FIRMWARE_TYPE("firmwareType", "firmware_type"),
        UPLOAD_PERIOD_START("uploadPeriodStart", "activation_start"),
        UPLOAD_PERIOD_END("uploadPeriodEnd", "activation_end"),
        ACTIVATION_DATE("activationDate", "activation_date"),
        VALIDATION_TIMEOUT("validationTimeout", "validation_timeout"),
        PROPERTIES("properties", "properties"),
        VALIDATION_COMTASK_ID("validationComTaskId", "VALIDATION_COMTASK_ID"),
        FIRMWARE_UPLOAD_COMTASK_ID("firmwareUploadComTaskId", "FIRMWARE_UPLOAD_COMTASK_ID"),
        VALIDATION_CONNECTIONSTRATEGY("validationConnectionStrategy", "VALIDATION_CONSTRATEGY"),
        FIRMWARE_UPLOAD_CONNECTIONSTRATEGY("firmwareUploadConnectionStrategy", "FIRMWARE_UPLOAD_CONSTRATEGY"),
        MANUALLY_CANCELLED("manuallyCancelled", "MANUALLY_CANCELLED"),
        WITH_UNIQUE_FIRMWARE_VERSION("withUniqueFirmwareVersion", "WITH_UNIQUE_FIRMWARE_VERSION");

        FieldNames(String javaName, String databaseName) {
            this.javaName = javaName;
            this.databaseName = databaseName;
        }

        private final String javaName;
        private final String databaseName;

        public String javaName() {
            return javaName;
        }

        public String databaseName() {
            return databaseName;
        }
    }

    public static final int SECONDS_IN_DAY = 86400;
    private static final Logger LOGGER = Logger.getLogger(FirmwareCampaignDomainExtension.class.getName());
    private final DataModel dataModel;
    private final DataModel cpsDataModel;
    private final Thesaurus thesaurus;
    private final ServiceCallService serviceCallService;
    private final EventService eventService;
    private final FirmwareServiceImpl firmwareService;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final DeviceMessageService deviceMessageService;
    private final Reference<ServiceCall> serviceCall = Reference.empty();

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String name;
    @IsPresent
    private final Reference<DeviceType> deviceType = Reference.empty();
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String deviceGroup;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private ProtocolSupportedFirmwareOptions managementOption;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private FirmwareType firmwareType;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Instant uploadPeriodStart;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Instant uploadPeriodEnd;
    private Instant activationDate;
    private TimeDuration validationTimeout;
    private List<FirmwareCampaignProperty> properties = new ArrayList<>();
    private Long firmwareUploadComTaskId;
    private ConnectionStrategy firmwareUploadConnectionStrategy;
    private Long validationComTaskId;
    private ConnectionStrategy validationConnectionStrategy;
    private boolean manuallyCancelled;
    private boolean withUniqueFirmwareVersion;

    @Inject
    public FirmwareCampaignDomainExtension(Thesaurus thesaurus, FirmwareServiceImpl firmwareService) {
        super();
        this.dataModel = firmwareService.getDataModel();
        this.thesaurus = thesaurus;
        this.serviceCallService = dataModel.getInstance(ServiceCallService.class);
        this.eventService = dataModel.getInstance(EventService.class);
        this.firmwareService = firmwareService;
        this.deviceMessageSpecificationService = dataModel.getInstance(DeviceMessageSpecificationService.class);
        this.cpsDataModel = dataModel.getInstance(OrmService.class).getDataModel(FirmwareCampaignPersistenceSupport.COMPONENT_NAME).get();
        this.deviceMessageService = dataModel.getInstance(DeviceMessageService.class);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public DeviceType getDeviceType() {
        return deviceType.get();
    }

    @Override
    public FirmwareType getFirmwareType() {
        return firmwareType;
    }

    public void setFirmwareType(FirmwareType firmwareType) {
        this.firmwareType = firmwareType;
    }

    @Override
    public ProtocolSupportedFirmwareOptions getFirmwareManagementOption() {
        return managementOption;
    }

    public void setManagementOption(ProtocolSupportedFirmwareOptions managementOption) {
        this.managementOption = managementOption;
    }

    public void setFirmwareUploadComTaskId(Long firmwareUploadComTaskId) {
        this.firmwareUploadComTaskId = firmwareUploadComTaskId;
    }

    public void setFirmwareUploadConnectionStrategy(ConnectionStrategy firmwareUploadConnectionStrategy) {
        this.firmwareUploadConnectionStrategy = firmwareUploadConnectionStrategy;
    }

    public void setValidationComTaskId(Long validationComTaskId) {
        this.validationComTaskId = validationComTaskId;
    }

    public void setValidationConnectionStrategy(ConnectionStrategy validationConnectionStrategy) {
        this.validationConnectionStrategy = validationConnectionStrategy;
    }

    @Override
    public Optional<ConnectionStrategy> getFirmwareUploadConnectionStrategy() {
        return Optional.ofNullable(firmwareUploadConnectionStrategy);
    }

    @Override
    public Long getValidationComTaskId() {
        return validationComTaskId;
    }

    @Override
    public Long getFirmwareUploadComTaskId() {
        return firmwareUploadComTaskId;
    }

    @Override
    public Optional<ConnectionStrategy> getValidationConnectionStrategy() {
        return Optional.ofNullable(validationConnectionStrategy);
    }

    @Override
    public Map<String, Object> getProperties() {
        Optional<DeviceMessageSpec> firmwareMessageSpec = getFirmwareMessageSpec();
        if (firmwareMessageSpec.isPresent()) {
            Map<String, Object> convertedProperties = new HashMap<>();
            for (FirmwareCampaignProperty property : properties) {
                firmwareMessageSpec
                        .get()
                        .getPropertySpec(property.getKey())
                        .ifPresent(propertySpec ->
                                convertedProperties.put(
                                        property.getKey(),
                                        propertySpec.getValueFactory().fromStringValue(property.getValue())));
            }
            return convertedProperties;
        }
        return Collections.emptyMap();
    }

    public Optional<DeviceMessageId> getFirmwareMessageId() {
        if (deviceType.isPresent() && deviceType.get().getDeviceProtocolPluggableClass().isPresent() && getFirmwareManagementOption() != null) {
            return firmwareService.bestSuitableFirmwareUpgradeMessageId(deviceType.get(), getFirmwareManagementOption(), getFirmwareVersion());
        }
        return Optional.empty();
    }

    @Override
    public Optional<DeviceMessageSpec> getFirmwareMessageSpec() {
        Optional<DeviceMessageId> firmwareMessageId = getFirmwareMessageId();
        if (firmwareMessageId.isPresent()) {
            return deviceMessageSpecificationService.findMessageSpecById(firmwareMessageId.get().dbValue());
        }
        return Optional.empty();
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType.set(deviceType);
    }

    @Override
    public String getDeviceGroup() {
        return deviceGroup;
    }

    public void setDeviceGroup(String deviceGroup) {
        this.deviceGroup = deviceGroup;
    }

    @Override
    public Instant getUploadPeriodStart() {
        return uploadPeriodStart;
    }

    public void setUploadPeriodStart(Instant activationStart) {
        this.uploadPeriodStart = activationStart;
    }

    @Override
    public Instant getUploadPeriodEnd() {
        return uploadPeriodEnd;
    }

    public void setUploadPeriodEnd(Instant uploadPeriodEnd) {
        this.uploadPeriodEnd = uploadPeriodEnd;
    }

    @Override
    public Instant getActivationDate() {
        return activationDate;
    }

    public void setActivationDate(Instant activationDate) {
        this.activationDate = activationDate;
    }

    @Override
    public TimeDuration getValidationTimeout() {
        return validationTimeout;
    }

    public void setValidationTimeout(TimeDuration validationTimeout) {
        this.validationTimeout = validationTimeout;
    }

    @Override
    public boolean isManuallyCancelled() {
        return manuallyCancelled;
    }

    public void setManuallyCancelled(boolean manuallyCancelled) {
        this.manuallyCancelled = manuallyCancelled;
    }

    @Override
    public long getId() {
        return serviceCall.get().getId();
    }


    @Override
    public ServiceCall getServiceCall() {
        return serviceCall.get();
    }

    @Override
    public boolean isWithUniqueFirmwareVersion() {
        return withUniqueFirmwareVersion;
    }


    public void withUniqueFirmwareVersion(boolean withUniqueFirmwareVersion) {
        this.withUniqueFirmwareVersion = withUniqueFirmwareVersion;
    }

    @Override
    public Map<DefaultState, Long> getNumbersOfChildrenWithStatuses() {
        return dataModel.getInstance(ServiceCallService.class).getChildrenStatus(getServiceCall().getId());
    }

    @Override
    public void update() {
        getServiceCall().update(this);
        eventService.postEvent(EventType.FIRMWARE_CAMPAIGN_EDITED.topic(), this);
    }

    @Override
    public void cancel() {
        if (isManuallyCancelled()) {
            throw new FirmwareCampaignException(thesaurus, MessageSeeds.CAMPAIGN_ALREADY_CANCELLED);
        }
        ServiceCall serviceCall = getServiceCall();
        setManuallyCancelled(true);
        serviceCall.update(this);
        serviceCall.log(LogLevel.INFO, thesaurus.getSimpleFormat(MessageSeeds.CANCELED_BY_USER).format());
        FirmwareCampaignServiceImpl firmwareCampaignService = firmwareService.getFirmwareCampaignService();
        firmwareCampaignService.cancelServiceCall(serviceCall);
        firmwareCampaignService.cancelDeviceMessage(serviceCall);
    }

    @Override
    public void delete() {
        this.clearProperties();
        getServiceCall().delete();
    }

    @Override
    public FirmwareVersion getFirmwareVersion() {
        Optional<DeviceMessageSpec> firmwareMessageSpec = firmwareService.defaultFirmwareVersionSpec();
        if (firmwareMessageSpec.isPresent()) {
            Optional<PropertySpec> firmwareVersionPropertySpec = firmwareMessageSpec.get()
                    .getPropertySpecs()
                    .stream()
                    .filter(propertySpec -> BaseFirmwareVersion.class.isAssignableFrom(propertySpec.getValueFactory().getValueType()))
                    .findAny();
            if (firmwareVersionPropertySpec.isPresent()) {
                Object firmwareVersion = properties.stream()
                        .filter(property -> property.getKey().equals(firmwareVersionPropertySpec.get().getName()))
                        .findFirst()
                        .map(property -> firmwareVersionPropertySpec.get().getValueFactory().fromStringValue(property.getValue()))
                        .orElse(null);
                if (firmwareVersion instanceof FirmwareVersion) {
                    return (FirmwareVersion) firmwareVersion;
                }
            }
        }
        return null;
    }

    @Override
    public FirmwareCampaign addProperties(Map<PropertySpec, Object> map) {
        map.forEach((propertySpec, propertyValue) -> {
            FirmwareCampaignProperty newProperty = dataModel.getInstance(FirmwareCampaignPropertyImpl.class)
                    .init(this, propertySpec.getName(), propertySpec.getValueFactory().toStringValue(propertyValue));
            dataModel.getValidatorFactory().getValidator().validate(newProperty);
            this.properties.add(newProperty);
        });
        dataModel.mapper(FirmwareCampaignProperty.class).persist(properties);
        return this;
    }

    @Override
    public void clearProperties() {
        this.properties.clear();
    }

    @Override
    public void copyFrom(ServiceCall domainInstance, CustomPropertySetValues propertyValues, Object... additionalPrimaryKeyValues) {
        this.serviceCall.set(domainInstance);
        this.setName((String) propertyValues.getProperty(FieldNames.NAME.javaName()));
        this.setDeviceType((DeviceType) propertyValues.getProperty(FieldNames.DEVICE_TYPE.javaName()));
        this.setDeviceGroup((String) propertyValues.getProperty(FieldNames.DEVICE_GROUP.javaName()));
        this.setUploadPeriodStart((Instant) propertyValues.getProperty(FieldNames.UPLOAD_PERIOD_START.javaName()));
        this.setUploadPeriodEnd((Instant) propertyValues.getProperty(FieldNames.UPLOAD_PERIOD_END.javaName()));
        this.setActivationDate((Instant) propertyValues.getProperty(FieldNames.ACTIVATION_DATE.javaName()));
        this.setValidationTimeout((TimeDuration) propertyValues.getProperty(FieldNames.VALIDATION_TIMEOUT.javaName()));
        this.setFirmwareType((FirmwareType) propertyValues.getProperty(FieldNames.FIRMWARE_TYPE.javaName()));
        this.setManagementOption((ProtocolSupportedFirmwareOptions) propertyValues.getProperty(FieldNames.MANAGEMENT_OPTION.javaName()));
        this.setFirmwareUploadComTaskId((Long) propertyValues.getProperty(FieldNames.FIRMWARE_UPLOAD_COMTASK_ID.javaName()));
        this.setFirmwareUploadConnectionStrategy((ConnectionStrategy) propertyValues.getProperty(FieldNames.FIRMWARE_UPLOAD_CONNECTIONSTRATEGY.javaName()));
        this.setValidationComTaskId((Long) propertyValues.getProperty(FieldNames.VALIDATION_COMTASK_ID.javaName()));
        this.setValidationConnectionStrategy((ConnectionStrategy) propertyValues.getProperty(FieldNames.VALIDATION_CONNECTIONSTRATEGY.javaName()));
        this.setManuallyCancelled((boolean) propertyValues.getProperty(FieldNames.MANUALLY_CANCELLED.javaName()));
        this.withUniqueFirmwareVersion((boolean) propertyValues.getProperty(FieldNames.WITH_UNIQUE_FIRMWARE_VERSION.javaName()));
    }

    @Override
    public void copyTo(CustomPropertySetValues propertySetValues, Object... additionalPrimaryKeyValues) {
        propertySetValues.setProperty(FieldNames.NAME.javaName(), this.getName());
        propertySetValues.setProperty(FieldNames.DEVICE_TYPE.javaName(), this.getDeviceType());
        propertySetValues.setProperty(FieldNames.DEVICE_GROUP.javaName(), this.getDeviceGroup());
        propertySetValues.setProperty(FieldNames.UPLOAD_PERIOD_START.javaName(), this.getUploadPeriodStart());
        propertySetValues.setProperty(FieldNames.UPLOAD_PERIOD_END.javaName(), this.getUploadPeriodEnd());
        propertySetValues.setProperty(FieldNames.ACTIVATION_DATE.javaName(), this.getActivationDate());
        propertySetValues.setProperty(FieldNames.VALIDATION_TIMEOUT.javaName(), this.getValidationTimeout());
        propertySetValues.setProperty(FieldNames.FIRMWARE_TYPE.javaName(), this.getFirmwareType());
        propertySetValues.setProperty(FieldNames.MANAGEMENT_OPTION.javaName(), this.getFirmwareManagementOption());
        propertySetValues.setProperty(FieldNames.FIRMWARE_UPLOAD_COMTASK_ID.javaName(), this.getFirmwareUploadComTaskId());
        propertySetValues.setProperty(FieldNames.FIRMWARE_UPLOAD_CONNECTIONSTRATEGY.javaName(), this.getFirmwareUploadConnectionStrategy().isPresent() ? this.getFirmwareUploadConnectionStrategy()
                .get() : null);
        propertySetValues.setProperty(FieldNames.VALIDATION_COMTASK_ID.javaName(), this.getValidationComTaskId());
        propertySetValues.setProperty(FieldNames.VALIDATION_CONNECTIONSTRATEGY.javaName(), this.getValidationConnectionStrategy().isPresent() ? this.getValidationConnectionStrategy().get() : null);
        propertySetValues.setProperty(FieldNames.MANUALLY_CANCELLED.javaName(), this.isManuallyCancelled());
        propertySetValues.setProperty(FieldNames.WITH_UNIQUE_FIRMWARE_VERSION.javaName(), this.isWithUniqueFirmwareVersion());
    }

    @Override
    public void validateDelete() {
        // nothing to validate
    }

    @Override
    public boolean isValidName(boolean caseSensitive) {
        Condition condition = where(FieldNames.DOMAIN.javaName()).isNotEqual(getServiceCall());
        return cpsDataModel.query(FirmwareCampaignDomainExtension.class).select(condition.and(where(FieldNames.NAME.databaseName()).isEqualTo(this.name))).isEmpty();
    }

    @Override
    public void postLoad() {
        properties = dataModel.mapper(FirmwareCampaignProperty.class)
                .find(FirmwareCampaignPropertyImpl.Fields.CAMPAIGN.fieldName(), this);
    }

    @Override
    public ComWindow getComWindow() {
        return new ComWindow((((Number) (this.getUploadPeriodStart().getEpochSecond() % SECONDS_IN_DAY)).intValue()),
                (((Number) (this.getUploadPeriodEnd().getEpochSecond() % SECONDS_IN_DAY)).intValue()));

    }

    @Override
    public boolean isWithVerification() {
        return !getFirmwareManagementOption().equals(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER);
    }

    @Override
    public Instant getStartedOn() {
        return getServiceCall().getCreationTime();
    }

    @Override
    public Instant getFinishedOn() {
        ServiceCall serviceCall = getServiceCall();
        return (serviceCall.getState().equals(DefaultState.CANCELLED)
                || serviceCall.getState().equals(DefaultState.SUCCESSFUL)) ? serviceCall.getLastModificationTime() : null;
    }

    @Override
    public List<DeviceInFirmwareCampaign> getDevices() {
        return firmwareService.getFirmwareCampaignService().streamDevicesInCampaigns()
                .join(ServiceCall.class).join(ServiceCall.class).join(State.class)
                .filter(Where.where("serviceCall.parent.id").isEqualTo(getId()))
                .collect(Collectors.toList());
    }
}
