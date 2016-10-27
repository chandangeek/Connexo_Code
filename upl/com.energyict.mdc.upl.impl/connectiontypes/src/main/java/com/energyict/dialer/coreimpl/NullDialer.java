package com.energyict.dialer.coreimpl;

import com.energyict.dialer.core.IPDial;
import com.energyict.dialer.core.LinkException;

import java.io.IOException;

public class NullDialer extends DialerImpl implements IPDial {

    protected void doConnect(String strDialAddress1, String strDialAddress2,
                             int timeout) throws IOException, LinkException {
        // do nothing
    }

    protected void doDisConnect() throws IOException, LinkException {
        // do nothing
    }


}
