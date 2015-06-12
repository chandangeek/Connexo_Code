package com.energyict.protocolimpl.dlms.prime;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.GenericRead;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocolimpl.dlms.common.AbstractDlmsSessionProtocol;
import com.energyict.protocolimpl.dlms.common.ProfileCacheImpl;
import com.energyict.protocolimpl.dlms.prime.events.PrimeEventLogs;
import com.energyict.protocolimpl.dlms.prime.messaging.PrimeMessaging;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;

/**
 * Prime protocol, that should be able to read all the prime compliant devices (L&G, ZIV, Current, Elster, ...)
 *
 * Copyrights EnergyICT
 * Date: 21/02/12
 * Time: 14:43
 */
public abstract class AbstractPrimeMeter extends AbstractDlmsSessionProtocol {

    private final PrimeProperties properties = new PrimeProperties();
    private PrimeProfile loadProfile;
    private PrimeEventLogs eventLogs;
    private PrimeClock clock;
    private PrimeRegisters registers;
    private PrimeMeterInfo meterInfo;
    private PrimeMessaging messaging;
    private ProfileCacheImpl cache = new ProfileCacheImpl();

    public String getProtocolVersion() {
        return "$Date$";
    }

    @Override
    protected void doInit() {
        this.loadProfile = new PrimeProfile(getSession(), getProperties().getLoadProfileObiscode(), this.cache);
        this.eventLogs = new PrimeEventLogs(getSession(), getProperties());
        this.clock = new PrimeClock(getSession());
        this.meterInfo = new PrimeMeterInfo(getSession());
        this.registers = new PrimeRegisters(getProperties(), getSession(), meterInfo);
        this.messaging = new PrimeMessaging(getSession(), getProperties());
    }

    @Override
    protected String readSerialNumber() throws IOException {
        if (getProperties().isFirmwareClient()) {
            String serial = getProperties().getSerialNumber().trim();
            getLogger().warning("Unable to read serial number from device while using firmware client ID. Using configured serial [" + serial + "]!");
            return serial;
        }
        return meterInfo.getMeterSerialNumber();
    }

    public void connect() throws IOException {
        getSession().connect();
        if (getProperties().isReadSerialNumber()) {
            String eisSerial = getProperties().getSerialNumber().trim();
            String meterSerialNumber = readSerialNumber().trim();
            getLogger().info("Meter serial number [" + meterSerialNumber + "]");
            if (eisSerial.length() != 0) {
                if (!eisSerial.equalsIgnoreCase(meterSerialNumber)) {
                    String message = "Configured serial number [" + eisSerial + "] does not match with the meter serial number [" + meterSerialNumber + "]!";
                    getLogger().severe(message);
                    throw new IOException(message);
                }
            } else {
                getLogger().info("Skipping validation of meter serial number: No serial number found in EIServer.");
            }
        }
    }

    /**
     * Getter for the protocol properties wrapper
     *
     * @return The properties of the protocol
     */
    public PrimeProperties getProperties() {
        return properties;
    }

    public String getFirmwareVersion() throws IOException {
        return meterInfo.getAllFirmwareVersions();
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        if (getProperties().isFirmwareClient()) {
            getLogger().warning("Unable to read profile data from device while using firmware client ID. Skipping profile readout!");
            return new ProfileData();
        }

        final ProfileData profileData;
        if (properties.isEventsOnly()) {
            if (getProperties().isEventsOnly()) {
                getLogger().warning("Skipping profile because of [" + PrimeProperties.EVENTS_ONLY + "] property. Using empty ProfileData object!");
            }
            profileData = new ProfileData();
        } else {
            profileData = loadProfile.getProfileData(from, to);
        }

        if (includeEvents) {
            profileData.setMeterEvents(eventLogs.getMeterEvents(from, to));
        }

        profileData.sort();
        return profileData;

    }

    public int getNumberOfChannels() throws IOException {
        if (getProperties().isFirmwareClient()) {
            getLogger().warning("Unable to read number of profile channels from device while using firmware client ID. Returning [0] channels!");
            return 0;
        }

        if (getProperties().isEventsOnly()) {
            getLogger().warning("Skipping profile because of [" + PrimeProperties.EVENTS_ONLY + "] property. Returning [0] channels!");
            return 0;
        }

        return loadProfile.getNumberOfChannels();
    }

    public int getProfileInterval() throws IOException {
        if (getProperties().isFirmwareClient()) {
            final int profileInterval = getProperties().getProfileInterval();
            getLogger().warning("Unable to read profile interval from device while using firmware client ID. Returning configured interval of [" + profileInterval + " seconds]!");
            return profileInterval;
        }
        if (getProperties().isEventsOnly()) {
            final int profileInterval = getProperties().getProfileInterval();
            getLogger().warning("Skipping profile because of [" + PrimeProperties.EVENTS_ONLY + "] property. Returning configured interval of [" + profileInterval + " seconds]!");
            return profileInterval;
        }
        return loadProfile.getProfileInterval();
    }

    public Date getTime() throws IOException {
        if (getProperties().isFirmwareClient()) {
            getLogger().warning("Unable to read device time while using firmware client ID. Returning system time!");
            return new Date();
        }
        return clock.getTime();
    }

    public void setTime() throws IOException {
        if (getProperties().isFirmwareClient()) {
            getLogger().warning("Unable to change device time while using firmware client ID. Skipping clock set!");
        }
        clock.setTime();
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
            return registers.readRegister(obisCode);
        } catch (IOException e) {
            throw new NoSuchRegisterException(e.getMessage());
        } catch (RuntimeException e) {
            if (getLogger().isLoggable(Level.SEVERE)) {
                getLogger().log(Level.SEVERE, "Got RuntimeException while reading register with obis code [" + obisCode + "]: " + e.getMessage(), e);
            }
            throw new NoSuchRegisterException("Unable to read register with obis [" + obisCode + "]" + e.getMessage());
        }
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return messaging.queryMessage(messageEntry);
    }

    public List<MessageCategorySpec> getMessageCategories() {
        return PrimeMessaging.getMessageCategories();
    }

    @Override
    public Object getCache() {
        return this.cache;
    }

    @Override
    public void setCache(Object cache) {
        if ((cache != null) && (cache instanceof ProfileCacheImpl)) {
            this.cache = (ProfileCacheImpl) cache;
        }
    }

    /**
     * Read a raw register value
     *
     * @param rawRegisterId The raw register id (classId:obiscode:attributeNr) (see {@link com.energyict.dlms.DLMSAttribute#fromString(java.lang.String)})
     * @return The value of the requested register id as hex string (still ber encoded)
     * @throws java.io.IOException
     * @see com.energyict.dlms.DLMSAttribute#fromString(java.lang.String)
     */
    @Override
    public String getRegister(final String rawRegisterId) throws IOException {
        try {
            final DLMSAttribute dlmsAttribute = DLMSAttribute.fromString(rawRegisterId);
            final CosemObjectFactory cof = getSession().getCosemObjectFactory();
            final GenericRead genericRead = cof.getGenericRead(dlmsAttribute);
            final byte[] responseData = genericRead.getResponseData();
            return responseData != null ? ProtocolTools.getHexStringFromBytes(responseData, "") : null;
        } catch (IllegalArgumentException e) {
            final String msg = "The given register id [" + rawRegisterId + "] could not be parsed: " + e.getMessage();
            getLogger().log(Level.WARNING, msg, e);
            throw new NoSuchRegisterException(msg);
        } catch (IOException e) {
            final String msg = "An error occurred while reading raw register with id [" + rawRegisterId + "]: " + e.getMessage();
            getLogger().log(Level.WARNING, msg, e);
            throw new NoSuchRegisterException(msg);
        }
    }

}
