package com.energyict.smartmeterprotocolimpl.elster.apollo;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.smartmeterprotocolimpl.common.SimpleMeter;
import com.energyict.smartmeterprotocolimpl.elster.apollo.eventhandling.ApolloEventProfiles;
import com.energyict.smartmeterprotocolimpl.elster.apollo.messaging.AS300MessageExecutor;
import com.energyict.smartmeterprotocolimpl.elster.apollo.messaging.AS300Messaging;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 29-jun-2011
 * Time: 11:32:30
 */
public class AS300 extends AbstractSmartDlmsProtocol implements SimpleMeter, MessageProtocol, TimeOfUseMessaging {

    private AS300Properties properties;
    private AS300ObjectFactory objectFactory;
    private RegisterReader registerReader;
    private LoadProfileBuilder loadProfileBuilder;
    private AS300Messaging messageProtocol;

    @Override
    protected DlmsProtocolProperties getProperties() {
        if (properties == null) {
            properties = new AS300Properties();
        }
        return properties;
    }

    public AS300ObjectFactory getObjectFactory() {
        if (objectFactory == null) {
            objectFactory = new AS300ObjectFactory(getDlmsSession());
        }
        return objectFactory;
    }

    private RegisterReader getRegisterReader() {
        if (registerReader == null) {
            registerReader = new RegisterReader(this);
        }
        return registerReader;
    }

    private LoadProfileBuilder getLoadProfileBuilder() {
        if (loadProfileBuilder == null) {
            loadProfileBuilder = new LoadProfileBuilder(this);
        }
        return loadProfileBuilder;
    }

    @Override
    protected void initAfterConnect() throws ConnectionException {
        //Nothing, no topology available.
    }

    public String getFirmwareVersion() throws IOException {
        return getObjectFactory().getFirmwareVersion().getString();
    }

    @Override
    public Date getTime() throws IOException {
        return getObjectFactory().getClock().getDateTime();
    }

    @Override
    public void setTime(Date newMeterTime) throws IOException {
        getObjectFactory().getClock().setAXDRDateTimeAttr(new AXDRDateTime(newMeterTime));
    }

    public String getMeterSerialNumber() throws IOException {
        return getObjectFactory().getSerialNumber().getString();
    }

    public RegisterInfo translateRegister(Register register) throws IOException {
        return null; //TODO
    }

    /**
     * Overridden because the object list is not used.
     */
    @Override
    public void connect() throws IOException {
        getDlmsSession().connect();
    }

    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        return getRegisterReader().read(registers);
    }

    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws IOException {
        ApolloEventProfiles logs = new ApolloEventProfiles(this);
        Calendar cal = Calendar.getInstance(getTimeZone());
        cal.setTime(lastLogbookDate == null ? new Date(0) : lastLogbookDate);
        return logs.getEventLog(cal);
    }

    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }

    public String getVersion() {
        return "$Date$";
    }

    public String getSerialNumber() {
        return getProperties().getSerialNumber();
    }

    public int getPhysicalAddress() {
        return 0;//TODO?
    }

    @Override
    protected void checkCacheObjects() throws IOException {
        getDlmsSession().getMeterConfig().setInstantiatedObjectList(AS300ObjectList.OBJECT_LIST);
    }

    public AS300Messaging getMessageProtocol() {
        if(this.messageProtocol == null){
            this.messageProtocol = new AS300Messaging(new AS300MessageExecutor(this));
        }
        return messageProtocol;
    }

    /**
     * Provides the full list of outstanding messages to the protocol.
     * If for any reason certain messages have to be grouped before they are sent to a device, then this is the place to do it.
     * At a later timestamp the framework will query each {@link com.energyict.protocol.MessageEntry} (see {@link #queryMessage(com.energyict.protocol.MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link com.energyict.protocol.MessageEntry}s
     * @throws java.io.IOException if a logical error occurs
     */
    public void applyMessages(final List messageEntries) throws IOException {
        getMessageProtocol().applyMessages(messageEntries);
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        return getMessageProtocol().queryMessage(messageEntry);
    }

    public List getMessageCategories() {
        return getMessageProtocol().getMessageCategories();
    }

    public String writeMessage(final Message msg) {
        return getMessageProtocol().writeMessage(msg);
    }

    public String writeTag(final MessageTag tag) {
        return getMessageProtocol().writeTag(tag);
    }

    public String writeValue(final MessageValue value) {
        return getMessageProtocol().writeValue(value);
    }

    /**
     * Indicates whether the protocol needs a 'name' for the tarif calendar or not.
     * For some meter a name needs to be sent to the meter.  If this is necessary the protocol must
     * return true for this method.
     *
     * @return    <code>true</code> a 'name' is needed for the tarif calendar, <code>false</code> if not.
     */
    public boolean needsName() {
        return getMessageProtocol().needsName();
    }

    /**
     * Indicates whether the tarif calendar data is saved in code table or not.
     *
     * @return    <code>true</code> the tarif calendar data is saved in code table, <code>false</code> if not.
     */
    public boolean supportsCodeTables() {
        return getMessageProtocol().supportsCodeTables();
    }

    /**
     * Returns the message builder capable of generating and parsing 'time of use' messages.
     *
     * @return The {@link com.energyict.protocol.messaging.MessageBuilder} capable of generating and parsing 'time of use' messages.
     */
    public TimeOfUseMessageBuilder getTimeOfUseMessageBuilder() {
        return getMessageProtocol().getTimeOfUseMessageBuilder();
    }

    /**
     * Indicates whether the tarif calendar data is saved in a userfile or not.
     *
     * @return    <code>true</code> the tarif calendar data is saved in a userfile, <code>false</code> if not.
     */
    public boolean supportsUserFiles() {
        return getMessageProtocol().supportsUserFiles();
    }

    /**
     * Indicates whether the implementer supports {@link com.energyict.mdw.core.UserFile} references. This will typically be used by generic protocols, as these have
     * access to the database. If {@link #supportsUserFiles()} is true, and this method returns <code>false</code>, the entire file is sent as payload in
     * the message. If this method returns <code>true</code>, a user file ID is passed on to the implementer, who can then query the database for the
     * contents of the file.
     *
     * @return <code>true</code> if the protocol supports a file ID reference, <code>false</code> if it does not. This can equally be translated into
     *         <code>true</code> for a generic protocol, and <code>false</code> for a normal one.
     */
    public boolean supportsUserFileReferences() {
        return getMessageProtocol().supportsUserFileReferences();
    }

    /**
     * Indicates whether the implementer supports {@link com.energyict.mdw.core.Code} references. This will typically be used by generic protocols, as these have
     * access to the database. If {@link #supportsCodeTables()} is true, and this method returns <code>false</code>, the entire file is sent as payload in
     * the message. If this method returns <code>true</code>, a user file ID is passed on to the implementer, who can then query the database for the
     * contents of the file.
     *
     * @return <code>true</code> if the protocol supports a codeTable ID reference, <code>false</code> if it does not. This can equally be translated into
     *         <code>true</code> for a generic protocol, and <code>false</code> for a normal one.
     */
    public boolean supportsCodeTableReferences() {
        return getMessageProtocol().supportsCodeTableReferences();
    }

    /**
     * Indicates whether the content of the {@link com.energyict.mdw.core.UserFile} of the {@link com.energyict.mdw.core.Code} must be zipped when it is inlined in the
     * RtuMessage. This is only taken into account when {@link #supportsCodeTableReferences()} or {@link #supportsUserFileReferences()} is false.</br>
     * <b><u>NOTE:</u> If the content is zipped, then Base64 Encoding is also applied!</b>
     *
     * @return true if the content needs to be zipped, false otherwise
     */
    public boolean zipContent() {
        return getMessageProtocol().zipContent();
    }

    /**
     * Indicate whether the content of the {@link com.energyict.mdw.core.UserFile} or {@link com.energyict.mdw.core.Code} must be Base64 Encoded.
     * <b><u>NOTE:</u> Base64 encoding will be automatically applied if {@link #zipContent()} returns true</b>
     *
     * @return true if the content needs to be Base64 encoded, false otherwise
     */
    public boolean encodeContentToBase64() {
        return getMessageProtocol().encodeContentToBase64();
    }
}