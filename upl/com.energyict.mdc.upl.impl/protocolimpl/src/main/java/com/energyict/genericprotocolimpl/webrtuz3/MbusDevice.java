package com.energyict.genericprotocolimpl.webrtuz3;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Unit;
import com.energyict.dialer.core.Link;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Data;
import com.energyict.genericprotocolimpl.common.CommonUtils;
import com.energyict.genericprotocolimpl.common.DLMSProtocol;
import com.energyict.genericprotocolimpl.webrtu.common.obiscodemappers.MbusObisCodeMapper;
import com.energyict.genericprotocolimpl.webrtuz3.messagehandling.MbusMessageExecutor;
import com.energyict.genericprotocolimpl.webrtuz3.messagehandling.MbusMessages;
import com.energyict.genericprotocolimpl.webrtuz3.profiles.MbusDailyMonthly;
import com.energyict.genericprotocolimpl.webrtuz3.profiles.MbusEventProfile;
import com.energyict.genericprotocolimpl.webrtuz3.profiles.MbusProfile;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.amr.RtuRegister;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * @author gna
 * Changes:
 * GNA |27012009| Instead of using the nodeAddress as channelnumber we search for the channelnumber by looking at the mbusSerialNumbers
 * GNA |28012009| Added the connect/disconnect messages. There is an option to enter an activationDate but there is no Object description for the
 * 					Mbus disconnect controller yet ...
 * GNA |04022009| Mbus connect/disconnect can be applied with a scheduler. We use 0.x.24.6.0.255 as the ControlScheduler and 0.x.24.7.0.255 as ScriptTable
 * GNA |19022009| Added a message to change to connectMode of the disconnectorObject;
 * 					Changed all messageEntrys in date-form to a UnixTime entry;
 */

public class MbusDevice extends MbusMessages implements GenericProtocol {

    private static final ObisCode PROFILE_OBISCODE = ObisCode.fromString("0.0.24.3.0.255");
    public static final ObisCode MONTHLY_PROFILE_OBIS = ObisCode.fromString("0.0.98.1.0.255");
    public static final ObisCode DAILY_PROFILE_OBIS = ObisCode.fromString("0.0.99.2.0.255");

    private long mbusAddress	= -1;		// this is the address that was given by the E-meter or a hardcoded MBusAddress in the MBusMeter itself
	private int physicalAddress = -1;		// this is the orderNumber of the MBus meters on the E-meter, we need this to compute the ObisRegisterValues
	private int medium = 15;				// value of an unknown medium
	private String customerID;
	private boolean valid;

	public Rtu	mbus;
	public CommunicationProfile commProfile;
	private WebRTUZ3 webRtu;
	private Logger logger;
	private Unit mbusUnit;
	private MbusObisCodeMapper mocm = null;

	public MbusDevice(){
		this.valid = false;
	}

	public MbusDevice(String serial, Rtu mbusRtu, Logger logger){
		this(0, 0, serial, 15, mbusRtu, Unit.get(BaseUnit.UNITLESS), logger);
	}

	public MbusDevice(String serial, int physicalAddress, Rtu mbusRtu, Logger logger){
		this(0, physicalAddress, serial, 15, mbusRtu, Unit.get(BaseUnit.UNITLESS), logger);
	}

	public MbusDevice(long mbusAddress, int phyaddress, String serial, int medium, Rtu mbusRtu, Unit mbusUnit, Logger logger){
		this.mbusAddress = mbusAddress;
		this.physicalAddress = phyaddress;
		this.medium = medium;
		this.customerID = serial;
		this.mbusUnit = mbusUnit;
		this.mbus = mbusRtu;
		this.logger = logger;
		this.valid = true;
	}

	private void verifySerialNumber() throws IOException{
		String serial;
		String eiSerial = getMbus().getSerialNumber();
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

	private CosemObjectFactory getCosemObjectFactory(){
		return getWebRTU().getCosemObjectFactory();
	}

	private DLMSMeterConfig getMeterConfig(){
		return getWebRTU().getMeterConfig();
	}

	public boolean isValid() {
		return valid;
	}

	public String getCustomerID() {
		return this.customerID;
	}

	public String getVersion() {
		return "$Date$";
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
			getLogger().log(Level.INFO, "Getting loadProfile for meter with serialnumber: " + getMbus().getSerialNumber());
			MbusProfile mp = new MbusProfile(this);
			ProfileData pd = mp.getProfile(getProfileObisCode());
			if(this.webRtu.isBadTime()){
				pd.markIntervalsAsBadTime();
			}
			this.webRtu.getStoreObject().add(pd, getMbus());
		}

		if(commProfile.getReadMeterEvents()){
			getLogger().log(Level.INFO, "Getting events for meter with serialnumber: " + getMbus().getSerialNumber());
			MbusEventProfile mep = new MbusEventProfile(this);
			ProfileData eventPd = mep.getEvents();
			this.webRtu.getStoreObject().add(eventPd, getMbus());
		}

		// import daily/monthly
		if(commProfile.getReadMeterReadings()){
			MbusDailyMonthly mdm = new MbusDailyMonthly(this);

			if(getWebRTU().isReadDaily()){
				getLogger().log(Level.INFO, "Getting Daily values for meter with serialnumber: " + getMbus().getSerialNumber());
                ProfileData dailyPd = mdm.getDailyProfile(getCorrectedObisCode(DAILY_PROFILE_OBIS));
				this.webRtu.getStoreObject().add(dailyPd, getMbus());
			}

			if(getWebRTU().isReadMonthly()){
				getLogger().log(Level.INFO, "Getting Monthly values for meter with serialnumber: " + getMbus().getSerialNumber());
                ProfileData montProfileData = mdm.getMonthlyProfile(getCorrectedObisCode(MONTHLY_PROFILE_OBIS));
				this.webRtu.getStoreObject().add(montProfileData, getMbus());

			}
			getLogger().log(Level.INFO, "Getting registers from Mbus meter " + (getPhysicalAddress()+1));
			doReadRegisters();
		}

		// send rtuMessages
		if(commProfile.getSendRtuMessage()){
			sendMeterMessages();
		}
	}

    private void testMethod() {
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
    }


	/**
	 * We don't use the {@link DLMSProtocol#doReadRegisters()} method because we need to adjust the mbusChannel
	 * @throws IOException
	 */
	private void doReadRegisters() throws IOException{
		Iterator it = getMbus().getRegisters().iterator();
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
						rv = readRegister(getCorrectedObisCode(oc));
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

	private void sendMeterMessages() throws BusinessException, SQLException {
		MbusMessageExecutor messageExecutor = new MbusMessageExecutor(this);

		Iterator<RtuMessage> it = getMbus().getPendingMessages().iterator();
		RtuMessage rm = null;
		while(it.hasNext()){
			rm = it.next();
			messageExecutor.doMessage(rm);
		}
	}

	private RegisterValue readRegister(ObisCode oc) throws IOException{
		if(this.mocm == null){
			this.mocm = new MbusObisCodeMapper(getCosemObjectFactory());
		}
		return mocm.getRegisterValue(oc);
	}

	public Rtu getMbus(){
		return this.mbus;
	}

	public int getPhysicalAddress(){
		return this.physicalAddress;
	}

	public void addProperties(Properties properties) {
	}

	public List getOptionalKeys() {
		return new ArrayList(0);
	}

	public List getRequiredKeys() {
		return new ArrayList(0);
	}

	public Logger getLogger() {
		return this.logger;
	}

	public void setWebRtu(WebRTUZ3 webRTUKP) {
		this.webRtu = webRTUKP;
	}

	public WebRTUZ3 getWebRTU(){
		return this.webRtu;
	}

	public long getTimeDifference() {
		return 0;
	}

	public ObisCode getProfileObisCode() {
		return ProtocolTools.setObisCodeField(PROFILE_OBISCODE, 1, (byte) physicalAddress);
	}

	@Override
	public String toString() {
		return "[" + physicalAddress + "] " + customerID;
	}

    /**
     *
      * @param baseObisCode
     * @return
     */
    public ObisCode getCorrectedObisCode(ObisCode baseObisCode) {
		return ProtocolTools.setObisCodeField(baseObisCode, 1, (byte) physicalAddress);
	}
    

}
