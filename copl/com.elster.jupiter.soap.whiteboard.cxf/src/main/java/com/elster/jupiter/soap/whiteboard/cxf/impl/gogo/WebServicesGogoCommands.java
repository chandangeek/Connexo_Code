package com.elster.jupiter.soap.whiteboard.cxf.impl.gogo;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.LogLevel;
import com.elster.jupiter.soap.whiteboard.WebServicesService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 4/29/16.
 */
@Component(name = "com.elster.jupiter.soap", service = WebServicesGogoCommands.class,
        property = {"osgi.command.scope=ws",
                "osgi.command.function=services",
                "osgi.command.function=endpoints",
                "osgi.command.function=endpoint",
                "osgi.command.function=publish",
                "osgi.command.function=unpublish"
        }, immediate = true)
public class WebServicesGogoCommands {

    private WebServicesService webServicesService;
    private EndPointConfigurationService endPointConfigurationService;
    private ThreadPrincipalService threadPrincipalService;
    private TransactionService transactionService;

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

    public void services() {
        List<List<?>> collect = webServicesService.getWebServices()
                .stream()
                .map(Collections::singletonList)
                .collect(toList());
        collect.add(0, Collections.singletonList("Web service"));
        new MysqlPrint().printTableWithHeader(collect);
    }

    public void endpoints() {
        List<List<?>> collect = endPointConfigurationService.findEndPointConfigurations()
                .stream()
                .map(ep -> Arrays.asList(ep.getName(), ep.getWebServiceName(), ep.getUrl(), ep.isActive()))
                .collect(toList());
        collect.add(0, Arrays.asList("Endpoint", "Web service", "URL", "Active"));
        new MysqlPrint().printTableWithHeader(collect);

    }

    public void endpoint() {
        System.out.println("Create a new endpoint");
        System.out.println("usage: endpoint <name> <webServiceName> <url> <log level>");
    }

    public void endpoint(String name, String webServiceName, String url, String logLevel) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            if (webServicesService.isInbound(webServiceName)) {
                endPointConfigurationService.newInboundEndPointConfiguration(name, webServiceName, url)
                        .logLevel(LogLevel.valueOf(logLevel))
                        .create();
            } else {
                endPointConfigurationService.newOutboundEndPointConfiguration(name, webServiceName, url)
                        .logLevel(LogLevel.valueOf(logLevel))
                        .create();
            }
            context.commit();
        }
    }

    public void publish() {
        System.out.println("usage: publish <end point name>");
    }

    public void publish(String name) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            endPointConfigurationService.getEndPointConfiguration(name)
                    .ifPresent(endPointConfigurationService::activate);
            context.commit();
        }
    }

    public void unpublish() {
        System.out.println("usage: unpublish <end point name>");
    }

    public void unpublish(String name) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            endPointConfigurationService.getEndPointConfiguration(name)
                    .ifPresent(endPointConfigurationService::deactivate);
            context.commit();
        }
    }
}
