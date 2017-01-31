/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * EnergyTypeCodes.java
 *
 * Created on 11 maart 2004, 15:27
 */

package com.energyict.protocolimpl.pact.core.common;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author  Koen
 */
public class EnergyTypeCode {

	private static final int ACTIVE=0;
	private static final int REACTIVE=1;
	private static final int APPARENT=2;
	private static final int NONELECTRICAL=3;

	private static final String[] types = {"Active","Reactive","Apparent","Non Electrical"};


    // See PRI PACT protocol document 0060-0105 (Energy Type Codes)
    // for the description of the energy type codes
	private static List list = new ArrayList();
    static {
       list.add(new EnergyTypeCode(128,0x80,ACTIVE,"Non-specific"));

       /*
       list.add(new EnergyTypeCode(148,0x81,ACTIVE,"export, fundamental"));
       list.add(new EnergyTypeCode(149,0x82,ACTIVE,"import, fundamental"));
       list.add(new EnergyTypeCode(150,0x83,ACTIVE,"import + export, fundamental"));
       list.add(new EnergyTypeCode(151,0x84,ACTIVE,"import + export, fundamental"));
       */
       list.add(new EnergyTypeCode(02,0x81,ACTIVE,"export, fundamental"));
       list.add(new EnergyTypeCode(01,0x82,ACTIVE,"import, fundamental"));
       list.add(new EnergyTypeCode(16,0x83,ACTIVE,"import + export, fundamental"));
       list.add(new EnergyTypeCode(15,0x84,ACTIVE,"import + export, fundamental"));
       /*
       list.add(new EnergyTypeCode(02,0x85,ACTIVE,"export, net"));
       list.add(new EnergyTypeCode(01,0x86,ACTIVE,"import, net"));
       list.add(new EnergyTypeCode(16,0x87,ACTIVE,"import + export, net"));
       list.add(new EnergyTypeCode(15,0x88,ACTIVE,"import + export, net"));
       */
       list.add(new EnergyTypeCode(148,0x85,ACTIVE,"export, net"));
       list.add(new EnergyTypeCode(149,0x86,ACTIVE,"import, net"));
       list.add(new EnergyTypeCode(150,0x87,ACTIVE,"import + export, net"));
       list.add(new EnergyTypeCode(151,0x88,ACTIVE,"import + export, net"));


       list.add(new EnergyTypeCode(129,0x89,ACTIVE,"export + import, fundamental"));
       list.add(new EnergyTypeCode(130,0x8A,ACTIVE,"export + import, net"));

       list.add(new EnergyTypeCode(131,0x90,REACTIVE,"Non-specific"));
       list.add(new EnergyTypeCode(4,0x91,REACTIVE,"export."));
       list.add(new EnergyTypeCode(3,0x92,REACTIVE,"import."));
       list.add(new EnergyTypeCode(132,0x93,REACTIVE,"import + export"));
       list.add(new EnergyTypeCode(133,0x94,REACTIVE,"import + export"));
       list.add(new EnergyTypeCode(8,0x95,REACTIVE,"export while active import"));
       list.add(new EnergyTypeCode(5,0x96,REACTIVE,"import while active import"));
       list.add(new EnergyTypeCode(7,0x97,REACTIVE,"export while active export"));
       list.add(new EnergyTypeCode(6,0x98,REACTIVE,"import while active export"));
       list.add(new EnergyTypeCode(134,0x99,REACTIVE,"lag + lead while active import"));
       list.add(new EnergyTypeCode(135,0x9A,REACTIVE,"lag + lead while active export"));
       list.add(new EnergyTypeCode(136,0x9B,REACTIVE,"lag - lead while active import"));
       list.add(new EnergyTypeCode(137,0x9C,REACTIVE,"lead - lag while active import"));
       list.add(new EnergyTypeCode(138,0x9D,REACTIVE,"lag + lead while active export"));
       list.add(new EnergyTypeCode(139,0x9E,REACTIVE,"lead + lag while active export"));
       list.add(new EnergyTypeCode(140,0x9F,REACTIVE,"export + import"));

       list.add(new EnergyTypeCode(141,0xA0,APPARENT,"type 1, all quadrants"));
       list.add(new EnergyTypeCode(9,0xA1,APPARENT,"type 2, while active import"));
       list.add(new EnergyTypeCode(142,0xA2,APPARENT,"type 4"));
       list.add(new EnergyTypeCode(143,0xA3,APPARENT,"type 3"));
       list.add(new EnergyTypeCode(144,0xA4,APPARENT,"type 5"));
       list.add(new EnergyTypeCode(145,0xA5,APPARENT,"type 6, while active export"));
       list.add(new EnergyTypeCode(146,0xA6,APPARENT,"summator, when import > export"));
       list.add(new EnergyTypeCode(147,0xA7,APPARENT,"summator, when import < export"));

       list.add(new EnergyTypeCode(152,0xB0,NONELECTRICAL,"pulses water"));
       list.add(new EnergyTypeCode(153,0xB1,NONELECTRICAL,"pulses gas"));
       list.add(new EnergyTypeCode(154,0xB2,NONELECTRICAL,"pulses liquid fuel"));
       list.add(new EnergyTypeCode(155,0xB3,NONELECTRICAL,"energy"));
       list.add(new EnergyTypeCode(156,0xB4,NONELECTRICAL,"m3 water"));
       list.add(new EnergyTypeCode(157,0xB5,NONELECTRICAL,"m3 gas"));
       list.add(new EnergyTypeCode(158,0xB6,NONELECTRICAL,"100ft3 gas"));
       list.add(new EnergyTypeCode(159,0xBD,NONELECTRICAL,"pulses channel 3"));
       list.add(new EnergyTypeCode(160,0xBE,NONELECTRICAL,"pulses channel 2"));
       list.add(new EnergyTypeCode(161,0xBF,NONELECTRICAL,"pulses channel 1"));

    }

    private int obisCCode;
    private int pacsEtypeCode;
    private int energyType;
    private String info;

    /** Creates a new instance of EnergyTypeCodes */
    public EnergyTypeCode(int obisCCode, int pacsEtypeCode, int energyType, String info) {
        this.obisCCode=obisCCode;
        this.pacsEtypeCode=pacsEtypeCode;
        this.energyType=energyType;
        this.info=info;
    }


    static public int getObisCCode(int etype) {
        Iterator it = list.iterator();
        while(it.hasNext()) {
            EnergyTypeCode energyTypeCode = (EnergyTypeCode)it.next();
            if (energyTypeCode.getPacsEtypeCode()==etype) {
				return energyTypeCode.getObisCCode();
			}
        }
        return -1;
    }

    static public int getPacsEtypeCode(int obisCCode) {
        Iterator it = list.iterator();
        while(it.hasNext()) {
            EnergyTypeCode energyTypeCode = (EnergyTypeCode)it.next();
            if (energyTypeCode.getObisCCode()==obisCCode) {
				return energyTypeCode.getPacsEtypeCode();
			}
        }
        return -1;
    }

    static public java.lang.String getCompountInfoFromObisC(int obisCCode, boolean energy) {
        Iterator it = list.iterator();
        while(it.hasNext()) {
            EnergyTypeCode energyTypeCode = (EnergyTypeCode)it.next();
            if (energyTypeCode.getObisCCode()==obisCCode) {
                if (energy) {
					return types[energyTypeCode.getEnergyType()]+" energy, "+energyTypeCode.getInfo();
				} else {
					return types[energyTypeCode.getEnergyType()]+" power, "+energyTypeCode.getInfo();
				}
            }
        }
        return null;
    }

    // energy = true energy values, else demand (power) units
    static public Unit getUnit(int etype, boolean energy ) {
        if (energy) {
            if ((etype == 0x80) || (etype == 0x90) || (etype == 0xA6) || (etype == 0xA7)) {
				return Unit.get(255);
			} else if ((etype >= 0x81) && (etype <=0x8A)) {
				return Unit.get("kWh");
			} else if ((etype >= 0x91) && (etype <=0x9f)) {
				return Unit.get("kvarh");
			} else if ((etype >= 0xA0) && (etype <=0xA5)) {
				return Unit.get("kVAh");
			} else if ((etype >= 0xB0) && (etype <=0xB2)) {
				return Unit.get(255);
			} else if (etype == 0xB3) {
				return Unit.get("kWh");
			} else if (etype == 0xB4) {
				return Unit.get("m3");
			} else if (etype == 0xB5) {
				return Unit.get("m3");
			} else if (etype == 0xB6) {
				return Unit.get(BaseUnit.CUBICFEET,100);
			} else if ((etype >= 0xBD) && (etype <=0xBF)) {
				return Unit.get(255);
			} else if ((etype >= 0xC0) && (etype <=0xCF)) {
				return Unit.get(255);
			} else if (etype == 0xD0) {
				return Unit.get("kWh");
			} else if (etype == 0xD1) {
				return Unit.get("kvarh");
			} else if (etype == 0xD2) {
				return Unit.get("kvarh");
			} else if (etype == 0xD3) {
				return Unit.get("kVAh");
			} else if ((etype >= 0xD4) && (etype <=0xD7)) {
				return Unit.get("kWh");
			} else if (etype == 0xD8) {
				return Unit.get("kVAh");
			} else if ((etype >= 0xD9) && (etype <=0xDA)) {
				return Unit.get("kvarh");
			} else if ((etype >= 0xDB) && (etype <=0xDF)) {
				return Unit.get(255);
			} else if (etype == 0xE0) {
				return Unit.get("Vh");
			} else if (etype == 0xE1) {
				return Unit.get("kWh");
			} else if ((etype >= 0xE2) && (etype <=0xE4)) {
				return Unit.get("kvarh");
			} else if (etype == 0xFF) {
				return Unit.get(255);
			}
        }
        else {
            if ((etype == 0x80) || (etype == 0x90) || (etype == 0xA6) || (etype == 0xA7)) {
				return Unit.get(255);
			} else if ((etype >= 0x81) && (etype <=0x8A)) {
				return Unit.get("kW");
			} else if ((etype >= 0x91) && (etype <=0x9f)) {
				return Unit.get("kvar");
			} else if ((etype >= 0xA0) && (etype <=0xA5)) {
				return Unit.get("kVA");
			} else if ((etype >= 0xB0) && (etype <=0xB2)) {
				return Unit.get(255);
			} else if (etype == 0xB3) {
				return Unit.get("kW");
			} else if (etype == 0xB4) {
				return Unit.get("m3/h");
			} else if (etype == 0xB5) {
				return Unit.get("m3/h");
			} else if (etype == 0xB6) {
				return Unit.get(BaseUnit.CUBICFEETPERHOUR,100);
			} else if ((etype >= 0xBD) && (etype <=0xBF)) {
				return Unit.get(255);
			} else if ((etype >= 0xC0) && (etype <=0xCF)) {
				return Unit.get(255);
			} else if (etype == 0xD0) {
				return Unit.get("kW");
			} else if (etype == 0xD1) {
				return Unit.get("kvar");
			} else if (etype == 0xD2) {
				return Unit.get("kvar");
			} else if (etype == 0xD3) {
				return Unit.get("kVA");
			} else if ((etype >= 0xD4) && (etype <=0xD7)) {
				return Unit.get("kW");
			} else if (etype == 0xD8) {
				return Unit.get("kVA");
			} else if ((etype >= 0xD9) && (etype <=0xDA)) {
				return Unit.get("kvar");
			} else if ((etype >= 0xDB) && (etype <=0xDF)) {
				return Unit.get(255);
			} else if (etype == 0xE0) {
				return Unit.get("V");
			} else if (etype == 0xE1) {
				return Unit.get("kW");
			} else if ((etype >= 0xE2) && (etype <=0xE4)) {
				return Unit.get("kvar");
			} else if (etype == 0xFF) {
				return Unit.get(255);
			}
        }
        return null;

    } // static public Unit getUnit(int etype, boolean energy )

    static public boolean isStatusFlagsChannel(int etype) {
         if ((etype >= 0xC0) && (etype <=0xCF)) {
			return true;
		} else {
			return false;
		}
    }

    /**
     * Getter for property obisCCode.
     * @return Value of property obisCCode.
     */
    public int getObisCCode() {
        return obisCCode;
    }

    /**
     * Getter for property pacsEtypeCode.
     * @return Value of property pacsEtypeCode.
     */
    public int getPacsEtypeCode() {
        return pacsEtypeCode;
    }

    /**
     * Getter for property energyType.
     * @return Value of property energyType.
     */
    public int getEnergyType() {
        return energyType;
    }

    /**
     * Getter for property info.
     * @return Value of property info.
     */
    public java.lang.String getInfo() {
        return info;
    }

}
