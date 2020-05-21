/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.CxfSupportFactory;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.SoapProviderSupportFactory;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import org.osgi.service.http.HttpService;

import java.util.function.Function;

/**
 * Created by bvn on 6/7/16.
 */
public class WebServicesModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(HttpService.class);

        bind(WebServicesDataModelService.class).to(WebServicesDataModelServiceImpl.class).in(Scopes.SINGLETON);
        bind(WebServicesService.class).toProvider(provide(WebServicesDataModelService::getWebServicesService)).in(Scopes.SINGLETON);
        bind(EndPointConfigurationService.class).toProvider(provide(WebServicesDataModelService::getEndPointConfigurationService)).in(Scopes.SINGLETON);
        bind(SoapProviderSupportFactory.class).to(CxfSupportFactory.class).in(Scopes.SINGLETON);
        bind(WebServiceCallOccurrenceService.class).toProvider(provide(WebServicesDataModelService::getWebServiceCallOccurrenceService)).in(Scopes.SINGLETON);
    }

    private static <T> Provider<T> provide(Function<WebServicesDataModelService, T> method) {
        return new Provider<T>() {
            @Inject
            private Provider<WebServicesDataModelService> webServicesDataModelServiceProvider;

            @Override
            public T get() {
                return method.apply(webServicesDataModelServiceProvider.get());
            }
        };
    }
}
