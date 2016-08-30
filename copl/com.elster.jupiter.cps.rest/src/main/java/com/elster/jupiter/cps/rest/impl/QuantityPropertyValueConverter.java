package com.elster.jupiter.cps.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.QuantityValueFactory;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.PropertyValueInfo;
import com.elster.jupiter.util.units.Quantity;

import java.util.Map;

/**
 * Created by mbarinov on 19.08.2016.
 */
public class QuantityPropertyValueConverter implements PropertyValueConverter {

    static class QuantityInfo {
        public String id;
        public String displayValue;
    }

    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && propertySpec.getValueFactory() instanceof QuantityValueFactory;
    }

    @Override
    public PropertyType getPropertyType(PropertySpec propertySpec) {
        return PropertyType.QUANTITY;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        if (infoValue != null && infoValue instanceof Map) {
            Map value = (Map) infoValue;
            if (value.get("id") != null) {
                return new QuantityValueFactory().fromStringValue(value.get("id").toString());
            }
        } else if (infoValue instanceof String) {
            return new QuantityValueFactory().fromStringValue((String) infoValue);
        }
        return null;
    }

    @Override
    public PropertyValueInfo convertValueToInfo(PropertySpec propertySpec, Object propertyValue, Object defaultValue) {
        return new PropertyValueInfo<>(createQuantityInfo(propertyValue), createQuantityInfo(defaultValue));
    }

    private QuantityInfo createQuantityInfo(Object domainValue) {
        QuantityInfo quantityInfo = new QuantityInfo();
        if (domainValue != null && domainValue instanceof Quantity) {
            Quantity value = (Quantity) domainValue;
            quantityInfo.id = new QuantityValueFactory().toStringValue(value);
            String[] valueParts = value.toString().split(" ");
            quantityInfo.displayValue = valueParts[1];
        }
        return quantityInfo;
    }

}
