package com.energyict.smartmeterprotocolimpl.iskra.mt880;

import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.properties.PropertySpec;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.smartmeterprotocolimpl.iskra.mt880.events.MT880EventProfile;
import com.energyict.smartmeterprotocolimpl.iskra.mt880.profiles.LoadProfileBuilder;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * @author sva
 * @since 7/10/13 - 15:38
 */
public class IskraMT880 extends AbstractSmartDlmsProtocol implements MessageProtocol, SerialNumberSupport {

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

    public String getMeterSerialNumber()  {
        try {
            return getComposedMeterInfo().getMeterSerialNumber();
        }
        catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, dlmsSession.getProperties().getRetries() +1);
        }
    }


    public String getFirmwareVersion() throws IOException {
        return getComposedMeterInfo().getFirmwareVersion();
    }

    public String getVersion() {
        return "$Date: 2015-11-26 15:26:47 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getSerialNumber() {
        return getMeterSerialNumber();
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return getProperties().getPropertySpecs();
    }
}
