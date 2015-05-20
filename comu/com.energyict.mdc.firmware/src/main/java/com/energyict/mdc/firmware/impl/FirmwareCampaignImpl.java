package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.QueryEndDeviceGroup;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.util.conditions.Condition;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.firmware.DeviceInFirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignProperty;
import com.energyict.mdc.firmware.FirmwareCampaignStatus;
import com.energyict.mdc.firmware.FirmwareManagementDeviceStatus;
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
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@UniqueName(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.NAME_MUST_BE_UNIQUE + "}")
public class FirmwareCampaignImpl implements FirmwareCampaign, HasUniqueName {

    public enum Fields {
        ID ("id"),
        NAME ("name"),
        STATUS ("status"),
        DEVICE_TYPE ("deviceType"),
        DEVICE_GROUP ("deviceGroup"),
        UPGRADE_OPTION ("upgradeOption"),
        FIRMWARE_TYPE ("firmwareType"),
        PLANNED_DATE ("plannedDate"),
        STARTED_ON ("startedOn"),
        FINISHED_ON ("finishedOn"),
        DEVICES ("devices"),
        PROPERTIES ("properties"),
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
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<EndDeviceGroup> deviceGroup = ValueReference.absent();
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private ProtocolSupportedFirmwareOptions upgradeOption;
    @NotNull(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private FirmwareType firmwareType;
    private Instant plannedDate;
    private Instant startedOn;
    private Instant finishedOn;
    @Valid
    private List<DeviceInFirmwareCampaign> devices = new ArrayList<>();
    @Valid
    private List<FirmwareCampaignProperty> properties = new ArrayList<>();

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
    private final DeviceService deviceService;
    private final DeviceMessageSpecificationService deviceMessageSpecificationService;

    @Inject
    public FirmwareCampaignImpl(DataModel dataModel, Clock clock, FirmwareService firmwareService, DeviceService deviceService, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        this.dataModel = dataModel;
        this.clock = clock;
        this.firmwareService = firmwareService;
        this.deviceService = deviceService;
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
    }

    FirmwareCampaign init(DeviceType deviceType, EndDeviceGroup group) {
        this.status = FirmwareCampaignStatus.NOT_STARTED;
        this.deviceType.set(deviceType);
        this.deviceGroup.set(group);
        return this;
    }

    public void cloneDeviceList(){
        List<Device> devices = Collections.emptyList();
        EndDeviceGroup group = this.deviceGroup.get();
        if (group instanceof QueryEndDeviceGroup){
            Condition deviceQuery = ((QueryEndDeviceGroup) group).getCondition();
            deviceQuery = deviceQuery.and(where("deviceConfiguration.deviceType").isEqualTo(deviceType.get()));
            devices = deviceService.findAllDevices(deviceQuery).find();
        } else {
            devices = group.getMembers(this.clock.instant())
                    .stream()
                    .map(endDevice -> deviceService.findDeviceById(Long.parseLong(endDevice.getAmrId())))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .filter(device -> device.getDeviceConfiguration().getDeviceType().getId() == deviceType.get().getId())
                    .collect(Collectors.toList());
        }
        devices.stream()
                .forEach(device -> {
                    FirmwareCampaignImpl.this.devices.add(dataModel.getInstance(DeviceInFirmwareCampaignImpl.class)
                            .init(FirmwareCampaignImpl.this, device));
                });
        setStatus(FirmwareCampaignStatus.SCHEDULED);
        save();
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
        this.name = name;
    }

    @Override
    public FirmwareCampaignStatus getStatus() {
        return status;
    }

    void setStatus(FirmwareCampaignStatus status){
        if (!this.status.isValidStatusTransition(status)){
            // TODO throw exception
        }
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
        return upgradeOption;
    }

    @Override
    public void setUpgradeOption(ProtocolSupportedFirmwareOptions upgradeOption) {
        if (!FirmwareCampaignStatus.NOT_STARTED.equals(this.status)
                && !FirmwareCampaignStatus.SCHEDULED.equals(this.status)){
            // TODO throw exception (upgrade already in progress)
        }
        this.upgradeOption = upgradeOption;
    }

    @Override
    public Instant getPlannedDate() {
        return plannedDate;
    }

    @Override
    public void setPlannedDate(Instant plannedDate) {
        this.plannedDate = plannedDate;
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
        }
    }

    @Override
    public void delete(){
        dataModel.remove(this);
    }

    @Override
    public void start(){
        this.startedOn = clock.instant();
        setStatus(FirmwareCampaignStatus.ONGOING);
        this.devices.stream().forEach(DeviceInFirmwareCampaign::startFirmwareProcess);
        save();
    }

    public void updateStatus(){
        List<FirmwareManagementDeviceStatus> deviceStatusList = this.devices.stream().map(DeviceInFirmwareCampaign::updateStatus).collect(Collectors.toList());
        calculateStatusBasedOnDeviceStatuses(deviceStatusList);
        save();
    }

    private void calculateStatusBasedOnDeviceStatuses(List<FirmwareManagementDeviceStatus> statuses){

    }
}
