package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.export.DataExportService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.exception.MessageSeed;
import com.elster.jupiter.validation.ValidationService;

import java.util.List;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class StandardCsvDataFormatterFactoryTest {

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private NlsMessageFormat messageFormat;
    @Mock
    private DataExportService dataExportService;
    @Mock
    private ValidationService validationService;
    @Mock
    private NlsService nlsService;
    @Mock
    private MeteringService meteringService;
    @Mock
    private OrmService ormService;
    @Mock
    private TimeService timeService;

    @Before
    public void initializeThesaurus() {
        when(this.nlsService.getThesaurus(anyString(), any(Layer.class))).thenReturn(this.thesaurus);
        when(this.thesaurus.getFormat(any(MessageSeed.class))).thenReturn(this.messageFormat);
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(this.messageFormat);
        when(this.messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
    }

    @Test
    public void testGetProperties() throws Exception {
        PropertySpecServiceImpl propertySpecService = new PropertySpecServiceImpl(this.timeService, this.ormService);
        StandardCsvDataFormatterFactory factory = new StandardCsvDataFormatterFactory(propertySpecService, this.dataExportService, this.validationService, this.nlsService, this.meteringService);

        List<PropertySpec> properties = factory.getPropertySpecs();
        assertThat(properties).hasSize(3);

        // Order IS important
        PropertySpec tagProperty = properties.get(0);
        assertThat(tagProperty.getName()).isEqualTo("formatterProperties.tag");
        assertThat(tagProperty.isRequired()).isTrue();
        assertThat(tagProperty.getValueFactory()).isInstanceOf(StringFactory.class);
        assertThat(tagProperty.getPossibleValues()).isNull();;

        PropertySpec updateTagProperty = properties.get(1);
        assertThat(updateTagProperty.getName()).isEqualTo("formatterProperties.update.tag");
        assertThat(updateTagProperty.isRequired()).isTrue();
        assertThat(updateTagProperty.getValueFactory()).isInstanceOf(StringFactory.class);
        assertThat(updateTagProperty.getPossibleValues()).isNull();;

        PropertySpec separatorProperty = properties.get(2);
        assertThat(separatorProperty.getName()).isEqualTo("formatterProperties.separator");
        assertThat(separatorProperty.isRequired()).isTrue();
        assertThat(separatorProperty.getValueFactory()).isInstanceOf(StringFactory.class);
        assertThat(separatorProperty.getPossibleValues().isExhaustive()).isTrue();
    }

}