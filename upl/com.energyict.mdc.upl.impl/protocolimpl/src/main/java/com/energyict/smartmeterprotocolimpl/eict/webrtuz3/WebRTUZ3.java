package com.energyict.smartmeterprotocolimpl.eict.webrtuz3;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupFinder;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.CachedMeterTime;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.common.MasterMeter;
import com.energyict.smartmeterprotocolimpl.common.SimpleMeter;
import com.energyict.smartmeterprotocolimpl.common.topology.DeviceMapping;
import com.energyict.smartmeterprotocolimpl.eict.webrtuz3.composedobjects.ComposedMeterInfo;
import com.energyict.smartmeterprotocolimpl.eict.webrtuz3.events.EMeterEventProfile;
import com.energyict.smartmeterprotocolimpl.eict.webrtuz3.messaging.WebRTUZ3MessageExecutor;
import com.energyict.smartmeterprotocolimpl.eict.webrtuz3.messaging.WebRTUZ3Messaging;
import com.energyict.smartmeterprotocolimpl.eict.webrtuz3.profiles.LoadProfileBuilder;
import com.energyict.smartmeterprotocolimpl.eict.webrtuz3.topology.MeterTopology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * //TODO we should find a way to store all our request and responses so we can reuse them in other requests
 * <p/>
 * Copyrights EnergyICT
 * Date: 7-feb-2011
 * Time: 14:15:14
 */
@Deprecated //Use the V2 protocol instead: com.energyict.protocolimplv2.eict.webrtuz3.WebRTUZ3
public class WebRTUZ3 extends AbstractSmartDlmsProtocol implements MasterMeter, SimpleMeter, MessageProtocol {

    /**
     * Contains properties related to the WebRTUZ3 protocol
     */
    private WebRTUZ3Properties properties;

    /**
     * Contains information about the meter (serialNumber, firwmareVersion, ...)
     */
    private ComposedMeterInfo meterInfo;

    /**
     * The Factory containing all the Register related information/functionality
     */
    private WebRTUZ3RegisterFactory registerFactory;

    /**
     * Contains the actual meterTime
     */
    private CachedMeterTime cachedMeterTime;

    /**
     * Represents the meter his topology
     */
    private MeterTopology meterTopology;

    /**
     * Contains a summary of all the slaveMeters
     */
    private List<SlaveMeter> slaveMeters = new ArrayList<SlaveMeter>();

    /**
     * Represents the <CODE>LoadProfileBuilder</CODE> to use
     */
    private LoadProfileBuilder loadProfileBuilder;

    private final WebRTUZ3Messaging messageProtocol;

    private static final int ObisCodeBFieldIndex = 1;

    private final PropertySpecService propertySpecService;

    public WebRTUZ3(TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor messageFileExtractor, PropertySpecService propertySpecService, NumberLookupFinder numberLookupFinder, NumberLookupExtractor numberLookupExtractor) {
        this.messageProtocol = new WebRTUZ3Messaging(new WebRTUZ3MessageExecutor(this, calendarFinder, calendarExtractor, messageFileFinder, messageFileExtractor, numberLookupFinder, numberLookupExtractor));
        this.propertySpecService = propertySpecService;
    }

//    /**
//     * Contains a Map of all the requested objects in this communicationSession
//     */
//    private Map<DLMSAttribute, AbstractDataType> sessionCachedObjects = new HashMap<DLMSAttribute, AbstractDataType>();

    @Override
    protected WebRTUZ3Properties getProperties() {
        if (properties == null) {
            properties = new WebRTUZ3Properties(this.propertySpecService);
        }
        return properties;
    }

    @Override
    protected void initAfterConnect() throws ConnectionException {
        searchForSlaveDevices();
        for (DeviceMapping dm : getMeterTopology().geteMeterMap()) {
            this.slaveMeters.add(new EMeter(this, dm.getSerialNumber(), dm.getPhysicalAddress()));
        }
        for (DeviceMapping dm : getMeterTopology().getMbusMap()) {
            this.slaveMeters.add(new MbusDevice(this, dm.getSerialNumber(), dm.getPhysicalAddress()));
        }
    }

    public MeterTopology getMeterTopology() {
        if (this.meterTopology == null) {
            this.meterTopology = new MeterTopology(this);
        }
        return this.meterTopology;
    }

    public void setMeterTopology(MeterTopology meterTopology) {
        this.meterTopology = meterTopology;
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT WebRTU Z3 DLMS";
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-11-26 15:26:01 +0200 (Thu, 26 Nov 2015)$";
    }

    public String getFirmwareVersion() throws IOException {
        try {
            StringBuilder firmware = new StringBuilder();
            firmware.append(getMeterInfo().getFirmwareVersion());
            String rfFirmware = getRFFirmwareVersion();
            if (!"".equalsIgnoreCase(rfFirmware)) {
                firmware.append(" - RF-FirmwareVersion : ");
                firmware.append(rfFirmware);
            }
            return firmware.toString();
        } catch (IOException e) {
            String message = "Could not fetch the firmwareVersion. " + e.getMessage();
            getLogger().finest(message);
            return "UnKnown version";
        }
    }

    /**
     * Read the Z3/R2 RF-Firmwareversion
     *
     * @return the firmwareversion, if it's not available then return an empty string
     */
    private String getRFFirmwareVersion() {
        try {
            return getMeterInfo().getRFFirmwareVersion();
        } catch (IOException e) {
            return "";
        }
    }

    public String getMeterSerialNumber() throws IOException {
        try {
            return getMeterInfo().getSerialNr();
        } catch (IOException e) {
            String message = "Could not retrieve the serialnumber of the meter. " + e.getMessage();
            getLogger().finest(message);
            throw new IOException(message);
        }
    }

    public ComposedMeterInfo getMeterInfo() {
        if (meterInfo == null) {
            meterInfo = new ComposedMeterInfo(getDlmsSession(), supportsBulkRequests());
        }
        return meterInfo;
    }

    @Override
    public int requestConfigurationChanges() throws IOException {
        return getMeterInfo().getConfigurationChanges();
    }

    /**
     * Get a description of a given register, identified by the obiscode/serial number combination
     *
     * @param register The register we need to get info for
     * @return The register info for the requested info, or null if the register is unknown
     * @throws IOException
     */
    public RegisterInfo translateRegister(Register register) throws IOException {
        // TODO: Implement the translateRegister method
        return null;
    }

    @Override
    public Date getTime() throws IOException {
        if (this.cachedMeterTime == null) {
            this.cachedMeterTime = new CachedMeterTime(super.getTime());
        }
        return this.cachedMeterTime.getTime();
    }

    @Override
    public void setTime(Date newMeterTime) throws IOException {
        this.cachedMeterTime = null;    // we clear the cachedMeterTime again
        super.setTime(newMeterTime);
    }

    @Override
    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        return getRegisterFactory().readRegisters(registers);
    }

    @Override
    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws IOException {
        List<MeterEvent> meterEvents = new ArrayList<>();
        EMeterEventProfile eventProfile = new EMeterEventProfile(this, getDlmsSession());
        meterEvents.addAll(eventProfile.getEvents(lastLogbookDate));


        //TODO check if the MBus event Profiles have another ObisCode

        // Loop all slave EventProfiles
        for (SlaveMeter sm : this.slaveMeters) {
            try {
                eventProfile = new EMeterEventProfile(sm, getDlmsSession());
                meterEvents.addAll(eventProfile.getEvents(lastLogbookDate));
            } catch (IOException e) {
                getLogger().info("Could not read the events from meter " + sm.getConfiguredSerialNumber());
            }
        }

        return meterEvents;
    }

    @Override
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders) throws IOException {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfileReaders);
    }

    @Override
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }

    public WebRTUZ3RegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new WebRTUZ3RegisterFactory(this);
        }
        return this.registerFactory;
    }

    @Override
    public String getConfiguredSerialNumber() {
        return getProperties().getSerialNumber();
    }

    @Override
    public int getPhysicalAddress() {
        return 0; // the 'Master' has physicalAddress 0
    }

    /**
     * Search for the physicalAddress of the meter with the given serialNumber
     *
     * @param serialNumber the serialNumber of the meter
     * @return the requested physical address or -1 when it could not be found
     */
    public int getPhysicalAddressFromSerialNumber(String serialNumber) {
        return getMeterTopology().getPhysicalAddress(serialNumber);
    }

    /**
     * Return a B-Field corrected ObisCode.
     *
     * @param obisCode     the ObisCode to correct
     * @param serialNumber the serialNumber of the device for which this ObisCode must be corrected
     * @return the corrected ObisCode
     */
    public ObisCode getPhysicalAddressCorrectedObisCode(ObisCode obisCode, String serialNumber) {
        int address = getPhysicalAddressFromSerialNumber(serialNumber);
        if (address != -1) {
            return ProtocolTools.setObisCodeField(obisCode, ObisCodeBFieldIndex, (byte) address);
        }
        return null;
    }

    /**
     * Return the serialNumber of the meter which corresponds with the B-Field of the given ObisCode
     *
     * @param obisCode the ObisCode
     * @return the serialNumber
     */
    public String getSerialNumberFromCorrectObisCode(ObisCode obisCode) {
        return getMeterTopology().getSerialNumber(obisCode);
    }

    /**
     * Getter for the {@link #loadProfileBuilder}
     *
     * @return the lazyLoaded <CODE>LoadProfileBuilder</CODE>
     */
    public LoadProfileBuilder getLoadProfileBuilder() {
        if (this.loadProfileBuilder == null) {
            this.loadProfileBuilder = new LoadProfileBuilder(this);
        }
        return this.loadProfileBuilder;
    }

    //    /**
//     * Save the given object to the {@link #sessionCachedObjects}
//     *
//     * @param dlmsAttribute    the definition of the object to cache
//     * @param abstractDataType the result in the meter of the object
//     */
//    public void saveObject(DLMSAttribute dlmsAttribute, AbstractDataType abstractDataType) {
//        this.sessionCachedObjects.put(dlmsAttribute, abstractDataType);
//    }

    @Override
    public void applyMessages(final List<MessageEntry> messageEntries) throws IOException {
        this.messageProtocol.applyMessages(messageEntries);
    }

    @Override
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        return this.messageProtocol.queryMessage(messageEntry);
    }

    @Override
    public List<MessageCategorySpec> getMessageCategories() {
        return this.messageProtocol.getMessageCategories();
    }

    @Override
    public String writeMessage(final Message msg) {
        return this.messageProtocol.writeMessage(msg);
    }

    @Override
    public String writeTag(final MessageTag tag) {
        return this.messageProtocol.writeTag(tag);
    }

    @Override
    public String writeValue(final MessageValue value) {
        return this.messageProtocol.writeValue(value);
    }

    @Override
    public void searchForSlaveDevices() throws ConnectionException {
        getMeterTopology().discoverSlaveDevices();
    }

    public SlaveMeter getSlaveMeterForSerial(String serialNumber){
        for (SlaveMeter slaveMeter : slaveMeters) {
            if(slaveMeter.getConfiguredSerialNumber().equalsIgnoreCase(serialNumber)){
                return slaveMeter;
            }
        }
        return null;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return getProperties().getUPLPropertySpecs();
    }

}