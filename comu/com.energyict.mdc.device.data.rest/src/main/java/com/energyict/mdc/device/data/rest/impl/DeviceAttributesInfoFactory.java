package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.fsm.State;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.LocalizedFieldValidationException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.rest.util.RestValidationBuilder;
import com.elster.jupiter.util.Checks;
import com.energyict.mdc.device.data.CIMLifecycleDates;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.imp.DeviceImportService;
import com.energyict.mdc.device.data.rest.impl.DeviceAttributesInfo.DeviceAttribute;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Optional;

public class DeviceAttributesInfoFactory {
    private final DeviceImportService deviceImportService;
    private final MeteringService meteringService;
    private final Thesaurus thesaurus;

    @Inject
    public DeviceAttributesInfoFactory(DeviceImportService deviceImportService, MeteringService meteringService, Thesaurus thesaurus) {
        this.deviceImportService = deviceImportService;
        this.meteringService = meteringService;
        this.thesaurus = thesaurus;
    }

    public DeviceAttributesInfo from(Device device){
        DeviceAttributesInfo info = new DeviceAttributesInfo();
        info.deviceVersion = device.getVersion();
        State state = device.getState();

        info.mrid = new DeviceAttributeInfo();
        info.mrid.displayValue = device.getmRID();
        fillAvailableAndEditable(info.mrid, DeviceAttribute.MRID, state);

        info.deviceType = new DeviceAttributeInfo();
        info.deviceType.displayValue = device.getDeviceType().getName();
        info.deviceType.attributeId = device.getDeviceType().getId();
        fillAvailableAndEditable(info.deviceType, DeviceAttribute.DEVICE_TYPE, state);

        info.deviceConfiguration = new DeviceAttributeInfo();
        info.deviceConfiguration.displayValue = device.getDeviceConfiguration().getName();
        info.deviceConfiguration.attributeId = device.getDeviceConfiguration().getId();
        fillAvailableAndEditable(info.deviceConfiguration, DeviceAttribute.DEVICE_CONFIGURATION, state);

        info.serialNumber = new DeviceAttributeInfo();
        info.serialNumber.displayValue = device.getSerialNumber();
        fillAvailableAndEditable(info.serialNumber, DeviceAttribute.SERIAL_NUMBER, state);

        info.yearOfCertification = new DeviceAttributeInfo();
        info.yearOfCertification.displayValue = device.getYearOfCertification();
        fillAvailableAndEditable(info.yearOfCertification, DeviceAttribute.YEAR_OF_CERTIFICATION, state);

        info.lifeCycleState = new DeviceAttributeInfo();
        info.lifeCycleState.displayValue = state.getName();
        info.lifeCycleState.attributeId = state.getId();
        fillAvailableAndEditable(info.lifeCycleState, DeviceAttribute.LIFE_CYCLE_STATE, state);

        info.batch = new DeviceAttributeInfo();
        deviceImportService.findBatch(device.getId()).ifPresent(batch -> {
            info.batch.attributeId = batch.getId();
            info.batch.displayValue = batch.getName();
        });
        fillAvailableAndEditable(info.batch, DeviceAttribute.BATCH, state);

        info.usagePoint = new DeviceAttributeInfo();
        device.getUsagePoint().ifPresent(usagePoint -> {
            info.usagePoint.displayValue = usagePoint.getName();
            info.usagePoint.attributeId = usagePoint.getId();
        });
        fillAvailableAndEditable(info.usagePoint, DeviceAttribute.USAGE_POINT, state);

        CIMLifecycleDates lifecycleDates = device.getLifecycleDates();
        info.shipmentDate = new DeviceAttributeInfo();
        info.shipmentDate.displayValue = lifecycleDates.getReceivedDate().orElse(null);
        fillAvailableAndEditable(info.shipmentDate, DeviceAttribute.SHIPMENT_DATE, state);

        info.installationDate = new DeviceAttributeInfo();
        info.installationDate.displayValue = lifecycleDates.getInstalledDate().orElse(null);
        fillAvailableAndEditable(info.installationDate, DeviceAttribute.INSTALLATION_DATE, state);

        info.deactivationDate = new DeviceAttributeInfo();
        info.deactivationDate.displayValue = lifecycleDates.getRemovedDate().orElse(null);
        fillAvailableAndEditable(info.deactivationDate, DeviceAttribute.DEACTIVATION_DATE, state);

        info.decommissioningDate = new DeviceAttributeInfo();
        info.decommissioningDate.displayValue = lifecycleDates.getRetiredDate().orElse(null);
        fillAvailableAndEditable(info.decommissioningDate, DeviceAttribute.DECOMMISSIONING_DATE, state);

        return info;
    }

    private void fillAvailableAndEditable(DeviceAttributeInfo attribute, DeviceAttribute mapping, State state){
        attribute.available = mapping.isAvailableForState(state);
        attribute.editable = mapping.isEditableForState(state);
    }

    public void validateOn(Device device, DeviceAttributesInfo info){
        State currentState = device.getState();
        RestValidationBuilder validationBuilder = new RestValidationBuilder();
        if (DeviceAttributesInfo.DeviceAttribute.SHIPMENT_DATE.isEditableForState(currentState)){
            validationBuilder.notEmpty(info.shipmentDate.displayValue, "shipmentDate");
        }
        if (DeviceAttributesInfo.DeviceAttribute.INSTALLATION_DATE.isEditableForState(currentState)){
            validateDate(validationBuilder, "installationDate", info.getInstallationDate(), info.getShipmentDate(), DefaultTranslationKey.CIM_DATE_RECEIVE);
        }
        if (DeviceAttributesInfo.DeviceAttribute.DEACTIVATION_DATE.isEditableForState(currentState)){
            validateDate(validationBuilder, "deactivationDate", info.getDeactivationDate(), info.getInstallationDate(), DefaultTranslationKey.CIM_DATE_INSTALLED);
        }
        if (DeviceAttributesInfo.DeviceAttribute.DECOMMISSIONING_DATE.isEditableForState(currentState)){
            validateDate(validationBuilder, "decommissioningDate", info.getDecommissioningDate(), info.getDeactivationDate(), DefaultTranslationKey.CIM_DATE_REMOVE);
        }
        validationBuilder.validate();
    }

    private void validateDate(RestValidationBuilder validationBuilder, String fieldName, Optional<Instant> currentDate, Optional<Instant> previousDate, DefaultTranslationKey cimDateTranslation) {
        if (!currentDate.isPresent()) {
            validationBuilder.addValidationError(new LocalizedFieldValidationException(MessageSeeds.THIS_FIELD_IS_REQUIRED, fieldName));
        } else {
            if (previousDate.isPresent() && currentDate.get().isBefore(previousDate.get())){
                validationBuilder.addValidationError(new LocalizedFieldValidationException(MessageSeeds.CIM_DATE_SHOULD_BE_AFTER_X, fieldName,
                        thesaurus.getString(cimDateTranslation.getKey(), cimDateTranslation.getDefaultFormat())));
            }
        }
    }

    public void writeTo(Device device, DeviceAttributesInfo info){
        State state = device.getState();
        if (DeviceAttribute.SERIAL_NUMBER.isEditableForState(state) && info.serialNumber != null) {
            device.setSerialNumber(info.serialNumber.displayValue);
        }
        if (DeviceAttribute.YEAR_OF_CERTIFICATION.isEditableForState(state) && info.yearOfCertification != null){
            device.setYearOfCertification(info.yearOfCertification.displayValue);
        }
        if(DeviceAttribute.BATCH.isEditableForState(state) && info.batch != null){
            if (Checks.is(info.batch.displayValue).emptyOrOnlyWhiteSpace()) {
                deviceImportService.findBatch(device.getId()).ifPresent(batch -> {
                    batch.removeDevice(device);
                });
            }
            this.deviceImportService.addDeviceToBatch(device, info.batch.displayValue);
        }
        if(DeviceAttribute.USAGE_POINT.isEditableForState(state) && info.usagePoint != null){
            meteringService.findUsagePoint(info.usagePoint.attributeId).ifPresent(usagePoint -> {

            });
        }
        CIMLifecycleDates lifecycleDates = device.getLifecycleDates();
        if(DeviceAttribute.SHIPMENT_DATE.isEditableForState(state) && info.shipmentDate != null){
            lifecycleDates.setReceivedDate(info.shipmentDate.displayValue);
        }
        if(DeviceAttribute.INSTALLATION_DATE.isEditableForState(state) && info.installationDate != null){
            lifecycleDates.setInstalledDate(info.installationDate.displayValue);
        }
        if(DeviceAttribute.DEACTIVATION_DATE.isEditableForState(state) && info.deactivationDate != null){
            lifecycleDates.setRemovedDate(info.deactivationDate.displayValue);
        }
        if(DeviceAttribute.DECOMMISSIONING_DATE.isEditableForState(state) && info.decommissioningDate != null){
            lifecycleDates.setRetiredDate(info.decommissioningDate.displayValue);
        }
        lifecycleDates.save();
        device.save();
    }
}
