package com.elster.protocolimpl.lis100.objects.api;

import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.ConnectionException;

/**
 * Interface for a object returning an int
 *
 * User: heuckeg
 * Date: 14.03.11
 * Time: 13:48
 */
public interface IIntegerObject extends IBaseObject {

    public int getIntValue() throws NestedIOException, ConnectionException;
}
