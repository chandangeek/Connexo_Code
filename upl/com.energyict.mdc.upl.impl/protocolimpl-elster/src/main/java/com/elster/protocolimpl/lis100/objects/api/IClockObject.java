package com.elster.protocolimpl.lis100.objects.api;

import com.energyict.mdc.upl.io.NestedIOException;

import com.energyict.dialer.connection.ConnectionException;

import java.io.IOException;
import java.util.Date;

/**
 * Interface for a clock object
 *
 * User: heuckeg
 * Date: 14.03.11
 * Time: 13:51
 */
public interface IClockObject {
    Date getDate() throws NestedIOException, ConnectionException;
    @SuppressWarnings({"unused"})
    void setDate(Date date) throws IOException;
}