/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit.deviceProtocol;

import com.elster.jupiter.audit.AbstractAuditDecoder;
import com.elster.jupiter.audit.AuditDomainContextType;
import com.elster.jupiter.audit.AuditLogChange;
import com.elster.jupiter.audit.AuditLogChangeBuilder;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataMapper;
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
import com.elster.jupiter.util.Pair;
import com.elster.jupiter.util.conditions.Operator;
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

public class AuditTrailDeviceProtocolDecoder extends AbstractAuditDecoder {

    private volatile OrmService ormService;
    private volatile PropertyValueInfoService propertyValueInfoService;
    private volatile MeteringService meteringService;
    private volatile DeviceService deviceService;

    private Long deviceId;
    private Optional<Device> device;

    AuditTrailDeviceProtocolDecoder(OrmService ormService, MeteringService meteringService, DeviceService deviceService, PropertyValueInfoService propertyValueInfoService) {
        this.ormService = ormService;
        this.propertyValueInfoService = propertyValueInfoService;
        this.meteringService = meteringService;
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
        return UnexpectedNumberOfUpdatesException.Operation.UPDATE;
    }

    @Override
    protected void decodeReference() {
        meteringService.findEndDeviceById(getAuditTrailReference().getPkcolumn())
                .ifPresent(endDevice -> {
                    deviceId = Long.parseLong(endDevice.getAmrId());
                    device = deviceService.findDeviceById(deviceId);
                });
    }

    @Override
    public List<AuditLogChange> getAuditLogChanges() {
        try {
            List<AuditLogChange> auditLogChanges = new ArrayList<>();

            List<PropertySpec> deviceProtocolPropertySpecs = getDeviceProtocolPropertySpecs();
            DataModel dataModel = ormService.getDataModel(DeviceDataServices.COMPONENT_NAME).get();
            DataMapper<DeviceProtocolProperty> dataMapper = dataModel.mapper(DeviceProtocolProperty.class);

            List<DeviceProtocolProperty> actualEntries = getActualEntries(dataMapper, getActualClauses());
            List<DeviceProtocolProperty> historyByModTimeEntries = getHistoryEntries(dataMapper, getHistoryByModTimeClauses());
            List<DeviceProtocolProperty> historyByJournalTimeEntries = getHistoryEntries(dataMapper, getHistoryByJournalClauses());

            for (PropertySpec propertySpec : deviceProtocolPropertySpecs) {
                Optional<DeviceProtocolProperty> inActualEntries = actualEntries.stream().filter(e -> e.getName().compareToIgnoreCase(propertySpec.getName()) == 0).findFirst();
                Optional<DeviceProtocolProperty> inHistoryByModTimeEntries = historyByModTimeEntries.stream().filter(e -> e.getName().compareToIgnoreCase(propertySpec.getName()) == 0).findFirst();
                Optional<DeviceProtocolProperty> inHistoryByJournalTimeEntries = historyByJournalTimeEntries.stream()
                        .filter(e -> e.getName().compareToIgnoreCase(propertySpec.getName()) == 0)
                        .findFirst();

                if (inActualEntries.isPresent()) {
                    getAuditLogChange(deviceProtocolPropertySpecs, inHistoryByJournalTimeEntries, inActualEntries).ifPresent(auditLogChanges::add);
                } else if (inHistoryByModTimeEntries.isPresent() && inHistoryByJournalTimeEntries.isPresent()) {
                    getAuditLogChange(deviceProtocolPropertySpecs, inHistoryByJournalTimeEntries, inHistoryByModTimeEntries).ifPresent(auditLogChanges::add);
                } else if (inHistoryByModTimeEntries.isPresent()) {
                    getAuditLogChange(deviceProtocolPropertySpecs, Optional.empty(), inHistoryByModTimeEntries).ifPresent(auditLogChanges::add);
                } else if (inHistoryByJournalTimeEntries.isPresent()) {
                    getAuditLogChange(deviceProtocolPropertySpecs, inHistoryByJournalTimeEntries, Optional.empty()).ifPresent(auditLogChanges::add);
                }
            }
            return auditLogChanges;
        } catch (Exception e) {
        }
        return Collections.emptyList();
    }

    private Map<String, Object> getActualClauses() {
        return ImmutableMap.of("DEVICEID", deviceId);
    }

    private Map<Operator, Pair<String, Object>> getHistoryByModTimeClauses() {
        return ImmutableMap.of(Operator.EQUAL, Pair.of("DEVICEID", deviceId),
                Operator.GREATERTHANOREQUAL, Pair.of("modTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("modTime", getAuditTrailReference().getModTimeEnd()));
    }

    private Map<Operator, Pair<String, Object>> getHistoryByJournalClauses() {
        return ImmutableMap.of(Operator.EQUAL, Pair.of("DEVICEID", deviceId),
                Operator.GREATERTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeStart()),
                Operator.LESSTHANOREQUAL, Pair.of("journalTime", getAuditTrailReference().getModTimeEnd()));
    }

    private Optional<AuditLogChange> getAuditLogChange(List<PropertySpec> deviceProtocolPropertySpecs, Optional<DeviceProtocolProperty> oldDeviceProtocolProperty, Optional<DeviceProtocolProperty> newDeviceProtocolProperty) {

        String propertyName = Optional.of(newDeviceProtocolProperty.orElseGet(() -> oldDeviceProtocolProperty.get()))
                .map(dpp -> dpp.getName())
                .orElseGet(String::new);
        String displayPropertyName = deviceProtocolPropertySpecs.stream()
                .filter(ps -> ps.getName().compareToIgnoreCase(propertyName) == 0)
                .map(PropertySpec::getDisplayName).findFirst().orElse(propertyName);
        PropertySpec propertySpec = Optional.of(newDeviceProtocolProperty.orElseGet(() -> oldDeviceProtocolProperty.get()))
                .map(dpp -> ((DeviceProtocolPropertyImpl) dpp)
                        .getPropertySpec())
                .orElseThrow(() -> new IllegalStateException("Cannot found propertySpec"));

        PropertyValueConverter propertyValueConverter = propertyValueInfoService.getConverter(propertySpec);
        PropertyType propertyType = propertyValueConverter.getPropertyType(propertySpec);
        String propertyTypeName = propertyValueConverter.getPropertyType(propertySpec).toString();

        if (!oldDeviceProtocolProperty.isPresent() && getValueFromDeviceProtocolProperty(newDeviceProtocolProperty.get()).equals(getDefaultValue(propertySpec))) {
            return Optional.empty();
        } else if (!newDeviceProtocolProperty.isPresent() && getValueFromDeviceProtocolProperty(oldDeviceProtocolProperty.get()).equals(getDefaultValue(propertySpec))) {
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

        AuditLogChange auditLogChanges = new AuditLogChangeBuilder()
                .setName(displayPropertyName)
                .setValue(newPropertyValue)
                .setPreviousValue(oldPropertyValue)
                .setType(propertyTypeName);
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