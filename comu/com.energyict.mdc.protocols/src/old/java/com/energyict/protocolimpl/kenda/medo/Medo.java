package com.energyict.protocolimpl.kenda.medo;

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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * ---------------------------------------------------------------------------------<p>
 * Medo Protocol description:<p>
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
 * 3) The MedoCommunicationsFactory deals with all the communication issues.  It
 * arranges the data flows and communication.  It starts with sending the command
 * and getting back the right object to parse the data in.  Also the profileData
 * factory is implemented in this class.
 * <p/>
 * 4) This class: Medo.java masters and is the interface from the server
 * <p/>
 * Additional classes are implemented mostly to help in the Unit testing.
 * <p/>
 * no information on STPERIOD in code 7 can be found, therefore 2 possible implementations
 * are made, use the properties profileDataPointer to give the starting year of counting
 * to the protocol.
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
 * Author: Peter Staelens, ITelegance (peter@Itelegance.com or P.Staelens@EnergyICT.com)<p>
 * Version:1.01<p>
 * First edit date: 26/08/2008<p>
 * Last edit date: 29/08/2008<p>
 * Comments: Problems in the profile data read solved. In the MedoRequestReadMeterDemands
 * on line 23-29 there will be an adaptation, stperiod number is not yet completely reverse
 * engineered (december last year problem) a flag in the properties has been added<p>
 * released for testing: 29/08/2008<p>
 *
 * @author Peter Staelens, ITelegance (peter@Itelegance.com or P.Staelens@EnergyICT.com)<p>
 * @version 1.02 <p>
 * First edit date: 29/08/2008 PST<p>
 * Last edit date: 1/09/2008  PST<p>
 * Comments: Beta+ ready for testing<p>
 * Released for testing: 1/09/2008<p>
 */
public class Medo extends PluggableMeterProtocol implements RegisterProtocol {

    @Override
    public String getProtocolDescription() {
        return "Kenda Medeo";
    }

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
    private static final byte fullPersTableRead = 0x01;      // ram initialised, table defines modes
    private static final byte readRTC = 0x03;
    private static final byte firmwareVersion = 0x04;
    private static final byte status = 0x05;
    private static final byte powerFailDetails = 0x0C;
    // first three bits are to be set in BuildIdent method (later)
    // byte: 8 bit, word 16 bit signed integer, long 32 bit signed integer

    @Inject
    public Medo(PropertySpecService propertySpecService) {
        super(propertySpecService);
        byte[] blank = {0, 0};
        sourceCode = blank;        // Defines central equipment of origin
        sourceCodeExt = 0;        // Defines peripheral equipment of origin
        destinationCode = blank;    // Defines central equipment of final destination
        destinationCodeExt = 0;    // Defines peripheral equipment of final destination
    }

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
    }

    public String getFirmwareVersion() throws IOException {
        MedoFirmwareVersion mfv = (MedoFirmwareVersion) mcf.transmitData(firmwareVersion, null);
        return mfv.getVersion();
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

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

    public MedoPowerFailDetails getPowerFailDetails() throws IOException {
        return (MedoPowerFailDetails) mcf.transmitData(powerFailDetails, null);
    }

    public void setTime() throws IOException {
        // set time is only possible on commissioning or after loading a new personality table (pg 8)
        // use only trimmer.
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

    public MedoFullPersonalityTable getFullPersonalityTable() throws IOException {
        MedoFullPersonalityTable mfpt = (MedoFullPersonalityTable) mcf.transmitData(fullPersTableRead, null);
        ;
        return mfpt;
    }

    public MedoStatus getmedoStatus() throws IOException {
        MedoStatus statusreg;
        try {
            statusreg = (MedoStatus) mcf.transmitData(status, null);
        } catch (IOException e) {
            throw new IOException(e.getMessage() + ". Interframe timeout probably caused because no node address " + this.outstationID + " is found");
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException(e.getMessage() + ". Error probably caused because no node address " + this.outstationID + " is found");
        }
        return statusreg;
    }

    public void init(InputStream inputStream, OutputStream outputStream, TimeZone arg2,
                     Logger arg3) throws IOException {
        // set streams
        this.timezone = arg2;
        // build command factory
        this.mcf = new MedoCommunicationsFactory(sourceCode, sourceCodeExt, destinationCode, destinationCodeExt, inputStream, outputStream);
        mcf.setRetries(retry);
        mcf.setTimeOut(timeout);
        mcf.setTimeZone(timezone);
    }

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

    public void disconnect() throws IOException {
    }

    public Object fetchCache(int arg0) {
        return null;
    }

    public Object getCache() {
        return null;
    }

    public Quantity getMeterReading(int arg0) throws
            IOException {
        return null;
    }

    public Quantity getMeterReading(String arg0) throws
            IOException {
        return null;
    }

    public int getNumberOfChannels() throws IOException {
        return channelMap.getNrOfUsedProtocolChannels();
    }

    public ProfileData getProfileData(boolean arg0) throws IOException {
        return null;
    }

    public ProfileData getProfileData(Date fromTime, boolean includeEvents)
            throws IOException {
        Calendar cal = Calendar.getInstance(timezone);
        return getProfileData(fromTime, cal.getTime(), includeEvents);
    }

    public ProfileData getProfileData(Date start, Date stop, boolean arg2)
            throws IOException {

        ProfileData pd = mcf.retrieveProfileData(start, stop, getProfileInterval(), arg2);
        if (statusreg.getBatlow() > 0 && arg2) {
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

    public void setProperties(Properties properties) throws InvalidPropertyException {
        try {
            this.outstationID = Integer.parseInt(properties.getProperty("NodeAddress"));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("The node address field has not been filled in");
        }
        this.destinationCode = Parsers.parseCArraytoBArray(Parsers.parseShortToChar((short) outstationID));
        this.channelMap = new ProtocolChannelMap(properties.getProperty("ChannelMap", "1"));
        this.timeout = Integer.parseInt(properties.getProperty("TimeOut", "10000"));
        this.retry = Integer.parseInt(properties.getProperty("Retry", "3"));
        this.delayAfterConnect = Integer.parseInt(properties.getProperty("DelayAfterConnect", "500"));
    }

    public void setRegister(String arg0, String arg1) throws IOException {
    }

    public void updateCache(int arg0, Object arg1) {
    }

    public List<String> getOptionalKeys() {
        return Arrays.asList(
                    "TimeOut",
                    "Retry",
                    "DelayAfterConnect");
    }

    public List<String> getRequiredKeys() {
        return Collections.emptyList();
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

    public MedoCommunicationsFactory getMcf() {
        return mcf;
    }

    public int getRetry() {
        return retry;
    }

    public int getTimeout() {
        return timeout;
    }

}