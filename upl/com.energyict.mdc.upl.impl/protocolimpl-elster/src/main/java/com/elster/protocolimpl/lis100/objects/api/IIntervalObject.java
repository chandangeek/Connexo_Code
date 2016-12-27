package com.elster.protocolimpl.lis100.objects.api;

import com.energyict.mdc.io.NestedIOException;

import com.energyict.dialer.connection.ConnectionException;

/**
 * Interface for object returning an interval
 *
 * User: heuckeg
 * Date: 14.03.11
 * Time: 15:50
 */
public interface IIntervalObject extends IIntegerObject {
    int getIntervalSeconds() throws NestedIOException, ConnectionException;
}