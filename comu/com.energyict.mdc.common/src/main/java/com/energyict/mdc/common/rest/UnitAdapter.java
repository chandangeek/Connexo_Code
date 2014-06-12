package com.energyict.mdc.common.rest;

import com.energyict.mdc.common.Unit;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class UnitAdapter extends XmlAdapter<String, Unit> {

    @Override
    public Unit unmarshal(String unitString) throws Exception {
        int index = unitString.indexOf('(');
        String unitStringValue = unitString.substring(index + 1, unitString.length() - 1);
        Unit unit = Unit.get(unitStringValue);
        if (unit==null) {
            throw new IllegalArgumentException("Invalid unit: "+unitString);
        }
        return unit;
    }

    @Override
    public String marshal(Unit v) throws Exception {
        return v==null?"":v.toString();
    }

}