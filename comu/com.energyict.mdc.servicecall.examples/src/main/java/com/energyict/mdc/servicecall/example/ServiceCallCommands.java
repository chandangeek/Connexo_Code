package com.energyict.mdc.servicecall.example;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.cps.RegisteredCustomPropertySet;
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

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;

/**
 * Created by bvn on 3/4/16.
 */
@Component(name = "com.energyict.mdc.servicecall.examples", service = ServiceCallCommands.class,
        property = {"osgi.command.scope=sch",
                "osgi.command.function=updateYearOfCertification",
        }, immediate = true)
public class ServiceCallCommands {
    public static final String NAME = "yearUpdater";
    public static final String VERSION_NAME = "v1.0";
    private volatile ServiceCallService serviceCallService;
    private volatile CustomPropertySetService customPropertySetService;
    private volatile DeviceService deviceService;

    private volatile TransactionService transactionService;
    private volatile ThreadPrincipalService threadPrincipalService;


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

            ServiceCallType serviceCallType = serviceCallService.findServiceCallType(NAME, VERSION_NAME)
                    .orElseGet(() -> serviceCallService.createServiceCallType(NAME, VERSION_NAME)
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
}
