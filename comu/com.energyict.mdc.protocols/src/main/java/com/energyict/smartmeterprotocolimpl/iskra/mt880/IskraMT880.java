/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.iskra.mt880;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.LoadProfileConfiguration;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.data.MessageEntry;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.protocols.mdc.services.impl.OrmClient;

import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.smartmeterprotocolimpl.iskra.mt880.events.MT880EventProfile;
import com.energyict.smartmeterprotocolimpl.iskra.mt880.profiles.LoadProfileBuilder;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author sva
 * @since 7/10/13 - 15:38
 */
public class IskraMT880 extends AbstractSmartDlmsProtocol implements MessageProtocol {

    @Override
    public String getProtocolDescription() {
        return "Iskraemeco MT880 DLMS";
    }

    /** Contains all properties of the Iskra MT880 device **/
    private IskraMT880Properties properties;

    /** The ComposedMeterInfo, used to request common info from the device **/
    private ComposedMeterInfo composedMeterInfo;

    /** The RegisterFactory used to read out all registers **/
    private RegisterFactory registerFactory;

    /** the LoadProfileBuilder used to read out load profile data **/
    private LoadProfileBuilder loadProfileBuilder;

    /** the EventProfile used to read out all event logbooks **/
    private MT880EventProfile eventProfile;

    /** the Messaging class used to send out all device messages **/
    private MT880Messaging messaging;

    @Inject
    public IskraMT880(PropertySpecService propertySpecService, OrmClient ormClient) {
        super(propertySpecService, ormClient);
    }

    @Override
    protected DlmsProtocolProperties getProperties() {
        if (this.properties == null) {
            this.properties = new IskraMT880Properties();
        }
        return this.properties;
    }

    @Override
    protected void initAfterConnect() throws ConnectionException {
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean enableDataReadout) throws ConnectionException {
        try {
            getDlmsSession().init();
        } catch (IOException e) {
            throw new ConnectionException("Failed while initializing the DLMS connection.");
        }

        HHUSignOn hhuSignOn = new IskraHHUConnection(commChannel, getProperties().getTimeout(), getProperties().getRetries(), 300, 0);
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);                          //HDLC:         38400 baud, 8N1
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(enableDataReadout);
        getDlmsSession().getDLMSConnection().setHHUSignOn(hhuSignOn, "", 0);    //IEC1107:      300 baud, 7E1
    }

    public RegisterInfo translateRegister(Register register) throws IOException {
        return getRegisterFactory().translateRegister(register);
    }

    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        return getRegisterFactory().readRegisters(registers);
    }

    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }

    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws IOException {
        return getEventProfile().getEvents(lastLogbookDate);
    }

    public void applyMessages(List messageEntries) throws IOException {
        getMessaging().applyMessages(messageEntries);
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return getMessaging().queryMessage(messageEntry);
    }

    public List getMessageCategories() {
        return getMessaging().getMessageCategories();
    }

    public String writeMessage(Message msg) {
        return getMessaging().writeMessage(msg);
    }

    public String writeTag(MessageTag tag) {
        return getMessaging().writeTag(tag);
    }

    public String writeValue(MessageValue value) {
        return getMessaging().writeValue(value);
    }

    /**
     * Get the AXDRDateTimeDeviationType for this DeviceType
     *
     * @return the requested type
     */
    public AXDRDateTimeDeviationType getDateTimeDeviationType() {
        return AXDRDateTimeDeviationType.Negative;
    }

    public RegisterFactory getRegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new RegisterFactory(this);
        }
        return registerFactory;
    }

    public LoadProfileBuilder getLoadProfileBuilder() {
        if (loadProfileBuilder == null){
            loadProfileBuilder = new LoadProfileBuilder(this);
        }
        return loadProfileBuilder;
    }

    public MT880EventProfile getEventProfile() {
        if (this.eventProfile == null) {
            this.eventProfile = new MT880EventProfile(this);
        }
        return eventProfile;
    }

    public MT880Messaging getMessaging() {
        if (this.messaging == null) {
            this.messaging = new MT880Messaging(this);
        }
        return messaging;
    }

    public ComposedMeterInfo getComposedMeterInfo() {
        if (composedMeterInfo == null ) {
            composedMeterInfo = new ComposedMeterInfo(getDlmsSession(), supportsBulkRequests());
        }
        return composedMeterInfo;
    }

    public String getMeterSerialNumber() throws IOException {
        return getComposedMeterInfo().getMeterSerialNumber();
    }

    public String getFirmwareVersion() throws IOException {
        return getComposedMeterInfo().getFirmwareVersion();
    }

    public String getVersion() {
        return "$Date: 2013-09-30 15:38:06 +0200 (ma, 30 sep 2013) $";
    }
}
