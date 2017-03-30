/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.enermet.e120;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

class SeriesResponse implements Response {

    private NackCode nackCode;
    private int registerIndex;
    private Map<Date, E120RegisterValue> valueMap;

    SeriesResponse(NackCode nackCode) {
        this.nackCode = nackCode;
        valueMap = new TreeMap<>();
    }

    void setRegisterIndex(int registerIndex) {
        this.registerIndex = registerIndex;
    }

    SeriesResponse addValue(E120RegisterValue value) {
        valueMap.put(value.getTime(), value);
        return this;
    }

    public NackCode getNackCode() {
        return nackCode;
    }

    public Object getValue() {
        return valueMap;
    }

    public boolean isOk() {
        return NackCode.OK.equals(nackCode);
    }

    public E120RegisterValue get(Date key) {
        return valueMap.get(key);
    }

    public Set<Date> keySet() {
        return valueMap.keySet();
    }

    public String toString() {
        StringBuilder rslt = new StringBuilder();

        rslt.append("SeriesResponse [");
        rslt.append("registerIndex: ").append(registerIndex).append(", ");

        for (E120RegisterValue value : valueMap.values()) {
            rslt.append("\n\t").append(value);
        }

        rslt.append("]");
        return rslt.toString();
    }

}