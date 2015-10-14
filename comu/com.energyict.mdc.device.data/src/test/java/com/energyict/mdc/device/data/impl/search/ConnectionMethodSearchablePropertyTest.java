package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.datavault.DataVaultService;
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
import com.energyict.mdc.device.data.impl.finders.ConnectionTypeFinder;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.ReferencePropertySpecFinderProvider;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConnectionMethodSearchablePropertyTest {
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
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private Thesaurus thesaurus;
    @Mock
    private ReferencePropertySpecFinderProvider referencePropertySpecFinderProvider;
    @Mock
    private ConnectionTypeFinder connectionTypeFinder;

    private PropertySpecService propertySpecService;

    @Before
    public void initializeMocks() {
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(PropertyTranslationKeys.CONNECTION_METHOD.getDefaultFormat());
        when(thesaurus.getFormat(PropertyTranslationKeys.CONNECTION_METHOD)).thenReturn(messageFormat);
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(this.dataModel);
        this.propertySpecService = new PropertySpecServiceImpl(jupiterPropertySpecService, dataVaultService, timeService, ormService);
        when(connectionTypeFinder.factoryId()).thenReturn(FactoryIds.CONNECTION_TYPE);
        when(connectionTypeFinder.valueDomain()).thenReturn(ConnectionTypePluggableClass.class);
        when(referencePropertySpecFinderProvider.finders()).thenReturn(Arrays.asList(connectionTypeFinder));
        ConnectionTypePluggableClass ctpc = mock(ConnectionTypePluggableClass.class);
        when(ctpc.getId()).thenReturn(13L);
        when(ctpc.getName()).thenReturn("EIWEB");
        when(protocolPluggableService.findAllConnectionTypePluggableClasses()).thenReturn(Arrays.asList(ctpc));
        this.propertySpecService.addFactoryProvider(this.referencePropertySpecFinderProvider);
    }

    @Test
    public void testGetDomain() {
        ConnectionMethodSearchableProperty property = this.getTestInstance();

        // Business method
        SearchDomain domain = property.getDomain();

        // Asserts
        assertThat(domain).isEqualTo(this.domain);
    }

    @Test
    public void testNoGroup() {
        ConnectionMethodSearchableProperty property = this.getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group).isEmpty();
    }

    @Test
    public void testVisibility() {
        ConnectionMethodSearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.Visibility visibility = property.getVisibility();

        // Asserts
        assertThat(visibility).isEqualTo(SearchableProperty.Visibility.REMOVABLE);
    }

    @Test
    public void testSelectionMode() {
        ConnectionMethodSearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.SelectionMode selectionMode = property.getSelectionMode();

        // Asserts
        assertThat(selectionMode).isEqualTo(SearchableProperty.SelectionMode.MULTI);
    }

    @Test
    public void testTranslation() {
        ConnectionMethodSearchableProperty property = this.getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.thesaurus).getFormat(PropertyTranslationKeys.CONNECTION_METHOD);
    }

    @Test
    public void testSpecification() {
        ConnectionMethodSearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification).isNotNull();
        assertThat(specification.isReference()).isTrue();
        assertThat(specification.getValueFactory().getValueType()).isEqualTo(ConnectionTypePluggableClass.class);
    }

    @Test
    public void testPossibleValuesWithoutRefresh() {
        ConnectionMethodSearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification.getPossibleValues()).isNotNull();
        assertThat(specification.getPossibleValues().isExhaustive()).isTrue();
        assertThat(specification.getPossibleValues().getAllValues()).hasSize(1);
    }

    @Test
    public void testPropertyHasNoConstraints() {
        ConnectionMethodSearchableProperty property = this.getTestInstance();

        // Business method
        List<SearchableProperty> constraints = property.getConstraints();

        // Asserts
        assertThat(constraints).isEmpty();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testDisplayBigDecimalValue() {
        ConnectionMethodSearchableProperty property = this.getTestInstance();

        // Business method
        property.toDisplay(BigDecimal.TEN);

        // Asserts: see expected exception rule
    }

    @Test
    public void testDisplayConnectionTypeValue() {
        ConnectionMethodSearchableProperty property = this.getTestInstance();
        ConnectionTypePluggableClass connectionTypePluggableClass = mock(ConnectionTypePluggableClass.class);
        when(connectionTypePluggableClass.getName()).thenReturn("EIWEB");

        // Business method
        String displayValue = property.toDisplay(connectionTypePluggableClass);

        // Asserts
        assertThat(displayValue).isEqualTo("EIWEB");
    }

    private ConnectionMethodSearchableProperty getTestInstance() {
        return new ConnectionMethodSearchableProperty(protocolPluggableService, propertySpecService, thesaurus).init(this.domain);
    }
}
