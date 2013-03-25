package com.energyict.protocolimpl.dlms.prime;

import com.energyict.dlms.DlmsSessionProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.messaging.FirmwareUpdateMessageBuilder;
import com.energyict.protocol.messaging.FirmwareUpdateMessaging;
import com.energyict.protocol.messaging.FirmwareUpdateMessagingConfig;
import com.energyict.protocolimpl.dlms.common.AbstractDlmsSessionProtocol;

import java.io.IOException;
import java.util.Date;

/**
 * Prime protocol, that should be able to read all the prime compliant devices (L&G, ZIV, Current, Elster, ...)
 *
 * Copyrights EnergyICT
 * Date: 21/02/12
 * Time: 14:43
 */
public abstract class AbstractPrimeMeter extends AbstractDlmsSessionProtocol implements FirmwareUpdateMessaging {

    @Override
    protected DlmsSessionProperties getProperties() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected void doInit() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    protected String readSerialNumber() throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public FirmwareUpdateMessagingConfig getFirmwareUpdateMessagingConfig() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public FirmwareUpdateMessageBuilder getFirmwareUpdateMessageBuilder() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getProtocolVersion() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getProfileInterval() throws UnsupportedException, IOException {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Date getTime() throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setTime() throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
