package com.energyict.genericprotocolimpl.webrtuz3;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.Link;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.CommonUtils;
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

    public static final ObisCode FIRMWARE_OBISCODE = ObisCode.fromString("0.0.0.2.0.255");
    public static final ObisCode SERIAL_OBISCODE = ObisCode.fromString("0.0.96.1.0.255");
    public static final ObisCode PROFILE_OBISCODE = ObisCode.fromString("0.0.99.1.0.255");
    public static final ObisCode EVENTS_OBISCODE = ObisCode.fromString("0.0.99.98.0.255");
    public static final ObisCode MONTHLY_PROFILE_OBIS = ObisCode.fromString("1.0.99.3.0.255");
    public static final ObisCode DAILY_PROFILE_OBIS = ObisCode.fromString("1.0.99.2.0.255");

    private CommunicationProfile	commProfile;
	private WebRTUZ3				webRtu;
	private String					serialNumber;
	private int						physicalAddress;
	private Rtu						eMeterRtu;
	private Logger					logger;

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

	public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
		this.commProfile = scheduler.getCommunicationProfile();

        //testMethod();

        try {
			// Before reading data, check the serialnumber
			verifySerialNumber();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e.getMessage());
		}

		// import profile
		if(commProfile.getReadDemandValues()){
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
		if(commProfile.getReadMeterReadings()){
			DailyMonthly mdm = new DailyMonthly(this);

			if(getWebRTU().isReadDaily()){
				getLogger().log(Level.INFO, "Getting Daily values for meter with serialnumber: " + geteMeterRtu().getSerialNumber());
                ProfileData dailyPd = mdm.getDailyValues(getCorrectedObisCode(DAILY_PROFILE_OBIS));
				this.webRtu.getStoreObject().add(dailyPd, geteMeterRtu());
			}

			if(getWebRTU().isReadMonthly()){
				getLogger().log(Level.INFO, "Getting Monthly values for meter with serialnumber: " + geteMeterRtu().getSerialNumber());
                ProfileData montProfileData = mdm.getMonthlyValues(getCorrectedObisCode(MONTHLY_PROFILE_OBIS));
				this.webRtu.getStoreObject().add(montProfileData, geteMeterRtu());

			}
            getLogger().log(Level.INFO, "Getting register values for meter with serialnumber: " + geteMeterRtu().getSerialNumber());
			doReadRegisters();
		}

		// send rtuMessages
		if(commProfile.getSendRtuMessage()){
			sendMeterMessages();
		}
	}

    private void testMethod() {
        String crlfcrlf = "\r\n\r\n";
        System.out.println(crlfcrlf + "EMeter testMethod(): ");
        try {
            UniversalObject[] objects = getMeterConfig().getInstantiatedObjectList();
            for (int i = 0; i < objects.length; i++) {
                UniversalObject object = objects[i];
                if (object.getObisCode().getB() == physicalAddress) {
                    System.out.println(object.getDescription());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(crlfcrlf);

        try {
            ProfileGeneric pg = getCosemObjectFactory().getProfileGeneric(getCorrectedObisCode(EVENTS_OBISCODE));
            byte[] bytes = pg.getBufferData();
            Array events = new Array(bytes, 0, 0);
            System.out.println(ProtocolTools.getHexStringFromBytes(bytes));
            System.out.println(pg);
            System.out.println(events);
        } catch (IOException e) {
            e.printStackTrace();
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
		Iterator it = geteMeterRtu().getRegisters().iterator();
		List groups = this.commProfile.getRtuRegisterGroups();
		ObisCode oc = null;
		RegisterValue rv;
		RtuRegister rr;
		while(it.hasNext()){
			try {
				rr = (RtuRegister)it.next();
				if (CommonUtils.isInRegisterGroup(groups, rr)) {
					oc = rr.getRtuRegisterSpec().getObisCode();
					try{
						rv = readRegister(oc);
						if(rv != null){
							rv.setRtuRegisterId(rr.getId());

							if(rr.getReadingAt(rv.getReadTime()) == null){
								getWebRTU().getStoreObject().add(rr, rv);
							}
						} else {
							getLogger().log(Level.INFO, "Obiscode " + oc + " is not supported.");
						}

					} catch (NoSuchRegisterException e) {
						e.printStackTrace();
						getLogger().log(Level.INFO, "ObisCode " + oc + " is not supported by the meter.");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				getLogger().log(Level.INFO, "Reading register with obisCode " + oc + " FAILED.");
			}
		}
	}


    /**
     * 
     * @param obisCode
     * @return
     * @throws IOException
     */
	private RegisterValue readRegister(ObisCode obisCode) throws IOException {
		try {
            ObisCode physicalObisCode = ProtocolTools.setObisCodeField(obisCode, 1, (byte) getPhysicalAddress());
            Register register = getCosemObjectFactory().getRegister(physicalObisCode);
			return new RegisterValue(obisCode, register.getQuantityValue());
		} catch (Exception e) {
			e.printStackTrace();
			throw new NoSuchRegisterException(e.getMessage());
		}
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
			e.printStackTrace();
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

	}

    /**
     *
     * @return
     */
    public String getVersion() {
		return "$Date$";
	}

	public List getOptionalKeys() {
		return new ArrayList(0);
	}

	public List getRequiredKeys() {
		return new ArrayList(0);
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

}
