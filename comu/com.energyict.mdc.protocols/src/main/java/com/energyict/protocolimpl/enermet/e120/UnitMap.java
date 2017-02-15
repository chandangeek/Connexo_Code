/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.enermet.e120;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Unit;

import java.util.HashMap;

/**
 * Contains a map linking the id's of the meter units to Unit Objects.
 *
 * @author fbo
 */
class UnitMap {

    private static HashMap map = new HashMap();

    static {
        add( 0, Unit.getUndefined() );
        add( 1, Unit.get( BaseUnit.WATT, 0) );
        add( 2, Unit.get( BaseUnit.WATT, 3) );
        add( 3, Unit.get( BaseUnit.WATT, 6) );
        add( 4, Unit.get( BaseUnit.WATT, 9) );
        add( 5, Unit.get( BaseUnit.VOLTAMPEREREACTIVE, 0) );
        add( 6, Unit.get( BaseUnit.VOLTAMPEREREACTIVE, 3) );
        add( 7, Unit.get( BaseUnit.VOLTAMPEREREACTIVE, 6) );
        add( 8, Unit.get( BaseUnit.VOLTAMPEREREACTIVE, 9) );
        add( 9, Unit.get( BaseUnit.WATTHOUR, 0) );
        add( 10, Unit.get( BaseUnit.WATTHOUR, 3) );
        add( 11, Unit.get( BaseUnit.WATTHOUR, 6) );
        add( 12, Unit.get( BaseUnit.WATTHOUR, 9) );
        add( 13, Unit.get( BaseUnit.VOLTAMPEREREACTIVEHOUR, 0) );
        add( 14, Unit.get( BaseUnit.VOLTAMPEREREACTIVEHOUR, 3) );
        add( 15, Unit.get( BaseUnit.VOLTAMPEREREACTIVEHOUR, 6) );
        add( 16, Unit.get( BaseUnit.VOLTAMPEREREACTIVEHOUR, 9) );
        add( 17, Unit.get( BaseUnit.VOLT, 0) );
        add( 18, Unit.get( BaseUnit.AMPERE, 0) );
        // cosf
        add( 19, Unit.get( BaseUnit.UNITLESS, 0) );
        add( 20, Unit.get( BaseUnit.CUBICMETER, 0) );
        add( 21, Unit.get( BaseUnit.LITER, 0) );
        add( 22, Unit.get( BaseUnit.LITER, -3) );
        add( 23, Unit.get( BaseUnit.GALLON, 0) );
        add( 24, Unit.get( BaseUnit.JOULE, 9) );
        add( 25, Unit.get( BaseUnit.JOULE, 6) );
        // 26 - 29 not (yet) supported since units not in BaseUnit
        add( 30, Unit.get( BaseUnit.JOULEPERHOUR, 6) );
        /* ms/s */
        add( 31, Unit.get( BaseUnit.UNITLESS, 0) );
        /* l/s */
        add( 32, Unit.get( BaseUnit.UNITLESS, 0) );
        add( 33, Unit.get( BaseUnit.CUBICMETERPERHOUR, 0) );
        add( 34, Unit.get( BaseUnit.DEGREE_CELSIUS, 0) );
        add( 35, Unit.get( BaseUnit.LITERPERHOUR, 0) );
        add( 36, Unit.get( BaseUnit.VOLTAMPERE, 0) );
        add( 37, Unit.get( BaseUnit.VOLTAMPERE, 3) );
        add( 38, Unit.get( BaseUnit.VOLTAMPERE, 6) );
        add( 39, Unit.get( BaseUnit.VOLTAMPERE, 9) );
        add( 40, Unit.get( BaseUnit.VOLTAMPEREHOUR, 0) );
        add( 41, Unit.get( BaseUnit.VOLTAMPEREHOUR, 3) );
        add( 42, Unit.get( BaseUnit.VOLTAMPEREHOUR, 6) );
        add( 43, Unit.get( BaseUnit.VOLTAMPEREHOUR, 9) );
        add( 44, Unit.get( BaseUnit.VOLT, 3) );
        add( 45, Unit.get( BaseUnit.AMPERE, 3) );
        add( 46, Unit.get( BaseUnit.HOUR, 0) );
        add( 47, Unit.get( BaseUnit.MINUTE, 0) );
        add( 48, Unit.get( BaseUnit.SECOND, 0) );
    }

    private int id;
    private Unit unit;

    public static Unit get(int id){
        Unit unit = ((UnitMap)map.get(""+id)).getUnit();
        if( unit == null )
            throw new ApplicationException("Unit: " + id + " is not supported");
        return unit;
    }

    public UnitMap(int id, Unit unit) {
        this.id = id;
        this.unit = unit;
    }

    private static void add(int id, Unit aUnit){
        map.put(""+id, new UnitMap(id, aUnit));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Unit getUnit() {
        return unit;
    }

    public void setUnit(Unit unit) {
        this.unit = unit;
    }

    public String toString( ){
        String u = ( (unit!=null)?unit.toString():"");
        return "Enermet Unit [" + id + " ," + u + "]";
    }

}
