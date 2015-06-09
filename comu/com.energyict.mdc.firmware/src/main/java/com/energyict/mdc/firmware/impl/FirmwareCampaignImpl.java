package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignProperty;
import com.energyict.mdc.firmware.FirmwareCampaignStatus;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_MUST_BE_UNIQUE + "}")
@HasValidFirmwareCampaignAttributes(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
public class FirmwareCampaignImpl implements FirmwareCampaign, HasUniqueName {

    public enum Fields {
        ID ("id"),
        NAME ("name"),
        STATUS ("status"),
        DEVICE_TYPE ("deviceType"),
        MANAGEMENT_OPTION("managementOption"),
        FIRMWARE_TYPE ("firmwareType"),
        STARTED_ON ("startedOn"),
        FINISHED_ON ("finishedOn"),
        DEVICES ("devices"),
        PROPERTIES ("properties"),
        DEVICES_STATUS ("devicesStatus"),
        ;

        private String name;

        Fields(String name) {
            this.name = name;
        }

        public String fieldName(){
            return this.name;
        }
    }

    private long id;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private String name;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private FirmwareCampaignStatus status;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<DeviceType> deviceType = ValueReference.absent();
    @NotNull(groups = {Save.Create.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private EndDeviceGroup deviceGroup;

    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private ProtocolSupportedFirmwareOptions managementOption;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private FirmwareType firmwareType;
    private Instant startedOn;
    private Instant finishedOn;
    @Valid
    private List<DeviceInFirmwareCampaign> devices = new ArrayList<>();
    private List<FirmwareCampaignProperty> properties = new ArrayList<>();
    private Reference<DevicesInFirmwareCampaignStatusImpl> devicesStatus = ValueReference.absent();

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private final DataModel dataModel;
    private final Clock clock;
    private final FirmwareService firmwareService;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;
    private final EventService eventService;

    @Inject
    public FirmwareCampaignImpl(DataModel dataModel, Clock clock, FirmwareService firmwareService, DeviceMessageSpecificationService deviceMessageSpecificationService, EventService eventService) {
        this.dataModel = dataModel;
        this.clock = clock;
        this.firmwareService = firmwareService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        this.eventService = eventService;
    }

    FirmwareCampaign init(DeviceType deviceType, EndDeviceGroup group) {
        this.startedOn = clock.instant();
        setStatus(FirmwareCampaignStatus.ONGOING);
        this.deviceType.set(deviceType);
        this.deviceGroup = group;
        this.startedOn = clock.instant();

        DevicesInFirmwareCampaignStatusImpl devicesStatus = dataModel.getInstance(DevicesInFirmwareCampaignStatusImpl.class);
        devicesStatus.init(this);
        this.devicesStatus.set(devicesStatus);

        return this;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        if (!Checks.is(name).emptyOrOnlyWhiteSpace()){
            this.name = name.trim();
        } else {
            this.name = null;
        }
    }

    @Override
    public FirmwareCampaignStatus getStatus() {
        return status;
    }

    void setStatus(FirmwareCampaignStatus status){
        this.status = status;
    }

    @Override
    public DeviceType getDeviceType() {
        return deviceType.get();
    }

    @Override
    public FirmwareType getFirmwareType() {
        return firmwareType;
    }

    @Override
    public void setFirmwareType(FirmwareType firmwareType) {
        this.firmwareType = firmwareType;
    }

    @Override
    public FirmwareVersion getFirmwareVersion() {
        return (FirmwareVersion) getProperties().get(DeviceMessageConstants.firmwareUpdateFileAttributeName);
    }

    @Override
    public ProtocolSupportedFirmwareOptions getFirmwareManagementOption() {
        return managementOption;
    }

    @Override
    public void setManagementOption(ProtocolSupportedFirmwareOptions managementOption) {
        this.managementOption = managementOption;
    }

    @Override
    public Instant getStartedOn() {
        return startedOn;
    }

    @Override
    public void setStartedOn(Instant startedOn) {
        this.startedOn = startedOn;
    }

    @Override
    public Instant getFinishedOn() {
        return finishedOn;
    }

    @Override
    public void setFinishedOn(Instant finishedOn) {
        this.finishedOn = finishedOn;
    }

    @Override
    public List<DeviceInFirmwareCampaign> getDevices(){
        return new ArrayList<>(this.devices);
    }

    @Override
    public Map<String, Object> getProperties(){
        Optional<DeviceMessageSpec> firmwareMessageSpec = getFirmwareMessageSpec();
        if (firmwareMessageSpec.isPresent()){
            Map<String, Object> convertedProperties = new HashMap<>();
            for (FirmwareCampaignProperty property : properties) {
                PropertySpec propertySpec = firmwareMessageSpec.get().getPropertySpec(property.getKey());
                if (propertySpec != null){
                    convertedProperties.put(property.getKey(), propertySpec.getValueFactory().fromStringValue(property.getValue()));
                }
            }
            return convertedProperties;
        }
        return Collections.emptyMap();
    }

    public Optional<DeviceMessageId> getFirmwareMessageId() {
        if (deviceType.isPresent() && getFirmwareManagementOption() != null) {
            return deviceType.get().getDeviceProtocolPluggableClass().getDeviceProtocol().getSupportedMessages()
                    .stream()
                    .filter(firmwareMessageCandidate -> {
                        Optional<ProtocolSupportedFirmwareOptions> firmwareOptionForCandidate = deviceMessageSpecificationService.getProtocolSupportedFirmwareOptionFor(firmwareMessageCandidate);
                        return firmwareOptionForCandidate.isPresent() && getFirmwareManagementOption().equals(firmwareOptionForCandidate.get());
                    })
                    .findFirst();
        }
        return Optional.empty();
    }

    @Override
    public Optional<DeviceMessageSpec> getFirmwareMessageSpec(){
        Optional<DeviceMessageId> firmwareMessageId = getFirmwareMessageId();
        if (firmwareMessageId.isPresent()) {
            return deviceMessageSpecificationService.findMessageSpecById(firmwareMessageId.get().dbValue());
        }
        return Optional.empty();
    }

    @Override
    public FirmwareCampaign addProperty(String key, String value){
        FirmwareCampaignProperty newProperty = dataModel.getInstance(FirmwareCampaignPropertyImpl.class).init(this, key, value);
        dataModel.getValidatorFactory().getValidator().validate(newProperty);
        this.properties.add(newProperty);
        return this;
    }

    @Override
    public void clearProperties(){
        this.properties.clear();
    }

    @Override
    public boolean isValidName(boolean caseSensitive) {
        Condition condition = Condition.TRUE;
        if (getId() > 0){
            condition = where(Fields.ID.fieldName()).isNotEqual(getId());
        }
        return dataModel.query(FirmwareCampaign.class).select(condition.and(where(Fields.NAME.fieldName()).isEqualTo(this.name))).isEmpty();
    }

    @Override
    public void save() {
        if (getId() > 0){
            Save.UPDATE.save(dataModel, this);
        } else {
            Save.CREATE.save(dataModel, this);
            this.eventService.postEvent(EventType.FIRMWARE_CAMPAIGN_CREATED.topic(), this);
        }
    }

    @Override
    public void delete(){
        dataModel.remove(this);
    }

    @Override
    public void cancel() {
        if (this.finishedOn == null){
            this.finishedOn = clock.instant();
        }
        setStatus(FirmwareCampaignStatus.CANCELLED);
        save();
//        TODO: cancel campaign
        this.eventService.postEvent(EventType.FIRMWARE_CAMPAIGN_CANCELLED.topic(), this);
    }

    @Override
    public Map<String, Long> getDevicesStatusMap() {
        return this.devicesStatus.get().getStatusMap();
    }

    public void updateStatistic(){
        DevicesInFirmwareCampaignStatusImpl devicesStatus = this.devicesStatus.get();
        devicesStatus.update();
        if (devicesStatus.getOngoing() == 0 && devicesStatus.getPending() == 0){
            setStatus(FirmwareCampaignStatus.COMPLETE);
            this.finishedOn = clock.instant();
            save();
        }
    }

    @SuppressWarnings("unused")
    /** We need this getter for successful event serialization */
    public EndDeviceGroup getDeviceGroup() {
        return deviceGroup;
    }

    public Instant getModTime() {
        return modTime;
    }
}
