package com.energyict.protocolimpl.kenda.meteor;

import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Quantity;
import com.energyict.dialer.core.DialerCarrierException;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
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
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

public class Meteor extends PluggableMeterProtocol implements RegisterProtocol {

    private int DEBUG = 0;
    private MeteorCommunicationsFactory mcf;
    private int outstationID, retry, timeout, delayAfterConnect;

    private byte[] sourceCode;        // Defines central equipment of origin
    private byte sourceCodeExt;        // Defines peripheral equipment of origin
    private byte[] destinationCode;    // Defines central equipment of final destination
    private byte destinationCodeExt;// Defines peripheral equipment of final destination

    // data objects
    private MeteorFullPersonalityTable fullperstable = null;
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

    private final PropertySpecService propertySpecService;

    public Meteor(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;// blank constructor for testing purposes only
        byte[] blank = {0, 0};
        sourceCode = blank;        // Defines central equipment of origin
        sourceCodeExt = 0;        // Defines peripheral equipment of origin
        destinationCode = blank;    // Defines central equipment of final destination
        destinationCodeExt = 0;    // Defines peripheral equipment of final destination
    }

    public Meteor(  // real constructor, sets header correct.
            byte[] sourceCode,
            byte sourceCodeExt,
            byte[] destinationCode,
            byte destinationCodeExt,
            PropertySpecService propertySpecService) {
        this.sourceCode = sourceCode;
        this.sourceCodeExt = sourceCodeExt;
        this.destinationCode = destinationCode;
        this.destinationCodeExt = destinationCodeExt;
        this.propertySpecService = propertySpecService;
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        MeteorFirmwareVersion mfv = (MeteorFirmwareVersion) mcf.transmitData(firmwareVersion, null);
        return mfv.getVersion();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    @Override
    public Date getTime() throws IOException {
        MeteorCLK clk = (MeteorCLK) mcf.transmitData(readRTC, null);
        return clk.getCalendar().getTime();
    }

    @Override
    public void setTime() throws IOException {
        // set time is only possible on commissioning or after loading a new personality table (pg 8)
        // => use only trimmer.
        // the value sent to the meter is added on the RTC value in the meter
        long gettime, settime;
        byte result;
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

    private MeteorFullPersonalityTable getFullPersonalityTable() throws IOException {
        return (MeteorFullPersonalityTable) mcf.transmitData(fullPersTableRead, null);
    }

    private MeteorStatus getMeteorStatus() throws IOException {
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
                if (e instanceof DialerCarrierException) {
                    throw new IOException("NO CARRIER received");
                }
            }
        }
        if (!ack) {
            throw new IOException(except.getMessage() + ". Error probably caused because no node address " + this.outstationID + " is found");
        }
        System.out.println("status");
        return statusreg;
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone arg2,
                     Logger arg3) throws IOException {
        System.out.println("init");
        // set streams
        /*
      ---------------------------------------------------------------------------------<p>
      Meteor Protocol description:<p>
      The protocol consists out of layers<p>
      <p/>
      1) The deepest layer is the Parsers layer.  Part of the layer has been made
      static and made abstract.  The second layer extends this class.
      <p/>
      2) All registers are implemented in classes. The classes always implement the
      type of methods: process to parse (deserializes) the byte array in the
      object variables,printData visualizes the data matrix (parsed) in the console
      and parseToByteArray serializes the object.
      <p/>
      3) The MeteorCommunicationsFactory deals with all the communication issues.  It
      arranges the data flows and communication.  It starts with sending the command
      and getting back the right object to parse the data in.  Also the profileData
      factory is implemented in this class.
      <p/>
      4) This class: Meteor.java masters and is the interface from the server
      <p/>
      Additional classes are implemented mostly to help in the Unit testing.
      <p/>
      Initial version:<p>
      ----------------<p>
      Author: Peter Staelens, ITelegance (peter@Itelegance.com or P.Staelens@EnergyICT.com)<p>
      Version: 1.0 <p>
      First edit date: 1/07/2008 PST<p>
      Last edit date: 13/08/2008  PST<p>
      Comments: Beta ready for testing<p>
      Released for testing: 13/08/2008<p>
      <p/>
      Revisions<p>
      ----------------<p>

      */
        // build command factory
        this.mcf = new MeteorCommunicationsFactory(sourceCode, sourceCodeExt, destinationCode, destinationCodeExt, inputStream, outputStream);
        // set the timeout and retry (set in the properties method)
        this.timezone = arg2;
        mcf.setTimeZone(timezone);
        mcf.setRetries(retry);
        mcf.setTimeOut(timeout);
        //setTime();
    }

    @Override
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

    @Override
    public void disconnect() throws IOException {
    }

    @Override
    public Quantity getMeterReading(int arg0) throws IOException {
        return null;
    }

    @Override
    public Quantity getMeterReading(String arg0) throws IOException {
        return null;
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return channelMap.getNrOfUsedProtocolChannels();  // the meter always has the same number of physical channels
    }

    @Override
    public ProfileData getProfileData(boolean arg0) throws IOException {
        return null;
    }

    @Override
    public ProfileData getProfileData(Date fromTime, boolean includeEvents) throws IOException {
        Calendar cal = Calendar.getInstance(timezone);
        return getProfileData(fromTime, cal.getTime(), includeEvents);
    }

    @Override
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
        ProfileData pdtemp = new ProfileData();

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

    @Override
    public int getProfileInterval() throws IOException {
        if (fullperstable == null) {
            fullperstable = getFullPersonalityTable();
        }
        return 60 * fullperstable.getDemper();
    }

    @Override
    public String getRegister(String arg0) throws IOException {
        throw new UnsupportedException("No registers configured on meter.");
    }

    @Override
    public void initializeDevice() throws IOException {
    }

    @Override
    public void release() throws IOException {
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.integerSpec(NODEID.getName()),
                this.integerSpec(TIMEOUT.getName()),
                this.integerSpec("Retry"),
                this.integerSpec("DelayAfterConnect"),
                ProtocolChannelMap.propertySpec("ChannelMap", false));
    }

    private <T> PropertySpec spec(String name, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, optionsSupplier).finish();
    }

    private PropertySpec integerSpec(String name) {
        return this.spec(name, this.propertySpecService::integerSpec);
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws InvalidPropertyException, MissingPropertyException {
        try {
            this.outstationID = Integer.parseInt(properties.getTypedProperty(NODEID.getName()));
            this.destinationCode = Parsers.parseCArraytoBArray(Parsers.parseShortToChar((short) outstationID));
            this.channelMap = new ProtocolChannelMap(properties.getTypedProperty("ChannelMap", "1"));
            this.timeout = Integer.parseInt(properties.getTypedProperty(TIMEOUT.getName(), "5000"));
            this.retry = Integer.parseInt(properties.getTypedProperty("Retry", "3"));
            this.delayAfterConnect = Integer.parseInt(properties.getTypedProperty("DelayAfterConnect", "0"));
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before");
        }
    }

    @Override
    public void setRegister(String arg0, String arg1) throws IOException {
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        if (ocm == null) {
            ocm = new ObisCodeMapper(this);
        }
        return ocm.getRegisterValue(obisCode);
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo("");
    }

    MeteorCommunicationsFactory getMcf() {
        return mcf;
    }

    public int getRetry() {
        return retry;
    }

    public int getTimeout() {
        return timeout;
    }

}