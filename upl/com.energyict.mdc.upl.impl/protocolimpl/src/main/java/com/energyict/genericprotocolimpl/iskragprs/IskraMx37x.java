/**
 *
 */
package com.energyict.genericprotocolimpl.iskragprs;

import com.energyict.cbo.*;
import com.energyict.cpo.Environment;
import com.energyict.dialer.connection.*;
import com.energyict.dialer.core.*;
import com.energyict.dialer.coreimpl.SocketStreamConnection;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.PPPSetup.PPPAuthenticationType;
import com.energyict.genericprotocolimpl.common.*;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.genericprotocolimpl.common.tou.ActivityCalendarReader;
import com.energyict.genericprotocolimpl.common.tou.CosemActivityCalendarBuilder;
import com.energyict.genericprotocolimpl.iskragprs.csd.CSDCall;
import com.energyict.genericprotocolimpl.iskragprs.csd.CSDCaller;
import com.energyict.genericprotocolimpl.iskragprs.imagetransfer.ImageTransfer;
import com.energyict.genericprotocolimpl.nta.abstractnta.NTASecurityProvider;
import com.energyict.mdw.amr.*;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.mbus.core.ValueInformationfieldCoding;
import com.energyict.protocolimpl.messages.*;

import java.io.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author gna
 *         Changes:
 *         GNA |29012009| Added force clock
 *         GNA |02022009| Mad some changes to the sendTOU message. No activationDate is immediate activation using the Object method
 *         GNA |23022009| Added mbus install/remove/dataretrieval messages
 *         GNA |26032009| Added the activate Wakeup message
 *         GNA |02042009| Added extra registers to check if the MO switch has successfully been done
 *         GNA |28052009| Deleted the 'DeleteNumberFromWhiteList' message and adjusted the set message to a limit of 5 numbers
 *         GNA |28092009| Added 4 custom properties to allow setting the 4 profiles
 */
public class IskraMx37x implements GenericProtocol, ProtocolLink, CacheMechanism, Messaging, HHUEnabler {

    private static final String DUPLICATE_SERIALS =
            "Multiple meters where found with serial: {0}.  Data will not be read.";
    private static final String FOLDER_EXT_NAME = "FolderExtName";

    /**
     * The maximum allowed number of phoneNumbers to make a CSD call to the device
     */
    private static final int maxNumbersCSDWhiteList = 8;
    /**
     * The maximum allowed number of managed calls to be put in the whiteList
     */
    private static final int maxNumbersManagedWhiteList = 8;

    private static final int ELECTRICITY = 0x00;

    private static final int MBUS = 0x01;

    private final static String RTU_TYPE = "RtuType";

    public static final int MBUS_MAX = 0x04;

    public static ScalerUnit[] demandScalerUnits = {new ScalerUnit(0, 30), new ScalerUnit(0, 255), new ScalerUnit(0, 255), new ScalerUnit(0, 255), new ScalerUnit(0, 255)};


    private int DEBUG = 0;
    private int TESTLOGGING = 0;
    private boolean initCheck = false;

    private Logger logger;
    private Properties properties;
    private CommunicationProfile communicationProfile;
    private Link link;
    private DLMSConnection dlmsConnection;
    private CosemObjectFactory cosemObjectFactory;
    private SecureConnection secureConnection;
    private Rtu rtu;
    private Clock clock;
    private Cache dlmsCache;
    private DLMSMeterConfig meterConfig;
    private ObisCodeMapper ocm = null;
    private MbusDevice[] mbusDevices = {null, null, null, null};                // max. 4 MBus meters
    private CommunicationScheduler scheduler;

    private int iHDLCTimeoutProperty;
    private int iProtocolRetriesProperty;
    private int iClientMacAddress;
    private int iServerUpperMacAddress;
    private int iServerLowerMacAddress;
    private int iSecurityLevelProperty;
    private int iRequestTimeZone;
    private int iRoundtripCorrection;
    private int extendedLogging;
    private int addressingMode;
    private int connectionMode;
    private int configProgramChanges;
    private int csdCall;

    private long timeDifference;

    private boolean forcedMbusCheck = false;

    private String strID;
    private String strPassword;
    private String firmwareVersion;
    private String nodeId;
    private String serialNumber;
    private String rtuType;
    private String serialnr = null;
    private String devID = null;

    private ObisCode genericEMeterProfile = ObisCode.fromString("1.0.99.1.0.255");        // mostly considered as intervalProfile
    private ObisCode genericMbusMeterProfile = ObisCode.fromString("1.0.99.2.0.255");        // mostly considered as MBus profile of daily profile
    private ObisCode genericDailyProfile = ObisCode.fromString("1.0.98.1.0.255");        // mostly considered as monthly or daily profile
    private ObisCode genericMonthlyProfile = ObisCode.fromString("1.0.98.2.0.255");        // mostly considered as daily or monthly profile
    private ObisCode breakerObisCode = ObisCode.fromString("0.0.128.30.21.255");
    private ObisCode deviceLogicalName = ObisCode.fromString("0.0.42.0.0.255");
    private ObisCode endOfBilling = ObisCode.fromString("0.0.15.0.0.255");
    private ObisCode endOfCapturedObjects = ObisCode.fromString("0.0.15.1.0.255");
    private ObisCode[] mbusPrimaryAddress = {ObisCode.fromString("0.1.128.50.20.255"),
            ObisCode.fromString("0.2.128.50.20.255"),
            ObisCode.fromString("0.3.128.50.20.255"),
            ObisCode.fromString("0.4.128.50.20.255")};
    private ObisCode[] mbusCustomerID = {ObisCode.fromString("0.1.128.50.21.255"),
            ObisCode.fromString("0.2.128.50.21.255"),
            ObisCode.fromString("0.3.128.50.21.255"),
            ObisCode.fromString("0.4.128.50.21.255")};
    private ObisCode[] mbusUnit = {ObisCode.fromString("0.1.128.50.30.255"),
            ObisCode.fromString("0.2.128.50.30.255"),
            ObisCode.fromString("0.3.128.50.30.255"),
            ObisCode.fromString("0.4.128.50.30.255")};
    private ObisCode[] mbusMedium = {ObisCode.fromString("0.1.128.50.23.255"),
            ObisCode.fromString("0.2.128.50.23.255"),
            ObisCode.fromString("0.3.128.50.23.255"),
            ObisCode.fromString("0.4.128.50.23.255")};
    private ObisCode crGroupID = ObisCode.fromString("0.0.128.62.0.255");
    private ObisCode crStartDate = ObisCode.fromString("0.0.128.62.1.255");
    private ObisCode crDuration = ObisCode.fromString("0.0.128.62.2.255");
    private ObisCode crPowerLimit = ObisCode.fromString("0.0.128.62.3.255");
    private ObisCode crMeterGroupID = ObisCode.fromString("0.0.128.62.6.255");
    private ObisCode contractPowerLimit = ObisCode.fromString("0.0.128.61.1.255");
    private ObisCode llsSecretObisCode1 = ObisCode.fromString("0.0.128.100.1.255");
    private ObisCode llsSecretObisCode2 = ObisCode.fromString("0.0.128.100.2.255");
    private ObisCode llsSecretObisCode3 = ObisCode.fromString("0.0.128.100.3.255");
    private ObisCode llsSecretObisCode4 = ObisCode.fromString("0.0.128.100.4.255");
    private ObisCode dailyObisCode = null;
    private ObisCode monthlyObisCode = null;
    private ObisCode loadProfileObisCode = null;
    private ObisCode[] mbusLProfileObisCode = {null, null, null, null};

    private ArrayList monthlyProfilConfig = new ArrayList(20);

    private byte[] connectMsg = new byte[]{DLMSCOSEMGlobals.TYPEDESC_UNSIGNED, 0x01};
    private byte[] disconnectMsg = new byte[]{DLMSCOSEMGlobals.TYPEDESC_UNSIGNED, 0x00};
    private byte[] contractPowerLimitMsg = new byte[]{DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG_UNSIGNED, 0, 0, 0, 0};
    private byte[] crPowerLimitMsg = new byte[]{DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG_UNSIGNED, 0, 0, 0, 0};
    private byte[] crDurationMsg = new byte[]{DLMSCOSEMGlobals.TYPEDESC_DOUBLE_LONG_UNSIGNED, 0, 0, 0, 0};
    private byte[] crMeterGroupIDMsg = new byte[]{DLMSCOSEMGlobals.TYPEDESC_LONG_UNSIGNED, 0, 0};
    private byte[] crGroupIDMsg = new byte[]{DLMSCOSEMGlobals.TYPEDESC_LONG_UNSIGNED, 0, 0};


    /**
     *
     */
    public IskraMx37x() {
    }

    public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {

        String ipAddress = null;

        rtu = scheduler.getRtu();
        validateProperties();


        // if it is a CSD scheduler, we just have to make a call
        if (scheduler.displayString().toLowerCase().indexOf("csd") > 0) {

            if (!scheduler.getDialerFactory().getName().equalsIgnoreCase("nulldialer")) {
                throw new IOException("Only NoDialer is allowed as csd dialers.");
            }

            if (this.csdCall != 0) {
                CSDCall call = new CSDCall(link);
                call.doCall(rtu.getPhoneNumber(), rtu.getPostDialCommand());
                logger.log(Level.INFO, "Made a successful call.");

            } else {
                throw new IOException("CSDCall can not be executed if the csdProperty is not enabled");
            }


        } else {    // else we do a regular data readout

            this.logger = logger;
            this.communicationProfile = scheduler.getCommunicationProfile();
            this.link = link;
            this.scheduler = scheduler;
            if (this.csdCall != 0) {

                if (!this.scheduler.getDialerFactory().getName().equalsIgnoreCase("nulldialer")) {
                    throw new IOException("Only NoDialer is allowed for csd calls.");
                }

                CSDCaller caller = new CSDCaller(rtu);
                ipAddress = caller.doWakeUp();
                if (!ipAddress.equalsIgnoreCase("")) {
                    this.rtu.updateIpAddress(ipAddress);
                    ipAddress = checkIPAddressForPortNumber(ipAddress);
                    getLogger().log(Level.INFO, "IPAddress " + ipAddress + " found for meter with serialnumber " + this.serialNumber);

                    this.link.setStreamConnection(new SocketStreamConnection(ipAddress));
                    this.link.getStreamConnection().open();
                    getLogger().log(Level.INFO, "Connected to " + ipAddress);
                } else {
                    throw new ConnectionException("CSD Wakeup call failed.");
                }
            } else if (scheduler.getDialerFactory().getName().equalsIgnoreCase("nulldialer")) {
                throw new IOException("NoDialer is only allowed for CSD calls (CsdCall property should be set to 1)");
            }

            init(this.link.getInputStream(), this.link.getOutputStream());

            try {
                connect();

                // Set clock or Force clock... if necessary
                if (communicationProfile.getForceClock()) {
                    Date cTime = getTime();
                    Date now = Calendar.getInstance(getTimeZone()).getTime();
                    this.timeDifference = (now.getTime() - cTime.getTime());
                    getLogger().info("Difference between metertime and systemtime is " + this.timeDifference + " ms");
                    getLogger().log(Level.INFO, "Forced to set meterClock to systemTime: " + now);
                    setTimeClock();
                } else if (communicationProfile.getWriteClock()) {
                    setTime();
                }

                // Read profiles and events ... if necessary
                if (communicationProfile.getReadDemandValues()) {
                    doTheCheckMethods();
                    getLogger().log(Level.INFO, "Getting loadProfile for meter with serialnumber: " + rtu.getSerialNumber());
                    ElectricityProfile ep = new ElectricityProfile(this);
                    ep.getProfile(loadProfileObisCode, communicationProfile.getReadMeterEvents());
                }

                // Read registers ... if necessary
                /**
                 * Here we are assuming that the daily and monthly values should be read.
                 * In future it can be that this doesn't work for all customers, then we should implement a SmartMeterProperty to indicate whether you
                 * want to read the actual registers or the daily/monthly registers ...
                 */
                if (communicationProfile.getReadMeterReadings()) {
                    doTheCheckMethods();

                    getLogger().log(Level.INFO, "Getting daily and monthly values for meter with serialnumber: " + rtu.getSerialNumber());
                    DailyMonthly dm = new DailyMonthly(this);
                    if (dailyObisCode != null) {
                        dm.getDailyValues(dailyObisCode);
                    }
                    if (monthlyObisCode != null) {
                        dm.getMonthlyValues(monthlyObisCode);
                    }
                }

                // Send messages ... if there are messages
                if (communicationProfile.getSendRtuMessage()) {
                    sendMeterMessages();
                }

                // Handle the MBus meters
                if (mbusCheck()) {
                    getLogger().log(Level.INFO, "Starting to handle the MBus meters.");
                    handleMbusMeters();
                }

                if (TESTLOGGING >= 1) {
                    getLogger().log(Level.INFO, "GN - TESTLOG: Stopping the cache mechanism, saving to disk.");
                }
                GenericCache.stopCacheMechanism(rtu, dlmsCache);
                disConnect();

                getLogger().log(Level.INFO, "Meter with serialnumber " + rtu.getSerialNumber() + " has completely finished.");
            } catch (DLMSConnectionException e) {
                disConnect();
                e.printStackTrace();
                throw new BusinessException(e);
            } catch (ParseException e) {
                e.printStackTrace();
                disConnect();
                throw new BusinessException(e);
            } catch (SQLException e) {
                e.printStackTrace();
                disConnect();

                /** Close the connection after an SQL exception, connection will startup again if requested */
                Environment.getDefault().closeConnection();
                throw new BusinessException(e);
            } catch (BusinessException e) {
                e.printStackTrace();
                disConnect();
                throw new BusinessException(e);
            } finally {
                if (dlmsCache.getObjectList() != null) {
                    GenericCache.stopCacheMechanism(rtu, dlmsCache);
                }
            }
        }
    }

    /**
     * If the received IP address doesn't contain a portnumber, then put one in it
     *
     * @param ipAddress
     * @return
     */
    private String checkIPAddressForPortNumber(String ipAddress) {
        if (!ipAddress.contains(":")) {
            StringBuffer strBuff = new StringBuffer();
            strBuff.append(ipAddress);
            strBuff.append(":");
            strBuff.append(getPortNumber());
            return strBuff.toString();
        }
        return ipAddress;
    }

    /**
     * Look if there is a portnumber given with the property IpPortNumber, else use the default 2048
     *
     * @return
     */
    private String getPortNumber() {
        String port = getMeter().getProperties().getProperty("IpPortNumber");
        if (port != null) {
            return port;
        } else {
            return "2048";    // default port number
        }
    }

    private void justATestMethod() {
        try {
            getCosemObjectFactory().getGenericInvoke(ObisCode.fromString("0.0.10.50.128.255"), 9, 1).invoke(new Unsigned16(4).getBEREncodedByteArray());

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Checks if there is in fact an MBus meter configured on the E-meter
     *
     * @return true or false
     */
    private boolean mbusCheck() {

        for (int i = 0; i < MBUS_MAX; i++) {
            if (mbusDevices[i] != null) {
                if (mbusDevices[i].getMbus() != null) {
                    return true;
                }
            }
        }
        return false;

    }

    private void handleMbusMeters() {
        for (int i = 0; i < MBUS_MAX; i++) {
            if (mbusDevices[i] != null) {
                try {
                    mbusDevices[i].setIskraDevice(this);
                    mbusDevices[i].execute(scheduler, null, null);
                } catch (BusinessException e) {
                    /*
                          * A single MBusMeter failed: log and try next MBusMeter.
                          */
                    e.printStackTrace();
                    getLogger().log(Level.SEVERE, "MBusMeter with serial: " + mbusDevices[i].getCustomerID() + " has failed.");

                } catch (SQLException e) {

                    /** Close the connection after an SQL exception, connection will startup again if requested */
                    Environment.getDefault().closeConnection();

                    /*
                          * A single MBusMeter failed: log and try next MBusMeter.
                          */
                    e.printStackTrace();
                    getLogger().log(Level.SEVERE, "MBusMeter with serial: " + mbusDevices[i].getCustomerID() + " has failed.");

                } catch (IOException e) {
                    /*
                          * A single MBusMeter failed: log and try next MBusMeter.
                          */
                    e.printStackTrace();
                    getLogger().log(Level.SEVERE, "MBusMeter with serial: " + mbusDevices[i].getCustomerID() + " has failed.");

                }
            }
        }
    }

    private void doTheCheckMethods() throws IOException, SQLException, BusinessException {
        if (!initCheck) {
            checkConfiguration();
            initCheck = true;
        }
    }

    /**
     * Short notation for MeteringWarehouse.getCurrent()
     */
    private MeteringWarehouse mw() {
        return MeteringWarehouse.getCurrent();
    }

    private void checkConfiguration() throws IOException {
        this.loadProfileObisCode = genericEMeterProfile;
        for (int i = 0; i < MBUS_MAX; i++) {
            mbusLProfileObisCode[i] = genericMbusMeterProfile;
        }
        this.dailyObisCode = genericDailyProfile;
        this.monthlyObisCode = genericMonthlyProfile;
    }

    private void checkMbusDevices() throws IOException, SQLException, BusinessException {
        String mSerial = "";
        if (!((getMeter().getDownstreamRtus().size() == 0) && (getRtuType() == null))) {
            for (int i = 0; i < MBUS_MAX; i++) {
                int mbusAddress = (int) getCosemObjectFactory().getCosemObject(mbusPrimaryAddress[i]).getValue();
                if (mbusAddress > 0) {
                    mSerial = getMbusSerial(mbusCustomerID[i]);
                    if (!mSerial.equals("")) {
                        Unit mUnit = getMbusUnit(mbusUnit[i]);
                        int mMedium = (int) getCosemObjectFactory().getCosemObject(mbusMedium[i]).getValue();
                        Rtu mbusRtu = findOrCreateNewMbusDevice(mSerial);
                        if (mbusRtu != null) {
                            mbusDevices[i] = new MbusDevice(mbusAddress, i, mSerial, mMedium, mbusRtu, mUnit, getLogger());
                        } else {
                            mbusDevices[i] = null;
                        }
                    } else {
                        mbusDevices[i] = null;
                    }
                } else {
                    mbusDevices[i] = null;
                }
            }
        }
        updateMbusDevices(rtu.getDownstreamRtus());
    }

    private void updateMbusDevices(List<Rtu> downstreamRtus) throws SQLException, BusinessException {
        Iterator<Rtu> it = downstreamRtus.iterator();
        boolean delete = true;
        while (it.hasNext()) {
            Rtu mbus = it.next();
            delete = true;
            for (int i = 0; i < mbusDevices.length; i++) {
                if (mbusDevices[i] != null) {
                    if (mbus.getSerialNumber().equalsIgnoreCase(mbusDevices[i].getCustomerID())) {
                        delete = false;
                    }
                }
            }
            if (delete) {
                RtuShadow shadow = mbus.getShadow();
                shadow.setGatewayId(0);
                mbus.update(shadow);
            }
        }
    }

    private Unit getMbusUnit(ObisCode obisCode) throws IOException {
        try {
            String vifResult = Integer.toString((int) getCosemObjectFactory().getData(obisCode).getRawValueAttr()[2], 16);
            ValueInformationfieldCoding vif = ValueInformationfieldCoding.findPrimaryValueInformationfieldCoding(Integer.parseInt(vifResult, 16), -1);
            return vif.getUnit();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Could not retrieve the MBus Unit");
        }
    }

    private Rtu findOrCreateNewMbusDevice(String customerID) throws SQLException, BusinessException, IOException {
        List mbusList = mw().getRtuFactory().findBySerialNumber(customerID);
        if (mbusList.size() == 1) {
            Rtu mbusRtu = (Rtu) mbusList.get(0);
            // Check if gateway has changed, and update if it has
            if ((mbusRtu.getGateway() == null) || (mbusRtu.getGateway().getId() != rtu.getId())) {
                mbusRtu.updateGateway(rtu);
            }
            return mbusRtu;
        }
        if (mbusList.size() > 1) {
            getLogger().severe(toDuplicateSerialsErrorMsg(customerID));
            return null;
        }
        RtuType rtuType = getRtuType();
        if (rtuType == null) {
            return null;
        } else {
            return createMeter(rtu, getRtuType(), customerID);
        }
    }

    private Rtu createMeter(Rtu rtu2, RtuType type, String customerID) throws SQLException, BusinessException {
        RtuShadow shadow = type.newRtuShadow();

        Date lastreading = shadow.getLastReading();

        shadow.setName(customerID);
        shadow.setSerialNumber(customerID);

        String folderExtName = getFolderID();
        if (folderExtName != null) {
            Folder result = mw().getFolderFactory().findByExternalName(folderExtName);
            if (result != null) {
                shadow.setFolderId(result.getId());
            } else {
                getLogger().log(Level.INFO, "No folder found with external name: " + folderExtName + ", new meter will be placed in prototype folder.");
            }
        } else {
            getLogger().log(Level.INFO, "New meter will be placed in prototype folder.");
        }

        shadow.setGatewayId(rtu.getId());
        shadow.setLastReading(lastreading);
        return mw().getRtuFactory().create(shadow);
    }

    /**
     * @return the folderID of the given rtu
     */
    private String getFolderID() {
        String folderid = getProperty(FOLDER_EXT_NAME);
        return folderid;
    }

    private RtuType getRtuType() throws IOException {
        String type = getProperty(RTU_TYPE);
        if (Utils.isNull(type)) {
            getLogger().warning("No automatic meter creation: no property RtuType defined.");
            return null;
        } else {
            RtuType rtuType = mw().getRtuTypeFactory().find(type);
            if (rtuType == null) {
                throw new IOException("Iskra Mx37x, No rtutype defined with name '" + type + "'");
            }
            if (rtuType.getPrototypeRtu() == null) {
                throw new IOException("Iskra Mx37x, rtutype '" + type + "' has no prototype rtu");
            }
            return rtuType;
        }
    }

    private String getProperty(String key) {
        return (String) properties.get(key);
    }

    private String toDuplicateSerialsErrorMsg(String serial) {
        return new MessageFormat(DUPLICATE_SERIALS).format(new Object[]{serial});
    }

    private void init(InputStream is, OutputStream os) throws IOException {

        configProgramChanges = -1;

        dlmsCache = new Cache();
        cosemObjectFactory = new CosemObjectFactory((ProtocolLink) this);
        meterConfig = DLMSMeterConfig.getInstance("ISK");
        ocm = new ObisCodeMapper(getCosemObjectFactory());

        try {
            if (connectionMode == 0) {
                dlmsConnection = new HDLCConnection(is, os, iHDLCTimeoutProperty, 100, iProtocolRetriesProperty, iClientMacAddress, iServerLowerMacAddress, iServerUpperMacAddress, addressingMode);
            } else {
                dlmsConnection = new TCPIPConnection(is, os, iHDLCTimeoutProperty, 100, iProtocolRetriesProperty, iClientMacAddress, iServerLowerMacAddress);
            }


            if (DialerMarker.hasOpticalMarker(this.link)) {
                ((HHUEnabler) this).enableHHUSignOn(this.link.getSerialCommunicationChannel());
            }


        } catch (DLMSConnectionException e) {
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Protected setter for the DLMSConnection.
     * Mostly used in test-cases
     *
     * @param connection - the {@link com.energyict.dlms.DLMSConnection} to set
     */
    protected void setDlmsConnection(DLMSConnection connection) {
        this.dlmsConnection = connection;
    }

    private void connect() throws IOException, DLMSConnectionException, SQLException, BusinessException {
        getDLMSConnection().connectMAC();

        dlmsConnection.setIskraWrapper(1);

        secureConnection = new SecureConnection(iSecurityLevelProperty,
                firmwareVersion, strPassword, getDLMSConnection());
        if (TESTLOGGING >= 1) {
            getLogger().log(Level.INFO, "GN - TESTLOG: Starting the Cache mechanism(checking if cache exists, reading cache if it doesn't exist)");
        }
        if (TESTLOGGING >= 1) {
            getLogger().log(Level.INFO, "GN - TESTLOG: Starting the Cache mechanism does not mean saving to disk.");
        }
        Object temp = GenericCache.startCacheMechanism(rtu);
        dlmsCache = (temp == null) ? new Cache() : (Cache) temp;
        collectCache();
        if (meterConfig.getCapturedObjectList() == null) {
            meterConfig.setInstantiatedObjectList(dlmsCache.getObjectList());
        }
        if (!verifyMeterID()) {
            throw new IOException("Iskra Mx37x, connect, Wrong DeviceID!, settings=" + strID + ", meter=" + getDeviceAddress());
        }
        if (!verifyMeterSerialNR()) {
            throw new IOException("Iskra Mx37x, connect, Wrong SerialNR!, settings=" + serialNumber + ", meter=" + getSerialNumber());
        }
        if (extendedLogging >= 1) {
            logger.info(getRegistersInfo(extendedLogging));
        }
    }

    private void disConnect() {
        try {
            if (secureConnection != null) {
                secureConnection.disConnect();
                getDLMSConnection().disconnectMAC();
            }
        } catch (DLMSConnectionException e) {
            logger.severe("DLMSLN: disconnect(), " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void mbusMeterDeletionCheck() throws SQLException, BusinessException, IOException {
        if (!((getMeter().getDownstreamRtus().size() == 0) && (getRtuType() == null))) {
            for (int i = 0; i < dlmsCache.getMbusCount(); i++) {
                if (rtuExists(dlmsCache.getMbusCustomerID(i))) {
                    mbusDevices[i] = new MbusDevice((int) dlmsCache.getMbusAddress(i), dlmsCache.getMbusPhysicalAddress(i), dlmsCache.getMbusCustomerID(i),
                            dlmsCache.getMbusMedium(i), findOrCreateNewMbusDevice(dlmsCache.getMbusCustomerID(i)), dlmsCache.getMbusUnit(i), getLogger());
                } else {
                    forcedMbusCheck = true;
                    break;
                }
            }
        }
    }

    /**
     * Checks if the Rtu with the serialnumber exists in database
     *
     * @param serial
     * @return true or false
     */
    protected boolean rtuExists(String serial) {
        List meterList = mw().getRtuFactory().findBySerialNumber(serial);
        if (meterList.size() == 1) {
            return true;
        } else if (meterList.size() == 0) {
            return false;
        } else {    // should never get here, no multiple serialNumbers can be allowed
            getLogger().severe(toDuplicateSerialsErrorMsg(serial));
        }
        return false;
    }

    private void collectCache() throws IOException, SQLException, BusinessException {
        int iConf = -1;
        getLogger().log(Level.INFO, "Reading configuration");

        try {
            if (dlmsCache.getObjectList() != null) {
                meterConfig.setInstantiatedObjectList(dlmsCache.getObjectList());
                mbusMeterDeletionCheck();

                try {
                    iConf = requestConfigurationProgramChanges();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    iConf = -1;
                    logger.severe("Iskra Mx37x: Configuration change is not accessible, request object list...");
                    requestObjectList();
                    checkMbusDevices();
                }

                if ((iConf != dlmsCache.getConfProgChange())) {

                    if (DEBUG >= 1) {
                        System.out.println("iConf=" + iConf + ", dlmsCache.getConfProgChange()=" + dlmsCache.getConfProgChange());
                    }

                    logger.severe("Iskra Mx37x: Configuration changed, request object list...");
                    requestObjectList();    // request object list again from rtu

                    if (DEBUG >= 1) {
                        System.out.println("after requesting objectlist (conf changed)... iConf=" + iConf + ", dlmsCache.getConfProgChange()=" + dlmsCache.getConfProgChange());
                    }
                }
                if (forcedMbusCheck) {    // you do not need to read the whole cache if you just changed the mbus meters
                    checkMbusDevices();
                }
            } else { // Cache not exist
                logger.info("Iskra Mx37x: Cache does not exist, request object list.");
                requestObjectList();
                checkMbusDevices();
                try {
                    iConf = requestConfigurationProgramChanges();

                    if (DEBUG >= 1) {
                        System.out.println("after requesting objectlist... iConf=" + iConf + ", dlmsCache.getConfProgChange()=" + dlmsCache.getConfProgChange());
                    }
                }
                catch (IOException e) {
                    iConf = -1;
                }
            }
        } finally {
            dlmsCache.saveObjectList(meterConfig.getInstantiatedObjectList());  // save object list in cache
            dlmsCache.setConfProgChange(iConf);  // set new configuration program change
            dlmsCache.setMbusParameters(mbusDevices);
        }
    }

    private Clock getClock() throws IOException {
        if (this.clock == null) {
            this.clock = getCosemObjectFactory().getClock();
        }
        return this.clock;
    }

    public Date getTime() throws IOException {
        return getClock().getDateTime();
    }

    private void setTime() throws ParseException, IOException {

        /* Don't worry about clock sets over interval boundaries, Iskra
        * will (probably) handle this.
        */
        Date cTime = getTime();

        Date now = new Date();

        this.timeDifference = (now.getTime() - cTime.getTime());
        long sAbsDiff = Math.abs(this.timeDifference) / 1000;

        getLogger().info("Difference between metertime and systemtime is " + this.timeDifference + " ms");

        long max = communicationProfile.getMaximumClockDifference();
        long min = communicationProfile.getMinimumClockDifference();

        if ((sAbsDiff < max) && (sAbsDiff > min)) {
            getLogger().log(Level.INFO, "Setting meterTime");
            setTimeClock();
        }
    }

    public void setTimeClock() throws IOException {
        doSetTime(Calendar.getInstance(getTimeZone()));
    }

    private void doSetTime(Calendar calendar) throws IOException {
        getClock().setTimeAttr(new DateTime(calendar));
    }

    private byte[] createByteDate(Calendar calendar) {
        byte[] byteStartDateBuffer = new byte[14];

        byteStartDateBuffer[0] = DLMSCOSEMGlobals.TYPEDESC_OCTET_STRING;
        byteStartDateBuffer[1] = 12; // length
        byteStartDateBuffer[2] = (byte) (calendar.get(calendar.YEAR) >> 8);
        byteStartDateBuffer[3] = (byte) calendar.get(calendar.YEAR);
        byteStartDateBuffer[4] = (byte) (calendar.get(calendar.MONTH) + 1);
        byteStartDateBuffer[5] = (byte) calendar.get(calendar.DAY_OF_MONTH);
        byte bDOW = (byte) calendar.get(calendar.DAY_OF_WEEK);
        byteStartDateBuffer[6] = bDOW-- == 1 ? (byte) 7 : bDOW;
        byteStartDateBuffer[7] = (byte) calendar.get(calendar.HOUR_OF_DAY);
        byteStartDateBuffer[8] = (byte) calendar.get(calendar.MINUTE);
        byteStartDateBuffer[9] = (byte) calendar.get(calendar.SECOND);
        byteStartDateBuffer[10] = (byte) 0x0; // hundreds of seconds

        byteStartDateBuffer[11] = (byte) (0x80);
        byteStartDateBuffer[12] = (byte) 0;

        if (getTimeZone().inDaylightTime(calendar.getTime())) {
            byteStartDateBuffer[13] = (byte) 0x80; //0x00;
        } else {
            byteStartDateBuffer[13] = (byte) 0x00; //0x00;
        }

        return byteStartDateBuffer;
    }

    public Calendar getToCalendar() {
        return ProtocolUtils.getCalendar(getTimeZone());
    }

    /**
     * @param channel
     * @return a Calendar object from the lastReading of the given channel, if the date is NULL,
     *         a date from one month ago is created at midnight.
     */
    public Calendar getFromCalendar(Channel channel) {
        Date lastReading = channel.getLastReading();
        if (lastReading == null) {
            lastReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(channel.getRtu());
        }
        Calendar cal = ProtocolUtils.getCleanCalendar(getTimeZone());
        cal.setTime(lastReading);
        return cal;
    }

    private boolean verifyMeterID() throws IOException {
        if ((strID == null) || ("".compareTo(strID) == 0) || (strID.compareTo(getDeviceAddress()) == 0)) {
            return true;
        } else {
            return false;
        }
    }

    public String getDeviceAddress() throws IOException {
        if (devID == null) {
            devID = getCosemObjectFactory().getGenericRead(deviceLogicalName, DLMSUtils.attrLN2SN(2), 1).getString();
        }
        return devID;
    }

    private boolean verifyMeterSerialNR() throws IOException {
        if ((serialNumber == null) || ("".compareTo(serialNumber) == 0) || (serialNumber.compareTo(getSerialNumber()) == 0)) {
            return true;
        } else {
            return false;
        }
    }

    public String getSerialNumber() throws IOException {
        if (serialnr == null) {
            UniversalObject uo = meterConfig.getSerialNumberObject();
            serialnr = getCosemObjectFactory().getGenericRead(uo).getString();
        }
        return serialnr;
    }

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        StringBuffer strBuff = new StringBuffer();
        Iterator it;

        // all total and rate values...
        strBuff.append("********************* All instantiated objects in the meter *********************\n");
        for (int i = 0; i < getMeterConfig().getInstantiatedObjectList().length; i++) {
            UniversalObject uo = getMeterConfig().getInstantiatedObjectList()[i];
            strBuff.append(uo.getObisCode().toString() + " " + uo.getObisCode().getDescription() + "\n");
        }

        strBuff.append("********************* Objects captured into load profile *********************\n");
        it = getCosemObjectFactory().getProfileGeneric(loadProfileObisCode).getCaptureObjects().iterator();
        while (it.hasNext()) {
            CapturedObject capturedObject = (CapturedObject) it.next();
            strBuff.append(capturedObject.getLogicalName().getObisCode().toString() + " " + capturedObject.getLogicalName().getObisCode().getDescription() + " (load profile)\n");
        }

        return strBuff.toString();
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        if (ocm == null) {
            ocm = new ObisCodeMapper(getCosemObjectFactory());
        }
        return ocm.getRegisterValue(obisCode);
    }

    public static ScalerUnit getScalerUnit(ObisCode obisCode) {

        if (obisCode.toString().indexOf("1.0") == 0) {
            return demandScalerUnits[ELECTRICITY];
        } else {
            return demandScalerUnits[MBUS];
        }
    }

    public int requestConfigurationProgramChanges() throws IOException {
        if (configProgramChanges == -1) {
            configProgramChanges = (int) getCosemObjectFactory().getCosemObject(getMeterConfig().getConfigObject().getObisCode()).getValue();
        }

        // check if the customerID from the meter matches the customerID from the cache
        if (!((getMeter().getDownstreamRtus().size() == 0) && (getRtuType() == null))) {
            String meterCustomerID;
            String customerID;
            for (int i = 0; i < MBUS_MAX; i++) {
                customerID = dlmsCache.getMbusCustomerID(i);
                meterCustomerID = getMbusSerial(mbusCustomerID[i]);
                if (customerID != null) {
                    if (!customerID.equalsIgnoreCase(meterCustomerID)) {
                        forcedMbusCheck = true;
                        break;
                    }
                } else {
                    if (!meterCustomerID.equals("")) {
                        forcedMbusCheck = true;
                        break;
                    }
                }
            }
        }

        return configProgramChanges;
    }

    protected String getMbusSerial(ObisCode oc) throws IOException {
        try {
            String str = "";
            byte[] data = getCosemObjectFactory().getData(oc).getRawValueAttr();
            byte[] parseStr = new byte[data.length - 2];
            System.arraycopy(data, 2, parseStr, 0, parseStr.length);
            if (com.energyict.genericprotocolimpl.common.ParseUtils.checkIfAllAreChars(parseStr)) {
                str = new String(parseStr);
            } else {
                str = com.energyict.genericprotocolimpl.common.ParseUtils.decimalByteToString(parseStr);
            }
            return str;
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Could not retrieve the MBus serialNumber");
        }
    }

    private void requestObjectList() throws IOException {
        meterConfig.setInstantiatedObjectList(getCosemObjectFactory().getAssociationLN().getBuffer());
    }

    public String requestAttribute(short sIC, byte[] LN, byte bAttr) throws IOException {
        return doRequestAttribute(sIC, LN, bAttr).print2strDataContainer();
    }

    private DataContainer doRequestAttribute(int classId, byte[] ln, int lnAttr) throws IOException {
        DataContainer dc = getCosemObjectFactory().getGenericRead(ObisCode.fromByteArray(ln), DLMSUtils.attrLN2SN(lnAttr), classId).getDataContainer();
        return dc;
    }

    public void addProperties(Properties properties) {
        this.properties = properties;
    }

    private void validateProperties() throws MissingPropertyException, InvalidPropertyException {
        try {
            Iterator iterator = getRequiredKeys().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (properties.getProperty(key) == null) {
                    throw new MissingPropertyException(key + " key missing");
                }
            }

            strID = rtu.getDeviceId();
            if ((strID != null) && (strID.length() > 16)) {
                throw new InvalidPropertyException("ID must be less or equal then 16 characters.");
            }

            strPassword = rtu.getPassword();
            iHDLCTimeoutProperty = Integer.parseInt(properties.getProperty("Timeout", "5000").trim());
            iProtocolRetriesProperty = Integer.parseInt(properties.getProperty("Retries", "10").trim());
            iSecurityLevelProperty = Integer.parseInt(properties.getProperty("SecurityLevel", "1").trim());
            iRequestTimeZone = Integer.parseInt(properties.getProperty("RequestTimeZone", "0").trim());
            iRoundtripCorrection = Integer.parseInt(properties.getProperty("RoundtripCorrection", "0").trim());
            iClientMacAddress = Integer.parseInt(properties.getProperty("ClientMacAddress", "100").trim());
            iServerUpperMacAddress = Integer.parseInt(properties.getProperty("ServerUpperMacAddress", "17").trim());
            iServerLowerMacAddress = Integer.parseInt(properties.getProperty("ServerLowerMacAddress", "1").trim());
            firmwareVersion = properties.getProperty("FirmwareVersion", "ANY");
            nodeId = rtu.getNodeAddress();
            serialNumber = rtu.getSerialNumber();
            extendedLogging = Integer.parseInt(properties.getProperty("ExtendedLogging", "0"));
            addressingMode = Integer.parseInt(properties.getProperty("AddressingMode", "2"));
            connectionMode = Integer.parseInt(properties.getProperty("Connection", "1")); // 0=HDLC, 1= TCP/IP
            rtuType = properties.getProperty("RtuType", "");
            TESTLOGGING = Integer.parseInt(properties.getProperty("TestLogging", "0"));
            csdCall = Integer.parseInt(properties.getProperty("CsdCall", "0"));

            genericEMeterProfile = ObisCode.fromString(properties.getProperty("ElectricityLoadProfile", "1.0.99.1.0.255"));
            genericMbusMeterProfile = ObisCode.fromString(properties.getProperty("MbusLoadProfile", "1.0.99.2.0.255"));
            genericDailyProfile = ObisCode.fromString(properties.getProperty("DailyLoadProfile", "1.0.98.2.0.255"));
            genericMonthlyProfile = ObisCode.fromString(properties.getProperty("MonthlyLoadProfile", "1.0.98.1.0.255"));


        }
        catch (NumberFormatException e) {

        }
    }

    public String getVersion() {
        return "$Date$";
    }

    public List getOptionalKeys() {
        List result = new ArrayList(21);
        result.add("Timeout");
        result.add("Retries");
        result.add("DelayAfterFail");
        result.add("RequestTimeZone");
        result.add("FirmwareVersion");
        result.add("SecurityLevel");
        result.add("ClientMacAddress");
        result.add("ServerUpperMacAddress");
        result.add("ServerLowerMacAddress");
        result.add("ExtendedLogging");
        result.add("LoadProfileId");
        result.add("AddressingMode");
        result.add("Connection");
        result.add("RtuType");
        result.add("TestLogging");
        result.add("FolderExtName");
        result.add("CsdCall");            // enable the csd call functionality
        result.add("PollTimeOut");        // timeout for polling the radius database
        result.add("IpPortNumber");        // portnumber for iskra meter (default 2048)
        result.add("CsdCallTimeOut");    // timeout between triggering the csd schedule and actually doing the schedule
        result.add("CsdPollFrequency"); // seconds between 2 request to the radius server
        result.add("FixedIpAddress");    // us the filled in ip address for csd calls
        result.add("ElectricityLoadProfile");
        result.add("MbusLoadProfile");
        result.add("DailyLoadProfile");
        result.add("MonthlyLoadProfile");
        result.add(NTASecurityProvider.NEW_LLS_SECRET);
        return result;
    }

    public List getRequiredKeys() {
        List result = new ArrayList();
//        result.add("Connection");
        return result;
    }

    public DLMSConnection getDLMSConnection() {
        return dlmsConnection;
    }

    public Logger getLogger() {
        return logger;
    }

    public DLMSMeterConfig getMeterConfig() {
        return meterConfig;
    }

    public int getReference() {
        return 0;
    }

    public int getRoundTripCorrection() {
        return iRoundtripCorrection;
    }

    public StoredValues getStoredValues() {
        return null;
    }

    public TimeZone getTimeZone() {
        try {
            return isRequestTimeZone() ? TimeZone.getTimeZone(Integer.toString(getClock().getTimeZone())) : rtu.getDeviceTimeZone();
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().log(Level.INFO, "Could not verify meterTimeZone so EIServer timeZone is used.");
            return rtu.getDeviceTimeZone();
        }
    }

    public boolean isRequestTimeZone() {
        return (this.iRequestTimeZone == 1) ? true : false;
    }

    public CosemObjectFactory getCosemObjectFactory() {
        return cosemObjectFactory;
    }

    public void setCosemObjectFactory(CosemObjectFactory cosemObjectFactory) {
        this.cosemObjectFactory = cosemObjectFactory;
    }

    public Object getCache() {
        return dlmsCache;
    }

    public String getFileName() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "_" + strID + "_" + iServerUpperMacAddress + "_IskraMx37x.cache";
    }

    public void setCache(Object cacheObject) {
        this.dlmsCache = (Cache) cacheObject;
    }

    //*******************************************************************************************
    //    M e s s a g e P r o t o c o l  i n t e r f a c e
    //     * @throws IOException
    //     * @throws SQLException
    //     * @throws BusinessException
    //*******************************************************************************************/

    /**
     * Handle the messages.
     * TODO first in line to refactor
     *
     * @throws IOException
     * @throws BusinessException
     * @throws SQLException
     */
    private void sendMeterMessages() throws IOException, BusinessException, SQLException {

        Iterator mi = rtu.getPendingMessages().iterator();
        if (mi.hasNext()) {
            getLogger().log(Level.INFO, "Handling messages for meter with serialnumber: " + rtu.getSerialNumber());
        }

        while (mi.hasNext()) {
            RtuMessage msg = (RtuMessage) mi.next();
            String msgString = msg.getContents();
            String contents = msgString.substring(msgString.indexOf("<") + 1, msgString.indexOf(">"));
            if (contents.endsWith("/")) {
                contents = contents.substring(0, contents.length() - 1);
            }
            BigDecimal breakerState = null;

            boolean disconnect = contents.equalsIgnoreCase(RtuMessageConstant.DISCONNECT_LOAD);
            boolean connect = contents.equalsIgnoreCase(RtuMessageConstant.CONNECT_LOAD);
            boolean ondemand = contents.equalsIgnoreCase(RtuMessageConstant.READ_ON_DEMAND);
            boolean threshpars = contents.equalsIgnoreCase(RtuMessageConstant.PARAMETER_GROUPID);
            boolean threshold = contents.equalsIgnoreCase(RtuMessageConstant.THRESHOLD_GROUPID);
            boolean thresholdcl = contents.equalsIgnoreCase(RtuMessageConstant.CLEAR_THRESHOLD);
            boolean falsemsg = contents.equalsIgnoreCase(RtuMessageConstant.THRESHOLD_STARTDT) || contents.equalsIgnoreCase(RtuMessageConstant.THRESHOLD_STOPDT);
            boolean tou = contents.equalsIgnoreCase(RtuMessageConstant.TOU_SCHEDULE);
            boolean apnUnPw = contents.equalsIgnoreCase(RtuMessageConstant.GPRS_APN) ||
                    contents.equalsIgnoreCase(RtuMessageConstant.GPRS_USERNAME) ||
                    contents.equalsIgnoreCase(RtuMessageConstant.GPRS_PASSWORD);
//            boolean gprsCred	= contents.equalsIgnoreCase(RtuMessageConstant.GPRS_MODEM_CREDENTIALS);
            boolean gprsCred = contents.indexOf(RtuMessageConstant.GPRS_MODEM_CREDENTIALS) == 0 ? true : false;
            boolean mbusInstall = contents.equalsIgnoreCase(RtuMessageConstant.MBUS_INSTALL);
            boolean mbusInstDR = contents.equalsIgnoreCase(RtuMessageConstant.MBUS_INSTALL_DATAREADOUT);
            boolean mbusRemove = contents.equalsIgnoreCase(RtuMessageConstant.MBUS_REMOVE);
            boolean wuChangeTimeOut = contents.equalsIgnoreCase(RtuMessageConstant.WAKEUP_INACT_TIMEOUT);
            boolean changeLLSSecret = contents.equalsIgnoreCase(RtuMessageConstant.AEE_CHANGE_LLS_SECRET);

            boolean wuAddWhiteList = false;
            for (int i = 0; i < maxNumbersCSDWhiteList; i++) {
                wuAddWhiteList |= contents.equalsIgnoreCase(RtuMessageConstant.WAKEUP_NR + (i + 1));
            }

            boolean wuAddManagedWhiteList = false;
            for (int i = 0; i < maxNumbersManagedWhiteList; i++) {
                wuAddManagedWhiteList |= contents.equalsIgnoreCase(RtuMessageConstant.WAKEUP_MANAGED_NR + (i + 1));
            }

            boolean wuActivate = contents.equalsIgnoreCase(RtuMessageConstant.WAKEUP_ACTIVATE);
            boolean firmware = contents.equalsIgnoreCase(RtuMessageConstant.FIRMWARE);
            boolean changeConMode = contents.equalsIgnoreCase(RtuMessageConstant.CONNECT_MODE);

            if (falsemsg) {
                msg.setFailed();
                AMRJournalManager amrJournalManager =
                        new AMRJournalManager(rtu, scheduler);
                amrJournalManager.journal(
                        new AmrJournalEntry(AmrJournalEntry.DETAIL, "No groupID was entered."));
                amrJournalManager.journal(new AmrJournalEntry(AmrJournalEntry.CC_UNEXPECTED_ERROR));
                amrJournalManager.updateRetrials();
                getLogger().severe("No groupID was entered.");
            }

            if (connect || disconnect) {
                if (disconnect) {
                    getLogger().log(Level.INFO, "Sending disconnect message for meter with serialnumber: " + rtu.getSerialNumber());
                    cosemObjectFactory.writeObject(breakerObisCode, 1, 2, disconnectMsg);
                    breakerState = readRegister(breakerObisCode).getQuantity().getAmount();
                }

                if (connect) {
                    getLogger().log(Level.INFO, "Sending connect message for meter with serialnumber: " + rtu.getSerialNumber());
                    cosemObjectFactory.writeObject(breakerObisCode, 1, 2, connectMsg);
                    breakerState = readRegister(breakerObisCode).getQuantity().getAmount();
                }

                switch (breakerState.intValue()) {

                    case 0: {
                        if (contents.indexOf(RtuMessageConstant.DISCONNECT_LOAD) != -1) {
                            msg.confirm();
                        } else {
                            msg.setFailed();
                        }
                    }
                    break;

                    case 1: {
                        if (contents.indexOf(RtuMessageConstant.CONNECT_LOAD) != -1) {
                            msg.confirm();
                        } else {
                            msg.setFailed();
                        }
                    }
                    break;

                    default: {
                        msg.setFailed();
                        break;
                    }
                }
            } else if (changeConMode) {
                changeConnectorMode(msg);
            } else if (tou) {
                sendActivityCalendar(contents, msg);
            } else if (ondemand) {
                onDemand(rtu, msg);
            } else if (threshpars) {
                thresholdParameters(msg);
            } else if (threshold) {
                applyThresholdValue(msg);
            } else if (thresholdcl) {
                clearThreshold(msg);
            } else if (apnUnPw) {
                changeApnUserNamePassword(msg);
            } else if (gprsCred) {
                changeGprsCredentials(msg);
            } else if (mbusInstall) {
                getCosemObjectFactory().getGenericInvoke(ObisCode.fromString("0.0.10.50.128.255"), 9, 1).invoke(new Unsigned16(0).getBEREncodedByteArray());

                msg.confirm();
            } else if (mbusRemove) {
                getCosemObjectFactory().getGenericInvoke(ObisCode.fromString("0.0.10.50.129.255"), 9, 1).invoke(new Unsigned16(0).getBEREncodedByteArray());

                clearMbusGateWays();

                msg.confirm();
            } else if (mbusInstDR) {
                getCosemObjectFactory().getGenericInvoke(ObisCode.fromString("0.0.10.50.130.255"), 9, 1).invoke(new Unsigned16(0).getBEREncodedByteArray());

                checkMbusDevices();    // we do this to update the ConcentratorGateway

                msg.confirm();
            } else if (wuAddWhiteList) {
                String description = "Adding numbers to whitelist for meter with serialnumber: " + rtu.getSerialNumber();
                getLogger().log(Level.INFO, description);
                try {
                    addPhoneToWhiteList(msg);
                } catch (BusinessException e) {
                    e.printStackTrace();
                    fail(e, msg, description);
                } catch (SQLException e) {
                    e.printStackTrace();
                    fail(e, msg, description);
                } catch (IOException e) {
                    e.printStackTrace();
                    fail(e, msg, description);
                }
            } else if (wuAddManagedWhiteList) {
                addPhoneToManagedList(msg);
            } else if (wuChangeTimeOut) {
                changeInactivityTimeout(msg);
            } else if (wuActivate) {
                activateWakeUp(msg);
            } else if (firmware) {
                upgradeFirmware(msg);
            } else if (changeLLSSecret) {
                changeLLSSecret(msg);
            } else {
                msg.setFailed();
            }
        }
    }

    private void changeLLSSecret(final RtuMessage msg) throws BusinessException, SQLException, IOException {
        String newLLSSecret = getProperty(NTASecurityProvider.NEW_LLS_SECRET);
        if (newLLSSecret == null) {
            fail(new InvalidPropertyException("Invalid new LLS secret property."), msg, "Invalid new LLS secret property");
        } else if (newLLSSecret.length() > 16) {
            fail(new InvalidPropertyException("Invalid length of the new LLS secret property, MAX 16 char long."), msg, "Invalid length of the new LLS secret property, MAX 16 char long.");
        } else {
            try {
                Data authKeyData = getCosemObjectFactory().getData(llsSecretObisCode4);
                authKeyData.setValueAttr(OctetString.fromString(newLLSSecret));
                authKeyData = getCosemObjectFactory().getData(llsSecretObisCode3);
                authKeyData.setValueAttr(OctetString.fromString(newLLSSecret));
                authKeyData = getCosemObjectFactory().getData(llsSecretObisCode2);
                authKeyData.setValueAttr(OctetString.fromString(newLLSSecret));
                authKeyData = getCosemObjectFactory().getData(llsSecretObisCode1);
                authKeyData.setValueAttr(OctetString.fromString(newLLSSecret));
                msg.confirm();
//                for (int i = 4; i > 0; i--) {
//                    ObisCode oc = new ObisCode(llsSecretObisCode.getA(), llsSecretObisCode.getB(), llsSecretObisCode.getC(), llsSecretObisCode.getD(), i, llsSecretObisCode.getF());
//                    Data authKeyData = getCosemObjectFactory().getData(oc);
//                    authKeyData.setValueAttr(OctetString.fromString(newLLSSecret));
//                }
            } catch (Exception e) {
                fail(e, msg, "Could not write all the necessary LLS keys.");
            }
        }
    }

    private void changeConnectorMode(RtuMessage msg) throws BusinessException, SQLException, IOException {
        String description = "Changing the connectorMode for meter with serialnumber: " + rtu.getSerialNumber();

        getLogger().log(Level.INFO, description);

        String mode = getMessageValue(msg.getContents(), RtuMessageConstant.CONNECT_MODE);
        if (ParseUtils.isInteger(mode)) {

            Data dataMode = getCosemObjectFactory().getData(ObisCode.fromString("0.0.128.30.22.255"));
            dataMode.setValueAttr(new Unsigned8(Integer.parseInt(mode)));
            msg.confirm();

        } else {
            fail(new NumberFormatException(), msg, description);
        }


    }

    /**
     * NOTE: Updating the gateway of an RTU with NULL is not compatible with EIServer 7.x!!
     *
     * @throws SQLException      if a database exception occurred
     * @throws BusinessException if a business exception occurred
     */
    private void clearMbusGateWays() throws SQLException, BusinessException {
        List slaves = getMeter().getDownstreamRtus();
        Iterator it = slaves.iterator();
        while (it.hasNext()) {
            Rtu slave = (Rtu) it.next();
//			slave.updateGateway(null);
            RtuShadow shadow = slave.getShadow();
            shadow.setGatewayId(0);
            slave.update(shadow);
        }
    }

    private void upgradeFirmware(RtuMessage msg) throws BusinessException, SQLException {
        String description = "Upgrade firmware for meter with serialnumber: " + rtu.getSerialNumber();

        try {
            getLogger().log(Level.INFO, description);
            String id = getMessageValue(msg.getContents(), RtuMessageConstant.FIRMWARE);

            UserFile uf = mw().getUserFileFactory().find(Integer.parseInt(id));
            if (uf != null) {

                byte[] imageData = uf.loadFileInByteArray();
                ImageTransfer it = new ImageTransfer(this);
                it.upgradeImage(imageData);

                msg.confirm();

            } else {
                throw new IOException("No userfile found with id " + id);
            }

        } catch (Exception e) {
            e.printStackTrace();
            fail(e, msg, description);
        }
    }

    private void clearWhiteList(RtuMessage msg) throws BusinessException, SQLException {
        String description = "Clear whitelist for meter with serialnumber: " + rtu.getSerialNumber();

        try {
            getLogger().log(Level.INFO, description);
            AutoConnect autoConnect = getCosemObjectFactory().getAutoConnect();
            autoConnect.clearWhiteList();

            byte[] b = new byte[]{(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff
                    , (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
            getCosemObjectFactory().getGenericWrite(ObisCode.fromString("0.0.128.20.20.255"), 2, 1).write(OctetString.fromByteArray(b).getBEREncodedByteArray());

            msg.confirm();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e, msg, description);
        }
    }

    /**
     * Set the gsm mode to GSM.
     * Values:
     * - 0 : GSM
     * - 1 : GSM/PPP
     * - 2 : GPRS
     *
     * @param msg
     * @throws SQLException
     * @throws BusinessException
     */
    private void activateWakeUp(RtuMessage msg) throws IOException, BusinessException, SQLException {
        try {
            Unsigned8 gsmMode = new Unsigned8(0);
            getCosemObjectFactory().getGenericWrite(ObisCode.fromString("0.0.128.20.10.255"), 2, 1).write(gsmMode.getBEREncodedByteArray());

            msg.confirm();
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("Could not write the GSM mode");
        }
    }

    private void changeGeneralPhoneRestriction(RtuMessage msg) throws BusinessException, SQLException {
        String description = "Changing Incomming calls general restriction for meter with serialnumber: " + rtu.getSerialNumber();

        try {
            getLogger().log(Level.INFO, description);
            String restriction = getMessageValue(msg.getContents(), RtuMessageConstant.WAKEUP_GENERAL_RESTRICTION);
            if (!ParseUtils.isInteger(restriction)) {
                throw new NumberFormatException("Value for timeout is not a number.");
            } else {
                Unsigned8 restrict = new Unsigned8(Integer.valueOf(restriction));
                getCosemObjectFactory().getGenericWrite(ObisCode.fromString("0.0.128.20.21.255"), 2, 1).write(restrict.getBEREncodedByteArray());
                msg.confirm();
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e, msg, description);
        }
    }

    private void changeInactivityTimeout(RtuMessage msg) throws BusinessException, SQLException {
        String description = "Changing inactivity timeout for meter with serialnumber: " + rtu.getSerialNumber();

        try {
            getLogger().log(Level.INFO, description);
            String timeout = getMessageValue(msg.getContents(), RtuMessageConstant.WAKEUP_INACT_TIMEOUT);
            if (!ParseUtils.isInteger(timeout)) {
                throw new NumberFormatException("Value for timeout is not a number.");
            } else {
                TCPUDPSetup tcpUdpSetup = getCosemObjectFactory().getTCPUDPSetup();
                tcpUdpSetup.writeInactivityTimeout(Integer.parseInt(timeout));
                msg.confirm();
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e, msg, description);
        }
    }

    /**
     * Set the Managed numbers for the whitelist to the meter.
     * These numbers are allowed to set up a telnet session
     *
     * @param msg - the message containing the numbers
     * @throws BusinessException if we failed to create an AMR journal entry
     * @throws SQLException      if we failed to create an AMR journal entry
     */
    private void addPhoneToManagedList(RtuMessage msg) throws BusinessException, SQLException {
        String description = "Adding Managed numbers to whitelist for meter with serialnumber: " + rtu.getSerialNumber();

        try {
            getLogger().log(Level.INFO, description);
            AutoConnect autoConnect = getCosemObjectFactory().getAutoConnect();
            byte[] restrictions = getCosemObjectFactory().getData(ObisCode.fromString("0.0.128.20.20.255")).getValueAttr().getOctetString().getOctetStr();
            Array list = getCosemObjectFactory().getAutoConnect().readDestinationList();    // the list from the meter
            Array newList = new Array();                                                    // the new list

            // copy the CSD numbers to the new list
            for (int i = 0; i < maxNumbersCSDWhiteList; i++) {
                if (i < list.nrOfDataTypes()) {
                    newList.addDataType(list.getDataType(i));
                } else {
                    newList.addDataType(OctetString.fromString(""));
                }
            }
            int offset = maxNumbersCSDWhiteList; //offset for managed numbers in the restriction list
            for (int i = 0; i < maxNumbersManagedWhiteList; i++) {
                if (!"".equalsIgnoreCase(getMessageValue(msg.getContents(), RtuMessageConstant.WAKEUP_MANAGED_NR + (i + 1)))) {
                    newList.addDataType(OctetString.fromString(getMessageValue(msg.getContents(), RtuMessageConstant.WAKEUP_MANAGED_NR + (i + 1))));
                    restrictions[i + offset] = (byte) 0x02;
                } else {
                    newList.addDataType(OctetString.fromString(""));
                    restrictions[i + offset] = (byte) 0x00;
                }
            }
            autoConnect.writeDestinationList(newList);
            getCosemObjectFactory().getGenericWrite(ObisCode.fromString("0.0.128.20.20.255"), 2, 1).write(OctetString.fromByteArray(restrictions).getBEREncodedByteArray());

            msg.confirm();
        } catch (Exception e) {
            e.printStackTrace();
            fail(e, msg, description);
        }

    }

    /**
     * Set the numbers from the whitelist to the meter.
     * These numbers are allowed to make a CSD call to the meter
     *
     * @param msg - the message containing the numbers
     * @throws BusinessException if we failed to create an AMR journal entry
     * @throws SQLException      if we failed to create an AMR journal entry
     */
    protected void addPhoneToWhiteList(RtuMessage msg) throws BusinessException, SQLException, IOException {
        AutoConnect autoConnect = getCosemObjectFactory().getAutoConnect();
        byte[] restrictions = getCosemObjectFactory().getData(ObisCode.fromString("0.0.128.20.20.255")).getValueAttr().getOctetString().getOctetStr();
        Array list = getCosemObjectFactory().getAutoConnect().readDestinationList();    // the list from the meter
        Array newList = new Array();                                                    // the new list
        for (int i = 0; i < maxNumbersCSDWhiteList; i++) {
            if (!"".equalsIgnoreCase(getMessageValue(msg.getContents(), RtuMessageConstant.WAKEUP_NR + (i + 1)))) {
                newList.addDataType(OctetString.fromString(getMessageValue(msg.getContents(), RtuMessageConstant.WAKEUP_NR + (i + 1))));
                restrictions[i] = (byte) 0x03;
            } else {
                newList.addDataType(OctetString.fromString(""));
                restrictions[i] = (byte) 0x00;
            }
        }

        for (int i = 0; i < maxNumbersManagedWhiteList; i++) {
            if ((i + maxNumbersCSDWhiteList) <= list.nrOfDataTypes()) {
                newList.addDataType(list.getDataType(i + maxNumbersCSDWhiteList));
            }
        }

        autoConnect.writeDestinationList(newList);
        getCosemObjectFactory().getGenericWrite(ObisCode.fromString("0.0.128.20.20.255"), 2, 1).write(OctetString.fromByteArray(restrictions).getBEREncodedByteArray());

        msg.confirm();
    }

    private void changeApnUserNamePassword(RtuMessage msg) throws BusinessException, SQLException {
        String description = "Changing apn/username/password for meter with serialnumber: " + rtu.getSerialNumber();

        try {
            getLogger().log(Level.INFO, description);
            String apn = getMessageValue(msg.getContents(), RtuMessageConstant.GPRS_APN);
            if (apn.equalsIgnoreCase("")) {
                throw new ApplicationException("APN value is required for message " + msg.displayString());
            }
            String userName = getMessageValue(msg.getContents(), RtuMessageConstant.GPRS_USERNAME);
            String pass = getMessageValue(msg.getContents(), RtuMessageConstant.GPRS_PASSWORD);

            PPPAuthenticationType pppat = getCosemObjectFactory().getPPPSetup().new PPPAuthenticationType();
            pppat.setAuthenticationType(PPPSetup.LCPOptionsType.AUTH_PAP);
            pppat.setUserName(userName);
            pppat.setPassWord(pass);

            getCosemObjectFactory().getPPPSetup().writePPPAuthenticationType(pppat);

            getCosemObjectFactory().getGPRSModemSetup().writeAPN(apn);

            msg.confirm();

        } catch (Exception e) {
            e.printStackTrace();
            fail(e, msg, description);
        }

    }

    private void changeGprsCredentials(RtuMessage msg) throws BusinessException, SQLException {
        String description = "Changing gprs credentials for meter with serialnumber: " + rtu.getSerialNumber();

        try {
            getLogger().log(Level.INFO, description);
            String userName = getMessageAttribute(msg.getContents(), RtuMessageConstant.GPRS_USERNAME);
            String pass = getMessageAttribute(msg.getContents(), RtuMessageConstant.GPRS_PASSWORD);

            PPPAuthenticationType pppat = getCosemObjectFactory().getPPPSetup().new PPPAuthenticationType();
            pppat.setAuthenticationType(PPPSetup.LCPOptionsType.AUTH_PAP);
            pppat.setUserName(userName);
            pppat.setPassWord(pass);

            getCosemObjectFactory().getPPPSetup().writePPPAuthenticationType(pppat);

            msg.confirm();

        } catch (Exception e) {
            e.printStackTrace();
            fail(e, msg, description);
        }

    }


    private void clearThreshold(RtuMessage msg) throws BusinessException, SQLException {
        String description = "Clear threshold for meter with serialnumber: " + rtu.getSerialNumber();
        try {
            getLogger().log(Level.INFO, description);
            String groupID = getMessageValue(msg.getContents(), RtuMessageConstant.CLEAR_THRESHOLD);
            if (groupID.equalsIgnoreCase("")) {
                throw new BusinessException("No groupID was entered.");
            }

            int grID = 0;

            try {
                grID = Integer.parseInt(groupID);
                crGroupIDMsg[1] = (byte) (grID >> 8);
                crGroupIDMsg[2] = (byte) grID;

            } catch (NumberFormatException e) {
                throw new BusinessException("Invalid groupID");
            }

//	    	String startDate = "";
//	    	String stopDate = "";
            Calendar startCal = null;
            Calendar stopCal = null;

            startCal = Calendar.getInstance(getTimeZone());
            stopCal = startCal;

            long crDur = (Math.abs(stopCal.getTimeInMillis() - startCal.getTimeInMillis())) / 1000;
            crDurationMsg[1] = (byte) (crDur >> 24);
            crDurationMsg[2] = (byte) (crDur >> 16);
            crDurationMsg[3] = (byte) (crDur >> 8);
            crDurationMsg[4] = (byte) crDur;
            byte[] byteDate = createByteDate(startCal);

            getLogger().log(Level.INFO, description);
            getCosemObjectFactory().writeObject(crGroupID, 1, 2, crGroupIDMsg);
            getCosemObjectFactory().writeObject(crStartDate, 1, 2, byteDate);
            getCosemObjectFactory().writeObject(crDuration, 3, 2, crDurationMsg);

            msg.confirm();

        } catch (Exception e) {
            fail(e, msg, description);
        }
    }

    private void applyThresholdValue(RtuMessage msg) throws BusinessException, SQLException {
        String description = "Setting threshold value for meter with serialnumber: " + rtu.getSerialNumber();
        try {
            getLogger().log(Level.INFO, description);
            String groupID = getMessageValue(msg.getContents(), RtuMessageConstant.THRESHOLD_GROUPID);
            if (groupID.equalsIgnoreCase("")) {
                throw new BusinessException("No groupID was entered.");
            }

            int grID = 0;

            try {
                grID = Integer.parseInt(groupID);
                crGroupIDMsg[1] = (byte) (grID >> 8);
                crGroupIDMsg[2] = (byte) grID;

            } catch (NumberFormatException e) {
                throw new BusinessException("Invalid groupID");
            }

            String startDate = "";
            String stopDate = "";
            Calendar startCal = null;
            Calendar stopCal = null;

            startDate = getMessageValue(msg.getContents(), RtuMessageConstant.THRESHOLD_STARTDT);
            stopDate = getMessageValue(msg.getContents(), RtuMessageConstant.THRESHOLD_STOPDT);
            startCal = (startDate.equalsIgnoreCase("")) ? Calendar.getInstance(getTimeZone()) : getCalendarFromString(startDate);
            if (stopDate.equalsIgnoreCase("")) {
                stopCal = Calendar.getInstance();
                stopCal.setTime(startCal.getTime());
                stopCal.add(Calendar.YEAR, 1);
            } else {
                stopCal = getCalendarFromString(stopDate);
            }

            long crDur = (Math.abs(stopCal.getTimeInMillis() - startCal.getTimeInMillis())) / 1000;
            crDurationMsg[1] = (byte) (crDur >> 24);
            crDurationMsg[2] = (byte) (crDur >> 16);
            crDurationMsg[3] = (byte) (crDur >> 8);
            crDurationMsg[4] = (byte) crDur;
            byte[] byteDate = createByteDate(startCal);

            getLogger().log(Level.INFO, description);
            getCosemObjectFactory().writeObject(crGroupID, 1, 2, crGroupIDMsg);
            getCosemObjectFactory().writeObject(crStartDate, 1, 2, byteDate);
            getCosemObjectFactory().writeObject(crDuration, 3, 2, crDurationMsg);

            msg.confirm();

        } catch (Exception e) {
            fail(e, msg, description);
        }
    }

    private void thresholdParameters(RtuMessage msg) throws BusinessException, SQLException {
        String description = "Sending threshold configuration for meter with serialnumber: " + rtu.getSerialNumber();
        try {
            getLogger().log(Level.INFO, description);
            String groupID = getMessageValue(msg.getContents(), RtuMessageConstant.PARAMETER_GROUPID);
            if (groupID.equalsIgnoreCase("")) {
                throw new BusinessException("No groupID was entered.");
            }

            String thresholdPL = getMessageValue(msg.getContents(), RtuMessageConstant.THRESHOLD_POWERLIMIT);
            String contractPL = getMessageValue(msg.getContents(), RtuMessageConstant.CONTRACT_POWERLIMIT);
            if ((thresholdPL.equalsIgnoreCase("")) && (contractPL.equalsIgnoreCase(""))) {
                throw new BusinessException("Neighter contractual nor threshold limit was given.");
            }

            long conPL = 0;
            long limit = 0;
            int grID = -1;

            try {
                grID = Integer.parseInt(groupID);
                crMeterGroupIDMsg[1] = (byte) (grID >> 8);
                crMeterGroupIDMsg[2] = (byte) grID;

                if (!contractPL.equalsIgnoreCase("")) {
                    conPL = Integer.parseInt(contractPL);
                    contractPowerLimitMsg[1] = (byte) (conPL >> 24);
                    contractPowerLimitMsg[2] = (byte) (conPL >> 16);
                    contractPowerLimitMsg[3] = (byte) (conPL >> 8);
                    contractPowerLimitMsg[4] = (byte) conPL;
                }

                if (!thresholdPL.equalsIgnoreCase("")) {
                    limit = Integer.parseInt(thresholdPL);
                    crPowerLimitMsg[1] = (byte) (limit >> 24);
                    crPowerLimitMsg[2] = (byte) (limit >> 16);
                    crPowerLimitMsg[3] = (byte) (limit >> 8);
                    crPowerLimitMsg[4] = (byte) limit;
                }

            } catch (NumberFormatException e) {
                throw new BusinessException("Invalid groupID");
            }
            getLogger().log(Level.INFO, description);
            getCosemObjectFactory().writeObject(crMeterGroupID, 1, 2, crMeterGroupIDMsg);
            if (!contractPL.equalsIgnoreCase("")) {
                getCosemObjectFactory().writeObject(contractPowerLimit, 3, 2, contractPowerLimitMsg);
            }
            if (!thresholdPL.equalsIgnoreCase("")) {
                getCosemObjectFactory().writeObject(crPowerLimit, 3, 2, crPowerLimitMsg);
            }

            msg.confirm();

        } catch (Exception e) {
            fail(e, msg, description);
        }

    }

    protected void onDemand(Rtu rtu, RtuMessage msg) throws IOException, SQLException, BusinessException {
        String description = "Getting ondemand registers for meter with serialnumber: " + rtu.getSerialNumber();
        try {
            getLogger().log(Level.INFO, description);
            MeterReadingData mrd = new MeterReadingData();
            Iterator i = rtu.getRtuType().getRtuRegisterSpecs().iterator();
            while (i.hasNext()) {
                try {

                    RtuRegisterSpec spec = (RtuRegisterSpec) i.next();
                    ObisCode oc = spec.getObisCode();
                    RtuRegister register = rtu.getRegister(oc);

                    if (register != null) {

                        if (oc.getF() == 255) {
                            RegisterValue rv = readRegister(oc);
                            rv.setRtuRegisterId(register.getId());
                            mrd.add(rv);
                        }
                    } else {
                        String obis = oc.toString();
                        String msgError = "Register " + obis + " not defined on device";
                        getLogger().info(msgError);
                    }

                } catch (NoSuchRegisterException e) {
                    // absorb
                }
            }
            rtu.store(mrd);
            msg.confirm();
        }
        catch (Exception e) {
            fail(e, msg, description);
        }
    }

    protected void fail(Exception e, RtuMessage msg, String description) throws BusinessException, SQLException {
        msg.setFailed();
        AMRJournalManager amrJournalManager =
                new AMRJournalManager(rtu, scheduler);
        amrJournalManager.journal(
                new AmrJournalEntry(AmrJournalEntry.DETAIL, description + ": " + e.toString()));
        amrJournalManager.journal(new AmrJournalEntry(AmrJournalEntry.CC_UNEXPECTED_ERROR));
        amrJournalManager.updateRetrials();
        getLogger().severe(e.toString());
    }

    public void sendActivityCalendar(String contents, RtuMessage msg) throws SQLException, BusinessException, IOException {
        String description =
                "Sending new Tariff Program message to meter with serialnumber: " + rtu.getSerialNumber();
        try {
            getLogger().log(Level.INFO, description);
            UserFile userFile = getUserFile(msg.getContents());

            ActivityCalendar activityCalendar =
                    getCosemObjectFactory().getActivityCalendar(ObisCode.fromString("0.0.13.0.0.255"));

            com.energyict.genericprotocolimpl.common.tou.ActivityCalendar calendarData =
                    new com.energyict.genericprotocolimpl.common.tou.ActivityCalendar();
            ActivityCalendarReader reader = new IskraActivityCalendarReader(calendarData, getTimeZone(), getMeter().getTimeZone());
            calendarData.setReader(reader);
            calendarData.read(new ByteArrayInputStream(userFile.loadFileInByteArray()));
            CosemActivityCalendarBuilder builder = new
                    CosemActivityCalendarBuilder(calendarData);

            activityCalendar.writeCalendarNamePassive(builder.calendarNamePassive());
            activityCalendar.writeSeasonProfilePassive(builder.seasonProfilePassive());
            activityCalendar.writeWeekProfileTablePassive(builder.weekProfileTablePassive());
            activityCalendar.writeDayProfileTablePassive(builder.dayProfileTablePassive());
            if (calendarData.getActivatePassiveCalendarTime() != null) {
                activityCalendar.writeActivatePassiveCalendarTime(builder.activatePassiveCalendarTime());
            } else {
                activityCalendar.activateNow();
            }

            // check if xml file contains special days
            int newSpecialDays = calendarData.getSpecialDays().size();
            if (newSpecialDays > 0) {
                SpecialDaysTable specialDaysTable =
                        getCosemObjectFactory().getSpecialDaysTable(ObisCode.fromString("0.0.11.0.0.255"));
                // delete old special days
                Array array = specialDaysTable.readSpecialDays();
                int currentMaxSpecialDayIndex = array.nrOfDataTypes();
                for (int i = newSpecialDays; i < currentMaxSpecialDayIndex; i++) {
                    calendarData.addDummyDay(i);
                }
                specialDaysTable.writeSpecialDays(builder.specialDays());
            }
            msg.confirm();
        }
        catch (Exception e) {
            fail(e, msg, description);
        }
    }

    protected UserFile getUserFile(String contents) throws IOException {
        int id = getTouFileId(contents);
        UserFile userFile =
                MeteringWarehouse.getCurrent().getUserFileFactory().find(id);
        if (userFile == null) {
            throw new IOException("No userfile found with id " + id);
        }
        return userFile;
    }

    protected int getTouFileId(String contents) throws IOException {
        int startIndex = 2 + RtuMessageConstant.TOU_SCHEDULE.length();  // <TOU>
        int endIndex = contents.indexOf("</" + RtuMessageConstant.TOU_SCHEDULE + ">");
        String value = contents.substring(startIndex, endIndex);
        try {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e) {
            throw new IOException("Invalid userfile id: " + value);
        }
    }

    private Calendar getCalendarFromString(String strDate) throws IOException {
        try {
            Calendar cal = Calendar.getInstance(getTimeZone());
            cal.set(Calendar.DATE, Integer.parseInt(strDate.substring(0, strDate.indexOf("/"))));
            cal.set(Calendar.MONTH, (Integer.parseInt(strDate.substring(strDate.indexOf("/") + 1, strDate.lastIndexOf("/")))) - 1);
            cal.set(Calendar.YEAR, Integer.parseInt(strDate.substring(strDate.lastIndexOf("/") + 1, strDate.indexOf(" "))));

            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(strDate.substring(strDate.indexOf(" ") + 1, strDate.indexOf(":"))));
            cal.set(Calendar.MINUTE, Integer.parseInt(strDate.substring(strDate.indexOf(":") + 1, strDate.lastIndexOf(":"))));
            cal.set(Calendar.SECOND, Integer.parseInt(strDate.substring(strDate.lastIndexOf(":") + 1, strDate.length())));
            cal.clear(Calendar.MILLISECOND);
            return cal;
        }
        catch (NumberFormatException e) {
            throw new IOException("Invalid dateTime format for the applyThreshold message.");
        }

    }

    public String getMessageValue(String msgStr, String str) {
        try {
            return msgStr.substring(msgStr.indexOf(str + ">") + str.length()
                    + 1, msgStr.indexOf("</" + str));
        } catch (Exception e) {
            return "";
        }
    }

    public String getMessageAttribute(String messageValue, String attributeName) {
        String result;
        try {
            int ptr = messageValue.indexOf(" " + attributeName);
            if (ptr == -1) {
                throw new IOException("Unable to find the attribute with name '" + attributeName + "'.");
            }
            result = messageValue.substring(ptr + attributeName.length() + 1);
            ptr = result.indexOf('"');
            if (ptr == -1) {
                throw new IOException("Unable to find the opening '\"' for the attribute with name '" + attributeName + "'.");
            }
            result = result.substring(ptr + 1);
            ptr = result.indexOf('"');
            if (ptr == -1) {
                throw new IOException("Unable to find the closing '\"' for the attribute with name '" + attributeName + "'.");
            }
            result = result.substring(0, ptr);
        } catch (Exception e) {
            result = "";
        }
        return result;
    }

    public List getMessageCategories() {
        List theCategories = new ArrayList();
        MessageCategorySpec cat = new MessageCategorySpec(RtuMessageCategoryConstants.BASICMESSAGES);
        MessageCategorySpec catLoadLimit = new MessageCategorySpec(RtuMessageCategoryConstants.LOADLIMIT);
        MessageCategorySpec catMbus = new MessageCategorySpec(RtuMessageCategoryConstants.MBUSMESSAGES);
        MessageCategorySpec catWakeUp = new MessageCategorySpec(RtuMessageCategoryConstants.WAKEUPFUNCTIONALITY);
        MessageCategorySpec catFirmware = new MessageCategorySpec(RtuMessageCategoryConstants.FIRMWARE);
        MessageCategorySpec catAuthenticationEncryption = getAuthEncryptCategory();

        MessageSpec msgSpec = addBasicMsg("Disconnect meter", RtuMessageConstant.DISCONNECT_LOAD, false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addBasicMsg("Connect meter", RtuMessageConstant.CONNECT_LOAD, false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addValueMessage("ConnectControl Mode", RtuMessageConstant.CONNECT_MODE, false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addBasicMsg("ReadOnDemand", RtuMessageConstant.READ_ON_DEMAND, false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addTouMessage("Set new tariff program", RtuMessageConstant.TOU_SCHEDULE, false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addGPRSModemSetup("Change GPRS Modem setup", RtuMessageConstant.GPRS_MODEM_SETUP, false);
        cat.addMessageSpec(msgSpec);
        msgSpec = addGPRSModemCredantials("Change GPRS Modem credentials", RtuMessageConstant.GPRS_MODEM_CREDENTIALS, false);
        cat.addMessageSpec(msgSpec);

        msgSpec = addThresholdParameters(RtuMessageKeyIdConstants.LOADLIMITCONFIG, RtuMessageConstant.THRESHOLD_PARAMETERS, false);
        catLoadLimit.addMessageSpec(msgSpec);
        msgSpec = addThresholdMessage("Apply LoadLimiting", RtuMessageConstant.APPLY_THRESHOLD, false);
        catLoadLimit.addMessageSpec(msgSpec);
        msgSpec = addClearThresholdMessage(RtuMessageKeyIdConstants.LOADLIMITCLEAR, RtuMessageConstant.CLEAR_THRESHOLD, false);
        catLoadLimit.addMessageSpec(msgSpec);

        msgSpec = addBasicMsg("Install MBus device", RtuMessageConstant.MBUS_INSTALL, false);
        catMbus.addMessageSpec(msgSpec);
        msgSpec = addBasicMsg("Remove MBus device", RtuMessageConstant.MBUS_REMOVE, false);
        catMbus.addMessageSpec(msgSpec);
        msgSpec = addBasicMsg("Install-DataReadout Mbus device", RtuMessageConstant.MBUS_INSTALL_DATAREADOUT, false);
        catMbus.addMessageSpec(msgSpec);

        msgSpec = addValueMessage("Change inactivity timeout", RtuMessageConstant.WAKEUP_INACT_TIMEOUT, false);
        catWakeUp.addMessageSpec(msgSpec);
        msgSpec = addWhiteListPhoneNumbers("Add numbers to white list", RtuMessageConstant.WAKEUP_ADD_WHITELIST, false);
        catWakeUp.addMessageSpec(msgSpec);
        msgSpec = addManagedListPhoneNumbers("Add Managed numbers to the white list", RtuMessageConstant.WAKEUP_ADD_WHITELIST, false);
        catWakeUp.addMessageSpec(msgSpec);
        msgSpec = addBasicMsg("Activate wakeup mechanism", RtuMessageConstant.WAKEUP_ACTIVATE, false);
        catWakeUp.addMessageSpec(msgSpec);
        // TODO not complete yet!
//        msgSpec = addValueMessage("Upgrade Firmware", RtuMessageConstant.FIRMWARE, false);
//        catFirmware.addMessageSpec(msgSpec);

        theCategories.add(cat);
        theCategories.add(catLoadLimit);
        theCategories.add(catMbus);
        theCategories.add(catWakeUp);
        theCategories.add(catAuthenticationEncryption);
        //TODO
        //TODO	The FirmwareMessage is disabled for the current release, it's not complete yet!
        //TODO
        //TODO
//        theCategories.add(catFirmware);
        return theCategories;
    }

    /**
     * Create three messages, one to change the <b>globalKey</b>, one to change the
     * <b>AuthenticationKey</b>, and the other one to change
     * the <b>HLSSecret</b>
     *
     * @return a category with four MessageSpecs for Authenticate/Encrypt functionality
     */
    public MessageCategorySpec getAuthEncryptCategory() {
        MessageCategorySpec catAuthEncrypt = new MessageCategorySpec(
                RtuMessageCategoryConstants.AUTHENTICATEENCRYPT);
        MessageSpec msgSpec = addBasicMsg(RtuMessageKeyIdConstants.CHANGELLSSECRET,
                RtuMessageConstant.AEE_CHANGE_LLS_SECRET, false);
        catAuthEncrypt.addMessageSpec(msgSpec);
        return catAuthEncrypt;
    }

    private MessageSpec addValueMessage(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        return msgSpec;

    }

    private MessageSpec addClearThresholdMessage(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(RtuMessageConstant.CLEAR_THRESHOLD);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addGPRSModemSetup(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(RtuMessageConstant.GPRS_APN);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        tagSpec = new MessageTagSpec(RtuMessageConstant.GPRS_USERNAME);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        tagSpec = new MessageTagSpec(RtuMessageConstant.GPRS_PASSWORD);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addGPRSModemCredantials(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        MessageValueSpec msgVal = new MessageValueSpec();
        msgVal.setValue(" ");
        MessageAttributeSpec msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.GPRS_USERNAME, false);
        tagSpec.add(msgAttrSpec);
        msgAttrSpec = new MessageAttributeSpec(RtuMessageConstant.GPRS_PASSWORD, false);
        tagSpec.add(msgAttrSpec);
        tagSpec.add(msgVal);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addThresholdParameters(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(RtuMessageConstant.PARAMETER_GROUPID);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        tagSpec = new MessageTagSpec(RtuMessageConstant.THRESHOLD_POWERLIMIT);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        tagSpec = new MessageTagSpec(RtuMessageConstant.CONTRACT_POWERLIMIT);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addManagedListPhoneNumbers(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec;

        for (int i = 0; i < 8; i++) {
            tagSpec = new MessageTagSpec(RtuMessageConstant.WAKEUP_MANAGED_NR + (i + 1));
            tagSpec.add(new MessageValueSpec());
            msgSpec.add(tagSpec);
        }
        return msgSpec;

    }

    private MessageSpec addWhiteListPhoneNumbers(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec;

        for (int i = 0; i < 8; i++) {
            tagSpec = new MessageTagSpec(RtuMessageConstant.WAKEUP_NR + (i + 1));
            tagSpec.add(new MessageValueSpec());
            msgSpec.add(tagSpec);
        }
        return msgSpec;
    }

    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    public String writeTag(MessageTag msgTag) {
        StringBuffer buf = new StringBuffer();

        // a. Opening tag
        buf.append("<");
        buf.append(msgTag.getName());

        // b. Attributes
        for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext();) {
            MessageAttribute att = (MessageAttribute) it.next();
            if (att.getValue() == null || att.getValue().length() == 0) {
                continue;
            }
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        if (msgTag.getSubElements().isEmpty()) {
            buf.append("/>");
            return buf.toString();
        }
        buf.append(">");
        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext();) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag()) {
                buf.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if (value == null || value.length() == 0) {
                    return "";
                }
                buf.append(value);
            }
        }

        // d. Closing tag
        buf.append("</");
        buf.append(msgTag.getName());
        buf.append(">");

        return buf.toString();
    }

    public String writeValue(MessageValue msgValue) {
        return msgValue.getValue();
    }

    private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addTouMessage(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addThresholdMessage(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(RtuMessageConstant.THRESHOLD_GROUPID);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        tagSpec = new MessageTagSpec(RtuMessageConstant.THRESHOLD_STARTDT);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        tagSpec = new MessageTagSpec(RtuMessageConstant.THRESHOLD_STOPDT);
        tagSpec.add(new MessageValueSpec());
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    public Rtu getMeter() {
        return this.rtu;
    }

    public ObisCode getMbusLoadProfile(int address) {
        return mbusLProfileObisCode[address];
    }

    public ObisCode getDailyLoadProfile() {
        return dailyObisCode;
    }

    public ObisCode getMonthlyLoadProfile() {
        return monthlyObisCode;
    }

    public long getTimeDifference() {
        return this.timeDifference;
    }

    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    /**
     * Used by the framework
     *
     * @param commChannel communication channel object
     * @param datareadout enable or disable data readout
     * @throws com.energyict.dialer.connection.ConnectionException
     *          thrown when a connection exception happens
     */
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        HHUSignOn hhuSignOn =
                (HHUSignOn) new IEC1107HHUConnection(commChannel, this.iHDLCTimeoutProperty, this.iProtocolRetriesProperty, 300, 0);
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.enableDataReadout(datareadout);
        getDLMSConnection().setHHUSignOn(hhuSignOn, this.nodeId);
    }

    /**
     * Getter for the data readout
     *
     * @return byte[] with data readout
     */
    public byte[] getHHUDataReadout() {
        return getDLMSConnection().getHhuSignOn().getDataReadout();
    }

}
