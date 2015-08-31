package com.elster.insight.common.rest;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.elster.insight.common.Unit;
import com.elster.jupiter.util.Checks;

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