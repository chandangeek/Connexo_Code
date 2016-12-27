package com.elster.protocolimpl.lis100.objects.api;

import com.energyict.mdc.io.NestedIOException;

import com.energyict.dialer.connection.ConnectionException;

/**
 * Interface for a 'basic' object
 *
 * User: heuckeg
 * Date: 14.03.11
 * Time: 13:46
 */
public interface IBaseObject {
    String getValue() throws NestedIOException, ConnectionException;
}
