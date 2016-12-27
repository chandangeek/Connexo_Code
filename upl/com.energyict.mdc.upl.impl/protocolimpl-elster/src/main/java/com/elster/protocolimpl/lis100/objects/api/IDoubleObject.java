package com.elster.protocolimpl.lis100.objects.api;

import com.energyict.mdc.io.NestedIOException;

import com.energyict.dialer.connection.ConnectionException;

/**
 * Iterface for Object returing a double value
 * User: heuckeg
 * Date: 14.03.11
 * Time: 13:49
 * To change this template use File | Settings | File Templates.
 */
public interface IDoubleObject extends IBaseObject {
    double getDoubleValue() throws NestedIOException, ConnectionException;
}
