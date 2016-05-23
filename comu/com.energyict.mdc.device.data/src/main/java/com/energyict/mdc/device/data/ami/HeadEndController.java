package com.energyict.mdc.device.data.ami;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.EndDeviceControlType;
import com.elster.jupiter.metering.ami.CommandFactory;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;

import javax.inject.Inject;
import java.util.List;

public class HeadEndController {

    private final MultiSenseHeadEndInterface multiSenseAMR;

    @Inject
    public HeadEndController(MultiSenseHeadEndInterface multiSenseAMR) {
        this.multiSenseAMR = multiSenseAMR;
    }

    public void performContactorOperations(EndDevice endDevice, ServiceCall serviceCall, ContactorInfo contactorInfo) {
        CommandFactory commandFactory = multiSenseAMR.getCommandFactory();
        List<EndDeviceControlType> endDeviceControlTypes = multiSenseAMR.getCapabilities(endDevice)
                .getSupportedControlTypes();
        int nrOfDeviceCommands = 0;
        nrOfDeviceCommands += performBreakerOperations(commandFactory, endDevice, serviceCall, contactorInfo);
        nrOfDeviceCommands += performLoadLimitOperations(commandFactory, endDevice, serviceCall, contactorInfo);

        serviceCall.log(LogLevel.INFO, "Scheduled " + nrOfDeviceCommands + " device command(s).");
        updateNrOfUnconfirmedDeviceCommands(serviceCall, nrOfDeviceCommands);
    }

    private int performBreakerOperations(CommandFactory commandFactory, EndDevice endDevice, ServiceCall serviceCall, ContactorInfo contactorInfo) {
        int nrOfDeviceCommands = 0;

        serviceCall.log(LogLevel.INFO, "Handling breaker operations - the breaker will be " + contactorInfo.status.name() + ".");
        switch (contactorInfo.status) {
            case CONNECTED:
                multiSenseAMR.sendCommand(commandFactory.createConnectCommand(endDevice, contactorInfo.activationDate), contactorInfo.activationDate, serviceCall);
                serviceCall.log(LogLevel.FINE, "Scheduled device command to connect the breaker.");
                nrOfDeviceCommands++;
                break;
            case DISCONNECTED:
                multiSenseAMR.sendCommand(commandFactory.createDisconnectCommand(endDevice, contactorInfo.activationDate), contactorInfo.activationDate, serviceCall);
                serviceCall.log(LogLevel.FINE, "Scheduled device command to disconnect the breaker.");
                nrOfDeviceCommands++;
                break;
            case ARMED:
                multiSenseAMR.sendCommand(commandFactory.createArmCommand(endDevice, true, contactorInfo.activationDate), contactorInfo.activationDate, serviceCall);
                serviceCall.log(LogLevel.FINE, "Scheduled two device commands to arm the breaker.");
                nrOfDeviceCommands += 2; // Will be transmitted as 2 separate DeviceCommands (first a DISCONNECT, then an ARM)
                break;
        }
        return nrOfDeviceCommands;
    }

    private int performLoadLimitOperations(CommandFactory commandFactory, EndDevice endDevice, ServiceCall serviceCall, ContactorInfo contactorInfo)  {
        int nrOfDeviceCommands = 0;
        if (shouldPerformLoadLimitOperations(contactorInfo)) {
            serviceCall.log(LogLevel.INFO, "Handling load limitation operations.");
            if (contactorInfo.loadLimit == null) {
                multiSenseAMR.sendCommand(commandFactory.createDisableLoadLimitCommand(endDevice), contactorInfo.activationDate, serviceCall);
                serviceCall.log(LogLevel.FINE, "Scheduled device command to disable the load limiting");
                nrOfDeviceCommands++;
            } /*else if (contactorInfo.loadLimit != null) {
                multiSenseAMR.configureLoadLimitThresholdAndDuration(endDevice, serviceCall, contactorInfo.loadLimit.getValue(), contactorInfo.loadLimit.getUnit(), contactorInfo.tariffs, contactorInfo.loadTolerance);
                serviceCall.log(LogLevel.FINE, "Scheduled device command to configure the load limit");
                nrOfDeviceCommands++;
            } */ else if (contactorInfo.loadLimit != null) {
                multiSenseAMR.sendCommand(commandFactory.createEnableLoadLimitCommand(endDevice, contactorInfo.loadLimit), contactorInfo.activationDate, serviceCall);
                serviceCall.log(LogLevel.FINE, "Scheduled device command to configure the load limit");
                nrOfDeviceCommands++;
            }
        }

           /* if (contactorInfo.readingType != null) {
                multiSenseAMR.configureLoadLimitMeasurementReadingType(endDevice, serviceCall, contactorInfo.readingType);
                serviceCall.log(LogLevel.FINE, "Scheduled device command to set the load limit measurement type");
                nrOfDeviceCommands++;
            } */
        return nrOfDeviceCommands;
    }

    private boolean shouldPerformLoadLimitOperations(ContactorInfo contactorInfo) {
        return contactorInfo.loadLimit != null; //|| contactorInfo.loadTolerance != null || contactorInfo.readingType != null;
    }

    private void updateNrOfUnconfirmedDeviceCommands(ServiceCall serviceCall, int nrOfDeviceCommands) {
        ContactorOperationDomainExtension contactorOperationDomainExtension = serviceCall.getExtensionFor(new ContactorOperationCustomPropertySet())
                .get();
        contactorOperationDomainExtension.setNrOfUnconfirmedDeviceCommands(nrOfDeviceCommands);
        serviceCall.update(contactorOperationDomainExtension);
    }
}