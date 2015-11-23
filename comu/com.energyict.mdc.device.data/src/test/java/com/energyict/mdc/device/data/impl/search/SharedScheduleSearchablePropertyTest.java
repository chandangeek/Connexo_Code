package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.domain.util.Finder;
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
import com.energyict.mdc.scheduling.model.impl.ComScheduleFinder;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;
import com.energyict.mdc.scheduling.SchedulingService;
import com.energyict.mdc.scheduling.model.ComSchedule;
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
public class SharedScheduleSearchablePropertyTest {

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
    private ComScheduleFinder comScheduleFinder;

    private PropertySpecService propertySpecService;

    @Before
    public void initializeMocks() {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(PropertyTranslationKeys.SHARED_SCHEDULE.getDefaultFormat());
        when(thesaurus.getFormat(PropertyTranslationKeys.SHARED_SCHEDULE)).thenReturn(messageFormat);
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(this.dataModel);
        this.propertySpecService = new PropertySpecServiceImpl(jupiterPropertySpecService, dataVaultService, timeService, ormService);
        when(comScheduleFinder.factoryId()).thenReturn(FactoryIds.COMSCHEDULE);
        when(comScheduleFinder.valueDomain()).thenReturn(ComSchedule.class);
        when(referencePropertySpecFinderProvider.finders()).thenReturn(Arrays.asList(comScheduleFinder));
        ComSchedule comSchedule = mock(ComSchedule.class);
        Finder finder = mock(Finder.class);
        when(finder.find()).thenReturn(Arrays.asList(comSchedule));
        when(schedulingService.findAllSchedules()).thenReturn(finder);
        this.propertySpecService.addFactoryProvider(this.referencePropertySpecFinderProvider);
    }

    @Test
    public void testGetDomain() {
        SharedScheduleSearchableProperty property = this.getTestInstance();

        // Business method
        SearchDomain domain = property.getDomain();

        // Asserts
        assertThat(domain).isEqualTo(this.domain);
    }

    @Test
    public void testNoGroup() {
        SharedScheduleSearchableProperty property = this.getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group).isEmpty();
    }

    @Test
    public void testVisibility() {
        SharedScheduleSearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.Visibility visibility = property.getVisibility();

        // Asserts
        assertThat(visibility).isEqualTo(SearchableProperty.Visibility.REMOVABLE);
    }

    @Test
    public void testSelectionMode() {
        SharedScheduleSearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.SelectionMode selectionMode = property.getSelectionMode();

        // Asserts
        assertThat(selectionMode).isEqualTo(SearchableProperty.SelectionMode.MULTI);
    }

    @Test
    public void testTranslation() {
        SharedScheduleSearchableProperty property = this.getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.thesaurus).getFormat(PropertyTranslationKeys.SHARED_SCHEDULE);
    }

    @Test
    public void testSpecification() {
        SharedScheduleSearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification).isNotNull();
        assertThat(specification.isReference()).isTrue();
        assertThat(specification.getValueFactory().getValueType()).isEqualTo(ComSchedule.class);
    }

    @Test
    public void testPossibleValuesWithoutRefresh() {
        SharedScheduleSearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification.getPossibleValues()).isNotNull();
        assertThat(specification.getPossibleValues().isExhaustive()).isTrue();
        assertThat(specification.getPossibleValues().getAllValues()).hasSize(1);
    }

    @Test
    public void testPropertyHasNoConstraints() {
        SharedScheduleSearchableProperty property = this.getTestInstance();

        // Business method
        List<SearchableProperty> constraints = property.getConstraints();

        // Asserts
        assertThat(constraints).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDisplayBigDecimalValue() {
        SharedScheduleSearchableProperty property = this.getTestInstance();

        // Business method
        property.toDisplay(BigDecimal.TEN);

        // Asserts: see expected exception rule
    }

    @Test
    public void testDisplayComScheduleValue() {
        SharedScheduleSearchableProperty property = this.getTestInstance();
        ComSchedule comSchedule = mock(ComSchedule.class);
        when(comSchedule.getName()).thenReturn("ComSchedule");

        // Business method
        String displayValue = property.toDisplay(comSchedule);

        // Asserts
        assertThat(displayValue).isEqualTo("ComSchedule");
    }

    private SharedScheduleSearchableProperty getTestInstance() {
        return new SharedScheduleSearchableProperty(schedulingService, propertySpecService, thesaurus).init(this.domain);
    }
}
