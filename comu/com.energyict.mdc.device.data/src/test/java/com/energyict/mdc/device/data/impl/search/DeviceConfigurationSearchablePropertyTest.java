package com.energyict.mdc.device.data.impl.search;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.config.DeviceConfiguration;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DeviceConfigurationSearchableProperty} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-02 (09:47)
 */
@RunWith(MockitoJUnitRunner.class)
public class DeviceConfigurationSearchablePropertyTest {

    @Mock
    private DeviceSearchDomain domain;
    @Mock
    private SearchableProperty deviceTypeSearchableProperty;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DataVaultService dataVaultService;
    @Mock
    private com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService;
    @Mock
    private DataModel dataModel;
    @Mock
    private OrmService ormService;
    @Mock
    private ReferencePropertySpecFinderProvider deviceConfigurationProvider;
    @Mock
    private CanFindByLongPrimaryKey<DeviceConfiguration> deviceConfigurationFinder;
    private PropertySpecService propertySpecService;

    @Before
    public void initializeMocks() {
        when(this.ormService.newDataModel(anyString(), anyString())).thenReturn(this.dataModel);
        this.propertySpecService = new PropertySpecServiceImpl(this.jupiterPropertySpecService, this.dataVaultService, this.ormService);
        when(this.deviceConfigurationFinder.factoryId()).thenReturn(FactoryIds.DEVICE_CONFIGURATION);
        when(this.deviceConfigurationFinder.valueDomain()).thenReturn(DeviceConfiguration.class);
        when(this.deviceConfigurationProvider.finders()).thenReturn(Arrays.asList(this.deviceConfigurationFinder));
        this.propertySpecService.addFactoryProvider(this.deviceConfigurationProvider);
        when(this.deviceTypeSearchableProperty.hasName(DeviceTypeSearchableProperty.PROPERTY_NAME)).thenReturn(true);
    }

    @Test
    public void testGetDomain() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();

        // Business method
        SearchDomain domain = property.getDomain();

        // Asserts
        assertThat(domain).isEqualTo(this.domain);
    }

    @Test
    public void testNoGroup() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group).isEmpty();
    }

    @Test
    public void testStickyVisibility() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.Visibility visibility = property.getVisibility();

        // Asserts
        assertThat(visibility).isEqualTo(SearchableProperty.Visibility.STICKY);
    }

    @Test
    public void testMultiSelection() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.SelectionMode selectionMode = property.getSelectionMode();

        // Asserts
        assertThat(selectionMode).isEqualTo(SearchableProperty.SelectionMode.MULTI);
    }

    @Test
    public void testTranslation() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.thesaurus).getString(eq(PropertyTranslationKeys.DEVICE_CONFIGURATION.getKey()), anyString());
    }

    @Test
    public void specificationIsReference() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification).isNotNull();
        assertThat(specification.isReference()).isTrue();
        assertThat(specification.getValueFactory().getValueType()).isEqualTo(DeviceConfiguration.class);
    }

    @Test
    public void noPossibleValuesWithoutRefresh() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification.getPossibleValues()).isNotNull();
        assertThat(specification.getPossibleValues().isExhaustive()).isTrue();
        assertThat(specification.getPossibleValues().getAllValues()).isEmpty();
    }

    @Test
    public void propertyIsConstraintByDeviceTypeProperty() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();

        // Business method
        List<SearchableProperty> constraints = property.getConstraints();

        // Asserts
        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).isEqualTo(this.deviceTypeSearchableProperty);
    }

    @Test(expected = IllegalArgumentException.class)
    public void refreshWithoutConstrictions() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();

        // Business method
        property.refreshWithConstrictions(Collections.emptyList());

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void refreshWithTooManyConstrictions() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();
        DeviceType deviceType = mock(DeviceType.class);
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(this.deviceTypeSearchableProperty, Arrays.asList(deviceType));
        SearchableProperty otherSearchableProperty = mock(SearchableProperty.class);
        SearchablePropertyConstriction otherConstriction = SearchablePropertyConstriction.noValues(otherSearchableProperty);

        // Business method
        property.refreshWithConstrictions(Arrays.asList(deviceTypeConstriction, otherConstriction));

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void refreshWithConstrictionsOfWrongType() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(this.deviceTypeSearchableProperty, Arrays.asList("Wrong", "type"));

        // Business method
        property.refreshWithConstrictions(Arrays.asList(deviceTypeConstriction));

        // Asserts: see expected exception rule
    }

    @Test
    public void refresh() {
        DeviceConfigurationSearchableProperty property = this.getTestInstance();
        DeviceConfiguration config1 = mock(DeviceConfiguration.class);
        DeviceConfiguration config2 = mock(DeviceConfiguration.class);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getConfigurations()).thenReturn(Arrays.asList(config1, config2));
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(this.deviceTypeSearchableProperty, Arrays.asList(deviceType));

        // Business method
        property.refreshWithConstrictions(Arrays.asList(deviceTypeConstriction));

        // Asserts
        PropertySpecPossibleValues possibleValues = property.getSpecification().getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getAllValues()).containsOnly(config2, config1);
    }

    private DeviceConfigurationSearchableProperty getTestInstance() {
        return new DeviceConfigurationSearchableProperty(this.domain, this.deviceTypeSearchableProperty, this.propertySpecService, this.thesaurus);
    }

}