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
import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command.CurrentSeasonCumDemandAndLastResetRXCommand;
import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command.CurrentSeasonTOUDemandDataRXCommand;
import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command.HighestMaximumDemandsCommand;
import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command.NegativeEnergyCommand;
import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command.PreviousSeasonDemandDataCommand;
import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command.PreviousSeasonTOUDataRXCommand;
import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command.RateBinsAndTotalEnergyRXCommand;
import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command.SelfReadDataRXCommand;
import com.energyict.protocolimpl.landisgyr.s4.protocol.dgcom.command.ThirdMetricValuesCommand;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author Koen
 */
public class RegisterMapperRX extends RegisterMapper {

    final int MAX_RATES=5;

    // not mapped are: - 4 highest max demands (only 1 of 5 highest max demands is mapped)
    //                 - power factors


    //private PreviousIntervalDemandCommand previousIntervalDemandCommand; // 14h
    private PreviousSeasonTOUDataRXCommand previousSeasonTOUDataRXCommand; // ABh
    private RateBinsAndTotalEnergyRXCommand RateBinsAndTotalEnergyRXCommand; // A0h
    private NegativeEnergyCommand negativeEnergyCommand; // 53h
    private HighestMaximumDemandsCommand highestMaximumDemandsCommand; // 74h
    private CurrentSeasonCumDemandAndLastResetRXCommand currentSeasonCumDemandAndLastResetRXCommand; // AAh
    private CurrentSeasonTOUDemandDataRXCommand currentSeasonTOUDemandDataRXCommand; // ACh
    private PreviousSeasonDemandDataCommand previousSeasonDemandDataCommand; // 4Ch
    private ThirdMetricValuesCommand thirdMetricValuesCommand; // 99h

    /** Creates a new instance of RegisterMapper */
    public RegisterMapperRX(S4 s4) throws IOException {
        super(s4);
    }

    public void buildRegisterValues(int billingPoint) throws IOException {

        if (billingPoint == 255) {
            if (!current) {
                //previousIntervalDemandCommand = s4.getCommandFactory().getPreviousIntervalDemandCommand();
                setPreviousSeasonTOUDataRXCommand(s4.getCommandFactory().getPreviousSeasonTOUDataRXCommand());
                setRateBinsAndTotalEnergyRXCommand(s4.getCommandFactory().getRateBinsAndTotalEnergyRXCommand());
                setNegativeEnergyCommand(s4.getCommandFactory().getNegativeEnergyCommand());
                setHighestMaximumDemandsCommand(s4.getCommandFactory().getHighestMaximumDemandsCommand());
                setCurrentSeasonCumDemandAndLastResetRXCommand(s4.getCommandFactory().getCurrentSeasonCumDemandAndLastResetRXCommand());
                setCurrentSeasonTOUDemandDataRXCommand(s4.getCommandFactory().getCurrentSeasonTOUDemandDataRXCommand());
                setPreviousSeasonDemandDataCommand(s4.getCommandFactory().getPreviousSeasonDemandDataCommand());
                if (s4.getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00)
                   setThirdMetricValuesCommand(s4.getCommandFactory().getThirdMetricValuesCommand());
                current = true;
                doBuildRegisterValues(billingPoint,null);
            } // if (!current)
        }
        else {

            if (billingPoint>(getNrOfBillingPeriods()-1))
                throw new NoSuchRegisterException("Billing set "+billingPoint+" does not exist!");
            if (!selfread[billingPoint]) {
                SelfReadDataRXCommand srd = s4.getCommandFactory().getSelfReadDataRXCommand(billingPoint);
                //previousIntervalDemandCommand = srd.getPreviousIntervalDemandCommand();
                setPreviousSeasonTOUDataRXCommand(srd.getPreviousSeasonTOUDataRXCommand());
                setRateBinsAndTotalEnergyRXCommand(srd.getRateBinsAndTotalEnergyRXCommand());
                setNegativeEnergyCommand(srd.getNegativeEnergyCommand());
                setHighestMaximumDemandsCommand(srd.getHighestMaximumDemandsCommand());
                setCurrentSeasonCumDemandAndLastResetRXCommand(srd.getCurrentSeasonCumDemandAndLastResetRXCommand());
                setCurrentSeasonTOUDemandDataRXCommand(srd.getCurrentSeasonTOUDemandDataRXCommand());
                setPreviousSeasonDemandDataCommand(srd.getPreviousSeasonDemandDataCommand());
                if (s4.getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00)
                   setThirdMetricValuesCommand(srd.getThirdMetricValuesCommand());
                selfread[billingPoint] = true;
                doBuildRegisterValues(billingPoint,srd.getSelfReadTimestamp());
            } // if (!selfread[billingPoint])
        }
    }

    protected String getBillingExtensionDescription() throws IOException {
        return "The meter contains "+getNrOfBillingPeriods()+" selfread sets of registers (billing point sets)\n";
    }

    public void doBuildRegisterValues(int billingPoint,Date toTime) throws IOException {

        BigDecimal bd;

        // Mapping from the dialogs in L&G 1132Com software
        // ********************************************************************************************************************************
        // energy & demand dialog
        // Total kWh
        bd = toEngineeringEnergy(BigDecimal.valueOf(getRateBinsAndTotalEnergyRXCommand().getTotalKWHInPulses()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.8.0."+billingPoint),new Quantity(bd, Unit.get("kWh")),null,toTime)));

        // Selectable Metric kMh
        bd = toEngineeringEnergy(BigDecimal.valueOf(getRateBinsAndTotalEnergyRXCommand().getTotalKMHInPulses()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.100.8.0."+billingPoint),new Quantity(bd,s4.getCommandFactory().getMeasurementUnitsCommand().getSelectableMetricUnit(true)),null,toTime),"selectable metric"));

        // Negative kWh
        bd = toEngineeringEnergy(BigDecimal.valueOf(getNegativeEnergyCommand().getNegativeEnergyInPulses()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.2.8.0."+billingPoint),new Quantity(bd,Unit.get("kWh")),null,toTime)));

        // leading kvarh
        bd = toEngineeringEnergy(BigDecimal.valueOf(getNegativeEnergyCommand().getLeadingkvarhInPulses()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.6.8.0."+billingPoint),new Quantity(bd,Unit.get("kvarh")),null,toTime)));

        //KV 26072007
        if (s4.getCommandFactory().getFirmwareVersionCommand().getNumericFirmwareVersion()>=3.00) {
            // third metric kM3h
            bd = toEngineeringEnergy(BigDecimal.valueOf(getThirdMetricValuesCommand().getTotalkM3h()));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.102.8.0."+billingPoint),new Quantity(bd,s4.getCommandFactory().getMeasurementUnitsCommand().getThirdMetricUnit(true)),null,toTime),"Thrird selectable metric"));
            // third metric kM3 max
            bd = toEngineeringDemand(BigDecimal.valueOf(getThirdMetricValuesCommand().getMaxkM3InPulses()));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.102.6.0."+billingPoint),new Quantity(bd,s4.getCommandFactory().getMeasurementUnitsCommand().getThirdMetricUnit(false)),getThirdMetricValuesCommand().getMaxkM3Timestamp(),toTime),"Third selectable metric"));
        }


        // cumulative kW
        bd = toEngineeringDemand(BigDecimal.valueOf(getPreviousSeasonDemandDataCommand().getCurrentSeasonCumulativeKWInPulses()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.2.0."+billingPoint),new Quantity(bd,Unit.get("kW")),null,toTime)));

        // cumulative kM selectable metric
        bd = toEngineeringDemand(BigDecimal.valueOf(getPreviousSeasonDemandDataCommand().getCurrentSeasonCumulativeKMInPulses()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.100.2.0."+billingPoint),new Quantity(bd,s4.getCommandFactory().getMeasurementUnitsCommand().getSelectableMetricUnit(false)),null,toTime),"selectable metric"));

        // highest max demand in selectable metric
        bd = toEngineeringDemand(BigDecimal.valueOf(getHighestMaximumDemandsCommand().getHighestMaxKW()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.101.6.0."+billingPoint),
                                             new Quantity(bd,s4.getCommandFactory().getMeasurementUnitsCommand().getBillingMetricUnit(false)),
                                             getHighestMaximumDemandsCommand().getHighestMaxKWTimestamp(),toTime),"selectable metric highest max demand"));
        // max demand
        bd = toEngineeringDemand(BigDecimal.valueOf(getHighestMaximumDemandsCommand().getMaxkWInPulses()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.6.0."+billingPoint),
                                             new Quantity(bd,Unit.get("kW")),
                                             getHighestMaximumDemandsCommand().getMaxkWTimestamp(),toTime)));

        // max demand kM selectable metric
        bd = toEngineeringDemand(BigDecimal.valueOf(getHighestMaximumDemandsCommand().getMaxkMInPulses()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.100.6.0."+billingPoint),
                                             new Quantity(bd,s4.getCommandFactory().getMeasurementUnitsCommand().getSelectableMetricUnit(false)),
                                             getHighestMaximumDemandsCommand().getMaxkMTimestamp(),toTime),"selectable metric"));


        bd = toEngineeringDemand(BigDecimal.valueOf(getHighestMaximumDemandsCommand().getHighestCoincident()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.100.128.0."+billingPoint),
                                             new Quantity(bd,s4.getCommandFactory().getMeasurementUnitsCommand().getBillingMetricUnit(false))
                                             ,null,toTime),"selectable metric coincident at highest max demand"));




        // ********************************************************************************************************************************
        // previous season energy & demand dialog
        // Total kWh
        bd = toEngineeringEnergy(BigDecimal.valueOf(getPreviousSeasonTOUDataRXCommand().getTotalKWHInPulses()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.8.6."+billingPoint),new Quantity(bd,Unit.get("kWh")),null,toTime)));

        // Selectable Metric kMh
        bd = toEngineeringEnergy(BigDecimal.valueOf(getPreviousSeasonTOUDataRXCommand().getTotalKMHInPulses()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.100.8.6."+billingPoint),new Quantity(bd,s4.getCommandFactory().getMeasurementUnitsCommand().getSelectableMetricUnit(true)),null,toTime),"selectable metric"));

        // Negative kWh
        bd = toEngineeringEnergy(BigDecimal.valueOf(getPreviousSeasonTOUDataRXCommand().getTotalNegativeKWHInPulses()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.2.8.6."+billingPoint),new Quantity(bd,Unit.get("kWh")),null,toTime)));

        // leading kvarh
        bd = toEngineeringEnergy(BigDecimal.valueOf(getPreviousSeasonTOUDataRXCommand().getLeadingKVARHInPulses()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.6.8.6."+billingPoint),new Quantity(bd,Unit.get("kvarh")),null,toTime)));

        // cumulative kW
        bd = toEngineeringDemand(BigDecimal.valueOf(getPreviousSeasonDemandDataCommand().getPreviousSeasonCumulativeKWInPulses()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.2.6."+billingPoint),new Quantity(bd,Unit.get("kW")),null,toTime)));

        // cumulative kM selectable metric
        bd = toEngineeringDemand(BigDecimal.valueOf(getPreviousSeasonDemandDataCommand().getPreviousSeasonCumulativeKMInPulses()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.100.2.6."+billingPoint),new Quantity(bd,s4.getCommandFactory().getMeasurementUnitsCommand().getSelectableMetricUnit(false)),null,toTime),"selectable metric"));

        // coincident
        bd = toEngineeringDemand(BigDecimal.valueOf(getPreviousSeasonDemandDataCommand().getPreviousSeasonCoincidentDemandInPulses()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.101.128.6."+billingPoint),
                                             new Quantity(bd,s4.getCommandFactory().getMeasurementUnitsCommand().getBillingMetricUnit(false)),
                                             getPreviousSeasonTOUDataRXCommand().getHighestMaxKWTimestamp(),toTime),"selectable metric highest max demand"));

        // highest max demand in selectable metric
        bd = toEngineeringDemand(BigDecimal.valueOf(getPreviousSeasonTOUDataRXCommand().getHighestMaxKW()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.101.6.6."+billingPoint),
                                             new Quantity(bd,s4.getCommandFactory().getMeasurementUnitsCommand().getBillingMetricUnit(false)),
                                             getPreviousSeasonTOUDataRXCommand().getHighestMaxKWTimestamp(),toTime),"selectable metric highest max demand"));
        // max demand
        bd = toEngineeringDemand(BigDecimal.valueOf(getPreviousSeasonTOUDataRXCommand().getMaximumKWInPulses()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.6.6."+billingPoint),
                                             new Quantity(bd,Unit.get("kW")),
                                             getPreviousSeasonTOUDataRXCommand().getTimestampOfMaximumKW(),toTime)));

        // coincident value
        bd = toEngineeringDemand(BigDecimal.valueOf(getPreviousSeasonDemandDataCommand().getPreviousSeasonCoincidentDemandInPulses()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.128.6."+billingPoint),
                                             new Quantity(bd,Unit.get("kW")),
                                             getPreviousSeasonTOUDataRXCommand().getTimestampOfMaximumKW(),toTime)));

        // max demand kM selectable metric
        bd = toEngineeringDemand(BigDecimal.valueOf(getPreviousSeasonTOUDataRXCommand().getMaximumKMInPulses()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.100.6.6."+billingPoint),
                                             new Quantity(bd,s4.getCommandFactory().getMeasurementUnitsCommand().getSelectableMetricUnit(false)),
                                             getPreviousSeasonTOUDataRXCommand().getTimestampOfMaximumKM(),toTime),"selectable metric"));


        bd = toEngineeringDemand(BigDecimal.valueOf(getPreviousSeasonTOUDataRXCommand().getHighestCoincident()));
        registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.100.128.6."+billingPoint),
                                             new Quantity(bd,s4.getCommandFactory().getMeasurementUnitsCommand().getBillingMetricUnit(false)),null,toTime),"selectable metric coincident at highest max demand"));


        // TOU data dialog
        for (int i=0;i<MAX_RATES;i++) {
            // cumulative kW
            bd = toEngineeringDemand(BigDecimal.valueOf(getCurrentSeasonCumDemandAndLastResetRXCommand().getCumKWDemandInPulsesRates()[i]));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.2."+(1+i)+"."+billingPoint),new Quantity(bd,Unit.get("kW")),null,toTime)));
            // cumulative kM
            bd = toEngineeringDemand(BigDecimal.valueOf(getCurrentSeasonCumDemandAndLastResetRXCommand().getCumKMDemandInPulsesRates()[i]));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.100.2."+(1+i)+"."+billingPoint),new Quantity(bd,s4.getCommandFactory().getMeasurementUnitsCommand().getSelectableMetricUnit(false)),null,toTime),"selectable metric"));
            // maximum kW
            bd = toEngineeringDemand(BigDecimal.valueOf(getCurrentSeasonTOUDemandDataRXCommand().getMaximumKWsRates()[i]));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.6."+(1+i)+"."+billingPoint),new Quantity(bd,Unit.get("kW")),
                    getCurrentSeasonTOUDemandDataRXCommand().getMaximumKWtimestampsRates()[i],toTime)));
            // maximum kM
            bd = toEngineeringDemand(BigDecimal.valueOf(getCurrentSeasonTOUDemandDataRXCommand().getMaximumKMsRates()[i]));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.100.6."+(1+i)+"."+billingPoint),new Quantity(bd,s4.getCommandFactory().getMeasurementUnitsCommand().getSelectableMetricUnit(false)),
                    getCurrentSeasonTOUDemandDataRXCommand().getMaximumKMtimestampsRates()[i],toTime),"selectable metric"));
            // coincident
            bd = toEngineeringDemand(BigDecimal.valueOf(getCurrentSeasonTOUDemandDataRXCommand().getMaximumKMsRates()[i]));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.100.128."+(1+i)+"."+billingPoint),new Quantity(bd,s4.getCommandFactory().getMeasurementUnitsCommand().getSelectableMetricUnit(false)),
                    getCurrentSeasonTOUDemandDataRXCommand().getMaximumKMtimestampsRates()[i],toTime),"selectable metric coincident demand"));
            // kWh
            bd = toEngineeringEnergy(BigDecimal.valueOf(getRateBinsAndTotalEnergyRXCommand().getRatekWHInPulses()[i]));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.8."+(1+i)+"."+billingPoint),new Quantity(bd,Unit.get("kWh")),null,toTime)));
            // kMh
            bd = toEngineeringEnergy(BigDecimal.valueOf(getRateBinsAndTotalEnergyRXCommand().getRatekWHInPulses()[i]));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.100.8."+(1+i)+"."+billingPoint),new Quantity(bd,s4.getCommandFactory().getMeasurementUnitsCommand().getSelectableMetricUnit(false)),null,toTime)));
        } // for (int i=0;i<MAX_RATES;i++)

        // Previous season TOU data dialog
        for (int i=0;i<MAX_RATES;i++) {
            // cumulative kW
            bd = toEngineeringDemand(BigDecimal.valueOf(getPreviousSeasonTOUDataRXCommand().getCumulativeKWDemandInPulsesRates()[i]));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.2."+(7+i)+"."+billingPoint),new Quantity(bd,Unit.get("kW")),null,toTime)));
            // cumulative kM
            bd = toEngineeringDemand(BigDecimal.valueOf(getPreviousSeasonTOUDataRXCommand().getCumulativeKMDemandInPulsesRates()[i]));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.100.2."+(7+i)+"."+billingPoint),new Quantity(bd,s4.getCommandFactory().getMeasurementUnitsCommand().getSelectableMetricUnit(false)),null,toTime),"selectable metric"));
            // maximum kW
            bd = toEngineeringDemand(BigDecimal.valueOf(getPreviousSeasonTOUDataRXCommand().getMaximumKWInPulsesRate()[i]));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.6."+(7+i)+"."+billingPoint),new Quantity(bd,Unit.get("kW")),
                    getPreviousSeasonTOUDataRXCommand().getTimestampMaximumKWRate()[i])));
            // maximum kM
            bd = toEngineeringDemand(BigDecimal.valueOf(getPreviousSeasonTOUDataRXCommand().getMaximumKMInPulsesRate()[i]));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.100.6."+(7+i)+"."+billingPoint),new Quantity(bd,s4.getCommandFactory().getMeasurementUnitsCommand().getSelectableMetricUnit(false)),
                    getPreviousSeasonTOUDataRXCommand().getTimestampMaximumKMRate()[i],toTime),"selectable metric"));
            // kWh
            bd = toEngineeringEnergy(BigDecimal.valueOf(getPreviousSeasonTOUDataRXCommand().getKWHInPulsesRate()[i]));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.1.8."+(7+i)+"."+billingPoint),new Quantity(bd,Unit.get("kWh")),null,toTime)));

            // kMh
            bd = toEngineeringEnergy(BigDecimal.valueOf(getPreviousSeasonTOUDataRXCommand().getKMHInPulsesRate()[i]));
            registers.add(new Register(new RegisterValue(ObisCode.fromString("1.1.100.8."+(7+i)+"."+billingPoint),new Quantity(bd,s4.getCommandFactory().getMeasurementUnitsCommand().getSelectableMetricUnit(false)),null,toTime)));
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

    private PreviousSeasonTOUDataRXCommand getPreviousSeasonTOUDataRXCommand() {
        return previousSeasonTOUDataRXCommand;
    }

    private void setPreviousSeasonTOUDataRXCommand(PreviousSeasonTOUDataRXCommand previousSeasonTOUDataRXCommand) {
        this.previousSeasonTOUDataRXCommand = previousSeasonTOUDataRXCommand;
    }

    private RateBinsAndTotalEnergyRXCommand getRateBinsAndTotalEnergyRXCommand() {
        return RateBinsAndTotalEnergyRXCommand;
    }

    private void setRateBinsAndTotalEnergyRXCommand(RateBinsAndTotalEnergyRXCommand RateBinsAndTotalEnergyRXCommand) {
        this.RateBinsAndTotalEnergyRXCommand = RateBinsAndTotalEnergyRXCommand;
    }

    private NegativeEnergyCommand getNegativeEnergyCommand() {
        return negativeEnergyCommand;
    }

    private void setNegativeEnergyCommand(NegativeEnergyCommand negativeEnergyCommand) {
        this.negativeEnergyCommand = negativeEnergyCommand;
    }

    private HighestMaximumDemandsCommand getHighestMaximumDemandsCommand() {
        return highestMaximumDemandsCommand;
    }

    private void setHighestMaximumDemandsCommand(HighestMaximumDemandsCommand highestMaximumDemandsCommand) {
        this.highestMaximumDemandsCommand = highestMaximumDemandsCommand;
    }

    private CurrentSeasonCumDemandAndLastResetRXCommand getCurrentSeasonCumDemandAndLastResetRXCommand() {
        return currentSeasonCumDemandAndLastResetRXCommand;
    }

    private void setCurrentSeasonCumDemandAndLastResetRXCommand(CurrentSeasonCumDemandAndLastResetRXCommand currentSeasonCumDemandAndLastResetRXCommand) {
        this.currentSeasonCumDemandAndLastResetRXCommand = currentSeasonCumDemandAndLastResetRXCommand;
    }

    private CurrentSeasonTOUDemandDataRXCommand getCurrentSeasonTOUDemandDataRXCommand() {
        return currentSeasonTOUDemandDataRXCommand;
    }

    private void setCurrentSeasonTOUDemandDataRXCommand(CurrentSeasonTOUDemandDataRXCommand currentSeasonTOUDemandDataRXCommand) {
        this.currentSeasonTOUDemandDataRXCommand = currentSeasonTOUDemandDataRXCommand;
    }

    private PreviousSeasonDemandDataCommand getPreviousSeasonDemandDataCommand() {
        return previousSeasonDemandDataCommand;
    }

    private void setPreviousSeasonDemandDataCommand(PreviousSeasonDemandDataCommand previousSeasonDemandDataCommand) {
        this.previousSeasonDemandDataCommand = previousSeasonDemandDataCommand;
    }

    public ThirdMetricValuesCommand getThirdMetricValuesCommand() {
        return thirdMetricValuesCommand;
    }

    private void setThirdMetricValuesCommand(ThirdMetricValuesCommand thirdMetricValuesCommand) {
        this.thirdMetricValuesCommand = thirdMetricValuesCommand;
    }

}
