/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit;

import com.elster.jupiter.audit.AbstractAuditDecoder;
import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditLogChanges;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.PropertyType;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.properties.rest.SimplePropertyType;
import com.elster.jupiter.time.TimeDuration;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceProtocolProperty;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.DeviceProtocolPropertyImpl;

import com.google.common.collect.ImmutableMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AuditTrailDeviceProtocolDecoder extends AbstractAuditDecoder {

    private volatile OrmService ormService;
    private volatile PropertyValueInfoService propertyValueInfoService;
    private volatile DeviceService deviceService;

    private Long deviceId;
    private Optional<Device> device;

    AuditTrailDeviceProtocolDecoder(OrmService ormService, DeviceService deviceService, PropertyValueInfoService propertyValueInfoService) {
        this.ormService = ormService;
        this.propertyValueInfoService = propertyValueInfoService;
        this.deviceService = deviceService;
    }

    @Override
    public String getName() {
        return device
                .map(Device::getName)
                .orElseThrow(() -> new IllegalArgumentException("Device cannot be found"));
    }

    @Override
    public UnexpectedNumberOfUpdatesException.Operation getOperation(UnexpectedNumberOfUpdatesException.Operation operation, AuditDomainContextType context) {
        if (operation.equals(UnexpectedNumberOfUpdatesException.Operation.INSERT)) {
            return UnexpectedNumberOfUpdatesException.Operation.UPDATE;
        }
        return operation;
    }

    @Override
    protected void decodeReference() {
        deviceId = getAuditTrailReference().getPkcolumn();
        device = deviceService.findDeviceById(deviceId);
    }

    @Override
    public List<AuditLogChanges> getAuditLogChanges() {
        try {
            List<AuditLogChanges> auditLogChanges = new ArrayList<>();

            List<PropertySpec> deviceProtocolPropertySpecs = getDeviceProtocolPropertySpecs();
            DataModel dataModel = ormService.getDataModel(DeviceDataServices.COMPONENT_NAME).get();
            Map<String, List<DeviceProtocolProperty>> affectedDeviceProtocolProperties =
                    getAffected(dataModel.mapper(DeviceProtocolProperty.class), ImmutableMap.of("DEVICEID", deviceId)).stream()
                            .collect(Collectors.groupingBy(dpp -> dpp.getName()));

            for (Map.Entry<String, List<DeviceProtocolProperty>> entry : affectedDeviceProtocolProperties.entrySet()) {
                Optional<DeviceProtocolProperty> newDeviceProtocolProperty = entry.getValue().stream().findFirst();
                Optional<DeviceProtocolProperty> oldDeviceProtocolProperty = entry.getValue().stream()
                        .skip(1).findFirst().map(Optional::of)
                        .orElseGet(() -> getJournalEntry(dataModel.mapper(DeviceProtocolProperty.class),
                                ImmutableMap.of("DEVICEID", deviceId,
                                        "PROPERTYSPEC", newDeviceProtocolProperty.get().getName(),
                                        "VERSIONCOUNT", newDeviceProtocolProperty.get()
                                                .getVesion() - 1)));//getDeviceProtocolPropertyFromHistory(newDeviceProtocolProperty.get().getVesion() - 1, newDeviceProtocolProperty.get().getName()));
                getAuditLogChange(deviceProtocolPropertySpecs, oldDeviceProtocolProperty, newDeviceProtocolProperty).ifPresent(auditLogChanges::add);
            }
            return auditLogChanges;
        } catch (Exception e) {
        }
        return Collections.emptyList();
    }

    private Optional<AuditLogChangesImpl> getAuditLogChange(List<PropertySpec> deviceProtocolPropertySpecs, Optional<DeviceProtocolProperty> oldDeviceProtocolProperty, Optional<DeviceProtocolProperty> newDeviceProtocolProperty) {

        String propertyName = Optional.of(newDeviceProtocolProperty)
                .map(dpp -> dpp.get().getName())
                .orElseGet(String::new);
        String displayPropertyName = deviceProtocolPropertySpecs.stream()
                .filter(ps -> ps.getName().compareToIgnoreCase(propertyName) == 0)
                .map(PropertySpec::getDisplayName).findFirst().orElse(propertyName);
        PropertySpec propertySpec = newDeviceProtocolProperty
                .map(dpp -> ((DeviceProtocolPropertyImpl) dpp)
                        .getPropertySpec())
                .orElseThrow(() -> new IllegalStateException("Cannot found propertySpec"));

        PropertyValueConverter propertyValueConverter = propertyValueInfoService.getConverter(propertySpec);
        PropertyType propertyType = propertyValueConverter.getPropertyType(propertySpec);
        String propertyTypeName = propertyValueConverter.getPropertyType(propertySpec).toString();

        if (!oldDeviceProtocolProperty.isPresent() && getValueFromDeviceProtocolProperty(newDeviceProtocolProperty.get()).equals(getDefaultValue(propertySpec))) {
            return Optional.empty();
        }

        Object newPropertyValue = newDeviceProtocolProperty
                .map(dpp -> getValueFromDeviceProtocolProperty(dpp))
                .map(value -> encodeValue(propertyType, propertyValueConverter, propertySpec, value))
                .orElseGet(String::new);

        Object oldPropertyValue = oldDeviceProtocolProperty
                .map(dpp -> getValueFromDeviceProtocolProperty(dpp))
                .map(value -> encodeValue(propertyType, propertyValueConverter, propertySpec, value))
                .orElseGet(() -> encodeValue(propertyType, propertyValueConverter, propertySpec, getDefaultValue(propertySpec)));

        AuditLogChangesImpl auditLogChanges = new AuditLogChangesImpl();
        auditLogChanges.setName(displayPropertyName);
        auditLogChanges.setValue(newPropertyValue);
        auditLogChanges.setPreviousValue(oldPropertyValue);
        auditLogChanges.setType(propertyTypeName);
        return Optional.of(auditLogChanges);
    }

    private Object getValueFromDeviceProtocolProperty(DeviceProtocolProperty deviceProtocolProperty) {
        return getValueFromDeviceProtocolProperty(deviceProtocolProperty.getPropertyValue(), ((DeviceProtocolPropertyImpl) deviceProtocolProperty).getPropertySpec());
    }

    private Object getValueFromDeviceProtocolProperty(Object value, PropertySpec propertySpec) {
        Object propertyValue;
        ValueFactory valueFactory = propertySpec.getValueFactory();
        if (valueFactory instanceof BigDecimalFactory || valueFactory instanceof BooleanFactory) {
            propertyValue = (Integer.parseInt(value.toString()));
        } else {
            propertyValue = propertySpec.getValueFactory().valueFromDatabase(value);
        }
        return propertyValue;
    }

    private Object encodeValue(PropertyType propertyType, PropertyValueConverter propertyValueConverter, PropertySpec propertySpec, Object propertyValue) {
        try {
            if (propertyType == SimplePropertyType.DURATION) {
                return ((TimeDuration) propertyValue).getTimeUnit().name() + ":" + ((TimeDuration) propertyValue).getCount();
            } else if (propertyType == SimplePropertyType.IDWITHNAME) {
                return ((HashMap) propertyValueConverter.convertValueToInfo(propertySpec, propertyValue)).get("name").toString();
            }
            return propertyValue;
        } catch (Exception e) {

        }

        return "";
    }

    private Object getDefaultValue(PropertySpec propertySpec) {
        ValueFactory valueFactory = propertySpec.getValueFactory();
        PropertySpecPossibleValues propertySpecPossibleValues = propertySpec.getPossibleValues();
        if ((valueFactory instanceof BooleanFactory) && (propertySpecPossibleValues == null)) {
            return (Integer.parseInt("0"));
        }
        if (valueFactory instanceof BigDecimalFactory || valueFactory instanceof BooleanFactory) {
            return (Integer.parseInt(propertySpec.getValueFactory().valueToDatabase(propertySpec.getPossibleValues().getDefault()).toString()));
        }
        if (propertySpec.getPossibleValues() == null) {
            return "";
        }
        return propertySpec.getPossibleValues().getDefault();
    }

    private List<PropertySpec> getDeviceProtocolPropertySpecs() {
        return deviceService.findDeviceById(deviceId).map(device ->
                device.getDeviceType().getDeviceProtocolPluggableClass().map(deviceProtocolPluggableClass ->
                        deviceProtocolPluggableClass.getDeviceProtocol().getPropertySpecs()).orElse(Collections.emptyList()))
                .orElse(Collections.emptyList());
    }
}