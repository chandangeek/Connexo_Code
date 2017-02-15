package com.energyict.protocolimpl.kenda.medo;

import com.energyict.cbo.Quantity;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.nls.TranslationKey;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;

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

public class Medo extends PluggableMeterProtocol implements RegisterProtocol {

    private MedoCommunicationsFactory mcf;
    private int outstationID, retry, timeout, delayAfterConnect;

    private byte[] sourceCode;        // Defines central equipment of origin
    private byte sourceCodeExt;        // Defines peripheral equipment of origin
    private byte[] destinationCode;    // Defines central equipment of final destination
    private byte destinationCodeExt;// Defines peripheral equipment of final destination

    // data objects
    private MedoFullPersonalityTable fullperstable = null;
    private MedoStatus statusreg = null;
    private ObisCodeMapper ocm;
    private ProtocolChannelMap channelMap;
    private TimeZone timezone;

    // used for debugging timerequest problem (asking 2 times the time in 1 call is impossible)
    private boolean timeRequest = false;
    private int timeoffset = 0;

    // ident byte
    // Ack bit, first block bit, last block bit, R/W, 4 bit operation select

    // operation select codes, lowest 5 bit of ident
    private static final byte RESERVED = 0x00;          // internal DIP/Central System function
    private static final byte fullPersTableRead = 0x01;      // ram initialised, table defines modes
    private static final byte fullPersTableWrite = 0x11;    // ... page 6/16 medo communications protocol
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
    private final NlsService nlsService;

    public Medo(PropertySpecService propertySpecService, NlsService nlsService) {// blank constructor for testing purposes only
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        byte[] blank = {0, 0};
        sourceCode = blank;        // Defines central equipment of origin
        sourceCodeExt = 0;        // Defines peripheral equipment of origin
        destinationCode = blank;    // Defines central equipment of final destination
        destinationCodeExt = 0;    // Defines peripheral equipment of final destination
    }

    public Medo(  // real constructor, sets header correct.
            byte[] sourceCode,
            byte sourceCodeExt,
            byte[] destinationCode,
            byte destinationCodeExt,
            PropertySpecService propertySpecService,
                  NlsService nlsService) {
        this.sourceCode = sourceCode;
        this.sourceCodeExt = sourceCodeExt;
        this.destinationCode = destinationCode;
        this.destinationCodeExt = destinationCodeExt;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        MedoFirmwareVersion mfv = (MedoFirmwareVersion) mcf.transmitData(firmwareVersion, null);
        return mfv.getVersion();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:26:01 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public Date getTime() throws IOException {
        if (!timeRequest) {
            MedoCLK clk = (MedoCLK) mcf.transmitData(readRTC, null);
            Calendar c = Calendar.getInstance(timezone);
            timeoffset = (int) (clk.getCalendar().getTimeInMillis() - c.getTimeInMillis());
            timeRequest = true;
            return clk.getCalendar().getTime();
        } else {
            Calendar c = Calendar.getInstance(timezone);
            long temp = c.getTimeInMillis() + timeoffset;
            c.setTimeInMillis(temp);
            return c.getTime();
        }
    }

    @Override
    public void setTime() throws IOException {
        // set time is only possible on commissioning or after loading a new personality table (pg 8)
        // use only trimmer.
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

    private MedoFullPersonalityTable getFullPersonalityTable() throws IOException {
        MedoFullPersonalityTable mfpt = (MedoFullPersonalityTable) mcf.transmitData(fullPersTableRead, null);
        return mfpt;
    }

    private MedoStatus getmedoStatus() throws IOException {
        try {
            return (MedoStatus) mcf.transmitData(status, null);
        } catch (IOException e) {
            throw new IOException(e.getMessage() + ". Interframe timeout probably caused because no node address " + this.outstationID + " is found");
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException(e.getMessage() + ". Error probably caused because no node address " + this.outstationID + " is found");
        }
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone arg2, Logger arg3) throws IOException {
        // set streams
        this.timezone = arg2;
        // build command factory
        this.mcf = new MedoCommunicationsFactory(sourceCode, sourceCodeExt, destinationCode, destinationCodeExt, inputStream, outputStream);
        mcf.setRetries(retry);
        mcf.setTimeOut(timeout);
        mcf.setTimeZone(timezone);
    }

    @Override
    public void connect() throws IOException {
        ProtocolUtils.delayProtocol(delayAfterConnect);
        statusreg = getmedoStatus();
        // getTime(); // this can be uncommented if the command sequence appears to time out.
        fullperstable = getFullPersonalityTable();
        // set multipliers
        mcf.setMultipliers(fullperstable.getDialexp(), fullperstable.getDialmlt());
        mcf.setNumChan((int) statusreg.getMtrs());
        statusreg.printData();
        mcf.setNumChan((int) statusreg.getMtrs());
        if (mcf.getNumChan() < channelMap.getNrOfUsedProtocolChannels()) {
            throw new InvalidPropertyException("the meter has less channels available than defined in the properties");
        }
        // channelmap is to be set in the factory
        mcf.setMeterChannelMap(fullperstable.getMeterChannelMap());
        mcf.setChannelMap(this.channelMap);
    }

    @Override
    public void disconnect() throws IOException {
    }

    @Override
    public Quantity getMeterReading(int arg0) {
        return null;
    }

    @Override
    public Quantity getMeterReading(String arg0) {
        return null;
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return channelMap.getNrOfUsedProtocolChannels();
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
        ProfileData pd = mcf.retrieveProfileData(start, stop, getProfileInterval(), arg2);
        if (statusreg.getBatlow() > 0 && arg2) {
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
    public String getRegister(String arg0) throws UnsupportedException {
        throw new UnsupportedException("No registers configured on meter.");
    }

    @Override
    public void initializeDevice() {
    }

    @Override
    public void release() {
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.stringSpec(NODEID.getName(), PropertyTranslationKeys.KENDA_NODEID),
                ProtocolChannelMap.propertySpec("ChannelMap", false, this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.KENDA_CHANNEL_MAP).format(), this.nlsService.getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.KENDA_CHANNEL_MAP_DESCRIPTION).format()),
                this.integerSpec(TIMEOUT.getName(), PropertyTranslationKeys.KENDA_TIMEOUT),
                this.integerSpec("Retry", PropertyTranslationKeys.KENDA_RETRY),
                this.integerSpec("DelayAfterConnect", PropertyTranslationKeys.KENDA_DELAY_AFTER_CONNECT));
    }

    private <T> PropertySpec spec(String name, TranslationKey translationKey, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, translationKey, optionsSupplier).finish();
    }

    private PropertySpec stringSpec(String name, TranslationKey translationKey) {
        return this.spec(name, translationKey, this.propertySpecService::stringSpec);
    }

    private PropertySpec integerSpec(String name, TranslationKey translationKey) {
        return this.spec(name, translationKey, this.propertySpecService::integerSpec);
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws InvalidPropertyException {
        try {
            this.outstationID = Integer.parseInt(properties.getTypedProperty(NODEID.getName()));
            this.destinationCode = Parsers.parseCArraytoBArray(Parsers.parseShortToChar((short) outstationID));
            this.channelMap = new ProtocolChannelMap(properties.getTypedProperty("ChannelMap", "1"));
            this.timeout = Integer.parseInt(properties.getTypedProperty(TIMEOUT.getName(), "10000"));
            this.retry = Integer.parseInt(properties.getTypedProperty("Retry", "3"));
            this.delayAfterConnect = Integer.parseInt(properties.getTypedProperty("DelayAfterConnect", "500"));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("The node address field has not been filled in");
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

    MedoCommunicationsFactory getMcf() {
        return mcf;
    }

    public int getRetry() {
        return retry;
    }

    public int getTimeout() {
        return timeout;
    }

}