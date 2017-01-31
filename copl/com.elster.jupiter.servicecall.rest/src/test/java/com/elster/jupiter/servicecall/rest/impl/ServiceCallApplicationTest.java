/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.devtools.rest.FelixRestApplicationJerseyTest;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.QueryParameters;
import com.elster.jupiter.properties.rest.PropertyValueInfoService;
import com.elster.jupiter.rest.whiteboard.ReferenceResolver;
import com.elster.jupiter.servicecall.ServiceCallService;

import javax.ws.rs.core.Application;
import java.util.List;

import org.mockito.Mock;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * This is the setup to test all resource in the ServiceCallApplication
 * Created by bvn on 2/11/16.
 */
public abstract class ServiceCallApplicationTest extends FelixRestApplicationJerseyTest {
    @Mock
    public ServiceCallService serviceCallService;
    @Mock
    ReferenceResolver refResolver;
    @Mock
    CustomPropertySetService customPropertySetService;
    @Mock
    PropertyValueInfoService propertyValueInfoService;
    @Mock
    public ReferenceResolver referenceResolver;

    @Override
    protected Application getApplication() {
        ServiceCallApplication serviceCallApplication = new ServiceCallApplication();
        serviceCallApplication.setServiceCallService(serviceCallService);
        serviceCallApplication.setNlsService(nlsService);
        serviceCallApplication.setTransactionService(transactionService);
        serviceCallApplication.setReferenceResolver(referenceResolver);
        serviceCallApplication.setCustomPropertySetService(customPropertySetService);
        serviceCallApplication.setPropertyValueInfoService(propertyValueInfoService);
        return serviceCallApplication;
    }

    <T> Finder<T> mockFinder(List<T> list) {
        Finder<T> finder = mock(Finder.class);

        when(finder.paged(anyInt(), anyInt())).thenReturn(finder);
        when(finder.sorted(anyString(), any(Boolean.class))).thenReturn(finder);
        when(finder.from(any(QueryParameters.class))).thenReturn(finder);
        when(finder.find()).thenReturn(list);
        when(finder.stream()).thenReturn(list.stream());
        return finder;
    }

}
