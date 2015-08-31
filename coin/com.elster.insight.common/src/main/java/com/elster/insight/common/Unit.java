/*
 * Unit.java
 *
 * Created on 4 oktober 2002, 22:53
 */

package com.elster.insight.common;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Unit represent a physical unit
 *
 * @author Karel
 */
public class Unit implements java.io.Serializable {

    private String MICRO = "\u00B5";

    private static Map commonUnits = new HashMap();

    static {
        Iterator iterator = BaseUnit.iterator();
        while (iterator.hasNext()) {
            BaseUnit base = (BaseUnit) iterator.next();
            putToMap(base.toString(), new Unit(base,0));
            putToMap("k" + base.toString(), new Unit(base,3));
            putToMap("M" + base.toString(), new Unit(base,6));
            putToMap("G" + base.toString(), new Unit(base,9));
            putToMap("m" + base.toString(), new Unit(base,-3));
        }
        BaseUnit base = BaseUnit.get(BaseUnit.CUBICFEET);
        putToMap("Ccf",new Unit(base,2));
        putToMap("Mcf",new Unit(base,3));
        putToMap("MMcf",new Unit(base,6));
        base = BaseUnit.get(BaseUnit.CUBICFEETPERHOUR);
        putToMap("Ccf/h",new Unit(base,2));
        putToMap("Mcf/h",new Unit(base,3));
        putToMap("MMcf/h",new Unit(base,6));
    }

    private static void putToMap(String key,Unit value){
        if(!commonUnits.containsKey(key)){
            commonUnits.put(key, value);
        }
    }

    BaseUnit baseUnit;
    int scale;

    private Unit(BaseUnit baseUnit, int scale) {
        this.baseUnit = baseUnit;
        this.scale = scale;
    }

    /**
     * return the unit corresponding to the argument
     *
     * @param acronym identifying the unit to return.
     *                e.g kWh , MJ , m3
     * @return the unit representing the argument
     */
    public static Unit get(String acronym) {
        return (Unit) commonUnits.get(acronym);
    }

    /**
     * return the unit with a <CODE>BaseUnit</CODE> defined by the
     * argument and scale zero
     *
     * @param code the <CODE>BaseUnit</CODE> code
     * @return the corresponding unit
     */
    public static Unit get(int code) {
        return get(code, 0);
    }

    /**
     * return the unit with its <CODE>BaseUnit</CODE> defined by the code
     * argument and scale equal to the second argument
     *
     * @param code  the <CODE>BaseUnit</CODE> code
     * @param scale the unit scale
     * @return the corresponding unit
     */
    public static Unit get(int code, int scale) {
        return new Unit(BaseUnit.get(code), scale);
    }

    /**
     * returns the unit corresponding to its database representation
     *
     * @param dbString the database String representation
     * @return the requested unit
     */
    public static Unit fromDb(String dbString) {
        StringTokenizer tokenizer = new StringTokenizer(dbString.trim(), ".");
        int dlmsCode = Integer.parseInt(tokenizer.nextToken());
        int scale = Integer.parseInt(tokenizer.nextToken());
        return get(dlmsCode, scale);
    }

    /**
     * return the receiver's <CODE>BaseUnit</CODE>
     *
     * @return the <CODE>BaseUnit</CODE>
     */
    public BaseUnit getBaseUnit() {
        return baseUnit;
    }

    /**
     * return the receiver's scale
     *
     * @return the scale
     */
    public int getScale() {
        return scale;
    }

    /**
     * return the code if the receiver's <CODE>BaseUnit</CODE>
     *
     * @return the receiver's <CODE>BaseUnit</CODE> code
     */
    public int getDlmsCode() {
        return baseUnit.getDlmsCode();
    }

    /**
     * test if the receiver is a "volume" unit.
     * Each "volume" unit has a correspondig flow unit,
     * that represents the same quantity differentiated to time.
     * e.g. kWh -> kW  or m3 -> m3/h
     *
     * @return true if the receiver is a "volume" unit.
     */
    public boolean isVolumeUnit() {
        return baseUnit.isVolumeUnit();
    }

    /**
     * test if the receiver is a "flow" unit.
     * Quantities expressed in "flow" unit represent
     * an instantenous value. Each "flow" unit
     * has a correspondig flow unit, that represents
     * the same quantity integrated over time.
     * e.g. kW -> kWh and m3/h -> m3
     *
     * @return true if the receiver is a flow unit.
     */
    public boolean isFlowUnit() {
        return baseUnit.isFlowUnit();
    }

    /**
     * return the corresponding "volume" unit if the receiver is a
     * "flow" unit , null otherwise
     *
     * @return the corresponding "volume" unit or null
     */
    public Unit getVolumeUnit() {
        BaseUnit volumeBase = baseUnit.getVolumeUnit();
        if (volumeBase == null) {
            return null;
        } else {
            return new Unit(volumeBase, this.scale);
        }
    }

    /**
     * return the corresponding "flow" unit if the receiver is a
     * "volume" unit , null otherwise
     *
     * @return the corresponding "flow" unit or null
     */
    public Unit getFlowUnit() {
        BaseUnit flowBase = baseUnit.getFlowUnit();
        if (flowBase == null) {
            return null;
        } else {
            return new Unit(flowBase, this.scale);
        }
    }

    /**
     * return the receiver's database string representation
     *
     * @return the database string representation
     */
    public String dbString() {
        return "" + getDlmsCode() + "." + scale;
    }

    /**
     * return a String representation of the receiver
     *
     * @return a string representation
     */
    public String toString() {
        String basicName = baseUnit.toString();
        switch (scale) {
            case 0:
                return basicName;
            case 2:
                if ("cf".equals(basicName) || "cf/h".equals(basicName)) {
                    return "C" + basicName;
                } else {
                    return "h" + basicName;
                }
            case 3:
                if ("cf".equals(basicName) || "cf/h".equals(basicName)) {
                    return "M" + basicName;
                } else {
                    return "k" + basicName;
                }
            case 6:
                if ("cf".equals(basicName) || "cf/h".equals(basicName)) {
                    return "MM" + basicName;
                } else {
                    return "M" + basicName;
                }
            case 9:
                return "G" + basicName;
            case 12:
                return "T" + basicName;
            case -2:
                return "c" + basicName;
            case -3:
                return "m" + basicName;
            case -6:
                return MICRO + basicName;
            case -9:
                return "n" + basicName;
            case -12:
                return "p" + basicName;
            default:
                StringBuilder buffer = new StringBuilder(basicName);
                buffer.append("(x");
                if (scale > 0) {
                    buffer.append("1");
                    for (int i = 0; i < scale; i++) {
                        buffer.append("0");
                    }
                } else {
                    buffer.append("0.");
                    for (int i = scale; i < 0; i++) {
                        if (i == -1) {
                            buffer.append("1");
                        } else {
                            buffer.append("0");
                        }
                    }
                }
                buffer.append(")");
                return buffer.toString();
        }
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param anObject the reference object with which to compare.
     * @return true if this object is the same as the obj argument; false otherwise.
     */
    public boolean equals(Object anObject) {
        if (anObject == null) {
            return false;
        }
        if (anObject instanceof Unit) {
            Unit other = (Unit) anObject;
            return equalBaseUnit(other) && (scale == other.getScale());
        } else {
            return false;
        }
    }

    public boolean equalBaseUnit(Unit other) {
        return (baseUnit.equals(other.getBaseUnit()));
    }

    /**
     * Returns the undefined unit.
     *
     * @return the undefined unit.
     */
    public static Unit getUndefined() {
        return get(BaseUnit.UNITLESS);
    }

    /**
     * Tests if the receiver represents an undefined unit.
     *
     * @return true if the unit is undefined, false otherwise.
     */
    public boolean isUndefined() {
        return baseUnit.isUndefined();
    }

    /**
     * Tests if the receiver represents an undefined unit without scale.
     *
     * @return true if the unit is undefined and has scale zero , false otherwise.
     */

    public boolean isNeutral() {
        return isUndefined() && (getScale() == 0);
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return baseUnit.hashCode() + scale * 256;
    }

    /**
     * returns a sorted TreeMap of known "volume" and "flow" units.
     *
     * @return a TreeMap, having unit string representations as keys
     *         and units as values
     */
    public static TreeMap getSortedCommonUnits() {
        TreeMap<String, Unit> ts = new TreeMap<String, Unit>(
                new Comparator<String>() {
                    public int compare(String o1, String o2) {
                        return (o1.toLowerCase().compareTo(o2.toLowerCase()));
                    }
                });
        Iterator it = commonUnits.values().iterator();
        Unit unit;
        while (it.hasNext()) {
            unit = (Unit) it.next();
            if ((unit.isFlowUnit()) || (unit.isVolumeUnit())) {
                ts.put(unit.toString(), unit);
                //if (unit.isVolumeUnit()) System.out.println("volume: " + unit.toString() + ",  flow: " + unit.getFlowUnit().toString());
                //else if (unit.isFlowUnit()) System.out.println("volume: " + unit.getVolumeUnit().toString() + ",  flow: " + unit.toString());
            }
        }
        return ts;
    }


}
