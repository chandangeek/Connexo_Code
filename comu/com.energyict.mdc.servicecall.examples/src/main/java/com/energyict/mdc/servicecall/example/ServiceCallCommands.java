package com.energyict.mdc.servicecall.example;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.transaction.TransactionContext;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.protocol.api.TrackingCategory;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.time.Instant;
import java.util.Optional;

/**
 * Created by bvn on 3/4/16.
 */
@Component(name = "com.energyict.mdc.servicecall.examples", service = ServiceCallCommands.class,
        property = {"osgi.command.scope=sch",
                "osgi.command.function=updateYearOfCertification",
                "osgi.command.function=updateYearOfCertificationGroup",
                "osgi.command.function=hook",
                "osgi.command.function=crash",
                "osgi.command.function=createTrackedCommand"
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

}
