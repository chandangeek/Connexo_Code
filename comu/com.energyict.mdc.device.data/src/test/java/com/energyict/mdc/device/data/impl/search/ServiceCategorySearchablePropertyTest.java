package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.time.TimeService;
import com.energyict.mdc.common.FactoryIds;
import com.energyict.mdc.device.data.impl.finders.ServiceCategoryFinder;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;
import com.energyict.mdc.scheduling.SchedulingService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ServiceCategorySearchablePropertyTest {

    @Mock
    private DataVaultService dataVaultService;
    @Mock
    private com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService;
    @Mock
    private DataModel dataModel;
    @Mock
    private TimeService timeService;
    @Mock
    private OrmService ormService;
    @Mock
    private DeviceSearchDomain domain;
    @Mock
    private SchedulingService schedulingService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ReferencePropertySpecFinderProvider referencePropertySpecFinderProvider;
    @Mock
    private ServiceCategoryFinder serviceCategoryFinder;
    @Mock
    private MeteringService meteringService;

    private PropertySpecService propertySpecService;

    @Before
    public void initializeMocks() {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(PropertyTranslationKeys.SERVICE_CATEGORY.getDefaultFormat());
        when(thesaurus.getFormat(PropertyTranslationKeys.SERVICE_CATEGORY)).thenReturn(messageFormat);
        when(thesaurus.getStringBeyondComponent(anyString(), anyString())).thenAnswer(invocationOnMock -> invocationOnMock.getArguments()[1]);
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(this.dataModel);
        this.propertySpecService = new PropertySpecServiceImpl(jupiterPropertySpecService, dataVaultService, timeService, ormService);
        when(serviceCategoryFinder.factoryId()).thenReturn(FactoryIds.SERVICE_CATEGORY);
        when(serviceCategoryFinder.valueDomain()).thenReturn(ServiceCategory.class);
        when(referencePropertySpecFinderProvider.finders()).thenReturn(Arrays.asList(serviceCategoryFinder));
        ServiceCategory gasCategory = mock(ServiceCategory.class);
        when(meteringService.getServiceCategory(any())).thenReturn(Optional.empty());
        when(meteringService.getServiceCategory(ServiceKind.GAS)).thenReturn(Optional.of(gasCategory));
        this.propertySpecService.addFactoryProvider(this.referencePropertySpecFinderProvider);
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
    public void testVisibility() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.Visibility visibility = property.getVisibility();

        // Asserts
        assertThat(visibility).isEqualTo(SearchableProperty.Visibility.REMOVABLE);
    }

    @Test
    public void testSelectionMode() {
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
        verify(this.thesaurus).getFormat(PropertyTranslationKeys.SERVICE_CATEGORY);
    }

    @Test
    public void testSpecification() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification).isNotNull();
        assertThat(specification.isReference()).isTrue();
        assertThat(specification.getValueFactory().getValueType()).isEqualTo(ServiceCategory.class);
    }

    @Test
    public void testPossibleValuesWithoutRefresh() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification.getPossibleValues()).isNotNull();
        assertThat(specification.getPossibleValues().isExhaustive()).isTrue();
        assertThat(specification.getPossibleValues().getAllValues()).hasSize(1);
    }

    @Test
    public void testPropertyHasNoConstraints() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        List<SearchableProperty> constraints = property.getConstraints();

        // Asserts
        assertThat(constraints).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDisplayBigDecimalValue() {
        ServiceCategorySearchableProperty property = this.getTestInstance();

        // Business method
        property.toDisplay(BigDecimal.TEN);

        // Asserts: see expected exception rule
    }

    @Test
    public void testDisplayValue() {
        ServiceCategorySearchableProperty property = this.getTestInstance();
        ServiceCategory serviceCategory = mock(ServiceCategory.class);
        when(serviceCategory.getKind()).thenReturn(ServiceKind.GAS);

        // Business method
        String displayValue = property.toDisplay(serviceCategory);

        // Asserts
        assertThat(displayValue).isEqualTo("gas");
    }

    private ServiceCategorySearchableProperty getTestInstance() {
        return new ServiceCategorySearchableProperty(meteringService, propertySpecService, thesaurus).init(this.domain);
    }
}
