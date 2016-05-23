package com.energyict.mdc.device.data.ami;


import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Optional;
import java.util.stream.Stream;

@Component(name = "com.energyict.mdc.device.data.ami.multisense.contactor.operation.handler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=ContactorOperationHandler")
public class ContactorOperationHandler implements ServiceCallHandler{

    private volatile DeviceService deviceService;

    public ContactorOperationHandler() {
    }

    @Reference
    public void setCustomPropertySetService(CustomPropertySetService customPropertySetService) {
    }

    @Reference
    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @Activate
    public void activate() {
        System.out.println("Activating contactor operation handler");
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINEST, "Now entering state " + newState.getKey());
        switch (newState) {
            case ONGOING:
                updateBreakerStatus(serviceCall);
                break;
            case PENDING:
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
            default:
                serviceCall.log(LogLevel.WARNING, String.format("I entered a state I have no action for: %s", newState));
        }
    }

    protected void updateBreakerStatus(ServiceCall serviceCall) {
        ContactorOperationDomainExtension extensionFor = serviceCall.getExtensionFor(new ContactorOperationCustomPropertySet())
                .get();
        Optional<Device> device = deviceService.findByUniqueMrid(extensionFor.getmRIDDevice());
        if (device.isPresent()) {
            serviceCall.log(LogLevel.FINE, "Device found");
            BreakerStatus breakerStatus = extensionFor.getBreakerStatus();
            if (!Stream.of(BreakerStatus.values()).filter(status -> status.equals(breakerStatus)).findAny().isPresent()) {
                serviceCall.log(LogLevel.SEVERE, "Invalid breaker status: " + breakerStatus);
                serviceCall.requestTransition(DefaultState.FAILED);
                return;
            }
            //TODO something like
            //device.get().setBreakerStatus(breakerStatus);
            serviceCall.log(LogLevel.FINE, "Breaker status updated on device");
            //update device
            device.get().save();
            serviceCall.log(LogLevel.INFO, "Device updated");
            serviceCall.requestTransition(DefaultState.SUCCESSFUL);

        } else {
            serviceCall.log(LogLevel.SEVERE, "No device with mRID " + extensionFor.getmRIDDevice());
            serviceCall.requestTransition(DefaultState.FAILED);
        }
    }
}
