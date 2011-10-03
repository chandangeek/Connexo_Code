package com.elster.protocolimpl.lis100.objects;

import com.elster.protocolimpl.lis100.objects.api.IBaseObject;
import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.ConnectionException;

/**
 * class for a "constant" object (never read anything, because it's constant...)
 *
 * User: heuckeg
 * Date: 14.03.11
 * Time: 14:08
 */
public class ConstantObject implements IBaseObject {

    protected String constData = null;

    public ConstantObject(String constData) {
        this.constData = constData;
    }

    public String getValue() throws NestedIOException, ConnectionException {
        return constData;
    }
}
