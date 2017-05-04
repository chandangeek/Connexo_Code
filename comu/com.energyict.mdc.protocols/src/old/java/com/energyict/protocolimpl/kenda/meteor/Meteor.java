package com.energyict.protocolimpl.kenda.meteor;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.events.MeterEvent;
import com.energyict.mdc.protocol.api.legacy.dynamic.PropertySpecFactory;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

public class Meteor extends PluggableMeterProtocol implements RegisterProtocol {

    @Override
    public String getProtocolDescription() {
        return "Kenda Meteor";
    }

    /**
     * ---------------------------------------------------------------------------------<p>
     * Meteor Protocol description:<p>
     * The protocol consists out of layers<p>
     * <p/>
     * 1) The deepest layer is the Parsers layer.  Part of the layer has been made
     * static and made abstract.  The second layer extends this class.
     * <p/>
     * 2) All registers are implemented in classes. The classes always implement the
     * type of methods: process to parse (deserializes) the byte array in the
     * object variables,printData visualizes the data matrix (parsed) in the console
     * and parseToByteArray serializes the object.
     * <p/>
     * 3) The MeteorCommunicationsFactory deals with all the communication issues.  It
     * arranges the data flows and communication.  It starts with sending the command
     * and getting back the right object to parse the data in.  Also the profileData
     * factory is implemented in this class.
     * <p/>
     * 4) This class: Meteor.java masters and is the interface from the server
     * <p/>
     * Additional classes are implemented mostly to help in the Unit testing.
     * <p/>
     * Initial version:<p>
     * ----------------<p>
     * Author: Peter Staelens, ITelegance (peter@Itelegance.com or P.Staelens@EnergyICT.com)<p>
     * Version: 1.0 <p>
     * First edit date: 1/07/2008 PST<p>
     * Last edit date: 13/08/2008  PST<p>
     * Comments: Beta ready for testing<p>
     * Released for testing: 13/08/2008<p>
     * <p/>
     * Revisions<p>
     * ----------------<p>
     *
     * @Author: Peter Staelens, ITelegance (peter@Itelegance.com or P.Staelens@EnergyICT.com)<p>
     * @Version: 1.01 <p>
     * First edit date: 26/08/2008 PST<p>
     * Last edit date: 1/09/2008  PST<p>
     * Comments: Beta+ ready for testing<p>
     * Released for testing: 1/09/2008<p>
     * ---------------------------------------------------------------------------------<p>
     * <p/>
     * Changes:
     * JME	|05022009|	Fixed timing issues that prevented some meters to readout the load profile data.
     * -> Fixed by changing the receive interframe timeout to 3 times the timeout.
     * -> Lowered the interframe retries to prevent huge call times when meter hangs.
     */

    private OutputStream outputStream;
    private InputStream inputStream;
    private int DEBUG = 0;
    private MeteorCommunicationsFactory mcf;
    private int outstationID, retry, timeout, delayAfterConnect;

    // command descriptions from the datasheet
    // Header format, at the moment I consider only ident as a variable
    private byte ident;                    // see ident format listed below
    private byte blockSize;                 // character count of block modulo 256
    private byte[] sourceCode;        // Defines central equipment of origin
    private byte sourceCodeExt;        // Defines peripheral equipment of origin
    private byte[] destinationCode;    // Defines central equipment of final destination
    private byte destinationCodeExt;// Defines peripheral equipment of final destination
    private byte unit;                    // DIP routing ???
    private byte port;                    // DIP routing ???

    // data objects
    private MeteorFullPersonalityTable fullperstable = null;
    private MeteorExtendedPersonalityTable extperstable = null;
    private MeteorStatus statusreg = null;
    private ObisCodeMapper ocm;
    private ProtocolChannelMap channelMap;

    private TimeZone timezone;

    // ident byte
    // Ack bit, first block bit, last block bit, R/W, 4 bit operation select

    // operation select codes, lowest 5 bit of ident
    private static final byte RESERVED = 0x00;          // internal DIP/Central System function
    private static final byte fullPersTableRead = 0x01;      // ram initialised, table defines modes
    private static final byte fullPersTableWrite = 0x11;    // ... page 6/16 Meteor communications protocol
    private static final byte extendedPersTableRead = 0x02;
    private static final byte extendedPersTableWrite = 0x12;
    private static final byte readRTC = 0x03;
    private static final byte setRTC = 0x13;
    private static final byte trimRTC = 0x14;
    private static final byte firmwareVersion = 0x04;
    private static final byte status = 0x05;
    private static final byte readRelay = 0x16;
    private static final byte setRelay = 0x06;
    private static final byte meterDemands = 0x07;
    private static final byte totalDemands = 0x08;  // Not available (page 12/16)
    private static final byte readingTimes = 0x09;
    private static final byte writingTimes = 0x19;
    private static final byte readdialReadingCurrent = 0x0A;
    private static final byte writedialReadingCurrent = 0x1A;
    private static final byte dialReadingPast = 0x0B;
    private static final byte powerFailDetails = 0x0C;
    private static final byte readCommissioningCounters = 0x0D;
    private static final byte writeCommissioningCounters = 0x1D;
    private static final byte readMemoryDirect = 0x0E;
    private static final byte writeMemoryDirect = 0x1E;
    private static final byte priorityTelNo = 0x1F; // N/A
    private static final byte alarmChanTimes = 0x0F; // N/A
    // first three bits are to be set in BuildIdent method (later)
    // byte: 8 bit, word 16 bit signed integer, long 32 bit signed integer

    @Inject
    public Meteor(PropertySpecService propertySpecService) {
        super(propertySpecService);
        byte[] blank = {0, 0};
        ident = 0;                // see ident format listed below
        blockSize = 11;            // character count of block modulo 256
        sourceCode = blank;        // Defines central equipment of origin
        sourceCodeExt = 0;        // Defines peripheral equipment of origin
        destinationCode = blank;    // Defines central equipment of final destination
        destinationCodeExt = 0;    // Defines peripheral equipment of final destination
        unit = 0;                    // DIP routing ???
        port = 0;                    // DIP routing ???
    }

    protected void doValidateProperties(Properties properties)
            throws MissingPropertyException, InvalidPropertyException {
    }

    public String getFirmwareVersion() throws IOException, UnsupportedException {
        MeteorFirmwareVersion mfv = (MeteorFirmwareVersion) mcf.transmitData(firmwareVersion, null);
        return mfv.getVersion();
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public Date getTime() throws IOException {
        MeteorCLK clk = (MeteorCLK) mcf.transmitData(readRTC, null);
        return clk.getCalendar().getTime();
    }

    public MeteorPowerFailDetails getPowerFailDetails() throws IOException {
        MeteorPowerFailDetails mpfd = (MeteorPowerFailDetails) mcf.transmitData(powerFailDetails, null);
        return mpfd;
    }

    public void setTime() throws IOException {
        // set time is only possible on commissioning or after loading a new personality table (pg 8)
        // => use only trimmer.
        // the value sent to the meter is added on the RTC value in the meter
        long gettime, settime;
        byte result = 0;
        Calendar cal = Calendar.getInstance(timezone);
        Calendar getCal = Calendar.getInstance(timezone);
        getCal.setTime(getTime());
        gettime = getCal.getTimeInMillis();
        settime = cal.getTimeInMillis();
        if (Math.abs(gettime - settime) / 1000 < 59) {
            // max 59 sec deviation
            result = (byte) ((int) ((settime - gettime) / 1000) & 0x000000FF);
        } else {
            result = 59;
            if (gettime > settime) {
                result = -59;
            }
        }
        mcf.trimRTC(result);
    }

    public MeteorFullPersonalityTable getFullPersonalityTable() throws IOException {
        MeteorFullPersonalityTable mfpt = (MeteorFullPersonalityTable) mcf.transmitData(fullPersTableRead, null);
        ;
        return mfpt;
    }

    public MeteorStatus getMeteorStatus() throws IOException {
        MeteorStatus statusreg = null;
        boolean ack = false;
        int pog = this.retry;
        Exception except = new Exception();

        while (!ack && pog > 0) {
            pog--;
            try {
                statusreg = (MeteorStatus) mcf.transmitData(status, null);
                ack = true;
            } catch (Exception e) {
                except = e;
                ack = false;
            }
        }
        if (!ack) {
            throw new IOException(except.getMessage() + ". Error probably caused because no node address " + this.outstationID + " is found");
        }
        System.out.println("status");
        return statusreg;
    }

    public void init(InputStream inputStream, OutputStream outputStream, TimeZone arg2,
                     Logger arg3) throws IOException {
        System.out.println("init");
        // set streams
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        // build command factory
        this.mcf = new MeteorCommunicationsFactory(sourceCode, sourceCodeExt, destinationCode, destinationCodeExt, inputStream, outputStream);
        // set the timeout and retry (set in the properties method)
        this.timezone = arg2;
        mcf.setTimeZone(timezone);
        mcf.setRetries(retry);
        mcf.setTimeOut(timeout);
        //setTime();
    }

    public void connect() throws IOException {
        // full personality table should be downloaded because some of the registers
        // are needed in the communicationsfactory
        ProtocolUtils.delayProtocol(delayAfterConnect);
        statusreg = getMeteorStatus();
        statusreg.printData();
        fullperstable = getFullPersonalityTable();
        fullperstable.printData();
        // set multipliers
        mcf.setMultipliers(fullperstable.getDialexp(), fullperstable.getDialdiv());
        mcf.setNumChan((int) statusreg.getMtrs());
        if (mcf.getNumChan() < channelMap.getNrOfUsedProtocolChannels()) {
            throw new InvalidPropertyException("the meter has less channels available than defined in the properties");
        }
        statusreg.printData();
        // flag the events for the profile

        // channelmap is to be set in the factory
        mcf.setMeterChannelMap(fullperstable.getMeterChannelMap());
        mcf.setChannelMap(this.channelMap);
        // extended personality table contains only meta data and is not implemented
        // reasons not to implement are
        // 1) not needed
        // 2) the format described in the datasheet is not correct
    }

    public void disconnect() throws IOException {
    }

    public Object fetchCache(int arg0) {
        return null;
    }

    public Object getCache() {
        return null;
    }

    public Quantity getMeterReading(int arg0) throws IOException {
        return null;
    }

    public Quantity getMeterReading(String arg0) throws IOException {
        return null;
    }

    public int getNumberOfChannels() throws IOException {
        return channelMap.getNrOfUsedProtocolChannels();  // the meter always has the same number of physical channels
    }

    public ProfileData getProfileData(boolean arg0) throws IOException {
        return null;
    }

    public ProfileData getProfileData(Date fromTime, boolean includeEvents)
            throws IOException {
        Calendar cal = Calendar.getInstance(timezone);
        return getProfileData(fromTime, cal.getTime(), includeEvents);
    }

    public ProfileData getProfileData(Date start, Date stop, boolean arg2) throws IOException {
        long dataInc = 24 * 3600 * 1000;
        boolean firstentry = true;
        Calendar st = Calendar.getInstance(timezone);
        st.setTime(start);
        Calendar stp = Calendar.getInstance(timezone);
        stp.setTime(stop);
        long startint = st.getTimeInMillis();
        long stopint = stp.getTimeInMillis();
        ProfileData pd = new ProfileData();
        ProfileData pdtemp;

        while (startint < stopint) {
            st.setTimeInMillis(startint);
            if (startint + dataInc < stopint) {
                stp.setTimeInMillis(startint + dataInc);
            } else {
                stp.setTimeInMillis(stopint);
            }

            pdtemp = mcf.retrieveProfileData(st.getTime(), stp.getTime(), getProfileInterval(), arg2);

            if (firstentry) {
                firstentry = false;
                for (int i = 0; i < pdtemp.getNumberOfChannels(); i++) {  // deep copy of pdtemp
                    pd.addChannel(pdtemp.getChannel(i));
                }
            }
            for (int i = 0; i < pdtemp.getNumberOfIntervals(); i++) {
                pd.addInterval(pdtemp.getIntervalData(i));
            }
            for (int i = 0; i < pdtemp.getNumberOfEvents(); i++) {
                pd.addEvent(pdtemp.getEvent(i));
            }
            startint += dataInc;
        }
        //pd.getIntervalDatas().get(x)
        if (statusreg.getBatLow() > 0 && arg2) {
            pd.addEvent(new MeterEvent(getTime(), MeterEvent.OTHER, "BATTERY LOW"));
        }
        return pd;
    }

    public int getProfileInterval() throws IOException {
        if (fullperstable == null) {
            fullperstable = getFullPersonalityTable();
        }
        return 60 * fullperstable.getDemper();
    }

    public String getRegister(String arg0) throws IOException {
        throw new UnsupportedException("No registers configured on meter.");
    }

    public void initializeDevice() throws IOException {
    }

    public void release() throws IOException {
    }

    public void setCache(Object arg0) {
    }

    public void setProperties(Properties properties) throws InvalidPropertyException,
            MissingPropertyException {
        try {
            this.outstationID = Integer.parseInt(properties.getProperty("NodeAddress"));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("The node address field has not been filled in");
        }
        this.destinationCode = Parsers.parseCArraytoBArray(Parsers.parseShortToChar((short) outstationID));
        this.channelMap = new ProtocolChannelMap(properties.getProperty("ChannelMap", "1"));
        this.timeout = Integer.parseInt(properties.getProperty("TimeOut", "5000"));
        this.retry = Integer.parseInt(properties.getProperty("Retry", "3"));
        this.delayAfterConnect = Integer.parseInt(properties.getProperty("DelayAfterConnect", "0"));
    }

    public void setRegister(String arg0, String arg1) throws IOException {
    }

    public void updateCache(int arg0, Object arg1) {
    }

    public List<String> getOptionalKeys() {
        List<String> list = new ArrayList<>();
        list.add("TimeOut");
        list.add("Retry");
        list.add("ChannelMap");
        list.add("DelayAfterConnect");
        return list;
    }

    public List<String> getRequiredKeys() {
        return new ArrayList<>();
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys(), this.getPropertySpecService());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys(), this.getPropertySpecService());
    }

    /**
     * ****************************************************************************************
     * R e g i s t e r P r o t o c o l  i n t e r f a c e
     * *****************************************************************************************
     */
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        if (ocm == null) {
            ocm = new ObisCodeMapper(this);
        }
        return ocm.getRegisterValue(obisCode);
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo("");
    }

    public MeteorCommunicationsFactory getMcf() {
        return mcf;
    }

    public int getRetry() {
        return retry;
    }

    public int getTimeout() {
        return timeout;
    }
}
