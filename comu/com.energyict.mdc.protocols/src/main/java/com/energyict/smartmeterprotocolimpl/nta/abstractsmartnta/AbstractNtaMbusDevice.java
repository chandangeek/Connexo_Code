package com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta;

import com.elster.jupiter.calendar.CalendarService;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.LoadProfileConfiguration;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocols.mdc.services.impl.OrmClient;
import com.energyict.smartmeterprotocolimpl.common.SimpleMeter;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Clock;
import java.util.ArrayList;
import java.util.Arrays;
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

    private final PropertySpecService propertySpecService;
    private final AbstractSmartNtaProtocol meterProtocol;
    private final Clock clock;
    private final TopologyService topologyService;
    private final LoadProfileFactory loadProfileFactory;
    private final String serialNumber;
    private final int physicalAddress;

    public abstract MessageProtocol getMessageProtocol();

    protected AbstractNtaMbusDevice(Clock clock, TopologyService topologyService, CalendarService calendarService, MdcReadingTypeUtilService readingTypeUtilService, LoadProfileFactory loadProfileFactory, OrmClient ormClient, PropertySpecService propertySpecService) {
        this.topologyService = topologyService;
        this.propertySpecService = propertySpecService;
        this.meterProtocol = new WebRTUKP(propertySpecService, clock, topologyService, calendarService, readingTypeUtilService, loadProfileFactory, ormClient);
        this.serialNumber = "CurrentlyUnKnown";
        this.physicalAddress = -1;
        this.loadProfileFactory = loadProfileFactory;
        this.clock = clock;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    protected Clock getClock() {
        return clock;
    }

    protected TopologyService getTopologyService() {
        return topologyService;
    }

    protected LoadProfileFactory getLoadProfileFactory() {
        return loadProfileFactory;
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
     * At a later timestamp the framework will query each {@link MessageEntry} (see {@link #queryMessage(MessageEntry)}) to actually
     * perform the message.
     *
     * @param messageEntries a list of {@link MessageEntry}s
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
    public Date getTime() throws UnsupportedException {
        throw new UnsupportedException("The Mbus device does not have a time");
    }

    @Override
    public void setTime(Date newMeterTime) {
        // nothing to set
    }

    @Override
    public void initializeDevice() {
        // nothing to initialize
    }

    @Override
    public void release() {
        // nothing to release
    }

    @Override
    public RegisterInfo translateRegister(Register register) throws UnsupportedException {
        throw new UnsupportedException("The Mbus device can't translate its registers ...");
    }

    @Override
    public List<RegisterValue> readRegisters(List<Register> registers) throws UnsupportedException {
        throw new UnsupportedException("The Mbus device does not read its own registers, its master will do this.");
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
    public Object fetchCache(int rtuId) {
        return null;  //nothing to fetch
    }

    @Override
    public void updateCache(int rtuId, Object cacheObject) {
        // nothing to update
    }

    @Override
    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws UnsupportedException {
        throw new UnsupportedException("The Mbus device does not read its own events, its master will do this.");
    }

    @Override
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws UnsupportedException {
        throw new UnsupportedException("The Mbus device does not fetch its own loadProfile configs, its master will do this.");
    }

    @Override
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws UnsupportedException {
        throw new UnsupportedException("The Mbus device does not read its own loadProfiles, its master will do this.");
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
        return Arrays.asList(DlmsProtocolProperties.NTA_SIMULATION_TOOL);
    }

}