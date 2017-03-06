/*
 * ObisCodeFactory.java
 *
 * Created on 24 maart 2006, 11:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk10.registermapping;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.edmi.common.CommandLineProtocol;
import com.energyict.protocolimpl.edmi.common.command.ReadCommand;
import com.energyict.protocolimpl.edmi.common.core.DateTimeBuilder;
import com.energyict.protocolimpl.edmi.common.core.TOUChannelTypeParser;
import com.energyict.util.Pair;

import java.math.BigDecimal;
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
    private final int PERIOD_PREVIOUS1 = 1;
    private final int PERIOD_BILLING_TOTAL = 14;
    private final int PERIOD_TOTAL = 15;

    // tou channel
    private final int CHANNEL_START = 0;
    private final int CHANNEL_NR_OF_CHANNELS = 16;

    // tou register function
    private final int RATE_UNIFIED = 0;
    private final int RATE_START = 1;

    public void initTOURegisterInfos() {
        touRegisterInfos = new ArrayList<>();
        TOUChannelTypeParser tou_ctp;

        for (int channel = CHANNEL_START; channel < CHANNEL_NR_OF_CHANNELS; channel++) {
            int c_definitions = getProtocol().getCommandFactory().getReadCommand(MK10Register.TOU_CHANNEL_DEFINITIONS + channel).getRegister().getBigDecimal().intValue();
            tou_ctp = new TOUChannelTypeParser(c_definitions);

            if (tou_ctp.isChannel() && (tou_ctp.getObisCField() > 0)) {
                int obisc = tou_ctp.getObisCField();
                int rates = tou_ctp.getRates() + 1; // Total amount of rates + unified rate
                int dps = tou_ctp.getDecimalPointScaling();
                Unit unit = tou_ctp.getUnit();

                String name1 = "Energy " + tou_ctp.getName() + " Current period";
                String name2 = "Energy " + tou_ctp.getName() + " Previous period";
                String name3 = "Energy " + tou_ctp.getName() + " Billing total period";
                String name4 = "Energy " + tou_ctp.getName() + " Total period";

                String name5 = "Max demand " + tou_ctp.getName() + " Current period";
                String name6 = "Max demand " + tou_ctp.getName() + " Previous period";
                String name7 = "Max demand " + tou_ctp.getName() + " Billing totalent period";
                String name8 = "Max demand " + tou_ctp.getName() + " Total period";

                // energy tou registers
                addTOURegisters(TYPE_ENERGY, channel, PERIOD_CURRENT, obisc, name1, rates, dps, unit);
                addTOURegisters(TYPE_ENERGY, channel, PERIOD_PREVIOUS1, obisc, name2, rates, dps, unit);
                addTOURegisters(TYPE_ENERGY, channel, PERIOD_BILLING_TOTAL, obisc, name3, rates, dps, unit);
                addTOURegisters(TYPE_ENERGY, channel, PERIOD_TOTAL, obisc, name4, rates, dps, unit);

                // max demand registers
                addTOURegisters(TYPE_MAX_DEMAND, channel, PERIOD_CURRENT, obisc, name5, rates, dps, unit);
                addTOURegisters(TYPE_MAX_DEMAND, channel, PERIOD_PREVIOUS1, obisc, name6, rates, dps, unit);
                addTOURegisters(TYPE_MAX_DEMAND, channel, PERIOD_BILLING_TOTAL, obisc, name7, rates, dps, unit);
                addTOURegisters(TYPE_MAX_DEMAND, channel, PERIOD_TOTAL, obisc, name8, rates, dps, unit);
            }
        }
    }

    private void addTOURegisters(int type, int channel, int period, int cField, String description, int numberOfRates, int decimal, Unit unit) {
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
                        hasBillingTimestampDate,
                        decimal,
                        unit
                )
        );
        for (int rate = RATE_START; rate < numberOfRates; rate++) {
            touRegisterInfos.add(
                    new TOURegisterInfo(
                            new ObisCode(1, 1, cField, dField, rate, fField),
                            buildEdmiEnergyRegisterId(type, channel, period, rate),
                            "EDMI descr: " + description + " rate " + rate,
                            hasTimeOfMaxDemandDate,
                            hasBillingTimestampDate,
                            decimal,
                            unit
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

    // The TOU registerid for the EDMI MK10 is of the following format: 0x0000 => 0aab bbbc cccc dddd
    //	aa		=>	type of data
    //					0 = accumulated
    //					1 = maximum demand value
    //					3 = time of maximum demand
    //	bbbb	=>	specified period
    //					0 = current period
    //					1-13 = previous periods
    //					14 = billing total
    //					15 = total
    //	ccccc	=>	TOU channel
    //	dddd	=>	rate number
    //					0 = unified rate
    //					1-8 = specified rate
    private int buildEdmiEnergyRegisterId(int type, int channel, int period, int rate) {
        type &= 0x0003;
        period &= 0x000F;
        channel &= 0x001F;
        rate &= 0x000F;
        return (type << 13) | (period << 9) | (channel << 4) | rate;
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws NoSuchRegisterException {
        BigDecimal registerValue;
        TOURegisterInfo touri = findTOURegisterInfo(obisCode);
        ReadCommand rc = getProtocol().getCommandFactory().getReadCommand(touri.getEdmiEnergyRegisterId());
        Date to = null;
        int dp = touri.getDecimalPoint();
        Unit unit = touri.getUnit();
        int scaler = unit.getScale();

        if (touri.hasBillingTimestampDate()) {
            to = doValidateDate(getBillingInfo().getToDate());
        }

        registerValue = rc.getRegister().getBigDecimal();
        registerValue = registerValue.movePointLeft(scaler);
        registerValue = registerValue.setScale(dp, BigDecimal.ROUND_HALF_UP);

        if (touri.hasTimeOfMaxDemandDate()) {
            Date eventDate = doValidateDate(getProtocol().getCommandFactory().getReadCommand(touri.getEdmiMaxDemandRegisterId()).getRegister().getDate());
            return new RegisterValue(obisCode, new Quantity(registerValue, unit), eventDate, null, to);
        } else {
            return new RegisterValue(obisCode, new Quantity(registerValue, unit), null, null, to);
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
}