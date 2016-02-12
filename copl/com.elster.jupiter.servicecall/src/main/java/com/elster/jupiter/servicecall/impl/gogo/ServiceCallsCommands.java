package com.elster.jupiter.servicecall.impl.gogo;

import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.inject.Inject;

/**
 * Created by bvn on 2/12/16.
 */
@Component(name = "com.elster.jupiter.servicecall.gogo", service = ServiceCallsCommands.class,
        property = {"osgi.command.scope=scs",
                "osgi.command.function=serviceCallTypes",
                "osgi.command.function=createServiceCallType"
        }, immediate = true)
public class ServiceCallsCommands {

    private volatile ServiceCallService serviceCallService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Reference
    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    public void serviceCallTypes() {
        serviceCallService.getServiceCallTypes().stream().forEach(sct -> System.out.println(sct.getName()+" "+sct.getVersionName()));
    }

    public void createServiceCallType() {
        System.out.println("Usage: createServiceCallType <name> <version name>");
    }

    public void createServiceCallType(String name, String versionName) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            serviceCallService.createServiceCallType(name, versionName).add();
            context.commit();
        }
    }
}
