package com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.ihd;

import com.energyict.protocol.*;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.AM110R;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.ihd.messaging.InHomeDisplayMessageExecutor;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.ihd.messaging.InHomeDisplayMessaging;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * The InHomeDisplay logical device has limited functionality, currently only serves as placeholder
 */
public class InHomeDisplay extends AM110R {

    @Override
    public MessageProtocol getMessageProtocol() {
        if (messageProtocol == null) {
            messageProtocol = new InHomeDisplayMessaging(new InHomeDisplayMessageExecutor(this));
        }
        return messageProtocol;
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-09-20 15:21:01 +0200 (Fri, 20 Sep 2013) $";
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
