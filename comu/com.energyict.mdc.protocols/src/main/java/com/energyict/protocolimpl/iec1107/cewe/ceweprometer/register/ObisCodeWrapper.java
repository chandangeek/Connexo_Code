/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.cewe.ceweprometer.register;

import com.energyict.mdc.common.ObisCode;

/**
 * The ObisCodeMapper works with a Map that links the available obis
 * codes to ValueFactories that can retrieve data from the RegisterFactory.
 * <p/>
 * The keys of the Map are actuall ObisCodes.  But the equal method of
 * obis codes makes a distinction between relative period (VZ) and
 * absolute periods.  This is not the behaviour that is needed here.
 * ObisCodeWrapper will provide the ObisCodes with an equals and hash
 * method that does not make a distinction between relative and absolute
 * periods.
 */
public class ObisCodeWrapper implements Comparable {

    private ObisCode obisCode;

    private String os;
    private String reversedOs;

    public ObisCodeWrapper(ObisCode oc) {
        this.obisCode = oc;
        this.os = oc.getA() + "." + oc.getB() + "." + oc.getC() + "." + oc.getD() + "." + oc.getE() + "." + Math.abs(oc.getF());
        this.reversedOs = new StringBuffer(os).reverse().toString();
    }

    public ObisCode getObisCode() {
        return obisCode;
    }

    public boolean equals(Object o) {
        if (!(o instanceof ObisCodeWrapper)) {
            return false;
        }

        ObisCodeWrapper other = (ObisCodeWrapper) o;
        return os.equals(other.os);
    }

    public int hashCode() {
        return os.hashCode();
    }

    public String toString() {
        return obisCode.toString();
    }

    public int compareTo(Object o) {
        ObisCodeWrapper other = (ObisCodeWrapper) o;
        return reversedOs.compareTo(other.reversedOs);
    }

}
