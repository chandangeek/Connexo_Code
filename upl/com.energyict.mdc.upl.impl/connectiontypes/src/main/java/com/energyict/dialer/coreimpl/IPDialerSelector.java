/*
 * IPDialerSelector.java
 *
 * Created on 16 april 2004, 16:33
 */

package com.energyict.dialer.coreimpl;

import com.energyict.dialer.core.IPDial;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.PostSelect;

import java.io.IOException;

/**
 * @author Koen
 */
public class IPDialerSelector extends IPDialer implements PostSelect, IPDial {

    /**
     * Creates a new instance of IPDialerSelector
     */
    public IPDialerSelector() {
    }

    // send id 3 times, so we are for 99% sure that it has been received...
    private void sendId(String strId) throws IOException {
        write(strId + "\r\n", 500);
        write(strId + "\r\n", 500);
        write(strId + "\r\n", 500);
    }

    protected void dial(String strDialAddress1, String strDialAddress2, int iTimeout) throws IOException, LinkException {
        if (strDialAddress2 != null) {
            sendId(strDialAddress2);
        }
    }
}
