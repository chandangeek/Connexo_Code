package com.energyict.mdc.device.data.ami;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallHandler;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;

import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.DeviceService;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component(name = "com.energyict.mdc.device.data.ami.multisense.servicecall.handler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=MultisenseServiceCallHandler")
public class MultisenseServiceCallHandler implements ServiceCallHandler {
    private volatile CustomPropertySetService customPropertySetService;
    private volatile ServiceCallService serviceCallService;
    private volatile MeteringService meteringService;
    private volatile DeviceService deviceService;


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

    @Activate
    public void activate() {
        System.out.println("Activating multisense service call handler");
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINEST, "Now entering state " + newState.getKey());
        switch (newState) {
            case WAITING:
                serviceCall.log(LogLevel.FINE, "ServiceCall children at work");
                break;
            case ONGOING:
                if (oldState.equals(DefaultState.PENDING)) {
                    createChildren(serviceCall); // We will only create children on the first entry, that is, from PENDING
                } else {
                    calculateResult(serviceCall); // When we get to ONGOING from WAITING, we will calculate the result. So looks like we have an issue with PAUSED here
                }
                break;
            case PENDING:
                serviceCall.requestTransition(DefaultState.ONGOING); // If we forget this case, our process is stuck forever...
                break;
            case CANCELLED:
                serviceCall.findChildren().stream().forEach(sc -> sc.requestTransition(DefaultState.CANCELLED));
                break;
            default:
                serviceCall.log(LogLevel.WARNING, String.format("I entered a state I have no action for: %s", newState)); // Nothing will ever happen after we reach this state
                break;
        }
    }

    @Override
    public boolean allowStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        return !DefaultState.CANCELLED.equals(newState);
    }

    private void calculateResult(ServiceCall serviceCall) {
        Map<DefaultState, Long> collect = countStates(serviceCall);

        boolean finished = EnumSet.of(DefaultState.PENDING, DefaultState.CREATED, DefaultState.SCHEDULED, DefaultState.PAUSED, DefaultState.WAITING, DefaultState.ONGOING)
                .stream().noneMatch(state -> collect.getOrDefault(state, 0L) > 0L);

        if (finished && !EnumSet.of(DefaultState.PARTIAL_SUCCESS, DefaultState.CANCELLED, DefaultState.SUCCESSFUL, DefaultState.FAILED)
                .contains(serviceCall.getState())) {
            if (collect.getOrDefault(DefaultState.FAILED, 0L) > 0L) {
                if (collect.getOrDefault(DefaultState.SUCCESSFUL, 0L) > 0L) {
                    serviceCall.requestTransition(DefaultState.PARTIAL_SUCCESS);
                } else {
                    serviceCall.requestTransition(DefaultState.FAILED);
                }
            } else {
                serviceCall.requestTransition(DefaultState.SUCCESSFUL); // If there are no children, we consider this success...
            }
        }
    }

    @Override
    public void onChildStateChange(ServiceCall parentServiceCall, ServiceCall childServiceCall, DefaultState oldState, DefaultState newState) {
        switch (newState) {
            case SUCCESSFUL:
                parentServiceCall.log(LogLevel.FINE, "Child serviceall completed work");
                break;
            case FAILED:
            case CANCELLED:
            case REJECTED:
                parentServiceCall.log(LogLevel.FINE, "Child servicecall did not succeed");
                break;
            default:
                parentServiceCall.log(LogLevel.FINEST, String.format("Service call child entered a default state: %s", newState));
        }
        Map<DefaultState, Long> collect = countStates(parentServiceCall);

        boolean finished = EnumSet.of(DefaultState.PENDING, DefaultState.CREATED, DefaultState.SCHEDULED, DefaultState.PAUSED, DefaultState.WAITING, DefaultState.ONGOING)
                .stream().noneMatch(state -> collect.getOrDefault(state, 0L) > 0L);

        if (finished && parentServiceCall.getState() == DefaultState.WAITING) { // I have to check for WAITING state, because I might hit the finished state once for EVERY child, but only want to transition once
            parentServiceCall.requestTransition(DefaultState.ONGOING); // Race condition ?
        }
    }

    protected Map<DefaultState, Long> countStates(ServiceCall parentServiceCall) {
        return parentServiceCall.findChildren()
                .stream()
                .collect(Collectors.groupingBy(ServiceCall::getState, Collectors.counting()));
    }

    protected void createChildren(ServiceCall serviceCall) {
        ServiceOperationDomainExtension extensionFor = serviceCall.getExtensionFor(new ServiceOperationCustomPropertySet())
                .get();

        Optional<EndDevice> endDevice = meteringService.findEndDevice(extensionFor.getmRIDDevice());

        if (endDevice.isPresent()) {
            Optional<ServiceCallType> serviceCallType = serviceCallService.findServiceCallType(ServiceCallCommands.ServiceCallTypes.valueOf(extensionFor.getBreakerStatus().getDescription())
                    .getTypeName(), ServiceCallCommands.ServiceCallTypes.valueOf(extensionFor.getBreakerStatus().getDescription()).getTypeVersion());
            if (serviceCallType.isPresent()) {
                ContactorOperationDomainExtension domainExtension = new ContactorOperationDomainExtension();
                Device device = deviceService.findDeviceById(Long.valueOf(endDevice.get().getAmrId())).get();
                domainExtension.setmRIDDevice(device.getmRID());
                domainExtension.setActivationDate(extensionFor.getActivationDate());
                domainExtension.setReadingType(extensionFor.getReadingType());
                domainExtension.setBreakerStatus(extensionFor.getBreakerStatus());
                domainExtension.setCallback(extensionFor.getCallback());
                domainExtension.setLoadLimitEnabled(extensionFor.getLoadLimitEnabled());
                domainExtension.setLoadLimit(extensionFor.getLoadLimit());
                // domainExtension.setNrOfUnconfirmedDeviceCommands(...)


                ServiceCall childServiceCall = serviceCall.newChildCall(serviceCallType.get())
                        .extendedWith(domainExtension)
                        .targetObject(device)
                        .origin(serviceCall.getOrigin().orElse(null))
                        .externalReference(serviceCall.getExternalReference().orElse(null))
                        .create();
                childServiceCall.requestTransition(DefaultState.PENDING);
            }
            serviceCall.requestTransition(DefaultState.WAITING);
        } else {
            serviceCall.log(LogLevel.SEVERE, "Could not locate required service call type for children");
            serviceCall.requestTransition(DefaultState.FAILED);
        }

    }
}
