package com.elster.protocolimpl.lis100.objects;

import com.energyict.mdc.upl.io.NestedIOException;

import com.elster.protocolimpl.lis100.objects.api.IBaseObject;
import com.elster.protocolimpl.lis100.objects.api.ICounterObject;
import com.energyict.dialer.connection.ConnectionException;

/**
 * class to get data of a counter inside the device
 *
 * User: heuckeg
 * Date: 25.01.11
 * Time: 11:20
 */
public class CounterObject implements ICounterObject {

    protected IBaseObject base;


    public CounterObject(IBaseObject base) {
        this.base = base;
    }

    public String getValue() throws NestedIOException, ConnectionException {
        return base.getValue();
    }
    public double getCounterValue() throws NestedIOException, ConnectionException {
        return Double.parseDouble(base.getValue());
    }

}

