package com.energyict.mdc.servicecall.example;

import com.elster.jupiter.cps.CustomPropertySetService;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
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

import java.time.Instant;
import java.util.Optional;

/**
 * Example handler taking care of a setting the year of certification for a group of devices
 */
@Component(name = "com.energyict.mdc.servicecall.example.devicegroup.certification.handler",
        service = ServiceCallHandler.class,
        immediate = true,
        property = "name=DeviceGroupCertificationHandler")
public class DeviceGroupCertificationHandler implements ServiceCallHandler {

    private volatile CustomPropertySetService customPropertySetService;
    private volatile MeteringGroupsService meteringGroupsService;
    private volatile ServiceCallService serviceCallService;
    private volatile DeviceService deviceService;

    public DeviceGroupCertificationHandler() {
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
    public void setMeteringGroupsService(MeteringGroupsService meteringGroupsService) {
        this.meteringGroupsService = meteringGroupsService;
    }

    @Activate
    public void activate() {
        System.out.println("Activating device group certification handler");
    }

    @Override
    public void onStateChange(ServiceCall serviceCall, DefaultState oldState, DefaultState newState) {
        serviceCall.log(LogLevel.FINEST, "Now entering state " + newState.getKey());
        switch (newState) {
            case ONGOING:
                createChildren(serviceCall);
                break;
            case PENDING:
                serviceCall.requestTransition(DefaultState.ONGOING);
                break;
            default:
                serviceCall.log(LogLevel.WARNING, String.format("I entered a state I have no action for: %s", newState));
        }
    }

    protected void createChildren(ServiceCall serviceCall) {
        DeviceGroupCertificationDomainExtension extensionFor = serviceCall.getExtensionFor(new DeviceGroupCertificationCustomPropertySet(customPropertySetService))
                .get();
        Optional<EndDeviceGroup> endDeviceGroup = meteringGroupsService.findEndDeviceGroup(extensionFor.getDeviceGroupId());

        if (endDeviceGroup.isPresent()) {
            Optional<ServiceCallType> serviceCallType = serviceCallService.findServiceCallType(ServiceCallCommands.HANDLER_NAME, ServiceCallCommands.HANDLER_VERSION_NAME);
            if (serviceCallType.isPresent()) {
                for (EndDevice meter : endDeviceGroup.get().getMembers(Instant.now())) {
                    DeviceCertificationDomainExtension domainExtension = new DeviceCertificationDomainExtension();
                    Device device = deviceService.findDeviceById(Long.valueOf(meter.getAmrId())).get();
                    domainExtension.setDeviceId(device.getId());
                    domainExtension.setYearOfCertification(extensionFor.getYearOfCertification());
                    ServiceCall childServiceCall = serviceCall.newChildCall(serviceCallType.get())
                            .extendedWith(domainExtension)
                            .targetObject(device)
                            .origin(serviceCall.getOrigin().orElse(null))
                            .externalReference(serviceCall.getExternalReference().orElse(null))
                            .create();
                    childServiceCall.requestTransition(DefaultState.PENDING);
                }
            } else {
                serviceCall.log(LogLevel.SEVERE, "Could not locate required service call type for children");
            }
        } else {
            serviceCall.log(LogLevel.SEVERE, "No end device group with id " + extensionFor.getDeviceGroupId());
            serviceCall.requestTransition(DefaultState.FAILED);
        }
    }
}
