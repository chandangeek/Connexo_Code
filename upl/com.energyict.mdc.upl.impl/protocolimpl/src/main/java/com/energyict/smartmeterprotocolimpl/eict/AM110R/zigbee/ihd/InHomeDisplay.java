package com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.ihd;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.AM110R;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.ihd.messaging.InHomeDisplayMessageExecutor;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.zigbee.ihd.messaging.InHomeDisplayMessaging;

import java.util.Date;
import java.util.List;

/**
 * The InHomeDisplay logical device has limited functionality, currently only serves as placeholder
 */
public class InHomeDisplay extends AM110R {

    public InHomeDisplay(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        if (messageProtocol == null) {
            messageProtocol = new InHomeDisplayMessaging(new InHomeDisplayMessageExecutor(this));
        }
        return messageProtocol;
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-08-26 14:01:32 +0200 (Wed, 26 Aug 2015) $";
    }

    @Override
    public List<RegisterValue> readRegisters(List<Register> registers) throws UnsupportedException {
        throw new UnsupportedException("InHomeDisplay, readRegisters(List<Register> registers) not supported.");
    }

    @Override
    public RegisterInfo translateRegister(Register register) throws UnsupportedException {
        throw new UnsupportedException("InHomeDisplay, translateRegister(Register register) not supported.");
    }

    @Override
    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws UnsupportedException {
        throw new UnsupportedException("InHomeDisplay, getMeterEvents(Date lastLogbookDate) not supported.");
    }

    @Override
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws UnsupportedException {
        throw new UnsupportedException("InHomeDisplay, fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) not supported.");
    }

    @Override
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws UnsupportedException {
        throw new UnsupportedException("InHomeDisplay, getLoadProfileData(List<LoadProfileReader> loadProfiles) not supported.");
    }
}
