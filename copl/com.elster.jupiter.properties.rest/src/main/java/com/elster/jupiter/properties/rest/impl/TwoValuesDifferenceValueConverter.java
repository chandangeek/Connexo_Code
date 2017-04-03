/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties.rest.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.TwoValuesAbsoluteDifference;
import com.elster.jupiter.properties.TwoValuesDifference;
import com.elster.jupiter.properties.TwoValuesDifferenceValueFactory;
import com.elster.jupiter.properties.TwoValuesPercentDifference;
import com.elster.jupiter.properties.rest.PropertyType;
import com.elster.jupiter.properties.rest.PropertyValueConverter;
import com.elster.jupiter.properties.rest.SimplePropertyType;

import java.math.BigDecimal;
import java.util.DoubleSummaryStatistics;
import java.util.Map;
import java.util.Objects;

/**
 * Created by dantonov on 29.03.2017.
 */
public class TwoValuesDifferenceValueConverter implements PropertyValueConverter {
    @Override
    public boolean canProcess(PropertySpec propertySpec) {
        return propertySpec != null && propertySpec.getValueFactory() instanceof TwoValuesDifferenceValueFactory;
    }

    @Override
    public PropertyType getPropertyType(PropertySpec propertySpec) {
        return SimplePropertyType.TWO_VALUES_DIFFERENCE;
    }

    @Override
    public Object convertInfoToValue(PropertySpec propertySpec, Object infoValue) {
        if (infoValue instanceof Map<?,?>){
           String type = (String)((Map) infoValue).get("type");
           Object valueObj = ((Map) infoValue).get("value");
           Double value = null;
           if (valueObj instanceof Integer) {
               value = ((Integer) valueObj).doubleValue();
           } else if (valueObj instanceof Double){
               value = (Double)valueObj;
           }
           if (type.equals(TwoValuesAbsoluteDifference.Type.absolute.name())) {
               TwoValuesAbsoluteDifference twoValuesAbsoluteDifference = new TwoValuesAbsoluteDifference();
               twoValuesAbsoluteDifference.value = BigDecimal.valueOf(value);
               return twoValuesAbsoluteDifference;
           } else if (type.equals(TwoValuesAbsoluteDifference.Type.percent.name())) {
               TwoValuesPercentDifference twoValuesPercentDifference = new TwoValuesPercentDifference();
               twoValuesPercentDifference.percent = value;
               return twoValuesPercentDifference;
           }
           return null;
        }else {
            return null;
        }
    }

    @Override
    public Object convertValueToInfo(PropertySpec propertySpec, Object domainValue) {
        if(domainValue!=null){
            if (domainValue instanceof TwoValuesAbsoluteDifference) {
                return domainValue;
            } else if (domainValue instanceof TwoValuesPercentDifference){
                return domainValue;
            }
        }
        return null;
    }
}
