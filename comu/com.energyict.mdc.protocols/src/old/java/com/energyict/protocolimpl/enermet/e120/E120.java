package com.energyict.protocolimpl.enermet.e120;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterProtocol;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexController;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.base.ProtocolConnection;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * A 100 Mile high overview of the E120 protocol:
 *
 * (*) Connection       : Entry point for communication
 *
 * (*) Message          : Application layer request or response
 * (*) Packet           : Network layer request or response
 * (*) Frame            : Datalink layer request or response
 *
 * (*) MessageType      : Application layer method (f.g. seriesBasedOnTime)
 *                        Linking of MessageType and message parsers
 *
 * (*) MessageBody      : Markup interface for Message content
 * (*) Request          : Request to meter (Application layer)
 * (*) Response         : Response from meter
 * (*) DefaultResponse  : Response of a single value
 * (*) SeriesResponse   : Response of list of values
 *
 * (*) NackCode         : Application layer status (Message status)
 * (*) StatCode         : Network layer status (Packet status)
 *
 * (*) UnitMap          : Maps between E120 unit codes and EIS Units
 * (*) DataType         : Composite datatype parser for application data
 *
 * @author fbo
 *
 * @beginchanges
   FBL|15012006| initial release
 * @endchanges
 */

public class E120 extends AbstractProtocol implements RegisterProtocol {

    @Override
    public String getProtocolDescription() {
        return "Enermet E120 ODEP";
    }

    /** Maximum nr of intervals that can be fetched in 1 SeriesOnCount request */
    private static final int FETCH_LIMIT = 306;

    static final MessageFormat ERROR_0 = new MessageFormat(
            "Configured profile interval size differs from requested size " +
            "(configured={0}s, meter={1}s)" );
    static final MessageFormat ERROR_1 = new MessageFormat(
            "Found different nr of entries in: \n {0} \n {1} ");

    /** Property Default values */
    static final int PD_RETRIES = 5;
    /** Property default for the channel configuration */
    static final String PD_CHANNEL_MAP = "1.29+10:0:0:0";

    /** Property keys specific for E120 protocol. */
    static final String PK_TIMEOUT = "Timeout";
    static final String PK_RETRIES = "Retries";
    static final String PK_EXTENDED_LOGGING = "ExtendedLogging";
    static final String PK_USER_ID = "userId";
    static final String PK_PASSWORD = "password";
    static final String PK_CHANNEL_MAP = "ChannelMap";

    private String pUserId;
    private String pPassword;
    private int pExtendedLogging;
    private ProtocolChannelMap pChannelMap;

    private Connection connection;
    private ObisCodeMapper obisCodeMapper;
    private DataType dataType;

    private int pRetries;

    @Inject
    public E120(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    protected ProtocolConnection doInit(
            InputStream inputStream, OutputStream outputStream,
            int timeoutProperty, int retries, int forcedDelay,
            int echoCancelling, int protocolCompatible, Encryptor encryptor,
            HalfDuplexController halfDuplexController) throws IOException {

        pRetries = retries;

        if (getLogger().isLoggable(Level.INFO)) {
            String infoMsg =
                "E120 protocol init \n"
                    + " UserId = " + pUserId + ","
                    + " Password = " + pPassword + ","
                    + " Retries = " + pRetries + ","
                    + " Ext. Logging = " + pExtendedLogging + ","
                    + " TimeZone = " + getTimeZone().getID();

            getLogger().info(infoMsg);

        }

        connection = new Connection(this, inputStream, outputStream );

        obisCodeMapper = new ObisCodeMapper(this);
        initDataType(getTimeZone());

        return connection;

    }

    void initDataType(TimeZone timeZone){
        this.dataType = new DataType(timeZone);
    }

    /* (non-Javadoc)
     * @see com.energyict.protocolimpl.base.AbstractProtocol#doConnect()
     */
    protected void doConnect() throws IOException {

        if( !connection.connect(pUserId, pPassword).isOk() )
            throw new IOException("connect failed");

        if(pExtendedLogging==1)
            getLogger().info(obisCodeMapper.toString());

        if(pExtendedLogging==2)
            getLogger().info(obisCodeMapper.getExtendedLogging());

    }

    /* (non-Javadoc)
     * @see com.energyict.protocolimpl.base.AbstractProtocol#doDisConnect()
     */
    protected void doDisConnect() throws IOException {
        /* do nothing */
    }

    public List getRequiredKeys() {
        List result = new ArrayList(0);
        result.add( PK_USER_ID );
        return result;
    }

    protected List doGetOptionalKeys() {
        return new ArrayList(){
            {
                add( MeterProtocol.PASSWORD );
                add( PK_TIMEOUT );
                add( PK_RETRIES );
                add( PK_EXTENDED_LOGGING );
                add( PK_CHANNEL_MAP );
            }
        };
    }

    protected void doValidateProperties(Properties p)
        throws MissingPropertyException, InvalidPropertyException {

        if(p.getProperty(PK_USER_ID)==null) {
            String msg = PK_USER_ID + " is required. ";
            throw new MissingPropertyException(msg);
        }
        pUserId = p.getProperty(PK_USER_ID);

        pPassword = p.getProperty(PK_PASSWORD);

        if( !propertyExists(p, PK_CHANNEL_MAP) ){
            String msg = PK_CHANNEL_MAP + " is required. ";
            throw new MissingPropertyException(msg);
        }
        pChannelMap = new ProtocolChannelMap( p.getProperty(PK_CHANNEL_MAP) );

        if( propertyExists(p, PK_RETRIES) )
            pRetries = Integer.parseInt(p.getProperty(PK_RETRIES));

        if( propertyExists(p, PK_EXTENDED_LOGGING) )
            pExtendedLogging = Integer.parseInt(p.getProperty(PK_EXTENDED_LOGGING));

    }

    private boolean propertyExists(Properties p, String key){
        String aProperty = p.getProperty(key);
        return (  aProperty != null ) && !"".equals(aProperty);
    }

    int getRetries( ){
        return pRetries;
    }

    public Date getTime() throws IOException {
        Message mr = connection.send(MessageType.GET_TIME);
        return (Date)((DefaultResponse)mr.getBody()).getValue();
    }

    public void setTime() throws IOException {

        Calendar c = Calendar.getInstance(getTimeZone());
        c.add(Calendar.MILLISECOND, getInfoTypeRoundtripCorrection());
        ByteArray value = dataType.getTime().construct(c.getTime());

        Response response =(Response)connection.setTime( value ).getBody();

        if( !response.getNackCode().isOk() ) {
            String msg = "Set time failed: " + response.getNackCode();
            getLogger().severe(msg);
        }

    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        if(obisCodeMapper==null)
            obisCodeMapper = new ObisCodeMapper(this);
        return obisCodeMapper.getRegisterInfo(obisCode);
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    }

    public String getProtocolVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    public String getFirmwareVersion() throws IOException {
        return "<unknown>";
    }

    /* (non-Javadoc)
     * @see com.energyict.protocolimpl.base.AbstractProtocol#getProfileData(boolean)
     */
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar c = Calendar.getInstance(getTimeZone());
        c.add(Calendar.YEAR, -1);

        return getProfileData(c.getTime(),includeEvents);
    }

    /* (non-Javadoc)
     * @see com.energyict.protocolimpl.base.AbstractProtocol#getProfileData(java.util.Date, boolean)
     */
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents)
        throws IOException {

        /* if the to-time is after the metertime, to-time becomes metertime
         * (since you can not fetch future data) */
        if( getTime().before(to) ) to = getTime();

        List queryArguments = getProfileQueryArguments(from, to);

        ProfileMerge profile = new ProfileMerge(this);

        if( pChannelMap.isProtocolChannelEnabled(0) )
            fetch(queryArguments, profile, 1);

        if( pChannelMap.isProtocolChannelEnabled(1) )
            fetch(queryArguments, profile, 2);

        if( pChannelMap.isProtocolChannelEnabled(2) )
            fetch(queryArguments, profile, 3);

        if( pChannelMap.isProtocolChannelEnabled(3) )
            fetch(queryArguments, profile, 4);

        return profile.toProfileData(includeEvents);

    }

    private void fetch(List fetchList, ProfileMerge profile, int address) throws IOException {
        Iterator i = fetchList.iterator();
        while( i.hasNext() ) {

            ProfileQueryArguments pi = (ProfileQueryArguments)i.next();
            Date f = pi.from;
            int nr = pi.nrIntervals;

            Message msg = connection.seriesOnCount(address, f, nr);
            SeriesResponse rsp = (SeriesResponse)msg.getBody();

            if( !rsp.getNackCode().isOk() )
                throw new IOException("get profiledata " +rsp.getNackCode());

            profile.merge( rsp );

        }
    }

    /** ProfileQueryArguments contains the arguments for 1 SeriesOnCount
     * request. */
    class ProfileQueryArguments {

        Date from;
        int nrIntervals;

        ProfileQueryArguments(Date from, int nrIntervals){
            this.from = from;
            this.nrIntervals = nrIntervals;
        }

        public String toString(){
            return "ProfileInterval[" + from + ", " + nrIntervals + "]";
        }

    }

    /** Build a List containing all the QueryArguments. */
    private List getProfileQueryArguments(Date from, Date to) throws IOException{

        List result = new ArrayList();

        Calendar fromC = Calendar.getInstance(getTimeZone());
        fromC.setTime(from);

        int intervalMilli = getProfileInterval() * 1000;

        int nrIntvls = nrIntervalsBetween(from, to);

        while(nrIntvls>0){
            nrIntvls = Math.min(nrIntvls, FETCH_LIMIT);
            result.add( new ProfileQueryArguments(fromC.getTime(), nrIntvls) );
            fromC.add(Calendar.MILLISECOND, nrIntvls*intervalMilli);
            nrIntvls = nrIntervalsBetween(fromC.getTime(), to);
        }

        return result;

    }

    /** Calculate the nr of intervals between from and to date.
     *
     * - Calculate the nr of profileIntervals between the from and to date,
     *   and add 1 in case of rounding.
     *
     * - By calculating with ms, DST transitions are taken into account
     *   automatically.
     *
     * @return nr of intervals
     * @throws IOException in case of communication problems
     */
    private int nrIntervalsBetween(Date from, Date to) throws IOException {

        long msProfileInterval = getProfileInterval() * 1000;

        long msFrom = from.getTime() - (from.getTime()%msProfileInterval);
        long msTo   = to.getTime();

        long result = 0;
        result = ( msTo - msFrom ) / msProfileInterval;
        result += ((( msTo - msFrom ) % msProfileInterval ) > 0 ) ? 1 : 0;

        return (int)result;

    }

    Connection getConnection( ){
        return connection;
    }

    DataType getDataType(){
        return dataType;
    }

    ProtocolChannelMap getPChannelMap() {
        return pChannelMap;
    }

}
