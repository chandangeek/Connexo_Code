package com.energyict.protocolimpl.iec1107.abba1140;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**@author  Koen */

public class EnergyTypeCode {

    static List list = new ArrayList();

    static {
        list.add(new EnergyTypeCode(0,Unit.get(BaseUnit.WATTHOUR,-3),1,"active import"));
        list.add(new EnergyTypeCode(1,Unit.get(BaseUnit.WATTHOUR,-3),2,"active export"));
        list.add(new EnergyTypeCode(2,Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR,-3),5,"reactive Q1"));
        list.add(new EnergyTypeCode(3,Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR,-3),6,"reactive Q2"));
        list.add(new EnergyTypeCode(4,Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR,-3),7,"reactive Q3"));
        list.add(new EnergyTypeCode(5,Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR,-3),8,"reactive Q4"));
        list.add(new EnergyTypeCode(6,Unit.get(BaseUnit.VOLTAMPEREHOUR,-3),9,"import apparent"));
        list.add(new EnergyTypeCode(7,Unit.get(BaseUnit.VOLTAMPEREHOUR,-3),10,"export apparent"));

        list.add(new EnergyTypeCode(14,Unit.get(BaseUnit.COUNT,0),128,"customer defined 1"));
        list.add(new EnergyTypeCode(15,Unit.get(BaseUnit.COUNT,0),129,"customer defined 2"));
        list.add(new EnergyTypeCode(-1,Unit.get(BaseUnit.WATTHOUR,-3),130,"active import net consumption"));
    }

    int regSource;
    Unit unit;
    int obisC;
    String description;


    static  public boolean isCustomerDefined(int regSource) {
        return (regSource>=8) && (regSource<=10);
    }

    /** Creates a new instance of EnergyTypeCode */
    private EnergyTypeCode(int regSource,Unit unit,int obisC,String description) {
       this.regSource=regSource;
       this.unit=unit;
       this.obisC=obisC;
       this.description=description;
    }



    static public Unit getUnitFromRegSource(int regSource, boolean energy) throws NoSuchRegisterException {
        Iterator it = list.iterator();
        while(it.hasNext()) {
            EnergyTypeCode etc = (EnergyTypeCode)it.next();
            if (etc.getRegSource()==regSource) {
                Unit unit = etc.getUnit();
                if (energy) return unit.getVolumeUnit();
                else return unit.getFlowUnit();
            }
        }
        throw new NoSuchRegisterException("EnergyTypeCode, getUnitFromSource, invalid register source code, "+regSource);
    }

    static public String getDescriptionfromRegSource(int regSource, boolean energy) throws NoSuchRegisterException {
        Iterator it = list.iterator();
        while(it.hasNext()) {
            EnergyTypeCode etc = (EnergyTypeCode)it.next();
            if (etc.getRegSource()==regSource) {
                return etc.getDescription();
            }
        }
        throw new NoSuchRegisterException("EnergyTypeCode, getUnitFromSource, invalid register source code, "+regSource);
    }

    static public int getObisCFromRegSource(int regSource, boolean energy) throws NoSuchRegisterException {
        Iterator it = list.iterator();
        while(it.hasNext()) {
            EnergyTypeCode etc = (EnergyTypeCode)it.next();
            if (etc.getRegSource()==regSource) {
                return etc.getObisC();
            }
        }
        throw new NoSuchRegisterException("EnergyTypeCode, getUnitFromSource, invalid register source code, "+regSource);
    }

    static public List getEnergyTypeCodes() throws NoSuchRegisterException {
        return list;
    }

    static public Unit getUnitFromObisCCode(int obisC, boolean energy) throws NoSuchRegisterException {
        Iterator it = list.iterator();
        while(it.hasNext()) {
            EnergyTypeCode etc = (EnergyTypeCode)it.next();
            if (etc.getObisC()==obisC) {
                Unit unit = etc.getUnit();
                if (energy) return unit.getVolumeUnit();
                else return unit.getFlowUnit();
            }
        }
        throw new NoSuchRegisterException("EnergyTypeCode, getUnitObisCCode, invalid obis C code, "+obisC);
    }

    static public int getRegSourceFromObisCCode(int obisC) throws NoSuchRegisterException {
        Iterator it = list.iterator();
        while(it.hasNext()) {
            EnergyTypeCode etc = (EnergyTypeCode)it.next();
            if (etc.getObisC()==obisC) {
                return etc.getRegSource();
            }
        }
        throw new NoSuchRegisterException("EnergyTypeCode, getRegSourceFromObisCCode, invalid obis C code, "+obisC);
    }

    static public String getCompountInfoFromObisC(int obisC, boolean energy) throws NoSuchRegisterException {
        Iterator it = list.iterator();
        while(it.hasNext()) {
            EnergyTypeCode etc = (EnergyTypeCode)it.next();
            if (etc.getObisC()==obisC) {
                if (energy) return "energy, "+etc.getDescription();
                else return "power, "+etc.getDescription();
            }
        }
        throw new NoSuchRegisterException("EnergyTypeCode, getCompountInfoFromObisC, invalid obis C code, "+obisC);
    }

    /**
     * Getter for property regSource.
     * @return Value of property regSource.
     */
    public int getRegSource() {
        return regSource;
    }

    /**
     * Setter for property regSource.
     * @param regSource New value of property regSource.
     */
    public void setRegSource(int regSource) {
        this.regSource = regSource;
    }

    /**
     * Getter for property unit.
     * @return Value of property unit.
     */
    public Unit getUnit() {
        return unit;
    }

    /**
     * Setter for property unit.
     * @param unit New value of property unit.
     */
    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    /**
     * Getter for property obisC.
     * @return Value of property obisC.
     */
    public int getObisC() {
        return obisC;
    }

    /**
     * Setter for property obisC.
     * @param obisC New value of property obisC.
     */
    public void setObisC(int obisC) {
        this.obisC = obisC;
    }

    /**
     * Getter for property description.
     * @return Value of property description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Setter for property description.
     * @param description New value of property description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

}
