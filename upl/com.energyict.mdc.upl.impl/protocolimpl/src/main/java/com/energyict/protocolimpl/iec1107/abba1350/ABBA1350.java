package com.energyict.protocolimpl.iec1107.abba1350;

import java.io.*;
import java.math.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.*;

import com.energyict.cbo.*;
import com.energyict.dialer.connection.*;
import com.energyict.dialer.core.*;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.iec1107.*;
import com.energyict.protocolimpl.iec1107.vdew.*;
import com.energyict.protocol.messaging.*;
import com.energyict.obis.ObisCode;

/**
 * @version 1.0
 * @author Koenraad Vanderschaeve
 * @author fbl
 * @author jme
 * @beginchanges 
 * 18-11-2008 jme > Implemented MessageProtocol to support messages. Messages for new Switch Point Clock data from alphaSET 3.0
 * 20-11-2008 jme > Added check for serial number match
 * 24-11-2008 jme > Added firmware version and hardware key readout
 * 24-11-2008 jme > Added support for power Quality readout (P.02)
 * 27-11-2008 jme > Added support for register readout from 
 * @endchanges
 */
public class ABBA1350 
implements  MeterProtocol, HHUEnabler, ProtocolLink, MeterExceptionInfo, 
RegisterProtocol, MessageProtocol {

	private final static int DEBUG = 0;

	private static final int MIN_LOADPROFILE = 1;
	private static final int MAX_LOADPROFILE = 2;

	private String strID;
	private String strPassword;
	private int iIEC1107TimeoutProperty;
	private int iProtocolRetriesProperty;
	private int iRoundtripCorrection;
	private int iSecurityLevel;
	private String nodeId;
	private String serialNumber;
	private int iEchoCancelling;
	private int iForceDelay;

	private int profileInterval;
	private ChannelMap channelMap;
	private int requestHeader;
	private ProtocolChannelMap protocolChannelMap = null;
	private int scaler;
	private int dataReadoutRequest;
	private int loadProfileNumber;

	private TimeZone timeZone;
	private Logger logger;
	private int extendedLogging;
	private int vdewCompatible;
	private int failOnUnitMismatch = 0;

	private FlagIEC1107Connection flagIEC1107Connection = null;
	private ABBA1350Registry abba1350Registry = null;
	private ABBA1350Profile abba1350Profile = null;
	private ABBA1350Messages abba1350Messages = new ABBA1350Messages(this);
	private ABBA1350ObisCodeMapper abba1350ObisCodeMapper = new ABBA1350ObisCodeMapper(this);

	private byte[] dataReadout = null;
	private int [] billingCount;
	private String firmwareVersion = null;
	private Date meterDate = null;
	private String meterSerial = null;

	/** Creates a new instance of ABBA1350, empty constructor */
	public ABBA1350() {
	} 

	public ProfileData getProfileData(boolean includeEvents) throws IOException {
		Calendar calendar = ProtocolUtils.getCalendar(timeZone);
		calendar.add(Calendar.YEAR, -10);
		return getProfileData(calendar.getTime(), includeEvents);
	}

	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
		return getAbba1350Profile().getProfileData(lastReading, includeEvents, loadProfileNumber);
	}

	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException,
	UnsupportedException {
		return getAbba1350Profile().getProfileData(from, to, includeEvents, loadProfileNumber);
	}

	public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
		throw new UnsupportedException();
	}

	public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
		throw new UnsupportedException();
	}

	/**
	 * This method sets the time/date in the remote meter equal to the system 
	 * time/date of the machine where this object resides.
	 * 
	 * @exception IOException
	 */

	public void setTime() throws IOException {
		if (vdewCompatible == 1)
			setTimeVDEWCompatible();
		else
			setTimeAlternativeMethod();
	}

	
	
	private void setTimeAlternativeMethod() throws IOException {
		Calendar calendar = null;
		calendar = ProtocolUtils.getCalendar(timeZone);
		calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
		Date date = calendar.getTime();
		getAbba1350Registry().setRegister("TimeDate2", date);
	} // public void setTime() throws IOException

	private void setTimeVDEWCompatible() throws IOException {
		Calendar calendar = null;
		calendar = ProtocolUtils.getCalendar(timeZone);
		calendar.add(Calendar.MILLISECOND, iRoundtripCorrection);
		Date date = calendar.getTime();
		getAbba1350Registry().setRegister("Time", date);
		getAbba1350Registry().setRegister("Date", date);
	} // public void setTime() throws IOException

	public Date getTime() throws IOException {
		sendDebug("getTime request !!!", 2);
		//if (this.meterDate == null) 
			this.meterDate = (Date) getAbba1350Registry().getRegister("TimeDate");
		return new Date(this.meterDate.getTime() - iRoundtripCorrection);
	}


	/** ************************************ MeterProtocol implementation ************************************** */

	/**
	 * This implementation calls <code> validateProperties </code> and assigns 
	 * the argument to the properties field
	 */
	public void setProperties(Properties properties) 
	throws MissingPropertyException, InvalidPropertyException {

		validateProperties(properties);

	}

	/**
	 * Validates the properties.  The default implementation checks that all 
	 * required parameters are present.
	 */
	private void validateProperties(Properties properties) 
	throws MissingPropertyException, InvalidPropertyException {

		try {
			Iterator iterator = getRequiredKeys().iterator();
			while (iterator.hasNext()) {
				String key = (String) iterator.next();
				if (properties.getProperty(key) == null)
					throw new MissingPropertyException(key + " key missing");
			}
			strID = properties.getProperty(MeterProtocol.ADDRESS, "");
			strPassword = properties.getProperty(MeterProtocol.PASSWORD);
			serialNumber=properties.getProperty(MeterProtocol.SERIALNUMBER);
			iIEC1107TimeoutProperty = Integer.parseInt(properties.getProperty("Timeout", "20000").trim());
			iProtocolRetriesProperty = Integer.parseInt(properties.getProperty("Retries", "5").trim());
			iRoundtripCorrection = Integer.parseInt(properties.getProperty("RoundtripCorrection", "0").trim());
			iSecurityLevel = Integer.parseInt(properties.getProperty("SecurityLevel", "1").trim());
			nodeId = properties.getProperty(MeterProtocol.NODEID, "");
			iEchoCancelling = Integer.parseInt(properties.getProperty("EchoCancelling", "0").trim());
			iForceDelay = Integer.parseInt(properties.getProperty("ForceDelay", "0").trim());
			profileInterval = Integer.parseInt(properties.getProperty("ProfileInterval", "3600").trim());
			channelMap = new ChannelMap(properties.getProperty("ChannelMap", "0"));
			requestHeader = Integer.parseInt(properties.getProperty("RequestHeader", "1").trim());
			protocolChannelMap = new ProtocolChannelMap(properties.getProperty("ChannelMap", "0:0:0:0:0:0"));
			scaler = Integer.parseInt(properties.getProperty("Scaler", "0").trim());
			dataReadoutRequest = Integer.parseInt(properties.getProperty("DataReadout", "0").trim());
			extendedLogging = Integer.parseInt(properties.getProperty("ExtendedLogging", "0").trim());
			vdewCompatible = Integer.parseInt(properties.getProperty("VDEWCompatible", "0").trim());
			loadProfileNumber = Integer.parseInt(properties.getProperty("LoadProfileNumber", "1"));
			//failOnUnitMismatch = Integer.parseInt(properties.getProperty("FailOnUnitMismatch", "0"));

		} catch (NumberFormatException e) {
			throw new InvalidPropertyException("DukePower, validateProperties, NumberFormatException, "
					+ e.getMessage());
		}
		
//		if ((failOnUnitMismatch < 0) || (loadProfileNumber > 1)) 
//			throw new InvalidPropertyException("Invalid value for failOnUnitMismatch (" + failOnUnitMismatch + ") This property can only be 1 (to enable) or 0 (to disable). "); 

		if ((loadProfileNumber < MIN_LOADPROFILE) || (loadProfileNumber > MAX_LOADPROFILE)) 
			throw new InvalidPropertyException("Invalid loadProfileNumber (" + loadProfileNumber + "). Minimum value: " + MIN_LOADPROFILE + " Maximum value: " + MAX_LOADPROFILE); 

	}

	protected boolean isDataReadout() {
		return (dataReadoutRequest == 1);
	}

	public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byteArrayOutputStream.write(name.getBytes());
		flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
		byte[] data = flagIEC1107Connection.receiveRawData();
		return new String(data);
	}

	public void setRegister(String name, String value) throws IOException, NoSuchRegisterException,
	UnsupportedException {
		getAbba1350Registry().setRegister(name, value);
	}

	/**
	 * this implementation throws UnsupportedException. Subclasses may override
	 * 
	 */
	public void initializeDevice() throws IOException, UnsupportedException {
		throw new UnsupportedException();
	}

	/**
	 * the implementation returns both the address and password key
	 * 
	 * @return a list of strings
	 */
	public List getRequiredKeys() {
		List result = new ArrayList(0);
		return result;
	}

	/**
	 * this implementation returns an empty list
	 * 
	 * @return a list of strings
	 */
	public List getOptionalKeys() {
		List result = new ArrayList();
		result.add("LoadProfileNumber");
		result.add("Timeout");
		result.add("Retries");
		result.add("SecurityLevel");
		result.add("EchoCancelling");
		result.add("ChannelMap");
		result.add("RequestHeader");
		result.add("Scaler");
		result.add("DataReadout");
		result.add("ExtendedLogging");
		result.add("VDEWCompatible");
		result.add("ForceDelay");
		//result.add("FailOnUnitMismatch");
		return result;
	}

	public String getProtocolVersion() {
		return "$Revision: 1.7 $";
	}

	public String getFirmwareVersion() throws IOException, UnsupportedException {
		if (this.firmwareVersion == null) {
			this.firmwareVersion = (String)getAbba1350Registry().getRegister(abba1350Registry.FIRMWAREID);
		}
		return this.firmwareVersion;
	} // public String getFirmwareVersion()

	/**
	 * initializes the receiver
	 * 
	 */
	public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) {
		this.timeZone = timeZone;
		this.logger = logger;

		try {
			flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, iIEC1107TimeoutProperty,
					iProtocolRetriesProperty, iForceDelay, iEchoCancelling, 1);
			abba1350Registry = new ABBA1350Registry(this, this);
			abba1350Profile = new ABBA1350Profile(this, this, abba1350Registry);

		} catch (ConnectionException e) {
			if (logger != null) logger.severe("ABBA1350: init(...), " + e.getMessage());
		}

	}

	/**
	 * @throws IOException
	 */
	public void connect() throws IOException {
		try {
			if ((getFlagIEC1107Connection().getHhuSignOn() == null) && (isDataReadout())) {
				dataReadout = cleanDataReadout(flagIEC1107Connection.dataReadout(strID, nodeId));
				flagIEC1107Connection.disconnectMAC();
			}

			flagIEC1107Connection.connectMAC(strID, strPassword, iSecurityLevel, nodeId);

			if ((getFlagIEC1107Connection().getHhuSignOn() != null) && (isDataReadout())) {
				dataReadout = cleanDataReadout(getFlagIEC1107Connection().getHhuSignOn().getDataReadout());
			}

		} catch (FlagIEC1107ConnectionException e) {
			throw new IOException(e.getMessage());
		}


		validateSerialNumber();
		abba1350ObisCodeMapper.initObis();

		if (extendedLogging >= 2) getMeterInfo();
		if (extendedLogging >= 1) getRegistersInfo();

	}

	private byte[] cleanDataReadout(byte[] dro) {
		if (DEBUG >= 1) sendDebug("cleanDataReadout()  INPUT dro = " + new String(dro), 2);

		for (int i = 0; i < dro.length; i++) {
			if (((i+3) < dro.length) && (dro[i] == '&')) {
				if (dro[i+3] == '(') dro[i] = '*';
			}
		}
		if (DEBUG >= 1) sendDebug("cleanDataReadout() OUTPUT dro = " + new String(dro), 2);
		return dro;
	}

	public void disconnect() throws IOException {
		try {
			flagIEC1107Connection.disconnectMAC();
		} catch (FlagIEC1107ConnectionException e) {
			logger.severe("disconnect() error, " + e.getMessage());
		}
	}

	public int getNumberOfChannels() throws UnsupportedException, IOException {
		if (requestHeader == 1)
			return getAbba1350Profile().getProfileHeader(loadProfileNumber).getNrOfChannels();
		else
			return getProtocolChannelMap().getNrOfProtocolChannels();
	}

	public int getISecurityLevel() {
		return iSecurityLevel;
	}

	public int getProfileInterval() throws UnsupportedException, IOException {
		if (requestHeader == 1)
			return getAbba1350Profile().getProfileHeader(loadProfileNumber).getProfileInterval();
		else
			return profileInterval;
	}

	// Implementation of interface ProtocolLink
	public FlagIEC1107Connection getFlagIEC1107Connection() {
		return flagIEC1107Connection;
	}

	public TimeZone getTimeZone() {
		return timeZone;
	}

	public boolean isIEC1107Compatible() {
		return true;
	}

	public String getPassword() {
		return strPassword;
	}

	public byte[] getDataReadout() {
		return dataReadout;
	}

	public Object getCache() {
		return null;
	}

	public Object fetchCache(int rtuid) throws SQLException, BusinessException {
		return null;
	}

	public void setCache(Object cacheObject) {
	}

	public void updateCache(int rtuid, Object cacheObject) 
	throws SQLException, BusinessException { }

	public ChannelMap getChannelMap() {
		return channelMap;
	}

	public void release() throws IOException {}

	public Logger getLogger() {
		return logger;
	}

	static Map exceptionInfoMap = new HashMap();
	static {
		exceptionInfoMap.put("ERROR", "Request could not execute!");
		exceptionInfoMap.put("ERROR01", "A1350 ERROR 01, invalid command!");
		exceptionInfoMap.put("ERROR06", "A1350 ERROR 06, invalid command!");
	}

	public String getExceptionInfo(String id) {
		String exceptionInfo = (String) exceptionInfoMap.get(ProtocolUtils.stripBrackets(id));
		if (exceptionInfo != null)
			return id + ", " + exceptionInfo;
		else
			return "No meter specific exception info for " + id;
	}

	public int getNrOfRetries() {
		return iProtocolRetriesProperty;
	}

	/**
	 * Getter for property requestHeader.
	 * 
	 * @return Value of property requestHeader.
	 */
	public boolean isRequestHeader() {
		return requestHeader == 1;
	}

	public ProtocolChannelMap getProtocolChannelMap() {
		return protocolChannelMap;
	}


	/* Translate the obis codes to edis codes, and read */ 
	public RegisterValue readRegister(ObisCode obis) throws IOException {

		try {

			sendDebug("readRegister() obis: " + obis.toString(), 2); 
			// it is not possible to translate the following edis code in this way
			if( "1.1.0.1.2.255".equals(obis.toString())) return new RegisterValue(obis, readTime());

			if( "1.1.0.0.0.255".equals(obis.toString())) return new RegisterValue(obis, getMeterSerial());
			if( "1.1.0.2.0.255".equals(obis.toString())) return new RegisterValue(obis, getFirmwareVersion());

			if( "1.1.0.0.1.255".equals(obis.toString())) return new RegisterValue(obis, readSpecialRegister((String)abba1350ObisCodeMapper.getObisMap().get(obis.toString())));
			if( "1.1.0.0.2.255".equals(obis.toString())) return new RegisterValue(obis, readSpecialRegister((String)abba1350ObisCodeMapper.getObisMap().get(obis.toString())));
			if( "1.1.0.0.3.255".equals(obis.toString())) return new RegisterValue(obis, readSpecialRegister((String)abba1350ObisCodeMapper.getObisMap().get(obis.toString())));
			if( "1.1.0.0.4.255".equals(obis.toString())) return new RegisterValue(obis, readSpecialRegister((String)abba1350ObisCodeMapper.getObisMap().get(obis.toString())));
			if( "1.1.0.0.5.255".equals(obis.toString())) return new RegisterValue(obis, readSpecialRegister((String)abba1350ObisCodeMapper.getObisMap().get(obis.toString())));
			if( "1.1.0.0.6.255".equals(obis.toString())) return new RegisterValue(obis, readSpecialRegister((String)abba1350ObisCodeMapper.getObisMap().get(obis.toString())));
			if( "1.1.0.0.7.255".equals(obis.toString())) return new RegisterValue(obis, readSpecialRegister((String)abba1350ObisCodeMapper.getObisMap().get(obis.toString())));
			if( "1.1.0.0.8.255".equals(obis.toString())) return new RegisterValue(obis, readSpecialRegister((String)abba1350ObisCodeMapper.getObisMap().get(obis.toString())));
			if( "1.1.0.0.9.255".equals(obis.toString())) return new RegisterValue(obis, readSpecialRegister((String)abba1350ObisCodeMapper.getObisMap().get(obis.toString())));
			if( "1.1.0.0.10.255".equals(obis.toString())) return new RegisterValue(obis, readSpecialRegister((String)abba1350ObisCodeMapper.getObisMap().get(obis.toString())));

			String fs = "";
			if( obis.getF() != 255 ) {
				int f = getBillingCount() - Math.abs(obis.getF());
				fs = "*" + ProtocolUtils.buildStringDecimal(f, 2);
			}
			String edis = obis.getC() + "." + obis.getD() + "." + obis.getE() + fs;
			byte[] data;
			try {
				data = read(edis);
			} catch (IOException e1) {
				if (DEBUG >= 3) e1.printStackTrace();
				throw e1;
			}

			sendDebug("Readregister Edis: " + edis + " Data: " + new String(data), 3);
			
			DataParser dp = new DataParser(getTimeZone());
			String temp = dp.parseBetweenBrackets(data, 0, 0);
			Unit readUnit = null; 
			if (temp.indexOf('*') != -1) {
				readUnit = Unit.get(temp.substring(temp.indexOf('*') + 1));
				temp = temp.substring(0, temp.indexOf('*'));
				sendDebug("ReadUnit: " + readUnit, 3);
			}

			sendDebug("Readregister Edis: " + edis + " Data: " + new String(data) + " temp: " + temp, 3);
			
			BigDecimal bd = new BigDecimal(temp);
			Date date = null;

			sendDebug("Readregister Edis: " + edis + " bd: " + bd, 3);

			try {

				String dString = dp.parseBetweenBrackets(data, 0, 1);

				if( "0000000000".equals(dString) ) 
					throw new NoSuchRegisterException();

				VDEWTimeStamp vts = new VDEWTimeStamp(getTimeZone());
				vts.parse(dString);
				date = vts.getCalendar().getTime();

			} catch (DataParseException e) {
				if (DEBUG >= 3) e.printStackTrace();
			} catch (NoSuchRegisterException e) {
				if (DEBUG >= 3) e.printStackTrace();
				return new RegisterValue(obis, null, null, null);
			}

			Quantity q = null;
			if (obis.getUnitElectricity(scaler).isUndefined()) {
				q = new Quantity(bd, obis.getUnitElectricity(0));
			} else {
				if (readUnit != null) {
					if (!readUnit.equals(obis.getUnitElectricity(scaler))) {
						String message = "Unit or scaler from obiscode is different from register Unit in meter!!! ";
						message += " (Unit from meter: " + readUnit;
						message += " -  Unit from obiscode: " + obis.getUnitElectricity(scaler) + ")\n";
						
						sendDebug(message);
						getLogger().info(message);
						if (failOnUnitMismatch == 1) throw new InvalidPropertyException(message);
					}
				} 
				q = new Quantity(bd, obis.getUnitElectricity(scaler));
			}
			
			return new RegisterValue(obis, q, date, null);

		} catch (NoSuchRegisterException e) {
			String m = "ObisCode " + obis.toString() + " is not supported!";
			if (DEBUG >= 3) e.printStackTrace();
			throw new NoSuchRegisterException(m);
		} catch (InvalidPropertyException e) {
			String m = "getMeterReading() error, " + e.getMessage();
			if (DEBUG >= 3) e.printStackTrace();
			throw new InvalidPropertyException(m);
		} catch (FlagIEC1107ConnectionException e) {
			String m = "getMeterReading() error, " + e.getMessage();
			if (DEBUG >= 3) e.printStackTrace();
			throw new IOException(m);
		} catch (IOException e) {
			String m = "getMeterReading() error, " + e.getMessage();
			if (DEBUG >= 3) e.printStackTrace();
			throw new IOException(m);
		} catch (NumberFormatException e) {
			String m = "ObisCode " + obis.toString() + " is not supported!";
			if (DEBUG >= 3) e.printStackTrace();
			throw new NoSuchRegisterException(m);
		}

	}

	private byte[] read(String edisNotation) throws IOException {
		byte[] data;
		if (!isDataReadout()) {
			String name = edisNotation + "(;)";
			sendDebug("Requesting read(): edisNotation = " + edisNotation, 2);
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			byteArrayOutputStream.write(name.getBytes());
			flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream
					.toByteArray());
			data = flagIEC1107Connection.receiveRawData();
		} else {
			sendDebug("Requesting read(): edisNotation = " + edisNotation + " dataReadOut: " + getDataReadout().length, 2);
			DataDumpParser ddp = new DataDumpParser(getDataReadout());
			data = ddp.getRegisterStrValue(edisNotation).getBytes();
		}
		return data;
	}

	Quantity readTime( ) throws IOException {
		Long seconds = new Long(getTime().getTime() / 1000);
		return new Quantity( seconds, Unit.get(BaseUnit.SECOND) );
	}

	public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
		sendDebug(" translateRegister(): " + obisCode.toString(), 2);
		String reginfo = (String) abba1350ObisCodeMapper.getObisMap().get(obisCode.toString());
		if (reginfo == null) reginfo = obisCode.getDescription();
		return new RegisterInfo("" + reginfo);
	}



	private void getRegistersInfo() throws IOException {
		StringBuffer rslt = new StringBuffer();

		Iterator i = abba1350ObisCodeMapper.getObisMap().keySet().iterator();
		while(i.hasNext()){
			String obis = (String)i.next();
			ObisCode oc = ObisCode.fromString(obis);

			if(DEBUG >= 5) {
				try {
					rslt.append( translateRegister(oc) + "\n" );
					rslt.append( readRegister(oc) + "\n" );
				} catch( NoSuchRegisterException nsre ) {
					// ignore and continue
				}
			} else {
				rslt.append( obis + " " + translateRegister(oc) + "\n");
			}

		}

		if (logger != null) logger.info(rslt.toString());
	}

	private void getMeterInfo() throws IOException {
		String returnString = "";
		if (iSecurityLevel < 1) {
			returnString = "Set the SecurityLevel > 0 to show more information about the meter.\n";
		} else {
			returnString += " Meter ID1: " + readSpecialRegister(ABBA1350ObisCodeMapper.ID1) + "\n";
			returnString += " Meter ID2: " + readSpecialRegister(ABBA1350ObisCodeMapper.ID2) + "\n";
			returnString += " Meter ID3: " + readSpecialRegister(ABBA1350ObisCodeMapper.ID3) + "\n";
			returnString += " Meter ID4: " + readSpecialRegister(ABBA1350ObisCodeMapper.ID4) + "\n";
			returnString += " Meter ID5: " + readSpecialRegister(ABBA1350ObisCodeMapper.ID5) + "\n";
			returnString += " Meter ID6: " + readSpecialRegister(ABBA1350ObisCodeMapper.ID6) + "\n"; 

			returnString += " Meter IEC1107 ID:" + readSpecialRegister(ABBA1350ObisCodeMapper.IEC1107_ID) + "\n";
			returnString += " Meter IECII07 address (optical):    " + readSpecialRegister(ABBA1350ObisCodeMapper.IEC1107_ADDRESS_OP) + "\n";
			returnString += " Meter IECII07 address (electrical): " + readSpecialRegister(ABBA1350ObisCodeMapper.IEC1107_ADDRESS_EL) + "\n";

		}
		if (logger != null) logger.info(returnString);
	}

	// ********************************************************************************************************
	// implementation of the HHUEnabler interface
	public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
		enableHHUSignOn(commChannel, isDataReadout());
	}

	public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
		HHUSignOn hhuSignOn = (HHUSignOn) new IEC1107HHUConnection(commChannel, iIEC1107TimeoutProperty,
				iProtocolRetriesProperty, 300, iEchoCancelling);
		hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
		hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
		hhuSignOn.enableDataReadout(datareadout);
		getFlagIEC1107Connection().setHHUSignOn(hhuSignOn);
	}

	public byte[] getHHUDataReadout() {
		return getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
	}

	public ABBA1350Registry getAbba1350Registry() {
		return abba1350Registry;
	}

	public ABBA1350Profile getAbba1350Profile() {
		return abba1350Profile;
	}

	int getBillingCount() throws IOException{
		if( billingCount == null ){

			if (isDataReadout()) {
				sendDebug("Requesting getBillingCount() dataReadOut: " + getDataReadout().length, 2);
				DataDumpParser ddp = new DataDumpParser(getDataReadout());
				billingCount = new int [] {ddp.getBillingCounter()};
			} else {

				String data;
				try {
					data = new String( read("0.1.0") );
				} catch (NoSuchRegisterException e) {
					if (!isDataReadout()) throw e;
					data = "()";
				}

				int start = data.indexOf('(') + 1;
				int stop = data.indexOf(')');
				String v = data.substring( start, stop );

				try {
					billingCount = new int [] { Integer.parseInt(v) };
				} catch (NumberFormatException e) {
					billingCount = new int [] {0}; 
					sendDebug("Unable to read billingCounter. Defaulting to 0!");
				}
			}

		}
		return billingCount[0];
	}

	private String getMeterSerial() throws IOException {
		if (this.meterSerial == null) {
			 this.meterSerial = (String)getAbba1350Registry().getRegister(abba1350Registry.SERIAL);
		}
		return this.meterSerial;
	}
	
	protected void validateSerialNumber() throws IOException {
		if ((serialNumber == null) || ("".compareTo(serialNumber)==0)) return;
		if (serialNumber.compareTo(getMeterSerial()) == 0) return;
		throw new IOException("SerialNumber mismatch! meter sn="+getMeterSerial()+", configured sn="+serialNumber);
	}

	/**
	 * Implementation of methods in MessageProtocol
	 */

	public void applyMessages(List messageEntries) throws IOException {
		abba1350Messages.applyMessages(messageEntries);
	}

	public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		return abba1350Messages.queryMessage(messageEntry);
	}

	public List getMessageCategories() {
		return abba1350Messages.getMessageCategories();
	}

	public String writeMessage(Message msg) {
		return abba1350Messages.writeMessage(msg);
	}

	public String writeTag(MessageTag tag) {
		return abba1350Messages.writeTag(tag);    
	}

	public String writeValue(MessageValue value) {
		return abba1350Messages.writeValue(value);
	}


	public void sendDebug(String str){
		if (DEBUG >= 1) {
			str = "######## DEBUG > " + str + "\n";
			Logger log = getLogger();
			if (log != null) {
				getLogger().info(str);
			} 
			else {
				System.out.println(str);
			}
		}
	}

	private String readSpecialRegister(String registerName) throws IOException {
		if (registerName.equals(ABBA1350ObisCodeMapper.ID1)) return new String(ProtocolUtils.convert2ascii(((String)getAbba1350Registry().getRegister(ABBA1350Registry.ID1)).getBytes()));
		if (registerName.equals(ABBA1350ObisCodeMapper.ID2)) return new String(ProtocolUtils.convert2ascii(((String)getAbba1350Registry().getRegister(ABBA1350Registry.ID2)).getBytes()));
		if (registerName.equals(ABBA1350ObisCodeMapper.ID3)) return new String(ProtocolUtils.convert2ascii(((String)getAbba1350Registry().getRegister(ABBA1350Registry.ID3)).getBytes()));
		if (registerName.equals(ABBA1350ObisCodeMapper.ID4)) return new String(ProtocolUtils.convert2ascii(((String)getAbba1350Registry().getRegister(ABBA1350Registry.ID4)).getBytes()));
		if (registerName.equals(ABBA1350ObisCodeMapper.ID5)) return new String(ProtocolUtils.convert2ascii(((String)getAbba1350Registry().getRegister(ABBA1350Registry.ID5)).getBytes()));
		if (registerName.equals(ABBA1350ObisCodeMapper.ID6)) return new String(ProtocolUtils.convert2ascii(((String)getAbba1350Registry().getRegister(ABBA1350Registry.ID6)).getBytes()));

		if (registerName.equals(ABBA1350ObisCodeMapper.IEC1107_ID)) return new String(ProtocolUtils.convert2ascii(((String)getAbba1350Registry().getRegister(ABBA1350Registry.IEC1107_ID)).getBytes()));
		if (registerName.equals(ABBA1350ObisCodeMapper.IEC1107_ADDRESS_OP)) return new String(ProtocolUtils.convert2ascii(((String)getAbba1350Registry().getRegister(ABBA1350Registry.IEC1107_ADDRESS_OP)).getBytes()));
		if (registerName.equals(ABBA1350ObisCodeMapper.IEC1107_ADDRESS_EL)) return new String(ProtocolUtils.convert2ascii(((String)getAbba1350Registry().getRegister(ABBA1350Registry.IEC1107_ADDRESS_EL)).getBytes()));
		if (registerName.equals(ABBA1350ObisCodeMapper.FIRMWAREID)) return getFirmwareVersion();

		if (registerName.equals(ABBA1350ObisCodeMapper.FIRMWARE)) {
			String fw = "";
			String hw = "";
			String dev = "";
			String fwdev = "";
			
			if (iSecurityLevel < 1) return "Unknown (SecurityLevel to low)";
			fwdev = (String)getAbba1350Registry().getRegister(ABBA1350Registry.FIRMWARE);
			hw = (String)getAbba1350Registry().getRegister(ABBA1350Registry.HARDWARE);
			
			if ((fwdev != null) && (fwdev.length() >= 30)) {
				fw = fwdev.substring(0, 10);
				dev = fwdev.substring(10, 30);
				fw = new String(ProtocolUtils.convert2ascii(fw.getBytes())).trim();
				dev = new String(ProtocolUtils.convert2ascii(dev.getBytes())).trim();
			} else {
				fw = "Unknown";
				dev = "Unknown";
			}
			
			if (hw != null) hw = new String(ProtocolUtils.convert2ascii(hw.getBytes())).trim();
				else hw = "Unknown";

			return dev + " " + "v" + fw + " " + hw;
		}
		
		return "";
	}
	
	private void sendDebug(String string, int i) {
		if (DEBUG >= i) sendDebug(string);
	}

	public static void main(String[] args) throws IOException {
		ABBA1350 abba = new ABBA1350();

		Dialer dialer=null;
		try {

			// direct rs232 connection
			dialer = DialerFactory.getOpticalDialer().newDialer();
			dialer.init("COM1");
			dialer.connect();

			// setup the properties (see AbstractProtocol for default properties)
			// protocol specific properties can be added by implementing doValidateProperties(..)
			Properties properties = new Properties();

			properties.setProperty("ChannelMap", "0:0:0:0:0:0");
			properties.setProperty(MeterProtocol.CORRECTTIME, "0");
			properties.setProperty("DataReadout", "0");
			properties.setProperty("ExtendedLogging", "1");
			properties.setProperty("LoadProfileNumber", "2");
			properties.setProperty(MeterProtocol.PASSWORD,"00000000");
			properties.setProperty(MeterProtocol.PROFILEINTERVAL, "600");
			properties.setProperty("RequestHeader", "0");
			properties.setProperty("SecurityLevel", "1");
			properties.setProperty("Timeout", "3000");
			properties.setProperty("VDEWCompatible", "1");



			//            iIEC1107TimeoutProperty = Integer.parseInt(properties.getProperty("Timeout", "20000").trim());
			//            iProtocolRetriesProperty = Integer.parseInt(properties.getProperty("Retries", "5").trim());
			//            iRoundtripCorrection = Integer.parseInt(properties.getProperty("RoundtripCorrection", "0").trim());
			//            iSecurityLevel = Integer.parseInt(properties.getProperty("SecurityLevel", "1").trim());
			//            nodeId = properties.getProperty(MeterProtocol.NODEID, "");
			//            iEchoCancelling = Integer.parseInt(properties.getProperty("EchoCancelling", "0").trim());
			//            profileInterval = Integer.parseInt(properties.getProperty("ProfileInterval", "3600").trim());
			//            channelMap = new ChannelMap(properties.getProperty("ChannelMap", "0"));
			//            requestHeader = Integer.parseInt(properties.getProperty("RequestHeader", "1").trim());
			//            protocolChannelMap = new ProtocolChannelMap(properties.getProperty("ChannelMap", "0:0:0:0:0:0"));
			//            scaler = Integer.parseInt(properties.getProperty("Scaler", "0").trim());
			//            dataReadoutRequest = Integer.parseInt(properties.getProperty("DataReadout", "0").trim());
			//            extendedLogging = Integer.parseInt(properties.getProperty("ExtendedLogging", "0").trim());
			//            vdewCompatible = Integer.parseInt(properties.getProperty("VDEWCompatible", "0").trim());
			//            loadProfileNumber = Integer.parseInt(properties.getProperty("LoadProfileNumber", "1"));


			//transfer the properties to the protocol
			abba.setProperties(properties);    

			// depending on the dialer, set the initial (pre-connect) communication parameters            
			//            dialer.getSerialCommunicationChannel().setParamsAndFlush(9600,
			//                                                                     SerialCommunicationChannel.DATABITS_7,
			//                                                                     SerialCommunicationChannel.PARITY_NONE,
			//                                                                     SerialCommunicationChannel.STOPBITS_1);
			// initialize the protocol
			abba.init(dialer.getInputStream(),dialer.getOutputStream(),TimeZone.getTimeZone("ECT"), null);

			// if optical head dialer, enable the HHU signon mechanism
			if (DialerMarker.hasOpticalMarker(dialer))
				((HHUEnabler)abba).enableHHUSignOn(dialer.getSerialCommunicationChannel());

			abba.connect(); // connect to the meter

			//            int aEnd = 16;
			//            int bEnd = 16;
			//            int cEnd = 16;
			//            
			//            for (int a = 0; a < aEnd; a++) {
			//                for (int b = 0; b < bEnd; b++) {
			//                    for (int c = 0; c < cEnd; c++) {
			//                    	if ((a > 9) || (b > 9) || (c > 9)) {
			//                    		String reg = 	ProtocolUtils.buildStringHex(a, 1).toUpperCase() + "." + 
			//                    		ProtocolUtils.buildStringHex(b, 1).toUpperCase() + "." + 
			//                    		ProtocolUtils.buildStringHex(c, 1).toUpperCase();
			//
			//                    		String result = new String(abba.read(reg));
			//                    		if (!result.equalsIgnoreCase(reg + "()")) abba.sendDebug(result);
			//                    	}
			//                    }
			//                }
			//			}


			int aEnd = 200;
			String startString = "1.6.0.99";

			for (int a = 0; a < aEnd; a++) {
				String reg = startString + ProtocolUtils.buildStringHex(a, 2).toUpperCase();
				String result = new String(abba.read(reg));
				if (!result.equalsIgnoreCase(reg + "()")) abba.sendDebug(" Reading register " + reg + ": " + result);
			}


			abba.disconnect();
			dialer.disConnect();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
} 
