package com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta;

import com.energyict.cbo.BusinessException;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.SmartMeterProtocol;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.smartmeterprotocolimpl.common.SimpleMeter;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * The Abstract NTA Mbus device implements the {@link SmartMeterProtocol} so we can
 * define this as a pluggable class in EIS 9.1.
 * Most of the methods throw an {@link UnsupportedException}, if your subclass wants
 * to use one of these, then simple override them.
 * <p/>
 * Copyrights EnergyICT
 * Date: 15-jul-2011
 * Time: 10:31:14
 */
public abstract class AbstractNtaMbusDevice implements SimpleMeter, SmartMeterProtocol, MessageProtocol {

    private final AbstractSmartNtaProtocol meterProtocol;

    private final String serialNumber;
    private final int physicalAddress;

    public abstract MessageProtocol getMessageProtocol();

    /**
     * Only for dummy instantiations
     */
    protected AbstractNtaMbusDevice() {
        this.meterProtocol = new WebRTUKP();
        this.serialNumber = "CurrentlyUnKnown";
        this.physicalAddress = -1;
    }

    public AbstractNtaMbusDevice(final AbstractSmartNtaProtocol meterProtocol, final String serialNumber, final int physicalAddress) {
        this.meterProtocol = meterProtocol;
        this.serialNumber = serialNumber;
        this.physicalAddress = physicalAddress;
    }

    /**
     * Return the DeviceTimeZone
     *
     * @return the DeviceTimeZone
     */
    public TimeZone getTimeZone() {
        return this.meterProtocol.getTimeZone();
    }

    /**
     * Getter for the used Logger
     *
     * @return the Logger
     */
    public Logger getLogger() {
        return this.meterProtocol.getLogger();
    }

    /**
     * The serialNumber of the meter
     *
     * @return the serialNumber of the meter
     */
    public String getSerialNumber() {
        return this.serialNumber;
    }

    /**
     * Get the physical address of the Meter. Mostly this will be an index of the meterList
     *
     * @return the physical Address of the Meter.
     */
    public int getPhysicalAddress() {
        return this.physicalAddress;
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

    public AbstractSmartNtaProtocol getMeterProtocol() {
        return meterProtocol;
    }

    /*
       Below are the 'unUsed' methods of the slaveMeters. But we need them because we want to implement
       the SmartMeterProtocol interface, so we can define these classes as 'pluggable' classes
    */


    @Override
    public void validateProperties() throws InvalidPropertyException, MissingPropertyException {
        //nothing to validate
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        // nothing to init
    }

    @Override
    public void connect() throws IOException {
        // nothing to connect, already connected
    }

    @Override
    public void disconnect() throws IOException {
        // nothing to disconnect, already disconnected
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "MbusFirmwareVersion - UnKnown";
    }

    @Override
    public String getMeterSerialNumber() throws IOException {
        return getSerialNumber();
    }

    @Override
    public Date getTime() throws IOException {
        throw new UnsupportedException("The Mbus device does not have a time");
    }

    @Override
    public void setTime(Date newMeterTime) throws IOException {
        // nothing to set
    }

    @Override
    public void initializeDevice() throws IOException, UnsupportedException {
        // nothing to initialize
    }

    @Override
    public void release() throws IOException {
        // nothing to release
    }

    @Override
    public RegisterInfo translateRegister(Register register) throws IOException {
        throw new UnsupportedException("The Mbus device can't translate his registers ...");
    }

    @Override
    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        throw new UnsupportedException("The Mbus device does not read his own registers, his master will do this.");
    }

    @Override
    public void setCache(Object cacheObject) {
        // nothing to set
    }

    @Override
    public Object getCache() {
        return null;  // nothing to set
    }

    @Override
    public Object fetchCache(int rtuId) throws SQLException, BusinessException {
        return null;  //nothing to fetch
    }

    @Override
    public void updateCache(int rtuId, Object cacheObject) throws SQLException, BusinessException {
        // nothing to update
    }

    @Override
    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws IOException {
        throw new UnsupportedException("The Mbus device does not read his own events, his master will do this for him.");
    }

    @Override
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        throw new UnsupportedException("The Mbus device does not fetch his own loadProfiles configs, his master will do this for him");
    }

    @Override
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        throw new UnsupportedException("The Mbus device does not read his own loadProfiles, his master will do this for him");
    }
    /**
     * Returns a list of required property keys
     *
     * @return a List of String objects
     */
    public List<String> getRequiredKeys() {
        return new ArrayList<>();
    }

    /**
     * Returns a list of optional property keys
     *
     * @return a List of String objects
     */
    public List<String> getOptionalKeys() {
        final ArrayList<String> optionals = new ArrayList<>(1);
        optionals.add(DlmsProtocolProperties.NTA_SIMULATION_TOOL);
        return optionals;
    }
}
