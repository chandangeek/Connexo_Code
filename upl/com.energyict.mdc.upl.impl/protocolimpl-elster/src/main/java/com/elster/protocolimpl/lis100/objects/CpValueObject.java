package com.elster.protocolimpl.lis100.objects;

import com.energyict.mdc.upl.io.NestedIOException;

import com.elster.protocolimpl.lis100.objects.api.IBaseObject;
import com.elster.protocolimpl.lis100.objects.api.IDoubleObject;
import com.energyict.dialer.connection.ConnectionException;

/**
 * class to handle cp value
 *
 * User: heuckeg
 * Date: 25.01.11
 * Time: 10:04
 */
public class CpValueObject implements IDoubleObject {

    protected IBaseObject base;

    public CpValueObject(IBaseObject base) {
        this.base = base;
    }

    public String getValue() throws ConnectionException, NestedIOException {
        return base.getValue();
    }

    public static double RawDataToCpValue(String s) {

        double result = 99.0;
        int i = Integer.parseInt(s);
        if (i != 99) {
            result = 0.01;
            while( i-- > 0) {
                result *= 10;
            }
        }
        return result;
    }

    public double getDoubleValue() throws NestedIOException, ConnectionException {
        return RawDataToCpValue(base.getValue());
    }
}
