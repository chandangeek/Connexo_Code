/*
 * Copyright (c) 2017 by Honeywell Inc. All rights reserved.
 */

package com.elster.jupiter.properties.rest;

import com.elster.jupiter.properties.PropertySelectionMode;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.impl.PropertyValueInfoServiceImpl;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.HasName;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;

import org.assertj.core.data.MapEntry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by bvn on 4/4/17.
 */
@RunWith(MockitoJUnitRunner.class)
public class PropertyValueInfoServiceImplTest {
    @Test
    public void testGetPossibleValues() throws Exception {
        PropertyValueInfoServiceImpl propertyValueInfoService = new PropertyValueInfoServiceImpl();
        propertyValueInfoService.activate();

        ValueFactory valueFactory = mock(ValueFactory.class);
        when(valueFactory.getValueType()).thenReturn(SomeValue.class);
        PropertySpec propertySpec = mock(PropertySpec.class);
        PropertySpecPossibleValues possibleValues = mock(PropertySpecPossibleValues.class);
        when(possibleValues.getAllValues()).thenReturn(Arrays.asList(new SomeValue(1, "First"), new SomeValue(2, "Second")));
        when(possibleValues.getSelectionMode()).thenReturn(PropertySelectionMode.COMBOBOX);
        when(possibleValues.isExhaustive()).thenReturn(true);
        when(propertySpec.getPossibleValues()).thenReturn(possibleValues);
        when(propertySpec.getValueFactory()).thenReturn(valueFactory);


        PropertyInfo propertyInfo = propertyValueInfoService.getPropertyInfo(propertySpec, null);
        assertThat(propertyInfo.propertyTypeInfo.predefinedPropertyValuesInfo.possibleValues).hasSize(2);
        assertThat(propertyInfo.propertyTypeInfo.predefinedPropertyValuesInfo.possibleValues[0] instanceof Map).describedAs("Property should be a MAP").isTrue();
        Map<String, Object> possibleValue1 = (Map<String, Object>) propertyInfo.propertyTypeInfo.predefinedPropertyValuesInfo.possibleValues[0];
        assertThat(possibleValue1).contains(MapEntry.<String, Object>entry("id", 1L));
        assertThat(possibleValue1).contains(MapEntry.<String, Object>entry("name", "First"));
        assertThat(propertyInfo.propertyTypeInfo.predefinedPropertyValuesInfo.possibleValues[1] instanceof Map).describedAs("Property should be a MAP").isTrue();
        Map<String, Object> possibleValue2 = (Map<String, Object>) propertyInfo.propertyTypeInfo.predefinedPropertyValuesInfo.possibleValues[1];
        assertThat(possibleValue2).contains(MapEntry.<String, Object>entry("id", 2L));
        assertThat(possibleValue2).contains(MapEntry.<String, Object>entry("name", "Second"));
    }

    /**
     * Represents some business object implementing both HasName and HasId
     */
    class SomeValue implements HasId, HasName {
        private long id;
        private String name;
        private BigInteger someBusinessValue;

        public SomeValue(long id, String name) {
            this.id = id;
            this.name = name;
        }

        public SomeValue() {
        }

        @Override
        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        @Override
        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public BigInteger getSomeBusinessValue() {
            return someBusinessValue;
        }

        public void setSomeBusinessValue(BigInteger someBusinessValue) {
            this.someBusinessValue = someBusinessValue;
        }
    }
}
