/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservices.demo.impl;

import com.elster.jupiter.appserver.AppServer;
import com.elster.jupiter.appserver.AppService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService.InboundEndPointConfigBuilder;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.Group;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.ws.rs.NotFoundException;

@Component(name = "com.elster.jupiter.webservices.demo",
        service = WebservicesCommands.class,
        property = {
                "osgi.command.scope=wsd",
                "osgi.command.function=createWebserviceDemoData"
        },
        immediate = true)
public class WebservicesCommands {
    public static final String CIM_NAME = "CIM GetMeterReadings";
    public static final String XE_NAME = "Currency exchange";
    public static final String WEAHTER_NAME = "Open weather maps";
    public static final String METER_NAME = "GetMeters";

    private volatile WebServicesService webServicesService;
    private volatile AppService appService;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private volatile UserService userService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;

    @Reference
    public void setWebservicesService(WebServicesService webservicesService) {
        this.webServicesService = webservicesService;
    }

    @Reference
    public void setAppService(AppService appService) {
        this.appService = appService;
    }

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Reference
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    public void createWebserviceDemoData() {
        System.out.println("Usage: createWebserviceDemoData <appserver name>");
    }

    public void createWebserviceDemoData(String appserverName) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {

            AppServer appserver = appService.findAppServer(appserverName).orElseThrow(() -> new IllegalArgumentException("No appserver configured with given name"));
            createCIMendpoint(appserver);
            createCurrencyEndpoint();
            createWeatherEndpoint();
            createMeterEndpoint(appserver);
            context.commit();
        }

    }

    private void createCIMendpoint(AppServer appserver) {
        InboundEndPointConfigBuilder builder = endPointConfigurationService.newInboundEndPointConfiguration("CIM Get meter readings", CIM_NAME, "cim");
        builder.logLevel(LogLevel.INFO);
        builder.traceFile("cim");
        builder.setAuthenticationMethod(EndPointAuthentication.NONE);
        builder.tracing();
        EndPointConfiguration endPointConfiguration = builder.create();
        endPointConfigurationService.activate(endPointConfiguration);
        appserver.supportEndPoint(endPointConfiguration);
        endPointConfiguration.log(LogLevel.INFO, "Request received");
        endPointConfiguration.log(LogLevel.INFO, "Request completed successfully");
        endPointConfiguration.log(LogLevel.INFO, "Request received");
        endPointConfiguration.log(LogLevel.INFO, "Request received");
        endPointConfiguration.log(LogLevel.INFO, "Request completed successfully");
        endPointConfiguration.log(LogLevel.SEVERE, "Request unsuccessful");

    }

    private void createCurrencyEndpoint() {
        EndPointConfigurationService.OutboundEndPointConfigBuilder builder = endPointConfigurationService.newOutboundEndPointConfiguration("Currency exchange", XE_NAME, "http://www.webservicex.net/CurrencyConvertor.asmx?wsdl");
        builder.logLevel(LogLevel.CONFIG);
        builder.setAuthenticationMethod(EndPointAuthentication.NONE);
        builder.schemaValidation();
        EndPointConfiguration endPointConfiguration = builder.create();
        endPointConfigurationService.activate(endPointConfiguration);
    }

    private void createWeatherEndpoint() {
        EndPointConfigurationService.OutboundEndPointConfigBuilder builder = endPointConfigurationService.newOutboundEndPointConfiguration("Open weather maps", WEAHTER_NAME, "http://api.openweathermap.org/data/2.5");
        builder.logLevel(LogLevel.FINEST);
        builder.setAuthenticationMethod(EndPointAuthentication.NONE);
        builder.schemaValidation();
        builder.create();
    }

    private void createMeterEndpoint(AppServer appserver) {
        InboundEndPointConfigBuilder builder = endPointConfigurationService.newInboundEndPointConfiguration("Get meters", METER_NAME, "meters");
        builder.logLevel(LogLevel.SEVERE);
        builder.schemaValidation();
        builder.httpCompression();
        builder.setAuthenticationMethod(EndPointAuthentication.BASIC_AUTHENTICATION);
        Group group = userService.getGroup("Administrators").orElseThrow(() -> new NotFoundException("Unable to find the role ''Administrators''"));
        builder.group(group);
        EndPointConfiguration endPointConfiguration = builder.create();
        endPointConfigurationService.activate(endPointConfiguration);
        appserver.supportEndPoint(endPointConfiguration);
    }
}
