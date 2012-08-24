package com.energyict.genericprotocolimpl.iskrap2lpc;

import com.energyict.cbo.*;
import com.energyict.cpo.Environment;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.genericprotocolimpl.common.GenericCache;
import com.energyict.genericprotocolimpl.common.ParseUtils;
import com.energyict.genericprotocolimpl.iskrap2lpc.Concentrator.XmlException;
import com.energyict.genericprotocolimpl.iskrap2lpc.stub.*;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.amr.RtuRegisterSpec;
import com.energyict.mdw.amrimpl.RtuRegisterReadingImpl;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.ProtocolChannel;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.mbus.core.ValueInformationfieldCoding;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import org.apache.axis.types.*;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.rpc.ServiceException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Meter handling:
 * - find or create meter
 * - read meter
 * - export message
 * Transaction: all operations for a meter fail or all succeed.
 * <p/>
 * NOTE:
 * In several methods you will see an IF-ELSE structure with a TESTING variable, this is only necessary for UnitTesting so we can actually
 * store meterData in the database, sometimes it is used to set configuration which we normally should have read from the meter.
 * <p/>
 * changes:
 * <p/>
 * GNA |26032009| Mantis 4170: Fixed duplicate MBus serialnumber issue
 */
public class MeterReadTransaction implements CacheMechanism {

//	1.8.0+9:2.8.0+9:1.8.1+9d:1.8.2+9d:2.8.1+9d:2.8.2+9d:1.8.1+9m:1.8.2+9m:2.8.1+9m:2.8.2+9m

    protected boolean TESTING = false;
    protected boolean DEBUG = false;
    protected String billingMonthly = "";
    protected String billingDaily = "";
    private String[] profileTestName;

    static final int ELECTRICITY = 0x00;
    static final int MBUS = 0x01;
    static final int MBUS_MAX = 0x04;

    private final Concentrator concentrator;

    /**
     * Cached Objects
     */
    public int confProgChange;
    public int loadProfilePeriod1;
    public int loadProfilePeriod2;
    public boolean changed;
    public ObjectDef[] loadProfileConfig1;
    public ObjectDef[] loadProfileConfig2;
    public ObjectDef[] loadProfileConfig3;
    public ObjectDef[] loadProfileConfig4;
    public CosemDateTime billingReadTime;
    public CosemDateTime captureObjReadTime;
    public ResultsFile resultsFile;

    private ProtocolChannelMap protocolChannelMap = null;
    private CommunicationProfile communicationProfile;
    private StoreObject storeObject;
    public Rtu rtuConcentrator;
    private Rtu meter;
    private String serial;
    private String mSerial;
    private Cache dlmsCache;
    private boolean initCheck = false;
    private boolean forcedMbusCheck = false;
    private boolean useParameters = false;

    protected MbusDevice[] mbusDevices = {null, null, null, null};
    private String[] requestedMbusSerials = {"", "", "", ""};

    public MeterReadTransaction(Concentrator concentrator, Rtu rtuConcentrator, String serial, CommunicationProfile communicationProfile) {

        this.concentrator = concentrator;
        this.rtuConcentrator = rtuConcentrator;
        this.serial = serial;
        this.communicationProfile = communicationProfile;
        this.storeObject = new StoreObject();
        this.dlmsCache = new Cache();
    }

    /**
     * Controls the while meter reading
     *
     * @throws BusinessException
     * @throws SQLException
     */
    public void doExecute() throws BusinessException, SQLException {
        boolean succes = false;

        XmlHandler dataHandler;        // the dataHandler constructs the loadProfile as well as the billing profiles with the given channelMap

        try {

            meter = findOrCreate(rtuConcentrator, serial);

            if (getMeter() != null) {

//            	doTheCheckMethods();	// enable this for quick cache reading
//            	readRawMbusFrame();
//            	readDLCMode();
//            	readValveState();

                // Import profile
                if (communicationProfile.getReadDemandValues()) {
                    dataHandler = new XmlHandler(getLogger(), getChannelMap());
                    dataHandler.setChannelUnit(Unit.get(BaseUnit.WATTHOUR, 3));
                    importProfile(meter, dataHandler, communicationProfile.getReadMeterEvents());
                }

                // Import Daily and Monthly registers
                if (communicationProfile.getReadMeterReadings()) {
                    dataHandler = new XmlHandler(getLogger(), getChannelMap());
                    dataHandler.setDailyMonthlyProfile(true);
                    dataHandler.setChannelUnit(Unit.get(BaseUnit.WATTHOUR, 3));
                    importDailyMonthly(getMeter(), dataHandler, serial);
                    dataHandler.setDailyMonthlyProfile(false);
                }

                // Send messages
                if (communicationProfile.getSendRtuMessage()) {
                    dataHandler = new XmlHandler(getLogger(), getChannelMap());
                    sendMeterMessages(getMeter(), dataHandler);
                }

                if (mbusCheck()) {
                    getLogger().log(Level.INFO, "Starting to handle the MBus meters.");
                    handleMBusMeter();
                }

                succes = true;

            }

        } catch (ServiceException thrown) {
            getConcentrator().severe(thrown, thrown.getMessage());
            thrown.printStackTrace();
            throw new BusinessException(thrown); /* roll back */

        } catch (NumberFormatException thrown) {
            getConcentrator().severe(thrown, thrown.getMessage());
            thrown.printStackTrace();
            throw new BusinessException(thrown); /* roll back */

        } catch (InvalidPropertyException thrown) {
            getConcentrator().severe(thrown, thrown.getMessage());
            thrown.printStackTrace();
            throw new BusinessException(thrown); /* roll back */

        } catch (IOException thrown) {
            getConcentrator().severe(thrown, thrown.getMessage());
            thrown.printStackTrace();
            throw new BusinessException(thrown); /* roll back */

        } finally {
            if (succes) {
                Environment.getDefault().execute(getStoreObjects());
                getLogger().log(Level.INFO, "Meter with serialnumber " + serial + " has completely finished");
            }
        }
    }

    /**
     * Only for testing
     */
    private void readRawMbusFrame() {
        String[] times = prepareCosemGetRequest();
        try {
            byte[] b = getConnection().cosemGetRequest(serial, times[0], times[1], "0.2.128.50.0.255", new UnsignedInt(4), new UnsignedInt(0));
            System.out.println("cosemGetRequest:");
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BusinessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Only for testing
     */
    private void readValveState() {
        String[] times = prepareCosemGetRequest();
        try {
            byte[] b = getConnection().cosemGetRequest(serial, times[0], times[1], Constant.valveState.toString(), new UnsignedInt(1), new UnsignedInt(2));
            System.out.println("ValveState:");
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BusinessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Only for testing
     */
    private void readDLCMode() {
        String[] times = prepareCosemGetRequest();
        try {
            byte[] b = getConnection().cosemGetRequest(serial, times[0], times[1], Constant.dlcRepeaterMode.toString(), new UnsignedInt(1), new UnsignedInt(2));
            System.out.println(b);
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ServiceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BusinessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Handles all the MBus meters.
     * If a meter fails, we log it and go to the next one.
     */
    private void handleMBusMeter() {
        for (int i = 0; i < MBUS_MAX; i++) {
            if (mbusDevices[i] != null) {
                try {
                    mbusDevices[i].setMeterReadTransaction(this);
                    mbusDevices[i].execute(getConcentrator().getCommunicationScheduler(), null, null);
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

    /**
     * @return the E-meter rtu
     */
    public Rtu getMeter() {
        return meter;
    }

    /**
     * Set the E-meter rtu
     *
     * @param meter
     */
    protected void setMeter(Rtu meter) {
        this.meter = meter;
    }

    /**
     * Reads:
     * (1) ProfileData
     * (2) If events enabled -> Events
     *
     * @param meter
     * @param dataHandler
     * @param bEvents
     * @throws ServiceException
     * @throws IOException
     * @throws BusinessException
     * @throws SQLException
     */
    protected void importProfile(Rtu meter, XmlHandler dataHandler, boolean bEvents) throws ServiceException, IOException, BusinessException, SQLException {

        String xml = null;
        String profile = null;
        String mtr = getMeter().getSerialNumber();

        String from = Constant.getInstance().format(new Date());
        Date toDate = new Date();
        String to = Constant.getInstance().format(toDate);

        String lpString1 = "99.1.0";
        String lpString2 = "99.2.0";

        /*
        * Read profile data
        */
        getLogger().log(Level.INFO, "Reading PROFILE from meter with serialnumber " + meter.getSerialNumber() + ".");

        Channel chn;
        for (int i = 0; i < dataHandler.getChannelMap().getNrOfProtocolChannels(); i++) {

            ProtocolChannel pc = dataHandler.getChannelMap().getProtocolChannel(i);
            xml = "";
            chn = getMeterChannelWithIndex(meter, i + 1);
            if (chn != null) {
                from = Constant.getInstance().format(getLastChannelReading(chn));
                if (!pc.containsDailyValues() && !pc.containsMonthlyValues()) {

                    profile = getConcentrator().getLpElectricity();

                    dataHandler.setProfileChannelIndex(i);
                    getLogger().log(Level.INFO, "Retrieving profiledata from " + from + " to " + to);
                    xml = getConnection().getMeterProfile(getMeter().getSerialNumber(), profile, pc.getRegister(), from, to);

                }
            }
            if (!xml.equalsIgnoreCase("")) {
                dataHandler.setChannelIndex(i);
                getConcentrator().importData(xml, dataHandler);
            }
        }

        getLogger().log(Level.INFO, "Done reading PROFILE.");

        /*
        * Read logbook
        */
        if (bEvents) {

            getLogger().log(Level.INFO, "Reading EVENTS from meter with serialnumber " + mtr + ".");

            from = Constant.getInstance().format(getLastLogboog(getMeter()));
            String events, powerFailures;
            getLogger().log(Level.INFO, "Retrieving events from " + from + " to " + to);
            events = getConnection().getMeterEvents(mtr, from, to);
            powerFailures = getConnection().getMeterPowerFailures(mtr, from, to);
            getConcentrator().importData(events, dataHandler);
            getConcentrator().importData(powerFailures, dataHandler);

            getLogger().log(Level.INFO, "Done reading EVENTS.");
        }

        // if complete profile is read, check if there are values in the future and then store it.
        ProfileData pd = dataHandler.getProfileData();
        ParseUtils.validateProfileData(pd, toDate);

        getStoreObjects().add(meter, pd);
    }

    /**
     * Reads the monthly and daily values of the meter
     *
     * @param meter
     * @param dataHandler
     * @param serialNumber
     * @throws BusinessException
     * @throws ServiceException
     * @throws IOException
     * @throws SQLException
     */
    protected void importDailyMonthly(Rtu meter, XmlHandler dataHandler, String serialNumber) throws BusinessException, ServiceException, IOException, SQLException {
        getLogger().log(Level.INFO, "Reading Daily/Monthly values from meter with serialnumber " + meter.getSerialNumber() + ".");
        String xml = "";
        String daily = getConcentrator().getLpDaily();
        String monthly = getConcentrator().getLpMonthly();

        String from = Constant.getInstance().format(new Date());
        Date toDate = new Date();
        String to = Constant.getInstance().format(toDate);

        try {
            Channel chn;
            ProtocolChannel pc;
            ProtocolChannelMap channelMap = dataHandler.getChannelMap();
            dataHandler.setChannelIndex(0);        // we will add channel per channel
            for (int i = 0; i < channelMap.getNrOfProtocolChannels(); i++) {
                pc = channelMap.getProtocolChannel(i);
                xml = "";
                chn = getMeterChannelWithIndex(meter, i + 1);
                dataHandler.setProfileChannelIndex(i);
                if (chn != null) {
                    from = Constant.getInstance().format(getLastChannelReading(chn));
                    if (pc.containsDailyValues()) {
                        if (chn.getInterval().getTimeUnitCode() == TimeDuration.DAYS) {
                            getLogger().log(Level.INFO, "Reading Daily values with registername: " + pc.getRegister() + " from " + from + " to " + to);
                            xml = getConnection().getMeterProfile(getMeter().getSerialNumber(), daily, pc.getRegister(), from, to);
                        } else {
                            throw new IOException("Channelconfiguration of channel \"" + chn + "\" is different from the channelMap");
                        }
                    } else if (pc.containsMonthlyValues()) {
                        if (chn.getInterval().getTimeUnitCode() == TimeDuration.MONTHS) {
                            getLogger().log(Level.INFO, "Reading Monthly values with registername: " + pc.getRegister() + " from " + from + " to " + to);
                            xml = getConnection().getMeterProfile(getMeter().getSerialNumber(), monthly, pc.getRegister(), from, to);
                        } else {
                            throw new IOException("Channelconfiguration of channel \"" + chn + "\" is different from the channelMap");
                        }
                    }
                } else {
                    throw new IOException("Channel out of bound exception: no channel with profileIndex " + i + 1 + " is configured on the meter.");
                }

                if (!xml.equalsIgnoreCase("")) {


                    getConcentrator().importData(xml, dataHandler);
                    ProfileData pd = dataHandler.getDailyMonthlyProfile();
                    pd = sortOutProfileData(pd, pc);
                    // if complete profile is read, check if there are values in the future and then store it.
                    ParseUtils.validateProfileData(pd, toDate);
                    getStoreObjects().add(chn, pd);
                    dataHandler.clearDailyMonthlyProfile();
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        } catch (ServiceException e) {
            e.printStackTrace();
            throw new ServiceException(e);
        }
    }

    /**
     * Method to check if a profileData object contains more than just the monthly or daily values.
     * If values where added that are not daily or monthly billings, then we delete those because it is a cumulative value
     *
     * @param pd - profileData
     * @param pc - protocolChannel
     * @return the profileData object without the overhead values
     */
    public ProfileData sortOutProfileData(ProfileData pd, ProtocolChannel pc) {
        ProfileData profileData = new ProfileData();
        profileData.setChannelInfos(pd.getChannelInfos());
        Iterator it = pd.getIntervalIterator();
        while (it.hasNext()) {
            IntervalData id = (IntervalData) it.next();
            if (pc.containsDailyValues()) {
                if (checkDailyBillingTime(id.getEndTime())) {
                    profileData.addInterval(id);
                }
            } else if (pc.containsMonthlyValues()) {
                if (checkMonthlyBillingTime(id.getEndTime())) {
                    profileData.addInterval(id);
                }
            }
        }
        profileData.sort();
        return profileData;
    }

    /**
     * Checks if the given date is a date at midnight
     *
     * @param date
     * @return true or false
     */
    private boolean checkDailyBillingTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (cal.get(Calendar.HOUR) == 0 && cal.get(Calendar.MINUTE) == 0 && cal.get(Calendar.SECOND) == 0 && cal.get(Calendar.MILLISECOND) == 0) {
            return true;
        }
        return false;
    }

    /**
     * Checks if the given data is a date at midnight on the first of the month
     *
     * @param date
     * @return true or false
     */
    private boolean checkMonthlyBillingTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        if (checkDailyBillingTime(date) && cal.get(Calendar.DAY_OF_MONTH) == 1) {
            return true;
        }
        return false;
    }

    /**
     * @param meter
     * @param profileIndex
     * @return the channel of the meter with the given profileIndex
     */
    public Channel getMeterChannelWithIndex(Rtu meter, int profileIndex) {
        Iterator it = meter.getChannels().iterator();
        while (it.hasNext()) {
            Channel chn = (Channel) it.next();
            if (chn.getLoadProfileIndex() == profileIndex) {
                return chn;
            }
        }
        return null;
    }

    /**
     * Store the registers from the dataHandler in the StoreObject
     *
     * @param dataHandler containing the imported registers
     * @param meter       on which the registers have to be stored
     * @throws ServiceException
     * @throws BusinessException
     * @throws SQLException
     */
    protected void handleRegisters(XmlHandler dataHandler, Rtu meter) throws ServiceException, BusinessException, SQLException {

        Iterator i = dataHandler.getMeterReadingData().getRegisterValues().iterator();
        while (i.hasNext()) {
            RegisterValue registerValue = (RegisterValue) i.next();
            RtuRegister register = meter.getRegister(registerValue.getObisCode());

            if (register != null) {
                if (register.getReadingAt(registerValue.getReadTime()) == null) {
                    getStoreObjects().add(register, registerValue);
                }
            } else {
                String obis = registerValue.getObisCode().toString();
                String msg = "Register " + obis + " not defined on device";
                getLogger().info(msg);
            }
        }
    }

    /**
     * Just for Testing method ot get a cosemDateTime from an xml string
     *
     * @param xml
     * @return
     */
    protected CosemDateTime getCosemDateTimeFromXmlString(String xml) {
        CosemDateTime cdt = null;
        try {
            Element topElement = getConcentrator().toDom(xml).getDocumentElement();
            UnsignedShort year = new UnsignedShort(topElement.getElementsByTagName("Year").item(0).getFirstChild().getTextContent());
            UnsignedByte month = new UnsignedByte(topElement.getElementsByTagName("Month").item(0).getFirstChild().getTextContent());
            UnsignedByte dayOfMonth = new UnsignedByte(topElement.getElementsByTagName("DayOfMonth").item(0).getFirstChild().getTextContent());
            UnsignedByte dayOfWeek = new UnsignedByte(topElement.getElementsByTagName("DayOfWeek").item(0).getFirstChild().getTextContent());
            UnsignedByte hour = new UnsignedByte(topElement.getElementsByTagName("Hour").item(0).getFirstChild().getTextContent());
            UnsignedByte minute = new UnsignedByte(topElement.getElementsByTagName("Minute").item(0).getFirstChild().getTextContent());
            cdt = new CosemDateTime(year, month, dayOfMonth, dayOfWeek, hour, minute);
        } catch (XmlException e) {
            e.printStackTrace();
        }
        return cdt;
    }

    /**
     * Returns the date of the last register in the given list
     *
     * @param registerValues
     * @return a date
     */
    private Date getLastRegisterDate(List registerValues) {
        Date lastDate = ((RtuRegisterReadingImpl) registerValues.get(0)).getToTime();
        Iterator it = registerValues.iterator();
        while (it.hasNext()) {
            Date dateRrri = ((RtuRegisterReadingImpl) it.next()).getToTime();
            if (dateRrri.after(lastDate)) {
                lastDate = dateRrri;
            }
        }
        return lastDate;
    }

    /**
     * Returns the lastReading date of the given channel, if the date is null we turn it into the current date minus a year at midnight.
     *
     * @param chn
     * @return a date
     */
    protected Date getLastChannelReading(Channel chn) {
        Date result = chn.getLastReading();
        if (result == null) {
            result = getClearLastMonthDate(chn.getRtu());
        }
        return result;
    }

    /**
     * Returns the lastReading of the rtu, if the date is null we turn it into the current date minus a year at midnight.
     *
     * @param rtu
     * @return a date
     */
    protected Date getLastReading(Rtu rtu) {
        Date result = rtu.getLastReading();
        if (result == null) {
            result = getClearLastMonthDate(rtu);
        }
        return result;
    }

    /**
     * Returns the lastLogbook of the rtu, if the date is null we turn it into the current date minus a year at midnight.
     *
     * @param rtu
     * @return a date
     */
    protected Date getLastLogboog(Rtu rtu) {
        Date result = rtu.getLastLogbook();
        if (result == null) {
            result = getClearLastMonthDate(rtu);
        }
        return result;
    }

    /**
     * Returns the current date minus a year at midnight.
     *
     * @param rtu
     * @return the current date minus a year at midnight.
     */
    private Date getClearLastMonthDate(Rtu rtu) {
        Calendar tempCalendar = Calendar.getInstance(rtu.getDeviceTimeZone());
        tempCalendar.add(Calendar.MONTH, -1);
        tempCalendar.set(Calendar.HOUR_OF_DAY, 0);
        tempCalendar.set(Calendar.MINUTE, 0);
        tempCalendar.set(Calendar.SECOND, 0);
        tempCalendar.set(Calendar.MILLISECOND, 0);
        return tempCalendar.getTime();
    }

    /**
     * @return the channelmap of the meter
     * @throws InvalidPropertyException when the property is not configured
     * @throws BusinessException        if the property is configured but when it is null
     */
    public ProtocolChannelMap getChannelMap() throws InvalidPropertyException, BusinessException {
        if (protocolChannelMap == null) {
            String sChannelMap = (String) getMeter().getProperties().getProperty(Constant.CHANNEL_MAP);
            if (sChannelMap != null) {
                protocolChannelMap = new ProtocolChannelMap(sChannelMap);
            } else {
                throw new BusinessException("No channelmap configured on the meter, meter will not be handled.");
            }
        }
        return protocolChannelMap;
    }

    /**
     * Read the firmwareversion of the meter
     *
     * @param meterID
     * @param oc      lets you choose for the Module or the Core firmware version
     * @return a nicely build firmwareversionString
     * @throws NumberFormatException
     * @throws ServiceException
     * @throws IOException
     * @throws BusinessException
     */
    private String getFirmwareVersions(String meterID, ObisCode oc) throws NumberFormatException, ServiceException, IOException, BusinessException {
        String times[] = prepareCosemGetRequest();
        byte[] strCore = getConnection().cosemGetRequest(meterID, times[0], times[1], oc.toString(), new UnsignedInt(1), new UnsignedInt(2));
        byte[] convertStr = new byte[strCore.length - 2];
        System.arraycopy(strCore, 2, convertStr, 0, convertStr.length);
        return ParseUtils.decimalByteToString(convertStr);
    }

    /**
     * Read the meterTime
     *
     * @return the current time of the meter
     * @throws NumberFormatException
     * @throws RemoteException
     * @throws ServiceException
     * @throws IOException
     * @throws BusinessException
     */
    private Date getTime() throws NumberFormatException, RemoteException, ServiceException, IOException, BusinessException {
        String times[] = prepareCosemGetRequest();
        byte[] strTime = getConnection().cosemGetRequest(serial, times[0], times[1], "0.0.1.0.0.255", new UnsignedInt(8), new UnsignedInt(2));
        return getResponceCalendar(strTime).getTime();
    }

    /**
     * Copy of the DLMS Clock method to parse a dlms clock object, unfortunately we don't have an instance of the dlms protocolLink ...
     *
     * @param responseData
     * @return
     */
    private Calendar getResponceCalendar(byte[] responseData) {
        Calendar gcalendarMeter = null;

        int status = (int) responseData[13] & 0xFF;
        if (status != 0xFF) {
            gcalendarMeter = ProtocolUtils.initCalendar((responseData[13] & (byte) 0x80) == (byte) 0x80, meter.getTimeZone());
        } else {
            gcalendarMeter = ProtocolUtils.getCleanCalendar(meter.getTimeZone());
        }

        int year = (int) ProtocolUtils.getShort(responseData, 2) & 0x0000FFFF;
        if (year != 0xFFFF) {
            gcalendarMeter.set(gcalendarMeter.YEAR, year);
        }

        int month = (int) responseData[4] & 0xFF;
        if (month != 0xFF) {
            gcalendarMeter.set(gcalendarMeter.MONTH, month - 1);
        }

        int date = (int) responseData[5] & 0xFF;
        if (date != 0xFF) {
            gcalendarMeter.set(gcalendarMeter.DAY_OF_MONTH, date);
        }

        int hour = (int) responseData[7] & 0xFF;
        if (hour != 0xFF) {
            gcalendarMeter.set(gcalendarMeter.HOUR_OF_DAY, hour);
        }

        int minute = (int) responseData[8] & 0xFF;
        if (minute != 0xFF) {
            gcalendarMeter.set(gcalendarMeter.MINUTE, minute);
        }

        int seconds = (int) responseData[9] & 0xFF;
        if (seconds != 0xFF) {
            gcalendarMeter.set(gcalendarMeter.SECOND, seconds);
        }

        gcalendarMeter.set(gcalendarMeter.MILLISECOND, 0);

        return gcalendarMeter;
    }

    public static void main(String[] args) {

//		byte[] b1 = {50, 56, 48, 48, 52, 48, 48, 48, 55, 51, 51, 48, 50, 55, 53, 48, 55};
//		byte[] b2 = {7, 1, 67, 7};
//		System.out.println("b1 = " + ParseUtils.checkIfAllAreChars(b1));
//		System.out.println("b2 = " + ParseUtils.checkIfAllAreChars(b2));

        byte[] b = {9, 12, 7, -40, 10, 15, 3, 16, 14, 42, 0, -1, -120, -128};
        MeterReadTransaction mrt = new MeterReadTransaction(null, null, null, null);
        System.out.println(mrt.getResponceCalendar(b).getTime());
    }

    /**
     * Checks if the Rtu with the serialnumber exists in database
     *
     * @param serial
     * @return true or false
     */
    protected boolean rtuExists(String serial) {
        List meterList = getConcentrator().mw().getRtuFactory().findBySerialNumber(serial);
        if (meterList.size() == 1) {
            return true;
        } else if (meterList.size() == 0) {
            return false;
        } else {    // should never get here, no multiple serialNumbers can be allowed
            getLogger().severe(toDuplicateSerialsErrorMsg(serial));
        }
        return false;
    }

    /**
     * Check if the given serialnumber exist in the database, if not and there is an RtuType configured, then you can create it.
     *
     * @param concentrator
     * @param serial
     * @return an rtu or NULL
     * @throws SQLException
     * @throws BusinessException
     * @throws IOException
     */
    protected Rtu findOrCreate(Rtu concentrator, String serial) throws SQLException, BusinessException, IOException {

        List meterList = getConcentrator().mw().getRtuFactory().findBySerialNumber(serial);

        if (meterList.size() == 1) {
            Rtu rtu = (Rtu) meterList.get(0);
            // Check if gateway has changed, and update if it has
            if ((rtu.getGateway() == null) || (rtu.getGateway().getId() != concentrator.getId())) {
                rtu.updateGateway(concentrator);
            }
            return rtu;
        } else if (meterList.size() > 1) {
            getLogger().severe(toDuplicateSerialsErrorMsg(serial));
            return null;
        }

        if (getConcentrator().getRtuType(concentrator) != null) {
            return createMeter(concentrator, getConcentrator().getRtuType(concentrator), serial);
        } else {
            getLogger().severe(Constant.NO_AUTODISCOVERY);
            return null;
        }
    }

    /**
     * Check if the given serialnumber exist in the database, if not and there is an RtuType configured with a lookuptable, then check if there is a prototype for the medium.
     * If there is a prototype, then you can create the meter
     *
     * @param concentrator
     * @param serial
     * @param medium
     * @return
     * @throws SQLException
     * @throws BusinessException
     * @throws IOException
     */
    protected Rtu findOrCreate(Rtu concentrator, String serial, int medium) throws SQLException, BusinessException, IOException {

        List meterList = getConcentrator().mw().getRtuFactory().findBySerialNumber(serial);

        if (meterList.size() == 1) {
            Rtu rtu = (Rtu) meterList.get(0);
            // Check if gateway has changed, and update if it has
            if ((rtu.getGateway() == null) || (rtu.getGateway().getId() != concentrator.getId())) {
                rtu.updateGateway(concentrator);
            }
            return rtu;
        } else if (meterList.size() > 1) {
            getLogger().severe(toDuplicateSerialsErrorMsg(serial));
            return null;
        }

        if (getMbusRtuType(medium) != null) {
            return createMeter(concentrator, getMbusRtuType(medium), serial);
        } else {
            getLogger().severe(Constant.NO_AUTODISCOVERY);
            return null;
        }
    }

    private RtuType getMbusRtuType(int medium) throws IOException {
        String lookup = (String) meter.getProperties().getProperty(Constant.RTU_TYPE);
        String type = "";
        if (lookup != null) {
            Lookup lut = getConcentrator().mw().getLookupFactory().find(lookup);
            if (lut == null) {
                throw new IOException("No lookuptable defined with name '" + lookup + "'");
            } else {
                type = lut.getValue(medium);
                if ((type != null) && !type.equalsIgnoreCase("")) {
                    RtuType rtuType = getConcentrator().mw().getRtuTypeFactory().find(type);
                    if (rtuType == null) {
                        throw new IOException("Iskra Mx37x, There is no prototype defined for the MBus medium with code '" + type + "'");
                    }
                    if (rtuType.getPrototypeRtu() == null) {
                        throw new IOException("Iskra Mx37x, rtutype '" + type + "' has no prototype rtu");
                    }
                    return rtuType;
                } else {
                    throw new IOException("No RtuType defined in lookuptable '" + lookup + "' for the value '" + medium + "'");
                }
            }
        } else {
            getLogger().warning("No automatic MBusMeter creation: RtuType has no LookupTable defined.");
        }
        return null;
    }

    /**
     * @param concentrator
     * @return the folderID of the given rtu
     */
    private String getFolderID(Rtu concentrator) {
        String folderid = (String) concentrator.getProperties().getProperty(Constant.FOLDER_EXT_NAME);
        return folderid;
    }

    /**
     * Create a meter for configured RtuType
     *
     * @throws BusinessException
     * @throws SQLException
     */

    private Rtu createMeter(Rtu concentrator, RtuType type, String serial) throws SQLException, BusinessException {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -10);
        Date lastreading = cal.getTime();

        RtuShadow shadow = type.newRtuShadow();

        shadow.setName(serial);
        shadow.setSerialNumber(serial);

        String folderExtName = getFolderID(concentrator);
        if (folderExtName != null) {
            Folder result = getConcentrator().mw().getFolderFactory().findByExternalName(folderExtName);
            if (result != null) {
                shadow.setFolderId(result.getId());
            } else {
                getLogger().log(Level.INFO, "No folder found with external name: " + folderExtName + ", new meter will be placed in prototype folder.");
            }
        } else {
            getLogger().log(Level.INFO, "New meter will be placed in prototype folder.");
        }

        shadow.setGatewayId(concentrator.getId());
        shadow.setLastReading(lastreading);
        return getConcentrator().mw().getRtuFactory().create(shadow);

    }

    private String toDuplicateSerialsErrorMsg(String serial) {
        return new MessageFormat(Constant.DUPLICATE_SERIALS)
                .format(new Object[]{serial});
    }

    /**
     * @return the logger from the concentrator
     */
    public Logger getLogger() {
        return getConcentrator().getLogger();
    }

    /**
     * Only for Testing to create an objectDefinition from an xml string
     *
     * @param xml
     * @return
     */
    private ObjectDef[] getlpConfigObjectDefFromString(String xml) {
        ObjectDef[] od = {null, null, null, null, null, null, null, null, null, null};
        try {
            Element topElement = getConcentrator().toDom(xml).getDocumentElement();
            NodeList objects = topElement.getElementsByTagName("Object");

            for (int i = 0; i < objects.getLength(); i++) {
                Element object = (Element) objects.item(i);
                UnsignedShort classId = new UnsignedShort(object.getElementsByTagName("ClassId").item(0).getFirstChild().getTextContent());
                String instanceId = object.getElementsByTagName("InstanceId").item(0).getFirstChild().getTextContent();
                byte attributeId = (byte) Integer.parseInt(object.getElementsByTagName("AttributeId").item(0).getFirstChild().getTextContent());
                UnsignedShort dataId = new UnsignedShort(object.getElementsByTagName("DataId").item(0).getFirstChild().getTextContent());
                od[i] = new ObjectDef(classId, instanceId, attributeId, dataId);
            }

        } catch (XmlException e) {
            e.printStackTrace();
        }

        return od;
    }

    protected boolean isTESTING() {
        return TESTING;
    }

    protected void setTESTING(boolean testing) {
        TESTING = testing;
    }

    public Concentrator getConcentrator() {
        return this.concentrator;
    }

    /**
     * If the testlogging property is valid, then print the msg
     *
     * @param msg
     */
    private void testLogging(String msg) {
        if (getConcentrator().getTESTLOGGING() >= 1) {
            getLogger().log(Level.INFO, msg);
        }
    }

    /**
     * @return a StringArray containing two dates, we need those to request a cosemGetRequest.
     *         - the first date means that the request has to be made before this date.
     *         - the second date means that the request has to be ended before this date.
     */
    public String[] prepareCosemGetRequest() {
        String times[] = {"", ""};
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 5);
        times[0] = Constant.getInstance().getDateFormatFixed().format(cal.getTime());
        cal.add(Calendar.MINUTE, 10);
        times[1] = Constant.getInstance().getDateFormatFixed().format(cal.getTime());
        return times;
    }

    public Connection getConnection() {
        return getConcentrator().getConnection();
    }

    protected StoreObject getStoreObjects() {
        return this.storeObject;
    }

    /**
     * ****************************************************************************************
     * Message implementation
     *
     * @throws IOException
     * @throws NumberFormatException *****************************************************************************************
     */

    protected void sendMeterMessages(Rtu rtu, XmlHandler dataHandler) throws BusinessException, SQLException, NumberFormatException, IOException {
        sendMeterMessages(rtu, null, dataHandler);
    }

    /**
     * Send Pending RtuMessage to meter.
     * Currently we use the eRtu as a concentrator for the mbusRtu, so the serialNumber is this from the eRtu.
     * The messages them are those from the mbus device if this is not NULL.
     *
     * @throws IOException
     * @throws NumberFormatException
     */
    protected void sendMeterMessages(Rtu eRtu, Rtu mbusRtu, XmlHandler dataHandler) throws BusinessException, SQLException, NumberFormatException, IOException {

        /* short circuit */
        if (!communicationProfile.getSendRtuMessage()) {
            return;
        }

        Iterator mi = null;
        String showSerial = null;

        if (mbusRtu != null) {    //mbus messages
            mi = mbusRtu.getPendingMessages().iterator();
            showSerial = mbusRtu.getSerialNumber();
        } else {                    //eRtu messages
            mi = eRtu.getPendingMessages().iterator();
            showSerial = eRtu.getSerialNumber();
        }

        String serial = eRtu.getSerialNumber();

        if (mi.hasNext()) {
            getLogger().log(Level.INFO, "Handling MESSAGES from meter with serialnumber " + showSerial);
        } else {
            return;
        }

        while (mi.hasNext()) {

            RtuMessage msg = (RtuMessage) mi.next();
            String contents = msg.getContents();

            boolean doReadRegister = contents.toLowerCase().indexOf(RtuMessageConstant.READ_ON_DEMAND.toLowerCase()) != -1;
            boolean doDisconnect = contents.toLowerCase().indexOf(RtuMessageConstant.DISCONNECT_LOAD.toLowerCase()) != -1;
            boolean doConnect = (contents.toLowerCase().indexOf(RtuMessageConstant.CONNECT_LOAD.toLowerCase()) != -1) && !doDisconnect;

            boolean thresholdParameters = (contents.toLowerCase().indexOf(RtuMessageConstant.THRESHOLD_GROUPID.toLowerCase()) != -1) ||
                    (contents.toLowerCase().indexOf(RtuMessageConstant.THRESHOLD_POWERLIMIT.toLowerCase()) != -1) ||
                    (contents.toLowerCase().indexOf(RtuMessageConstant.CONTRACT_POWERLIMIT.toLowerCase()) != -1);

            boolean changeRepeaterMode = contents.toLowerCase().indexOf(RtuMessageConstant.REPEATER_MODE.toLowerCase()) != -1;
            boolean changePLCFrequency = contents.toLowerCase().indexOf(RtuMessageConstant.CHANGE_PLC_FREQUENCY.toLowerCase()) != -1;

            /* A single message failure must not stop the other msgs. */
            try {

                if (doReadRegister) {

                    dataHandler.getMeterReadingData().getRegisterValues().clear();

                    List rl = new ArrayList();
                    Iterator i = null;

                    if (mbusRtu != null) {
                        i = mbusRtu.getRtuType().getRtuRegisterSpecs().iterator();
                    } else {
                        i = eRtu.getRtuType().getRtuRegisterSpecs().iterator();
                    }

                    while (i.hasNext()) {

                        RtuRegisterSpec spec = (RtuRegisterSpec) i.next();
                        ObisCode oc = spec.getRegisterMapping().getObisCode();
                        if (oc.getF() == 255) {

                            if (checkManObisCodes(oc)) {
                                rl.add(oc.toString());
                            } else if (checkOtherObisCodes(oc)) {
                                rl.add(new String(oc.getC() + "." + oc.getD() + "." + oc.getE()));
                            } else if (checkFirmwareObisCodes(oc)) {
                                String fwv = getFirmwareVersions(serial, oc);
                                Date meterTime = getTime();
                                Date d = new Date(System.currentTimeMillis());
                                dataHandler.getMeterReadingData().add(new RegisterValue(oc, null, null, null, meterTime, d, 0, fwv));
                            } else if (oc.toString().equalsIgnoreCase(Constant.activeCalendarName.toString())) {
//                        		String meterStatus = getConnection().getMeterStatus(serial);
//                        		getConcentrator().importData(meterStatus, dataHandler);
//                        		dataHandler.getMeterReadingData().add(new RegisterValue(oc, null, null, null, dataHandler.getActiveCalendarDate(),
//                        				new Date(System.currentTimeMillis()), 0, dataHandler.getActiveCalendar()));

                                String calendarName = getCalendarName(serial, oc);
                                Date meterTime = getTime();
                                Date d = new Date(System.currentTimeMillis());
                                dataHandler.getMeterReadingData().add(new RegisterValue(oc, null, null, null, meterTime, d, 0, calendarName));
                            } else {
                                getLogger().log(Level.INFO, "Register with obisCode " + oc.toString() + " is not supported.");
                            }

                            dataHandler.checkOnDemands(true);
                            dataHandler.setProfileDuration(-1);
                        }

                    }
                    if (DEBUG) {
                        System.out.println(rl);
                    }
                    String registers[];
                    String r = null;
                    if (rl.size() > 0) {
                        registers = (String[]) rl.toArray(new String[0]);
                        r = getConnection().getMeterOnDemandResultsList(serial, registers);
                        getConcentrator().importData(r, dataHandler);
                    }

                    if (mbusRtu != null) {
                        handleRegisters(dataHandler, mbusRtu);
                    } else {
                        handleRegisters(dataHandler, eRtu);
                    }
                    dataHandler.checkOnDemands(false);

                    msg.confirm();
                    getLogger().log(Level.INFO, "Current message " + contents + " has finished.");
                } else if (doDisconnect) {
                    getConnection().setMeterDisconnectControl(serial, false);
                    msg.confirm();
                    getLogger().log(Level.INFO, "Current message " + contents + " has finished.");
                } else if (doConnect) {
                    getConnection().setMeterDisconnectControl(serial, true);
                    msg.confirm();
                    getLogger().log(Level.INFO, "Current message " + contents + " has finished.");
                } else if (thresholdParameters) {

                    String groupID = getConcentrator().getMessageValue(contents, RtuMessageConstant.THRESHOLD_GROUPID);
                    if (groupID.equalsIgnoreCase("")) {
                        msg.setFailed();
                        throw new BusinessException("No groupID was entered.");
                    }

                    String thresholdPL = getConcentrator().getMessageValue(contents, RtuMessageConstant.THRESHOLD_POWERLIMIT);
                    String contractPL = getConcentrator().getMessageValue(contents, RtuMessageConstant.CONTRACT_POWERLIMIT);
                    if ((thresholdPL.equalsIgnoreCase("")) && (contractPL.equalsIgnoreCase(""))) {
                        msg.setFailed();
                        throw new BusinessException("Neighter contractual nor threshold limit was given.");
                    }

                    UnsignedInt uiGrId = new UnsignedInt();
                    UnsignedInt crPl = new UnsignedInt();
                    byte[] contractPowerLimit = new byte[]{AxdrType.DOUBLE_LONG_UNSIGNED.getTag(), 0, 0, 0, 0};

                    if (thresholdPL.equalsIgnoreCase("")) {
                        msg.setFailed();
                        throw new BusinessException("No threshold powerLimit was given.");
                    }

                    try {
                        uiGrId.setValue((long) Integer.parseInt(groupID));
                        if (!thresholdPL.equalsIgnoreCase("")) {
                            crPl.setValue((long) Integer.parseInt(thresholdPL));
                        }
                        if (!contractPL.equalsIgnoreCase("")) {
                            contractPowerLimit[1] = (byte) ((long) Integer.parseInt(contractPL) >> 24);
                            contractPowerLimit[2] = (byte) ((long) Integer.parseInt(contractPL) >> 16);
                            contractPowerLimit[3] = (byte) ((long) Integer.parseInt(contractPL) >> 8);
                            contractPowerLimit[4] = (byte) ((long) Integer.parseInt(contractPL));
                        }
                    } catch (NumberFormatException e) {
                        throw new BusinessException("Invalid threshold parameters");
                    }
                    /*
                          * Normally the webService setMeterPowerLimit should be used, but it doens't work with that,
                          * to speed up the development we used the general setCosem method and this works fine!
                          *
                          * 		port(concentrator).setMeterPowerLimit(serial, contractPl);
                          *
                          */
                    if (!contractPL.equalsIgnoreCase("")) {
                        String[] times = prepareCosemGetRequest();        // it is a setRequest, but its the same
                        getConnection().cosemSetRequest(serial, times[0], times[1], Constant.powerLimitObisCode.toString(), new UnsignedInt(3), new UnsignedInt(2), contractPowerLimit);
                    }

                    getConnection().setMeterCodeRedGroupId(serial, uiGrId);
                    if (!thresholdPL.equalsIgnoreCase("")) {
                        getConnection().setMeterCodeRedPowerLimit(serial, crPl);
                    }

                    msg.confirm();
                    getLogger().log(Level.INFO, "Current message " + contents + " has finished.");
                } else if (changeRepeaterMode) {
                    String value = getConcentrator().getMessageValue(contents, RtuMessageConstant.REPEATER_MODE);
                    if (!(value.equalsIgnoreCase("0") || value.equalsIgnoreCase("1") || value.equalsIgnoreCase("2"))) {
                        msg.setFailed();
                        getLogger().log(Level.INFO, value + " is not a valid entry for the current message (" + contents + ").");
                    } else {
                        byte[] mode = new byte[]{AxdrType.UNSIGNED.getTag(), 0};
                        mode[1] = (byte) Integer.parseInt(value);
                        String[] times = prepareCosemGetRequest();        // it is a setRequest, but its the same
                        getConnection().cosemSetRequest(serial, times[0], times[1], Constant.dlcRepeaterMode.toString(), new UnsignedInt(1), new UnsignedInt(2), mode);
                        msg.confirm();
                        getLogger().log(Level.INFO, "Current message " + contents + " has finished.");
                    }
                } else if (changePLCFrequency) {
                    String value = getConcentrator().getMessageValue(contents, RtuMessageConstant.CHANGE_PLC_FREQUENCY);
                    if (!(value.equalsIgnoreCase("0") || value.equalsIgnoreCase("1") || value.equalsIgnoreCase("2") || value.equalsIgnoreCase("3") || value.equalsIgnoreCase("4"))) {
                        msg.setFailed();
                        getLogger().log(Level.INFO, value + " is not a valid entry for the current message (" + contents + ").");
                    } else {
                        byte[] freq = new byte[]{AxdrType.UNSIGNED.getTag(), 0};
                        freq[1] = (byte) Integer.parseInt(value);
                        String[] times = prepareCosemGetRequest();        // it is a setRequest, but its the same
                        getConnection().cosemSetRequest(serial, times[0], times[1], Constant.dlcCarrierFrequency.toString(), new UnsignedInt(1), new UnsignedInt(2), freq);
                        msg.confirm();
                        getLogger().log(Level.INFO, "Current message " + contents + " has finished.");
                    }
                } else {
                    msg.setFailed();
                    getLogger().log(Level.INFO, "Current message " + contents + " has failed.");
                }

            } catch (RemoteException re) {
                msg.setFailed();
                getLogger().log(Level.INFO, "Current message " + contents + " has failed. (" + re.getMessage() + ")");
                re.printStackTrace();
            } catch (ServiceException se) {
                msg.setFailed();
                getLogger().log(Level.INFO, "Current message " + contents + " has failed. (" + se.getMessage() + ")");
                se.printStackTrace();
            }
        }
        getLogger().log(Level.INFO, "Done handling messages.");
    }

    private String getCalendarName(String meterID, ObisCode oc) throws NumberFormatException, RemoteException, ServiceException, IOException, BusinessException {
        String times[] = prepareCosemGetRequest();
        byte[] strCore = getConnection().cosemGetRequest(meterID, times[0], times[1], oc.toString(), new UnsignedInt(20), new UnsignedInt(2));
        byte[] convertStr = new byte[strCore.length - 2];
        System.arraycopy(strCore, 2, convertStr, 0, convertStr.length);
        return Integer.toString(convertStr[0] & 0xFF);
    }

    /**
     * Checks is the obiscode is a manufacturer specific
     *
     * @param oc
     * @return
     */
    private boolean checkManObisCodes(ObisCode oc) {
        if (oc.getC() == 128) {
            if ((oc.getA() == 0) && ((oc.getB() == 0) || (oc.getB() == 1))) {
                if (oc.getD() == 7) {            // dips and swells
                    if ((oc.getE() >= 11) && (oc.getE() <= 17)) {
                        return true;
                    }
                    if ((oc.getE() >= 21) && (oc.getE() <= 27)) {
                        return true;
                    }
                    if ((oc.getE() >= 31) && (oc.getE() <= 37)) {
                        return true;
                    }
                    if ((oc.getE() >= 41) && (oc.getE() <= 47)) {
                        return true;
                    }
                    if ((oc.getE() >= 50) && (oc.getE() <= 51)) {
                        return true;
                    }
                } else if (oc.getD() == 8) {    // daily peak and minimum
                    if ((oc.getE() >= 0) && (oc.getE() <= 3)) {
                        return true;
                    }
                    if ((oc.getE() >= 10) && (oc.getE() <= 13)) {
                        return true;
                    }
                    if ((oc.getE() >= 20) && (oc.getE() <= 23)) {
                        return true;
                    }
                    if ((oc.getE() >= 30) && (oc.getE() <= 33)) {
                        return true;
                    }
                    if (oc.getE() == 50) {
                        return true;
                    }
                } else if (oc.getD() == 6) {        // reclosing counter
                    if (oc.getE() == 1) {
                        return true;
                    }
                } else if ((oc.getD() == 50) && (oc.getE() == 0)) {
                    return true;
//				else if((oc.getD() == 0)&&(oc.getE() == 2))	// DLC frequency pair
//					return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if the obiscode is the one for the firmwareversions
     *
     * @param oc
     * @return
     */
    private boolean checkFirmwareObisCodes(ObisCode oc) {
        if (oc.getD() == 101) {
            if (oc.getE() == 18 || oc.getE() == 28 || oc.getE() == 26) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check all the rest.
     *
     * @param oc
     * @return
     */
    private boolean checkOtherObisCodes(ObisCode oc) {
        if ((oc.getA() == 1) && ((oc.getB() == 0) || (oc.getB() == 1))) {
            if ((oc.getC() == 1) || (oc.getC() == 2)) {
                if ((oc.getD() == 4) && (oc.getE() == 0)) {
                    return true;        // Current average demand
                }
                if ((oc.getD() == 5) && (oc.getE() == 0)) {
                    return true;        // Last average demand
                }
                if ((oc.getD() == 6) && ((oc.getE() >= 0) && (oc.getE() <= 4))) {
                    return true;        // Max. demand rate E
                }
                if ((oc.getD() == 8) && ((oc.getE() >= 0) && (oc.getE() <= 4))) {
                    return true;        // Active energy
                }
            }
        }
        return false;
    }

    protected String getBillingMonthly() {
        return billingMonthly;
    }

    protected void setBillingMonthly(String billingMonthly) {
        this.billingMonthly = billingMonthly;
    }

    protected String getBillingDaily() {
        return billingDaily;
    }

    protected void setBillingDaily(String billingDaily) {
        this.billingDaily = billingDaily;
    }

    protected void setProfileTestName(String[] name) {
        this.profileTestName = name;
    }

    protected String[] getProfileTestName() {
        return profileTestName;
    }

    /*******************************************************************************************
     Caching and collecting
     *******************************************************************************************/

    /**
     * Start the check methods.
     * We get the cached blob from the database, store it in the dlmsCache object, see if we need to update the cache, store it afterwards.
     */
    private void doTheCheckMethods() throws NumberFormatException, IOException, ServiceException, SQLException, BusinessException {

        /**
         * Original implementation but changed this to use the optional parameters.$
         * Commented this part for when the change must be undone (created 26/11/2008)
         */
//    	if(!initCheck){
//    		try {
//    			testLogging("TESTLOGGING - Start the cache mechanism.");
//    			dlmsCache = (Cache)GenericCache.startCacheMechanism(meter);
//    		} catch (FileNotFoundException e) {
//    			e.printStackTrace();	// absorb - The transaction may NOT fail, if the file is not found, then make one.
//    		} catch (IOException e) {
//    			e.printStackTrace();
//    		} 
//    		
//    		collectCache();
//    		saveConfiguration();
//    		initCheck = true;
//    	}

        if (!initCheck) {
            try {
                testLogging("TESTLOGGING - Start the cache mechanism.");
                dlmsCache = (Cache) GenericCache.startCacheMechanism(meter);

                if (validConfigurationProperties()) {
                    if (dlmsCache == null) {
                        useParameters = true;
                        doMbusParameterCheck();
                    } else {
                        setCachedObjects();
                    }
                    initCheck = true;
                } else {
                    collectCache();
                    saveConfiguration();
                    initCheck = true;
                }

            } catch (FileNotFoundException e) {

                if (validConfigurationProperties()) {
                    useParameters = true;
                    doMbusParameterCheck();
                } else {
                    throw new BusinessException("Cache file is empty and not all config parameters are entered. Meter will not be handled.");
                }

            } catch (IOException e) {
                e.printStackTrace();
                throw new IOException(e.getMessage());
            }
        }
    }

    private void doMbusParameterCheck() {
        List<Rtu> slaveMeters = meter.getDownstreamRtus();
        if (slaveMeters.size() > 0) {
            for (int i = 0; i < slaveMeters.size(); i++) {
                Rtu mbus = slaveMeters.get(i);
                int nodeAddress = 0;
                try {
                    nodeAddress = Integer.parseInt(mbus.getNodeAddress());
                    if ((nodeAddress >= 1) && (nodeAddress <= 4)) {
                        mbusDevices[i] = new MbusDevice(0, nodeAddress, mbus.getSerialNumber(), 0, mbus, Unit.get(BaseUnit.UNITLESS), getLogger());
                    } else {
                        getLogger().log(Level.INFO, "NodeAddress of meter " + mbus.getSerialNumber() + " is not valid.");
                    }
                } catch (NumberFormatException e) {
                    getLogger().log(Level.INFO, "NodeAddress of meter " + mbus.getSerialNumber() + " is not a number.");
                    e.printStackTrace();
                }
            }
        }
    }

    private boolean validConfigurationProperties() throws IOException {
        if (!getConcentrator().getLpDaily().equalsIgnoreCase("")
                && !getConcentrator().getLpElectricity().equalsIgnoreCase("")
                && !getConcentrator().getLpMbus().equalsIgnoreCase("")
                && !getConcentrator().getLpMonthly().equalsIgnoreCase("")) {
            if (ParseUtils.countEqualSignsInString(getConcentrator().getLpDaily(), ".") != 2) {
                throw new IOException("Property DailyLoadProfile is not valid.");
            }

            if (ParseUtils.countEqualSignsInString(getConcentrator().getLpMonthly(), ".") != 2) {
                throw new IOException("Property MonthlyLoadProfile is not valid.");
            }

            if (ParseUtils.countEqualSignsInString(getConcentrator().getLpElectricity(), ".") != 2) {
                throw new IOException("Property ElectricityLoadProfile is not valid.");
            }

            if (ParseUtils.countEqualSignsInString(getConcentrator().getLpMbus(), ".") != 2) {
                throw new IOException("Property MBusLoadProfile is not valid.");
            }

            return true;

        }
        return false;
    }

    /**
     * Start the check methods for when only messages are sent.
     * We get the cache from the database and store it in the dlmsCache object
     *
     * @throws NumberFormatException
     * @throws SQLException
     * @throws BusinessException
     * @throws IOException
     * @throws ServiceException
     */
    private void doTheMbusCheckMethods() throws NumberFormatException, SQLException, BusinessException, IOException, ServiceException {

        /**
         * Original implementation but changed this to use the optional parameters.$
         * Commented this part for when the change must be undone (created 26/11/2008)
         */

//		try {
//			testLogging("TESTLOGGING - Verifying MBus meters.");
//			dlmsCache = (Cache)GenericCache.startCacheMechanism(meter);
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();	// absorb - The transaction may NOT fail, if the file is not found, then make one.
//		} catch (IOException e) {
//			e.printStackTrace();
//		} 
//		
//		setCachedObjects();

        try {
            testLogging("TESTLOGGING - Verifying MBus meters.");
            dlmsCache = (Cache) GenericCache.startCacheMechanism(meter);
            if (validConfigurationProperties()) {
                if (dlmsCache == null) {
                    useParameters = true;
                    doMbusParameterCheck();
                } else {
                    setCachedObjects();
                }
                initCheck = true;
            } else {
                setCachedObjects();
                initCheck = true;
            }

        } catch (FileNotFoundException e) {
            if (validConfigurationProperties()) {
                useParameters = true;
                doMbusParameterCheck();
            } else {
                throw new BusinessException("Cache file is empty and not all config parameters are entered. Meter will not be handled.");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    /**
     * Checks if there is in fact an MBus meter configured on the E-meter
     *
     * @return true or false
     * @throws SQLException
     * @throws IOException
     * @throws BusinessException
     * @throws ServiceException
     */
    protected boolean mbusCheck() throws ServiceException, BusinessException, IOException, SQLException {

//		for(int i = 0; i < MBUS_MAX; i++){
//			if ( mbusDevices[0] != null ){
//				if(mbusDevices[0].getRtu() != null){
//					return true;
//				}
//				else
//					getLogger().log(Level.CONFIG, "MBus serialnumber in EIServer(" + mbusDevices[0].getCustomerID() + ") didn't match serialnumber in meter(" + mSerial+ ")");
//			}
//		}
//		return false;


        List slaves = getMeter().getDownstreamRtus();
        int count = 0;
//		while(it.hasNext()){
//			mbusChannel = -1;
//			try {
//				mbus = (Rtu)it.next();
//				serialMbus = mbus.getSerialNumber();
//				mbusChannel = checkSerialForMbusChannel(serialMbus);
////				this.mbusDevices[count++] = new MbusDevice(serialMbus, mbus, getLogger());
//				this.mbusDevices[count++] = new MbusDevice(serialMbus, mbusChannel, mbus, getLogger());
//			} catch (ApplicationException e) {
//				// catch and go to next slave
//				e.printStackTrace();
//			}
//		}

        if (meter.getProperties().getProperty(Constant.RTU_TYPE) != null) {    // means we can create the mbusDevices
            for (int i = 0; i < MBUS_MAX; i++) {
                if (!getResultsFile().getSerialNumbers(i).equalsIgnoreCase("")) {
                    Rtu mbus = findOrCreate(getMeter(), getResultsFile().getSerialNumbers(i), 15);
                    if (mbus != null) {
                        this.mbusDevices[count++] = new MbusDevice(-1, i, getResultsFile().getSerialNumbers(i), 15,
                                mbus, Unit.get(BaseUnit.UNITLESS), getLogger());
                    }
                }
            }
        } else if (slaves.size() > 0) {    // we have downstreamRtu's
            for (int i = 0; i < slaves.size(); i++) {
                Rtu slave = (Rtu) slaves.get(i);
                int phyChannel = findPhysicalChannelBySerialNumber(slave.getSerialNumber());
                if ((phyChannel >= 0) && (phyChannel < MBUS_MAX)) {
                    this.mbusDevices[count++] = new MbusDevice(-1, phyChannel, slave.getSerialNumber(), 15,
                            slave, Unit.get(BaseUnit.UNITLESS), getLogger());
                } else {
                    getLogger().log(Level.INFO, "Mbus meter with serialnumber " + slave.getSerialNumber() + " is not found on the concentrator, this meter will not be handled.");
                }
            }
        }


        for (int i = 0; i < this.MBUS_MAX; i++) {
            if (mbusDevices[i] != null) {
                return true;
            }
        }

        return false;
    }

    private int findPhysicalChannelBySerialNumber(String serial) throws ServiceException, BusinessException, IOException {
        String tempSerial = "";
        for (int i = 0; i < this.MBUS_MAX; i++) {
            tempSerial = getResultsFile().getSerialNumbers(i);
            if (tempSerial.equalsIgnoreCase(serial)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Checks if the given meterList(the downstream Rtu's) are still physically connected to the E-meter, if not delete the concentrator gateway
     *
     * @param downstreamRtus - the list of Rtu's
     * @throws SQLException
     * @throws BusinessException
     */
    private void updateMbusDevices(List downstreamRtus) throws SQLException, BusinessException {
        Iterator it = downstreamRtus.iterator();
        int count = 0;
        while (it.hasNext()) {
            Rtu mbus = ((Rtu) it.next());
            boolean delete = true;
            for (int i = 0; i < mbusDevices.length; i++) {
                if (mbusDevices[i] != null) {
                    if (mbus.getSerialNumber().equalsIgnoreCase(mbusDevices[i].getCustomerID())) {
                        delete = false;
                    }
                }
            }
            if (delete) {
//    			mbus.updateGateway(null);	 // you can do this in the latest build of EIServer
                RtuShadow shadow = mbus.getShadow();
                shadow.setGatewayId(0);
                mbus.update(shadow);
            }
        }
    }

    /**
     * Reads the MBus serialNumber from the E-meter
     * Tricky part:
     * some Mbus meters return there serialnumber as a byteArray containing Hex values, other as Char values ...
     *
     * @param obisCode of the register in the E-meter
     * @return the serialNumber
     * @throws NumberFormatException
     * @throws ServiceException
     * @throws IOException
     * @throws BusinessException
     */
    private String getMbusSerial(String obisCode) throws NumberFormatException, ServiceException, IOException, BusinessException {
        String times[] = prepareCosemGetRequest();
        byte[] bStr = getConnection().cosemGetRequest(serial, times[0], times[1], obisCode, new UnsignedInt(1), new UnsignedInt(2));
        byte[] parseStr = new byte[bStr.length - 2];
        System.arraycopy(bStr, 2, parseStr, 0, bStr.length - 2);
        String str;
        if (ParseUtils.checkIfAllAreChars(parseStr)) {
            str = new String(parseStr);
        } else {
            str = ParseUtils.decimalByteToString(parseStr);
        }
        return str;
    }

    /**
     * Read the MBus address of the mbus meters, this address is either hardcoded in the meter or given by the E-meter
     *
     * @param obisCode of the register in the E-meter
     * @return a decimal number
     * @throws NumberFormatException
     * @throws RemoteException
     * @throws ServiceException
     * @throws IOException
     * @throws BusinessException
     */
    private int getMbusAddress(String obisCode) throws NumberFormatException, RemoteException, ServiceException, IOException, BusinessException {
        String times[] = prepareCosemGetRequest();
        byte[] b = getConnection().cosemGetRequest(serial, times[0], times[1], obisCode, new UnsignedInt(1), new UnsignedInt(2));
        return b[1];
    }

    private int getMbusMedium(String obisCode) throws NumberFormatException, RemoteException, ServiceException, IOException, BusinessException {
        String times[] = prepareCosemGetRequest();
        byte[] b = getConnection().cosemGetRequest(serial, times[0], times[1], obisCode, new UnsignedInt(1), new UnsignedInt(2));
        return b[1];
    }

    /**
     * Read the MBus VIF so we can get the UNIT
     *
     * @param obisCode of register in E-meter
     * @return the MBus Unit
     * @throws NumberFormatException
     * @throws RemoteException
     * @throws ServiceException
     * @throws IOException
     * @throws BusinessException
     */
    private Unit getMbusVIF(String obisCode) throws NumberFormatException, RemoteException, ServiceException, IOException, BusinessException {
        String times[] = prepareCosemGetRequest();
        byte[] b = getConnection().cosemGetRequest(serial, times[0], times[1], obisCode, new UnsignedInt(1), new UnsignedInt(2));
        ValueInformationfieldCoding vif = ValueInformationfieldCoding.findPrimaryValueInformationfieldCoding(b[2], -1);
        return vif.getUnit();
    }

    /**
     * Save all the captured values in the cache object and save the blob to the database
     *
     * @throws BusinessException
     * @throws SQLException
     */
    private void saveConfiguration() throws BusinessException, SQLException {
        dlmsCache.setBillingReadTime(billingReadTime);
        dlmsCache.setCaptureObjReadTime(captureObjReadTime); // not necessary
        dlmsCache.setLoadProfileConfig1(loadProfileConfig1);
        dlmsCache.setLoadProfileConfig2(loadProfileConfig2);
        dlmsCache.setLoadProfileConfig3(loadProfileConfig3);
        dlmsCache.setLoadProfileConfig4(loadProfileConfig4);
        dlmsCache.setLoadProfilePeriod1(loadProfilePeriod1);
        dlmsCache.setLoadProfilePeriod2(loadProfilePeriod2);
        dlmsCache.setMbusParameters(mbusDevices);
        GenericCache.stopCacheMechanism(meter, dlmsCache);
    }

    /**
     * Check if the configuration parameters need to be read again by comparing the 'Configuration number change'.
     * If the parameter is not accessible, then read the configuration as well.
     *
     * @throws BusinessException
     * @throws IOException
     * @throws SQLException
     */
    int iConf;

    private void collectCache() throws BusinessException, IOException, SQLException {
        try {
            if ((dlmsCache != null) && (dlmsCache.getLoadProfileConfig1() != null)) {
                testLogging("TESTLOGGING - Collect1/ cache file is not empty");
                setCachedObjects();

                try {
                    getLogger().log(Level.INFO, "Checking configuration parameters.");
                    iConf = requestConfigurationChanges();

                } catch (NumberFormatException e) {
                    iConf = -1;
                    getLogger().log(Level.INFO, "Iskra Mx37x: Configuration change is not accessible, requesting configuration parameters ...");
                    getLogger().log(Level.INFO, "(This will take several minutes.)");
                    requestConfigurationParameters();
                    dlmsCache.setConfProgChange(iConf);
                    e.printStackTrace();
                } catch (ServiceException e) {
                    iConf = -1;
                    getLogger().log(Level.INFO, "Iskra Mx37x: Configuration change is not accessible, requesting configuration parameters ...");
                    getLogger().log(Level.INFO, "(This will take several minutes.)");
                    requestConfigurationParameters();
                    dlmsCache.setConfProgChange(iConf);
                    e.printStackTrace();
                } catch (RemoteException e) {
                    iConf = -1;
                    getLogger().log(Level.INFO, "Iskra Mx37x: Configuration change is not accessible, requesting configuration parameters ...");
                    getLogger().log(Level.INFO, "(This will take several minutes.)");
                    requestConfigurationParameters();
                    dlmsCache.setConfProgChange(iConf);
                    e.printStackTrace();
                }

                if ((iConf != dlmsCache.getConfProgChange()) || forcedMbusCheck) {
                    getLogger().log(Level.INFO, "Iskra Mx37x: Configuration changed, requesting configuration parameters...");
                    getLogger().log(Level.INFO, "(This will take several minutes.)");
                    requestConfigurationParameters();
                    dlmsCache.setConfProgChange(iConf);
                }
            } else {     //if cache doesn't exist
                dlmsCache = new Cache();
                getLogger().log(Level.INFO, "Iskra Mx37x: Cache does not exist, requesting configuration parameters...");
                getLogger().log(Level.INFO, "(This will take several minutes.)");
                requestConfigurationParameters();

                try {
                    iConf = requestConfigurationChanges();
                    dlmsCache.setConfProgChange(iConf);

                } catch (NumberFormatException e) {
                    iConf = -1;
                    e.printStackTrace();
                } catch (ServiceException e) {
                    iConf = -1;
                    e.printStackTrace();
                } catch (RemoteException e) {
                    iConf = -1;
                    e.printStackTrace();
                }
            }
        } catch (RemoteException e1) {
            e1.printStackTrace();
            throw new BusinessException(e1); /* roll back */
        } catch (ServiceException e1) {
            e1.printStackTrace();
            throw new BusinessException(e1); /* roll back */
        }
    }

    public String getFileName() {
        Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "_" + serial + "_IskraMx37x.cache";
    }

    public Object getCache() {
        return dlmsCache;
    }

    public void setCache(Object cacheObject) {
        this.dlmsCache = (Cache) cacheObject;
    }

    /**
     * Getting all the configuration parameters including the MBus parameters
     *
     * @throws ServiceException
     * @throws IOException
     * @throws BusinessException
     * @throws SQLException
     */
    protected void requestConfigurationParameters() throws ServiceException, IOException, BusinessException, SQLException {
        String loadProfile1 = "LoadProfile1";
        String loadProfile2 = "LoadProfile2";
        String billingProfile = "BillingProfile";
        String scheduledProfile = "ScheduledProfile";

        try {
            testLogging("TESTLOGGING - Requesting1/ lp period1");
            this.loadProfilePeriod1 = getConnection().getMeterLoadProfilePeriod(serial, new PeriodicProfileType(loadProfile1)).intValue();
            testLogging("TESTLOGGING - Requesting2/ lp period2");
            this.loadProfilePeriod2 = getConnection().getMeterLoadProfilePeriod(serial, new PeriodicProfileType(loadProfile2)).intValue();
            testLogging("TESTLOGGING - Requesting3/ lp config1");
            this.loadProfileConfig1 = getConnection().getMeterProfileConfig(serial, new ProfileType(loadProfile1));
            testLogging("TESTLOGGING - Requesting4/ lp config2");
            this.loadProfileConfig2 = getConnection().getMeterProfileConfig(serial, new ProfileType(loadProfile2));
            testLogging("TESTLOGGING - Requesting5/ lp config3");
            this.loadProfileConfig3 = getConnection().getMeterProfileConfig(serial, new ProfileType(billingProfile));
            testLogging("TESTLOGGING - Requesting6/ lp config4");
            this.loadProfileConfig4 = getConnection().getMeterProfileConfig(serial, new ProfileType(scheduledProfile));
            testLogging("TESTLOGGING - Requesting7/ billing readTime");
            this.billingReadTime = getConnection().getMeterBillingReadTime(serial);
            testLogging("TESTLOGGING - Requesting8/ mbus configuration");
            requestMbusConfiguration();

        } catch (RemoteException e) {
            getLogger().log(Level.SEVERE, "IskraMx37x: could not retrieve configuration parameters, meter will NOT be handled");
            e.printStackTrace();
            throw new RemoteException("No parameters could be retrieved.", e);
        } catch (ServiceException e) {
            getLogger().log(Level.SEVERE, "IskraMx37x: could not retrieve configuration parameters, meter will NOT be handled");
            e.printStackTrace();
            throw new ServiceException("No parameters could be retrieved.", e);
        } catch (NumberFormatException e) {
            getLogger().log(Level.SEVERE, "IskraMx37x: could not retrieve configuration parameters, meter will NOT be handled");
            e.printStackTrace();
            throw new ProcessingException("No parameters could be retrieved.", e);
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "IskraMx37x: could not retrieve configuration parameters, meter will NOT be handled");
            e.printStackTrace();
            throw new SQLException("No parameters could be retrieved.");
        }
    }

    /**
     * Read every mbusAddress, if the address is greater then zero then there is an MBus meter installed on that address.
     * In that case we read the customerID and Unit as well.
     *
     * @throws NumberFormatException
     * @throws RemoteException
     * @throws ServiceException
     * @throws IOException
     * @throws BusinessException
     * @throws SQLException
     */
    private void requestMbusConfiguration() throws NumberFormatException, RemoteException, ServiceException, IOException, BusinessException, SQLException {
        for (int i = 0; i < MBUS_MAX; i++) {
            int mbusAddress = getMbusAddress(Constant.mbusAddressObisCode[i].toString());
            if (mbusAddress > 0) {
                if (!requestedMbusSerials[i].equalsIgnoreCase("")) {
                    mSerial = requestedMbusSerials[i];
                } else {
                    mSerial = getMbusSerial(Constant.mbusSerialObisCode[i].toString());
                    requestedMbusSerials[i] = mSerial;
                }
                Unit mUnit = getMbusVIF(Constant.mbusVIFObisCode[i].toString());
                int mbusMedium = getMbusMedium(Constant.mbusMediumObisCode[i].toString());
                if (!mSerial.equalsIgnoreCase("")) {
                    mbusDevices[i] = new MbusDevice(mbusAddress, i, mSerial, mbusMedium, findOrCreate(getMeter(), mSerial, mbusMedium), mUnit, getLogger());
                } else {
                    mbusDevices[i] = null;
                }
            } else {
                mbusDevices[i] = null;
            }
        }
        updateMbusDevices(getMeter().getDownstreamRtus());
    }

    /**
     * Fill in all the parameters from the cached object.
     * Tricky part for the MBus devices, if the meter does not exist in the database we have to check if the MBus meter is still installed on the E-meter.
     * If so then we can create the meter in the database again(if RtuType is defined), if not we force to do a complete configuration read.
     *
     * @throws SQLException
     * @throws BusinessException
     * @throws IOException
     * @throws NumberFormatException
     * @throws ServiceException
     */
    protected void setCachedObjects() throws SQLException, BusinessException, IOException, NumberFormatException, ServiceException {
        this.billingReadTime = dlmsCache.getBillingReadTime();
        this.captureObjReadTime = dlmsCache.getCaptureObjReadTime();
        this.loadProfileConfig1 = dlmsCache.getLoadProfileConfig1();
        this.loadProfileConfig2 = dlmsCache.getLoadProfileConfig2();
        this.loadProfileConfig3 = dlmsCache.getLoadProfileConfig3();
        this.loadProfileConfig4 = dlmsCache.getLoadProfileConfig4();
        this.loadProfilePeriod1 = dlmsCache.getLoadProfilePeriod1();
        this.loadProfilePeriod2 = dlmsCache.getLoadProfilePeriod2();
        for (int i = 0; i < dlmsCache.getMbusDeviceCount(); i++) {
            if (rtuExists(dlmsCache.getCustomerID(i))) {
                mbusDevices[i] = new MbusDevice(dlmsCache.getMbusAddress(i), dlmsCache.getPhysicalAddress(i), dlmsCache.getCustomerID(i),
                        dlmsCache.getMbusMedium(i), findOrCreate(getMeter(), dlmsCache.getCustomerID(i), dlmsCache.getMbusMedium(i)), dlmsCache.getUnit(i), getLogger());
            } else {
                if (getMbusRtuType(dlmsCache.getMbusMedium(i)) != null) {
                    requestedMbusSerials[i] = getMbusSerial(Constant.mbusSerialObisCode[i].toString());
                    if (!requestedMbusSerials[i].equalsIgnoreCase("")) {
                        if (dlmsCache.getCustomerID(i) != null) {
                            if (dlmsCache.getCustomerID(i).equalsIgnoreCase(requestedMbusSerials[i])) {
                                mbusDevices[i] = new MbusDevice(dlmsCache.getMbusAddress(i), dlmsCache.getPhysicalAddress(i), dlmsCache.getCustomerID(i),
                                        dlmsCache.getMbusMedium(i), findOrCreate(getMeter(), dlmsCache.getCustomerID(i), dlmsCache.getMbusMedium(i)), dlmsCache.getUnit(i), getLogger());
                            } else {
                                forcedMbusCheck = true;
                                break;
                            }
                        } else {
                            forcedMbusCheck = true;
                            break;
                        }
                    } else {
                        forcedMbusCheck = true;
                        break;
                    }

                } else {
                    mbusDevices[i] = null;
                    getLogger().log(Level.INFO, "No rtuType defined on meter -> no MBus meters created");
                }
            }
        }
    }

    /**
     * Requests the number of configuration changes from the meter.
     *
     * @return a number of changes
     * @throws NumberFormatException
     * @throws ServiceException
     * @throws IOException
     * @throws BusinessException
     */
    protected int requestConfigurationChanges() throws NumberFormatException, ServiceException, IOException, BusinessException {
        String times[] = prepareCosemGetRequest();
        byte[] byteStrs = getConnection().cosemGetRequest(serial, times[0], times[1], Constant.confChangeObisCode.toString(), new UnsignedInt(1), new UnsignedInt(2));
        int changes = byteStrs[2] & 0xFF;
        changes = changes + ((byteStrs[1] & 0xFF) << 8);

        // check if the customerID from the meter matches the customerID from the cache
        for (int i = 0; i < MBUS_MAX; i++) {
            String customerID = dlmsCache.getCustomerID(i);
            String meterCustomerID;
            if (!requestedMbusSerials[i].equalsIgnoreCase("")) {
                meterCustomerID = requestedMbusSerials[i];
            } else {
                meterCustomerID = getMbusSerial(Constant.mbusSerialObisCode[i].toString());
                requestedMbusSerials[i] = meterCustomerID;
            }
            if (customerID != null) {
                if (!customerID.equalsIgnoreCase(meterCustomerID)) {
                    forcedMbusCheck = true;
                    break;
                }
            } else {
                if (!meterCustomerID.equalsIgnoreCase("")) {
                    forcedMbusCheck = true;
                    break;
                }
            }
        }
        return changes;
    }

    public boolean useParameters() {
        return this.useParameters;
    }

    private ResultsFile getResultsFile() throws ServiceException, BusinessException, IOException {
        if (this.resultsFile == null) {
            resultsFile = new ResultsFile(this);
        }
        return this.resultsFile;
    }

}