package com.energyict.genericprotocolimpl.webrtuz3;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.Link;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.Register;
import com.energyict.genericprotocolimpl.common.CommonUtils;
import com.energyict.genericprotocolimpl.webrtuz3.profiles.DailyMonthly;
import com.energyict.genericprotocolimpl.webrtuz3.profiles.EDevice;
import com.energyict.genericprotocolimpl.webrtuz3.profiles.ElectricityProfile;
import com.energyict.genericprotocolimpl.webrtuz3.profiles.EventProfile;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.core.Channel;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Rtu;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * Copyrights EnergyICT
 *
 * @since 9-apr-2010 13:51:14
 * @author jme
 */
public class EMeter implements GenericProtocol, EDevice {

	public static final ObisCode PROFILE_OBISCODE = ObisCode.fromString("0.0.99.1.0.255");

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
			ElectricityProfile mp = new ElectricityProfile(this);
			ProfileData pd = mp.getProfile(getProfileObisCode());
			if(this.webRtu.isBadTime()){
				pd.markIntervalsAsBadTime();
			}
			this.webRtu.getStoreObject().add(pd, geteMeterRtu());
		}

		if(commProfile.getReadMeterEvents()){
			getLogger().log(Level.INFO, "Getting events for meter with serialnumber: " + geteMeterRtu().getSerialNumber());
			EventProfile mep = new EventProfile(this);
			ProfileData eventPd = mep.getEvents();
			this.webRtu.getStoreObject().add(eventPd, geteMeterRtu());
		}

		// import daily/monthly
		if(commProfile.getReadMeterReadings()){
			DailyMonthly mdm = new DailyMonthly(this);

			if(getWebRTU().isReadDaily()){
				getLogger().log(Level.INFO, "Getting Daily values for meter with serialnumber: " + geteMeterRtu().getSerialNumber());
				ProfileData dailyPd = mdm.getDailyValues(getMeterConfig().getDailyProfileObject().getObisCode());
				this.webRtu.getStoreObject().add(dailyPd, geteMeterRtu());
			}

			if(getWebRTU().isReadMonthly()){
				getLogger().log(Level.INFO, "Getting Monthly values for meter with serialnumber: " + geteMeterRtu().getSerialNumber());
				ProfileData montProfileData = mdm.getMonthlyValues(getMeterConfig().getMonthlyProfileObject().getObisCode());
				this.webRtu.getStoreObject().add(montProfileData, geteMeterRtu());

			}
			getLogger().log(Level.INFO, "Getting registers from Mbus meter " + (getPhysicalAddress()+1));
			doReadRegisters();
		}

		// send rtuMessages
		if(commProfile.getSendRtuMessage()){
			sendMeterMessages();
		}
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
						rv = readRegister(ProtocolTools.setObisCodeField(oc, 1, (byte) getPhysicalAddress()), oc);
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
     * @param originalObisCode
     * @return
     * @throws IOException
     */
	private RegisterValue readRegister(ObisCode obisCode, ObisCode originalObisCode) throws IOException {
		try {
			Register register = getCosemObjectFactory().getRegister(obisCode);
			return new RegisterValue(originalObisCode, register.getQuantityValue());
		} catch (Exception e) {
			e.printStackTrace();
			throw new NoSuchRegisterException(e.getMessage());
		}
	}

	/**
	 *
	 */
	private void sendMeterMessages() {
		// TODO Auto-generated method stub

	}

    /**
     *
     * @return
     */
    public ObisCode getProfileObisCode() {
		return ProtocolTools.setObisCodeField(PROFILE_OBISCODE, 1, (byte) physicalAddress);
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
		return getMeter().getTimeZone();
	}

	public DLMSMeterConfig getMeterConfig() {
		return getWebRTU().getMeterConfig();
	}

}
