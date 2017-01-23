/*
 * RegisterDataId.java
 *
 * Created on 5 september 2005, 9:50
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.transdata.markv.core.commands;


import com.energyict.obis.ObisCode;

/**
 *
 * @author koen
 */
public class RegisterDataId {


    // phenomenons
    public static final String[] PHENOMENONS = {"Total Usage","Current Demand","Peak Demand","Time Of Peak","Date Of Peak","Previous Demand","Coincident Demand","Cumulative Demand","Recorder Valid. Reading"};
    public static final int TOTAL_USAGE=0;
    public static final int CURRENT_DEMAND=1;
    public static final int PEAK_DEMAND=2;
    public static final int TIME_OF_PEAK=3;
    public static final int DATE_OF_PEAK=4;
    public static final int PREVIOUS_DEMAND=5;
    public static final int COINCIDENT_DEMAND=6;
    public static final int CUMULATIVE_DEMAND=7;
    public static final int RECORDER_VALID_READING=8;
    public static final int POWER_FACTOR3=9;
    public static final int VOLTAGE_A=10;
    public static final int VOLTAGE_B=11;
    public static final int VOLTAGE_C=12;
    public static final int AMPERE_A=13;
    public static final int AMPERE_B=14;
    public static final int AMPERE_C=15;
    public static final int WATT3=16;
    public static final int VAR3=17;
    public static final int OTHER=18;

    private static final int[] OBISC = {128,128,128,128,128,128,128,128,128,13,32,52,72,31,51,71,1,3,129};
    private static final int[] OBISD = {8,4,6,6,6,5,128,2,129,7,7,7,7,7,7,7,7,7};

    // type
    public static final int LONG=0;
    public static final int INT=1;
    public static final int TIME=2;
    public static final int STRING=3;



    int phenomenon; // left column of Transdata data id's table
    int type; // right column of Transdata data id's table
    int id; // (=data id) cell value of Transdata data id's table
    int channel; // 1 based
    int rate; // 0=all or NA, 1..n=rate 1..n
    int billing; // 0=current, 1=last
    String description;

    /** Creates a new instance of RegisterDataId */
    public RegisterDataId(int phenomenon, int type, int id, int channel, int rate, int billing, String description) {
        this.phenomenon=phenomenon;
        this.type=type;
        this.id=id;
        this.channel=channel;
        this.rate=rate;
        this.billing=billing;
        this.description=description;
    }

    public String toString() {
        return getObisCode()+", "+getDescription();
    }

    public ObisCode getObisCode() {
        if (getPhenomenon() != OTHER) {
            int obisC;
            // if self read registers, C = 129, overrule OBISC
            if ((getId()>=410) && (getId()<=483)) {
                   obisC = 129;
            }
            else obisC = OBISC[getPhenomenon()];
            return new ObisCode(1,getChannel(),obisC,OBISD[getPhenomenon()],getRate(),(getBilling()==0?255:getBilling()-1));
        }
        else {
            // manufacturer specific code
            // 0.0.96.99.E.F E=id/100 F=id%100
            return new ObisCode(0,0,96,99,getId()/100,getId()%100);
        }


    }

    public int getType() {
        return type;
    }

    public int getId() {
        return id;
    }

    public int getChannel() {
        return channel;
    }

    public int getPhenomenon() {
        return phenomenon;
    }

    public int getRate() {
        return rate;
    }

    public int getBilling() {
        return billing;
    }

    public String getDescription() {
        return description;
    }
}
