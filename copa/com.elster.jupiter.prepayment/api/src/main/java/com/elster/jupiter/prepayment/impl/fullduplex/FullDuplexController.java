package com.elster.jupiter.prepayment.impl.fullduplex;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.prepayment.impl.ContactorInfo;
import com.elster.jupiter.prepayment.impl.servicecall.ContactorOperationCustomPropertySet;
import com.elster.jupiter.prepayment.impl.servicecall.ContactorOperationDomainExtension;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;

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

    public void performContactorOperations(EndDevice endDevice, ServiceCall serviceCall, ContactorInfo contactorInfo) {
        int nrOfDeviceCommands = 0;
        nrOfDeviceCommands += performBreakerOperations(endDevice, serviceCall, contactorInfo);
        nrOfDeviceCommands += performLoadLimitOperations(endDevice, serviceCall, contactorInfo);

        serviceCall.log(LogLevel.INFO, "Scheduled " + nrOfDeviceCommands + " device command(s).");
        updateNrOfUnconfirmedDeviceCommands(serviceCall, nrOfDeviceCommands);
    }

    private int performBreakerOperations(EndDevice endDevice, ServiceCall serviceCall, ContactorInfo contactorInfo) {
        int nrOfDeviceCommands = 0;
        if (shouldPerformBreakerOperations(contactorInfo)) {
            serviceCall.log(LogLevel.INFO, "Handling breaker operations - the breaker will be " + contactorInfo.status.getDescription() + ".");
            switch (contactorInfo.status) {
                case CONNECTED:
                    multiSenseAMR.connectBreaker(endDevice, serviceCall, contactorInfo.activationDate);
                    serviceCall.log(LogLevel.FINE, "Scheduled device command to connect the breaker.");
                    nrOfDeviceCommands++;
                    break;
                case DISCONNECTED:
                    multiSenseAMR.disconnectBreaker(endDevice, serviceCall, contactorInfo.activationDate);
                    serviceCall.log(LogLevel.FINE, "Scheduled device command to disconnect the breaker.");
                    nrOfDeviceCommands++;
                    break;
                case ARMED:
                    multiSenseAMR.armBreaker(endDevice, serviceCall, contactorInfo.activationDate);
                    serviceCall.log(LogLevel.FINE, "Scheduled two device commands to arm the breaker.");
                    nrOfDeviceCommands += 2; // Will be transmitted as 2 separate DeviceCommands (first a DISCONNECT, then an ARM)
                    break;
            }
        }
        return nrOfDeviceCommands;
    }

    private int performLoadLimitOperations(EndDevice endDevice, ServiceCall serviceCall, ContactorInfo contactorInfo) {
        int nrOfDeviceCommands = 0;
        if (shouldPerformLoadLimitOperations(contactorInfo)) {
            serviceCall.log(LogLevel.INFO, "Handling load limitation operations.");
            if (contactorInfo.loadLimit != null && contactorInfo.loadLimit.shouldDisableLoadLimit()) {
                multiSenseAMR.disableLoadLimiting(endDevice, serviceCall);
                serviceCall.log(LogLevel.FINE, "Scheduled device command to disable the load limiting");
                nrOfDeviceCommands++;
            } else if (contactorInfo.loadLimit != null && contactorInfo.loadTolerance != null) {
                multiSenseAMR.configureLoadLimitThresholdAndDuration(endDevice, serviceCall, contactorInfo.loadLimit.limit, contactorInfo.loadLimit.unit, contactorInfo.loadTolerance);
                serviceCall.log(LogLevel.FINE, "Scheduled device command to configure the load limit and load tolerance");
                nrOfDeviceCommands++;
            } else if (contactorInfo.loadLimit != null) {
                multiSenseAMR.configureLoadLimitThreshold(endDevice, serviceCall, contactorInfo.loadLimit.limit, contactorInfo.loadLimit.unit);
                serviceCall.log(LogLevel.FINE, "Scheduled device command to configure the load limit");
                nrOfDeviceCommands++;
            }
        }
        return nrOfDeviceCommands;
    }

    private boolean shouldPerformBreakerOperations(ContactorInfo contactorInfo) {
        return contactorInfo.status != null;
    }

    private boolean shouldPerformLoadLimitOperations(ContactorInfo contactorInfo) {
        return contactorInfo.loadLimit != null;
    }

    private void updateNrOfUnconfirmedDeviceCommands(ServiceCall serviceCall, int nrOfDeviceCommands) {
        ContactorOperationDomainExtension contactorOperationDomainExtension = serviceCall.getExtensionFor(new ContactorOperationCustomPropertySet()).get();
        contactorOperationDomainExtension.setNrOfUnconfirmedDeviceCommands(nrOfDeviceCommands);
        serviceCall.update(contactorOperationDomainExtension);
    }
}