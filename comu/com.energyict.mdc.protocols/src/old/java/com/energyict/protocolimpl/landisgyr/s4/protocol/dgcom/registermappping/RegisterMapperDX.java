/*
 * RegisterMapper.java
 *
 * Created on 12 juni 2006, 9:42
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.registermappping;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.S4;
import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command.CurrentSeasonCumulativeDemandDataDXCommand;
import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command.CurrentSeasonLastResetValuesDXCommand;
import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command.CurrentSeasonTOUDemandDataDXCommand;
import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command.HighestMaximumDemandsCommand;
import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command.PreviousSeasonLastResetValuesDXCommand;
import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command.PreviousSeasonTOUDataDXCommand;
import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command.RateBinsAndTotalEnergyDXCommand;
import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command.SelfReadDataDXCommand;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class RegisterMapperDX extends RegisterMapper {

    final int MAX_RATES=4;


    // not mapped
    //                 - power factors

    private HighestMaximumDemandsCommand highestMaximumDemandsCommand; // 84h
    private RateBinsAndTotalEnergyDXCommand rateBinsAndTotalEnergyDXCommand; // 05h
    private CurrentSeasonTOUDemandDataDXCommand currentSeasonTOUDemandDataDXCommand; // 61h
    private CurrentSeasonCumulativeDemandDataDXCommand currentSeasonCumulativeDemandDataDXCommand; // 5Bh
    private CurrentSeasonLastResetValuesDXCommand currentSeasonLastResetValuesDXCommand; // 85h
    private PreviousSeasonTOUDataDXCommand previousSeasonTOUDataDXCommand; // 5Ch
    private PreviousSeasonLastResetValuesDXCommand previousSeasonLastResetValuesDXCommand; // 86h


    /** Creates a new instance of RegisterMapper */
    public RegisterMapperDX(S4 s4) throws IOException {
        super(s4);
    }

    protected String getBillingExtensionDescription() throws IOException {
        return "The meter contains "+getNrOfBillingPeriods()+" selfread sets of registers (billing point sets)\nHowever, the DX type meter does contain following registers in the billing points: negative energy,  cumulative kW (current and previous season), max demand kW in previous season\n";
    }

    protected void buildRegisterValues(int billingPoint) throws IOException {

        if (billingPoint == 255) {
            if (!current) {
                setHighestMaximumDemandsCommand(s4.getCommandFactory().getHighestMaximumDemandsCommand());
                setRateBinsAndTotalEnergyDXCommand(s4.getCommandFactory().getRateBinsAndTotalEnergyDXCommand());
                setCurrentSeasonTOUDemandDataDXCommand(s4.getCommandFactory().getCurrentSeasonTOUDemandDataDXCommand());
                setCurrentSeasonCumulativeDemandDataDXCommand(s4.getCommandFactory().getCurrentSeasonCumulativeDemandDataDXCommand());
                setCurrentSeasonLastResetValuesDXCommand(s4.getCommandFactory().getCurrentSeasonLastResetValuesDXCommand());
                setPreviousSeasonTOUDataDXCommand(s4.getCommandFactory().getPreviousSeasonTOUDataDXCommand());
                setPreviousSeasonLastResetValuesDXCommand(s4.getCommandFactory().getPreviousSeasonLastResetValuesDXCommand());
                current = true;
                doBuildRegisterValues(billingPoint, null);
            } // if (!current)
        }
        else {
            if (billingPoint>(getNrOfBillingPeriods()-1))
                throw new NoSuchRegisterException("Billing set "+billingPoint+" does not exist!");
            if (!selfread[billingPoint]) {
                SelfReadDataDXCommand srd = s4.getCommandFactory().getSelfReadDataDXCommand(billingPoint);
                setHighestMaximumDemandsCommand(srd.getHighestMaximumDemandsCommand());
                setRateBinsAndTotalEnergyDXCommand(srd.getRateBinsAndTotalEnergyDXCommand());
                setCurrentSeasonTOUDemandDataDXCommand(srd.getCurrentSeasonTOUDemandDataDXCommand());
                setCurrentSeasonCumulativeDemandDataDXCommand(srd.getCurrentSeasonCumulativeDemandDataDXCommand());
                setCurrentSeasonLastResetValuesDXCommand(srd.getCurrentSeasonLastResetValuesDXCommand());
                setPreviousSeasonTOUDataDXCommand(srd.getPreviousSeasonTOUDataDXCommand());
                setPreviousSeasonLastResetValuesDXCommand(srd.getPreviousSeasonLastResetValuesDXCommand());
                selfread[billingPoint] = true;
                doBuildRegisterValues(billingPoint,srd.getSelfReadTimestamp());
            } // if (!selfread[billingPoint])
        }
    }

    public void doBuildRegisterValues(int billingPoint,Date toTime) throws IOException {

        BigDecimal bd;

        // Mapping from the dialogs in L&G 1132Com software
        // ********************************************************************************************************************************
        // energy & demand dialog
        // Total kWh
        bd = toEngineeringEnergy(BigDecimal.valueOf(getRateBinsAndTotalEnergyDXCommand().getTotalKWHInPulses()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.8.0."+billingPoint),new Quantity(bd, Unit.get("kWh")),null,toTime)));

        if (billingPoint == 255) { // negative energy does not exist in the selfread data
            // Negative kWh
            bd = toEngineeringEnergy(BigDecimal.valueOf(s4.getCommandFactory().getNegativeEnergyCommand().getNegativeEnergyInPulses()));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.2.8.0.255"),new Quantity(bd,Unit.get("kWh")),null,toTime)));
        }

        if (billingPoint == 255) { // cumilative demand does not exist in the selfread data
            // cumulative kW
            bd = toEngineeringDemand(BigDecimal.valueOf(s4.getCommandFactory().getPreviousSeasonDemandDataCommand().getCurrentSeasonCumulativeKWInPulses()));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.2.0.255"),new Quantity(bd,Unit.get("kW")),null,toTime)));
        }

        // max demand
        bd = toEngineeringDemand(BigDecimal.valueOf(getHighestMaximumDemandsCommand().getMaxkWInPulses()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.6.0."+billingPoint),
                                             new Quantity(bd,Unit.get("kW")),
                                             getHighestMaximumDemandsCommand().getMaxkWTimestamp(),toTime)));


        // ********************************************************************************************************************************
        // previous season energy & demand dialog
        // Total kWh
        bd = toEngineeringEnergy(BigDecimal.valueOf(getPreviousSeasonTOUDataDXCommand().getTotalKWHInPulses()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.8.6."+billingPoint),new Quantity(bd,Unit.get("kWh")),null,toTime)));

        // Negative kWh
        bd = toEngineeringEnergy(BigDecimal.valueOf(getPreviousSeasonTOUDataDXCommand().getTotalNegativeKWHInPulses()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.2.8.6."+billingPoint),new Quantity(bd,Unit.get("kWh")),null,toTime)));

        if (billingPoint == 255) { // cumilative demand and maximum demand do not exist in the selfread data
            // cumulative kW
            bd = toEngineeringDemand(BigDecimal.valueOf(s4.getCommandFactory().getPreviousSeasonDemandDataCommand().getPreviousSeasonCumulativeKWInPulses()));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.2.0.255"),new Quantity(bd,Unit.get("kW")),null,toTime)));

            // max demand
            bd = toEngineeringDemand(BigDecimal.valueOf(s4.getCommandFactory().getPreviousSeasonDemandDataCommand().getPreviousSeasonMaximumKWInPulses()));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.6.6.255"),
                                                 new Quantity(bd,s4.getCommandFactory().getMeasurementUnitsCommand().getSelectableMetricUnit(false)),
                                                 s4.getCommandFactory().getPreviousSeasonDemandDataCommand().getPreviousSeasonTimestampMaximumKW(),toTime)));
        }

        // TOU data dialog
        for (int i=0;i<MAX_RATES;i++) {
            // cumulative kW
            bd = toEngineeringDemand(BigDecimal.valueOf(getCurrentSeasonCumulativeDemandDataDXCommand().getCumulativeDemandRatesInPulses()[i]));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.2."+(1+i)+"."+billingPoint),new Quantity(bd,Unit.get("kW")),null,toTime)));
            // maximum kW
            bd = toEngineeringDemand(BigDecimal.valueOf(getCurrentSeasonTOUDemandDataDXCommand().getMaximumKWsRates()[i]));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.6."+(1+i)+"."+billingPoint),new Quantity(bd,Unit.get("kW")),
                    getCurrentSeasonTOUDemandDataDXCommand().getMaximumKWtimestampsRates()[i],toTime)));
            // kWh
            bd = toEngineeringEnergy(BigDecimal.valueOf(getRateBinsAndTotalEnergyDXCommand().getRatekWHInPulses()[i]));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.8."+(1+i)+"."+billingPoint),new Quantity(bd,Unit.get("kWh")),null,toTime)));
        } // for (int i=0;i<MAX_RATES;i++)

        // Previous season TOU data dialog
        for (int i=0;i<MAX_RATES;i++) {
            // cumulative kW
            bd = toEngineeringDemand(BigDecimal.valueOf(getPreviousSeasonTOUDataDXCommand().getCumulativeKWInPulses()[i]));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.2."+(7+i)+"."+billingPoint),new Quantity(bd,Unit.get("kW")),null,toTime)));
            // maximum kW
            bd = toEngineeringDemand(BigDecimal.valueOf(getPreviousSeasonTOUDataDXCommand().getMaximumDemandKW()[i]));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.6."+(7+i)+"."+billingPoint),new Quantity(bd,Unit.get("kW")),
                    getPreviousSeasonTOUDataDXCommand().getTimestampMaximumDemand()[i],toTime)));
            // kWh
            bd = toEngineeringEnergy(BigDecimal.valueOf(getPreviousSeasonTOUDataDXCommand().getKWHInPulses()[i]));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.8."+(7+i)+"."+billingPoint),new Quantity(bd,Unit.get("kWh")),null,toTime)));
        } // for (int i=0;i<MAX_RATES;i++)


    } // public void buildRegisterValues() throws IOException


    private BigDecimal toEngineeringEnergy(BigDecimal bd) throws IOException {
        BigDecimal kf = s4.getCommandFactory().getKFactorCommand().getBdKFactor();
        bd = bd.multiply(kf); // pulses -> wH
        bd = bd.movePointLeft(3); // divide by 1000 Wh -> kWh
        return bd;
    }

    private BigDecimal toEngineeringDemand(BigDecimal bd) throws IOException {
        BigDecimal kf = s4.getCommandFactory().getKFactorCommand().getBdKFactor();
        bd = bd.multiply(kf); // pulses -> wH
        bd = bd.movePointLeft(3); // divide by 1000 Wh -> kWh
        bd = bd.multiply(BigDecimal.valueOf(s4.getCommandFactory().getDemandIntervalCommand().getNrOfIntervalsPerHour())); // multiply by the number of intervals/hour energy -> demand
        return bd;
    }

    private HighestMaximumDemandsCommand getHighestMaximumDemandsCommand() {
        return highestMaximumDemandsCommand;
    }

    private void setHighestMaximumDemandsCommand(HighestMaximumDemandsCommand highestMaximumDemandsCommand) {
        this.highestMaximumDemandsCommand = highestMaximumDemandsCommand;
    }

    private RateBinsAndTotalEnergyDXCommand getRateBinsAndTotalEnergyDXCommand() {
        return rateBinsAndTotalEnergyDXCommand;
    }

    public void setRateBinsAndTotalEnergyDXCommand(RateBinsAndTotalEnergyDXCommand rateBinsAndTotalEnergyDXCommand) {
        this.rateBinsAndTotalEnergyDXCommand = rateBinsAndTotalEnergyDXCommand;
    }

    private CurrentSeasonTOUDemandDataDXCommand getCurrentSeasonTOUDemandDataDXCommand() {
        return currentSeasonTOUDemandDataDXCommand;
    }

    private void setCurrentSeasonTOUDemandDataDXCommand(CurrentSeasonTOUDemandDataDXCommand currentSeasonTOUDemandDataDXCommand) {
        this.currentSeasonTOUDemandDataDXCommand = currentSeasonTOUDemandDataDXCommand;
    }

    private CurrentSeasonCumulativeDemandDataDXCommand getCurrentSeasonCumulativeDemandDataDXCommand() {
        return currentSeasonCumulativeDemandDataDXCommand;
    }

    private void setCurrentSeasonCumulativeDemandDataDXCommand(CurrentSeasonCumulativeDemandDataDXCommand currentSeasonCumulativeDemandDataDXCommand) {
        this.currentSeasonCumulativeDemandDataDXCommand = currentSeasonCumulativeDemandDataDXCommand;
    }

    private CurrentSeasonLastResetValuesDXCommand getCurrentSeasonLastResetValuesDXCommand() {
        return currentSeasonLastResetValuesDXCommand;
    }

    private void setCurrentSeasonLastResetValuesDXCommand(CurrentSeasonLastResetValuesDXCommand currentSeasonLastResetValuesDXCommand) {
        this.currentSeasonLastResetValuesDXCommand = currentSeasonLastResetValuesDXCommand;
    }

    private PreviousSeasonTOUDataDXCommand getPreviousSeasonTOUDataDXCommand() {
        return previousSeasonTOUDataDXCommand;
    }

    private void setPreviousSeasonTOUDataDXCommand(PreviousSeasonTOUDataDXCommand previousSeasonTOUDataDXCommand) {
        this.previousSeasonTOUDataDXCommand = previousSeasonTOUDataDXCommand;
    }

    private PreviousSeasonLastResetValuesDXCommand getPreviousSeasonLastResetValuesDXCommand() {
        return previousSeasonLastResetValuesDXCommand;
    }

    private void setPreviousSeasonLastResetValuesDXCommand(PreviousSeasonLastResetValuesDXCommand previousSeasonLastResetValuesDXCommand) {
        this.previousSeasonLastResetValuesDXCommand = previousSeasonLastResetValuesDXCommand;
    }




}
