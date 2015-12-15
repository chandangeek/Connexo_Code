package com.elster.jupiter.metering.impl.search;

import com.elster.jupiter.devtools.tests.FakeBuilder;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.properties.EnumFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.elster.jupiter.properties.PropertySpecBuilderWizard;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ServiceCategorySearchableProperty} component
 *
 * @author Anton Fomchenko
 * @since 09-09-2015
 */
@RunWith(MockitoJUnitRunner.class)
public class ServiceCategorySearchablePropertyTest {

    @Mock
    private UsagePointSearchDomain domain;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private PropertySpecService propertySpecService;

    @Before
    public void initializeMocks() {
        PropertySpec propertySpec = mock(PropertySpec.class);
        when(propertySpec.getName()).thenReturn("whatever");
        when(propertySpec.getDisplayName()).thenReturn("com.elster.jupiter.metering.impl.search.ServiceCategorySearchablePropertyTest.initializeMocks");
        PropertySpecBuilder propertySpecBuilder = FakeBuilder.initBuilderStub(propertySpec, PropertySpecBuilder.class);
        PropertySpecBuilderWizard.ThesaurusBased thesaurusOptions = mock(PropertySpecBuilderWizard.ThesaurusBased.class);
        when(thesaurusOptions.fromThesaurus(any(Thesaurus.class))).thenReturn(propertySpecBuilder);
        PropertySpecBuilderWizard.NlsOptions nlsOptions = mock(PropertySpecBuilderWizard.NlsOptions.class);
        when(nlsOptions
                .named(anyString(), any(TranslationKey.class)))
                .thenReturn(thesaurusOptions);
        when(nlsOptions
                .named(any(TranslationKey.class)))
                .thenReturn(thesaurusOptions);
        when(propertySpecService.specForValuesOf(any(EnumFactory.class))).thenReturn(nlsOptions);
    }

    @Test
    public void testGetDomain() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        SearchDomain domain = property.getDomain();

        // Asserts
        assertThat(domain).isEqualTo(this.domain);
    }

    @Test
    public void testNoGroup() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group).isEmpty();
    }

    @Test
    public void testRemovableVisibility() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.Visibility visibility = property.getVisibility();

        // Asserts
        assertThat(visibility).isEqualTo(SearchableProperty.Visibility.REMOVABLE);
    }

    @Test
    public void testMultiSelection() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.SelectionMode selectionMode = property.getSelectionMode();

        // Asserts
        assertThat(selectionMode).isEqualTo(SearchableProperty.SelectionMode.MULTI);
    }

    @Test
    public void testTranslation() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.thesaurus).getString(eq(PropertyTranslationKeys.USAGEPOINT_SERVICECATEGORY.getKey()), anyString());
    }

    @Test
    public void specificationIsNotAReference() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification).isNotNull();
        assertThat(specification.isReference()).isFalse();
        assertThat(specification.getValueFactory().getValueType()).isEqualTo(Enum.class);
    }

    @Test
    public void hasPossibleValuesWithoutRefresh() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification.getPossibleValues()).isNotNull();
    }

    @Test
    public void noConstraints() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        List<SearchableProperty> constraints = property.getConstraints();

        // Asserts
        assertThat(constraints).isEmpty();
    }

    @Test
    public void refreshWithoutConstrictions() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        property.refreshWithConstrictions(Collections.emptyList());

        // Asserts
        PropertySpec specification = property.getSpecification();
        assertThat(specification.getPossibleValues()).isNotNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void refreshWithTooManyConstrictions() {
        ServiceCategorySearchableProperty property = this.getTestInstance();
        SearchableProperty searchableProperty = mock(SearchableProperty.class);
        SearchablePropertyConstriction constriction = SearchablePropertyConstriction.noValues(searchableProperty);

        // Business method
        property.refreshWithConstrictions(Collections.singletonList(constriction));

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void displayBigDecimal() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        property.toDisplay(BigDecimal.TEN);

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void displayString() {
        ServiceCategorySearchableProperty property = this.getTestInstance();
        String valueToDisplay = "displayString";

        // Business method
        property.toDisplay(valueToDisplay);

        // Asserts: see expected exception rule
    }

    @Test
    public void displayEnum() {
        ServiceCategorySearchableProperty property = this.getTestInstance();
        ServiceKind valueToDisplay = ServiceKind.ELECTRICITY;

        // Business method
        String displayValue = property.toDisplay(valueToDisplay);

        // Asserts
        assertThat(displayValue).isEqualTo(valueToDisplay.toString());
    }

    private ServiceCategorySearchableProperty getTestInstance() {
        return new ServiceCategorySearchableProperty(this.domain, this.propertySpecService, this.thesaurus);
    }

}