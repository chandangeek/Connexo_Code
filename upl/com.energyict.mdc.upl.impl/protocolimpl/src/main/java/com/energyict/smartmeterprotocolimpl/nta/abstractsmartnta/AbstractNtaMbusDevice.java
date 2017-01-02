package com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta;

import com.energyict.mdc.upl.SmartMeterProtocol;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.smartmeterprotocolimpl.common.SimpleMeter;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.eict.WebRTUKP;
import com.google.common.base.Supplier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * The Abstract NTA Mbus device implements the {@link SmartMeterProtocol} so we can
 * define this as a pluggable class in EIS 9.1.
 * Most of the methods throw an {@link UnsupportedException}, if your subclass wants
 * to use one of these, then simple override them.
 * <p>
 * Copyrights EnergyICT
 * Date: 15-jul-2011
 * Time: 10:31:14
 */
public abstract class AbstractNtaMbusDevice implements SimpleMeter, SmartMeterProtocol, MessageProtocol, SerialNumberSupport {

    private final AbstractSmartNtaProtocol meterProtocol;
    private final PropertySpecService propertySpecService;
    private final String serialNumber;
    private final int physicalAddress;

    public abstract MessageProtocol getMessageProtocol();

    protected AbstractNtaMbusDevice(PropertySpecService propertySpecService, TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileExtractor messageFileExtractor) {
        this.propertySpecService = propertySpecService;
        this.meterProtocol = new WebRTUKP(calendarFinder, calendarExtractor, messageFileExtractor);
        this.serialNumber = "CurrentlyUnKnown";
        this.physicalAddress = -1;
    }

    public AbstractNtaMbusDevice(final AbstractSmartNtaProtocol meterProtocol, PropertySpecService propertySpecService, final String serialNumber, final int physicalAddress) {
        this.meterProtocol = meterProtocol;
        this.propertySpecService = propertySpecService;
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

    public List<MessageCategorySpec> getMessageCategories() {
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
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
        // nothing to init
    }

    @Override
    public void connect() {
        // nothing to connect, already connected
    }

    @Override
    public void disconnect() {
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
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws UnsupportedException {
        throw new UnsupportedException("The Mbus device does not fetch his own loadProfiles configs, his master will do this for him");
    }

    @Override
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws UnsupportedException {
        throw new UnsupportedException("The Mbus device does not read his own loadProfiles, his master will do this for him");
    }

    @Override
    public List<RegisterValue> readRegisters(List<Register> registers) throws UnsupportedException {
        throw new UnsupportedException("The Mbus device does not read his own registers, his master will do this.");
    }

    @Override
    public void setCache(Serializable cacheObject) {

    }

    @Override
    public Serializable getCache() {
        return null;
    }

    @Override
    public Serializable fetchCache(int deviceId, Connection connection) {
        return null;
    }

    @Override
    public void updateCache(int deviceId, Serializable cacheObject, Connection connection) {

    }

    @Override
    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws UnsupportedException {
        throw new UnsupportedException("The Mbus device does not read his own events, his master will do this for him.");
    }

    @Override
    public List<PropertySpec> getPropertySpecs() {
        return Collections.singletonList(this.spec(DlmsProtocolProperties.NTA_SIMULATION_TOOL, this.propertySpecService::stringSpec));
    }

    private <T> PropertySpec spec(String name, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, optionsSupplier).finish();
    }

}