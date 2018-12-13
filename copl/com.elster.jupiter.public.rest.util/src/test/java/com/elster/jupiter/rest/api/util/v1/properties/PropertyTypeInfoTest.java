/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.rest.api.util.v1.properties;

import com.elster.jupiter.properties.BigDecimalFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.ListValueFactory;
import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.RelativePeriodFactory;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ThreeStateFactory;
import com.elster.jupiter.properties.ValueFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonModel;
import com.sun.org.apache.xpath.internal.operations.String;

import java.math.BigDecimal;

import org.junit.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class PropertyTypeInfoTest {

    private static ObjectMapper mapper = new ObjectMapper();

    private JsonModel getJsonModel(Object info) throws Exception {
        return JsonModel.create(mapper.writeValueAsString(info));
    }

    enum BasicPropertyTypes implements PropertyType {
        UNKNOWN(Void.class),
        NUMBER(BigDecimalFactory.class),
        NULLABLE_BOOLEAN(ThreeStateFactory.class),
        BOOLEAN(BooleanFactory.class),
        TEXT(StringFactory.class),
        LISTVALUE(ListValueFactory.class),
        RELATIVEPERIOD(RelativePeriodFactory.class);

        private Class valueFactoryClass;

        BasicPropertyTypes(Class valueFactoryClass) {
            this.valueFactoryClass = valueFactoryClass;
        }

        private boolean matches(ValueFactory valueFactory) {
            return this.valueFactoryClass.isAssignableFrom(valueFactory.getClass());
        }

        public static BasicPropertyTypes getTypeFrom(ValueFactory valueFactory) {
            for (BasicPropertyTypes propertyType : values()) {
                if (propertyType.matches(valueFactory)) {
                    return propertyType;
                }
            }
            return UNKNOWN;
        }
    }

    @Test
    public void onlySimplePropertyTypeTest() throws Exception {
        PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo();
        propertyTypeInfo.simplePropertyType = BasicPropertyTypes.NUMBER;
        JsonModel jsonModel = getJsonModel(propertyTypeInfo);

        assertThat(jsonModel.<String>get("$.simplePropertyType")).isEqualTo("NUMBER");
        assertThat(jsonModel.<Object>get("$.propertyValidationRule")).isNull();
        assertThat(jsonModel.<Object>get("$.predefinedPropertyValuesInfo")).isNull();
        assertThat(jsonModel.<Object>get("$.referenceUri")).isNull();
    }

    @Test
    public void withValidationRulesTest() throws Exception {
        PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo();
        propertyTypeInfo.simplePropertyType = BasicPropertyTypes.NUMBER;
        NumberValidationRules<BigDecimal> bigDecimalNumberValidationRules = new NumberValidationRules<>();
        bigDecimalNumberValidationRules.minimumValue = BigDecimal.valueOf(10);
        bigDecimalNumberValidationRules.maximumValue = BigDecimal.valueOf(99);
        propertyTypeInfo.propertyValidationRule = bigDecimalNumberValidationRules;
        JsonModel jsonModel = getJsonModel(propertyTypeInfo);

        assertThat(jsonModel.<String>get("$.simplePropertyType")).isEqualTo("NUMBER");
        assertThat(jsonModel.<Number>get("$.propertyValidationRule.minimumValue")).isEqualTo(10);
        assertThat(jsonModel.<Number>get("$.propertyValidationRule.maximumValue")).isEqualTo(99);
        assertThat(jsonModel.<Boolean>get("$.propertyValidationRule.isEven")).isNull();
        assertThat(jsonModel.<Boolean>get("$.propertyValidationRule.allowDecimals")).isNull();
        assertThat(jsonModel.<Number>get("$.propertyValidationRule.minimumDigits")).isNull();
        assertThat(jsonModel.<Number>get("$.propertyValidationRule.maximumDigits")).isNull();
        assertThat(jsonModel.<Object>get("$.predefinedPropertyValuesInfo")).isNull();
        assertThat(jsonModel.<Object>get("$.referenceUri")).isNull();
    }

    @Test
    public void withPredefinedValueTest() throws Exception {
        PropertyTypeInfo propertyTypeInfo = new PropertyTypeInfo();
        propertyTypeInfo.simplePropertyType = BasicPropertyTypes.NUMBER;
        PredefinedPropertyValuesInfo<BigDecimal> bigDecimalPredefinedPropertyValuesInfo = new PredefinedPropertyValuesInfo<>();
        bigDecimalPredefinedPropertyValuesInfo.exhaustive = true;
        bigDecimalPredefinedPropertyValuesInfo.selectionMode = PropertySelectionMode.COMBOBOX;
        bigDecimalPredefinedPropertyValuesInfo.possibleValues = new BigDecimal[]{BigDecimal.ONE, BigDecimal.TEN, BigDecimal.valueOf(12345)};
        propertyTypeInfo.predefinedPropertyValuesInfo = bigDecimalPredefinedPropertyValuesInfo;
        JsonModel jsonModel = getJsonModel(propertyTypeInfo);

        assertThat(jsonModel.<String>get("$.simplePropertyType")).isEqualTo("NUMBER");
        assertThat(jsonModel.<Number>get("$.propertyValidationRule")).isNull();
        assertThat(jsonModel.<Boolean>get("$.predefinedPropertyValuesInfo.exhaustive")).isTrue();
        assertThat(jsonModel.<String>get("$.predefinedPropertyValuesInfo.selectionMode")).isEqualTo("COMBOBOX");
        assertThat(jsonModel.<Number>get("$.predefinedPropertyValuesInfo.possibleValues[0]")).isEqualTo(1);
        assertThat(jsonModel.<Number>get("$.predefinedPropertyValuesInfo.possibleValues[1]")).isEqualTo(10);
        assertThat(jsonModel.<Number>get("$.predefinedPropertyValuesInfo.possibleValues[2]")).isEqualTo(12345);
        assertThat(jsonModel.<Object>get("$.referenceUri")).isNull();
    }
}