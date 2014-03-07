package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.Unit;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class UnitAdapter extends XmlAdapter<String, Unit> {

    @Override
    public Unit unmarshal(String unitString) throws Exception {
        if (Checks.is(unitString).emptyOrOnlyWhiteSpace()) {
            return Unit.getUndefined();
        }
        return Unit.get(unitString);
    }

    @Override
    public String marshal(Unit v) throws Exception {
        return v==null?"":v.toString();
    }
}
