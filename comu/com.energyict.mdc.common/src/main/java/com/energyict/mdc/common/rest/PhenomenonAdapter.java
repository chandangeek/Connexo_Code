package com.energyict.mdc.common.rest;

import com.energyict.mdc.common.Unit;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class PhenomenonAdapter extends XmlAdapter<String, Unit> {

    @Override
    public Unit unmarshal(String unitString) throws Exception {
        Unit unit = Unit.get(unitString);
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