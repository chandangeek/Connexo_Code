package com.elster.jupiter.properties.impl;

import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.StringFactory;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link PropertySpecServiceImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-07 (19:27)
 */
@RunWith(MockitoJUnitRunner.class)
public class PropertySpecServiceImplTest {

    @Mock
    private Thesaurus thesaurus;
    @Mock
    private TranslationKey nameTranslationKey;
    @Mock
    private TranslationKey descriptionTranslationKey;

    @Before
    public void initializeMocks() {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn("Translation not supported in unit tests");
        when(this.thesaurus.getFormat(any(TranslationKey.class))).thenReturn(messageFormat);
    }

    @Test
    public void fluentApiForThesaurusBasedApproach() {
        PropertySpec propertySpec =
            this.getTestInstance()
                .stringSpec()
                .named(this.nameTranslationKey)
                .describedAs(this.descriptionTranslationKey)
                .fromThesaurus(this.thesaurus)
                .addValues("One", "Two", "Three")
                .markExhaustive()
                .setDefaultValue("One")
                .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
    }

    @Test
    public void fluentApiForHardCodedApproach() {
        PropertySpec propertySpec = this.getTestInstance()
                .specForValuesOf(new StringFactory())
                .named("hardcoded", "display name")
                .describedAs("hardcoded description")
                .addValues("One", "Two", "Three")
                .markExhaustive()
                .setDefaultValue("One")
                .finish();

        // Asserts
        assertThat(propertySpec).isNotNull();
    }

    private PropertySpecService getTestInstance() {
        return new PropertySpecServiceImpl();
    }

}