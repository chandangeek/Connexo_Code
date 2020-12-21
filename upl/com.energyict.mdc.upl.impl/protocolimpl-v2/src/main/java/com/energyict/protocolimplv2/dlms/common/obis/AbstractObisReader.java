package com.energyict.protocolimplv2.dlms.common.obis;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimplv2.dlms.common.obis.matchers.Matcher;

public abstract class AbstractObisReader<M, N, L> implements ObisReader<M, N, L> {

    private final Matcher<L> matcher;

    protected AbstractObisReader(Matcher<L> matcher) {
        this.matcher = matcher;
    }

    /**
     *
     * @param obisCode as received from CXO
     * @return real obisCode defined in device.
     */
    public ObisCode map(ObisCode obisCode) {
        return matcher.map(obisCode);
    }

    /**
     * @param o as per matcher used at construct time. Usually this is CXO obisCode or DLMSClassId
     * @return true if this reader is applicable for the object received as parameter (either ObisCode or DLMSClassId, however it is possible to implement matchers
     *  based on different criteria).
     */
    public boolean isApplicable(L o) {
        return matcher.matches(o);
    }
}
