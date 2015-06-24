package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StandardCsvDataFormatterFactoryTest {


    @Test
    public void testGetProperties() throws Exception {
        StandardCsvDataFormatterFactory factory = new StandardCsvDataFormatterFactory();
        factory.setPropertySpecService(new PropertySpecServiceImpl());

        List<PropertySpec> properties = factory.getPropertySpecs();
        assertThat(properties).hasSize(3);

        // Order IS important
        PropertySpec property = properties.get(0);
        assertThat(property.getName()).isEqualTo("formatterProperties.separator");
        assertThat(property.isRequired()).isTrue();
        assertThat(property.getValueFactory()).isInstanceOf(StringFactory.class);
        assertThat(property.getPossibleValues().isExhaustive()).isTrue();
        List<String> allValues = (List<String>) property.getPossibleValues().getAllValues();
        assertThat(allValues).hasSize(2);
        assertThat(allValues).containsExactly("comma", "semicolon");
        property = properties.get(1);
        assertThat(property.getName()).isEqualTo("formatterProperties.tag");
        assertThat(property.isRequired()).isTrue();
        assertThat(property.getValueFactory()).isInstanceOf(StringFactory.class);
        assertThat(property.getPossibleValues().isExhaustive()).isFalse();
        property = properties.get(2);
        assertThat(property.getName()).isEqualTo("formatterProperties.update.tag");
        assertThat(property.isRequired()).isTrue();
        assertThat(property.getValueFactory()).isInstanceOf(StringFactory.class);
        assertThat(property.getPossibleValues().isExhaustive()).isFalse();
    }
}