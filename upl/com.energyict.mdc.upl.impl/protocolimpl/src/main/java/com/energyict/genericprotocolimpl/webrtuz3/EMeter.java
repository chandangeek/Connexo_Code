package com.energyict.genericprotocolimpl.webrtuz3;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.Link;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.CommonUtils;
import com.energyict.genericprotocolimpl.webrtuz3.historical.HistoricalRegisterReadings;
import com.energyict.genericprotocolimpl.webrtuz3.messagehandling.EMeterMessageExecutor;
import com.energyict.genericprotocolimpl.webrtuz3.messagehandling.EmeterMessages;
import com.energyict.genericprotocolimpl.webrtuz3.profiles.*;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.core.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 *
 * @since 9-apr-2010 13:51:14
 * @author jme
 */
public class EMeter extends EmeterMessages implements GenericProtocol, EDevice {

    /**
     * Property names
     */
    private static final String PROPERTY_READ_REGULAR_DEMAND_VALUES = "ReadRegularDemandValues";
    private static final String PROPERTY_READ_DAILY_VALUES = "ReadDailyValues";
    private static final String PROPERTY_READ_MONTHLY_VALUES = "ReadMonthlyValues";
    private static final String PROPERTY_RUNTESTMETHOD = "RunTestMethod";

    /**
     * Property default values
     */
    private static final String DEFAULT_READ_REGULAR_DEMAND_VALUES = "1";
    private static final String DEFAULT_READ_DAILY_VALUES = "1";
    private static final String DEFAULT_READ_MONTHLY_VALUES = "1";
    private static final String DEFAULT_RUNTESTMETHOD = "0";

    public static final ObisCode FIRMWARE_OBISCODE = ObisCode.fromString("0.0.0.2.0.255");
    public static final ObisCode SERIAL_OBISCODE = ObisCode.fromString("0.0.96.1.0.255");
    public static final ObisCode PROFILE_OBISCODE = ObisCode.fromString("0.0.99.1.0.255");
    public static final ObisCode EVENTS_OBISCODE = ObisCode.fromString("0.0.99.98.0.255");
    public static final ObisCode MONTHLY_PROFILE_OBIS = ObisCode.fromString("0.0.99.3.0.255");
    public static final ObisCode DAILY_PROFILE_OBIS = ObisCode.fromString("0.0.99.2.0.255");
    public static final ObisCode DISCONNECTOR_OBIS = ObisCode.fromString("0.0.96.3.10.255");

    /**
     * Property to allow reading the regular 15m, 30m, hourly, ... values
     */
    private boolean readRegular = true;

    /**
     * Property to allow reading the daily values
     */
    private boolean readDaily = true;

    /**
     * Property to allow reading the monthly values
     */
    private boolean readMonthly = true;

    /**
     * Property to enable debugging on the emeter 
     */
    private boolean runTestMethod;


    private final Properties properties = new Properties();
    private CommunicationProfile commProfile;
    private WebRTUZ3 webRtu;
    private String serialNumber;
    private int physicalAddress;
    private Rtu eMeterRtu;
    private Logger logger;

    private HistoricalRegisterReadings historicalRegisters;
    private MeterAmrLogging meterAmrLogging;

    public EMeter() {

	}

	/**
	 * @param serial
	 * @param physicalAddress
	 * @param eMeterRtu
	 * @param logger
	 */
	public EMeter(String serial, int physicalAddress, Rtu eMeterRtu, Logger logger) {
		this.serialNumber = serial;
		this.physicalAddress = physicalAddress;
		this.eMeterRtu = eMeterRtu;
		this.logger = logger;
	}

    public void validateProperties() throws MissingPropertyException, InvalidPropertyException {
        this.readRegular = (ProtocolTools.getPropertyAsInt(getProperties(), PROPERTY_READ_REGULAR_DEMAND_VALUES, DEFAULT_READ_REGULAR_DEMAND_VALUES) == 1) ? true : false;
        this.readDaily = (ProtocolTools.getPropertyAsInt(getProperties(), PROPERTY_READ_DAILY_VALUES, DEFAULT_READ_DAILY_VALUES) == 1) ? true : false;
        this.readMonthly = (ProtocolTools.getPropertyAsInt(getProperties(), PROPERTY_READ_MONTHLY_VALUES, DEFAULT_READ_MONTHLY_VALUES) == 1) ? true : false;
        this.runTestMethod = (ProtocolTools.getPropertyAsInt(getProperties(), PROPERTY_RUNTESTMETHOD, DEFAULT_RUNTESTMETHOD) == 1) ? true : false;
    }

	public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
		this.commProfile = scheduler.getCommunicationProfile();
        validateProperties();

        testMethod();

        // Before reading data, check the serialnumber
        verifySerialNumber();

        // import profile
		if(commProfile.getReadDemandValues() && isReadRegular()){
			getLogger().log(Level.INFO, "Getting loadProfile for meter with serialnumber: " + geteMeterRtu().getSerialNumber());
			EMeterProfile mp = new EMeterProfile(this);
			ProfileData pd = mp.getProfile(getCorrectedObisCode(PROFILE_OBISCODE));
			if(this.webRtu.isBadTime()){
				pd.markIntervalsAsBadTime();
			}
			this.webRtu.getStoreObject().add(pd, geteMeterRtu());
		}

		if(commProfile.getReadMeterEvents()){
			getLogger().log(Level.INFO, "Getting events for meter with serialnumber: " + geteMeterRtu().getSerialNumber());
			EMeterEventProfile mep = new EMeterEventProfile(this);
			ProfileData eventPd = mep.getEvents();
			this.webRtu.getStoreObject().add(eventPd, geteMeterRtu());
		}

		// import daily/monthly
        if(commProfile.getReadDemandValues()){
			DailyMonthly mdm = new DailyMonthly(this);

			if(isReadDaily()){
				getLogger().log(Level.INFO, "Getting Daily values for meter with serialnumber: " + geteMeterRtu().getSerialNumber());
                ProfileData dailyPd = mdm.getDailyValues(getCorrectedObisCode(DAILY_PROFILE_OBIS));
				this.webRtu.getStoreObject().add(dailyPd, geteMeterRtu());
			}

			if(isReadMonthly()){
				getLogger().log(Level.INFO, "Getting Monthly values for meter with serialnumber: " + geteMeterRtu().getSerialNumber());
                ProfileData montProfileData = mdm.getMonthlyValues(getCorrectedObisCode(MONTHLY_PROFILE_OBIS));
				this.webRtu.getStoreObject().add(montProfileData, geteMeterRtu());

			}
        }

        // Read registers
        if(commProfile.getReadMeterReadings()){
            getLogger().log(Level.INFO, "Getting register values for meter with serialnumber: " + geteMeterRtu().getSerialNumber());
			doReadRegisters();
		}

		// send rtuMessages
		if(commProfile.getSendRtuMessage()){
			sendMeterMessages();
		}
	}

    private void testMethod() {
        if (isRunTestMethod()) {
            StringBuffer sb = new StringBuffer();
            sb.append("\r\n\r\n").append("Emeter objects = ").append("\r\n");
            try {
                UniversalObject[] objects = getMeterConfig().getInstantiatedObjectList();
                for (int i = 0; i < objects.length; i++) {
                    UniversalObject object = objects[i];
                    if (object.getObisCode().getB() == physicalAddress) {
                        sb.append(object.getDescription()).append("\r\n");
                    }
                }
            } catch (Exception e) {
                sb.append("\r\n");
                sb.append("An error occured while reading the objectList: ").append(e.getMessage());
            }
            getLogger().warning(sb.toString());
        }
    }

    /**
     *
     * @return
     */
    private String doReadFirmwareVersion() {
        String firmware = null;
        try{
            Data firmwareObject = getCosemObjectFactory().getData(getCorrectedObisCode(FIRMWARE_OBISCODE));
            firmware = firmwareObject.getString();
        } catch (IOException e) {
            firmware = null;
        }
        return firmware == null ? "Unknown" : firmware;
    }

	/**
	 *
	 */
	private void doReadRegisters() {
        Iterator registerIterator = geteMeterRtu().getRegisters().iterator();
        List rtuRegisterGroups = this.commProfile.getRtuRegisterGroups();

        while (registerIterator.hasNext()) {
            ObisCode obisCode = null;
			try {
                RtuRegister rtuRegister = (RtuRegister) registerIterator.next();
                if (CommonUtils.isInRegisterGroup(rtuRegisterGroups, rtuRegister)) {
                    obisCode = rtuRegister.getRtuRegisterSpec().getObisCode();
                    try {
                        RegisterValue registerValue = readRegister(obisCode);
                        if (registerValue != null) {
                            registerValue.setRtuRegisterId(rtuRegister.getId());
                            if (rtuRegister.getReadingAt(registerValue.getReadTime()) == null) {
                                getWebRTU().getStoreObject().add(rtuRegister, registerValue);
							}
						} else {
                            throw new NoSuchRegisterException("Register returned null");
						}
					} catch (NoSuchRegisterException e) {
                        getMeterAmrLogging().logRegisterFailure(e, obisCode);
                        getLogger().log(Level.INFO, "ObisCode " + obisCode + " is not supported by the meter. [" + e.getMessage() + "]");
					}
				}
			} catch (IOException e) {
                getMeterAmrLogging().logRegisterFailure(e, obisCode);
                getLogger().log(Level.INFO, "Reading register with obisCode " + obisCode + " FAILED. [" + e.getMessage() + "]");
			}
		}
	}


    /**
     * @param obisCode
     * @return
     * @throws IOException
     */
    private RegisterValue readRegister(ObisCode obisCode) throws IOException {
        RegisterValue registerValue = null;

        try {
            ObisCode physicalObisCode = ProtocolTools.setObisCodeField(obisCode, 1, (byte) getPhysicalAddress());
            if ((physicalObisCode.getF() >= 0) && (physicalObisCode.getF() <= 11)) { // Monthly billing value
                registerValue = getHistoricalRegisters().readHistoricalMonthlyRegister(physicalObisCode);
            } else if ((physicalObisCode.getF() >= 12) && (physicalObisCode.getF() <= 90)) { // Daily billing value
                physicalObisCode = ProtocolTools.setObisCodeField(physicalObisCode, 5, (byte) (physicalObisCode.getF() - 12));
                registerValue = getHistoricalRegisters().readHistoricalDailyRegister(physicalObisCode);
            } else if (obisCode.equals(ObisCode.fromString("1.0.96.3.10.255"))) { //Another register
                Disconnector register = getCosemObjectFactory().getDisconnector(ProtocolTools.setObisCodeField(DISCONNECTOR_OBIS, 1, (byte) getPhysicalAddress()));
                registerValue = register != null ? register.asRegisterValue(2) : null;
            } else { //Another register
                Register register = getCosemObjectFactory().getRegister(physicalObisCode);
                registerValue = (register != null) ? new RegisterValue(obisCode, register.getQuantityValue()) : null;
            }
        } catch (Exception e) {
            throw new NoSuchRegisterException(e.getMessage());
        }

        if (registerValue == null) {
            throw new NoSuchRegisterException("Register " + obisCode.toString() + " returned 'null'!");
        }

        return ProtocolTools.setRegisterValueObisCode(registerValue, obisCode);
    }

    /**
	 * Execute the pending device messages
	 */
	private void sendMeterMessages() throws BusinessException, SQLException {
		EMeterMessageExecutor messageExecutor = new EMeterMessageExecutor(this);
        List<RtuMessage> pendingMessages = geteMeterRtu().getPendingMessages();
        for (int i = 0; i < pendingMessages.size(); i++) {
            RtuMessage rtuMessage =  pendingMessages.get(i);
            messageExecutor.doMessage(rtuMessage);
        }
	}

    /**
     *
      * @param baseObisCode
     * @return
     */
    public ObisCode getCorrectedObisCode(ObisCode baseObisCode) {
		return ProtocolTools.setObisCodeField(baseObisCode, 1, (byte) physicalAddress);
	}

	private void verifySerialNumber() throws IOException{
		String serial;
		String eiSerial = geteMeterRtu().getSerialNumber();
		try {
			Data serialDataObject = getCosemObjectFactory().getData(ProtocolTools.setObisCodeField(WebRTUZ3.SERIALNR_OBISCODE, 1, (byte) physicalAddress));
			OctetString serialOctetString = serialDataObject.getAttrbAbstractDataType(2).getOctetString();
			serial = serialOctetString != null ? serialOctetString.stringValue() : null;
		} catch (IOException e) {
			throw new IOException("Could not retrieve the serialnumber of meter " + eiSerial + e);
		}
		if(!eiSerial.equals(serial)){
			throw new IOException("Wrong serialnumber, EIServer settings: " + eiSerial + " - Meter settings: " + serial);
		}
	}

    /**
     *
     * @return
     */
    public long getTimeDifference() {
		return 0;
	}

    /**
     *
     * @param properties
     */
    public void addProperties(Properties properties) {
        if (properties != null) {
            getProperties().putAll(properties);
        }
	}

    /**
     *
     * @return
     */
    public String getVersion() {
		return "$Date$";
	}

	public List<String> getOptionalKeys() {
        List<String> optionalKeys = new ArrayList<String>();
        optionalKeys.add(PROPERTY_READ_REGULAR_DEMAND_VALUES);
        optionalKeys.add(PROPERTY_READ_DAILY_VALUES);
        optionalKeys.add(PROPERTY_READ_MONTHLY_VALUES);
        optionalKeys.add(PROPERTY_RUNTESTMETHOD);
        return optionalKeys;
	}

	public List<String> getRequiredKeys() {
        List<String> requiredKeys = new ArrayList<String>(0);
        return requiredKeys;
	}

	/**
	 * @return the eMeterRtu
	 */
	public Rtu geteMeterRtu() {
		return eMeterRtu;
	}

	/**
	 * @return the physicalAddress
	 */
	public int getPhysicalAddress() {
		return physicalAddress;
	}

	/**
	 * @return the logger
	 */
	public Logger getLogger() {
		return logger;
	}

	/**
	 * @return the webRTUZ3
	 */
	public WebRTUZ3 getWebRTU() {
		return webRtu;
	}

	/**
	 * @param webRTUZ3
	 */
	public void setWebRtu(WebRTUZ3 webRTUZ3) {
		this.webRtu = webRTUZ3;
	}

	/**
	 * @return
	 */
	public String getSerialNumber() {
		return serialNumber;
	}

	@Override
	public String toString() {
		return "[" + physicalAddress + "] " + serialNumber;
	}

	public CosemObjectFactory getCosemObjectFactory() {
		return getWebRTU().getCosemObjectFactory();
	}

	public Calendar getFromCalendar(Channel channel) {
		Date lastReading = channel.getLastReading();
		if (lastReading == null) {
			lastReading = com.energyict.genericprotocolimpl.common.ParseUtils.getClearLastMonthDate(channel.getRtu());
		}
		Calendar cal = ProtocolUtils.getCleanCalendar(getTimeZone());
		cal.setTime(lastReading);
		return cal;
	}

	public Rtu getMeter() {
		return geteMeterRtu();
	}

	public Calendar getToCalendar() {
		return ProtocolUtils.getCalendar(getTimeZone());
	}

	public TimeZone getTimeZone() {
		return getMeter().getDeviceTimeZone();
	}

	public DLMSMeterConfig getMeterConfig() {
		return getWebRTU().getMeterConfig();
	}

    /**
     *
     * @return
     */
    public HistoricalRegisterReadings getHistoricalRegisters() {
        if (historicalRegisters == null) {
            historicalRegisters = new HistoricalRegisterReadings(getCosemObjectFactory(), getCorrectedObisCode(DAILY_PROFILE_OBIS), getCorrectedObisCode(MONTHLY_PROFILE_OBIS), getLogger());
}
        return historicalRegisters;
    }

    /**
     *
     * @return
     */
    public MeterAmrLogging getMeterAmrLogging() {
        if (meterAmrLogging == null) {
            meterAmrLogging = new MeterAmrLogging();
        }
        return meterAmrLogging;
    }

    public boolean isReadRegular() {
        return readRegular;
    }

    public boolean isReadDaily() {
        return readDaily;
    }

    public boolean isReadMonthly() {
        return readMonthly;
    }

    public boolean isRunTestMethod() {
        return runTestMethod;
    }

    public Properties getProperties() {
        return properties;
    }
}
