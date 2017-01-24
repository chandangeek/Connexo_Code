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

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.edmi.mk6.MK6;
import com.energyict.protocolimpl.edmi.mk6.command.ReadCommand;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author koen
 */
public class ObisCodeFactory implements Serializable{

    /** Generated SerialVersionUID */
	private static final long serialVersionUID = -7421692268267373026L;
	private MK6 mk6;
    private List touRegisterInfos;
    private BillingInfo billingInfo=null;

    /** Creates a new instance of ObisCodeFactory */
    public ObisCodeFactory(MK6 mk6) throws IOException {
        this.mk6=mk6;
        initTOURegisterInfos();
    }

    // tou register type
    private static final int TYPE_ENERGY=0;
    private static final int TYPE_MAX_DEMAND=1;
    private static final int TYPE_TIME_OF_MAX_DEMAND=8;


    // tou period
    private static final int PERIOD_CURRENT=0;
    private static final int PERIOD_PREVIOUS1=2;
    private static final int PERIOD_BILLING_TOTAL=4;
    private static final int PERIOD_TOTAL=6;

    // tou channel
    private static final int CHANNEL_START=0;
    private static final int CHANNEL_NR_OF_CHANNELS=12;

    // tou register function
    private static final int RATE_UNIFIED=9;
    private static final int RATE_START=0;
    private static final int RATE_NR_OF_RATES=8;


    public void initTOURegisterInfos() throws IOException {
        touRegisterInfos = new ArrayList();
        for (int channel=CHANNEL_START;channel<CHANNEL_NR_OF_CHANNELS;channel++) {
            int edmiEnergyRegisterId = mk6.getCommandFactory().getReadCommand(0xF780+channel).getRegister().getBigDecimal().intValue();
            RegisterInf ri = RegisterFactory.getRegisterInf(edmiEnergyRegisterId&0xFFFF); // get external register!

            if (ri!=null) {
                // energy tou registers
                addTOURegisters(TYPE_ENERGY, channel, PERIOD_CURRENT, ri.getObisCField(), "Energy "+ri.getDescription()+" Current period");
                addTOURegisters(TYPE_ENERGY, channel, PERIOD_PREVIOUS1, ri.getObisCField(), "Energy "+ri.getDescription()+" Previous period");
                addTOURegisters(TYPE_ENERGY, channel, PERIOD_BILLING_TOTAL, ri.getObisCField(), "Energy "+ri.getDescription()+" Billing total period");
                addTOURegisters(TYPE_ENERGY, channel, PERIOD_TOTAL, ri.getObisCField(), "Energy "+ri.getDescription()+" Total period");

                // max demand registers
                addTOURegisters(TYPE_MAX_DEMAND, channel, PERIOD_CURRENT, ri.getObisCField(), "Max demand "+ri.getDescription()+" Current period");
                addTOURegisters(TYPE_MAX_DEMAND, channel, PERIOD_PREVIOUS1, ri.getObisCField(), "Max demand "+ri.getDescription()+" Previous period");
                addTOURegisters(TYPE_MAX_DEMAND, channel, PERIOD_BILLING_TOTAL, ri.getObisCField(), "Max demand "+ri.getDescription()+" Billing totalent period");
                addTOURegisters(TYPE_MAX_DEMAND, channel, PERIOD_TOTAL, ri.getObisCField(), "Max demand "+ri.getDescription()+" Total period");
            }
        }
    }

    private int buildEdmiEnergyRegisterId(int type, int channel, int period, int function) {
        return (type << 12) | (channel << 8) | ( period << 4) | function;
    }

    private void addTOURegisters(int type, int channel, int period, int cField, String description) {

        boolean timeOfMaxDemand=false;
        boolean billingTimestampFrom=false;
        boolean billingTimestampTo=false;

        int dField=0;
        int eField=255;


        switch(period) {

            case PERIOD_CURRENT: {
                billingTimestampFrom=true; // beginning of the current billing period...
                eField=255;
                switch(type) {
                    case TYPE_ENERGY: { // Time Integral 2, from begin of current billing period to the instantaneous time point
                        dField=9;
                    } break; // TYPE_ENERGY

                    case TYPE_MAX_DEMAND: {
                        timeOfMaxDemand=true;
                        dField=16;
                    } break; // TYPE_MAX_DEMAND
                } //switch(type)
            } break; // PERIOD_CURRENT

            case PERIOD_PREVIOUS1: { // Time Integral 2, from begin of previous billing period to the end of the previous billing period
                eField=0;
                billingTimestampFrom=true;
                billingTimestampTo=true;
                switch(type) {
                    case TYPE_ENERGY: {
                        dField=9;
                    } break; // TYPE_ENERGY

                    case TYPE_MAX_DEMAND: {
                        timeOfMaxDemand=true;
                        dField=16;
                    } break; // TYPE_MAX_DEMAND
                } //switch(type)
            } break; // PERIOD_PREVIOUS1

            case PERIOD_BILLING_TOTAL: { // Time Integral 1 , from the start of measurements to the end of the previous billing period
                billingTimestampTo=true;
                eField=0;
                switch(type) {
                    case TYPE_ENERGY: {
                        dField=8;
                    } break; // TYPE_ENERGY

                    case TYPE_MAX_DEMAND: { // Cumulative max demand
                        dField=2;
                    } break; // TYPE_MAX_DEMAND
                } //switch(type)
            } break; // PERIOD_BILLING_TOTAL

            case PERIOD_TOTAL: {  // Time Integral 1 , from the start of measurements to the instantaneous time point
                eField=255;
                switch(type) {
                    case TYPE_ENERGY: {
                        dField=8;
                    } break; // TYPE_ENERGY

                    case TYPE_MAX_DEMAND: { // Cumulative max demand
                        dField=2;
                    } break; // TYPE_MAX_DEMAND
                } //switch(type)
            } break; // PERIOD_TOTAL

        } // switch(period)

        touRegisterInfos.add(new TOURegisterInfo(new ObisCode(1,1,cField,dField,0,eField), buildEdmiEnergyRegisterId(type, channel, period, RATE_UNIFIED), "EDMI descr: "+description+" total",timeOfMaxDemand,billingTimestampFrom,billingTimestampTo));
        for (int rate=RATE_START;rate<RATE_NR_OF_RATES;rate++) {
            touRegisterInfos.add(new TOURegisterInfo(new ObisCode(1,1,cField,dField,rate+1,eField), buildEdmiEnergyRegisterId(type, channel, period, rate), "EDMI descr: "+description+" rate "+rate,timeOfMaxDemand,billingTimestampFrom,billingTimestampTo));
        }
    }

    public String getRegisterInfoDescription() {
        StringBuffer strBuff = new StringBuffer();
        Iterator it = touRegisterInfos.iterator();
        while(it.hasNext()) {
            TOURegisterInfo touri = (TOURegisterInfo)it.next();
            strBuff.append(touri.getObisCode().toString()+", "+touri.getObisCode().getDescription()+", "+touri.getDescription()+"\n");
        }
        return strBuff.toString();
    }


    private int findEdmiEnergyRegisterId(ObisCode obisCode) throws IOException {
        Iterator it = touRegisterInfos.iterator();
        while(it.hasNext()) {
            TOURegisterInfo touri = (TOURegisterInfo)it.next();
            if (touri.getObisCode().equals(obisCode)) {
                return touri.getEdmiEnergyRegisterId();
            }
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }

    private TOURegisterInfo findTOURegisterInfo(ObisCode obisCode) throws IOException {
        Iterator it = touRegisterInfos.iterator();
        while(it.hasNext()) {
            TOURegisterInfo touri = (TOURegisterInfo)it.next();
            if (touri.getObisCode().equals(obisCode)) {
                return touri;
            }
        }
        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        TOURegisterInfo touri = findTOURegisterInfo(obisCode);
        ReadCommand rc = mk6.getCommandFactory().getReadCommand(touri.getEdmiEnergyRegisterId());

        Date from=null;
        Date to=null;

        if (touri.isBillingTimestampFrom() && touri.isBillingTimestampTo()) {
            from = getBillingInfo().getFromDate();
            to = getBillingInfo().getToDate();
        }
        if (touri.isBillingTimestampFrom() && !touri.isBillingTimestampTo()) {
            from = getBillingInfo().getToDate(); // !!!!!!
        }
        if (!touri.isBillingTimestampFrom() && touri.isBillingTimestampTo()) {
            to = getBillingInfo().getToDate(); // !!!!!!
        }

        if (touri.isTimeOfMaxDemand()) {
            Date eventDate = mk6.getCommandFactory().getReadCommand(touri.getEdmiEnergyRegisterId() - 0x1000 + 0x8000).getRegister().getDate();
            return new RegisterValue(obisCode,new Quantity(rc.getRegister().getBigDecimal(),rc.getUnit()),eventDate,from,to);
        } else {
            return new RegisterValue(obisCode,new Quantity(rc.getRegister().getBigDecimal(),rc.getUnit()),null,from,to);
        }
    }

    public BillingInfo getBillingInfo() throws IOException {
        if (billingInfo==null) {
            billingInfo = new BillingInfo(mk6.getCommandFactory());
//            System.out.println("KV_DEBUG> "+billingInfo);
        }
        return billingInfo;
    }

}
