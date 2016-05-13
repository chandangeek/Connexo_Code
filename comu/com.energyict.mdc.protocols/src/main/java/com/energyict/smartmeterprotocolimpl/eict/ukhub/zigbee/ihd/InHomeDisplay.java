package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.DeviceMessageFileService;
import com.energyict.mdc.protocol.api.LoadProfileConfiguration;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.protocols.mdc.services.impl.OrmClient;

import com.energyict.smartmeterprotocolimpl.eict.ukhub.UkHub;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd.messaging.InHomeDisplayMessageExecutor;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.ihd.messaging.InHomeDisplayMessaging;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * The InHomeDisplay logical device has limited functionality, currently only serves as placeholder
 */
public class InHomeDisplay extends UkHub {

    @Override
    public String getProtocolDescription() {
        return "Elster AM110R DLMS (SSWG IC) Zigbee IHD";
    }

    @Inject
    public InHomeDisplay(PropertySpecService propertySpecService, OrmClient ormClient, DeviceMessageFileService deviceMessageFileService) {
        super(propertySpecService, ormClient, deviceMessageFileService);
    }

    @Override
    protected void initAfterConnect() throws ConnectionException {
        super.initAfterConnect();
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
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
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