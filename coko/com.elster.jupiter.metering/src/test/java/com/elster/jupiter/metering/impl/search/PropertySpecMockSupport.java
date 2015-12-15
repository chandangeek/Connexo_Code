package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.PropertySpecBuilderWizard;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.TimeZoneFactory;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Provides mock support for {@link PropertySpec}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-15 (09:29)
 */
public final class PropertySpecMockSupport {

    public static void mockReferencePropertySpec(String name, Class referencedClass, PropertySpecService propertySpecService) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        PropertySpecBuilder propertySpecBuilder = FakeBuilder.initBuilderStub(propertySpec, PropertySpecBuilder.class);
        PropertySpecBuilderWizard.ThesaurusBased thesaurusOptions = mock(PropertySpecBuilderWizard.ThesaurusBased.class);
        when(thesaurusOptions.fromThesaurus(any(Thesaurus.class))).thenReturn(propertySpecBuilder);
        PropertySpecBuilderWizard.NlsOptions nlsOptions = mock(PropertySpecBuilderWizard.NlsOptions.class);
        when(nlsOptions
                .named(name, any(TranslationKey.class)))
                .thenReturn(thesaurusOptions);
        when(nlsOptions
                .named(any(TranslationKey.class)))
                .thenReturn(thesaurusOptions);
        when(propertySpecService.referenceSpec(referencedClass)).thenReturn(nlsOptions);
    }

    public static void mockStringPropertySpec(String name, PropertySpecService propertySpecService) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        PropertySpecBuilder propertySpecBuilder = FakeBuilder.initBuilderStub(propertySpec, PropertySpecBuilder.class);
        PropertySpecBuilderWizard.ThesaurusBased thesaurusOptions = mock(PropertySpecBuilderWizard.ThesaurusBased.class);
        when(thesaurusOptions.fromThesaurus(any(Thesaurus.class))).thenReturn(propertySpecBuilder);
        PropertySpecBuilderWizard.NlsOptions nlsOptions = mock(PropertySpecBuilderWizard.NlsOptions.class);
        when(nlsOptions
                .named(eq(name), any(TranslationKey.class)))
                .thenReturn(thesaurusOptions);
        when(nlsOptions
                .named(any(TranslationKey.class)))
                .thenReturn(thesaurusOptions);
        when(propertySpecService.stringSpec()).thenReturn(nlsOptions);
    }

    public static void mockTimeZonePropertySpec(String name, PropertySpecService propertySpecService) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        PropertySpecBuilder propertySpecBuilder = FakeBuilder.initBuilderStub(propertySpec, PropertySpecBuilder.class);
        PropertySpecBuilderWizard.ThesaurusBased thesaurusOptions = mock(PropertySpecBuilderWizard.ThesaurusBased.class);
        when(thesaurusOptions.fromThesaurus(any(Thesaurus.class))).thenReturn(propertySpecBuilder);
        PropertySpecBuilderWizard.NlsOptions nlsOptions = mock(PropertySpecBuilderWizard.NlsOptions.class);
        when(nlsOptions
                .named(eq(name), any(TranslationKey.class)))
                .thenReturn(thesaurusOptions);
        when(nlsOptions
                .named(any(TranslationKey.class)))
                .thenReturn(thesaurusOptions);
        when(propertySpecService.specForValuesOf(any(TimeZoneFactory.class))).thenReturn(nlsOptions);
    }

    public static void mockLongPropertySpec(String name, PropertySpecService propertySpecService) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        PropertySpecBuilder propertySpecBuilder = FakeBuilder.initBuilderStub(propertySpec, PropertySpecBuilder.class);
        PropertySpecBuilderWizard.ThesaurusBased thesaurusOptions = mock(PropertySpecBuilderWizard.ThesaurusBased.class);
        when(thesaurusOptions.fromThesaurus(any(Thesaurus.class))).thenReturn(propertySpecBuilder);
        PropertySpecBuilderWizard.NlsOptions nlsOptions = mock(PropertySpecBuilderWizard.NlsOptions.class);
        when(nlsOptions
                .named(eq(name), any(TranslationKey.class)))
                .thenReturn(thesaurusOptions);
        when(nlsOptions
                .named(any(TranslationKey.class)))
                .thenReturn(thesaurusOptions);
        when(propertySpecService.longSpec()).thenReturn(nlsOptions);
    }

    public static void mockBooleanPropertySpec(String name, PropertySpecService propertySpecService) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        PropertySpecBuilder propertySpecBuilder = FakeBuilder.initBuilderStub(propertySpec, PropertySpecBuilder.class);
        PropertySpecBuilderWizard.ThesaurusBased thesaurusOptions = mock(PropertySpecBuilderWizard.ThesaurusBased.class);
        when(thesaurusOptions.fromThesaurus(any(Thesaurus.class))).thenReturn(propertySpecBuilder);
        PropertySpecBuilderWizard.NlsOptions nlsOptions = mock(PropertySpecBuilderWizard.NlsOptions.class);
        when(nlsOptions
                .named(eq(name), any(TranslationKey.class)))
                .thenReturn(thesaurusOptions);
        when(nlsOptions
                .named(any(TranslationKey.class)))
                .thenReturn(thesaurusOptions);
        when(propertySpecService.booleanSpec()).thenReturn(nlsOptions);
    }

}