package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.UkHub;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd.messaging.InHomeDisplayMessageExecutor;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd.messaging.InHomeDisplayMessaging;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * The InHomeDisplay logical device has limited functionality, currently only serves as placeholder
 */
public class InHomeDisplay extends UkHub {

    public InHomeDisplay(PropertySpecService propertySpecService, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor extractor) {
        super(propertySpecService, messageFileFinder, extractor);
    }

    @Override
    protected void initAfterConnect() throws ConnectionException {
        super.initAfterConnect();
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        if (messageProtocol == null) {
            messageProtocol = new InHomeDisplayMessaging(new InHomeDisplayMessageExecutor(this, messageFileFinder, messageFileExtractor));
        }
        return messageProtocol;
    }

    @Override
    public String getVersion() {
        return "$Date: Thu Oct 27 15:46:50 2016 +0200 $";
    }

    @Override
    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        throw new UnsupportedException("InHomeDisplay, readRegisters(List<Register> registers) not supported.");
    }

    @Override
    public RegisterInfo translateRegister(Register register) throws IOException {
        throw new UnsupportedException("InHomeDisplay, translateRegister(Register register) not supported.");
    }

    @Override
    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws IOException {
        throw new UnsupportedException("InHomeDisplay, getMeterEvents(Date lastLogbookDate) not supported.");
    }

    @Override
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        throw new UnsupportedException("InHomeDisplay, fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) not supported.");
    }

    @Override
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        throw new UnsupportedException("InHomeDisplay, getLoadProfileData(List<LoadProfileReader> loadProfiles) not supported.");
    }
}
