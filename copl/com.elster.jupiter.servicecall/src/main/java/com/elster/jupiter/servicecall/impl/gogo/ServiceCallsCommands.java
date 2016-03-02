package com.elster.jupiter.servicecall.impl.gogo;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.*;
import com.elster.jupiter.servicecall.impl.IServiceCallType;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 2/12/16.
 */
@Component(name = "com.elster.jupiter.servicecall.gogo", service = ServiceCallsCommands.class,
        property = {"osgi.command.scope=scs",
                "osgi.command.function=serviceCallTypes",
                "osgi.command.function=createServiceCallType",
                "osgi.command.function=customPropertySets",
                "osgi.command.function=createServiceCallLifeCycle",
                "osgi.command.function=serviceCall",
                "osgi.command.function=createServiceCall",
                "osgi.command.function=createChildServiceCall"
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
        System.out.println("Usage: createServiceCallType <name> <version name> <optional:log level> <optional: life cycle name> <optional:cps ids>");
    }

    public void createServiceCallType(String name, String versionName) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            serviceCallService.createServiceCallType(name, versionName).create();
            context.commit();
        }
    }

    public void createServiceCallType(String name, String versionName, String logLevel, Long... cpsIds) {
        List<Long> ids = Arrays.asList(cpsIds);
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallTypeBuilder builder = serviceCallService.createServiceCallType(name, versionName).logLevel(LogLevel.valueOf(logLevel));

            customPropertySetService.findActiveCustomPropertySets().stream()
                    .filter(cps -> ids.contains(cps.getId()))
                    .forEach(builder::customPropertySet);
            builder.create();
            context.commit();
        }
    }

    public void createServiceCallType(String name, String versionName, String logLevel, String lifeCycleName, Long... cpsIds) {
        List<Long> ids = Arrays.asList(cpsIds);
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallLifeCycle serviceCallLifeCycle = serviceCallService.getServiceCallLifeCycle(lifeCycleName).orElseThrow(() -> new NoSuchElementException("No service call life cycle with name: " + lifeCycleName));
            ServiceCallTypeBuilder builder = serviceCallService.createServiceCallType(name, versionName, serviceCallLifeCycle).logLevel(LogLevel.valueOf(logLevel));

            customPropertySetService.findActiveCustomPropertySets().stream()
                    .filter(cps -> ids.contains(cps.getId()))
                    .forEach(builder::customPropertySet);
            builder.create();
            context.commit();
        }
    }

    public void serviceCall(long id) {
        serviceCallService.getServiceCall(id)
                .map(sc -> sc.getNumber() + " "
                        + sc.getState().getKey() + " " + sc.getType().getName() + " "
                        + sc.getParent().map(p -> p.getNumber()).orElse("-P-") + " "
                        + sc.getOrigin().orElse("-O-") + " "
                        + sc.getExternalReference().orElse("-E-"))
                .ifPresent(System.out::println);
    }

    public void createServiceCall(String typeName, String typeVersion, String origin, String externalReference) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            serviceCallService.getServiceCallTypes().find().stream()
                    .filter(sct -> sct.getName().equals(typeName) && sct.getVersionName().equals(typeVersion))
                    .findFirst()
                    .map(sct -> sct.newServiceCall()
                            .origin(origin)
                            .externalReference(externalReference)
                            .create())
                    .map(sc -> sc.getId())
                    .ifPresent(System.out::println);

            context.commit();
        }
    }

    public void createServiceCall(long parentId, String typeName, String typeVersion, String origin, String externalReference) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            Optional<ServiceCall> parent = serviceCallService.getServiceCall(parentId);
            Optional<ServiceCallType> type = serviceCallService.getServiceCallTypes().find().stream()
                    .filter(sct -> sct.getName().equals(typeName) && sct.getVersionName().equals(typeVersion))
                    .findFirst();

            parent.orElseThrow(() -> new NoSuchElementException("No service call with id: " + parentId))
                    .newChildCall(type.orElseThrow(() -> new NoSuchElementException("No service call type with name: " + typeName + " and version: " + typeVersion)))
                    .origin(origin)
                    .externalReference(externalReference)
                    .create();
            context.commit();
        }
    }

    public void customPropertySets() {
        customPropertySetService.findActiveCustomPropertySets().stream()
                .map(cps -> cps.getId() + " " + cps.getCustomPropertySet().getDomainClass() + " " + cps.getCustomPropertySet().getName())
                .forEach(System.out::println);
    }

    public void createServiceCallLifeCycle() {
        System.out.println("Usage: createServiceCallLifeCycle <name> <optional:operations>");
        System.out.println("Operations: removeState:<state> removeTransition:<fromState>:<toState>");
    }

    public void createServiceCallLifeCycle(String name, String... operations) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallLifeCycleBuilder serviceCallLifeCycle = serviceCallService.createServiceCallLifeCycle(name);
            for (String operation : operations) {
                if (operation.contains("removeState:")) {
                    serviceCallLifeCycle.remove(DefaultState.valueOf(operation.split(":")[1]));
                } else if (operation.contains("removeTransition:")) {
                    serviceCallLifeCycle.removeTransition(DefaultState.valueOf(operation.split(":")[1]), DefaultState.valueOf(operation.split(":")[2]));
                }
            }
            serviceCallLifeCycle.create();
            context.commit();
        }
    }

    public void createServiceCall() {
        System.out.println("Usage: createServiceCall <type> <typeVersion> <externalReference>");
    }

    public void createServiceCall(String type, String typeVersion, String externalReference) {
        Optional<ServiceCallType> serviceCallType = serviceCallService.findServiceCallType(type, typeVersion);
        if(!serviceCallType.isPresent()) {
            System.out.println("There is no service call type with name: '" + type + "' and version: '" + typeVersion + "'");
        } else {
            try (TransactionContext context = transactionService.getContext()) {
                ServiceCall serviceCall = serviceCallType.get()
                        .newServiceCall()
                        .externalReference(externalReference)
                        .create();
                context.commit();

                System.out.println("Service call with reference '" + serviceCall.getNumber() + "' has been created");
            }
        }
    }

    public void createChildServiceCall() {
        System.out.println("Usage: createChildServiceCall <type> <typeVersion> <externalReference> <parentReference>");
    }

    public void createChildServiceCall(String type, String typeVersion, String externalReference, String parent) {
        Optional<ServiceCallType> serviceCallType = serviceCallService.findServiceCallType(type, typeVersion);
        Optional<ServiceCall> serviceCall = serviceCallService.getServiceCall(parent);
        if(!serviceCall.isPresent()) {
            System.out.println("There is no parent service call with the reference '" + parent + "'.");
            return;
        } else if(!serviceCallType.isPresent()) {
            System.out.println("There is no service call type with name: '" + type + "' and version: '" + typeVersion + "'");
            return;
        } else {
            ServiceCallType scType = serviceCallType.get();
            ServiceCall call = serviceCall.get();

            try (TransactionContext context = transactionService.getContext()) {
                ServiceCall child = call.newChildCall(scType)
                    .externalReference(externalReference)
                    .create();
                context.commit();
                System.out.println("Child service call of '" + parent +"' with reference '" + child.getNumber() + "' has been created");
            }
        }
    }
}
