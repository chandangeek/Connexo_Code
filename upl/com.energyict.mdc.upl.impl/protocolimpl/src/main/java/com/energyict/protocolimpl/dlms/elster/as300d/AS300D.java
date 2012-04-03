package com.energyict.protocolimpl.dlms.elster.as300d;

import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.common.AbstractDlmsSessionProtocol;
import com.energyict.protocolimpl.dlms.elster.as300d.events.AS300DEventLogs;
import com.energyict.protocolimpl.dlms.elster.as300d.messaging.AS300DMessaging;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 21/02/12
 * Time: 14:43
 */
public class AS300D extends AbstractDlmsSessionProtocol {

    private final AS300DProperties properties = new AS300DProperties();
    private AS300DProfile hourlyProfile;
    private AS300DEventLogs eventLogs;
    private AS300DClock clock;
    private AS300DRegisters registers;
    private AS300DMeterInfo meterInfo;
    private AS300DMessaging messaging;

    public AS300D() {

    }

    public String getProtocolVersion() {
        return "$Date: 2012-03-21 11:38:19 +0100 (Wed, 21 Mar 2012) $";
    }

    @Override
    protected void doInit() {
        this.hourlyProfile = new AS300DProfile(getSession());
        this.eventLogs = new AS300DEventLogs(getSession());
        this.clock = new AS300DClock(getSession());
        this.registers = new AS300DRegisters(getSession());
        this.meterInfo = new AS300DMeterInfo(getSession());
        this.messaging = new AS300DMessaging(getSession());
    }

    @Override
    protected String readSerialNumber() throws IOException {
        return meterInfo.getMeterSerialNumber();
    }

    /**
     * @return
     */
    public AS300DProperties getProperties() {
        return properties;
    }

    public String getFirmwareVersion() throws IOException {
        return meterInfo.getAllFirmwareVersions();
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        ProfileData profileData = hourlyProfile.getProfileData(from, to);
        if (includeEvents) {
            profileData.setMeterEvents(eventLogs.getMeterEvents(from, to));
        }
        profileData.sort();
        return profileData;
    }

    public int getNumberOfChannels() throws IOException {
        return hourlyProfile.getNumberOfChannels();
    }

    public int getProfileInterval() throws IOException {
        return hourlyProfile.getProfileInterval();
    }

    public Date getTime() throws IOException {
        return clock.getTime();
    }

    public void setTime() throws IOException {
        clock.setTime();
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
            return registers.readRegister(obisCode);
        } catch (IOException e) {
            throw new NoSuchRegisterException(e.getMessage());
        }
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return messaging.queryMessage(messageEntry);
    }

    public List getMessageCategories() {
        return AS300DMessaging.getMessageCategories();
    }
}
