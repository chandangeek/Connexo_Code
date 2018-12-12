/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;


import com.elster.jupiter.util.units.Unit;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class ReadingUnitAdapter extends XmlAdapter<String, Unit> {

    @Override
    public Unit unmarshal(String unitString) throws Exception {
        Unit unit = Unit.unitForSymbol(unitString);
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