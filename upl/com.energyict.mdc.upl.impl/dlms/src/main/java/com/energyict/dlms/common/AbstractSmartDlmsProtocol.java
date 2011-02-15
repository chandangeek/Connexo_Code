package com.energyict.dlms.common;

import com.energyict.protocolimpl.base.ProtocolProperties;
import com.energyict.smartmeterprotocolimpl.common.AbstractSmartMeterProtocol;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 11-feb-2011
 * Time: 13:18:30
 */
public abstract class AbstractSmartDlmsProtocol extends AbstractSmartMeterProtocol {

    private DlmsSession dlmsSession;

    protected abstract DlmsProtocolProperties getProperties();

    @Override
    protected ProtocolProperties getProtocolProperties() {
        return getProperties();
    }

    public DlmsSession getDlmsSession() {
        if (dlmsSession == null) {
            dlmsSession = new DlmsSession(getInputStream(), getOutputStream(), getLogger(), getProperties(), getTimeZone());
        }
        return dlmsSession;
    }

    /**
     * Make a connection to the physical device.
     * Setup the association and check the objectList
     *
     * @throws java.io.IOException             if errors occurred during data fetching
     */
    public void connect() throws IOException {
        getDlmsSession().connect();
        checkCacheObjects();
    }

    /**
     * Disconnect from the physical device.
     * CLose the association and check if we need to close the underlying connection
     */
    private void checkCacheObjects() {

    }

    @Override
    public void disconnect() throws IOException {
        getDlmsSession().disconnect();
    }

}
