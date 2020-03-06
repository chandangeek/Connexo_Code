/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.soap.webservices.impl.search;

import com.elster.jupiter.datavault.DataVaultService;
import com.elster.jupiter.nls.NlsMessageFormat;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchableProperty;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.search.SearchablePropertyOperator;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.DefaultBeanService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.device.data.impl.search.SearchableDeviceProperty;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceChannelSAPInfoDomainExtension;
import com.energyict.mdc.sap.soap.webservices.impl.custompropertyset.DeviceRegisterSAPInfoDomainExtension;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LogicalRegisterNumberSearchablePropertyTest {
    private SearchablePropertyGroup parentGroup;
    @Mock
    private DataVaultService dataVaultService;
    @Mock
    private TimeService timeService;
    @Mock
    private DataModel dataModel;
    @Mock
    private OrmService ormService;
    @Mock
    Thesaurus thesaurus;
    private BeanService beanService = new DefaultBeanService();
    private PropertySpecService propertySpecService;

    @Before
    public void initializeMocks() {
        when(ormService.getDataModel(anyString())).thenReturn(Optional.of(dataModel));
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService = new com.elster.jupiter.properties.impl.PropertySpecServiceImpl(timeService, this.ormService, this.beanService);
        propertySpecService = new PropertySpecServiceImpl(jupiterPropertySpecService, dataVaultService, ormService);
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(PropertyTranslationKeys.LOGICAL_REGISTER_NUMBER.getDefaultFormat());
        when(thesaurus.getFormat(PropertyTranslationKeys.LOGICAL_REGISTER_NUMBER)).thenReturn(messageFormat);

        parentGroup = new SapAttributesSearchablePropertyGroup(thesaurus);
    }

    @Test
    public void testGroup() {
        SearchableProperty property = getTestInstance();

        // Business method
        Optional<SearchablePropertyGroup> group = property.getGroup();

        // Asserts
        assertThat(group).isPresent();
        assertThat(group.get().getId()).isEqualTo(SapAttributesSearchablePropertyGroup.GROUP_NAME);
    }

    @Test
    public void testTranslation() {
        SearchableProperty property = getTestInstance();

        // Business method
        property.getDisplayName();

        // Asserts
        verify(this.thesaurus).getFormat(PropertyTranslationKeys.LOGICAL_REGISTER_NUMBER);
    }

    @Test
    public void testGetDomain() {
        SearchableProperty property = getTestInstance();

        // Business method
        SearchDomain domain = property.getDomain();

        // Asserts
        assertThat(domain).isNull();
    }

    @Test
    public void testVisibility() {
        SearchableProperty property = getTestInstance();

        // Business method
        SearchableProperty.Visibility visibility = property.getVisibility();

        // Asserts
        assertThat(visibility).isEqualTo(SearchableProperty.Visibility.REMOVABLE);
    }

    @Test
    public void testSelectionMode() {
        SearchableProperty property = getTestInstance();

        // Business method
        SearchableProperty.SelectionMode selectionMode = property.getSelectionMode();

        // Asserts
        assertThat(selectionMode).isEqualTo(SearchableProperty.SelectionMode.SINGLE);
    }

    @Test
    public void testSpecification() {
        SearchableProperty property = getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification).isNotNull();
        assertThat(specification.isReference()).isFalse();
        assertThat(specification.getValueFactory().getValueType()).isEqualTo(String.class);
    }

    @Test
    public void testPossibleValues() {
        SearchableProperty property = getTestInstance();

        // Business method
        PropertySpec specification = property.getSpecification();

        // Asserts
        assertThat(specification.getPossibleValues()).isNull();
    }

    @Test
    public void testPropertyHasNoConstraints() {
        SearchableProperty property = getTestInstance();

        // Business method
        List<SearchableProperty> constraints = property.getConstraints();

        // Asserts
        assertThat(constraints).isEmpty();
    }

    @Test
    public void testAvailableOperators() {
        SearchableProperty property = getTestInstance();

        // Business method
        List<String> operators = property.getAvailableOperators();

        // Asserts
        assertThat(operators).isEqualTo(Arrays.asList(SearchablePropertyOperator.EQUAL.code(), SearchablePropertyOperator.NOT_EQUAL.code(), SearchablePropertyOperator.IN.code()));
    }

    @Test
    public void testToSqlFragment() {
        QueryExecutor queryExecutor = mock(QueryExecutor.class);
        SqlFragment fragment = mock(SqlFragment.class);
        when(fragment.getText()).thenReturn("Fragment1", "Fragment2");
        doReturn(fragment).when(queryExecutor).asFragment(any(Condition.class), anyVararg());
        when(this.dataModel.query(any())).thenReturn(queryExecutor);
        Condition condition = mock(Condition.class);

        // Business method
        SqlFragment sqlFragment = getTestInstance().toSqlFragment(condition, Instant.now());

        // Asserts
        assertThat(sqlFragment.getText()).isEqualTo(" (Fragment1 UNION Fragment2) ");
        verify(dataModel).query(eq(DeviceRegisterSAPInfoDomainExtension.class));
        verify(queryExecutor, times(2)).asFragment(any(Condition.class), eq(DeviceRegisterSAPInfoDomainExtension.FieldNames.DEVICE_ID.javaName()));
        verify(dataModel).query(eq(DeviceChannelSAPInfoDomainExtension.class));
    }

    private SearchableDeviceProperty getTestInstance() {
        return new LogicalRegisterNumberSearchableProperty(propertySpecService, thesaurus, ormService).init(this.parentGroup);
    }
}
