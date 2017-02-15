package com.energyict.protocolimpl.enermet.e120;

import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;

import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;

/**
 * A 100 Mile high overview of the E120 protocol:
 * <p>
 * (*) Connection       : Entry point for communication
 * <p>
 * (*) Message          : Application layer request or response
 * (*) Packet           : Network layer request or response
 * (*) Frame            : Datalink layer request or response
 * <p>
 * (*) MessageType      : Application layer method (f.g. seriesBasedOnTime)
 * Linking of MessageType and message parsers
 * <p>
 * (*) MessageBody      : Markup interface for Message content
 * (*) Request          : Request to meter (Application layer)
 * (*) Response         : Response from meter
 * (*) DefaultResponse  : Response of a single value
 * (*) SeriesResponse   : Response of list of values
 * <p>
 * (*) NackCode         : Application layer status (Message status)
 * (*) StatCode         : Network layer status (Packet status)
 * <p>
 * (*) UnitMap          : Maps between E120 unit codes and EIS Units
 * (*) DataType         : Composite datatype parser for application data
 *
 * @author fbo
 * @beginchanges FBL|15012006| initial release
 * @endchanges
 */

public class E120 extends AbstractProtocol implements RegisterProtocol {

    /**
     * Maximum nr of intervals that can be fetched in 1 SeriesOnCount request
     */
    private static final int FETCH_LIMIT = 306;

    static final MessageFormat ERROR_0 = new MessageFormat(
            "Configured profile interval size differs from requested size " +
                    "(configured={0}s, meter={1}s)");
    static final MessageFormat ERROR_1 = new MessageFormat(
            "Found different nr of entries in: \n {0} \n {1} ");

    /**
     * Property keys specific for E120 protocol.
     */
    private static final String PK_TIMEOUT = PROP_TIMEOUT;
    private static final String PK_RETRIES = PROP_RETRIES;
    private static final String PK_EXTENDED_LOGGING = PROP_EXTENDED_LOGGING;
    private static final String PK_USER_ID = "userId";
    private static final String PK_PASSWORD = PASSWORD.getName();
    private static final String PK_CHANNEL_MAP = "ChannelMap";

    private String pUserId;
    private String pPassword;
    private int pExtendedLogging;
    private ProtocolChannelMap pChannelMap;

    private Connection connection;
    private ObisCodeMapper obisCodeMapper;
    private DataType dataType;

    private int pRetries;

    public E120(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    @Override
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

        connection = new Connection(this, inputStream, outputStream);

        obisCodeMapper = new ObisCodeMapper(this);
        initDataType(getTimeZone());

        return connection;

    }

    void initDataType(TimeZone timeZone) {
        this.dataType = new DataType(timeZone);
    }

    @Override
    protected void doConnect() throws IOException {

        if (!connection.connect(pUserId, pPassword).isOk()) {
            throw new IOException("connect failed");
        }

        if (pExtendedLogging == 1) {
            getLogger().info(obisCodeMapper.toString());
        }

        if (pExtendedLogging == 2) {
            getLogger().info(obisCodeMapper.getExtendedLogging());
        }

    }

    @Override
    protected void doDisconnect() throws IOException {
        /* do nothing */
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.stringSpec(PK_USER_ID, PropertyTranslationKeys.USE120_USER_ID , true));
        propertySpecs.add(ProtocolChannelMap.propertySpec(PK_CHANNEL_MAP, true, getNlsService().getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.E120_CHANNEL_MAP).format(), getNlsService().getThesaurus(Thesaurus.ID.toString()).getFormat(PropertyTranslationKeys.E120_CHANNEL_MAP_DESCRIPTION).format()));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        pUserId = properties.getTypedProperty(PK_USER_ID);
        pPassword = properties.getTypedProperty(PK_PASSWORD);
        pChannelMap = new ProtocolChannelMap(((String) properties.getTypedProperty(PK_CHANNEL_MAP)));
        if (propertyExists(properties, PK_RETRIES)) {
            pRetries = Integer.parseInt(properties.getTypedProperty(PK_RETRIES));
        }
        if (propertyExists(properties, PK_EXTENDED_LOGGING)) {
            pExtendedLogging = Integer.parseInt(properties.getTypedProperty(PK_EXTENDED_LOGGING));
        }
    }

    private boolean propertyExists(TypedProperties p, String key) {
        String aProperty = p.getTypedProperty(key);
        return (aProperty != null) && !"".equals(aProperty);
    }

    int getRetries() {
        return pRetries;
    }

    @Override
    public Date getTime() throws IOException {
        Message mr = connection.send(MessageType.GET_TIME);
        return (Date) ((DefaultResponse) mr.getBody()).getValue();
    }

    @Override
    public void setTime() throws IOException {
        Calendar c = Calendar.getInstance(getTimeZone());
        c.add(Calendar.MILLISECOND, getInfoTypeRoundtripCorrection());
        ByteArray value = dataType.getTime().construct(c.getTime());

        Response response = (Response) connection.setTime(value).getBody();

        if (!response.getNackCode().isOk()) {
            String msg = "Set time failed: " + response.getNackCode();
            getLogger().severe(msg);
        }
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        if (obisCodeMapper == null) {
            obisCodeMapper = new ObisCodeMapper(this);
        }
        return obisCodeMapper.getRegisterInfo(obisCode);
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return "<unknown>";
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        Calendar c = Calendar.getInstance(getTimeZone());
        c.add(Calendar.YEAR, -1);
        return getProfileData(c.getTime(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return getProfileData(lastReading, new Date(), includeEvents);
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents)
            throws IOException {

        /* if the to-time is after the metertime, to-time becomes metertime
         * (since you can not fetch future data) */
        if (getTime().before(to)) {
            to = getTime();
        }

        List queryArguments = getProfileQueryArguments(from, to);

        ProfileMerge profile = new ProfileMerge(this);

        if (pChannelMap.isProtocolChannelEnabled(0)) {
            fetch(queryArguments, profile, 1);
        }

        if (pChannelMap.isProtocolChannelEnabled(1)) {
            fetch(queryArguments, profile, 2);
        }

        if (pChannelMap.isProtocolChannelEnabled(2)) {
            fetch(queryArguments, profile, 3);
        }

        if (pChannelMap.isProtocolChannelEnabled(3)) {
            fetch(queryArguments, profile, 4);
        }

        return profile.toProfileData(includeEvents);

    }

    private void fetch(List fetchList, ProfileMerge profile, int address) throws IOException {
        for (Object aFetchList : fetchList) {
            ProfileQueryArguments pi = (ProfileQueryArguments) aFetchList;
            Date f = pi.from;
            int nr = pi.nrIntervals;

            Message msg = connection.seriesOnCount(address, f, nr);
            SeriesResponse rsp = (SeriesResponse) msg.getBody();

            if (!rsp.getNackCode().isOk()) {
                throw new IOException("get profiledata " + rsp.getNackCode());
            }
            profile.merge(rsp);
        }
    }

    /**
     * ProfileQueryArguments contains the arguments for 1 SeriesOnCount
     * request.
     */
    private class ProfileQueryArguments {

        Date from;
        int nrIntervals;

        ProfileQueryArguments(Date from, int nrIntervals) {
            this.from = from;
            this.nrIntervals = nrIntervals;
        }

        public String toString() {
            return "ProfileInterval[" + from + ", " + nrIntervals + "]";
        }

    }

    /**
     * Build a List containing all the QueryArguments.
     */
    private List<ProfileQueryArguments> getProfileQueryArguments(Date from, Date to) throws IOException {
        List<ProfileQueryArguments> result = new ArrayList<>();
        Calendar fromC = Calendar.getInstance(getTimeZone());
        fromC.setTime(from);
        int intervalMilli = getProfileInterval() * 1000;
        int nrIntvls = nrIntervalsBetween(from, to);
        while (nrIntvls > 0) {
            nrIntvls = Math.min(nrIntvls, FETCH_LIMIT);
            result.add(new ProfileQueryArguments(fromC.getTime(), nrIntvls));
            fromC.add(Calendar.MILLISECOND, nrIntvls * intervalMilli);
            nrIntvls = nrIntervalsBetween(fromC.getTime(), to);
        }
        return result;
    }

    /**
     * Calculate the nr of intervals between from and to date.
     * <p>
     * - Calculate the nr of profileIntervals between the from and to date,
     * and add 1 in case of rounding.
     * <p>
     * - By calculating with ms, DST transitions are taken into account
     * automatically.
     *
     * @return nr of intervals
     * @throws IOException in case of communication problems
     */
    private int nrIntervalsBetween(Date from, Date to) throws IOException {
        long msProfileInterval = getProfileInterval() * 1000;
        long msFrom = from.getTime() - (from.getTime() % msProfileInterval);
        long msTo = to.getTime();
        long result;
        result = (msTo - msFrom) / msProfileInterval;
        result += (((msTo - msFrom) % msProfileInterval) > 0) ? 1 : 0;
        return (int) result;
    }

    Connection getConnection() {
        return connection;
    }

    DataType getDataType() {
        return dataType;
    }

    ProtocolChannelMap getPChannelMap() {
        return pChannelMap;
    }

}