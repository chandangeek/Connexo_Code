package com.energyict.mdc.servicecall.examples;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
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
import java.time.Instant;
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
    public static final String HANDLER_NAME = "yearUpdater";
    public static final String HANDLER_VERSION_NAME = "v1.0";
    public static final String GROUP_HANDLER_NAME = "yearGroupUpdater";
    public static final String GROUP_HANDLER_VERSION_NAME = "v1.0";
    public static final String CAPTAIN_HOOK_HANDLER_NAME = "captainHook";
    public static final String CAPTAIN_HOOK_HANDLER_VERSION_NAME = "v1.0";
    public static final String NULL_POINTER_HANDLER_NAME = "NullPointer";
    public static final String NULL_POINTER_HANDLER_VERSION_NAME = "v1.0";
    private volatile ServiceCallService serviceCallService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile DeviceService deviceService;

    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;
    private volatile MeteringGroupsService meteringGroupService;

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
        System.out.println("Usage: createTrackedCommand <device mRID> <device message id> <service call id>");
        System.out.println("  Creates a device message for _device message id_ for the device identified by _device mRID_ so that the device command is tracked by the service call");
    }

    public void createTrackedCommand(String deviceMrid, long deviceMessageId, long serviceCallId) {
        threadPrincipalService.set(() -> "Console");

        try (TransactionContext context = transactionService.getContext()) {
            Device device = deviceService.findByUniqueMrid(deviceMrid)
                    .orElseThrow(() -> new IllegalArgumentException("No device known with mRID " + deviceMrid));


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
        System.out.println("Usage: createDeviceServiceCall <type> <version> <external reference> <device MRID>");
    }

    public void createDeviceServiceCall(String type, String typeVersion, String externalReference, String deviceMRID) {
        threadPrincipalService.set(() -> "Console");
        Optional<ServiceCallType> serviceCallType = serviceCallService.findServiceCallType(type, typeVersion);
        if (!serviceCallType.isPresent()) {
            System.out.println("There is no service call type with name: '" + type + "' and version: '" + typeVersion + "'");
        } else {
            try (TransactionContext context = transactionService.getContext()) {
                Device device = deviceService.findByUniqueMrid(deviceMRID)
                        .orElseThrow(() -> new IllegalArgumentException("No device known with mRID " + deviceMRID));

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
                .orElseGet(() -> createSimpleLifecycle());
    }

    private void createServiceCallTypes(ServiceCallLifeCycle simpleLifecycle) {
        threadPrincipalService.set(() -> "Console");
        InputStream source = getResourceAsStream("ServiceCallTypes.csv");
        try (Scanner scanner = new Scanner(source)) {
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
            RegisteredCustomPropertySet customPropertySet = customPropertySetService.findActiveCustomPropertySets(ServiceCall.class)
                    .stream()
                    .filter(cps -> cps.getCustomPropertySet()
                            .getName()
                            .equals(UsagePointMRIDCustomPropertySet.class.getSimpleName()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Could not find my custom property set"));


            LogLevel level = getLogLevel(columns[3]);
            ServiceCallType serviceCallType = serviceCallService.findServiceCallType(columns[0], columns[1])
                    .orElseGet(() -> getServiceCallTypeBuilder(columns[0], columns[1], columns[4], simpleLifecycle)
                            .handler("NullPointerHandler")
                            .logLevel(level)
                            .customPropertySet(customPropertySet)
                            .create());
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

}
