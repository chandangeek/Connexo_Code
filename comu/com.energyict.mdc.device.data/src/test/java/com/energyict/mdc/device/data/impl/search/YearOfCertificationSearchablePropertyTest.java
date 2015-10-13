package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.*;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.time.TimeService;
import com.energyict.mdc.device.data.DeviceFields;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalMatchers;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class YearOfCertificationSearchablePropertyTest {
    @Mock
    private DeviceSearchDomain domain;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DataModel dataModel;
    @Mock
    private TimeService timeService;
    @Mock
    private OrmService ormService;
    @Mock
    private DataVaultService dataVaultService;
    @Mock
    private PropertySpecService propertySpecService;

    @Before
    public void initializeMocks() {
        when(this.ormService.newDataModel(anyString(), anyString())).thenReturn(this.dataModel);

        when(this.propertySpecService.longPropertySpecWithValues(eq(DeviceFields.CERT_YEAR.fieldName()), eq(false), Matchers.anyVararg()))
                .thenReturn(new BasicPropertySpec(DeviceFields.CERT_YEAR.fieldName(), false, new LongFactory()));
    }

    @Test
    public void testGetDomain() {
        YearOfCertificationSearchableProperty property = this.getTestInstance();

        // Business method
        SearchDomain domain = property.getDomain();

        // Asserts
        assertThat(domain).isEqualTo(this.domain);
    }

    @Test
    public void testNoGroup() {
        YearOfCertificationSearchableProperty property = this.getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group).isEmpty();
    }

    @Test
    public void testRemovableVisibility() {
        YearOfCertificationSearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.Visibility visibility = property.getVisibility();

        // Asserts
        assertThat(visibility).isEqualTo(SearchableProperty.Visibility.REMOVABLE);
    }

    @Test
    public void testMultiSelection() {
        YearOfCertificationSearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.SelectionMode selectionMode = property.getSelectionMode();

        // Asserts
        assertThat(selectionMode).isEqualTo(SearchableProperty.SelectionMode.MULTI);
    }

    @Test
    public void testTranslation() {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(PropertyTranslationKeys.DEVICE_CERT_YEAR.getDefaultFormat());
        when(this.thesaurus.getFormat(PropertyTranslationKeys.DEVICE_CERT_YEAR)).thenReturn(messageFormat);
        YearOfCertificationSearchableProperty property = this.getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.thesaurus).getFormat(PropertyTranslationKeys.DEVICE_CERT_YEAR);
    }

    @Test
    public void specificationIsNotAReference() {
        YearOfCertificationSearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification).isNotNull();
        assertThat(specification.isReference()).isFalse();
        assertThat(specification.getValueFactory().getValueType()).isEqualTo(Long.class);
    }

    @Test
    public void noPossibleValuesWithoutRefresh() {
        YearOfCertificationSearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification.getPossibleValues()).isNull();
    }

    @Test
    public void noConstraints() {
        YearOfCertificationSearchableProperty property = this.getTestInstance();

        // Business method
        List<SearchableProperty> constraints = property.getConstraints();

        // Asserts
        assertThat(constraints).isEmpty();
    }

    @Test
    public void refreshWithoutConstrictions() {
        YearOfCertificationSearchableProperty property = this.getTestInstance();

        // Business method
        property.refreshWithConstrictions(Collections.emptyList());

        // Asserts
        PropertySpec specification = property.getSpecification();
        assertThat(specification.getPossibleValues()).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void refreshWithTooManyConstrictions() {
        YearOfCertificationSearchableProperty property = this.getTestInstance();
        SearchableProperty searchableProperty = mock(SearchableProperty.class);
        SearchablePropertyConstriction constriction = SearchablePropertyConstriction.noValues(searchableProperty);

        // Business method
        property.refreshWithConstrictions(Arrays.asList(constriction));

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void displayBigDecimal() {
        YearOfCertificationSearchableProperty property = this.getTestInstance();

        // Business method
        property.toDisplay(BigDecimal.TEN);

        // Asserts: see expected exception rule
    }

    @Test
    public void displayString() {
        YearOfCertificationSearchableProperty property = this.getTestInstance();
        long valueToDisplay = 2013;

        // Business method
        String displayValue = property.toDisplay(valueToDisplay);

        // Asserts
        assertThat(displayValue).isEqualTo(String.valueOf(valueToDisplay));
    }

    private YearOfCertificationSearchableProperty getTestInstance() {
        return new YearOfCertificationSearchableProperty(this.propertySpecService, this.thesaurus).init(this.domain);
    }

}
