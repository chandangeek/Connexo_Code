package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.soap.whiteboard.cxf.CxfSupportFactory;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.SoapProviderSupportFactory;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;

import com.google.inject.AbstractModule;

/**
 * Created by bvn on 6/7/16.
 */
public class WebServicesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(WebServicesService.class).to(WebServicesServiceImpl.class);
        bind(EndPointConfigurationService.class).to(EndPointConfigurationServiceImpl.class);
        bind(SoapProviderSupportFactory.class).to(CxfSupportFactory.class);
    }
}
