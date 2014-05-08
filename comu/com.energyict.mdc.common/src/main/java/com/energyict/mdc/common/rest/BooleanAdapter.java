package com.energyict.mdc.common.rest;

import com.elster.jupiter.util.Checks;
import com.energyict.mdc.common.ObisCode;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class BooleanAdapter extends XmlAdapter<String, ObisCode> {

    @Override
    public ObisCode unmarshal(String jsonValue) throws Exception {
        if (Checks.is(jsonValue).emptyOrOnlyWhiteSpace()) {
            return null;
        }
        return ObisCode.fromString(jsonValue);
    }

    @Override
    public String marshal(ObisCode obisCode) throws Exception {
        if (obisCode==null) {
            return null;
        }
        return obisCode.toString();
    }
}
