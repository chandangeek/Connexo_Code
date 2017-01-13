/*
 * PRIPremier.java
 *
 * Created on 24 maart 2004, 17:36
 */

package com.energyict.protocolimpl.pact.pripact;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.pact.core.common.ChannelMap;
import com.energyict.protocolimpl.pact.core.common.DateTime;
import com.energyict.protocolimpl.pact.core.common.PACTConnection;
import com.energyict.protocolimpl.pact.core.common.PACTMode;
import com.energyict.protocolimpl.pact.core.common.PACTProfile;
import com.energyict.protocolimpl.pact.core.common.PACTRegisterFactory;
import com.energyict.protocolimpl.pact.core.common.PACTToolkit;
import com.energyict.protocolimpl.pact.core.common.PasswordValidator;
import com.energyict.protocolimpl.pact.core.common.ProtocolLink;
import com.energyict.protocolimpl.pact.core.instant.InstantaneousFactory;
import com.energyict.protocolimpl.pact.core.meterreading.MeterReadingIdentifier;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS;
import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;
import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;
import static com.energyict.mdc.upl.MeterProtocol.Property.RETRIES;
import static com.energyict.mdc.upl.MeterProtocol.Property.ROUNDTRIPCORRECTION;
import static com.energyict.mdc.upl.MeterProtocol.Property.SECURITYLEVEL;
import static com.energyict.mdc.upl.MeterProtocol.Property.SERIALNUMBER;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

/**
 * @author Koen
 * @beginchanges KV|24082004|Add custom properties 'KeyInfoRequired' and 'StatusFlagChannel' ||Add mechanism to remove
 * doubles and shift status flags to the next interval! ||Add 1 day extra to retrieve for the load
 * profile! KV|06082004|Minor bugfix with statusflag channel KV|26112004|If no statusflaggchannel == 0
 * (default) nrofchannels = surveyinfo->nrofchannels-1 KV|13012005|Changed streaming connection in
 * PactConnection... KV|23032005|Changed header to be compatible with protocol version tool
 * KV|28042005|Protect against wrong and corrupt meter configuration KV|20062005|Add MeterType = 0 (not
 * relevant) e.g. pulse counters KV|05072005|Avoid future logging events KV|29082006|Added 2 new
 * intervalstate flags DEVICE_ERROR and BATTERY_LOW KV|30112006|Avoid nullpointerexception when meter has
 * no maximumdemand registers GN|02042008|Readout meterTime at different way, if not supported, still the
 * old way; new way has a more fixed format to check if it is correct
 * @endchanges
 */
public class PRIPact extends PluggableMeterProtocol implements ProtocolLink, RegisterProtocol {

    private final PropertySpecService propertySpecService;
    private int DEBUG = 0;

    private TimeZone timeZone;
    private TimeZone registerTimeZone;
    private Logger logger;
    private String readDate = null;

    private PACTConnection pactConnection = null;

    private String strID;
    private String strPassword;
    private String serialNumber;
    private int protocolTimeout;
    private int maxRetries;
    private int roundtripCorrection;
    private int securityLevel;
    private String nodeId;
    private int echoCancelling;
    private ChannelMap channelMap = null;
    private PACTMode pactMode;
    private int extendedLogging;
    private int statusFlagChannel;
    private int keyInfoRequired;
    private int forcedRequestExtraDays;
    private int modulo;
    private int meterType;

    private String highKeyRef = null;
    private String highKey = null;
    private String lowKey = null;

    private PACTRegisterFactory pactRegisterFactory = null;
    private PACTProfile pactProfile = null;
    private PACTToolkit pactToolkit = null;
    private InstantaneousFactory instantaneousFactory = null;

    public PRIPact(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public void connect() throws IOException {
        getPactConnection().connect();

        // do pactlan stuff here
        if (getPACTMode().isPACTLAN()) {
            getPactConnection().globalDisableMeter();
            getPactConnection().specificEnableMeter(serialNumber);

            // TODO
            // getPactConnection().globalEnableMeter();
        } else {
            // getPactConnection().globalEnableMeter();
        }

        // do password clearance
        if ((strPassword != null) && ("".compareTo(strPassword) != 0)) {
            byte[] seed = getPactConnection().getPasswordClearanceSeed();
            if (seed != null) {
                PasswordValidator pv = new PasswordValidator(seed);
                getPactConnection().sendPasswordClearance(
                        pv.getPasswordClearanceRequest(securityLevel, Long.parseLong(strPassword, 16)));
            }
        } // if ((strPassword != null) && ("".compareTo(strPassword)!=0))

        // validate the configured serialNumber with the meter's serialId got from the meterreadings block's first block
        // and validate the deviceId against the clem program name got from the meterreadings block's first block
        // (see doc 'interpreting meter readings')
        validateMeterIdentification();
    }

    @Override
    public void disconnect() throws IOException {
        // do pactlan stuff here
        if (getPACTMode().isPACTLAN()) {
            getPactConnection().globalEnableMeter();
        }

        getPactRegisterFactory().getFileTransfer().deleteFile();
    }

    public String getFirmwareVersion() throws IOException {
        return "CLEM program=" + getPactRegisterFactory().getMeterReadingsInterpreter().getClemProgramName()
                + ", MeterType=" + getPactRegisterFactory().getMeterIdentitySerialNumber().getMeterType().getStrType()
                + ", SerialId=" + getPactRegisterFactory().getMeterReadingsInterpreter().getSerialId(); // +
        // ", TariffName="+getPactRegisterFactory().getMeterReadingsInterpreter().getTariffNameFlags()
        // ", TariffName="++getPactRegisterFactory().getMeterReadingsInterpreter().getCurrentTariffName()
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-13 15:14:02 +0100 (Fri, 13 Nov 2015) $";
    }

    private void validateMeterIdentification() throws IOException {
        if ((strID != null) && ("".compareTo(strID) != 0)) {
            String clemProgramName = getPactRegisterFactory().getMeterReadingsInterpreter().getClemProgramName();
            if (strID.compareTo(clemProgramName) != 0) {
                throw new IOException("PRIPact, validateMeterIdentification(), Wrong clemProgramName!, meter="
                        + clemProgramName + ", configured=" + strID);
            }
        }
        if ((serialNumber != null) && ("".compareTo(serialNumber) != 0)) {
            String serialId = getPactRegisterFactory().getMeterReadingsInterpreter().getSerialId();
            // String serialId = getSerialNumber();
            if (serialNumber.compareTo(serialId) != 0) {
                throw new IOException("PRIPact, validateMeterIdentification(), Wrong serialNumber!, meter=" + serialId
                        + ", configured=" + serialNumber);
            }
        }
    }

    @Override
    public Quantity getMeterReading(String name) throws IOException {
        if (name.indexOf("_") == 0) {
            return getInstantaneousFactory().getRegisterValue(name.substring(1));
        } else {
            return getPactRegisterFactory().getMeterReadingsInterpreter().getValue(new MeterReadingIdentifier(name));
        }
    }

    @Override
    public Quantity getMeterReading(int channelId) throws IOException {
        getPactProfile().initChannelInfo();
        return getPactRegisterFactory().getMeterReadingsInterpreter().getValueEType(
                getPactProfile().getLoadSurveyInterpreter().getEnergyTypeCode(channelId));
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        if (getChannelMap().getChannelFunctions() == null) {
            if (!isStatusFlagChannel()) {
                return getPactRegisterFactory().getMeterReadingsInterpreter().getSurveyInfo().getNrOfChannels() - 1;
            } else {
                return getPactRegisterFactory().getMeterReadingsInterpreter().getSurveyInfo().getNrOfChannels();
            }
        } else {
            return getChannelMap().getChannelFunctions().length;
        }
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        return getPactProfile().getProfileData(includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(getTimeZone());
        return getPactProfile().getProfileData(lastReading, calendar.getTime(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        throw new UnsupportedException("getProfileData(from,to) is not supported by this meter");
    }

    @Override
    public int getProfileInterval() throws IOException {
        return getPactRegisterFactory().getMeterReadingsInterpreter().getSurveyInfo().getProfileInterval();
    }

    @Override
    public String getRegister(String name) throws IOException {
        // return getPactRegisterFactory().getMeterReadingsInterpreter().getValue(new
        // MeterReadingIdentifier(name)).toString();
        return getInstantaneousFactory().getRegisterValue(name).toString();
    }

    @Override
    public void setRegister(String name, String value) throws IOException {
        throw new UnsupportedException();
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.stringSpec(ADDRESS.getName()),
                this.stringSpec(PASSWORD.getName()),
                this.stringSpec(SERIALNUMBER.getName()),
                this.integerSpec(TIMEOUT.getName()),
                this.integerSpec(RETRIES.getName()),
                this.integerSpec(ROUNDTRIPCORRECTION.getName()),
                this.integerSpec(SECURITYLEVEL.getName()),
                this.stringSpec(NODEID.getName()),
                this.stringSpec("EchoCancelling"),
                this.stringSpec("ChannelMap"),
                this.stringSpec("HighKey"),
                this.stringSpec("HighKeyRef"),
                this.stringSpec("LowKey"),
                this.integerSpec("PAKNET"),
                this.integerSpec("PACTLAN"),
                this.integerSpec("ExtendedLogging"),
                this.stringSpec("RegisterTimeZone"),
                this.integerSpec("StatusFlagChannel"),
                this.integerSpec("Modulo"),
                this.integerSpec("MeterType"),
                this.integerSpec("KeyInfoRequired"),
                this.integerSpec("ForcedRequestExtraDays"));
    }

    private <T> PropertySpec spec(String name, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, optionsSupplier).finish();
    }

    private PropertySpec stringSpec(String name) {
        return this.spec(name, this.propertySpecService::stringSpec);
    }

    private PropertySpec integerSpec(String name) {
        return this.spec(name, this.propertySpecService::integerSpec);
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            strID = properties.getTypedProperty(ADDRESS.getName());
            strPassword = properties.getTypedProperty(PASSWORD.getName());
            serialNumber = properties.getTypedProperty(SERIALNUMBER.getName());
            protocolTimeout = Integer.parseInt(properties.getTypedProperty(TIMEOUT.getName(), "30000").trim());
            maxRetries = Integer.parseInt(properties.getTypedProperty(RETRIES.getName(), "10").trim());
            roundtripCorrection = Integer.parseInt(properties.getTypedProperty(ROUNDTRIPCORRECTION.getName(), "0").trim());
            securityLevel = Integer.parseInt(properties.getTypedProperty(SECURITYLEVEL.getName(), "2").trim());
            nodeId = properties.getTypedProperty(NODEID.getName(), "001");
            echoCancelling = Integer.parseInt(properties.getTypedProperty("EchoCancelling", "0").trim());
            try {
                channelMap = new ChannelMap(properties.getTypedProperty("ChannelMap"));
            } catch (IOException e) {
                throw new InvalidPropertyException(e, "PRIPremier, setProperties");
            }

            highKey = properties.getTypedProperty("HighKey");
            highKeyRef = properties.getTypedProperty("HighKeyRef");
            lowKey = properties.getTypedProperty("LowKey");

            pactMode = new PACTMode(
                    Integer.parseInt(properties.getTypedProperty("PAKNET", "0")),
                    Integer.parseInt(properties.getTypedProperty("PACTLAN", "0")));
            extendedLogging = Integer.parseInt(properties.getTypedProperty("ExtendedLogging", "0"));
            if (properties.getTypedProperty("RegisterTimeZone") == null) {
                registerTimeZone = null;
            } else {
                registerTimeZone = TimeZone.getTimeZone(((String) properties.getTypedProperty("RegisterTimeZone")));
            }

            statusFlagChannel = Integer.parseInt(properties.getTypedProperty("StatusFlagChannel", "0"));
            keyInfoRequired = Integer.parseInt(properties.getTypedProperty("KeyInfoRequired", "1"));
            forcedRequestExtraDays = Integer.parseInt(properties.getTypedProperty("ForcedRequestExtraDays", "0"));
            modulo = Integer.parseInt(properties.getTypedProperty("Modulo", "10000000"));
            setMeterType(Integer.parseInt(properties.getTypedProperty("MeterType", "0")));

            if (pactMode.isPAKNET()) {
                channelMap.reverse();
            }

        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

    @Override
    public Date getTime() throws IOException {
        long roundTripTime = System.currentTimeMillis();
        DateTime currentTime;
        Date returnDate;

        currentTime = new DateTime(getPactConnection().sendRequest(PACTConnection.RTC), getTimeZone());
        if (getPactRegisterFactory().getCurrentTime() != null) {
            long currentTemp = currentTime.getDate().getTime();
            long tableTemp = getPactRegisterFactory().getCurrentTime().getTime();
            if (Math.abs(currentTemp - tableTemp) > 60000) {
                returnDate = getPactRegisterFactory().getCurrentTime();
            } else {
                returnDate = currentTime.getDate();
            }
        } else {
            getLogger().log(Level.WARNING, "Probably old meter, no secundairy meterTime check is made.");
            returnDate = currentTime.getDate();
        }

        if (DEBUG >= 2) {
            System.out.println("SystemTime : " + new Date(System.currentTimeMillis()));
        }
        if (DEBUG >= 2) {
            System.out.println("OldTime: " + getPactRegisterFactory().getCurrentTime() + " - CurrentTime : "
                    + currentTime.getDate());
        }

        roundTripTime = System.currentTimeMillis() - roundTripTime;

        return new Date(returnDate.getTime() - roundTripTime);
    }

    @Override
    public void setTime() throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(timeZone);
        int delay = (5 - (calendar.get(Calendar.SECOND) % 5)) * 1000;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
        calendar = ProtocolUtils.getCalendar(timeZone);
        calendar.add(Calendar.MILLISECOND, roundtripCorrection);
        if (getPACTToolkit() != null) {
            int oldTimeSeed = getPactRegisterFactory().getMeterReadingsInterpreter().getSeeds().getTimeSeed();
            int newTimeSeed = 0;

            // KV 07082006
            // If a calmu, sprint or premier meter have E4 starting their CLEM program, we still have the possibility to
            // overrule by
            // setting MeterType to 2
            if (!((getPactRegisterFactory().getMeterReadingsInterpreter().getClemProgramName().startsWith("E4")) || isMeterTypeICM200())) {
                newTimeSeed = oldTimeSeed;
            } else if (isMeterTypeCSP()) {
                newTimeSeed = oldTimeSeed;
            }

            byte[] frame = getPACTToolkit().generateTimeSetMessage(calendar, oldTimeSeed, newTimeSeed);
            if (!getPactConnection().writeRequest(frame)) {
                throw new IOException("Time set error! Possibly wrong or missing key information!");
            }
        } else {
            DateTime dateTime = new DateTime(calendar);
            if (!getPactConnection().writeRequest(PACTConnection.WTC, dateTime.getData())) {
                throw new IOException("Time set error! Possibly wrong or missing key information!");
            }
        }
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, java.util.TimeZone timeZone,
                     java.util.logging.Logger logger) throws IOException {
        this.timeZone = timeZone;
        this.logger = logger;
        // if (registerTimeZone == null)
        // registerTimeZone = timeZone;

        try {
            pactConnection = new PACTConnection(inputStream, outputStream, protocolTimeout, maxRetries, 300, echoCancelling);
            pactRegisterFactory = new PACTRegisterFactory(this);
            pactProfile = new PACTProfile(this, pactRegisterFactory);
            instantaneousFactory = new InstantaneousFactory(this);
            if (isValidation()) {
                pactToolkit = new PACTToolkit(Integer.parseInt(highKeyRef), highKey, (int) Long.parseLong(lowKey, 16));
            } else {
                logger.severe("PRIPremier: init(...), incomplete or missing key info, data will not be validated!");
            }
        } catch (ConnectionException e) {
            logger.severe("PRIPremier: init(...), " + e.getMessage());
        }

    }

    private boolean isValidation() throws IOException {
        if (!isKeyInfoRequired()) {
            return ((highKey != null) && (highKeyRef != null) && (lowKey != null));
        } else {
            if ((highKey == null) || (highKeyRef == null) || (lowKey == null)) {
                throw new IOException("Key information incomplete or missing! Correct first!");
            }
            return true;
        }
    }

    @Override
    public void initializeDevice() throws UnsupportedException {
        throw new UnsupportedException();
    }

    @Override
    public void release() {
    }

    @Override
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public boolean isExtendedLogging() {
        return (extendedLogging == 1);
    }

    @Override
    public PACTConnection getPactConnection() {
        return pactConnection;
    }

    /**
     * Getter for property pactRegisterFactory.
     *
     * @return Value of property pactRegisterFactory.
     */
    public PACTRegisterFactory getPactRegisterFactory() {
        return pactRegisterFactory;
    }

    /**
     * Getter for property pactProfile.
     *
     * @return Value of property pactProfile.
     */
    public PACTProfile getPactProfile() {
        return pactProfile;
    }

    @Override
    public ChannelMap getChannelMap() {
        return channelMap;
    }

    @Override
    public PACTToolkit getPACTToolkit() {
        return pactToolkit;
    }

    @Override
    public PACTMode getPACTMode() {
        return pactMode;
    }

    /**
     * Getter for property instantaneousFactory.
     *
     * @return Value of property instantaneousFactory.
     */
    public InstantaneousFactory getInstantaneousFactory() {
        return instantaneousFactory;
    }

    /**
     * Setter for property instantaneousFactory.
     *
     * @param instantaneousFactory New value of property instantaneousFactory.
     */
    public void setInstantaneousFactory(InstantaneousFactory instantaneousFactory) {
        this.instantaneousFactory = instantaneousFactory;
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return (getPactRegisterFactory().getMeterReadingsInterpreter().getValue(obisCode));
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        MeterReadingIdentifier mrid = new MeterReadingIdentifier(obisCode);
        return new RegisterInfo(mrid.getObisRegisterMappingDescription());
    }

    @Override
    public TimeZone getRegisterTimeZone() {
        return registerTimeZone;
    }

    @Override
    public boolean isStatusFlagChannel() {
        return statusFlagChannel != 0;
    }

    /**
     * Getter for property keyInfoRequired.
     *
     * @return Value of property keyInfoRequired.
     */
    public boolean isKeyInfoRequired() {
        return keyInfoRequired != 0;
    }

    @Override
    public int getForcedRequestExtraDays() {
        return forcedRequestExtraDays;
    }

    @Override
    public int getModulo() {
        return modulo;
    }

    public String getNodeId() {
        return nodeId;
    }

    public int getRoundtripCorrection() {
        return roundtripCorrection;
    }

    public int getMeterType() {
        return meterType;
    }

    private void setMeterType(int meterType) {
        this.meterType = meterType;
    }

    private static final int ICM200 = 1;
    private static final int CSP = 2; // Calmu, Sprint, Premier

    @Override
    public boolean isMeterTypeICM200() {
        return getMeterType() == ICM200;
    }

    @Override
    public boolean isMeterTypeCSP() {
        return getMeterType() == CSP;
    }

}