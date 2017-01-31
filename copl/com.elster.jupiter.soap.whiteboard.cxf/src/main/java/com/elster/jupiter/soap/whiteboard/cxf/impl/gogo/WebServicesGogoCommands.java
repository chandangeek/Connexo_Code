/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.soap.whiteboard.cxf.impl.gogo;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointAuthentication;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.gogo.MysqlPrint;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 4/29/16.
 */
@Component(name = "com.elster.jupiter.soap", service = WebServicesGogoCommands.class,
        property = {"osgi.command.scope=ws",
                "osgi.command.function=webservices",
                "osgi.command.function=endpoints",
                "osgi.command.function=createEndpoint",
                "osgi.command.function=activate",
                "osgi.command.function=deactivate",
                "osgi.command.function=log",
                "osgi.command.function=endpoint"
        }, immediate = true)
public class WebServicesGogoCommands {

    public static final MysqlPrint MYSQL_PRINT = new MysqlPrint();
    private volatile WebServicesService webServicesService;
    private volatile EndPointConfigurationService endPointConfigurationService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile TransactionService transactionService;
    private volatile Clock clock;

    @Reference
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    @Reference
    public void setWebServicesService(WebServicesService webServicesService) {
        this.webServicesService = webServicesService;
    }

    @Reference
    public void setEndPointConfigurationService(EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    public void webservices() {
        List<List<?>> collect = webServicesService.getWebServices()
                .stream()
                .map(ws -> Arrays.asList(ws.getName(), ws.isInbound() ? "Inbound" : "Outbound", ws.getProtocol()))
                .collect(toList());
        collect.add(0, Arrays.asList("Web service", "Direction", "Type"));
        MYSQL_PRINT.printTableWithHeader(collect);
    }

    public void endpoints() {
        List<List<?>> collect = endPointConfigurationService.findEndPointConfigurations()
                .stream()
                .map(ep -> Arrays.asList(ep.getName(), ep.getWebServiceName(), ep.getUrl(), ep.isActive(), webServicesService
                        .isPublished(ep)))
                .collect(toList());
        collect.add(0, Arrays.asList("Endpoint", "Web service", "URL", "Active", "Published"));
        MYSQL_PRINT.printTableWithHeader(collect);

    }

    public void createEndpoint() {
        System.out.println("Create a new endpoint");
        System.out.println("usage: createEndpoint <name> <webServiceName> <url> <log level>");
    }

    public void createEndpoint(String name, String webServiceName, String url, String logLevel) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            if (webServicesService.isInbound(webServiceName)) {
                endPointConfigurationService.newInboundEndPointConfiguration(name, webServiceName, url)
                        .logLevel(LogLevel.valueOf(logLevel))
                        .setAuthenticationMethod(EndPointAuthentication.NONE)
                        .traceFile(webServiceName + "TraceFile.txt")
                        .create();
            } else {
                endPointConfigurationService.newOutboundEndPointConfiguration(name, webServiceName, url)
                        .logLevel(LogLevel.valueOf(logLevel))
                        .setAuthenticationMethod(EndPointAuthentication.NONE)
                        .traceFile(webServiceName + "TraceFile.txt")
                        .create();
            }
            context.commit();
        }
    }

    public void activate() {
        System.out.println("usage: activate <end point name>");
    }

    public void activate(String name) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            endPointConfigurationService.getEndPointConfiguration(name)
                    .ifPresent(endPointConfigurationService::activate);
            context.commit();
        }
    }

    public void deactivate() {
        System.out.println("usage: deactivate <end point name>");
    }

    public void deactivate(String name) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            endPointConfigurationService.getEndPointConfiguration(name)
                    .ifPresent(endPointConfigurationService::deactivate);
            context.commit();
        }
    }

    public void log() {
        System.out.println("Usage: log <end point name> <log level> <message>");
        System.out.println("e.g.   log CIM1 FINE That looks good to me");
    }

    public void log(String endPoint, String level, String... messageParts) {
        try (TransactionContext context = transactionService.getContext()) {
            EndPointConfiguration endPointConfiguration = endPointConfigurationService.getEndPointConfiguration(endPoint)
                    .orElseThrow(() -> new IllegalArgumentException("No such end point"));
            endPointConfiguration.log(LogLevel.valueOf(level), String.join(" ", messageParts));
            context.commit();
        }
    }

    public void endpoint() {
        System.out.println("Print configuration and logging for the endpoint");
        System.out.println("  Usage: endpoint <end point name>");
        System.out.println("  e.g.   endpoint Metering");
    }

    public void endpoint(String... nameParts) {
        String name = String.join(" ", nameParts);
        Optional<EndPointConfiguration> endPointConfiguration = endPointConfigurationService.getEndPointConfiguration(name);
        if (!endPointConfiguration.isPresent()) {
            System.err.println("No such end point configuration");
            return;
        }
        System.out.println("Log level      : " + endPointConfiguration.get().getLogLevel());
        System.out.println("URL            : " + endPointConfiguration.get().getUrl());
        System.out.println("Web service    : " + endPointConfiguration.get().getWebServiceName());
        System.out.println("Direction      : " + (endPointConfiguration.get().isInbound() ? "Inbound" : "Outbound"));
        System.out.println("Traced         : " + (endPointConfiguration.get().isTracing() ? "Tracing" : "No tracing"));
        System.out.println("Status         : " + (endPointConfiguration.get().isActive() ? "Active" : "Inactive"));
        System.out.println("Compression    : " + (endPointConfiguration.get().isHttpCompression() ? "GZIP" : "None"));
        System.out.println("Validation     : " + (endPointConfiguration.get()
                .isSchemaValidation() ? "Schema validation" : "No validation"));
        System.out.println("Authentication : " + endPointConfiguration.get().getAuthenticationMethod());
        DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.FULL)
                .withLocale(Locale.getDefault())
                .withZone(clock.getZone());
        for (EndPointLog log : endPointConfiguration.get().getLogs().sorted("timestamp", true).find()) {
            System.out.println(String.format("%s\t %s", formatter.format(log.getTime()), log.getMessage()));
            if (log.getStackTrace() != null) {
                System.out.println("\t" + log.getStackTrace());
            }
        }
    }
}
