package com.energyict.smartmeterprotocolimpl.webrtuz3;

import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.CachedMeterTime;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.smartmeterprotocolimpl.common.SimpleMeter;
import com.energyict.smartmeterprotocolimpl.webrtuz3.composedobjects.ComposedMeterInfo;
import com.energyict.smartmeterprotocolimpl.webrtuz3.profiles.EMeterEventProfile;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 7-feb-2011
 * Time: 14:15:14
 */
public class WebRTUZ3 extends AbstractSmartDlmsProtocol implements SimpleMeter {

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

    @Override
    protected WebRTUZ3Properties getProperties() {
        if (properties == null) {
            properties = new WebRTUZ3Properties();
        }
        return properties;
    }

    /**
     * Initialization method right after we are connected to the physical device.
     */
    @Override
    protected void initAfterConnect() {
        //TODO read the slaveDevices and create a list of serialNumbers mapped to ObisCode-B-channels
    }

    /**
     * The protocol version in the following format: yyyy-mm-dd
     * This field is updated by svn on every commit.
     *
     * @return
     */
    public String getVersion() {
        return "$Date$";
    }

    /**
     * Get the firmware version of the meter
     *
     * @return the version of the meter firmware
     * @throws java.io.IOException Thrown in case of an exception
     * @throws com.energyict.protocol.UnsupportedException
     *                             Thrown if method is not supported
     */
    public String getFirmwareVersion() throws IOException {
        try {
            StringBuilder firmware = new StringBuilder();
            firmware.append(getMeterInfo().getFirmwareVersion());
            String rfFirmware = getRFFirmwareVersion();
            if (!rfFirmware.equalsIgnoreCase("")) {
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

    /**
     * Get the SerialNumber of the device
     *
     * @return the serialNumber of the device
     * @throws java.io.IOException thrown in case of an exception
     */
    public String getMeterSerialNumber() throws IOException {
        try {
            return getMeterInfo().getSerialNr();
        } catch (IOException e) {
            String message = "Could not retrieve the serialnumber of the meter. " + e.getMessage();
            getLogger().finest(message);
            throw new IOException(message);
        }
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
    public int requestConfigurationChanges() throws IOException {
        return getMeterInfo().getConfigurationChanges();
    }

    /**
     * Tests if the Rtu wants to use the bulkRequests
     *
     * @return true if the Rtu wants to use BulkRequests, false otherwise
     */
    public boolean supportsBulkRequests() {
        return getProperties().isBulkRequest();
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

    /**
     * @return the current device time
     * @throws java.io.IOException <br>
     */
    @Override
    public Date getTime() throws IOException {
        if (this.cachedMeterTime == null) {
            this.cachedMeterTime = new CachedMeterTime(super.getTime());
        }
        return this.cachedMeterTime.getTime();
    }

    /**
     * <p>
     * sets the device time to the current system time.
     * </p>
     *
     * @param newMeterTime the time to set in the meter
     * @throws java.io.IOException Thrown in case of an exception
     */
    @Override
    public void setTime(Date newMeterTime) throws IOException {
        this.cachedMeterTime = null;    // we clear the cachedMeterTime again
        super.setTime(newMeterTime);
    }

    /**
     * Fetch a whole list of registers. We should combine as much as possible values in
     * one request/response to the meter to save time and bandwidth
     *
     * @param registers
     * @return
     * @throws IOException
     */
    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        return getRegisterFactory().readRegisters(registers);
    }

    /**
     * Get a list of MeterEvents from the WebRTUZ3 that occurred after the lastLogbookDate
     *
     * @param lastLogbookDate The last event that's already in EIServer
     * @return A list of meterEvents
     */
    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws IOException {
        EMeterEventProfile eventProfile = new EMeterEventProfile(this, getDlmsSession());

        //TODO need the slaveMeterEvents!!!!!!

        return eventProfile.getEvents(lastLogbookDate);
    }

    /**
     * Get the configuration(interval, number of channels, channelUnits) of all given LoadProfiles from the WebRTUZ3.
     *
     * @param loadProfileObisCodes
     * @return
     */
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileObisCodes) {
        List<LoadProfileConfiguration> loadProfileConfigurations = new ArrayList<LoadProfileConfiguration>();
        // TODO: Implement the loadprofile configuration
        return loadProfileConfigurations;
    }

    /**
     * Fetches one or more LoadProfiles from the WebRTUZ3.
     *
     * @param loadProfiles
     * @return
     * @throws IOException
     */
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        List<ProfileData> profileData = new ArrayList<ProfileData>();
        // TODO: Implement the fetching of the profile data
        return profileData;
    }

    public WebRTUZ3RegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new WebRTUZ3RegisterFactory(this);
        }
        return this.registerFactory;
    }

    /**
     * The serialNumber of the meter
     *
     * @return the serialNumber of the meter
     */
    public String getSerialNumber() {
        return getProperties().getSerialNumber();
    }

    /**
     * Get the physical address of the Meter. Mostly this will be an index of the meterList
     *
     * @return the physical Address of the Meter.
     */
    public int getPhysicalAddress() {
        return 0; // the 'Master' has physicalAddress 0
    }
}
