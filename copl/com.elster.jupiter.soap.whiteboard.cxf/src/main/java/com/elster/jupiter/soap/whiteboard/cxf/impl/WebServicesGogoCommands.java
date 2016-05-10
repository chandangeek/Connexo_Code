package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.EndPointConfigurationService;
import com.elster.jupiter.soap.whiteboard.LogLevel;
import com.elster.jupiter.soap.whiteboard.WebServicesService;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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
        webServicesService.getWebServices().stream().forEach(System.out::println);
    }

    public void endpoints() {
        endPointConfigurationService.findEndPointConfigurations()
                .stream()
                .forEach(ep -> System.out.println(ep.getName() + (ep.isActive() ? " serving " : " will serve ") + ep.getWebServiceName() + " on " + ep
                        .getUrl()));
    }

    public void endpoint() {
        System.out.println("Create a new endpoint");
        System.out.println("usage: endpoint <name> <webServiceName> <url> <log level>");
    }

    public void endpoint(String name, String webServiceName, String url, String logLevel) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            if (webServicesService.isInbound(webServiceName)) {
                endPointConfigurationService.newInboundEndPoint(name, webServiceName, url)
                        .logLevel(LogLevel.valueOf(logLevel))
                        .create();
            } else {
                endPointConfigurationService.newOutboundEndPoint(name, webServiceName, url)
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
//        endPointConfigurationService.getEndPointConfiguration(name).ifPresent(endPointConfigurationService::activate);
        endPointConfigurationService.getEndPointConfiguration(name).ifPresent(
                ep -> webServicesService.publishEndPoint(ep)
        );
    }

    public void unpublish(String name) {
//        endPointConfigurationService.getEndPointConfiguration(name).ifPresent(endPointConfigurationService::deactivate);
        endPointConfigurationService.getEndPointConfiguration(name).ifPresent(
                epc -> webServicesService.removeEndPoint(epc)
        );

    }
}
