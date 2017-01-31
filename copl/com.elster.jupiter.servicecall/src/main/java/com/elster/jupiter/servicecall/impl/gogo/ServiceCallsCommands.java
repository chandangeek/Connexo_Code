/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
import com.elster.jupiter.servicecall.ServiceCallLog;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.servicecall.ServiceCallTypeBuilder;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.gogo.MysqlPrint;

import com.google.common.collect.Lists;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

/**
 * Created by bvn on 2/12/16.
 */
@Component(name = "com.elster.jupiter.servicecall.gogo", service = ServiceCallsCommands.class,
        property = {"osgi.command.scope=scs",
                "osgi.command.function=serviceCallTypes",
                "osgi.command.function=createServiceCallType",
                "osgi.command.function=deprecateServiceCallType",
                "osgi.command.function=removeServiceCallType",
                "osgi.command.function=customPropertySets",
                "osgi.command.function=createServiceCallLifeCycle",
                "osgi.command.function=transitionServiceCall",
                "osgi.command.function=handlers",
                "osgi.command.function=serviceCallLifeCycles",
                "osgi.command.function=createServiceCall",
                "osgi.command.function=createChildServiceCall",
                "osgi.command.function=createServiceCallLifeCycle",
                "osgi.command.function=serviceCall",
                "osgi.command.function=serviceCalls",
                "osgi.command.function=log",
                "osgi.command.function=deleteServiceCallLifeCycle",
                "osgi.command.function=hierarchy",
                "osgi.command.function=deleteServiceCall",
                "osgi.command.function=cancel"
        }, immediate = true)
public class ServiceCallsCommands {

    private volatile ServiceCallService serviceCallService;
    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile CustomPropertySetService customPropertySetService;

    private final MysqlPrint mysqlPrint = new MysqlPrint();

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
                System.out.print("[" + serviceCallType.getId() + "] " + serviceCallType.getName() + " " + serviceCallType
                        .getVersionName() + " ");
                System.out.println(" cps: [" + String
                        .join(" + ", serviceCallType.getCustomPropertySets()
                                .stream()
                                .map(RegisteredCustomPropertySet::getCustomPropertySet)
                                .map(CustomPropertySet::getName)
                                .collect(toList())) + "] handled by " + serviceCallType.getServiceCallHandler().getDisplayName());
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

    public void createServiceCallType() {
        System.out.println("Usage: createServiceCallType <name> <version name> <log level> <handler> [life cycle name] <cps ids> ]");
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

    public void deprecateServiceCallType(String name, String versionName) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallType serviceCallType = serviceCallService.findServiceCallType(name, versionName)
                    .orElseThrow(NoSuchElementException::new);
            serviceCallType.deprecate();
            serviceCallType.save();
            context.commit();
        }
    }

    public void serviceCall() {
        System.out.println("Usage: serviceCall <id>");
    }

    public void serviceCall(long id) {
        ServiceCall sc = serviceCallService.getServiceCall(id)
                .orElseThrow(() -> new IllegalArgumentException("No such service call"));
        System.out.println(sc.getNumber() + " "
                        + sc.getState().getKey() + " " + sc.getType().getName() + " "
                + sc.getParent().map(p -> p.getNumber()).orElse("[no parent]") + " "
                + sc.getOrigin().orElse("[no orig]") + " "
                + sc.getExternalReference().orElse("[no ext ref]"));
        for (ServiceCallLog log : sc.getLogs().find()) {
            System.out.println("   " + log.getTime() + " [" + log.getLogLevel() + "] " + log.getMessage());
            if (log.getStackTrace() != null) {
                System.out.println("\t" + log.getStackTrace());
            }
        }
    }

    public void serviceCalls() {
        serviceCallService.getServiceCallFinder().find()
                .stream()
                .sorted(Comparator.comparing(ServiceCall::getId))
                .map(sc -> sc.getNumber() + " "
                + sc.getState().getKey() + " " + sc.getType().getName() + " "
                + sc.getParent().map(p -> p.getNumber()).orElse("[no parent]") + " "
                + sc.getOrigin().orElse("[no orig]") + " "
                + sc.getExternalReference().orElse("[no ext ref]"))
                .forEach(System.out::println);
    }

    public void transitionServiceCall() {
        System.out.println("Usage: transitionServiceCall <device id> <targetState>");
        System.out.println("   where targetState is one of: " + Stream.of(DefaultState.values())
                .map(DefaultState::name)
                .collect(joining(", ")));
    }

    public void transitionServiceCall(long id, String targetState) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            ServiceCall sc = serviceCallService.getServiceCall(id)
                    .orElseThrow(() -> new IllegalArgumentException("No such service call"));
            sc.requestTransition(DefaultState.valueOf(targetState));
            context.commit();
        }
    }

    public void removeServiceCallType(String name, String versionName) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            serviceCallService.findServiceCallType(name, versionName).get().delete();
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
        mysqlPrint.printTableWithHeader(Collections.singletonList("Service call handler name"),
                serviceCallService.findAllHandlers().stream().sorted().map(Collections::singletonList).collect(toList()));
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

    public void createServiceCall() {
        System.out.println("Usage: createServiceCall <type> <typeVersion> <externalReference>");
    }

    public void createServiceCall(String type, String typeVersion, String externalReference) {
        threadPrincipalService.set(() -> "Console");
        Optional<ServiceCallType> serviceCallType = serviceCallService.findServiceCallType(type, typeVersion);
        if (!serviceCallType.isPresent()) {
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
        System.out.println("Usage: createChildServiceCall <type> <typeVersion> <externalReference> <parentId>");
    }

    public void createChildServiceCall(String type, String typeVersion, String externalReference, Long parent) {
        threadPrincipalService.set(() -> "Console");
        Optional<ServiceCallType> serviceCallType = serviceCallService.findServiceCallType(type, typeVersion);
        Optional<ServiceCall> serviceCall = serviceCallService.getServiceCall(parent);
        if (!serviceCall.isPresent()) {
            System.out.println("There is no parent service call with the reference '" + parent + "'.");
        } else if (!serviceCallType.isPresent()) {
            System.out.println("There is no service call type with name: '" + type + "' and version: '" + typeVersion + "'");
        } else {
            ServiceCallType scType = serviceCallType.get();
            ServiceCall call = serviceCall.get();

            try (TransactionContext context = transactionService.getContext()) {
                ServiceCall child = call.newChildCall(scType)
                        .externalReference(externalReference)
                        .create();
                context.commit();
                System.out.println("Child service call of '" + parent + "' with reference '" + child.getNumber() + "' has been created");
            }
        }
    }

    public void log() {
        System.out.println("Usage: log <service call id> <log level> <message>");
        System.out.println("e.g.   log 7231 FINE That looks good to me");
    }

    public void log(long id, String level, String... messageParts) {
        try (TransactionContext context = transactionService.getContext()) {
            ServiceCall serviceCall = serviceCallService.getServiceCall(id)
                    .orElseThrow(() -> new IllegalArgumentException("No such service call"));
            serviceCall.log(LogLevel.valueOf(level), String.join(" ", messageParts));
            context.commit();
        }
    }

    public void deleteServiceCallLifeCycle() {
        System.out.println("Usage: deleteServiceCallLifeCycle <name>");
    }

    public void deleteServiceCallLifeCycle(String name) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            serviceCallService.getServiceCallLifeCycle(name)
                    .orElseThrow(() -> new IllegalStateException("Nu such service call life cycle"))
                    .delete();
            context.commit();
        }
    }

    public void hierarchy() {
        System.out.println("Usage: <service call type name> <service call type version> <# top level> [ < # level 2 > [ < # level 3 > [ < # level 4 > [ < # level 5 > ]]]]");
        System.out.println("  Creates a hierarchy of service calls, with the requested number of service calls on each level");
    }


    public void hierarchy(String name, String version, long... levels) {
        threadPrincipalService.set(() -> "Console");
        new HierarchyCreator(name, version, levels).create();
    }

    public void deleteServiceCall() {
        System.out.println("usage: deleteServiceCall <service call id>");
        System.out.println("    Deletes the service call, the service call's children and all logs of both the mentioned service call and all children");
    }

    public void deleteServiceCall(long serviceCallId) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            serviceCallService.getServiceCall(serviceCallId)
                    .orElseThrow(() -> new IllegalArgumentException("No such service call"))
                    .delete();
            context.commit();
        }
    }

    public void cancel() {
        System.out.println("usage: cancel <service call id>");
        System.out.println("    Cancel the service call");
    }

    public void cancel(long serviceCallId) {
        threadPrincipalService.set(() -> "Console");
        try (TransactionContext context = transactionService.getContext()) {
            serviceCallService.getServiceCall(serviceCallId)
                    .orElseThrow(() -> new IllegalArgumentException("No such service call"))
                    .cancel();
            context.commit();
        }
    }

    class HierarchyCreator {
        private double total;
        private double created;

        private long[] levels;
        private String name;
        private String version;

        public HierarchyCreator(String name, String version, long... levels) {
            this.levels = levels;
            this.name = name;
            this.version = version;
            total = Lists.reverse(LongStream.of(levels).boxed().collect(toList()))
                    .stream()
                    .reduce(0L, (a, b) -> (a + 1) * b);
        }

        public void create() {
            ServiceCallType serviceCallType = serviceCallService.findServiceCallType(name, version)
                    .orElseThrow(() -> new IllegalStateException("No such service call type"));

            System.out.println(String.format("Creating a total of %.0f service calls", total));

            if (levels.length > 0) {
                LongStream.range(0, levels[0]).forEach(i -> {
                    try (TransactionContext context = transactionService.getContext()) {
                    ServiceCall serviceCall = serviceCallType.newServiceCall().create();
                        created++;
                        if (levels.length >= 1) {
                            long[] subLevels = new long[levels.length - 1];
                            System.arraycopy(levels, 1, subLevels, 0, levels.length - 1);
                            createChildren(serviceCall, subLevels);
                            context.commit();
                        }
                    }
                    printUpdate();
                });
            }
            System.out.println("\nDone");
        }

        private void createChildren(ServiceCall serviceCall, long[] levels) {
            for (int index = 0; index < levels[0]; index++) {
                ServiceCall child = serviceCall.newChildCall(serviceCall.getType()).create();
                created++;
                if (levels.length > 1) {
                    long[] subLevels = new long[levels.length - 1];
                    System.arraycopy(levels, 1, subLevels, 0, levels.length - 1);
                    createChildren(child, subLevels);
                }
                printUpdate();
            }
        }

        private void printUpdate() {
            System.out.print(String.format("\r %2.1f %%      ", (100 * created) / total));
        }
    }
}
