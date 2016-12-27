package com.elster.protocolimpl.lis100.objects;

import com.energyict.mdc.io.NestedIOException;

import com.elster.protocolimpl.lis100.objects.api.IBaseObject;
import com.elster.protocolimpl.lis100.objects.api.IDoubleObject;
import com.energyict.dialer.connection.ConnectionException;

/**
 * Object returning a double value
 *
 * User: heuckeg
 * Date: 14.03.11
 * Time: 14:45
 */
public class DoubleObject implements IBaseObject, IDoubleObject {

    protected IBaseObject baseObject;

    public DoubleObject(IBaseObject baseObject) {
        this.baseObject = baseObject;
    }

    public String getValue() throws NestedIOException, ConnectionException {
        return baseObject.getValue();
    }
    public double getDoubleValue() throws NestedIOException, ConnectionException {
        return Double.parseDouble(baseObject.getValue());
    }

}
