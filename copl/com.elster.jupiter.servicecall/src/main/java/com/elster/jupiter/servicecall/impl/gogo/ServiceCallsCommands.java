package com.elster.jupiter.servicecall.impl.gogo;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import static java.util.stream.Collectors.toList;

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
    private volatile CustomPropertySetService customPropertySetService;

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

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    public void serviceCallTypes() {
        serviceCallService.getServiceCallTypes().stream()
                .forEach(sct -> System.out.println(sct.getName() + " " + sct.getVersionName() + " custom property sets: " + String
                        .join(" + ", sct.getCustomPropertySets()
                                .stream()
                                .map(RegisteredCustomPropertySet::getCustomPropertySet)
                                .map(CustomPropertySet::getName)
                                .collect(toList()))));
    }

    public void createServiceCallType() {
        System.out.println("Usage: createServiceCallType <name> <version name>");
    }

    public void createServiceCallType(String name, String versionName) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            serviceCallService.createServiceCallType(name, versionName).customPropertySet(customPropertySetService.findActiveCustomPropertySets(ServiceCallType.class).get(0)).add();
            context.commit();
        }
    }
}
