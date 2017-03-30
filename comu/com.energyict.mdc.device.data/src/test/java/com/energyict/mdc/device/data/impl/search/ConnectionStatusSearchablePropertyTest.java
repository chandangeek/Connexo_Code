/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.impl.PropertySpecServiceImpl;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.DefaultBeanService;
import com.energyict.mdc.dynamic.PropertySpecService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class ConnectionStatusSearchablePropertyTest {

    @Mock
    private DeviceSearchDomain domain;
    @Mock
    private SearchablePropertyGroup parentGroup;
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

    private BeanService beanService = new DefaultBeanService();
    private PropertySpecService propertySpecService;

    @Before
    public void initializeMocks() {
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(this.dataModel);

        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(PropertyTranslationKeys.CONNECTION_STATUS.getDefaultFormat());
        when(this.thesaurus.getFormat(PropertyTranslationKeys.CONNECTION_STATUS)).thenReturn(messageFormat);

        this.propertySpecService = new com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl(new PropertySpecServiceImpl(timeService, this.ormService, this.beanService), dataVaultService, ormService);
        this.parentGroup = new ConnectionSearchablePropertyGroup(this.thesaurus);
    }

    @Test
    public void testGetDomain() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        SearchDomain domain = property.getDomain();

        // Asserts
        assertThat(domain).isEqualTo(this.domain);
    }

    @Test
    public void testNoGroup() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group).isPresent();
        assertThat(group.get().getId()).isEqualTo(ConnectionSearchablePropertyGroup.GROUP_NAME);
    }

    @Test
    public void testVisibility() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.Visibility visibility = property.getVisibility();

        // Asserts
        assertThat(visibility).isEqualTo(SearchableProperty.Visibility.REMOVABLE);
    }

    @Test
    public void testMultiSelection() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        SearchableProperty.SelectionMode selectionMode = property.getSelectionMode();

        // Asserts
        assertThat(selectionMode).isEqualTo(SearchableProperty.SelectionMode.MULTI);
    }

    @Test
    public void testAffectsDomainSearchProperties() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        boolean affectsAvailableDomainProperties = property.affectsAvailableDomainProperties();

        // Asserts
        assertThat(affectsAvailableDomainProperties).isFalse();
    }

    @Test
    public void testTranslation() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.thesaurus).getFormat(PropertyTranslationKeys.CONNECTION_STATUS);
    }

    @Test
    public void specificationIsAReference() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification).isNotNull();
        assertThat(specification.isReference()).isFalse();
        assertThat(specification.getValueFactory().getValueType()).isEqualTo(ConnectionStatusSearchableProperty.ConnectionStatusSearchWrapper.class);
    }

    @Test
    public void propertyHasNoConstraints() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        List<SearchableProperty> constraints = property.getConstraints();

        // Asserts
        assertThat(constraints).isEmpty();
    }

    @Test
    public void propertyHasDefaultValues() {
        SearchableProperty property = this.getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        List allValues = specification.getPossibleValues().getAllValues();
        assertThat(allValues).isNotNull();
        assertThat(allValues).containsExactly(Arrays.stream(ConnectionStatusSearchableProperty.ConnectionStatusContainer.values())
                .map(ConnectionStatusSearchableProperty.ConnectionStatusSearchWrapper::new)
                .collect(Collectors.toList())
                .toArray(new ConnectionStatusSearchableProperty.ConnectionStatusSearchWrapper[ConnectionStatusSearchableProperty.ConnectionStatusContainer.values().length]));
        assertThat(specification.getValueFactory().getValueType()).isEqualTo(ConnectionStatusSearchableProperty.ConnectionStatusSearchWrapper.class);
    }

    private SearchableProperty getTestInstance() {
        return new ConnectionStatusSearchableProperty(this.propertySpecService, this.thesaurus)
                .init(this.domain, this.parentGroup);
    }
}