package com.elster.jupiter.prepayment.impl.fullduplex;

import com.elster.jupiter.prepayment.impl.ContactorInfo;
import com.elster.jupiter.prepayment.impl.servicecall.ContactorOperationCustomPropertySet;
import com.elster.jupiter.prepayment.impl.servicecall.ContactorOperationDomainExtension;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.device.data.Device;

import javax.inject.Inject;

/**
 * This class contains the controller who will translate request coming from our internal Prepayment code
 * to the proper FullDuplex commands
 *
 * @author sva
 * @since 31/03/2016 - 15:04
 */
public class FullDuplexController {

    private final MultiSenseAMRImpl multiSenseAMR;

    @Inject
    public FullDuplexController(MultiSenseAMRImpl multiSenseAMR) {
        this.multiSenseAMR = multiSenseAMR;
    }

    public void performContactorOperations(Device device, ServiceCall serviceCall, ContactorInfo contactorInfo) {
        int nrOfDeviceCommands = 0;
        nrOfDeviceCommands += performBreakerOperations(device, serviceCall, contactorInfo);
        nrOfDeviceCommands += performLoadLimitOperations(device, serviceCall, contactorInfo);

        serviceCall.log(LogLevel.INFO, "Scheduled " + nrOfDeviceCommands + " device command(s).");
        updateNrOfUnconfirmedDeviceCommands(serviceCall, nrOfDeviceCommands);
    }

    private int performBreakerOperations(Device device, ServiceCall serviceCall, ContactorInfo contactorInfo) {
        int nrOfDeviceCommands = 0;
        serviceCall.log(LogLevel.INFO, "Handling breaker operations - the breaker will be " + contactorInfo.status.name() + ".");
        switch (contactorInfo.status) {
            case connected:
                multiSenseAMR.connectBreaker(device, serviceCall, contactorInfo.activationDate);
                serviceCall.log(LogLevel.FINE, "Scheduled device command to connect the breaker.");
                nrOfDeviceCommands++;
                break;
            case disconnected:
                multiSenseAMR.disconnectBreaker(device, serviceCall, contactorInfo.activationDate);
                serviceCall.log(LogLevel.FINE, "Scheduled device command to disconnect the breaker.");
                nrOfDeviceCommands++;
                break;
            case armed:
                multiSenseAMR.armBreaker(device, serviceCall, contactorInfo.activationDate);
                serviceCall.log(LogLevel.FINE, "Scheduled two device commands to arm the breaker.");
                nrOfDeviceCommands += 2; // Will be transmitted as 2 separate DeviceCommands (first a DISCONNECT, then an ARM)
                break;
        }
        return nrOfDeviceCommands;
    }

    private int performLoadLimitOperations(Device device, ServiceCall serviceCall, ContactorInfo contactorInfo) {
        int nrOfDeviceCommands = 0;
        if (shouldPerformLoadLimitOperations(contactorInfo)) {
            serviceCall.log(LogLevel.INFO, "Handling load limitation operations.");
            if (contactorInfo.loadLimit != null && contactorInfo.loadLimit.shouldDisableLoadLimit()) {
                multiSenseAMR.disableLoadLimiting(device, serviceCall);
                serviceCall.log(LogLevel.FINE, "Scheduled device command to disable the load limiting");
                nrOfDeviceCommands++;
            } else if (contactorInfo.loadLimit != null && contactorInfo.loadTolerance != null) {
                multiSenseAMR.configureLoadLimitThresholdAndDuration(device, serviceCall, contactorInfo.loadLimit.limit, contactorInfo.loadLimit.unit, contactorInfo.tariffs, contactorInfo.loadTolerance);
                serviceCall.log(LogLevel.FINE, "Scheduled device command to configure the load limit and load tolerance");
                nrOfDeviceCommands++;
            } else if (contactorInfo.loadLimit != null) {
                multiSenseAMR.configureLoadLimitThreshold(device, serviceCall, contactorInfo.loadLimit.limit, contactorInfo.loadLimit.unit, contactorInfo.tariffs);
                serviceCall.log(LogLevel.FINE, "Scheduled device command to configure the load limit");
                nrOfDeviceCommands++;
            } else if (contactorInfo.loadTolerance != null) {
                multiSenseAMR.configureLoadLimitDuration(device, serviceCall, contactorInfo.loadTolerance);
                serviceCall.log(LogLevel.FINE, "Scheduled device command to configure the load limit tolereance");
                nrOfDeviceCommands++;
            }

            if (contactorInfo.readingType != null) {
                multiSenseAMR.configureLoadLimitMeasurementReadingType(device, serviceCall, contactorInfo.readingType);
                serviceCall.log(LogLevel.FINE, "Scheduled device command to set the load limit measurement type");
                nrOfDeviceCommands++;
            }
        }
        return nrOfDeviceCommands;
    }

    private boolean shouldPerformLoadLimitOperations(ContactorInfo contactorInfo) {
        return contactorInfo.loadLimit != null || contactorInfo.loadTolerance != null || contactorInfo.readingType != null;
    }

    private void updateNrOfUnconfirmedDeviceCommands(ServiceCall serviceCall, int nrOfDeviceCommands) {
        ContactorOperationDomainExtension contactorOperationDomainExtension = serviceCall.getExtensionFor(new ContactorOperationCustomPropertySet()).get();
        contactorOperationDomainExtension.setNrOfUnconfirmedDeviceCommands(nrOfDeviceCommands);
        serviceCall.update(contactorOperationDomainExtension);
    }
}