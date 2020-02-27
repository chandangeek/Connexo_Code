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
import com.elster.jupiter.search.SearchDomain;
import com.elster.jupiter.search.SearchDomainExtension;
import com.elster.jupiter.search.SearchablePropertyCondition;
import com.elster.jupiter.search.SearchablePropertyGroup;
import com.elster.jupiter.time.TimeService;
import com.elster.jupiter.util.beans.BeanService;
import com.elster.jupiter.util.beans.impl.DefaultBeanService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.data.impl.search.SearchableDeviceProperty;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.impl.PropertySpecServiceImpl;
import com.energyict.mdc.sap.soap.webservices.impl.WebServiceActivator;

import java.time.Clock;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class SapAttributesDeviceSearchDomainExtensionTest {

    @Mock
    private TimeService timeService;
    @Mock
    private DataVaultService dataVaultService;
    @Mock
    private DataModel dataModel;
    @Mock
    private OrmService ormService;
    @Mock
    Thesaurus thesaurus;
    @Mock
    private PropertySpecService propertySpecService;
    @Mock
    private Clock clock;
    @Mock
    private WebServiceActivator webServiceActivator;
    
    private BeanService beanService = new DefaultBeanService();
    private SearchableDeviceProperty deviceIdentifierProperty, deviceLocationProperty;

    @Before
    public void setUp() {
        when(ormService.getDataModel(anyString())).thenReturn(Optional.of(dataModel));
        when(ormService.newDataModel(anyString(), anyString())).thenReturn(dataModel);
        com.elster.jupiter.properties.PropertySpecService jupiterPropertySpecService = new com.elster.jupiter.properties.impl.PropertySpecServiceImpl(timeService, this.ormService, this.beanService);
        propertySpecService = new PropertySpecServiceImpl(jupiterPropertySpecService, dataVaultService, ormService);
        NlsMessageFormat messageFormat = mock(NlsMessageFormat.class);
        when(messageFormat.format(anyVararg())).thenReturn(PropertyTranslationKeys.DEVICE_IDENTIFIER.getDefaultFormat(), PropertyTranslationKeys.DEVICE_LOCATION.getDefaultFormat());
        when(thesaurus.getFormat(PropertyTranslationKeys.DEVICE_IDENTIFIER)).thenReturn(messageFormat);
        when(thesaurus.getFormat(PropertyTranslationKeys.DEVICE_LOCATION)).thenReturn(messageFormat);
        SearchablePropertyGroup parentGroup = new SapAttributesSearchablePropertyGroup(thesaurus);
        deviceIdentifierProperty = new DeviceIdentifierSearchableProperty(propertySpecService, thesaurus, ormService).init(parentGroup);
        deviceLocationProperty = new DeviceLocationSearchableProperty(propertySpecService, thesaurus, ormService).init(parentGroup);
        when(webServiceActivator.getSearchableProperties()).thenReturn(Arrays.asList(deviceIdentifierProperty, deviceLocationProperty));
    }

    @Test
    public void testIsExtensionForDevice() {
        SearchDomain domain = mock(SearchDomain.class);
        doReturn(Device.class).when(domain).getDomainClass();

        SearchDomainExtension domainExtension = new SapAttributesDeviceSearchDomainExtension(webServiceActivator, clock);

        Assert.assertTrue(domainExtension.isExtensionFor(domain, Collections.emptyList()));
    }

    @Test
    public void testAsFragmentOneCondition() {
        QueryExecutor queryExecutor = mock(QueryExecutor.class);
        SqlFragment fragment = mock(SqlFragment.class);
        when(fragment.getText()).thenReturn("Fragment1");
        doReturn(fragment).when(queryExecutor).asFragment(any(Condition.class), anyVararg());
        when(this.dataModel.query(any())).thenReturn(queryExecutor);
        SearchablePropertyCondition condition = mock(SearchablePropertyCondition.class);
        doReturn(deviceIdentifierProperty).when(condition).getProperty();

        List<SearchablePropertyCondition> conditions = Arrays.asList(condition);
        SearchDomainExtension domainExtension = new SapAttributesDeviceSearchDomainExtension(webServiceActivator, clock);
        SqlFragment sqlFragment = domainExtension.asFragment(conditions);

        assertThat(sqlFragment.getText()).isEqualTo("Fragment1");
    }

    @Test
    public void testAsFragmentTwoConditions() {
        QueryExecutor queryExecutor = mock(QueryExecutor.class);
        SqlFragment fragment = mock(SqlFragment.class);
        when(fragment.getText()).thenReturn("Fragment1", "Fragment2");
        doReturn(fragment).when(queryExecutor).asFragment(any(Condition.class), anyVararg());
        when(this.dataModel.query(any())).thenReturn(queryExecutor);
        SearchablePropertyCondition condition1 = mock(SearchablePropertyCondition.class);
        SearchablePropertyCondition condition2 = mock(SearchablePropertyCondition.class);
        doReturn(deviceIdentifierProperty).when(condition1).getProperty();
        doReturn(deviceLocationProperty).when(condition2).getProperty();

        List<SearchablePropertyCondition> conditions = Arrays.asList(condition1, condition2);
        SearchDomainExtension domainExtension = new SapAttributesDeviceSearchDomainExtension(webServiceActivator, clock);
        SqlFragment sqlFragment = domainExtension.asFragment(conditions);

        assertThat(sqlFragment.getText()).isEqualTo("Fragment1 INTERSECT Fragment2");
    }
}
