package com.elster.protocolimpl.lis100.objects.api;

import com.energyict.cbo.NestedIOException;
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

    public Date getDate() throws NestedIOException, ConnectionException;
    @SuppressWarnings({"unused"})
    public void setDate(Date date) throws IOException;
}
