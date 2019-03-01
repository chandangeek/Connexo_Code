package com.energyict.mdc.cim.webservices.inbound.soap.impl;

import com.elster.jupiter.servicecall.ServiceCall;
import com.energyict.mdc.cim.webservices.inbound.soap.meterconfig.MeterConfigFaultMessageFactory;
import com.energyict.mdc.device.data.Device;

import ch.iec.tc57._2011.executemeterconfig.FaultMessage;

import java.util.ArrayList;
import java.util.List;

public class FaultSituationHandler {
    private ServiceCall serviceCall;
    private final MeterConfigFaultMessageFactory faultMessageFactory;
    private List<FaultMessage> faults;
    private LoggerUtils loggerUtils;

    public FaultSituationHandler(ServiceCall serviceCall, LoggerUtils loggerUtils,  MeterConfigFaultMessageFactory faultMessageFactory){
        this.serviceCall = serviceCall;
        this.faultMessageFactory = faultMessageFactory;
        this.faults = new ArrayList<>();
        this.loggerUtils = loggerUtils;
    }

    public void logSevere(Device device, MessageSeeds messageSeeds, Object... args){
        loggerUtils.logSevere(device, faults, serviceCall, messageSeeds, args);
    }

    public void logSevere(Device device, FaultMessage ex) {
        loggerUtils.logSevere(device, faults, serviceCall, ex);
    }

    public void logException(Device device, Exception ex, MessageSeeds messageSeeds, Object... args){
        loggerUtils.logException(device, faults, serviceCall, ex, messageSeeds, args);
    }

    public boolean anyException() {
        return !faults.isEmpty();
    }

    public ArrayList<FaultMessage> faults() {
        return new ArrayList<>(faults);
    }

    public FaultMessage newFault(String deviceName, MessageSeeds messageSeeds, String argument) {
        return faultMessageFactory.meterConfigFaultMessageSupplier(deviceName, messageSeeds,
                argument).get();
    }
}
