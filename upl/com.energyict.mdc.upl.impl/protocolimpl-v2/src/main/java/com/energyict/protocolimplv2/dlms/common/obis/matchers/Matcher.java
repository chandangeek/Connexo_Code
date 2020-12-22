package com.energyict.protocolimplv2.dlms.common.obis.matchers;

import com.energyict.obis.ObisCode;

public interface Matcher<T> {

    boolean matches(T o);

    /**
     *
     * @param obisCode from connexo that is tainted (sometimes) with replacing a 0 with attribute number or position within array
     * @return device obis code (original one in device specs)
     */
    ObisCode map(ObisCode obisCode);

}
