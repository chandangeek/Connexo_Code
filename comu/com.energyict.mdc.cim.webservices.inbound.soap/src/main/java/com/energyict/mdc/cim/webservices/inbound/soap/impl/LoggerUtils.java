package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.servicecall.LogLevel;
import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigFaultMessageFactory;
import com.energyict.mdc.device.data.Device;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerUtils {
    private Logger logger;
    private final Thesaurus thesaurus;
    private final MeterConfigFaultMessageFactory faultMessageFactory;

    public LoggerUtils(Logger logger, Thesaurus thesaurus,
                       MeterConfigFaultMessageFactory faultMessageFactory){
        this.logger = logger;
        this.thesaurus = thesaurus;
        this.faultMessageFactory = faultMessageFactory;
    }


    public void logSevere(Device device, List<FaultMessage> allFaults, ServiceCall serviceCall,
                          FaultMessage faultMessage) {
        if (serviceCall != null) {
            serviceCall.log(LogLevel.SEVERE, faultMessage.getMessage());
        } else {
            logger.log(Level.SEVERE, faultMessage.getMessage());
        }
        allFaults.add(faultMessage);
    }

    public void logSevere(Device device, List<FaultMessage> allFaults, ServiceCall serviceCall,
                          MessageSeeds messageSeeds, Object... args) {
        if (serviceCall != null) {
            serviceCall.log(LogLevel.SEVERE, messageSeeds.translate(thesaurus, args));
        } else {
            logger.log(Level.SEVERE, messageSeeds.translate(thesaurus, args));
        }
        allFaults.add(faultMessageFactory.meterConfigFaultMessageSupplier(device.getName(), messageSeeds, args).get());
    }

    public void logInfo(ServiceCall serviceCall, MessageSeeds messageSeeds, Object... args) {
        if (serviceCall != null) {
            serviceCall.log(LogLevel.INFO, messageSeeds.translate(thesaurus, args));
        } else {
            logger.log(Level.INFO, messageSeeds.translate(thesaurus, args));
        }
    }

    public void logException(Device device, List<FaultMessage> faults, ServiceCall serviceCall, Exception ex,
                             MessageSeeds messageSeeds, Object... args) {
        if (serviceCall != null) {
            serviceCall.log(messageSeeds.translate(thesaurus, args), ex);
        } else {
            logger.log(Level.SEVERE, messageSeeds.translate(thesaurus, args), ex);
        }
        faults.add(faultMessageFactory.meterConfigFaultMessageSupplier(device.getName(), messageSeeds, args).get());
    }
}
