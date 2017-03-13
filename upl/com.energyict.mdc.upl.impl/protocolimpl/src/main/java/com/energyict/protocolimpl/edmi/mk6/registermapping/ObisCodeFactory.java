/*
 * ObisCodeFactory.java
 *
 * Created on 24 maart 2006, 11:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.registermapping;

import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.edmi.common.CommandLineProtocol;
import com.energyict.protocolimpl.edmi.common.command.ReadCommand;
import com.energyict.protocolimpl.edmi.common.core.DataType;
import com.energyict.protocolimpl.edmi.common.core.DateTimeBuilder;
import com.energyict.protocolimpl.edmi.common.registermapping.BillingInfo;
import com.energyict.protocolimpl.edmi.common.registermapping.TOURegisterInfo;
import com.energyict.util.Pair;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author koen
 */
public class ObisCodeFactory {

    CommandLineProtocol protocol;
    List<TOURegisterInfo> touRegisterInfos;
    private BillingInfo billingInfo = null;
    private List<String> warningMessages;

    /**
     * Creates a new instance of ObisCodeFactory
     */
    public ObisCodeFactory(CommandLineProtocol protocol) {
        this.protocol = protocol;
        initTOURegisterInfos();
    }

    // tou register type
    private final int TYPE_ENERGY = 0;
    private final int TYPE_MAX_DEMAND = 1;

    // tou period
    private final int PERIOD_CURRENT = 0;
    private final int PERIOD_PREVIOUS1 = 2;
    private final int PERIOD_BILLING_TOTAL = 4;
    private final int PERIOD_TOTAL = 6;

    // tou channel
    private final int CHANNEL_START = 0;
    private final int CHANNEL_NR_OF_CHANNELS = 12;

    // tou register function
    private final int RATE_UNIFIED = 9;
    private final int RATE_START = 0;
    private final int RATE_NR_OF_RATES = 8;


    public void initTOURegisterInfos() {
        touRegisterInfos = new ArrayList<>();

        for (int channel = CHANNEL_START; channel < CHANNEL_NR_OF_CHANNELS; channel++) {
            int edmiEnergyRegisterId = getProtocol().getCommandFactory().getReadCommand(0xF780 + channel, DataType.X_HEX_LONG).getRegister().getBigDecimal().intValue();
//            int scalingCode = getProtocol().getCommandFactory().getReadCommand(0xF7C0 + channel, DataType.C_BYTE).getRegister().getBigDecimal().intValue();   //TODO: do we need to apply scaling?

            RegisterInf ri = RegisterFactory.getRegisterInf(edmiEnergyRegisterId & 0xFFFF); // get external register!

            if (ri != null) {
                // energy tou registers
                addTOURegisters(TYPE_ENERGY, channel, PERIOD_CURRENT, ri.getObisCField(), "Energy " + ri.getDescription() + " Current period");
                addTOURegisters(TYPE_ENERGY, channel, PERIOD_PREVIOUS1, ri.getObisCField(), "Energy " + ri.getDescription() + " Previous period");
                addTOURegisters(TYPE_ENERGY, channel, PERIOD_BILLING_TOTAL, ri.getObisCField(), "Energy " + ri.getDescription() + " Billing total period");
                addTOURegisters(TYPE_ENERGY, channel, PERIOD_TOTAL, ri.getObisCField(), "Energy " + ri.getDescription() + " Total period");

                // max demand registers
                addTOURegisters(TYPE_MAX_DEMAND, channel, PERIOD_CURRENT, ri.getObisCField(), "Max demand " + ri.getDescription() + " Current period");
                addTOURegisters(TYPE_MAX_DEMAND, channel, PERIOD_PREVIOUS1, ri.getObisCField(), "Max demand " + ri.getDescription() + " Previous period");
                addTOURegisters(TYPE_MAX_DEMAND, channel, PERIOD_BILLING_TOTAL, ri.getObisCField(), "Max demand " + ri.getDescription() + " Billing totalent period");
                addTOURegisters(TYPE_MAX_DEMAND, channel, PERIOD_TOTAL, ri.getObisCField(), "Max demand " + ri.getDescription() + " Total period");
            }
        }
    }

    private void addTOURegisters(int type, int channel, int period, int cField, String description) {
        boolean hasTimeOfMaxDemandDate = false;
        boolean hasBillingTimestampDate = false;

        int dField = 0;
        int fField = 255;
        switch (period) {
            case PERIOD_CURRENT: { // Time Integral 2, from begin of current billing period to the instantaneous time point
                fField = 255;
                hasBillingTimestampDate = false;
                Pair<Integer, Boolean> pair = calculateDFieldAndTimeOfMaxDemandBoolean(type, period, 9, 16);
                dField = pair.getFirst();
                hasTimeOfMaxDemandDate = pair.getLast();
            }
            break;

            case PERIOD_PREVIOUS1: { // Time Integral 2, from begin of previous billing period to the end of the previous billing period
                fField = 0;
                hasBillingTimestampDate = true;
                Pair<Integer, Boolean> pair = calculateDFieldAndTimeOfMaxDemandBoolean(type, period, 9, 16);
                dField = pair.getFirst();
                hasTimeOfMaxDemandDate = pair.getLast();
            }
            break;

            case PERIOD_BILLING_TOTAL: { // Time Integral 1 , from the start of measurements to the end of the previous billing period
                fField = 0;
                hasBillingTimestampDate = true;
                Pair<Integer, Boolean> pair = calculateDFieldAndTimeOfMaxDemandBoolean(type, period, 8, 2);
                dField = pair.getFirst();
                hasTimeOfMaxDemandDate = pair.getLast();
            }
            break;

            case PERIOD_TOTAL: {  // Time Integral 1 , from the start of measurements to the instantaneous time point
                fField = 255;
                hasBillingTimestampDate = false;
                Pair<Integer, Boolean> pair = calculateDFieldAndTimeOfMaxDemandBoolean(type, period, 8, 2);
                dField = pair.getFirst();
                hasTimeOfMaxDemandDate = pair.getLast();
            }
            break;
        }

        touRegisterInfos.add(
                new TOURegisterInfo(
                        new ObisCode(1, 1, cField, dField, 0, fField),
                        buildEdmiEnergyRegisterId(type, channel, period, RATE_UNIFIED),
                        "EDMI descr: " + description + " total",
                        hasTimeOfMaxDemandDate,
                        hasBillingTimestampDate
                )
        );
        for (int rate = RATE_START; rate < RATE_NR_OF_RATES; rate++) {
            touRegisterInfos.add(
                    new TOURegisterInfo(
                            new ObisCode(1, 1, cField, dField, rate + 1, fField),
                            buildEdmiEnergyRegisterId(type, channel, period, rate),
                            "EDMI descr: " + description + " rate " + rate,
                            hasTimeOfMaxDemandDate,
                            hasBillingTimestampDate
                    )
            );
        }
    }

    private Pair<Integer, Boolean> calculateDFieldAndTimeOfMaxDemandBoolean(int type, int period, int energyDField, int maxDemandDField) {
        switch (type) {
            case TYPE_ENERGY: {
                return new Pair<>(energyDField, false);
            }
            case TYPE_MAX_DEMAND: {
                return new Pair<>(maxDemandDField, period == PERIOD_CURRENT || period == PERIOD_PREVIOUS1);
            }
        }
        return new Pair<>(0, false);
    }

    // The TOU registerid for the EDMI MK6 is of the following format: 0x0000 => aaaa bbbb cccc dddd
    //	aaaa    =>	type of data
    //					0 = accumulated energy
    //					1 = maximum demand value
    //					8 = time of maximum demand
    //	bbbb	=>	channel
    //					User defined channels in range 0 - 11
    //	ccccc	=>	period
    //                  0 = current
    //                  2 = previous
    //                  4 = billing total
    //                  6 = total
    //	dddd	=>	function
    //					0-7 = Rates 1 - 8
    //                  9 = Unified rate
    private int buildEdmiEnergyRegisterId(int type, int channel, int period, int function) {
        type &= 0x000F;
        channel &= 0x000F;
        period &= 0x000F;
        function &= 0x000F;
        return (type << 12) | (channel << 8) | (period << 4) | function;
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws NoSuchRegisterException {
        TOURegisterInfo touri = findTOURegisterInfo(obisCode);
        ReadCommand rc = getProtocol().getCommandFactory().getReadCommand(touri.getEdmiEnergyRegisterId());
        Date to = null;

        if (touri.hasBillingTimestampDate()) {
            to = doValidateDate(getBillingInfo().getToDate());
        }

        if (touri.hasTimeOfMaxDemandDate()) {
            Date eventDate = doValidateDate(getProtocol().getCommandFactory().getReadCommand(touri.getMK6TimeOfMaxDemandRegisterId(), DataType.T_TIME_DATE_SINCE__1_97).getRegister().getDate());
            return new RegisterValue(obisCode, new Quantity(rc.getRegister().getBigDecimal(), rc.getUnit()), eventDate, null, to);
        } else {
            return new RegisterValue(obisCode, new Quantity(rc.getRegister().getBigDecimal(), rc.getUnit()), null, null, to);
        }
    }

    private TOURegisterInfo findTOURegisterInfo(ObisCode obisCode) throws NoSuchRegisterException {
        for (TOURegisterInfo touri : touRegisterInfos) {
            if (touri.getObisCode().equals(obisCode)) {
                return touri;
            }
        }
        throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
    }

    public String getRegisterInfoDescription() {
        StringBuilder strBuff = new StringBuilder();
        for (TOURegisterInfo touri : touRegisterInfos) {
            strBuff.append(touri.getObisCode().toString()).append(", ").append(touri.getObisCode().getDescription()).append(", ").append(touri.getDescription()).append("\n");
        }
        return strBuff.toString();
    }

    // Check if date is valid.
    // The MK10 meter returns 1 January 1996 00:00 when the time is invalid.
    private Date doValidateDate(Date date) {
        if (date == null) {
            return null;
        }
        if (date.getTime() == DateTimeBuilder.getEpochMillisFor1Jan1996(getProtocol().getTimeZone())) {
            return null;
        } else {
            return date;
        }
    }

    public BillingInfo getBillingInfo() {
        if (billingInfo == null) {
            billingInfo = new BillingInfo(getProtocol().getCommandFactory());
        }
        return billingInfo;
    }

    public CommandLineProtocol getProtocol() {
        return protocol;
    }

    public List<String> getWarningMessages() {
        if (this.warningMessages == null) {
            this.warningMessages = new ArrayList<>();
        }
        return warningMessages;
    }
}