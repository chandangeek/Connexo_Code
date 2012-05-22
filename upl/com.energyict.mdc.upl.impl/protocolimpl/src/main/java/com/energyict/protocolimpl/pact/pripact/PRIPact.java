/*
 * PRIPremier.java
 *
 * Created on 24 maart 2004, 17:36
 */

package com.energyict.protocolimpl.pact.pripact;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.PropertySpecFactory;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.pact.core.common.*;
import com.energyict.protocolimpl.pact.core.instant.InstantaneousFactory;
import com.energyict.protocolimpl.pact.core.meterreading.MeterReadingIdentifier;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

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
public class PRIPact implements MeterProtocol, ProtocolLink, RegisterProtocol {

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

    /**
     * Creates a new instance of PRIPremier
     */
    public PRIPact() {
    }

    public void connect() throws java.io.IOException {

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

    public void disconnect() throws java.io.IOException {

        // do pactlan stuff here
        if (getPACTMode().isPACTLAN()) {
            getPactConnection().globalEnableMeter();
        }

        getPactRegisterFactory().getFileTransfer().deleteFile();
    }

    public Object fetchCache(int rtuid) throws java.sql.SQLException, com.energyict.cbo.BusinessException {
        return null;
    }

    public Object getCache() {
        return null;
    }

    public String getFirmwareVersion() throws java.io.IOException {
        return "CLEM program=" + getPactRegisterFactory().getMeterReadingsInterpreter().getClemProgramName()
                + ", MeterType=" + getPactRegisterFactory().getMeterIdentitySerialNumber().getMeterType().getStrType()
                + ", SerialId=" + getPactRegisterFactory().getMeterReadingsInterpreter().getSerialId(); // +
        // ", TariffName="+getPactRegisterFactory().getMeterReadingsInterpreter().getTariffNameFlags()
        // ", TariffName="++getPactRegisterFactory().getMeterReadingsInterpreter().getCurrentTariffName()
    }

    public String getProtocolVersion() {
        // return "$Revision: 1.29 $";
        return "$Date$";
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
    } // private boolean validateMeterIdentification() throws NestedIOException

    // private String getSerialNumber() throws NestedIOException, ConnectionException {
    // byte[] data = getPactConnection().sendStringRequest("S");
    // String serial = new String(data);
    // return serial.substring(2);
    // }

    public com.energyict.cbo.Quantity getMeterReading(String name) throws java.io.IOException {
        if (name.indexOf("_") == 0) {
            return getInstantaneousFactory().getRegisterValue(name.substring(1));
        } else {
            return getPactRegisterFactory().getMeterReadingsInterpreter().getValue(new MeterReadingIdentifier(name));
        }
    }

    public com.energyict.cbo.Quantity getMeterReading(int channelId) throws java.io.IOException {
        getPactProfile().initChannelInfo();
        return getPactRegisterFactory().getMeterReadingsInterpreter().getValueEType(
                getPactProfile().getLoadSurveyInterpreter().getEnergyTypeCode(channelId));
    }

    public int getNumberOfChannels() throws java.io.IOException {
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

    public java.util.List getOptionalKeys() {
        List result = new ArrayList();
        result.add("Timeout");
        result.add("Retries");
        result.add("EchoCancelling");
        result.add("ChannelMap");
        result.add("HighKey");
        result.add("HighKeyRef");
        result.add("LowKey");
        result.add("SecurityLevel");
        result.add("PAKNET");
        result.add("PACTLAN");
        result.add("ExtendedLogging");
        result.add("RegisterTimeZone");
        result.add("StatusFlagChannel");
        result.add("KeyInfoRequired");
        result.add("ForcedRequestExtraDays");
        result.add("Modulo");
        result.add("MeterType");
        return result;
    }

    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        return getPactProfile().getProfileData(includeEvents);
    }

    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        Calendar calendar = ProtocolUtils.getCalendar(getTimeZone());
        return getPactProfile().getProfileData(lastReading, calendar.getTime(), includeEvents);
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        throw new UnsupportedException("getProfileData(from,to) is not supported by this meter");
    }

    public int getProfileInterval() throws java.io.IOException {
        return getPactRegisterFactory().getMeterReadingsInterpreter().getSurveyInfo().getProfileInterval();
    }

    public String getRegister(String name) throws java.io.IOException, NoSuchRegisterException {
        // return getPactRegisterFactory().getMeterReadingsInterpreter().getValue(new
        // MeterReadingIdentifier(name)).toString();
        return getInstantaneousFactory().getRegisterValue(name).toString();
    }

    public void setRegister(String name, String value) throws java.io.IOException, NoSuchRegisterException {
        throw new UnsupportedException();
    }

    private void validateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            Iterator iterator = getRequiredKeys().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (properties.getProperty(key) == null) {
                    throw new MissingPropertyException(key + " key missing");
                }
            }
            strID = properties.getProperty(MeterProtocol.ADDRESS);
            strPassword = properties.getProperty(MeterProtocol.PASSWORD);
            serialNumber = properties.getProperty(MeterProtocol.SERIALNUMBER);
            protocolTimeout = Integer.parseInt(properties.getProperty("Timeout", "30000").trim());
            maxRetries = Integer.parseInt(properties.getProperty("Retries", "10").trim());
            roundtripCorrection = Integer.parseInt(properties.getProperty("RoundtripCorrection", "0").trim());
            securityLevel = Integer.parseInt(properties.getProperty("SecurityLevel", "2").trim());
            nodeId = properties.getProperty(MeterProtocol.NODEID, "001");
            echoCancelling = Integer.parseInt(properties.getProperty("EchoCancelling", "0").trim());
            try {
                channelMap = new ChannelMap(properties.getProperty("ChannelMap"));
            } catch (IOException e) {
                throw new InvalidPropertyException("PRIPremier, validateProperties, IOException, " + e.toString());
            }

            highKey = properties.getProperty("HighKey");
            highKeyRef = properties.getProperty("HighKeyRef");
            lowKey = properties.getProperty("LowKey");

            pactMode = new PACTMode(Integer.parseInt(properties.getProperty("PAKNET", "0")), Integer
                    .parseInt(properties.getProperty("PACTLAN", "0")));
            extendedLogging = Integer.parseInt(properties.getProperty("ExtendedLogging", "0"));
            if (properties.getProperty("RegisterTimeZone") == null) {
                registerTimeZone = null;
            } else {
                registerTimeZone = TimeZone.getTimeZone(properties.getProperty("RegisterTimeZone"));
            }

            statusFlagChannel = Integer.parseInt(properties.getProperty("StatusFlagChannel", "0"));
            keyInfoRequired = Integer.parseInt(properties.getProperty("KeyInfoRequired", "1"));
            forcedRequestExtraDays = Integer.parseInt(properties.getProperty("ForcedRequestExtraDays", "0"));
            modulo = Integer.parseInt(properties.getProperty("Modulo", "10000000"));
            setMeterType(Integer.parseInt(properties.getProperty("MeterType", "0")));

            if (pactMode.isPAKNET()) {
                channelMap.reverse();
            }

        } catch (NumberFormatException e) {
            throw new InvalidPropertyException("PRIPremier, validateProperties, NumberFormatException, "
                    + e.getMessage());
        }
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys());
    }

    public java.util.List getRequiredKeys() {
        List result = new ArrayList(0);
        return result;
    }

    // public java.util.Date getTime() throws java.io.IOException {
    // long roundTripTime = 0;
    // int hour = 0; int min = 0; int sec = 0;
    // roundTripTime = Calendar.getInstance().getTime().getTime();
    // Date oldMeterDateTime = getPactRegisterFactory().getMeterReadingsInterpreter().getCounters().getMeterDateTime();
    // Calendar oldCalendar = Calendar.getInstance(getTimeZone());
    // String newMeterTime = getNewMeterTime();
    // // if (!newMeterTime.equalsIgnoreCase("NotSupported")){
    // // hour = Integer.parseInt(newMeterTime.substring(2, 4));
    // // min = Integer.parseInt(newMeterTime.substring(5, 7));
    // // sec = Integer.parseInt(newMeterTime.substring(8, 10));
    // // oldCalendar.set(Calendar.HOUR_OF_DAY, hour);
    // // oldCalendar.set(Calendar.MINUTE, min);
    // // oldCalendar.set(Calendar.SECOND, sec);
    // // roundTripTime = Calendar.getInstance().getTime().getTime() - roundTripTime;
    // // oldCalendar.setTimeInMillis(oldCalendar.getTimeInMillis() - roundTripTime);
    // // }
    //
    // // else{
    // DateTime dateTime = new DateTime(getPactConnection().sendRequest(PACTConnection.RTC),getTimeZone());
    // Date dateTime2 = getPactRegisterFactory().getCurrentTime();
    // roundTripTime = Calendar.getInstance().getTime().getTime() - roundTripTime;
    // oldCalendar.setTime(dateTime.getDate());
    // oldCalendar.setTimeInMillis(oldCalendar.getTimeInMillis() - roundTripTime);
    // // }
    //
    // return oldCalendar.getTime();
    //
    // }

    // private String getNewMeterTime() throws IOException {
    // String newMeterTime = null;
    // int count = 0;
    // do{
    // if (count >= 3)
    // throw new IOException("Error reading the dateTime from meter, will try again next communication.");
    // byte[] data = getPactConnection().sendStringRequest("R");
    // // byte[] data = getPactConnection().sendStringRequest("D");
    // newMeterTime = new String(data);
    // if (newMeterTime.equalsIgnoreCase("R") || newMeterTime.equalsIgnoreCase("R="))
    // return new String("NotSupported");
    // count++;
    // }while( !(newMeterTime.startsWith("R=")) || !(newMeterTime.startsWith(":", 4)) || !(newMeterTime.startsWith(":",
    // 7)) || (newMeterTime.length() != 10) );
    // return newMeterTime;
    // }

    public java.util.Date getTime() throws java.io.IOException {
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

    public void setTime() throws java.io.IOException {
        Calendar calendar = null;
        calendar = ProtocolUtils.getCalendar(timeZone);
        int delay = (5 - (calendar.get(Calendar.SECOND) % 5)) * 1000;
        try {
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            e.printStackTrace(); // should never happen
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

    public void init(java.io.InputStream inputStream, java.io.OutputStream outputStream, java.util.TimeZone timeZone,
                     java.util.logging.Logger logger) throws java.io.IOException {
        this.timeZone = timeZone;
        this.logger = logger;
        // if (registerTimeZone == null)
        // registerTimeZone = timeZone;

        try {
            pactConnection = new PACTConnection(inputStream, outputStream, protocolTimeout, maxRetries, 300,
                    echoCancelling);
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

    public void initializeDevice() throws java.io.IOException {
        throw new UnsupportedException();
    }

    public void release() throws java.io.IOException {
    }

    public void setCache(Object cacheObject) {
    }

    public void setProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        validateProperties(properties);
    }

    public void updateCache(int rtuid, Object cacheObject) throws java.sql.SQLException,
            com.energyict.cbo.BusinessException {
    }

    // implementation of ProtocolLink
    public java.util.TimeZone getTimeZone() {
        return timeZone;
    }

    static public void main(String[] args) {
        Dialer dialer = null;
        PRIPact priPact = null;
        try {
            dialer = DialerFactory.getDefault().newDialer();
            dialer.init("COM1");
            dialer.connect("optical", 60000);
            InputStream is = dialer.getInputStream();
            OutputStream os = dialer.getOutputStream();
            priPact = new PRIPact();
            Properties properties = new Properties();
            properties.setProperty("SecurityLevel", "2");
            properties.setProperty(MeterProtocol.ADDRESS, "");
            // properties.setProperty(MeterProtocol.PASSWORD,"FEDC0003");
            properties.setProperty(MeterProtocol.NODEID, "001");
            properties.setProperty("Timeout", "5000");
            properties.setProperty("Retries", "3");
            properties.setProperty("ChannelMap", "0,3,3,3");
            properties.setProperty("HighKey", "FCCA766563FA4F44E00BA59B6F26FF7EE8BBD2CCE970D571");
            properties.setProperty("HighKeyRef", "8234");
            properties.setProperty("LowKey", "00000000");
            properties.setProperty("PAKNET", "1");
            priPact.setProperties(properties);

            // PRI CALMU3+ meter UK
            // priPact.init(is,os,TimeZone.getTimeZone("GMT"),Logger.getLogger("name"));
            // atDialer.setParams(1200, 8, SerialCommunicationChannel.PARITY_NONE, 1);

            // PRI PREMIER meter EnergyICT
            priPact.init(is, os, TimeZone.getTimeZone("GMT+1"), Logger.getLogger("name"));
            dialer.getSerialCommunicationChannel().setParams(2400, SerialCommunicationChannel.DATABITS_8,
                    SerialCommunicationChannel.PARITY_NONE, SerialCommunicationChannel.STOPBITS_1);

            System.out.println("Start session");

            priPact.connect();

            // byte[] data = priPact.getPactConnection().getMeterReadingDataStream();
            // ProtocolUtils.printResponseData(data);
            // data = priPact.getPactConnection().getLoadSurveyDataStream(78, 0);
            // ProtocolUtils.printResponseData(data);

            // try {
            // priPact.getPactConnection().getLogData();
            // }
            // catch(NestedIOException e) {
            // System.out.println("not supported...");
            // }
            // System.out.println("TIME&DATE: "+priPact.getTime());
            //
            //
            // System.out.println(priPact.getPactRegisterFactory().getMeterIdentitySerialNumber().toString());
            System.out.println(priPact.getPactRegisterFactory().getMeterReadingsInterpreter().toString());
            // System.out.println("TIME&DATE: "+priPact.getTime());
            // priPact.setTime();
            // System.out.println("time set");

            // System.out.println("TIME&DATE: "+priPact.getTime());
            // System.out.println(priPact.getPactRegisterFactory().getMeterIdentitySerialNumber().toString());
            // System.out.println(priPact.getPactRegisterFactory().getMeterReadingsInterpreter().toString());
            //
            // System.out.println("Nr of channels: "+priPact.getNumberOfChannels());
            // System.out.println("ProfileInterval: "+priPact.getProfileInterval());
            // System.out.println("Time: "+priPact.getTime());
            //
            // for (int i = 0; i<2;i++) { //priPact.getNumberOfChannels();i++) {
            // System.out.println(priPact.getMeterReading(i));
            // }
            // System.out.println(priPact.getMeterReading("TOTAL.1:ch0"));
            // System.out.println(priPact.getMeterReading("TOTAL.1:ch1"));
            // System.out.println(priPact.getMeterReading("TOTAL.1:ch2"));
            //
            //
            // Calendar cal = ProtocolUtils.getCalendar(priPact.getTimeZone());
            // cal.add(Calendar.HOUR_OF_DAY,-1);
            // System.out.println(priPact.getProfileData(cal.getTime(),true).toString());

            priPact.disconnect();

            System.out.println("End session");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                priPact.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    } // void main()

    public Logger getLogger() {
        return logger;
    }

    public boolean isExtendedLogging() {
        return (extendedLogging == 1);
    }

    /**
     * Getter for property pactConnection.
     *
     * @return Value of property pactConnection.
     */
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

    public ChannelMap getChannelMap() {
        return channelMap;
    }

    public PACTToolkit getPACTToolkit() {
        return pactToolkit;
    }

    /**
     * Getter for property pactMode.
     *
     * @return Value of property pactMode.
     */
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

    // implementation of the RegisterProtocol
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return (getPactRegisterFactory().getMeterReadingsInterpreter().getValue(obisCode));
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        MeterReadingIdentifier mrid = new MeterReadingIdentifier(obisCode);
        RegisterInfo registerInfo = new RegisterInfo(mrid.getObisRegisterMappingDescription());
        return registerInfo;
    }

    /**
     * Getter for property registerTimeZone.
     *
     * @return Value of property registerTimeZone.
     */
    public java.util.TimeZone getRegisterTimeZone() {
        return registerTimeZone;
    }

    /**
     * Getter for property statusFlagChannel.
     *
     * @return Value of property statusFlagChannel.
     */
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

    /**
     * Getter for property forcedRequestExtraDays.
     *
     * @return Value of property forcedRequestExtraDays.
     */
    public int getForcedRequestExtraDays() {
        return forcedRequestExtraDays;
    }

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

    public boolean isMeterTypeICM200() {
        return getMeterType() == ICM200;
    }

    public boolean isMeterTypeCSP() {
        return getMeterType() == CSP;
    }

}
