package com.elster.protocolimpl.lis100.objects.api;

import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.ConnectionException;

/**
 * Interface for a object of type counter
 *
 * User: heuckeg
 * Date: 15.03.11
 * Time: 09:30
 */
public interface ICounterObject extends IBaseObject {

    public double getCounterValue() throws NestedIOException, ConnectionException;
}
