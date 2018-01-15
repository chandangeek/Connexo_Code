/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.CxfSupportFactory;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.SoapProviderSupportFactory;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.osgi.service.http.HttpService;

/**
 * Created by bvn on 6/7/16.
 */
public class WebServicesModule extends AbstractModule {

    @Override
    protected void configure() {
        requireBinding(HttpService.class);

        bind(WebServicesService.class).to(WebServicesServiceImpl.class).in(Scopes.SINGLETON);
        bind(EndPointConfigurationService.class).to(EndPointConfigurationServiceImpl.class).in(Scopes.SINGLETON);
        bind(SoapProviderSupportFactory.class).to(CxfSupportFactory.class).in(Scopes.SINGLETON);
    }
}
