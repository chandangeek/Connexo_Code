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

import com.energyict.cbo.*;
import com.energyict.obis.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.edmi.mk10.command.*;
import com.energyict.protocolimpl.edmi.mk10.core.*;
import java.io.*;
import java.util.*;
import com.energyict.protocolimpl.edmi.mk10.*;

/**
 *
 * @author koen
 */
public class ObisCodeFactory {
    
    MK10 mk10;
    List touRegisterInfos;
    private BillingInfo billingInfo=null;
    
    /** Creates a new instance of ObisCodeFactory */
    public ObisCodeFactory(MK10 mk10) throws IOException {
        this.mk10=mk10;
        initTOURegisterInfos();
    }
    
    // tou register type
    private final int TYPE_ENERGY=0;
    private final int TYPE_MAX_DEMAND=1;
    private final int TYPE_TIME_OF_MAX_DEMAND=2;
    
    
    // tou period
    private final int PERIOD_CURRENT=0;
    private final int PERIOD_PREVIOUS1=1;
    private final int PERIOD_BILLING_TOTAL=14;
    private final int PERIOD_TOTAL=15;
    
    // tou channel
    private final int CHANNEL_START=0;
    private final int CHANNEL_NR_OF_CHANNELS=6;
    
    // tou register function
    private final int RATE_UNIFIED=0;
    private final int RATE_START=1;
    
    public void initTOURegisterInfos() throws IOException {
        touRegisterInfos = new ArrayList();
        TOUChannelTypeParser tou_ctp = null;
        
        for (int channel=CHANNEL_START;channel<CHANNEL_NR_OF_CHANNELS;channel++) {
        	int c_definitions = mk10.getCommandFactory().getReadCommand(MK10Register.TOU_CHANNEL_DEFINITIONS + channel).getRegister().getBigDecimal().intValue();
        	
        	tou_ctp = new TOUChannelTypeParser(c_definitions);
        	
        	if (tou_ctp.isChannel() && (tou_ctp.getObisCField()>0)) {

        		this.mk10.sendDebug(" *************************************** TOU CHANNEL: " + channel + " Rates: " + tou_ctp.getRates());
        		
        		// energy tou registers
                addTOURegisters(TYPE_ENERGY, channel, PERIOD_CURRENT, tou_ctp.getObisCField(), "Energy "+tou_ctp.getName()+" Current period", tou_ctp.getRates());
                addTOURegisters(TYPE_ENERGY, channel, PERIOD_PREVIOUS1, tou_ctp.getObisCField(), "Energy "+tou_ctp.getName()+" Previous period", tou_ctp.getRates());
                addTOURegisters(TYPE_ENERGY, channel, PERIOD_BILLING_TOTAL, tou_ctp.getObisCField(), "Energy "+tou_ctp.getName()+" Billing total period", tou_ctp.getRates());
                addTOURegisters(TYPE_ENERGY, channel, PERIOD_TOTAL, tou_ctp.getObisCField(), "Energy "+tou_ctp.getName()+" Total period", tou_ctp.getRates());
                
                // max demand registers
                addTOURegisters(TYPE_MAX_DEMAND, channel, PERIOD_CURRENT, tou_ctp.getObisCField(), "Max demand "+tou_ctp.getName()+" Current period", tou_ctp.getRates());
                addTOURegisters(TYPE_MAX_DEMAND, channel, PERIOD_PREVIOUS1, tou_ctp.getObisCField(), "Max demand "+tou_ctp.getName()+" Previous period", tou_ctp.getRates());
                addTOURegisters(TYPE_MAX_DEMAND, channel, PERIOD_BILLING_TOTAL, tou_ctp.getObisCField(), "Max demand "+tou_ctp.getName()+" Billing totalent period", tou_ctp.getRates());
                addTOURegisters(TYPE_MAX_DEMAND, channel, PERIOD_TOTAL, tou_ctp.getObisCField(), "Max demand "+tou_ctp.getName()+" Total period", tou_ctp.getRates());

        	}
        }
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
        return (type << 13) | ( period << 9) | (channel << 5) | rate;
    }
    
    private void addTOURegisters(int type, int channel, int period, int cField, String description, int numberofrates) {
        
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
        
        touRegisterInfos.add(new TOURegisterInfo(new ObisCode(1,1,cField,dField,0,eField), buildEdmiEnergyRegisterId(type, channel, period, RATE_UNIFIED), "EDMI descr: "+description+" total",timeOfMaxDemand,billingTimestampFrom));
        for (int rate=RATE_START;rate<=numberofrates;rate++) {
            touRegisterInfos.add(new TOURegisterInfo(new ObisCode(1,1,cField,dField,rate+1,eField), buildEdmiEnergyRegisterId(type, channel, period, rate), "EDMI descr: "+description+" rate "+rate,timeOfMaxDemand,billingTimestampFrom));
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
                this.mk10.sendDebug("  ------------------------------" + touri.getEdmiEnergyRegisterId() );
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
        this.mk10.sendDebug("----------------------------------------" + touri.getEdmiEnergyRegisterId());
        ReadCommand rc = mk10.getCommandFactory().getReadCommand(touri.getEdmiEnergyRegisterId());
        
        Date to=null;
        
        if (touri.isBillingTimestampTo()) {
            to = getBillingInfo().getToDate();
        }
        
        if (touri.isTimeOfMaxDemand()) {
            Date eventDate = mk10.getCommandFactory().getReadCommand(touri.getEdmiEnergyRegisterId() - 0x1000 + 0x8000).getRegister().getDate();
            return new RegisterValue(obisCode,new Quantity(rc.getRegister().getBigDecimal(),rc.getUnit()),eventDate,null,to);
        } else {
            return new RegisterValue(obisCode,new Quantity(rc.getRegister().getBigDecimal(),rc.getUnit()),null,null,to);
        }
    }

    public BillingInfo getBillingInfo() throws IOException {
        if (billingInfo==null) {
            billingInfo = new BillingInfo(mk10.getCommandFactory());
//            System.out.println("KV_DEBUG> "+billingInfo);
        }
        return billingInfo;
    }
    
}
