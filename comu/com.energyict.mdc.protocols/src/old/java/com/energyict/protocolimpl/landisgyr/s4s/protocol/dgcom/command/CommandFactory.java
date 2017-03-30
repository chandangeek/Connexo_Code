/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * CommandFactory.java
 *
 * Created on 22 mei 2006, 15:48
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.command;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.landisgyr.s4s.protocol.dgcom.S4s;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class CommandFactory {

    private S4s s4s;
    FirmwareVersionCommand fvc=null;
    S4sConfiguration s4sconfig=null;
    TOUAndLoadProfileOptions tOUAndLoadProfileOptions=null;
    DeviceIDExtendedCommand deviceIDExtendedCommand=null;
    SerialNumberCommand serialNumberCommand=null;
    DemandIntervalCommand demandIntervalCommand=null;
    MeasurementUnitsCommand measurementUnitsCommand=null;
    KFactorCommand kFactorCommand=null;
    ThermalKFactorCommand thermalKFactorCommand=null;
    KhValueCommand khValueCommand=null;
    ScaleFactorCommand scaleFactorCommand=null;
    TOUStatusCommand touStatusCommand=null;
    LoadProfileAndSeasonChangeOptionsCommand loadProfileAndSeasonChangeOptionsCommand=null;
    LoadProfileMetricSelectionRXCommand loadProfileMetricSelectionRXCommand=null;
    SelfReadConfigurationCommand selfReadConfigurationCommand=null;

    /** Creates a new instance of CommandFactory */
    public CommandFactory(S4s s4s) {
        this.setS4s(s4s);
    }

    public SelfReadConfigurationCommand getSelfReadConfigurationCommand() throws IOException {
        if (selfReadConfigurationCommand==null) {
            selfReadConfigurationCommand = new SelfReadConfigurationCommand(this);
            selfReadConfigurationCommand.invoke();
        }
        return selfReadConfigurationCommand;
    }

    public LoadProfileMetricSelectionRXCommand getLoadProfileMetricSelectionRXCommand() throws IOException {
        if (loadProfileMetricSelectionRXCommand==null) {
            loadProfileMetricSelectionRXCommand = new LoadProfileMetricSelectionRXCommand(this);
            loadProfileMetricSelectionRXCommand.invoke();
        }
        return loadProfileMetricSelectionRXCommand;
    }

    public LoadProfileAndSeasonChangeOptionsCommand getLoadProfileAndSeasonChangeOptionsCommand() throws IOException {
        if (loadProfileAndSeasonChangeOptionsCommand==null) {
            loadProfileAndSeasonChangeOptionsCommand = new LoadProfileAndSeasonChangeOptionsCommand(this);
            loadProfileAndSeasonChangeOptionsCommand.invoke();
        }
        return loadProfileAndSeasonChangeOptionsCommand;
    }

    public TOUStatusCommand getTOUStatusCommand() throws IOException {
        if (touStatusCommand==null) {
            touStatusCommand = new TOUStatusCommand(this);
            touStatusCommand.invoke();
        }
        return touStatusCommand;
    }

    public ScaleFactorCommand getScaleFactorCommand() throws IOException {
        if (scaleFactorCommand==null) {
            scaleFactorCommand = new ScaleFactorCommand(this);
            scaleFactorCommand.invoke();
        }
        return scaleFactorCommand;
    }

    public KhValueCommand getKhValueCommand() throws IOException {
        if (khValueCommand==null) {
            khValueCommand = new KhValueCommand(this);
            khValueCommand.invoke();
        }
        return khValueCommand;
    }

    public ThermalKFactorCommand getThermalKFactorCommand() throws IOException {
        if (thermalKFactorCommand==null) {
            thermalKFactorCommand = new ThermalKFactorCommand(this);
            thermalKFactorCommand.invoke();
        }
        return thermalKFactorCommand;
    }

    public KFactorCommand getKFactorCommand() throws IOException {
        if (kFactorCommand==null) {
            kFactorCommand = new KFactorCommand(this);
            kFactorCommand.invoke();
        }
        return kFactorCommand;
    }

    public MeasurementUnitsCommand getMeasurementUnitsCommand() throws IOException {
        if (measurementUnitsCommand==null) {
            measurementUnitsCommand = new MeasurementUnitsCommand(this);
            measurementUnitsCommand.invoke();
        }
        return measurementUnitsCommand;
    }

    public DemandIntervalCommand getDemandIntervalCommand() throws IOException {
        if (demandIntervalCommand==null) {
            demandIntervalCommand = new DemandIntervalCommand(this);
            demandIntervalCommand.invoke();
        }
        return demandIntervalCommand;
    }

    public SerialNumberCommand getSerialNumberCommand() throws IOException {
        if (serialNumberCommand==null) {
            serialNumberCommand = new SerialNumberCommand(this);
            serialNumberCommand.invoke();
        }
        return serialNumberCommand;
    }

    public DeviceIDExtendedCommand getDeviceIDExtendedCommand() throws IOException {
        if (deviceIDExtendedCommand==null) {
            deviceIDExtendedCommand = new DeviceIDExtendedCommand(this);
            deviceIDExtendedCommand.invoke();
        }
        return deviceIDExtendedCommand;
    }

    public TOUAndLoadProfileOptions getTOUAndLoadProfileOptions() throws IOException {
        if (tOUAndLoadProfileOptions==null) {
            tOUAndLoadProfileOptions = new TOUAndLoadProfileOptions(this);
            tOUAndLoadProfileOptions.invoke();
        }
        return tOUAndLoadProfileOptions;
    }

    public S4sConfiguration getS4sConfiguration() throws IOException {
        if (s4sconfig==null) {
            s4sconfig = new S4sConfiguration(this);
            s4sconfig.invoke();
        }
        return s4sconfig;
    }

    public FirmwareVersionCommand getFirmwareVersionCommand() throws IOException {
        if (fvc==null) {
            fvc = new FirmwareVersionCommand(this);
            fvc.invoke();
        }
        return fvc;
    }



    public ErrorCodesCommand getErrorCodesCommand() throws IOException {
        ErrorCodesCommand ecc = new ErrorCodesCommand(this);
        ecc.invoke();
        return ecc;
    }


    public NegativeEnergyCommand getNegativeEnergyCommand() throws IOException {
        NegativeEnergyCommand nec = new NegativeEnergyCommand(this);
        nec.invoke();
        return nec;
    }

    public PresentDemandCommand getPresentDemandCommand() throws IOException {
        PresentDemandCommand pdc = new PresentDemandCommand(this);
        pdc.invoke();
        return pdc;
    }

    public HighestMaximumDemandsCommand getHighestMaximumDemandsCommand() throws IOException {
        HighestMaximumDemandsCommand hmdc = new HighestMaximumDemandsCommand(this);
        hmdc.invoke();
        return hmdc;
    }

    public ThirdMetricValuesCommand getThirdMetricValuesCommand() throws IOException {
        ThirdMetricValuesCommand tmv = new ThirdMetricValuesCommand(this);
        tmv.invoke();
        return tmv;
    }

    public CurrentSeasonCumulativeDemandDataDXCommand getCurrentSeasonCumulativeDemandDataDXCommand() throws IOException {
        CurrentSeasonCumulativeDemandDataDXCommand cscddc = new CurrentSeasonCumulativeDemandDataDXCommand(this);
        cscddc.invoke();
        return cscddc;
    }

    public CurrentSeasonCumDemandAndLastResetRXCommand getCurrentSeasonCumDemandAndLastResetRXCommand() throws IOException {
        CurrentSeasonCumDemandAndLastResetRXCommand o = new CurrentSeasonCumDemandAndLastResetRXCommand(this);
        o.invoke();
        return o;
    }

    public CurrentSeasonTOUDemandDataDXCommand getCurrentSeasonTOUDemandDataDXCommand() throws IOException {
        CurrentSeasonTOUDemandDataDXCommand o = new CurrentSeasonTOUDemandDataDXCommand(this);
        o.invoke();
        return o;
    }

    public CurrentSeasonLastResetValuesDXCommand getCurrentSeasonLastResetValuesDXCommand() throws IOException {
        CurrentSeasonLastResetValuesDXCommand o = new CurrentSeasonLastResetValuesDXCommand(this);
        o.invoke();
        return o;
    }

    public CurrentSeasonTOUDemandDataRXCommand getCurrentSeasonTOUDemandDataRXCommand() throws IOException {
        CurrentSeasonTOUDemandDataRXCommand o = new CurrentSeasonTOUDemandDataRXCommand(this);
        o.invoke();
        return o;
    }

    public PreviousSeasonDemandDataCommand getPreviousSeasonDemandDataCommand() throws IOException {
        PreviousSeasonDemandDataCommand o = new PreviousSeasonDemandDataCommand(this);
        o.invoke();
        return o;
    }

    public PreviousSeasonTOUDataDXCommand getPreviousSeasonTOUDataDXCommand() throws IOException {
        PreviousSeasonTOUDataDXCommand o = new PreviousSeasonTOUDataDXCommand(this);
        o.invoke();
        return o;
    }

    public PreviousSeasonLastResetValuesDXCommand getPreviousSeasonLastResetValuesDXCommand() throws IOException {
        PreviousSeasonLastResetValuesDXCommand o = new PreviousSeasonLastResetValuesDXCommand(this);
        o.invoke();
        return o;
    }

    public PreviousSeasonTOUDataRXCommand getPreviousSeasonTOUDataRXCommand() throws IOException {
        PreviousSeasonTOUDataRXCommand o = new PreviousSeasonTOUDataRXCommand(this);
        o.invoke();
        return o;
    }

    public PreviousIntervalDemandCommand getPreviousIntervalDemandCommand() throws IOException {
        PreviousIntervalDemandCommand o = new PreviousIntervalDemandCommand(this);
        o.invoke();
        return o;
    }

    public RateBinsAndTotalEnergyDXCommand getRateBinsAndTotalEnergyDXCommand() throws IOException {
        RateBinsAndTotalEnergyDXCommand o = new RateBinsAndTotalEnergyDXCommand(this);
        o.invoke();
        return o;
    }

    public RateBinsAndTotalEnergyRXCommand getRateBinsAndTotalEnergyRXCommand() throws IOException {
        RateBinsAndTotalEnergyRXCommand o = new RateBinsAndTotalEnergyRXCommand(this);
        o.invoke();
        return o;
    }

    public SelfReadDataDXCommand getSelfReadDataDXCommand(int selfReadIndex) throws IOException {
        SelfReadDataDXCommand o = new SelfReadDataDXCommand(this);
        o.setSelfReadIndex(selfReadIndex);
        o.invoke();
        return o;
    }

    public SelfReadDataRXCommand getSelfReadDataRXCommand(int selfReadIndex) throws IOException {
        SelfReadDataRXCommand o = new SelfReadDataRXCommand(this);
        o.setSelfReadIndex(selfReadIndex);
        o.invoke();
        return o;
    }

    public LoadProfileLimit getLoadProfileLimit() throws IOException {
        LoadProfileLimit o = new LoadProfileLimit(this);
        o.invoke();
        return o;
    }


    public LoadProfileDataCommand getLoadProfileDataCommand(int memorySize) throws IOException {
        LoadProfileDataCommand o = new LoadProfileDataCommand(this);
        o.setMemorySize(memorySize);
        o.invoke();
        return o;
    }

    public void logoff() throws IOException {
        LogoffCommand logoff = new LogoffCommand(this);
        logoff.invoke();
    }




    public void unlock(String password) throws IOException {
        UnlockCommand unlock = new UnlockCommand(this);
        unlock.setPassword(password);
        unlock.invoke();
    }

    public void modemUnlock(String modemPassword) throws IOException {
        ModemPasswordCommand unlock = new ModemPasswordCommand(this);
        unlock.setPassword(modemPassword);
        unlock.invoke();
    }

    public Date getTime() throws IOException {
        TimeCommand tc = new TimeCommand(this);
        tc.invoke();
        DateCommand dc = new DateCommand(this);
        dc.invoke();

        Calendar calTime = ProtocolUtils.getCleanCalendar(getS4s().getTimeZone());
        calTime.setTime(tc.getTime());

        Calendar calDate = ProtocolUtils.getCleanCalendar(getS4s().getTimeZone());
        calDate.setTime(dc.getDate());

        calDate.set(Calendar.SECOND,calTime.get(Calendar.SECOND));
        calDate.set(Calendar.MINUTE,calTime.get(Calendar.MINUTE));
        calDate.set(Calendar.HOUR_OF_DAY,calTime.get(Calendar.HOUR_OF_DAY));

        return calDate.getTime();
    }

    public void setTime() throws IOException {
        Calendar cal = ProtocolUtils.getCalendar(getS4s().getTimeZone());
        cal.add(Calendar.MILLISECOND, getS4s().getInfoTypeRoundtripCorrection());
//        unlock(getS4s().getInfoTypePassword());
        DateCommand dc = new DateCommand(this);
        dc.setDate(cal.getTime());
        dc.invoke();
        TimeCommand tc = new TimeCommand(this);
        tc.setTime(cal.getTime());
        tc.invoke();
    }

    public S4s getS4s() {
        return s4s;
    }

    private void setS4s(S4s s4s) {
        this.s4s = s4s;
    }


}
