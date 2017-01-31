package com.energyict.protocolimpl.iec1107.cewe.prometer;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ChannelInfo;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalStateBits;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.RetryHandler;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.iec1107.IEC1107Connection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

/** <pre>

 * An overview of the Prometer protocol:
 *
 * (*) Networking is handled by IEC1107Connection.
 *
 * (*) Parsing of general/simple data types is handled by ProRegister itself
 *     without any separate parsers.  ProRegister does this, it's no beauty, but
 *     it's short.
 *
 *
 * Remarks:
 *
 * (*) The meter uses the serial nr as a nodeId/nodeAddress.  This is no problem
 * just something to keep in mind.
 *
 * (*) Profile data
 * The meter uses the word "logging memory" for indicating the load profile.
 *
 * (*) Billing data
 * Billing data from previous billing points can/will perform badly.  To find
 * a billing register with a certain phenomenon, it is necessary to scan through
 * the billing register until the register is found.
 *
 * </pre>
 *
 * @author FBL
 *
 * @beginchanges
 * FBL|26022006| initial release
 * FBL|12082007| Minor bug fix: registers that are not supported by
 * the protocol result in a NullPointer.  Now the appropriate exception is
 * thrown.
 * FBL|29102007| Bug fix: multiplier was not correct when not powers of 10
 * (10, 100, 1000).  Units with Mega prefix where not supported.
 * GNA|10012008| Bug fix: EIServer didn't read data after power outage on the meter
 * GNA|19052008| Unit conversions are NOT case sensitive anymore
 * @endchanges
 */
public class Prometer extends AbstractProtocol implements SerialNumberSupport {

    /** Property keys specific for CewePrometer protocol. */
    private static final String PK_EXTENDED_LOGGING = AbstractProtocol.PROP_EXTENDED_LOGGING;
    private static final String PK_LOGGER = "Logger";

    /** By default the load profile of logger 1 is fetched */
    private static final int PD_LOGGER = 0;

    /** property for logger that must be fetched (1 or 2) */
    private int pLogger = PD_LOGGER;

    /** property for extended logging
     * null - 0: off
     * 1: on fetch ALL possible obis codes
     * 2: on fetch obis codes supported by connected meter and values */
    private int pExtendedLogging;

    /** yyMMdd,HHmm */
    private SimpleDateFormat queryDateFormat;
    /** yyMMddHHmm */
    private SimpleDateFormat shortDateFormat;
    /** yyMMddHHmmss */
    private SimpleDateFormat eventDateFormat;
    /** yyyyMMddHHmmss */
    private SimpleDateFormat longDateFormat;
    /** yyMMdd */
    private SimpleDateFormat dateRegisterFormat;
    /** HHmmss */
    private SimpleDateFormat timeRegisterFormat;

    private IEC1107Connection connection = null;
    private ObisCodeMapper obisCodeMapper = null;

    /** TOU-select for every TOU register */
    int[] touMap = null;
    /** channelInfo registers retrieved from  */
    private List<ChannelInfo> channelInfo = null;
    /** timeDiff: time difference (millis) between system and meter */
    private long timeDiff[] = null;

    /** Meter serial number: 1 char + 6 figures */
    private ProRegister rSerial = new ProRegister(this, "0001");

    /** Meter firmware version */
    private ProRegister rSoftwareVersion = new ProRegister(this, "0006");

    private ProRegister rDate = new ProRegister(this, "0021", false);
    private ProRegister rTime = new ProRegister(this, "0022", false);

    /** Energy registers */
    ProRegister rEenergy [] = {
        new ProRegister(this, "0101"), /* 255 */
        new ProRegister(this, "0102"), /* VZ  */
        new ProRegister(this, "0103"), /* VZ-1 */
        new ProRegister(this, "0104"), /* VZ-2 */
        new ProRegister(this, "0105"), /* VZ-3 */
        new ProRegister(this, "0106"), /* VZ-4 */
    };

    /** Historical external registers (8 floats) */
    ProRegister rExternal [] = {
        new ProRegister(this, "0107"), /* 255  */
        new ProRegister(this, "0108"), /* VZ   */
        new ProRegister(this, "0109"), /* VZ-1 */
    };

    private String md [][] =
    {   /*  MD 0,   MD 1,   MD 2 */
        { "0180", "0200", "0220" }, /* phenomenon 0 */
        { "0181", "0201", "0221" }, /* phenomenon 1 */
        { "0182", "0202", "0222" }, /* phenomenon 2 */
        { "0183", "0203", "0223" }, /* phenomenon 3 */
        { "0184", "0204", "0224" }, /* phenomenon 4 */

        { "0191", "0211", "0231" }, /* highest */
        { "0192", "0212", "0232" }, /* second  */
        { "0193", "0213", "0233" }  /* third   */
    };

    /** Maximum demand registers */
    ProRegister rMaximumDemand [][];

    /* init maximum demand */
    {
        rMaximumDemand = new ProRegister[md.length][];
        for( int row = 0; row < md.length; row ++ ) {
            rMaximumDemand[row] = new ProRegister[md[row].length];
            for( int col = 0; col < md[row].length; col ++ ) {
                rMaximumDemand[row][col] = new ProRegister(this, md[row][col]);
            }
        }
    }

    private static final String mdDate [] [] =
    {   /*  MD 0,   MD 1,   MD 2 */
        { "0185", "0205", "0225" }, /* date 0 */
        { "0186", "0206", "0226" }, /* date 1 */
        { "0187", "0207", "0227" }, /* date 2 */
        { "0188", "0208", "0228" }, /* date 3 */
        { "0189", "0209", "0229" }, /* date 4 */

        { "0195", "0215", "0235" }, /* highest */
        { "0196", "0216", "0236" }, /* second  */
        { "0197", "0217", "0237" }  /* third   */

    };

    /** Maximum demand registers */
    ProRegister rMaximumDemandDate [][];

    /* init maximum demand dates */
    {
        rMaximumDemandDate = new ProRegister[mdDate.length][];
        for( int row = 0; row < mdDate.length; row ++ ) {
            rMaximumDemandDate[row] = new ProRegister[mdDate[row].length];
            for( int col = 0; col < mdDate[row].length; col ++ ) {
                rMaximumDemandDate[row][col] = new ProRegister(this, mdDate[row][col]);
            }
        }
    }

    private static final String tou [][] = {
        /*phen.1, phen.2, phen.3, phen.4, phen.5 */
        { "0121", "0131", "0141", "0151", "0161" }, /* rate 1 */
        { "0122", "0132", "0142", "0152", "0162" }, /* rate 2 */
        { "0123", "0133", "0143", "0153", "0163" }, /* rate 3 */
        { "0124", "0134", "0144", "0154", "0164" }, /* rate 4 */
        { "0125", "0135", "0145", "0155", "0165" }, /* rate 5 */
        { "0126", "0136", "0146", "0156", "0166" }, /* rate 6 */
        { "0127", "0137", "0147", "0157", "0167" }, /* rate 7 */
        { "0128", "0138", "0148", "0158", "0168" }, /* rate 8 */
    };

    /** Tou registers*/
    ProRegister rTou [][];
    /* init tou */
    {
        rTou = new ProRegister[tou.length][];
        for( int row = 0; row < tou.length; row ++ ) {
            rTou[row] = new ProRegister[tou[row].length];
            for( int col = 0; col < tou[row].length; col ++ ) {
                rTou[row][col] = new ProRegister(this, tou[row][col]);
            }
        }

    }

    /*
     * "Load profile" related registers
     */

    private ProRegister rProfileIntervalLength = new ProRegister(this, "0170");

    /** Log Read offset: before fetching the load profile set start date */
    private ProRegister rLogOffset = new ProRegister(this, "1039");

    /** Read next Log record */
    private ProRegister rLogNextRecord = new ProRegister(this, "1040", false, 15);

    /** Register containing the configuration of the
     *  - Profile channels
     *  - Maximum demand registers
     */
    private ProRegister rDemandRegister [] =
    {
        new ProRegister(this, "1001"),
        new ProRegister(this, "1002"),
        new ProRegister(this, "1003"),
        new ProRegister(this, "1004"),
        new ProRegister(this, "1005")
    };

    /** every alarm has it's own register address */
    private ProRegister rAlarm [] = {
        new ProRegister(this, "0040"),
        new ProRegister(this, "0041"),
        new ProRegister(this, "0042"),
        new ProRegister(this, "0043"),
        new ProRegister(this, "0044"),
        new ProRegister(this, "0045"),
        new ProRegister(this, "0046"),
        new ProRegister(this, "0047"),
        new ProRegister(this, "0048"),
        new ProRegister(this, "0049"),
        new ProRegister(this, "0050"),
        new ProRegister(this, "0051"),
        new ProRegister(this, "0052"),
        new ProRegister(this, "0053"),
        new ProRegister(this, "0054"),
        new ProRegister(this, "0055"),
        new ProRegister(this, "0056"),
        new ProRegister(this, "0057"),
        new ProRegister(this, "0058"),
        new ProRegister(this, "0059"),
        new ProRegister(this, "0060"),
        new ProRegister(this, "0061"),
        new ProRegister(this, "0062"),
        new ProRegister(this, "0063"),
        new ProRegister(this, "0064"),
    };

    ProRegister rBilling [] = {
        new ProRegister(this, "1100"),
        new ProRegister(this, "1200")
    };

    private static final String billingTotal [][] = {
        /*  bp 1,  bp 2*/
        { "1101", "1201" },
        { "1102", "1202" },
        { "1103", "1203" },
        { "1104", "1204" },
        { "1105", "1205" },
        { "1106", "1206" },
        { "1107", "1207" },
        { "1108", "1208" }
    };

    ProRegister rBillingTotal [][] = null;
    {
        rBillingTotal = new ProRegister[billingTotal.length][];
        for( int row = 0; row < billingTotal.length; row ++ ) {
            rBillingTotal[row] = new ProRegister[2];
            for( int col = 0; col < 2; col ++ ) {
                rBillingTotal[row][col] = new ProRegister(this, billingTotal[row][col]);
            }
        }
    }

    private static final String billingRegister [][] = {
        /*  bp 1,  bp 2*/
        { "1110", "1210" },
        { "1111", "1211" },
        { "1112", "1212" },
        { "1113", "1213" },
        { "1114", "1214" },
        { "1115", "1215" },
        { "1116", "1216" },
        { "1117", "1217" },
        { "1118", "1218" },
        { "1119", "1219" },
        { "1120", "1220" },
        { "1121", "1221" },
        { "1122", "1222" },
        { "1123", "1223" },
        { "1124", "1224" },
        { "1125", "1225" },
        { "1126", "1226" },
        { "1127", "1227" },
        { "1128", "1228" },
        { "1129", "1229" },
        { "1130", "1230" },
        { "1131", "1231" },
        { "1132", "1232" },
        { "1133", "1233" },
        { "1134", "1234" },
        { "1135", "1235" },
        { "1136", "1236" },
        { "1137", "1237" },
        { "1138", "1238" },
        { "1139", "1239" },
        { "1140", "1240" },
        { "1141", "1241" },
        { "1142", "1242" },
        { "1143", "1243" },
        { "1144", "1244" },
        { "1145", "1245" },
        { "1146", "1246" },
        { "1147", "1247" },
        { "1148", "1248" },
        { "1149", "1249" }
    };

    ProRegister rBillingRegister [][] = null;
    {
        rBillingRegister = new ProRegister[billingRegister.length][];
        for( int row = 0; row < billingRegister.length; row ++ ) {
            rBillingRegister[row] = new ProRegister[2];
            for( int col = 0; col < 2; col ++ ) {
                rBillingRegister[row][col] = new ProRegister(this, billingRegister[row][col]);
            }
        }
    }

    private static final String billingMD [][] = {
        { "1150", "1250" },
        { "1151", "1251" },
        { "1152", "1252" },
        { "1153", "1253" },
        { "1154", "1254" },
        { "1155", "1255" },
        { "1156", "1256" },
        { "1157", "1257" },
        { "1158", "1258" },
        { "1159", "1259" },
        { "1160", "1260" },
        { "1161", "1261" },
        { "1162", "1262" },
        { "1163", "1263" },
        { "1164", "1264" }
    };

    ProRegister rBillingMD [][] = null;

	private boolean software7E1;
    {
        rBillingMD = new ProRegister[billingMD.length][];
        for( int row = 0; row < billingMD.length; row ++ ) {
            rBillingMD[row] = new ProRegister[2];
            for( int col = 0; col < 2; col ++ ) {
                rBillingMD[row][col] = new ProRegister(this, billingMD[row][col]);
            }
        }
    }

    /* Constants for interpreting the demandRegister configuration. */

    /** Reg no. Active import */
    static final int ACTIVE_IMPORT    = 1021;
    /** Reg no. Active export */
    static final int ACTIVE_EXPORT    = 1022;
    /** Reg no. Reactive import */
    static final int REACTIVE_IMPORT  = 1023;
    /** Reg no. Reactive export */
    static final int REACTIVE_EXPORT  = 1024;
    /** Reg no. Apparent import */
    static final int APPARENT_IMPORT  = 1025;
    /** Reg no. Apparent export */
    static final int APPARENT_EXPORT  = 1026;
    /** Reg no. External input 1 */
    static final int EXTERNAL_INPUT_1 = 1027;
    /** Reg no. External input 2 */
    static final int EXTERNAL_INPUT_2 = 1028;
    /** Reg no. External input 3 */
    static final int EXTERNAL_INPUT_3 = 1029;
    /** Reg no. Summation 1 */
    static final int SUMMATION_1      = 1030;
    /** Reg no. Summation 2 */
    static final int SUMMATION_2      = 1031;


    /* Billing registers contain a String defining the stored phenomenon.
     * For example: PE,3,,11235.5*kWh
     * PE is a constant defining active export.
     *
     * To find a certain phenomenon, the protocol needs to search through all
     * the billing registers.  (So it is not realy randomly accessible).
     */

    /** Billing register reg type: Active import */
    static final String REG_TYPE_ACTIVE_IMPORT = "PI";

    /** Billing register reg type: Active export */
    static final String REG_TYPE_ACTIVE_EXPORT = "PE";

    /** Billing register reg type: Rective import */
    static final String REG_TYPE_REACTIVE_IMPORT = "QI";

    /** Billing register reg type: Rective export */
    static final String REG_TYPE_REACTIVE_EXPORT = "QE";

    /** Billing register reg type: Apparent import */
    static final String REG_TYPE_APPARENT_IMPORT = "SI";

    /** Billing register reg type: Apparent export */
    static final String REG_TYPE_APPARENT_EXPORT = "SE";

    /** Billing register reg type: Input 1 */
    static final String REG_TYPE_INPUT_1 = "I1";

    /** Billing register reg type: Input 2 */
    static final String REG_TYPE_INPUT_2 = "I2";

    /** Billing register reg type: Input 3 */
    static final String REG_TYPE_INPUT_3 = "I3";

    /** Billing register reg type: Sum 1 */
    static final String REG_TYPE_SUM_1 = "S1";

    /** Billing register reg type: Sum 2 */
    static final String REG_TYPE_SUM_2 = "S2";

    public Prometer(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    protected ProtocolConnection doInit(
            InputStream iStream, OutputStream oStream,
            int pTimeout, int pRetries, int pForcedDelay,
            int pEchoCancelling, int pCompatible, Encryptor encryptor,
            HalfDuplexController halfDuplexController) throws IOException {

        getLogger().info(
            "CewePrometer protocol init \n"
                + " Ext. Logging = " + pExtendedLogging + ","
                + " TimeZone = " + getTimeZone().getID() + ","
                + " Logger " + pLogger );

        try {
            connection=
                new IEC1107Connection( iStream,oStream,pTimeout,pRetries,
                        pForcedDelay,pEchoCancelling,pCompatible,"[", software7E1);
            obisCodeMapper = new ObisCodeMapper(this);
        } catch(ConnectionException e) {
            getLogger().log(Level.SEVERE, "init failed", e);
            throw new NestedIOException(e);
        }

        return connection;
    }

    /** during connect:
     * 1) check firmware verion
     * 2) trigger extended logging
     *
     * The minimum firmware version is 1.2.0.  It is (probably) not difficult
     * to support older meter versions.  But just in case throw an exception.
     *
     * @see com.energyict.protocolimpl.base.AbstractProtocol#doConnect()
     */
    @Override
    protected void doConnect() throws IOException {
        if (pExtendedLogging==1) {
            getLogger().info(obisCodeMapper.toString());
        }
        if (pExtendedLogging==2) {
            getLogger().info(obisCodeMapper.getExtendedLogging());
        }
    }

    @Override
    protected void doDisconnect() throws IOException {
        /* do nothing, in a hurry */
    }

    IEC1107Connection getConnection( ){
        return connection;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.stringSpec("Software7E1", false));
        propertySpecs.add(this.integerSpec(PK_LOGGER, false));
        return propertySpecs;
    }

    @Override
    protected boolean passwordIsRequired() {
        return true;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        String extendedLogging = properties.getTypedProperty(PK_EXTENDED_LOGGING);
        pExtendedLogging = (extendedLogging == null) ? 0 : Integer.parseInt(extendedLogging);

        String logger = properties.getTypedProperty(PK_LOGGER);
        pLogger = (logger == null) ? PD_LOGGER : Integer.parseInt(logger);
        this.software7E1 = !"0".equalsIgnoreCase(properties.getTypedProperty("Software7E1", "0"));
    }

    @Override
    public Date getTime() throws IOException {
        try {
            ProRegister dr = rDate.readAndFreeze();
            ProRegister tr = rTime.readAndFreeze();
            Date d = getEventDateFormat().parse(dr.asString(0) + tr.asString());
            timeDiff = new long[] { System.currentTimeMillis() - d.getTime() };
            return d;
        } catch(ParseException pex){
            throw new NestedIOException(pex);
        }
    }

    public Date calculateMeterTime( ) throws IOException{
        if( timeDiff == null ) {
            return getTime();
        }
        return new Date(System.currentTimeMillis() + timeDiff[0]);
    }

    @Override
    public void setTime() throws IOException {

        Calendar cMeter = Calendar.getInstance(getTimeZone());
        cMeter.setTime(calculateMeterTime());

        String meterDateString = getDateRegisterFormat().format( cMeter.getTime() );

        Calendar cNow = Calendar.getInstance(getTimeZone());
        if( getInfoTypeRoundtripCorrection() != 0 ) {
            int correction = getInfoTypeRoundtripCorrection();
            cNow.add(Calendar.SECOND, correction);
        }

        String nowDateString = getDateRegisterFormat().format( cNow.getTime() );
        String nowTimeString = getTimeRegisterFormat().format( cNow.getTime() );

        if( !meterDateString.equals(nowDateString) ) {
            write( toCmd( rDate, nowDateString ) );
        }

        write( toCmd(rTime, nowTimeString ));

    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return getChannelInfo().size();
    }

    /** build ChannelInfo based on LogChannelConfig
     *
     * Check the length of the first field.  If lenght == 0, the channel is
     * not used.
     * */
    List<ChannelInfo> getChannelInfo( ) throws IOException {
        if (channelInfo == null) {
            channelInfo = new ArrayList<>();
            ProRegister dr;
            Unit unit;
            for (int i = 0; i < rDemandRegister.length; i ++ ) {
                dr = rDemandRegister[i];
                if ((!dr.asString().isEmpty()) && (dr.asInt() != 0)) {
                    unit = dr.asUnit(3);
                    channelInfo.add(new ChannelInfo(i, "chn " + i, unit, 0, i, getMultiplier(dr)));
                }
            }
        }
        return channelInfo;
    }

    /* multiplier is inverse of divisor: 1/divisor  */
    private BigDecimal getMultiplier(ProRegister r) throws IOException {
        BigDecimal bd = new BigDecimal( r.asInt(2) );
        int nrDigits = 6;
        int round = BigDecimal.ROUND_HALF_UP;
        return new BigDecimal( 1 ).divide(bd, nrDigits, round);
    }

    @Override
    public String getSerialNumber() {
        try {
            return rSerial.asString();
        } catch (IOException e) {
           throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

    @Override
    public String getProtocolVersion() {
    	return "$Date: 2015-11-26 15:25:14 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return rSoftwareVersion.asString();
    }

    @Override
    public int getProfileInterval() throws IOException {
        Quantity q = rProfileIntervalLength.asQuantity();
        return q.getAmount().intValue() * 60;
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return getProfileData(from, true);
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        ProfileData pd = new ProfileData();
        pd.setChannelInfos(getChannelInfo());
        Date lastInterval = new Date(lastReading.getTime() - (getProfileInterval() * 1000));

        RetryHandler retryHandler = new RetryHandler(getInfoTypeRetries());
        while (lastInterval != null && lastInterval.before(calculateMeterTime())) {
            try {
                /* step 1: write the start date */
                System.out.println("\n\n" + lastInterval + "\n\n");
                Date nextDate2Request = new Date(lastInterval.getTime() + (getProfileInterval() * 1000));
                write(toCmd(rLogOffset, getQueryDateFormat().format(nextDate2Request)));
                /* step 2: fetch profile data */
                while (lastInterval != null && lastInterval.before(calculateMeterTime())) {
                    lastInterval = toProfileData(pd, rLogNextRecord.getRawData(false));
                    retryHandler.reset();
                }
            } catch (IOException e) {
                System.out.println("\n\n" + e.getMessage() + "\n\n");
                retryHandler.logFailure(e);
            }
        }

        /* step 3: fetch log book */
        if( includeEvents ) {
            pd.generateEvents();
            addLogBook(pd);
        }

        /* standard procedure */
        pd.applyEvents(getProfileInterval()/60);

        return pd;
    }

    /**
     * Turn a response into a ProfileData object.  A response consists of a
     * series of intervals.
     */
    private Date toProfileData(ProfileData profileData, String buffer) throws IOException {
        Date last;
        List<ProRegister> list = splitIntervals(buffer);
        Iterator<ProRegister> i = list.iterator();
        do {
            last = add(profileData, i.next());
        } while (i.hasNext());
        return last;
    }

    /**
     * Split the complete register consisting of multiple records into
     * individual/separate records, and objectify them into a list of
     * ProRegisters.  ProRegisters, because they are easily parseable.
     * */
    private List<ProRegister> splitIntervals(String buffer) {
        List<ProRegister> list = new ArrayList<>();
        boolean eof = false;
        int openIdx = buffer.indexOf( '(', 0 );
        int closeIdx = buffer.indexOf( ')', 0 );
        while (!eof && (openIdx!=-1) && (closeIdx!=-1)){
            String interval = buffer.substring(openIdx, closeIdx+1);
            eof = interval.contains("(EOF)");
            if( !eof ) {
                ProRegister pr = new ProRegister(interval);
                pr.setCeweProMeter(this); /* hmmm .... */
                list.add(pr);
            }
            openIdx = buffer.indexOf( '(', closeIdx );
            closeIdx = buffer.indexOf( ')', openIdx );
        }
        return list;
    }

    /** add a single ProRegister (=interval) to the ProfileData object */
    private Date add(ProfileData profileData, ProRegister register) throws IOException {
        try {

            if (register.size() <= 2) {
                return null;
            }

            String ds = register.asString(0) + "," + register.asString(1);
            Date date = getQueryDateFormat().parse(ds);

            IntervalData id = new IntervalData(date);

            if (!register.isEmpty(2) ) {
                id.addValue(register.asBigDecimal(2));
            }
            if (!register.isEmpty(3) ) {
                id.addValue(register.asBigDecimal(3));
            }
            if (!register.isEmpty(4) ) {
                id.addValue(register.asBigDecimal(4));
            }
            if (!register.isEmpty(5) ) {
                id.addValue(register.asBigDecimal(5));
            }
            if (!register.isEmpty(6) ) {
                id.addValue(register.asBigDecimal(6));
            }

            int pStatus = register.asInt(7);

            id.setEiStatus( toIntervalStateBitToEIStatus(pStatus) );
            id.setProtocolStatus(pStatus);

            profileData.addInterval(id);

            return date;

        } catch(ParseException pex){
            throw new NestedIOException(pex);
        }

    }

    /**
     * Convert the meter status code into an eiStatus.
     */
    private int toIntervalStateBitToEIStatus(int tag) {
        int ei = 0;
        if ((tag & 0x01) > 0) {
            ei |= IntervalStateBits.OTHER;
        }
        if ((tag & 0x02) > 0) {
            ei |= IntervalStateBits.OTHER;
        }
        if ((tag & 0x04) > 0) {
            ei |= IntervalStateBits.SHORTLONG;
        }
        if ((tag & 0x08) > 0) {
            ei |= IntervalStateBits.CORRUPTED;
        }
        return ei;
    }

    /**
     * Add the alarm registers to the logbook.
     *
     * Check every alarm register for events.  An alarm register can contain
     * a null value.
     *
     * @param profileData object on wich to add the MeterEvents
     * @throws IOException in case of trouble
     */
    private void addLogBook(ProfileData profileData) throws IOException {
        for (int i = 0; i < rAlarm.length; i++) {
            ErrorMessage error = ErrorMessage.get(rAlarm[i].asInt());
            if (error != null) {
                String sDate = rAlarm[i].asString(1) + rAlarm[i].asString(2);
                try {
                    Date date = getEventDateFormat().parse(sDate);
                    int eiServerCode = error.getEiCode();
                    int protocolCode = error.getProtocolCode();
                    String description = error.getDescription();
                    profileData.addEvent(new MeterEvent(date, eiServerCode, protocolCode, description));
                } catch (ParseException e) {
                    getLogger().severe("Unable to parse event date [" + sDate + "]: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new ObisCodeMapper().getRegisterInfo(obisCode);
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return obisCodeMapper.getRegisterValue(obisCode);
    }

    /** Create a meter command in ByteArray form
     * @param register to read
     * @param arg arguments
     */
    private String toCmd(ProRegister register, String arg){
        return new String( register.getId() ) + "(" + arg + ")";
    }

    /** send read command */
    String read(String cmd, boolean retry) throws ProtocolException, ConnectionException, NestedIOException {
        connection.sendRawCommandFrame(IEC1107Connection.READ1, cmd.getBytes());
        byte[] rawData = retry ? connection.receiveRawData() : connection.doReceiveData();
        return new String(rawData);
    }

    /** send write command */
    private void write(String cmd) throws IOException {
        connection.sendRawCommandFrame(IEC1107Connection.WRITE1, cmd.getBytes());
    }

    /** Date format: yyyyMMdd,HHmmss */
    private SimpleDateFormat getQueryDateFormat(){
        if(queryDateFormat==null) {
            queryDateFormat = new SimpleDateFormat("yyMMdd,HHmm");
            queryDateFormat.setTimeZone(getTimeZone());
        }
        return queryDateFormat;
    }

    /** Date format: yyMMddHHmm */
    SimpleDateFormat getShortDateFormat(){
        if(shortDateFormat== null) {
            shortDateFormat = new SimpleDateFormat( "yyMMddHHmm" );
            shortDateFormat.setTimeZone(getTimeZone());
        }
        return shortDateFormat;
    }

    /** Date format: yyMMddHHmmss */
    private SimpleDateFormat getEventDateFormat(){
        if(eventDateFormat== null) {
            eventDateFormat = new SimpleDateFormat( "yyMMddHHmmss" );
            eventDateFormat.setTimeZone(getTimeZone());
        }
        return eventDateFormat;
    }

    /** yyMMdd */
    private SimpleDateFormat getDateRegisterFormat() {
        if(dateRegisterFormat==null){
            dateRegisterFormat = new SimpleDateFormat( "yyMMdd" );
            dateRegisterFormat.setTimeZone(getTimeZone());
        }
        return dateRegisterFormat;
    }

    /** HHmmss */
    private SimpleDateFormat getTimeRegisterFormat() {
        if(timeRegisterFormat==null){
            timeRegisterFormat = new SimpleDateFormat( "HHmmss" );
            timeRegisterFormat.setTimeZone(getTimeZone());
        }
        return timeRegisterFormat;
    }

    /**
     * Find the MD register for a phenomenon.
     * see: "Constants for interpreting the demandRegister configuration."
     *
     * @param regNo the cewe constant identifying the phenomenon
     * @return idx of md register
     * @throws IOException
     */
    int findMDRegister(int regNo) throws IOException {
        for( int i = 0; i < rDemandRegister.length; i++) {
            if (rDemandRegister[i].asInt() == regNo) {
                return i;
            }
        }

        throw new NoSuchRegisterException();
    }

}