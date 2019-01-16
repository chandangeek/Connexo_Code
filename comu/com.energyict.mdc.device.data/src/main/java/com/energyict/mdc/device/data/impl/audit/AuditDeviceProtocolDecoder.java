/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.audit;

import com.elster.jupiter.audit.AuditDecoder;
import com.elster.jupiter.audit.AuditLogChanges;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.JournalEntry;
import com.elster.jupiter.orm.OrmService;
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
import com.elster.jupiter.util.conditions.Operator;
import com.energyict.mdc.device.data.DeviceDataServices;
import com.energyict.mdc.device.data.DeviceProtocolProperty;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.impl.DeviceProtocolPropertyImpl;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class AuditDeviceProtocolDecoder implements AuditDecoder {

    private volatile OrmService ormService;
    private volatile PropertyValueInfoService propertyValueInfoService;
    private volatile DeviceService deviceService;
    private String reference;
    private Long deviceId;
    private String propertyName;
    private Long versionCount;

    AuditDeviceProtocolDecoder(OrmService ormService, DeviceService deviceService, PropertyValueInfoService propertyValueInfoService) {
        this.ormService = ormService;
        this.propertyValueInfoService = propertyValueInfoService;
        this.deviceService = deviceService;
    }

    public AuditDeviceProtocolDecoder init(String reference) {
        this.reference = reference;
        decodeReference();
        return this;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public Object getReference() {
        return new Object();
    }

    @Override
    public List<AuditLogChanges> getAuditLogChanges() {
        try {
            List<PropertySpec> deviceProtocolPropertySpecs =
                    deviceService.findDeviceById(deviceId).map(device ->
                            device.getDeviceType().getDeviceProtocolPluggableClass().map(deviceProtocolPluggableClass ->
                                    deviceProtocolPluggableClass.getDeviceProtocol().getPropertySpecs()).orElse(Collections.emptyList()))
                            .orElse(Collections.emptyList());

            // new value
            Optional<DeviceProtocolProperty> newDeviceProtocolProperty = getDeviceProtocolPropertyByVersion(versionCount)
                    .map(Optional::of)
                    .orElse(getDeviceProtocolProperty(versionCount));

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

            // old value
            Optional<DeviceProtocolProperty> oldDeviceProtocolProperty = Optional.empty();
            if (isNotFirstVersion(versionCount)) {
                oldDeviceProtocolProperty = getDeviceProtocolPropertyByVersion(versionCount - 1);
            } else if (getValueFromDeviceProtocolProperty(newDeviceProtocolProperty.get()).equals(getDefaultValue(propertySpec))) {
                return Collections.emptyList();
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

            return Collections.singletonList(auditLogChanges);
        } catch (Exception e) {
        }
        return Collections.emptyList();
    }

    private static boolean isNotFirstVersion(Long versionCount) {
        return versionCount != 1;
    }

    private boolean decodeReference() {
        try {
            JSONObject jsonData = new JSONObject(reference);
            deviceId = ((Number) jsonData.get("DEVICEID")).longValue();
            propertyName = jsonData.get("PROPERTYSPEC").toString();
            versionCount = ((Number) jsonData.get("VERSIONCOUNT")).longValue();
        } catch (JSONException e) {
            return false;
        }
        return true;
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

    private Optional<DeviceProtocolProperty> getDeviceProtocolPropertyByVersion(long version) {
        DataModel dataModel = ormService.getDataModel(DeviceDataServices.COMPONENT_NAME).get();
        return dataModel.mapper(DeviceProtocolProperty.class)
                .at(Instant.EPOCH)
                .find(Arrays.asList(Operator.EQUAL.compare("DEVICEID", deviceId),
                        Operator.EQUAL.compare("PROPERTYSPEC", propertyName),
                        Operator.EQUAL.compare("VERSIONCOUNT", version)))
                .stream()
                .min(Comparator.comparing(JournalEntry::getJournalTime))
                .map(JournalEntry::get);
    }

    private Optional<DeviceProtocolProperty> getDeviceProtocolProperty(long version) {
        DataModel dataModel = ormService.getDataModel(DeviceDataServices.COMPONENT_NAME).get();
        return dataModel.mapper(DeviceProtocolProperty.class)
                .getUnique(new String[]{"DEVICEID", "propertyName", "version"}, new Object[]{deviceId, propertyName, version});
    }

    private Object encodeValue(PropertyType propertyType, PropertyValueConverter propertyValueConverter, PropertySpec propertySpec, Object propertyValue) {
        if (propertyType == SimplePropertyType.DURATION) {
            return ((TimeDuration) propertyValue).getTimeUnit().name() + ":" + ((TimeDuration) propertyValue).getCount();
        } else if (propertyType == SimplePropertyType.IDWITHNAME) {
            return ((HashMap) propertyValueConverter.convertValueToInfo(propertySpec, propertyValue)).get("name").toString();
        }
        return propertyValue;
    }

    private Object getDefaultValue(PropertySpec propertySpec) {
        ValueFactory valueFactory = propertySpec.getValueFactory();
        PropertySpecPossibleValues propertySpecPossibleValues = propertySpec.getPossibleValues();
        if ((valueFactory instanceof BooleanFactory) && (propertySpecPossibleValues == null)) {
            return (Integer.parseInt("0"));
        }
        if (valueFactory instanceof BigDecimalFactory || valueFactory instanceof BooleanFactory) {
            return (Integer.parseInt(propertySpec.getPossibleValues().getDefault().toString()));
        }
        return propertySpec.getPossibleValues().getDefault();
    }

}
