/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.servicecall.examples;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallBuilder;
import com.elster.jupiter.servicecall.ServiceCallLifeCycle;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.servicecall.ServiceCallTypeBuilder;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.protocol.api.TrackingCategory;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Created by bvn on 3/4/16.
 */
@Component(name = "com.energyict.mdc.servicecall.examples", service = ServiceCallCommands.class,
        property = {"osgi.command.scope=sch",
                "osgi.command.function=updateYearOfCertification",
                "osgi.command.function=updateYearOfCertificationGroup",
                "osgi.command.function=hook",
                "osgi.command.function=crash",
                "osgi.command.function=createTrackedCommand",
                "osgi.command.function=createDeviceServiceCall",
                "osgi.command.function=createServiceCallDemoData"
        }, immediate = true)
public class ServiceCallCommands {
    static final String HANDLER_NAME = "yearUpdater";
    static final String HANDLER_VERSION_NAME = "v1.0";
    private static final String GROUP_HANDLER_NAME = "yearGroupUpdater";
    private static final String GROUP_HANDLER_VERSION_NAME = "v1.0";
    private static final String CAPTAIN_HOOK_HANDLER_NAME = "captainHook";
    private static final String CAPTAIN_HOOK_HANDLER_VERSION_NAME = "v1.0";
    private static final String NULL_POINTER_HANDLER_NAME = "NullPointer";
    private static final String NULL_POINTER_HANDLER_VERSION_NAME = "v1.0";
    private volatile ServiceCallService serviceCallService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile DeviceService deviceService;

    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile MeteringGroupsService meteringGroupService;
    private volatile MeteringService meteringService;

    @Reference
    public void setMeteringGroupService(MeteringGroupsService meteringGroupService) {
        this.meteringGroupService = meteringGroupService;
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
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Reference
    public void setServiceCallService(ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
        this.customPropertySetService = customPropertySetService;
    }

    @Reference
    public void setMeteringService(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    public void updateYearOfCertification() {
        System.out.println("Usage: updateYearOfCertification <device id> <year>");
    }

    public void updateYearOfCertification(long deviceId, long updateYearOfCertification) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {

            RegisteredCustomPropertySet customPropertySet = customPropertySetService.findActiveCustomPropertySets(ServiceCall.class)
                    .stream()
                    .filter(cps -> cps.getCustomPropertySet()
                            .getName()
                            .equals(DeviceCertificationCustomPropertySet.class.getSimpleName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Could not find my custom property set"));

            ServiceCallType serviceCallType = serviceCallService.findServiceCallType(HANDLER_NAME, HANDLER_VERSION_NAME)
                    .orElseGet(() -> serviceCallService.createServiceCallType(HANDLER_NAME, HANDLER_VERSION_NAME)
                            .handler("DeviceCertificationHandler")
                            .logLevel(LogLevel.FINEST)
                            .customPropertySet(customPropertySet)
                            .create());

            Optional<Device> device = deviceService.findDeviceById(deviceId);
            DeviceCertificationDomainExtension deviceCertificationDomainExtension = new DeviceCertificationDomainExtension();
            deviceCertificationDomainExtension.setYearOfCertification(updateYearOfCertification);
            deviceCertificationDomainExtension.setDeviceId(deviceId);
            if (device.isPresent()) {
                ServiceCall serviceCall = serviceCallType.newServiceCall()
                        .origin("Gogo")
                        .extendedWith(deviceCertificationDomainExtension)
                        .targetObject(device.get())
                        .create();
                serviceCall.requestTransition(DefaultState.PENDING);
            } else {
                ServiceCall serviceCall = serviceCallType.newServiceCall()
                        .origin("Gogo")
                        .extendedWith(deviceCertificationDomainExtension)
                        .create();
                serviceCall.log(LogLevel.SEVERE, "Device could not be found");
                serviceCall.requestTransition(DefaultState.REJECTED);
            }
            context.commit();
        }
    }

    public void updateYearOfCertificationGroup() {
        System.out.println("Usage: updateYearOfCertificationGrop <device group id> <year>");
    }

    public void updateYearOfCertificationGroup(long deviceGroupId, long updateYearOfCertification) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {

            RegisteredCustomPropertySet customPropertySet = customPropertySetService.findActiveCustomPropertySets(ServiceCall.class)
                    .stream()
                    .filter(cps -> cps.getCustomPropertySet()
                            .getName()
                            .equals(DeviceGroupCertificationCustomPropertySet.class.getSimpleName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Could not find my custom property set"));

            ServiceCallType serviceCallType = serviceCallService.findServiceCallType(GROUP_HANDLER_NAME, GROUP_HANDLER_VERSION_NAME)
                    .orElseGet(() -> serviceCallService.createServiceCallType(GROUP_HANDLER_NAME, GROUP_HANDLER_VERSION_NAME)
                            .handler("DeviceGroupCertificationHandler")
                            .logLevel(LogLevel.FINEST)
                            .customPropertySet(customPropertySet)
                            .create());

            Optional<EndDeviceGroup> endDeviceGroup = meteringGroupService.findEndDeviceGroup(deviceGroupId);
            DeviceGroupCertificationDomainExtension deviceCertificationDomainExtension = new DeviceGroupCertificationDomainExtension();
            deviceCertificationDomainExtension.setYearOfCertification(updateYearOfCertification);
            deviceCertificationDomainExtension.setDeviceGroupId(deviceGroupId);
            if (endDeviceGroup.isPresent()) {
                ServiceCall serviceCall = serviceCallType.newServiceCall()
                        .origin("Gogo")
                        .extendedWith(deviceCertificationDomainExtension)
                        .targetObject(endDeviceGroup.get())
                        .create();
                serviceCall.requestTransition(DefaultState.PENDING);
            } else {
                ServiceCall serviceCall = serviceCallType.newServiceCall()
                        .origin("Gogo")
                        .extendedWith(deviceCertificationDomainExtension)
                        .create();
                serviceCall.log(LogLevel.SEVERE, "Device group could not be found");
                serviceCall.requestTransition(DefaultState.REJECTED);
            }
            context.commit();
        }
    }

    public void hook() {
        System.out.println("Usage: hook <device id>");
    }

    public void hook(long deviceId) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallType serviceCallType = serviceCallService.findServiceCallType(CAPTAIN_HOOK_HANDLER_NAME, CAPTAIN_HOOK_HANDLER_VERSION_NAME)
                    .orElseGet(() -> serviceCallService.createServiceCallType(CAPTAIN_HOOK_HANDLER_NAME, CAPTAIN_HOOK_HANDLER_VERSION_NAME)
                            .handler("CaptainHookHandler")
                            .logLevel(LogLevel.FINEST)
                            .create());

            Optional<Device> device = deviceService.findDeviceById(deviceId);

            if (device.isPresent()) {
                ServiceCall serviceCall = serviceCallType.newServiceCall()
                        .origin("Gogo")
                        .targetObject(device.get())
                        .create();
                serviceCall.requestTransition(DefaultState.PENDING);
            } else {
                ServiceCall serviceCall = serviceCallType.newServiceCall()
                        .origin("Gogo")
                        .create();
                serviceCall.log(LogLevel.SEVERE, "Device could not be found");
                serviceCall.requestTransition(DefaultState.REJECTED);
            }
            context.commit();
        }
    }

    public void crash(long deviceId) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallType serviceCallType = serviceCallService.findServiceCallType(NULL_POINTER_HANDLER_NAME, NULL_POINTER_HANDLER_VERSION_NAME)
                    .orElseGet(() -> serviceCallService.createServiceCallType(NULL_POINTER_HANDLER_NAME, NULL_POINTER_HANDLER_VERSION_NAME)
                            .handler("NullPointerHandler")
                            .logLevel(LogLevel.FINEST)
                            .create());

            Optional<Device> device = deviceService.findDeviceById(deviceId);

            if (device.isPresent()) {
                ServiceCall serviceCall = serviceCallType.newServiceCall()
                        .origin("Gogo")
                        .targetObject(device.get())
                        .create();
                serviceCall.requestTransition(DefaultState.PENDING);
            } else {
                ServiceCall serviceCall = serviceCallType.newServiceCall()
                        .origin("Gogo")
                        .create();
                serviceCall.log(LogLevel.SEVERE, "Device could not be found");
                serviceCall.requestTransition(DefaultState.REJECTED);
            }
            context.commit();
        }
    }

    public void createTrackedCommand() {
        System.out.println("Usage: createTrackedCommand <device name> <device message id> <service call id>");
        System.out.println("  Creates a device message for _device message id_ for the device identified by _device mRID_ so that the device command is tracked by the service call");
    }

    public void createTrackedCommand(String deviceName, long deviceMessageId, long serviceCallId) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            Device device = deviceService.findDeviceByName(deviceName)
                    .orElseThrow(() -> new IllegalArgumentException("No device known with name " + deviceName));


            device.newDeviceMessage(DeviceMessageId.havingId(deviceMessageId), TrackingCategory.serviceCall)
                    .setTrackingId("" + serviceCallId)
                    .setReleaseDate(Instant.now())
                    .add();
            context.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createDeviceServiceCall() {
        System.out.println("Usage: createDeviceServiceCall <type> <version> <external reference> <device name>");
    }

    public void createDeviceServiceCall(String type, String typeVersion, String externalReference, String deviceName) {
        threadPrincipalService.set(() -> "Console");
        Optional<ServiceCallType> serviceCallType = serviceCallService.findServiceCallType(type, typeVersion);
        if (!serviceCallType.isPresent()) {
            System.out.println("There is no service call type with name: '" + type + "' and version: '" + typeVersion + "'");
        } else {
            try (TransactionContext context = transactionService.getContext()) {
                Device device = deviceService.findDeviceByName(deviceName)
                        .orElseThrow(() -> new IllegalArgumentException("No device known with name " + deviceName));

                ServiceCall serviceCall = serviceCallType.get()
                        .newServiceCall()
                        .externalReference(externalReference)
                        .targetObject(device)
                        .create();
                context.commit();

                System.out.println("Service call with reference '" + serviceCall.getNumber() + "' has been created");
            }
        }
    }

    public void createServiceCallDemoData() {
        ServiceCallLifeCycle simpleLifecycle = getSimpleLifecycle();
        createServiceCallTypes(simpleLifecycle);
        List<Long> serviceCallIds = createServiceCalls();
        addLogLines(serviceCallIds);
    }

    private void addLogLines(List<Long> serviceCallIds) {
        threadPrincipalService.set(() -> "Console");
        InputStream source = getResourceAsStream("ServiceCallLogs.csv");
        try (Scanner scanner = new Scanner(source)) {
            scanner.nextLine();
            while (scanner.hasNextLine()) {
                parseLogRecord(scanner.nextLine(), serviceCallIds);
            }
        }
    }

    private void parseLogRecord(String record, List<Long> serviceCallIds) {
        String[] columns = record.split(";", -1);
        String index = columns[0];
        String loglevel = columns[1];
        String message = columns[2];
        try (TransactionContext context = transactionService.getContext()) {
            Optional<ServiceCall> serviceCall = serviceCallService.getServiceCall(serviceCallIds.get(Integer.parseInt(index)));
            if(serviceCall.isPresent()) {
                serviceCall.get().log(getLogLevel(loglevel),message);
            }
            context.commit();
        }
    }

    private ServiceCallLifeCycle createSimpleLifecycle() {
        threadPrincipalService.set(() -> "Console");
        System.out.println("Creating simple service call life cycle.");
        try (TransactionContext context = transactionService.getContext()) {
            ServiceCallLifeCycle simplLifeCycle = serviceCallService.createServiceCallLifeCycle("Simple")
                    .remove(DefaultState.CANCELLED)
                    .remove(DefaultState.SCHEDULED)
                    .remove(DefaultState.WAITING)
                    .remove(DefaultState.PAUSED)
                    .remove(DefaultState.PARTIAL_SUCCESS)
                    .remove(DefaultState.REJECTED)
                    .create();

            context.commit();
            return simplLifeCycle;
        }
    }

    private ServiceCallLifeCycle getSimpleLifecycle() {
        return serviceCallService.getServiceCallLifeCycle("Simple")
                .orElseGet(this::createSimpleLifecycle);
    }

    private void createServiceCallTypes(ServiceCallLifeCycle simpleLifecycle) {
        threadPrincipalService.set(() -> "Console");
        InputStream source = getResourceAsStream("ServiceCallTypes.csv");
        try (Scanner scanner = new Scanner(source)) {
            scanner.nextLine();
            while (scanner.hasNextLine()) {
                parseRecord(scanner.nextLine(), simpleLifecycle);
            }
        }
    }

    private InputStream getResourceAsStream(String name) {
        return getClass().getClassLoader().getResourceAsStream(name);
    }

    private void parseRecord(String record, ServiceCallLifeCycle simpleLifecycle) {
        String[] columns = record.split(";");
        try (TransactionContext context = transactionService.getContext()) {

            RegisteredCustomPropertySet customPropertySet = null;
            if (columns.length > 5) {
                customPropertySet = getCustomPropertySet(columns[5]);
            }
            LogLevel level = getLogLevel(columns[3]);
            ServiceCallType serviceCallType;
            if (customPropertySet != null) {
                final RegisteredCustomPropertySet finalCustomPropertySet = customPropertySet;
                serviceCallType = serviceCallService.findServiceCallType(columns[0], columns[1])
                        .orElseGet(() -> getServiceCallTypeBuilder(columns[0], columns[1], columns[4], simpleLifecycle)
                                .handler("ServiceCallDemoHandler")
                                .logLevel(level)
                                .customPropertySet(finalCustomPropertySet)
                                .create());
            } else {
                serviceCallType = serviceCallService.findServiceCallType(columns[0], columns[1])
                        .orElseGet(() -> getServiceCallTypeBuilder(columns[0], columns[1], columns[4], simpleLifecycle)
                                .handler("ServiceCallDemoHandler")
                                .logLevel(level)
                                .create());
            }
            if ("deprecated".equals(columns[2])) {
                serviceCallType.deprecate();
            }
            context.commit();
        }
    }

    private ServiceCallTypeBuilder getServiceCallTypeBuilder(String name, String version, String lifeCycleName, ServiceCallLifeCycle simpleLifecycle) {
        System.out.println("Creating service call type '" + name + "'");
        return "simple".equals(lifeCycleName) ? serviceCallService.createServiceCallType(name, version, simpleLifecycle) :
                serviceCallService.createServiceCallType(name, version);
    }

    private LogLevel getLogLevel(String loglevel) {
        switch (loglevel) {
            case "severe":
                return LogLevel.SEVERE;
            case "info":
                return LogLevel.INFO;
            case "config":
                return LogLevel.CONFIG;
            case "fine":
                return LogLevel.FINE;
            case "finer":
                return LogLevel.FINER;
            case "finest":
                return LogLevel.FINEST;
            default:
                return LogLevel.WARNING;
        }
    }

    private RegisteredCustomPropertySet getCustomPropertySet(String set) {
        String name = "";
        if ("BC".equals(set)) {
            name = BillingCycleCustomPropertySet.class.getSimpleName();
        } else if ("DR".equals(set)) {
            name = DisconnectRequestCustomPropertySet.class.getSimpleName();
        }

        if (!"".equals(name)) {
            final String finalName = name;
            return customPropertySetService.findActiveCustomPropertySets(ServiceCall.class)
                    .stream()
                    .filter(cps -> cps.getCustomPropertySet()
                            .getName()
                            .equals(finalName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Could not find the custom property set"));
        }

        return null;
    }

    private List<Long> createServiceCalls() {
        threadPrincipalService.set(() -> "Console");
        InputStream source = getResourceAsStream("ServiceCalls.csv");
        try (Scanner scanner = new Scanner(source)) {
            scanner.nextLine();
            List<Long> serviceCallIds = new ArrayList<>(5);
            serviceCallIds.add(0L);
            int counter = 1;
            while (scanner.hasNextLine()) {
                parseServiceCallRecord(scanner.nextLine(), counter, serviceCallIds);
                counter ++;
            }

            return serviceCallIds;
        }
    }

    private void parseServiceCallRecord(String record, int counter, List<Long> serviceCallIds) {
        String[] columns = record.split(";", -1);
        String externalRef = columns[0];
        String serviceCallType = columns[1];
        String version = columns[2];
        String origin = columns[3];
        String state = columns[4];
        String customAttributes = columns[5];
        String parent = columns[6];
        String object = columns[7];
        Optional<ServiceCallType> optionalServiceCallType = serviceCallService.findServiceCallType(serviceCallType,version);
        ServiceCallBuilder serviceCallBuilder = getServiceCallBuilder(serviceCallIds, parent, optionalServiceCallType);
        try (TransactionContext context = transactionService.getContext()) {
            if (serviceCallBuilder != null) {
                if (!"".equals(externalRef)) {
                    serviceCallBuilder.externalReference(externalRef);
                }
                if (!"".equals(origin)) {
                    serviceCallBuilder.origin(origin);
                }
                addTargetObject(object, serviceCallBuilder);
                addCustomAttributes(customAttributes, serviceCallBuilder);
                ServiceCall serviceCall = serviceCallBuilder.create();
                transitionServiceCall(serviceCall, state);
                serviceCallIds.add(counter, serviceCall.getId());
            }
            context.commit();
        }
    }

    private void addTargetObject(String object, ServiceCallBuilder serviceCallBuilder) {
        if (!"".equals(object)) {
            if (object.startsWith("UP")) {
                Optional<UsagePoint> usagePoint = meteringService.findUsagePointByName(object);
                usagePoint.ifPresent(serviceCallBuilder::targetObject);
            }
            if (object.startsWith("SP")) {
                Optional<Device> device = deviceService.findDeviceByName(object);
                device.ifPresent(serviceCallBuilder::targetObject);
            }
        }
    }

    private void addCustomAttributes(String customAttributes, ServiceCallBuilder serviceCallBuilder) {
        String[] attributes = customAttributes.split(":", -1);
        if(attributes.length < 2) {
            return;
        }
        if("DR".equals(attributes[0])) {
            DisconnectRequestDomainExtension extension = new DisconnectRequestDomainExtension();
            extension.setReason(attributes[1]);
            extension.setAttempts(BigDecimal.valueOf(Long.parseLong(attributes[2])));
            extension.setEnddate(Instant.ofEpochMilli(Long.parseLong(attributes[3])));
            serviceCallBuilder.extendedWith(extension);
        } else if ("BC".equals(attributes[0])) {
            BillingCycleDomainExtension extension = new BillingCycleDomainExtension();
            extension.setBillingCycle(attributes[1]);
            serviceCallBuilder.extendedWith(extension);
        }
    }

    private ServiceCallBuilder getServiceCallBuilder(List<Long> serviceCallIds, String parent, Optional<ServiceCallType> optionalServiceCallType) {
        if("".equals(parent)) {
           if(optionalServiceCallType.isPresent()) {
               return optionalServiceCallType.get().newServiceCall();
           }
        } else {
            try {
                Optional<ServiceCall> serviceCallOptional = serviceCallService.getServiceCall(serviceCallIds.get(Integer.parseInt(parent)));
                if(serviceCallOptional.isPresent() && optionalServiceCallType.isPresent()) {
                    return serviceCallOptional.get().newChildCall(optionalServiceCallType.get());
                }
            } catch (Exception e) {
                System.out.println("Can't parse '" + parent + "' to a number.");
            }
        }
        return null;
    }

    private DefaultState getState(String state) {
        switch (state) {
            case "created":
                return DefaultState.CREATED;
            case "rejected":
                return DefaultState.REJECTED;
            case "pending":
                return DefaultState.PENDING;
            case "ongoing":
                return DefaultState.ONGOING;
            case "paused":
                return DefaultState.PAUSED;
            case "waiting":
                return DefaultState.WAITING;
            case "scheduled":
                return DefaultState.SCHEDULED;
            case "cancelled":
                return DefaultState.CANCELLED;
            case "success":
                return DefaultState.SUCCESSFUL;
            case "partialsuccess":
                return DefaultState.PARTIAL_SUCCESS;
            case "failed":
                return DefaultState.FAILED;
        }
        return DefaultState.CREATED;
    }

    private void transitionServiceCall(ServiceCall serviceCall, String state) {
        switch (state) {
            case "created":
                return;
            case "ongoing":
                serviceCall.requestTransition(DefaultState.PENDING);
                break;
            case "failed":case "paused":case "partialsuccess":case "cancelled":
            case "waiting":case "success":
                serviceCall.requestTransition(DefaultState.PENDING);
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
            default:
                return;
        }
        DefaultState defaultState = getState(state);
        if(defaultState != null) {
            serviceCall.requestTransition(defaultState);
        }
        //serviceCall.requestTransition(DefaultState.PENDING);
    }

}
