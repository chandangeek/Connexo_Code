package com.energyict.mdc.device.data.impl.search;

import com.energyict.mdc.device.data.DeviceFields;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.BasicPropertySpec;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.StringFactory;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;

import java.math.BigDecimal;
import java.util.Arrays;
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
 * Tests the {@link SerialNumberSearchableProperty} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-02 (12:32)
 */
@RunWith(MockitoJUnitRunner.class)
public class SerialNumberSearchablePropertyTest {
    @Mock
    private DeviceSearchDomain domain;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DataModel dataModel;
    @Mock
    private OrmService ormService;
    @Mock
    private DataVaultService dataVaultService;
    @Mock
    private com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService;
    private PropertySpecService propertySpecService;

    @Before
    public void initializeMocks() {
        when(this.ormService.newDataModel(anyString(), anyString())).thenReturn(this.dataModel);
        when(this.jupiterPropertySpecService.basicPropertySpec(eq(DeviceFields.SERIALNUMBER.fieldName()), eq(false), any(ValueFactory.class)))
                .thenReturn(new BasicPropertySpec(DeviceFields.SERIALNUMBER.fieldName(), false, new StringFactory()));
        this.propertySpecService = new PropertySpecServiceImpl(this.jupiterPropertySpecService, this.dataVaultService, this.ormService);
    }

    @Test
    public void testGetDomain() {
        SerialNumberSearchableProperty property = this.getTestInstance();

        // Business method
        SearchDomain domain = property.getDomain();

        // Asserts
        assertThat(domain).isEqualTo(this.domain);
    }

    @Test
    public void testNoGroup() {
        SerialNumberSearchableProperty property = this.getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group).isEmpty();
    }

    @Test
    public void testStickyVisibility() {
        SerialNumberSearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.Visibility visibility = property.getVisibility();

        // Asserts
        assertThat(visibility).isEqualTo(SearchableProperty.Visibility.STICKY);
    }

    @Test
    public void testSingleSelection() {
        SerialNumberSearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.SelectionMode selectionMode = property.getSelectionMode();

        // Asserts
        assertThat(selectionMode).isEqualTo(SearchableProperty.SelectionMode.SINGLE);
    }

    @Test
    public void testTranslation() {
        SerialNumberSearchableProperty property = this.getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.thesaurus).getString(eq(PropertyTranslationKeys.DEVICE_SERIAL_NUMBER.getKey()), anyString());
    }

    @Test
    public void specificationIsNotAReference() {
        SerialNumberSearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification).isNotNull();
        assertThat(specification.isReference()).isFalse();
        assertThat(specification.getValueFactory().getValueType()).isEqualTo(String.class);
    }

    @Test
    public void noPossibleValuesWithoutRefresh() {
        SerialNumberSearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification.getPossibleValues()).isNull();
    }

    @Test
    public void noConstraints() {
        SerialNumberSearchableProperty property = this.getTestInstance();

        // Business method
        List<SearchableProperty> constraints = property.getConstraints();

        // Asserts
        assertThat(constraints).isEmpty();
    }

    @Test
    public void refreshWithoutConstrictions() {
        SerialNumberSearchableProperty property = this.getTestInstance();

        // Business method
        property.refreshWithConstrictions(Collections.emptyList());

        // Asserts
        PropertySpec specification = property.getSpecification();
        assertThat(specification.getPossibleValues()).isNull();
    }

    @Test(expected = IllegalArgumentException.class)
    public void refreshWithTooManyConstrictions() {
        SerialNumberSearchableProperty property = this.getTestInstance();
        SearchableProperty searchableProperty = mock(SearchableProperty.class);
        SearchablePropertyConstriction constriction = SearchablePropertyConstriction.noValues(searchableProperty);

        // Business method
        property.refreshWithConstrictions(Arrays.asList(constriction));

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void displayBigDecimal() {
        SerialNumberSearchableProperty property = this.getTestInstance();

        // Business method
        property.toDisplay(BigDecimal.TEN);

        // Asserts: see expected exception rule
    }

    @Test
    public void displayString() {
        SerialNumberSearchableProperty property = this.getTestInstance();
        String valueToDisplay = "displayString";

        // Business method
        String displayValue = property.toDisplay(valueToDisplay);

        // Asserts
        assertThat(displayValue).isEqualTo(valueToDisplay);
    }

    private SerialNumberSearchableProperty getTestInstance() {
        return new SerialNumberSearchableProperty(this.propertySpecService, this.thesaurus).init(this.domain);
    }

}