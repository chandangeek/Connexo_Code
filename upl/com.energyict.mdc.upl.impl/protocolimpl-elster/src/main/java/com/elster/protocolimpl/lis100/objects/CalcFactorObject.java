package com.elster.protocolimpl.lis100.objects;

import com.energyict.mdc.io.NestedIOException;

import com.elster.protocolimpl.lis100.objects.api.IBaseObject;
import com.elster.protocolimpl.lis100.objects.api.IDoubleObject;
import com.energyict.dialer.connection.ConnectionException;

/**
 * This class hold the device information "calculation factor"
 * <p/>
 * User: heuckeg
 * Date: 25.01.11
 * Time: 10:19
 */
public class CalcFactorObject implements IDoubleObject {

    protected IBaseObject base;

    public CalcFactorObject(IBaseObject base) {
        this.base = base;
    }

    public String getValue() throws ConnectionException, NestedIOException {
        return base.getValue();
    }

    /**
     * Convert a "raw" string to a calcFactor
     *
     * @param s - raw string
     * @return calcFactor
     */
    public static double RawDataToCalcFactor(String s) {

        int exp = Integer.parseInt(s.substring(10, 12));
        if (Integer.parseInt(s.substring(8, 10)) > 0) {
            exp = -exp;
        }

        String m = "0." + s.substring(0, 8) + "E" + exp;

        return Double.parseDouble(m);
    }

    public double getDoubleValue() throws NestedIOException, ConnectionException {
        return RawDataToCalcFactor(base.getValue());
    }
}
