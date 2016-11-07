package com.energyict.protocolimpl.iec1107.abba230;

import com.energyict.mdc.upl.NoSuchRegisterException;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**@author  Koen */

public class EnergyTypeCode {

    private static final List<EnergyTypeCode> ENERGY_TYPE_CODES = new ArrayList<>();

    static {
        ENERGY_TYPE_CODES.add(new EnergyTypeCode(0,Unit.get(BaseUnit.WATTHOUR,-3),1,"active import"));
        ENERGY_TYPE_CODES.add(new EnergyTypeCode(1,Unit.get(BaseUnit.WATTHOUR,-3),2,"active export"));
        ENERGY_TYPE_CODES.add(new EnergyTypeCode(2,Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR,-3),5,"reactive Q1"));
        ENERGY_TYPE_CODES.add(new EnergyTypeCode(3,Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR,-3),6,"reactive Q2"));
        ENERGY_TYPE_CODES.add(new EnergyTypeCode(4,Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR,-3),7,"reactive Q3"));
        ENERGY_TYPE_CODES.add(new EnergyTypeCode(5,Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR,-3),8,"reactive Q4"));
        ENERGY_TYPE_CODES.add(new EnergyTypeCode(6,Unit.get(BaseUnit.VOLTAMPEREHOUR,-3),9,"apparent"));
        //list.add(new EnergyTypeCode(7,Unit.get(BaseUnit.VOLTAMPEREHOUR,-3),10,"export apparent"));

        ENERGY_TYPE_CODES.add(new EnergyTypeCode(7,Unit.get(""),130,"reserved"));
        ENERGY_TYPE_CODES.add(new EnergyTypeCode(8,Unit.get(""),131,"reserved"));
        ENERGY_TYPE_CODES.add(new EnergyTypeCode(9,Unit.get(""),132,"reserved"));
        ENERGY_TYPE_CODES.add(new EnergyTypeCode(0xA,Unit.get(""),133,"reserved"));
        ENERGY_TYPE_CODES.add(new EnergyTypeCode(0xB,Unit.get(""),134,"reserved"));
        ENERGY_TYPE_CODES.add(new EnergyTypeCode(0xC,Unit.get(""),135,"reserved"));
        ENERGY_TYPE_CODES.add(new EnergyTypeCode(0xD,Unit.get(""),136,"reserved"));

        ENERGY_TYPE_CODES.add(new EnergyTypeCode(0xE,Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR,-3),3,"reactive import"));
        ENERGY_TYPE_CODES.add(new EnergyTypeCode(0xF,Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR,-3),4,"reactive export"));
    }

    private int regSource;
    private Unit unit;
    private int obisC;
    private String description;

    static boolean isCustomerDefined(int regSource) {
        return (regSource>=8) && (regSource<=10);
    }

    private EnergyTypeCode(int regSource, Unit unit, int obisC, String description) {
       this.regSource=regSource;
       this.unit=unit;
       this.obisC=obisC;
       this.description=description;
    }

    public static Unit getUnitFromRegSource(int regSource, boolean energy) throws NoSuchRegisterException {
        for (EnergyTypeCode etc : ENERGY_TYPE_CODES) {
            if (etc.getRegSource() == regSource) {
                Unit unit = etc.getUnit();
                if (energy) {
                    return unit.getVolumeUnit();
                } else {
                    return unit.getFlowUnit();
                }
            }
        }
        throw new NoSuchRegisterException("EnergyTypeCode, getUnitFromSource, invalid register source code, "+regSource);
    }

    static String getDescriptionfromRegSource(int regSource, boolean energy) throws NoSuchRegisterException {
        for (EnergyTypeCode etc : ENERGY_TYPE_CODES) {
            if (etc.getRegSource() == regSource) {
                return etc.getDescription();
            }
        }
        throw new NoSuchRegisterException("EnergyTypeCode, getUnitFromSource, invalid register source code, "+regSource);
    }

    static int getObisCFromRegSource(int regSource, boolean energy) throws NoSuchRegisterException {
        for (EnergyTypeCode etc : ENERGY_TYPE_CODES) {
            if (etc.getRegSource() == regSource) {
                return etc.getObisC();
            }
        }
        throw new NoSuchRegisterException("EnergyTypeCode, getUnitFromSource, invalid register source code, "+regSource);
    }

    static List<EnergyTypeCode> getEnergyTypeCodes() {
        return Collections.unmodifiableList(ENERGY_TYPE_CODES);
    }

    static Unit getUnitFromObisCCode(int obisC, boolean energy) throws NoSuchRegisterException {
        for (EnergyTypeCode etc : ENERGY_TYPE_CODES) {
            if (etc.getObisC() == obisC) {
                Unit unit = etc.getUnit();
                if (energy) {
                    return unit.getVolumeUnit();
                } else {
                    return unit.getFlowUnit();
                }
            }
        }
        throw new NoSuchRegisterException("EnergyTypeCode, getUnitObisCCode, invalid obis C code, "+obisC);
    }

    static int getRegSourceFromObisCCode(int obisC) throws NoSuchRegisterException {
        for (EnergyTypeCode etc : ENERGY_TYPE_CODES) {
            if (etc.getObisC() == obisC) {
                return etc.getRegSource();
            }
        }
        throw new NoSuchRegisterException("EnergyTypeCode, getRegSourceFromObisCCode, invalid obis C code, "+obisC);
    }

    public static String getCompountInfoFromObisC(int obisC, boolean energy) throws NoSuchRegisterException {
        for (EnergyTypeCode etc : ENERGY_TYPE_CODES) {
            if (etc.getObisC() == obisC) {
                if (energy) {
                    return "energy, " + etc.getDescription();
                } else {
                    return "power, " + etc.getDescription();
                }
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
    int getObisC() {
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
