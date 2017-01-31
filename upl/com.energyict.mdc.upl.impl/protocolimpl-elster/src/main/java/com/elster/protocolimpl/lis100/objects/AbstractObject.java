/**
 *
 */
package com.elster.protocolimpl.lis100.objects;

import com.energyict.mdc.upl.io.NestedIOException;

import com.elster.protocolimpl.lis100.ProtocolLink;
import com.elster.protocolimpl.lis100.objects.api.IBaseObject;
import com.energyict.dialer.connection.ConnectionException;

import java.io.IOException;

/**
 * base class for a readable value from lis100 device
 *
 * User: heuckeg
 * Date: 25.01.11
 * Time: 10:19
 */
public class AbstractObject implements IBaseObject {

    /**
     * The used {@link com.elster.protocolimpl.lis100.ProtocolLink}
     */
    protected ProtocolLink link;

    /**
     * The address of the object
     */
    protected byte readOrder;
    protected byte writeOrder;

    public AbstractObject(ProtocolLink link, byte readOrder, byte writeOrder) {
        this.link = link;
        this.readOrder = readOrder;
        this.writeOrder = writeOrder;
    }

    public AbstractObject(ProtocolLink link, byte readOrder) {
        this(link, readOrder, (byte) 0);
    }

    public String getValue() throws NestedIOException, ConnectionException {
        return link.getLis100Connection().receiveTelegram(readOrder);
    }

    public void writeValue(String value) throws IOException {
        if (writeOrder == 0) {
            throw new IOException(String.format("setValue not possible on %s", this.getClass()));
        }
        link.getLis100Connection().sendTelegram(writeOrder, value);
    }
}
