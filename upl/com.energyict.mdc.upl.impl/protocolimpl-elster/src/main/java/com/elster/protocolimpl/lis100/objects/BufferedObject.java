package com.elster.protocolimpl.lis100.objects;

import com.energyict.mdc.upl.io.NestedIOException;

import com.elster.protocolimpl.lis100.ProtocolLink;
import com.elster.protocolimpl.lis100.objects.api.IBaseObject;
import com.energyict.dialer.connection.ConnectionException;

/**
 * This class hold the data of a readable value (can also be a constant value given a creation)
 * <p/>
 * User: heuckeg
 * Date: 25.01.11
 * Time: 10:19
 */

public class BufferedObject extends AbstractObject implements IBaseObject {

    protected String data = null;

    public BufferedObject(ProtocolLink link, byte readOrder, byte writeOrder) {
        super(link, readOrder, writeOrder);
    }

    public BufferedObject(ProtocolLink link, byte readOrder) {
        this(link, readOrder, (byte) 0);
    }

    @Override
    public String getValue() throws NestedIOException, ConnectionException {
        if (data == null) {
            data = super.getValue();
        }
        return data;
    }
}
