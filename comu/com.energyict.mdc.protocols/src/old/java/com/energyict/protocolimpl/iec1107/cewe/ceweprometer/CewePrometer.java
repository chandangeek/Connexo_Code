package com.energyict.protocolimpl.iec1107.cewe.ceweprometer;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.ChannelInfo;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.FirmwareVersion;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.iec1107.IEC1107Connection;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.profile.CeweProfile;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.profile.EventParser;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.register.CeweRegisters;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.register.ObisCodeMapper;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.register.ProRegister;
import com.google.inject.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

/**
 * <pre>
 * An overview of the CewePrometer protocol:
 *
 * (*) Networking is handled by IEC1107Connection.
 *
 * (*) Parsing of general/simple data types is handled by ProRegister itself
 *     without any separate parsers.  ProRegister does this, it's no beauty, but
 *     it's short.
 *
 * (*) Two of the more complex types are handled by 2 special parsers.
 *
 *
 * Remarks:
 *
 * (*) Profile data
 * The meter uses the word "logger" for indicating the load profile.  There are
 * 2 separate load profiles in the meter that can have a different interval
 * size.
 *
 * (*) Maximum demands
 * The meter allows identical maximum demand registers!  For instance you
 * could have multiple maximum demand registers logging active import.
 * However they will have the same values!
 *
 * (*) Tou
 * The meter has 8 tou registers.  Every tou register logs a _user defined_
 * phenomenon (= unit + ...).  For example Tou register 1 can be configured
 * to log active import.
 *
 * There are 8 rate registers.  A day is/can be composed of different rate
 * registers.
 *
 * (*) DST transitions
 * SimpleDateFormat _always_ assumes wintertime when it does not know
 * the correct timezone.  This results in problems when there is a
 * "Summer to Winter" time transition. In different words: a transition
 * from Daylight savings time to Standard time.
 *
 * For instance Suppose Timezone WET
 *  -> transition at 1 am
 *  -> WET  = West European (Winter) time (=UTC+0)
 *  -> WEST = West European Summer time   (=UTC+1)
 *
 * MeterTime     Parsed time
 * 0710272330 => Sat Oct 27 22:30:00 GMT 2007 (meter tz= WEST)
 * 0710280000 => Sat Oct 27 23:00:00 GMT 2007 (meter tz= WEST)
 * 0710280030 => Sat Oct 27 23:30:00 GMT 2007 (meter tz= WEST)
 * 0710280100 => Sun Oct 28 01:00:00 GMT 2007 (meter tz= WET but should be WEST)
 * 0710280130 => Sun Oct 28 01:30:00 GMT 2007 (meter tz= WET but should be WEST)
 * 0710280100 => Sun Oct 28 01:00:00 GMT 2007 (meter tz= WEST OK)
 *
 *
 * When fetching starts on a DST interval, that will be parsed as an
 * interval in the Standard time zone ...
 * MeterTime     Parsed time
 * 0710280100 => Sun Oct 28 01:00:00 GMT 2007 (meter tz= WET but should be WEST)
 * 0710280130 => Sun Oct 28 01:30:00 GMT 2007 (meter tz= WET but should be WEST)
 * 0710280100 => Sun Oct 28 01:00:00 GMT 2007 (meter tz= WEST OK)
 *
 * ProtocoWorker prevents this from happening.  If the TO date should fall in
 * this period (gray zone), ProtocolWorker will change the TO date, in order to
 * make the protocol fetch before the changes.
 *
 * When fetching starts after transition, the "default wintertime"
 * parsing is fine:
 * MeterTime     Parsed time
 * 0710280100 => Sun Oct 28 01:00:00 GMT 2007 (meter tz= WEST OK)
 *
 *
 * This problem does not occur for "Winter to Summertime".
 *
 * MeterTime     Parsed time
 * 0703250000 => Sat Oct 27 00:00:00 GMT 2007  (-> WET winter time)
 * 0703250030 => Sat Oct 27 00:30:00 GMT 2007  (-> WET winter time)
 * 0703250200 => Sun Oct 28 01:00:00 GMT 2007  (-> WEST summer time)
 *
 * ATTENTION: this is not an airtight check.  When there is a power
 * down on the DST transition there can be missing intervals.
 *
 * </pre>
 *
 * @author FBL
 * @beginchanges FBL|29012007| initial release
 * FBL|26022007| fix version check bug
 * FBL|26022007| fix scaling & wrap-around bug in profile
 * FBL|27022007| fix profile bug: don't throw away last block of profiledata
 * FBL|03072007| password is no longer a required property, but optional.
 * FBL|12082007| Minor bug fix: registers that are not supported by
 * the protocol result in a NullPointer.  Now the appropriate exception is
 * thrown.
 * FBL|21092007| added supported for unknown event types.
 * FBL|09102007| NullPointer occured when doing timeset with insufficient
 * user rights.
 * @endchanges
 */

public class CewePrometer extends AbstractProtocol  {

    @Override
    public String getProtocolDescription() {
        return "CEWE CEWEPrometer IEC1107";
    }

    /** Property keys specific for CewePrometer protocol. */
    private static final String PK_EXTENDED_LOGGING = "ExtendedLogging";
    private static final String PK_LOGGER = "Logger";

    /** By default the load profile of logger 1 is fetched */
    private static final int PD_LOGGER = 0;
    private static final FirmwareVersion MINIMUM_FW_VERSION = new FirmwareVersion("1.2.0");
    private static final FirmwareVersion NEW_FW_REGISTER = new FirmwareVersion("1.5.0");

    /** property for logger that must be fetched (1 or 2) */
    private int pLogger = PD_LOGGER;

    /** property for extended logging
     * null - 0: off
     * 1: on fetch ALL possible obis codes
     * 2: on fetch obis codes supported by connected meter and values */
    private int pExtendedLogging;

    private IEC1107Connection connection = null;
    private ObisCodeMapper obisCodeMapper = null;
    /** event parser (for event log register)  */
    private EventParser eventParser = null;

    /** TOU-select for every TOU register */
    private int[] touMap = null;
    /** nr of meter channels: */
    private Integer channelCount = null;
    /** channelInfo registers retrieved from  */
    private List<ChannelInfo> channelInfo = null;
    /** timeDiff: time difference (millis) between system and meter */
    private long timeDiff = 0;

    private List<BillingPointIndex> billingPoints = null;

	private boolean software7E1;
    private FirmwareVersion firmwareVersion = null;

    private CeweDateFormats dateFormats = null;
    private CeweRegisters registers = null;

    @Inject
    public CewePrometer(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    /* (non-Javadoc)
        * @see com.energyict.protocolimpl.base.AbstractProtocol#doInit(java.io.InputStream, java.io.OutputStream, int, int, int, int, int, com.energyict.protocolimpl.base.Encryptor, com.energyict.dialer.core.HalfDuplexController)
        */
    protected ProtocolConnection doInit(InputStream in, OutputStream out, int pTimeout, int pRetries, int pForcedDelay, int pEchoCancelling, int pCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {
        try {
            connection = new IEC1107Connection(in, out, pTimeout, pRetries, pForcedDelay, pEchoCancelling, pCompatible, "ER:", software7E1);
            eventParser = new EventParser(this);
            setInfoTypeProtocolRetriesProperty(pRetries);
        } catch (ConnectionException e) {
            getLogger().log(Level.SEVERE, "init failed", e);
            throw new NestedIOException(e);
        }
        return connection;
    }

    /** during connect:
     * 1) check firmware version
     * 2) trigger extended logging
     *
     * The minimum firmware version is 1.2.0.  It is (probably) not difficult
     * to support older meter versions.  But just in case throw an exception.
     *
     * @see com.energyict.protocolimpl.base.AbstractProtocol#doConnect()
     */
    protected void doConnect() throws IOException {

        getFirmwareVersionObject().before(MINIMUM_FW_VERSION);

        int v1 = getRegisters().getrFirmwareVersionOld().asInt(0);
        int v2 = getRegisters().getrFirmwareVersionOld().asInt(1);

        double v = Double.parseDouble( v1 + "." + v2 );

        if( v < 1.2 ) {
            throw new ApplicationException("Meter firmware version " + getFirmwareVersion() + " is not supported.  " + "Minimum version "+MINIMUM_FW_VERSION+".");
        }

        if(pExtendedLogging==1)
            getLogger().info(getObisCodeMapper().toString() );

        if(pExtendedLogging==2)
            getLogger().info(getObisCodeMapper().getExtendedLogging());

    }

    /* (non-Javadoc)
     * @see com.energyict.protocolimpl.base.AbstractProtocol#doDisConnect()
     */
    protected void doDisConnect() throws IOException {
        /* when in doubt, do nothing */
    }

    IEC1107Connection getConnection( ){
        return connection;
    }

    /** @see AbstractProtocol#getRequiredKeys() */
    public List getRequiredKeys() {
        return  new ArrayList();
    }

    /** @see AbstractProtocol#doGetOptionalKeys() */
    protected List doGetOptionalKeys() {
        ArrayList result = new ArrayList();
        result.add( PK_LOGGER );
        result.add( MeterProtocol.PASSWORD );
        result.add("Software7E1");
        return result;
    }

    public ObisCodeMapper getObisCodeMapper() throws IOException {
        if (obisCodeMapper == null) {
            obisCodeMapper = new ObisCodeMapper(this);
        }
        return obisCodeMapper;
    }

    /** @see AbstractProtocol#doValidateProperties(java.util.Properties) */
    protected void doValidateProperties(Properties p) throws MissingPropertyException, InvalidPropertyException {
        String v = p.getProperty(PK_EXTENDED_LOGGING);
        pExtendedLogging = (v == null) ? 0 : Integer.parseInt(v);

        v = p.getProperty(PK_LOGGER);
        pLogger = (v == null) ? PD_LOGGER : Integer.parseInt(v);
        this.software7E1 = !p.getProperty("Software7E1", "0").equalsIgnoreCase("0");

    }

    /** @see AbstractProtocol#getTime() */
    public Date getTime() throws IOException {
        Date d = getRegisters().getrReadDate().readAndFreeze().asDate(getDateFormats().getLongDateFormat());
        timeDiff = System.currentTimeMillis() - d.getTime();
        return d;
    }

    /* timeDiff is written to SlideTime register
     *  @see AbstractProtocol#setTime() */
    public void setTime() throws IOException {

        int deltaSeconds = (int) (timeDiff / 1000) + ((timeDiff % 1000) > 500 ? 1 : 0);
        deltaSeconds = deltaSeconds + getInfoTypeRoundtripCorrection();

        if (Math.abs(deltaSeconds) > 28800) {
            getLogger().severe("Time could not be changed, difference is too big (" + deltaSeconds + "s)");
            return;
        }

        try {
            write(toCmd(getRegisters().getrSlideTime(), "" + deltaSeconds + "," + 40));
        } catch (WriteException e) {
            throw new IOException("Time could not be changed, error occured: \"" + e.getMessage() + "\"");
        }

    }

    /* @see AbstractProtocol#getNumberOfChannels() */
    public int getNumberOfChannels() throws IOException {
        if( channelCount == null ) {
            channelCount = getRegisters().getrLogChannelCount()[pLogger].asInteger();
        }
        return channelCount.intValue();
    }

    /* (non-Javadoc)
     * @see AbstractProtocol#getProtocolVersion()
     */
    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    /** Fetch firware version.
     * @see AbstractProtocol#getFirmwareVersion()
     */
    public FirmwareVersion getFirmwareVersionObject() throws IOException {
        if (firmwareVersion == null) {
            String mayor = getRegisters().getrFirmwareVersionOld().asString(0);
            String minor = getRegisters().getrFirmwareVersionOld().asString(1);
            String subversion = getRegisters().getrFirmwareVersionOld().asString(2);
            firmwareVersion = new FirmwareVersion(mayor + "." + minor + "." + subversion);
            if (firmwareVersion.afterOrEqual(NEW_FW_REGISTER)) {
                mayor = getRegisters().getrFirmwareVersionNew().asString(0);
                minor = getRegisters().getrFirmwareVersionNew().asString(1);
                subversion = getRegisters().getrFirmwareVersionNew().asString(2);
                firmwareVersion = new FirmwareVersion(mayor + "." + minor + "." + subversion);
            }
        }
        return firmwareVersion;
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return getFirmwareVersionObject().getVersionString();
    }

    /* (non-Javadoc)
    * @see AbstractProtocol#validateSerialNumber()
    */
    protected void validateSerialNumber() throws IOException {
        String configured = getInfoTypeSerialNumber();
        String meter = getRegisters().getrSerial().asString();
        if ((configured != null) && !configured.equals(meter)) {
            throw new IOException("SerialNumber mismatch! meter: " + meter + ", configured: " + configured);
        }
    }

    /**
     * Read the profile interval from the device
     *
     * @return
     * @throws IOException
     */
    public int getProfileInterval() throws IOException {
        return getRegisters().getrLogChannelInterval()[pLogger].asInt();
    }

    /** Read profile data in 2 (easy) steps:
     *
     * 1) read the profiledata
     * 2) if needed read event log book
     *
     * @param from
     * @param includeEvents enable or disable tht reading of meterevents
     * @return the profile data object
     * @throws IOException
     */
    public ProfileData getProfileData(Date from, boolean includeEvents) throws IOException {

        /* step 1: fetch and parse profile data */
        CeweProfile profile = new CeweProfile(this);
        ProfileData pd = profile.readProfileData(from);

        /* step 2: fetch logbook */
        if( includeEvents ) {
            boolean eof;
            write(toCmd(getRegisters().getrEventLogReadOffset(), getDateFormats().getQueryDateFormat().format(from)));
            do {
                String result = getRegisters().getrEventLogNextEvent().getRawData();
                eof = result.indexOf("(EOF)") != -1;
                if( !eof ) pd.addEvent( eventParser.parse(result) );
            } while( !eof );
        }

        pd.applyEvents(getProfileInterval()/60);
        return pd;
    }

    /** (non-Javadoc)
     * @see com.energyict.protocolimpl.base.AbstractProtocol#translateRegister(ObisCode)
     */
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new ObisCodeMapper(this).getRegisterInfo(obisCode);
    }

    /** (non-Javadoc)
     * @see com.energyict.protocolimpl.base.AbstractProtocol#readRegister(ObisCode)
     */
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return getObisCodeMapper().getRegisterValue(obisCode);
    }

    /** Create a meter command in ByteArray form
     * @param register to read
     * @param arg arguments
     */
    public String toCmd(ProRegister register, String arg){
        return new String( register.getId() ) + "(" + arg + ")";
    }

    /** send read command */
    String read(String cmd) throws IOException {
        return read(cmd, true);
    }

    /** send read command */
    public String read(String cmd, boolean retry) throws IOException {
        connection.sendRawCommandFrame(IEC1107Connection.READ1, cmd.getBytes());
        byte[] rawData = retry ? connection.receiveRawData() : connection.doReceiveData();
        String response = new String(rawData);

        if( ( null!=response ) && ( response.indexOf( "ER" ) != -1 ) ) {
            String id = response.substring(4,7);
            throw new WriteException( getExceptionInfo(id) );
        }

        return response;
    }


    /** send write command
     *
     * The CewePrometer returns and Error code when a write command fails.
     * This is a good/valid response according to the IEC62056-21 spec.  However
     * the IEC1107Connection class does not excpect/handle an ERROR code after a
     * write command.
     *
     * This is why this kind of error checking is introduced in this write
     * method. The error handling/checking could be done in the
     * IEC1107Connection class, which would be more general/reusable/correct.
     *
     * But it could potentially introduce errors in other IEC1107 protocols.
     * Hence CewePrometer does the checking for his own.
     * fbl 09/10/2007
     *
     * */
    public void write(String cmd) throws IOException {
        byte [] iecCmd = IEC1107Connection.WRITE1;
        byte [] b = cmd.getBytes();
        String r = connection.sendRawCommandFrameAndReturn(iecCmd, b);

        if( ( null!=r ) && ( r.indexOf( "ER" ) != -1 ) ) {
            String id = r.substring(4,7);
            throw new WriteException( getExceptionInfo(id) );
        }
    }

    public CeweDateFormats getDateFormats() {
        if (dateFormats == null) {
            dateFormats = new CeweDateFormats(getTimeZone());
        }
        return dateFormats;
    }

    public CeweRegisters getRegisters() {
        if (registers == null) {
            registers = new CeweRegisters(this);
        }
        return registers;
    }

    public int getPLogger() {
        return pLogger;
    }

    public int getRetries() {
        return getInfoTypeRetries();
    }

    private class BillingPointIndex implements Comparable {
        int index;
        Date date;
        BillingPointIndex(int index, ProRegister register) throws IOException{
            this.index = index;
            this.date = register.asDate(getDateFormats().getLongDateFormat());
        }
        public int compareTo(Object o) {
            BillingPointIndex bpi = (BillingPointIndex)o;
            return date.compareTo(bpi.date);
        }
        public String toString(){
            return "BillingPointIndex[" + index + ", " + date + "]";
        }
    }

    /** Translate a billing point into a Register Id.
     *
     * For example, a meter contains following "Historical period time stamps"
     *
     * -> Mon Jan 01 00:00:00 CET 2007
     * -> Thu Dec 14 16:40:57 CET 2006
     * -> Sat Jan 01 00:00:02 CET 2000
     * -> Fri Jun 03 09:04:32 CEST 2005
     * -> Sat Jan 01 00:00:02 CET 2000
     * -> Thu Apr 01 14:01:04 CEST 2004
     * -> Thu Jan 01 00:00:00 CET 1970
     * -> Thu Jan 01 00:00:00 CET 1970
     * -> Thu Jan 01 00:00:00 CET 1970
     * -> Thu Jan 01 00:00:00 CET 1970
     * -> Thu Jan 01 00:00:00 CET 1970
     * -> Thu Jan 01 00:00:00 CET 1970
     * -> Thu Jan 01 00:00:00 CET 1970
     * -> Thu Jan 01 00:00:00 CET 1970
     *
     * "Thu Jan 01 00:00:00 CET 1970" actually means 'no billing point' so all
     * values with this date need to be omitted.
     *
     * "Sat Jan 01 00:00:02 CET 2000" occurs twice, since we can not know
     * which billing point is the older or newer one, we just sort them like
     * the other billing points.
     *
     * The resulting billing point list looks like this.
     *                                                            idx    bp
     * -> BillingPointIndex[1, Mon Jan 01 00:00:00 CET 2007]    -> 0  -> VZ
     * -> BillingPointIndex[2, Thu Dec 14 16:40:57 CET 2006]    -> 1  -> VZ-1
     * -> BillingPointIndex[4, Fri Jun 03 09:04:32 CEST 2005]   -> 2  -> VZ-2
     * -> BillingPointIndex[6, Thu Apr 01 14:01:04 CEST 2004]   -> 3  -> VZ-3
     * -> BillingPointIndex[3, Sat Jan 01 00:00:02 CET 2000]    -> 4  -> VZ-4
     * -> BillingPointIndex[5, Sat Jan 01 00:00:02 CET 2000]    -> 5  -> VZ-5
     *
     * @param billingPoint to translate/map
     * @return Register Id of the billing point
     * @throws IOException
     */
    public int getRow(int billingPoint) throws IOException {

        /* no need to fetch any historical registers, just return 0 */
        if(billingPoint==255) return 0;

        /* lazily init the billing points collection */
        if(billingPoints==null){
            billingPoints = new ArrayList();
            for (int i = 1; i < getRegisters().getrTimestamp().length; i++) {
                if( ! getRegisters().getrTimestamp()[i].isNullDate() ) {
                    BillingPointIndex bpi = new BillingPointIndex(i, getRegisters().getrTimestamp()[i]);
                    billingPoints.add(bpi);
                }
            }
            Collections.sort(billingPoints, Collections.reverseOrder());
        }

        int abs = Math.abs(billingPoint);
        if( abs < billingPoints.size()   )
            return billingPoints.get(abs).index;

        /* meter does not have data for abs nr of billing points */
        return -1;

    }

    /**
     * @param   source
     * @return  index of source
     *          -1 if no TOU register configured for source
     * @throws  IOException
     */
    public int getTouIndex(int source) throws IOException {
        if( touMap == null ) { // fetch
            touMap = new int[] { -1, -1, -1, -1, -1, -1, -1, -1 };
            String t = getRegisters().getrTouRegisterSelect().asString();
            int ti = 0;
            for(int idx = 0; idx < 16; idx=idx+2 ){
                String v = t.substring(idx, idx+2);
                touMap[ti] = Integer.parseInt(v, 16);
                ti = ti +1;
            }
        }
        for(int i = 0; i < touMap.length; i++){
            if(touMap[i]==source) return i;
        }
        return -1;  // not found
    }

    /** method for displaying TOU-select register */
    private String touToString( ){
        StringBuffer r = new StringBuffer();

        for(int i = 0; i<touMap.length; i++ ){
            switch(touMap[i]) {
                case 0x00: r.append( "active energy imp.    \n" ); break;
                case 0x01: r.append( "active energy exp.    \n" ); break;
                case 0x02: r.append( "reactive energy imp.  \n" ); break;
                case 0x03: r.append( "reactive energy exp.  \n" ); break;
                case 0x04: r.append( "reactive energy ind.  \n" ); break;
                case 0x05: r.append( "reactive energy cap.  \n" ); break;
                case 0x06: r.append( "reactive energy QI    \n" ); break;
                case 0x07: r.append( "reactive energy QII   \n" ); break;
                case 0x08: r.append( "reactive energy QIII  \n" ); break;
                case 0x09: r.append( "reactive energy QIV   \n" ); break;
                case 0x0A: r.append( "apparent energy imp.  \n" ); break;
                case 0x0B: r.append( "apparent energy exp.  \n" ); break;
                default: r.append( "unknown: " + touMap[i] +  "\n" );
            }
        }

        return r.toString();
    }


    public String getExceptionInfo(String id) {
        return CeweExceptionInfo.getExceptionInfo(id);
    }

}
