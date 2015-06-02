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
        assertThat(properties).hasSize(4);

        // Order IS important
        PropertySpec property = properties.get(0);
        assertThat(property.getName()).isEqualTo("fileFormat.filenamePrefix");
        assertThat(property.isRequired()).isTrue();
        assertThat(property.getValueFactory()).isInstanceOf(StringFactory.class);
        property = properties.get(1);
        assertThat(property.getName()).isEqualTo("fileFormat.fileExtension");
        assertThat(property.isRequired()).isFalse();
        assertThat(property.getValueFactory()).isInstanceOf(StringFactory.class);
        assertThat(property.getPossibleValues().getDefault()).isEqualTo("csv");
        assertThat(property.getPossibleValues().isExhaustive()).isFalse();
       /* property = properties.get(2);
        assertThat(property.getName()).isEqualTo("fileFormat.updatedData.separateFile");
        assertThat(property.isRequired()).isFalse();
        assertThat(property.getValueFactory()).isInstanceOf(BooleanFactory.class);
        property = properties.get(3);
        assertThat(property.getName()).isEqualTo("fileFormat.updatedData.updateFilenamePrefix");
        assertThat(property.isRequired()).isFalse();
        assertThat(property.getValueFactory()).isInstanceOf(StringFactory.class);
        property = properties.get(4);
        assertThat(property.getName()).isEqualTo("fileFormat.updatedData.updateFileExtension");
        assertThat(property.isRequired()).isFalse();
        assertThat(property.getValueFactory()).isInstanceOf(StringFactory.class);
        assertThat(property.getPossibleValues().getDefault()).isEqualTo("csv");
        assertThat(property.getPossibleValues().isExhaustive()).isFalse();*/
        property = properties.get(2);
        assertThat(property.getName()).isEqualTo("formatterProperties.separator");
        assertThat(property.isRequired()).isTrue();
        assertThat(property.getValueFactory()).isInstanceOf(StringFactory.class);
        assertThat(property.getPossibleValues().isExhaustive()).isTrue();
        List<String> allValues = (List<String>) property.getPossibleValues().getAllValues();
        assertThat(allValues).hasSize(2);
        assertThat(allValues).containsExactly("comma", "semicolon");
        property = properties.stream()
                .filter(a -> a.getName().equals("fileFormat.path"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("PropertySpecs did not contain a property named fileFormat.path"));
        assertThat(property.isRequired()).isFalse();
        assertThat(property.getValueFactory()).isInstanceOf(StringFactory.class);
    }
}