/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.firmware.FirmwareVersionFilter;
import com.energyict.mdc.pluggable.rest.MdcPropertyUtils;
import com.energyict.mdc.pluggable.rest.PropertyDefaultValuesProvider;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.firmware.BaseFirmwareVersion;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FirmwareMessageInfoFactory {

    static final String PROPERTY_KEY_FIRMWARE_VERSION = "FirmwareDeviceMessage.upgrade.userfile";
    static final String PROPERTY_KEY_IMAGE_IDENTIFIER = "FirmwareDeviceMessage.image.identifier";
    static final String PROPERTY_KEY_RESUME = "FirmwareDeviceMessage.upgrade.resume";

    private final MdcPropertyUtils mdcPropertyUtils;
    private final FirmwareService firmwareService;

    @Inject
    public FirmwareMessageInfoFactory(MdcPropertyUtils mdcPropertyUtils, FirmwareService firmwareService) {
        this.mdcPropertyUtils = mdcPropertyUtils;
        this.firmwareService = firmwareService;
    }

    public Object findPropertyValue(PropertySpec propertySpec, Collection<PropertyInfo> propertyInfos){
        return mdcPropertyUtils.findPropertyValue(propertySpec, propertyInfos);        
    }

    public FirmwareMessageInfo from(DeviceMessageSpec deviceMessageSpec, Device device, String uploadOption, String firmwareType) {
        PropertyDefaultValuesProvider provider = (propertySpec, propertyType) -> {
            if (BaseFirmwareVersion.class.isAssignableFrom(propertySpec.getValueFactory().getValueType())) {
                return firmwareService.getAllUpgradableFirmwareVersionsFor(device, firmwareType != null ? FirmwareTypeFieldAdapter.INSTANCE.unmarshal(firmwareType) : null);
            }
            return null;
        };
        return from(deviceMessageSpec, uploadOption, provider);
    }

    public FirmwareMessageInfo from(DeviceMessageSpec deviceMessageSpec, DeviceType deviceType, String uploadOption, String firmwareType) {
        return from(deviceMessageSpec, uploadOption, firmwareVersionValuesProvider(deviceType, firmwareType));
    }
    
    public List<PropertyInfo> getProperties(DeviceMessageSpec deviceMessageSpec, DeviceType deviceType, String firmwareType, Map<String, Object> propertyValues ){
        TypedProperties typedProperties = TypedProperties.empty();
        if (propertyValues != null){
            for (Map.Entry<String, Object> property : propertyValues.entrySet()) {
                typedProperties.setProperty(property.getKey(), property.getValue());
            }
        }
        
        List<PropertyInfo> properties = mdcPropertyUtils.convertPropertySpecsToPropertyInfos(deviceMessageSpec.getPropertySpecs(), typedProperties, firmwareVersionValuesProvider(deviceType, firmwareType));
        if (typedProperties.size() == 0) {
            properties.stream().filter(y -> y.key.equals(PROPERTY_KEY_IMAGE_IDENTIFIER)).findFirst().ifPresent(x -> x.propertyValueInfo = new PropertyValueInfo<>(null, null, false));
            properties.stream().filter(y -> y.key.equals(PROPERTY_KEY_RESUME)).findFirst().ifPresent(x -> x.propertyValueInfo = new PropertyValueInfo<>(false, false, false));
        }
        return properties;
    }

    private PropertyDefaultValuesProvider firmwareVersionValuesProvider(DeviceType deviceType, String firmwareType){
        return (propertySpec, propertyType) -> {
                    if (BaseFirmwareVersion.class.equals(propertySpec.getValueFactory().getValueType())){
                        FirmwareVersionFilter filter = firmwareService.filterForFirmwareVersion(deviceType);
                        if (firmwareType != null) {
                            filter.addFirmwareTypes(Collections.singletonList(FirmwareTypeFieldAdapter.INSTANCE.unmarshal(firmwareType)));
                        }
                        filter.addFirmwareStatuses(Arrays.asList(FirmwareStatus.FINAL, FirmwareStatus.TEST));
                        return firmwareService.findAllFirmwareVersions(filter).find();
                    }
                    return null;
               };
    }
    
    private FirmwareMessageInfo from(DeviceMessageSpec deviceMessageSpec, String uploadOption, PropertyDefaultValuesProvider provider) {
        FirmwareMessageInfo info = new FirmwareMessageInfo();
        info.uploadOption = uploadOption;
        info.localizedValue = deviceMessageSpec.getName();
        info.setProperties(mdcPropertyUtils.convertPropertySpecsToPropertyInfos(deviceMessageSpec.getPropertySpecs(), TypedProperties.empty(), provider));
        initImageIdentifier(info, null);
        initResumeProperty(info, false);        
        return info;
    }

    void initImageIdentifier(FirmwareMessageInfo info, String imageIdentifier){
        info.getPropertyInfo(PROPERTY_KEY_IMAGE_IDENTIFIER).ifPresent(x -> x.propertyValueInfo = new PropertyValueInfo<>(imageIdentifier, imageIdentifier, imageIdentifier != null));
        info.setPropertyEditable(PROPERTY_KEY_IMAGE_IDENTIFIER, false);
    }

    void initResumeProperty(FirmwareMessageInfo info, boolean doResume){
        info.getPropertyInfo(PROPERTY_KEY_RESUME).ifPresent(x -> x.propertyValueInfo =new PropertyValueInfo<>(doResume, doResume, true));
        info.setPropertyEditable(PROPERTY_KEY_RESUME, false);
    }

}
