package com.energyict.smartmeterprotocolimpl.webrtuz3;

import com.energyict.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocol.*;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 7-feb-2011
 * Time: 14:15:14
 */
public class WebRTUZ3 extends AbstractSmartDlmsProtocol {

    private WebRTUZ3Properties properties;

    @Override
    protected WebRTUZ3Properties getProperties() {
        if (properties == null) {
            properties = new WebRTUZ3Properties();
        }
        return properties;
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
     * Get the current system time of the WebRTUZ3
     *
     * @return
     * @throws IOException
     */
    public Date getTime() throws IOException {
        // TODO: Implement the setTime method
        return new Date();
    }

    /**
     * Set the device time of the WebRTUZ3 to the current commserver time
     *
     * @throws IOException
     */
    public void setTime() throws IOException {
        // TODO: Implement the setTime method
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
     * Fetch a whole list of registers. We should combine as much as possible values in
     * one request/response to the meter to save time and bandwidth
     *
     * @param registers
     * @return
     * @throws IOException
     */
    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();
        // TODO: Implement the translateRegister method
        return registerValues;
    }

    /**
     * Get a list of MeterEvents from the WebRTUZ3 that occurred after the lastLogbookDate
     *
     * @param lastLogbookDate The last event that's already in EIServer
     * @return A list of meterEvents
     */
    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) {
        List<MeterEvent> meterEvents = new ArrayList<MeterEvent>();
        // TODO: Implement the meter events
        return meterEvents;
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

    /**
     * <p></p>
     *
     * @param name Register name. Devices supporting OBIS codes,
     *             should use the OBIS code as register name
     *             </p>
     * @return the value for the specified register
     *         </p><p>
     * @throws java.io.IOException <br>
     * @throws com.energyict.protocol.UnsupportedException
     *                             if the device does not support this operation
     * @throws com.energyict.protocol.NoSuchRegisterException
     *                             if the device does not support the specified register
     */
    public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
        //TODO check if you can put these in the AbstractSmartDlmsProtocol
        return null;  //TODO implement proper functionality.
    }

    /**
     * <p>
     * sets the specified register to value
     * </p>
     *
     * @param name  Register name. Devices supporting OBIS codes,
     *              should use the OBIS code as register name
     * @param value to set the register.
     *              </p>
     * @throws java.io.IOException <br>
     * @throws com.energyict.protocol.UnsupportedException
     *                             if the device does not support this operation
     * @throws com.energyict.protocol.NoSuchRegisterException
     *                             if the device does not support the specified register
     */
    public void setRegister(String name, String value) throws IOException, NoSuchRegisterException, UnsupportedException {
        //TODO check if you can put these in the AbstractSmartDlmsProtocol
        //TODO implement proper functionality.
    }
}
