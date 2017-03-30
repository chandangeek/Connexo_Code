/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.PropertySpecBuilderWizard;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.TimeZoneFactory;

import java.util.TimeZone;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Provides mock support for {@link PropertySpec}s.
 * Implementation note:
 * The fact that the NlsOptions are created only once per
 * {@link com.elster.jupiter.properties.ValueFactory} is
 * essential. If that is removed, the order in which this
 * class is called by the test class also determines the
 * order in which the tested component is calling the
 * {@link com.elster.jupiter.properties.PropertySpecService}.
 * With the cache of NlsOptions in place, the methods
 * {@link PropertySpecBuilderWizard.NlsOptions#named(String, String)},
 * {@link PropertySpecBuilderWizard.NlsOptions#named(String, TranslationKey)} and
 * {@link PropertySpecBuilderWizard.NlsOptions#named(TranslationKey)}
 * become the discriminating factor that instruct Mockito to produce which
 * PropertySpec for which name (be it a String or a TranslationKey).
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-12-15 (09:29)
 */
public final class PropertySpecMockSupport {

    private PropertySpecBuilderWizard.NlsOptions<String> stringNlsOptions;
    private PropertySpecBuilderWizard.NlsOptions refNlsOptions;
    private PropertySpecBuilderWizard.NlsOptions<TimeZone> timeZoneNlsOptions;
    private PropertySpecBuilderWizard.NlsOptions<Long> longNlsOptions;
    private PropertySpecBuilderWizard.NlsOptions<Boolean> booleanNlsOptions;

    public void mockReferencePropertySpec(String name, Class referencedClass, PropertySpecService propertySpecService) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        PropertySpecBuilder propertySpecBuilder = FakeBuilder.initBuilderStub(propertySpec, PropertySpecBuilder.class);
        PropertySpecBuilderWizard.ThesaurusBased thesaurusOptions = mock(PropertySpecBuilderWizard.ThesaurusBased.class);
        when(thesaurusOptions.fromThesaurus(any(Thesaurus.class))).thenReturn(propertySpecBuilder);
        if (this.refNlsOptions == null) {
            this.refNlsOptions = mock(PropertySpecBuilderWizard.NlsOptions.class);
        }
        doReturn(thesaurusOptions).when(this.refNlsOptions).named(eq(name), any(TranslationKey.class));
        doReturn(thesaurusOptions).when(this.refNlsOptions).named(any(TranslationKey.class));
        PropertySpecBuilderWizard.HardCoded hardCodedOptions = mock(PropertySpecBuilderWizard.HardCoded.class);
        doReturn(hardCodedOptions).when(this.stringNlsOptions).named(eq(name), eq(name));
        when(hardCodedOptions.describedAs(anyString())).thenReturn(propertySpecBuilder);
        when(propertySpecService.referenceSpec(referencedClass)).thenReturn(this.refNlsOptions);
    }

    public PropertySpec mockStringPropertySpec(String name, PropertySpecService propertySpecService) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        PropertySpecBuilder propertySpecBuilder = FakeBuilder.initBuilderStub(propertySpec, PropertySpecBuilder.class);
        PropertySpecBuilderWizard.ThesaurusBased<String> thesaurusOptions = mock(PropertySpecBuilderWizard.ThesaurusBased.class);
        when(thesaurusOptions.fromThesaurus(any(Thesaurus.class))).thenReturn(propertySpecBuilder);
        if (this.stringNlsOptions == null) {
            this.stringNlsOptions = mock(PropertySpecBuilderWizard.NlsOptions.class);
        }
        doReturn(thesaurusOptions).when(this.stringNlsOptions).named(eq(name), any(TranslationKey.class));
        doReturn(thesaurusOptions).when(this.stringNlsOptions).named(any(TranslationKey.class));
        PropertySpecBuilderWizard.HardCoded<String> hardCodedOptions = mock(PropertySpecBuilderWizard.HardCoded.class);
        doReturn(hardCodedOptions).when(this.stringNlsOptions).named(eq(name), eq(name));
        when(hardCodedOptions.describedAs(anyString())).thenReturn(propertySpecBuilder);
        when(propertySpecService.stringSpec()).thenReturn(this.stringNlsOptions);
        return propertySpec;
    }

    public void mockTimeZonePropertySpec(String name, PropertySpecService propertySpecService) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        PropertySpecBuilder propertySpecBuilder = FakeBuilder.initBuilderStub(propertySpec, PropertySpecBuilder.class);
        PropertySpecBuilderWizard.ThesaurusBased<TimeZone> thesaurusOptions = mock(PropertySpecBuilderWizard.ThesaurusBased.class);
        when(thesaurusOptions.fromThesaurus(any(Thesaurus.class))).thenReturn(propertySpecBuilder);
        if (this.timeZoneNlsOptions == null) {
            this.timeZoneNlsOptions = mock(PropertySpecBuilderWizard.NlsOptions.class);
        }
        doReturn(thesaurusOptions).when(this.timeZoneNlsOptions).named(eq(name), any(TranslationKey.class));
        doReturn(thesaurusOptions).when(this.timeZoneNlsOptions).named(any(TranslationKey.class));
        PropertySpecBuilderWizard.HardCoded<TimeZone> hardCodedOptions = mock(PropertySpecBuilderWizard.HardCoded.class);
        doReturn(hardCodedOptions).when(this.stringNlsOptions).named(eq(name), eq(name));
        when(hardCodedOptions.describedAs(anyString())).thenReturn(propertySpecBuilder);
        when(propertySpecService.specForValuesOf(any(TimeZoneFactory.class))).thenReturn(this.timeZoneNlsOptions);
    }

    public void mockLongPropertySpec(String name, PropertySpecService propertySpecService) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        PropertySpecBuilder propertySpecBuilder = FakeBuilder.initBuilderStub(propertySpec, PropertySpecBuilder.class);
        PropertySpecBuilderWizard.ThesaurusBased<Long> thesaurusOptions = mock(PropertySpecBuilderWizard.ThesaurusBased.class);
        when(thesaurusOptions.fromThesaurus(any(Thesaurus.class))).thenReturn(propertySpecBuilder);
        if (this.longNlsOptions == null) {
            this.longNlsOptions = mock(PropertySpecBuilderWizard.NlsOptions.class);
        }
        doReturn(thesaurusOptions).when(this.longNlsOptions).named(eq(name), any(TranslationKey.class));
        doReturn(thesaurusOptions).when(this.longNlsOptions).named(any(TranslationKey.class));
        PropertySpecBuilderWizard.HardCoded<Long> hardCodedOptions = mock(PropertySpecBuilderWizard.HardCoded.class);
        doReturn(hardCodedOptions).when(this.stringNlsOptions).named(eq(name), eq(name));
        when(hardCodedOptions.describedAs(anyString())).thenReturn(propertySpecBuilder);
        when(propertySpecService.longSpec()).thenReturn(this.longNlsOptions);
    }

    public void mockBooleanPropertySpec(String name, PropertySpecService propertySpecService) {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn(name);
        PropertySpecBuilder propertySpecBuilder = FakeBuilder.initBuilderStub(propertySpec, PropertySpecBuilder.class);
        PropertySpecBuilderWizard.ThesaurusBased<Boolean> thesaurusOptions = mock(PropertySpecBuilderWizard.ThesaurusBased.class);
        when(thesaurusOptions.fromThesaurus(any(Thesaurus.class))).thenReturn(propertySpecBuilder);
        if (this.booleanNlsOptions == null) {
            this.booleanNlsOptions = mock(PropertySpecBuilderWizard.NlsOptions.class);
        }
        doReturn(thesaurusOptions).when(this.booleanNlsOptions).named(eq(name), any(TranslationKey.class));
        doReturn(thesaurusOptions).when(this.booleanNlsOptions).named(any(TranslationKey.class));
        PropertySpecBuilderWizard.HardCoded<Boolean> hardCodedOptions = mock(PropertySpecBuilderWizard.HardCoded.class);
        doReturn(hardCodedOptions).when(this.stringNlsOptions).named(eq(name), eq(name));
        when(hardCodedOptions.describedAs(anyString())).thenReturn(propertySpecBuilder);
        when(propertySpecService.booleanSpec()).thenReturn(this.booleanNlsOptions);
    }

}