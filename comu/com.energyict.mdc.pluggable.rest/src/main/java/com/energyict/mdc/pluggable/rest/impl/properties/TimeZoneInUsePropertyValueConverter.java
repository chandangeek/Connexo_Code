package com.energyict.mdc.pluggable.rest.impl.properties;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.TimeZoneFactory;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.energyict.mdc.pluggable.rest.impl.TimeZoneInUseInfo;
import com.energyict.mdc.protocol.api.timezones.TimeZoneInUse;

/**
 * Created by mbarinov on 31.08.2016.
 */
public class TimeZoneInUsePropertyValueConverter implements PropertyValueConverter {

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && propertySpec.getValueFactory() instanceof TimeZoneFactory;
    }

    @Override
    public SimplePropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.TIMEZONEINUSE;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        return propertySpec.getValueFactory().fromStringValue(infoValue.toString());
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        return new TimeZoneInUseInfo((TimeZoneInUse) domainValue);
    }

}
