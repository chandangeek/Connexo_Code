package com.elster.jupiter.servicecall.impl.gogo;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;
import com.elster.jupiter.servicecall.ServiceCallLifeCycleBuilder;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.servicecall.ServiceCallTypeBuilder;
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
                "osgi.command.function=handlers",
                "osgi.command.function=serviceCallLifeCycles",
                "osgi.command.function=createServiceCallLifeCycle",
                "osgi.command.function=serviceCall",
                "osgi.command.function=createServiceCall"
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
        for (ServiceCallType serviceCallType : serviceCallService.getServiceCallTypes().find()) {
            try {
                System.out.print(serviceCallType.getName() + " " + serviceCallType.getVersionName() + " ");
                System.out.println(" cps: [" + String
                        .join(" + ", serviceCallType.getCustomPropertySets()
                                .stream()
                                .map(RegisteredCustomPropertySet::getCustomPropertySet)
                                .map(CustomPropertySet::getName)
                                .collect(toList())) + "] handled by " + serviceCallType.getServiceCallHandler()
                        .getClass()
                        .getSimpleName());
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    public void createServiceCallType() {
        System.out.println("Usage: createServiceCallType <name> <version name> [ <log level> <handler> [life cycle name] <cps ids> ]");
    }

    public void createServiceCallType(String name, String versionName) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            serviceCallService.createServiceCallType(name, versionName)
                    .customPropertySet(customPropertySetService.findActiveCustomPropertySets(ServiceCallType.class)
                            .get(0))
                    .handler("DisconnectHandler1")
                    .create();
            context.commit();
        }
    }

    public void createServiceCallType(String name, String versionName, String logLevel, String handler, Long... cpsIds) {
        List<Long> ids = Arrays.asList(cpsIds);
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallTypeBuilder builder = serviceCallService
                    .createServiceCallType(name, versionName)
                    .handler(handler)
                    .logLevel(LogLevel.valueOf(logLevel));

            customPropertySetService.findActiveCustomPropertySets().stream()
                    .filter(cps -> ids.contains(cps.getId()))
                    .forEach(builder::customPropertySet);
            builder.create();
            context.commit();
        }
    }

    public void createServiceCallType(String name, String versionName, String logLevel, String handler, String lifeCycleName, Long... cpsIds) {
        List<Long> ids = Arrays.asList(cpsIds);
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallLifeCycle serviceCallLifeCycle = serviceCallService.getServiceCallLifeCycle(lifeCycleName)
                    .orElseThrow(() -> new NoSuchElementException("No service call life cycle with name: " + lifeCycleName));
            ServiceCallTypeBuilder builder = serviceCallService.createServiceCallType(name, versionName, serviceCallLifeCycle)
                    .logLevel(LogLevel.valueOf(logLevel))
                    .handler(handler);

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
                .map(cps -> cps.getId() + " " + cps.getCustomPropertySet()
                        .getDomainClass() + " " + cps.getCustomPropertySet().getName())
                .forEach(System.out::println);
    }

    public void handlers() {
        serviceCallService.findAllHandlers().stream().forEach(System.out::println);
    }

    public void createServiceCallLifeCycle() {
        System.out.println("Usage: createServiceCallLifeCycle <name> <optional:operations>");
        System.out.println("Operations: removeState:<state> removeTransition:<fromState>:<toState>");
    }

    public void serviceCallLifeCycles() {
        serviceCallService.getServiceCallLifeCycles()
                .stream()
                .forEach(lc -> System.out.println(String.format("%d %s", lc.getId(), lc.getName())));
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
}
