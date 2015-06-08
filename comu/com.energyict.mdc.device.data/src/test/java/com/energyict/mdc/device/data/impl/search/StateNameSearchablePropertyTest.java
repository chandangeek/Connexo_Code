package com.energyict.mdc.device.data.impl.search;

import com.energyict.mdc.common.CanFindByLongPrimaryKey;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.device.lifecycle.config.DeviceLifeCycle;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.fsm.FiniteStateMachine;
import com.elster.jupiter.fsm.State;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecPossibleValues;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyConstriction;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.time.TimeService;

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
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link StateNameSearchableProperty} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-06-02 (12:39)
 */
@RunWith(MockitoJUnitRunner.class)
public class StateNameSearchablePropertyTest {

    @Mock
    private DeviceSearchDomain domain;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private DataVaultService dataVaultService;
    @Mock
    private TimeService timeService;
    @Mock
    private DataModel dataModel;
    @Mock
    private OrmService ormService;
    @Mock
    private ReferencePropertySpecFinderProvider deviceConfigurationProvider;
    @Mock
    private CanFindByLongPrimaryKey<DeviceType> deviceTypeFinder;
    @Mock
    private DeviceConfigurationService deviceConfigurationService;

    private DeviceTypeSearchableProperty deviceTypeSearchableProperty;
    private PropertySpecService propertySpecService;

    @Before
    public void initializeMocks() {
        when(this.ormService.newDataModel(anyString(), anyString())).thenReturn(this.dataModel);
        com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService = new com.elster.jupiter.properties.impl.PropertySpecServiceImpl(this.timeService);
        this.propertySpecService = new PropertySpecServiceImpl(jupiterPropertySpecService, this.dataVaultService, this.ormService);
        when(this.thesaurus.getStringBeyondComponent(eq("One"), anyString())).thenReturn("One");
        when(this.thesaurus.getStringBeyondComponent(eq("Two"), anyString())).thenReturn("Two");
        when(this.deviceTypeFinder.factoryId()).thenReturn(FactoryIds.DEVICE_TYPE);
        when(this.deviceTypeFinder.valueDomain()).thenReturn(DeviceType.class);
        when(this.deviceConfigurationProvider.finders()).thenReturn(Arrays.asList(this.deviceTypeFinder));
        Finder<DeviceType> finder = mock(Finder.class);
        when(finder.find()).thenReturn(Collections.emptyList());
        when(this.deviceConfigurationService.findAllDeviceTypes()).thenReturn(finder);
        this.propertySpecService.addFactoryProvider(this.deviceConfigurationProvider);
        this.deviceTypeSearchableProperty = new DeviceTypeSearchableProperty(this.deviceConfigurationService, this.propertySpecService, this.thesaurus);
    }

    @Test
    public void testGetDomain() {
        StateNameSearchableProperty property = this.getTestInstance();

        // Business method
        SearchDomain domain = property.getDomain();

        // Asserts
        assertThat(domain).isEqualTo(this.domain);
    }

    @Test
    public void testNoGroup() {
        StateNameSearchableProperty property = this.getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group).isEmpty();
    }

    @Test
    public void testStickyVisibility() {
        StateNameSearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.Visibility visibility = property.getVisibility();

        // Asserts
        assertThat(visibility).isEqualTo(SearchableProperty.Visibility.STICKY);
    }

    @Test
    public void testMultiSelection() {
        StateNameSearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.SelectionMode selectionMode = property.getSelectionMode();

        // Asserts
        assertThat(selectionMode).isEqualTo(SearchableProperty.SelectionMode.MULTI);
    }

    @Test
    public void testTranslation() {
        StateNameSearchableProperty property = this.getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.thesaurus).getString(eq(PropertyTranslationKeys.DEVICE_STATUS.getKey()), anyString());
    }

    @Test
    public void specificationIsNotAReference() {
        StateNameSearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification).isNotNull();
        assertThat(specification.isReference()).isFalse();
        assertThat(specification.getValueFactory().getValueType()).isEqualTo(String.class);
    }

    @Test
    public void noPossibleValuesWithoutRefresh() {
        StateNameSearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification.getPossibleValues()).isNotNull();
        assertThat(specification.getPossibleValues().getAllValues()).isEmpty();
        assertThat(specification.getPossibleValues().isExhaustive()).isTrue();
    }

    @Test
    public void propertyIsConstraintByDeviceTypeProperty() {
        StateNameSearchableProperty property = this.getTestInstance();

        // Business method
        List<SearchableProperty> constraints = property.getConstraints();

        // Asserts
        assertThat(constraints).hasSize(1);
        assertThat(constraints.get(0)).isEqualTo(this.deviceTypeSearchableProperty);
    }

    @Test(expected = IllegalArgumentException.class)
    public void refreshWithoutConstrictions() {
        StateNameSearchableProperty property = this.getTestInstance();

        // Business method
        property.refreshWithConstrictions(Collections.emptyList());

        // Asserts: see expected exception rule
    }

    @Test(expected = IllegalArgumentException.class)
    public void refreshWithTooManyConstrictions() {
        StateNameSearchableProperty property = this.getTestInstance();
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
        StateNameSearchableProperty property = this.getTestInstance();
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(this.deviceTypeSearchableProperty, Arrays.asList("Wrong", "type"));

        // Business method
        property.refreshWithConstrictions(Arrays.asList(deviceTypeConstriction));

        // Asserts: see expected exception rule
    }

    @Test
    public void refresh() {
        State state1 = mock(State.class);
        when(state1.getName()).thenReturn("One");
        State state2 = mock(State.class);
        when(state2.getName()).thenReturn("Two");
        FiniteStateMachine fsm = mock(FiniteStateMachine.class);
        when(fsm.getStates()).thenReturn(Arrays.asList(state1, state2));
        DeviceLifeCycle deviceLifeCycle = mock(DeviceLifeCycle.class);
        when(deviceLifeCycle.getFiniteStateMachine()).thenReturn(fsm);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getDeviceLifeCycle()).thenReturn(deviceLifeCycle);

        StateNameSearchableProperty property = this.getTestInstance();
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(this.deviceTypeSearchableProperty, Arrays.asList(deviceType));

        // Business method
        property.refreshWithConstrictions(Arrays.asList(deviceTypeConstriction));

        // Asserts
        PropertySpecPossibleValues possibleValues = property.getSpecification().getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getAllValues()).containsOnly("One", "Two");
    }

    @Test
    public void refreshWithMultipleDeviceTypes() {
        State state1 = mock(State.class);
        when(state1.getName()).thenReturn("One");
        FiniteStateMachine fsm1 = mock(FiniteStateMachine.class);
        when(fsm1.getStates()).thenReturn(Arrays.asList(state1));
        DeviceLifeCycle deviceLifeCycle1 = mock(DeviceLifeCycle.class);
        when(deviceLifeCycle1.getFiniteStateMachine()).thenReturn(fsm1);
        DeviceType deviceType1 = mock(DeviceType.class);
        when(deviceType1.getDeviceLifeCycle()).thenReturn(deviceLifeCycle1);
        when(deviceType1.getName()).thenReturn("DT-One");
        State state2 = mock(State.class);
        when(state2.getName()).thenReturn("Two");
        FiniteStateMachine fsm2 = mock(FiniteStateMachine.class);
        when(fsm2.getStates()).thenReturn(Arrays.asList(state2));
        DeviceLifeCycle deviceLifeCycle2 = mock(DeviceLifeCycle.class);
        when(deviceLifeCycle2.getFiniteStateMachine()).thenReturn(fsm2);
        DeviceType deviceType2 = mock(DeviceType.class);
        when(deviceType2.getDeviceLifeCycle()).thenReturn(deviceLifeCycle2);
        when(deviceType2.getName()).thenReturn("DT-Two");

        StateNameSearchableProperty property = this.getTestInstance();
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(this.deviceTypeSearchableProperty, Arrays.asList(deviceType1, deviceType2));

        // Business method
        property.refreshWithConstrictions(Arrays.asList(deviceTypeConstriction));

        // Asserts
        PropertySpecPossibleValues possibleValues = property.getSpecification().getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getAllValues()).containsOnly("One(DT-One)", "Two(DT-Two)");
    }

    @Test(expected = IllegalArgumentException.class)
    public void refreshWithConstrictionFromAnotherProperty() {
        State state1 = mock(State.class);
        when(state1.getName()).thenReturn("One");
        State state2 = mock(State.class);
        when(state2.getName()).thenReturn("Two");
        FiniteStateMachine fsm = mock(FiniteStateMachine.class);
        when(fsm.getStates()).thenReturn(Arrays.asList(state1, state2));
        DeviceLifeCycle deviceLifeCycle = mock(DeviceLifeCycle.class);
        when(deviceLifeCycle.getFiniteStateMachine()).thenReturn(fsm);
        DeviceType deviceType = mock(DeviceType.class);
        when(deviceType.getDeviceLifeCycle()).thenReturn(deviceLifeCycle);

        StateNameSearchableProperty property = this.getTestInstance();
        SearchableProperty otherProperty = mock(SearchableProperty.class);
        when(otherProperty.hasName(anyString())).thenReturn(false);
        SearchablePropertyConstriction deviceTypeConstriction = SearchablePropertyConstriction.withValues(otherProperty, Arrays.asList(deviceType));

        // Business method
        property.refreshWithConstrictions(Arrays.asList(deviceTypeConstriction));

        // Asserts: see expected exception rule
        PropertySpecPossibleValues possibleValues = property.getSpecification().getPossibleValues();
        assertThat(possibleValues).isNotNull();
        assertThat(possibleValues.isExhaustive()).isTrue();
        assertThat(possibleValues.getAllValues()).containsOnly("One", "Two");
    }

    @Test(expected = IllegalArgumentException.class)
    public void displayBigDecimal() {
        StateNameSearchableProperty property = this.getTestInstance();

        // Business method
        property.toDisplay(BigDecimal.TEN);

        // Asserts: see expected exception rule
    }

    @Test
    public void displayString() {
        StateNameSearchableProperty property = this.getTestInstance();
        String valueToDisplay = "displayString";

        // Business method
        String displayValue = property.toDisplay(valueToDisplay);

        // Asserts
        assertThat(displayValue).isEqualTo(valueToDisplay);
    }

    private StateNameSearchableProperty getTestInstance() {
        return new StateNameSearchableProperty(this.propertySpecService, this.thesaurus).init(this.domain, this.deviceTypeSearchableProperty);
    }

}