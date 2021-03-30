package com.energyict.protocolimplv2.dlms.common.obis.matchers;

import com.energyict.obis.ObisCode;

public class AnyObisMatcher implements Matcher<ObisCode> {

    @Override
    public boolean matches(ObisCode o) {
        return true;
    }

    @Override
    public ObisCode map(ObisCode obisCode) {
        return obisCode;
    }
}
