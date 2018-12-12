package com.elster.protocolimpl.lis100.objects;

import com.energyict.mdc.upl.io.NestedIOException;

import com.elster.protocolimpl.lis100.objects.api.IBaseObject;
import com.elster.protocolimpl.lis100.objects.api.IIntegerObject;
import com.energyict.dialer.connection.ConnectionException;

/**
 * Class to get an lis100 number object (int)
 * In contrast to a BufferedObject, the value is read again from device every read(xxx)value()
 *
 * User: heuckeg
 * Date: 24.01.11
 * Time: 15:07
 */
public class IntegerObject implements IBaseObject, IIntegerObject {

    protected IBaseObject baseObject;
    protected int base;

    public IntegerObject(IBaseObject baseObject, int base) {
        this.baseObject = baseObject;
        this.base = base;
    }

    public String getValue() throws NestedIOException, ConnectionException {
        return baseObject.getValue();
    }
    public int getIntValue() throws NestedIOException, ConnectionException {
        return Integer.parseInt(baseObject.getValue(), base);
    }
}
