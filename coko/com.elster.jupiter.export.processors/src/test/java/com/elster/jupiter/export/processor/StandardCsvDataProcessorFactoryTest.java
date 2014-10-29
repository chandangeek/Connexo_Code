package com.elster.jupiter.export.processor;

import com.elster.jupiter.export.processor.impl.StandardCsvDataProcessorFactory;
import com.elster.jupiter.properties.BooleanFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import org.junit.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StandardCsvDataProcessorFactoryTest {


    @Test
    public void testGetProperties() throws Exception {
        StandardCsvDataProcessorFactory factory = new StandardCsvDataProcessorFactory();
        factory.setPropertySpecService(new PropertySpecServiceImpl());

        List<PropertySpec<?>> properties = factory.getProperties();
        assertThat(properties).hasSize(6);

        // Order IS important
        PropertySpec<?> property = properties.get(0);
        assertThat(property.getName()).isEqualTo("fileFormat.filenamePrefix");
        assertThat(property.isRequired()).isTrue();
        assertThat(property.getValueFactory()).isInstanceOf(StringFactory.class);
        property = properties.get(1);
        assertThat(property.getName()).isEqualTo("fileFormat.fileExtension");
        assertThat(property.isRequired()).isFalse();
        assertThat(property.getValueFactory()).isInstanceOf(StringFactory.class);
        assertThat(property.getPossibleValues().getDefault()).isEqualTo("csv");
        assertThat(property.getPossibleValues().isExhaustive()).isFalse();
        property = properties.get(2);
        assertThat(property.getName()).isEqualTo("fileFormat.updatedData.separateFile");
        assertThat(property.isRequired()).isFalse();
        assertThat(property.getValueFactory()).isInstanceOf(BooleanFactory.class);
        property = properties.get(3);
        assertThat(property.getName()).isEqualTo("fileFormat.updatedData.filenamePrefix");
        assertThat(property.isRequired()).isFalse();
        assertThat(property.getValueFactory()).isInstanceOf(StringFactory.class);
        property = properties.get(4);
        assertThat(property.getName()).isEqualTo("fileFormat.updatedData.fileExtension");
        assertThat(property.isRequired()).isFalse();
        assertThat(property.getValueFactory()).isInstanceOf(StringFactory.class);
        assertThat(property.getPossibleValues().getDefault()).isEqualTo("csv");
        assertThat(property.getPossibleValues().isExhaustive()).isFalse();
        property = properties.get(5);
        assertThat(property.getName()).isEqualTo("formatterProperties.separator");
        assertThat(property.isRequired()).isTrue();
        assertThat(property.getValueFactory()).isInstanceOf(StringFactory.class);
        assertThat(property.getPossibleValues().isExhaustive()).isTrue();
        List<String> allValues = (List<String>) property.getPossibleValues().getAllValues();
        assertThat(allValues).hasSize(2);
        assertThat(allValues).containsExactly("comma", "semicolon");
    }
}