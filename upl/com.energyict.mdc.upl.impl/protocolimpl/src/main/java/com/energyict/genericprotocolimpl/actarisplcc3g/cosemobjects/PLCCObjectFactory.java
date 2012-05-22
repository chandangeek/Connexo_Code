/*
 * PLCCObjectFactory.java
 *
 * Created on 16 oktober 2007, 14:43
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects;

import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.genericprotocolimpl.actarisplcc3g.Concentrator;

import java.io.IOException;
import java.util.Date;

/**
 * @author kvds
 */

public class PLCCObjectFactory {

    private Concentrator concentrator;
    private CosemObjectFactory cosemObjectFactory;
    private PLCCFTPServerId pLCCFTPServerId = null;

    /**
     * Creates a new instance of PLCCObjectFactory
     */
    public PLCCObjectFactory(
            Concentrator concentrator, CosemObjectFactory cosemObjectFactory) {

        this.concentrator = concentrator;
        this.cosemObjectFactory = cosemObjectFactory;
    }

    public Concentrator getConcentrator() {
        return concentrator;
    }

    public CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    public PLCCSelectedMeter getPLCCSelectedMeter(String serialNumber) throws IOException {
        PLCCSelectedMeter o = new PLCCSelectedMeter(this);
        o.setMeterSerialNumber(serialNumber);
        o.invoke();
        return o;
    }

    public PLCCCurrentDateTime getPLCCCurrentDateTime() throws IOException {
        return new PLCCCurrentDateTime(this);
    }

    public PLCCMeterCurrentDateTime getPLCCMeterCurrentDateTime() throws IOException {
        return new PLCCMeterCurrentDateTime(this);
    }

    public PLCCMeterStatus getPLCCMeterStatus() throws IOException {
        PLCCMeterStatus o = new PLCCMeterStatus(this);
        o.invoke();
        return o;
    }

    public PLCCMeterErrorCodeRegister getPLCCMeterErrorCodeRegister() throws IOException {
        PLCCMeterErrorCodeRegister o = new PLCCMeterErrorCodeRegister(this);
        o.invoke();
        return o;
    }

    public PLCCMeterList getPLCCMeterList() throws IOException {
        PLCCMeterList o = new PLCCMeterList(this);
        o.invoke();
        return o;
    }

    public PLCCPLCEquipmentList getPLCCPLCEquipmentList() throws IOException {
        PLCCPLCEquipmentList o = new PLCCPLCEquipmentList(this);
        o.invoke();
        return o;
    }

    public PLCCMeterContactorState getPLCCMeterContactorState() throws IOException {
        PLCCMeterContactorState o = new PLCCMeterContactorState(this);
        o.invoke();
        return o;
    }

    public PLCCFTPServerId getPLCCFTPServerId() throws IOException {
        if (pLCCFTPServerId == null) {
            pLCCFTPServerId = new PLCCFTPServerId(this);
            pLCCFTPServerId.invoke();
        }
        return pLCCFTPServerId;
    }

    public PLCCMeterActivityCalendar getPLCCMeterActivityCalendar() throws IOException {
        PLCCMeterActivityCalendar o = new PLCCMeterActivityCalendar(this);
        o.invoke();
        return o;
    }

    public PLCCMeterActivityCalendar writePLCCMeterActivityCalendar(com.energyict.protocolimpl.edf.messages.objects.ActivityCalendar ac) throws IOException {
        PLCCMeterActivityCalendar o = new PLCCMeterActivityCalendar(this);
        o.invoke();
        o.writeActivityCalendar(ac);
        return o;
    }

    public PLCCMeterMovingPeak getPLCCMeterMovingPeak() throws IOException {
        PLCCMeterMovingPeak o = new PLCCMeterMovingPeak(this);
        o.invoke();
        return o;
    }

    public PLCCMeterMovingPeak writePLCCMeterMovingPeak(com.energyict.protocolimpl.edf.messages.objects.MovingPeak mp) throws IOException {
        PLCCMeterMovingPeak o = new PLCCMeterMovingPeak(this);
        o.invoke();
        o.writeMovingPeak(mp);
        return o;
    }


    public PLCCMeterDemandManagement getPLCCMeterDemandManagement() throws IOException {
        PLCCMeterDemandManagement o = new PLCCMeterDemandManagement(this);
        o.invoke();
        return o;
    }

    public PLCCMeterTICConfiguration getPLCCMeterTICConfiguration() throws IOException {
        PLCCMeterTICConfiguration pLCCMeterTICConfiguration = new PLCCMeterTICConfiguration(this);
        pLCCMeterTICConfiguration.invoke();
        return pLCCMeterTICConfiguration;
    }


    public PLCCMeterCurrentRatio getPLCCMeterCurrentRatio() throws IOException {
        PLCCMeterCurrentRatio pLCCMeterCurrentRatio = new PLCCMeterCurrentRatio(this);
        pLCCMeterCurrentRatio.invoke();
        return pLCCMeterCurrentRatio;
    }

    public PLCCMeterIdentification getPLCCMeterIdentification() throws IOException {
        PLCCMeterIdentification pLCCMeterIdentification = new PLCCMeterIdentification(this);
        pLCCMeterIdentification.invoke();
        return pLCCMeterIdentification;
    }


    // Electricity registers
    public PLCCMeterEnergyRegister getPLCCMeterEnergyRegister(int tariff) throws IOException {
        PLCCMeterEnergyRegister o = new PLCCMeterEnergyRegister(this);
        o.setTariff(tariff);
        o.invoke();
        return o;
    }

    // load profiles
    public PLCCMeterDailyEnergyValueProfile getPLCCMeterDailyEnergyValueProfile(Date from) throws IOException {
        return getPLCCMeterDailyEnergyValueProfile(from, null);
    }

    public PLCCMeterDailyEnergyValueProfile getPLCCMeterDailyEnergyValueProfile(Date from, Date to) throws IOException {
        PLCCMeterDailyEnergyValueProfile o = new PLCCMeterDailyEnergyValueProfile(this);
        o.setFrom(from);
        o.setTo(to);
        o.invoke();
        return o;
    }

    public PLCCMeterLoadProfileEnergy getPLCCMeterLoadProfileEnergy() throws IOException {
        return getPLCCMeterLoadProfileEnergy(null, null);
    }

    public PLCCMeterLoadProfileEnergy getPLCCMeterLoadProfileEnergy(Date from, Date to) throws IOException {
        PLCCMeterLoadProfileEnergy o = new PLCCMeterLoadProfileEnergy(this);

        o.setCompressed(Integer.parseInt((String) concentrator.getCurrentSelectedDevice().getProperties().getProperty("LoadProfileCompressed", "0")) == 1);
        o.setFrom(from);
        o.setTo(to);
        o.invoke();
        return o;
    }

    public PLCCMeterLogbook getPLCCMeterLogbook(Date from, Date to) throws IOException {
        PLCCMeterLogbook o = new PLCCMeterLogbook(this);
        o.setFrom(from);
        o.setTo(to);
        o.invoke();
        return o;
    }

    // power quality
    public PLCCMeterThresholdForSag getPLCCMeterThresholdForSag() throws IOException {
        PLCCMeterThresholdForSag o = new PLCCMeterThresholdForSag(this);
        o.invoke();
        return o;
    }

    public PLCCMeterThresholdForSwell getPLCCMeterThresholdForSwell() throws IOException {
        PLCCMeterThresholdForSwell o = new PLCCMeterThresholdForSwell(this);
        o.invoke();
        return o;
    }

    public PLCCMeterTimeIntegralForSagMeasurement getPLCCMeterTimeIntegralForSagMeasurement() throws IOException {
        PLCCMeterTimeIntegralForSagMeasurement o = new PLCCMeterTimeIntegralForSagMeasurement(this);
        o.invoke();
        return o;
    }

    public PLCCMeterTimeIntegralForSwellMeasurement getPLCCMeterTimeIntegralForSwellMeasurement() throws IOException {
        PLCCMeterTimeIntegralForSwellMeasurement o = new PLCCMeterTimeIntegralForSwellMeasurement(this);
        o.invoke();
        return o;
    }

    public PLCCMeterTimeThresholdForLongPowerFailure getPLCCMeterTimeThresholdForLongPowerFailure() throws IOException {
        PLCCMeterTimeThresholdForLongPowerFailure o = new PLCCMeterTimeThresholdForLongPowerFailure(this);
        o.invoke();
        return o;
    }

    public PLCCMeterTimeIntegralForInstantaneousDemand getPLCCMeterTimeIntegralForInstantaneousDemand() throws IOException {
        PLCCMeterTimeIntegralForInstantaneousDemand o = new PLCCMeterTimeIntegralForInstantaneousDemand(this);
        o.invoke();
        return o;
    }

    public PLCCMeterNumberOfLongPowerFailures getPLCCMeterNumberOfLongPowerFailures() throws IOException {
        PLCCMeterNumberOfLongPowerFailures o = new PLCCMeterNumberOfLongPowerFailures(this);
        o.invoke();
        return o;
    }


    public PLCCMeterNumberOfShortPowerFailures getPLCCMeterNumberOfShortPowerFailures() throws IOException {
        PLCCMeterNumberOfShortPowerFailures o = new PLCCMeterNumberOfShortPowerFailures(this);
        o.invoke();
        return o;
    }

    public PLCCMeterNumberOfSag getPLCCMeterNumberOfSag() throws IOException {
        PLCCMeterNumberOfSag o = new PLCCMeterNumberOfSag(this);
        o.invoke();
        return o;
    }

    public PLCCMeterNumberOfSwell getPLCCMeterNumberOfSwell() throws IOException {
        PLCCMeterNumberOfSwell o = new PLCCMeterNumberOfSwell(this);
        o.invoke();
        return o;
    }

    public PLCCMeterMaximumVoltage getPLCCMeterMaximumVoltage() throws IOException {
        PLCCMeterMaximumVoltage o = new PLCCMeterMaximumVoltage(this);
        o.invoke();
        return o;
    }

    public PLCCMeterMinimumVoltage getPLCCMeterMinimumVoltage() throws IOException {
        PLCCMeterMinimumVoltage o = new PLCCMeterMinimumVoltage(this);
        o.invoke();
        return o;
    }

    public PLCCMeterInstantaneousDemand getPLCCMeterInstantaneousDemand() throws IOException {
        PLCCMeterInstantaneousDemand o = new PLCCMeterInstantaneousDemand(this);
        o.invoke();
        return o;
    }

}