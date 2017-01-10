package com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta;

import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.messaging.LegacyPartialLoadProfileMessageBuilder;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.BulkRegisterProtocol;
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
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.common.MasterMeter;
import com.energyict.smartmeterprotocolimpl.common.SimpleMeter;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.Dsmr23Properties;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.Dsmr23RegisterFactory;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.composedobjects.ComposedMeterInfo;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.EventProfile;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.topology.MeterTopology;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 14-jul-2011
 * Time: 11:20:34
 */
public abstract class AbstractSmartNtaProtocol extends AbstractSmartDlmsProtocol implements MasterMeter, SimpleMeter, MessageProtocol, SerialNumberSupport {

    public static final int ObisCodeBFieldIndex = 1;

    public static final ObisCode dailyObisCode = ObisCode.fromString("1.0.99.2.0.255");
    public static final ObisCode monthlyObisCode = ObisCode.fromString("0.0.98.1.0.255");


    public abstract MessageProtocol getMessageProtocol();

    /**
     * Get the AXDRDateTimeDeviationType for this DeviceType
     *
     * @return the requested type
     */
    public abstract AXDRDateTimeDeviationType getDateTimeDeviationType();

    /**
     * The <code>Properties</code> used for this protocol
     */
    protected Dsmr23Properties properties;

    /**
     * Indicating if the meter has a breaker.
     * This implies whether or not we can control the breaker and read the control logbook.
     * This will be set to false in the cryptoserver protocols, because these meters don't have a breaker anymore.
     */
    private boolean hasBreaker = true;

    /**
     * The used {@link com.energyict.smartmeterprotocolimpl.nta.dsmr23.composedobjects.ComposedMeterInfo}
     */
    private ComposedMeterInfo meterInfo;

    /**
     * The used {@link com.energyict.smartmeterprotocolimpl.nta.dsmr23.Dsmr23RegisterFactory}
     */
    protected Dsmr23RegisterFactory registerFactory;

    /**
     * The used {@link com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.LoadProfileBuilder}
     */
    protected LoadProfileBuilder loadProfileBuilder;

    /**
     * The used {@link com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.EventProfile}
     */
    protected EventProfile eventProfile;

    /**
     * The used {@link com.energyict.smartmeterprotocolimpl.nta.dsmr23.topology.MeterTopology}
     */
    protected MeterTopology meterTopology;

    /**
     * A list of slaveDevices
     */
    private List<AbstractNtaMbusDevice> mbusDevices = new ArrayList<>();

    /**
     * Getter for the {@link com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties}
     *
     * @return the requested Properties
     */
    @Override
    public DlmsProtocolProperties getProperties() {
        if (this.properties == null) {
            this.properties = new Dsmr23Properties();
        }
        return this.properties;
    }

    @Override
    protected void initAfterConnect() throws ConnectionException {
        searchForSlaveDevices();
//        for (DeviceMapping dm : getMeterTopology().getMbusMeterMap()) {
//            this.mbusDevices.add(new MbusDevice(this, dm.getSerialNumber(), dm.getPhysicalAddress()));
//        }
    }

    @Override
    public boolean supportsBulkRequests() {
        return getProperties().isBulkRequest();
    }

    /**
     * 'Lazy' getter for the {@link #meterInfo}
     *
     * @return the {@link #meterInfo}
     */
    public ComposedMeterInfo getMeterInfo() {
        if (meterInfo == null) {
            meterInfo = new ComposedMeterInfo(getDlmsSession(), supportsBulkRequests());
        }
        return meterInfo;
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        try {
            return getMeterInfo().getFirmwareVersion();
        } catch (IOException e) {
            String message = "Could not fetch the firmwareVersion. " + e.getMessage();
            getLogger().finest(message);
            return "UnKnown version";
        }
    }

    @Override
    public String getMeterSerialNumber() throws IOException {
        try {
            return getMeterInfo().getSerialNr();
        } catch (IOException e) {
            String message = "Could not retrieve the serialnumber of the meter. " + e.getMessage();
            getLogger().finest(message);
            throw e;
        }
    }

    @Override
    public int requestConfigurationChanges() throws IOException {
        return getMeterInfo().getConfigurationChanges();
    }

    /**
     * This method is used to request a RegisterInfo object that gives info
     * about the meter's supporting the specific ObisCode. If the ObisCode is
     * not supported, NoSuchRegister is thrown.
     *
     * @param register the Register to request RegisterInfo for
     * @return RegisterInfo about the ObisCode
     * @throws java.io.IOException Thrown in case of an exception
     */
    public RegisterInfo translateRegister(final Register register) throws IOException {
        return getRegisterFactory().translateRegister(register);
    }

    @Override
    public List<RegisterValue> readRegisters(final List<Register> registers) throws IOException {
        return getRegisterFactory().readRegisters(registers);
    }

    @Override
    public List<MeterEvent> getMeterEvents(final Date lastLogbookDate) throws IOException {
        return getEventProfile().getEvents(lastLogbookDate);
    }

    @Override
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(final List<LoadProfileReader> loadProfilesToRead) throws IOException {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<ProfileData> getLoadProfileData(final List<LoadProfileReader> loadProfiles) throws IOException {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-11-26 15:24:28 +0200 (Thu, 26 Nov 2015)$";
    }

    public BulkRegisterProtocol getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new Dsmr23RegisterFactory(this);
        }
        return registerFactory;
    }

    @Override
    public String getSerialNumber() {
        return getProperties().getSerialNumber();
    }

    @Override
    public int getPhysicalAddress() {
        return 0; // the 'Master' has physicalAddress 0
    }

    /**
     * Return a B-Field corrected ObisCode.
     *
     * @param obisCode     the ObisCode to correct
     * @param serialNumber the serialNumber of the device for which this ObisCode must be corrected
     * @return the corrected ObisCode
     */
    public ObisCode getPhysicalAddressCorrectedObisCode(final ObisCode obisCode, final String serialNumber) {
        int address;

        if (obisCode.equalsIgnoreBChannel(dailyObisCode) || obisCode.equalsIgnoreBChannel(monthlyObisCode)) {
            address = 0;
        } else {
            address = getPhysicalAddressFromSerialNumber(serialNumber);
        }
        if ((address == 0 && obisCode.getB() != -1) || obisCode.getB() == 128) { // then don't correct the obisCode
            return obisCode;
        }
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
     * Search for the physicalAddress of the meter with the given serialNumber
     *
     * @param serialNumber the serialNumber of the meter
     * @return the requested physical address or -1 when it could not be found
     */
    public int getPhysicalAddressFromSerialNumber(final String serialNumber) {
        return getMeterTopology().getPhysicalAddress(serialNumber);
    }

    public LoadProfileBuilder getLoadProfileBuilder() {
        if (this.loadProfileBuilder == null) {
            this.loadProfileBuilder = new LoadProfileBuilder(this);
        }
        return loadProfileBuilder;
    }

    /**
     * Getter for the <b>DSMR 2.3</b> EventProfile
     *
     * @return the lazy loaded EventProfile
     */
    public EventProfile getEventProfile() {
        if (this.eventProfile == null) {
            this.eventProfile = new EventProfile(this);
        }
        return eventProfile;
    }

    @Override
    public void searchForSlaveDevices() throws ConnectionException {
        getMeterTopology().searchForSlaveDevices();
    }

    public MeterTopology getMeterTopology() {
        if (this.meterTopology == null) {
            this.meterTopology = new MeterTopology(this);
        }
        return meterTopology;
    }

    @Override
    public void applyMessages(final List messageEntries) throws IOException {
        getMessageProtocol().applyMessages(messageEntries);
    }

    @Override
    public MessageResult queryMessage(final MessageEntry messageEntry) throws IOException {
        return getMessageProtocol().queryMessage(messageEntry);
    }

    @Override
    public List<MessageCategorySpec> getMessageCategories() {
        return getMessageProtocol().getMessageCategories();
    }

    @Override
    public String writeMessage(final Message msg) {
        return getMessageProtocol().writeMessage(msg);
    }

    @Override
    public String writeTag(final MessageTag tag) {
        return getMessageProtocol().writeTag(tag);
    }

    @Override
    public String writeValue(final MessageValue value) {
        return getMessageProtocol().writeValue(value);
    }

    public LegacyPartialLoadProfileMessageBuilder getPartialLoadProfileMessageBuilder() {
        return new LegacyPartialLoadProfileMessageBuilder();
    }

    public LegacyLoadProfileRegisterMessageBuilder getLoadProfileRegisterMessageBuilder() {
        return new LegacyLoadProfileRegisterMessageBuilder();
    }

    /**
     * Sets a custom LoadProfileBuilder object
     *
     * @param loadProfileBuilder the loadProfileBuilder to set
     */
    protected void setLoadProfileBuilder(LoadProfileBuilder loadProfileBuilder) {
        this.loadProfileBuilder = loadProfileBuilder;
    }

    public boolean hasBreaker() {
        return hasBreaker;
    }

    /**
     * Setter is only called from the cryptoserver protocols to remove the breaker functionality
     */
    public void setHasBreaker(boolean hasBreaker) {
        this.hasBreaker = hasBreaker;
    }

}