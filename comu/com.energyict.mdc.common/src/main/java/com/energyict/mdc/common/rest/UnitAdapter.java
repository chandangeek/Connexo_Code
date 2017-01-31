/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.common.rest;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.Unit;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class UnitAdapter extends XmlAdapter<String, Unit> {

    @Override
    public Unit unmarshal(String unitString) throws Exception {
        if (Checks.is(unitString).emptyOrOnlyWhiteSpace()) {
            return Unit.getUndefined();
        }
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