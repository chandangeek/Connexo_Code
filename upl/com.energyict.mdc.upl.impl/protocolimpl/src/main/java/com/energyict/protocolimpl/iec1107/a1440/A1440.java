package com.energyict.protocolimpl.iec1107.a1440;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.MeterExceptionInfo;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocolimpl.base.DataDumpParser;
import com.energyict.protocolimpl.base.DataParseException;
import com.energyict.protocolimpl.base.DataParser;
import com.energyict.protocolimpl.base.ProtocolChannelMap;
import com.energyict.protocolimpl.iec1107.ChannelMap;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.VDEWTimeStamp;

/**
 * @author jme
 * @since 19-aug-2009
 * 
 * 19-08-2009 jme > Copied ABBA1350 protocol as base for new A1440 protocol
 * 
 */
public class A1440 implements MeterProtocol, HHUEnabler, ProtocolLink, MeterExceptionInfo, RegisterProtocol, MessageProtocol {

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
	private A1440Registry a1440Registry = null;
	private A1440Profile a1440Profile = null;
	private A1440Messages a1440Messages = new A1440Messages(this);
	private A1440ObisCodeMapper a1440ObisCodeMapper = new A1440ObisCodeMapper(this);

	private byte[] dataReadout = null;
	private int [] billingCount;
	private String firmwareVersion = null;
	private Date meterDate = null;
	private String meterSerial = null;

	private boolean software7E1;

	/** Creates a new instance of A1440, empty constructor */
	public A1440() {}

	public ProfileData getProfileData(boolean includeEvents) throws IOException {
		Calendar calendar = ProtocolUtils.getCalendar(this.timeZone);
		calendar.add(Calendar.YEAR, -10);
		return getProfileData(calendar.getTime(), includeEvents);
	}

	public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
		return getA1440Profile().getProfileData(lastReading, includeEvents, this.loadProfileNumber);
	}

	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
		return getA1440Profile().getProfileData(from, to, includeEvents, this.loadProfileNumber);
	}

	public Quantity getMeterReading(String name) throws UnsupportedException, IOException {
		throw new UnsupportedException();
	}

	public Quantity getMeterReading(int channelId) throws UnsupportedException, IOException {
		throw new UnsupportedException();
	}

	public void setTime() throws IOException {
		if (this.vdewCompatible == 1) {
			setTimeVDEWCompatible();
		} else {
			setTimeAlternativeMethod();
		}
	}

	private void setTimeAlternativeMethod() throws IOException {
		Calendar calendar = null;
		calendar = ProtocolUtils.getCalendar(this.timeZone);
		calendar.add(Calendar.MILLISECOND, this.iRoundtripCorrection);
		Date date = calendar.getTime();
		getA1440Registry().setRegister("TimeDate2", date);
	}

	private void setTimeVDEWCompatible() throws IOException {
		Calendar calendar = ProtocolUtils.getCalendar(this.timeZone);
		calendar.add(Calendar.MILLISECOND, this.iRoundtripCorrection);
		Date date = calendar.getTime();
		getA1440Registry().setRegister("Time", date);
		getA1440Registry().setRegister("Date", date);
	}

	public Date getTime() throws IOException {
		sendDebug("getTime request !!!", 2);
		this.meterDate = (Date) getA1440Registry().getRegister("TimeDate");
		return new Date(this.meterDate.getTime() - this.iRoundtripCorrection);
	}

	/** ************************************ MeterProtocol implementation ************************************** */

	/**
	 * This implementation calls <code> validateProperties </code> and assigns
	 * the argument to the properties field
	 */
	public void setProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
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
				if (properties.getProperty(key) == null) {
					throw new MissingPropertyException(key + " key missing");
				}
			}
			this.strID = properties.getProperty(MeterProtocol.ADDRESS, "");
			this.strPassword = properties.getProperty(MeterProtocol.PASSWORD);
			this.serialNumber=properties.getProperty(MeterProtocol.SERIALNUMBER);
			this.iIEC1107TimeoutProperty = Integer.parseInt(properties.getProperty("Timeout", "20000").trim());
			this.iProtocolRetriesProperty = Integer.parseInt(properties.getProperty("Retries", "5").trim());
			this.iRoundtripCorrection = Integer.parseInt(properties.getProperty("RoundtripCorrection", "0").trim());
			this.iSecurityLevel = Integer.parseInt(properties.getProperty("SecurityLevel", "1").trim());
			this.nodeId = properties.getProperty(MeterProtocol.NODEID, "");
			this.iEchoCancelling = Integer.parseInt(properties.getProperty("EchoCancelling", "0").trim());
			this.iForceDelay = Integer.parseInt(properties.getProperty("ForceDelay", "0").trim());
			this.profileInterval = Integer.parseInt(properties.getProperty("ProfileInterval", "3600").trim());
			this.channelMap = new ChannelMap(properties.getProperty("ChannelMap", "0"));
			this.requestHeader = Integer.parseInt(properties.getProperty("RequestHeader", "1").trim());
			this.protocolChannelMap = new ProtocolChannelMap(properties.getProperty("ChannelMap", "0:0:0:0:0:0"));
			this.scaler = Integer.parseInt(properties.getProperty("Scaler", "0").trim());
			this.dataReadoutRequest = Integer.parseInt(properties.getProperty("DataReadout", "0").trim());
			this.extendedLogging = Integer.parseInt(properties.getProperty("ExtendedLogging", "0").trim());
			this.vdewCompatible = Integer.parseInt(properties.getProperty("VDEWCompatible", "0").trim());
			this.loadProfileNumber = Integer.parseInt(properties.getProperty("LoadProfileNumber", "1"));
			this.software7E1 = !properties.getProperty("Software7E1", "0").equalsIgnoreCase("0");
		} catch (NumberFormatException e) {
			throw new InvalidPropertyException("DukePower, validateProperties, NumberFormatException, "	+ e.getMessage());
		}

		if ((this.loadProfileNumber < MIN_LOADPROFILE) || (this.loadProfileNumber > MAX_LOADPROFILE)) {
			throw new InvalidPropertyException("Invalid loadProfileNumber (" + this.loadProfileNumber + "). Minimum value: " + MIN_LOADPROFILE + " Maximum value: " + MAX_LOADPROFILE);
		}

	}

	protected boolean isDataReadout() {
		return (this.dataReadoutRequest == 1);
	}

	public String getRegister(String name) throws IOException, UnsupportedException, NoSuchRegisterException {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		byteArrayOutputStream.write(name.getBytes());
		this.flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream.toByteArray());
		byte[] data = this.flagIEC1107Connection.receiveRawData();
		return new String(data);
	}

	public void setRegister(String name, String value) throws IOException, NoSuchRegisterException,	UnsupportedException {

		if (name.equals("CONNECT")) {
			System.out.println("Received CONNECT message: " + value);
			A1440ContactorController cc = new A1440ContactorController(this);
			cc.doConnect();
		} else if (name.equals("DISCONNECT")) {
			System.out.println("Received DISCONNECT message: " + value);
			A1440ContactorController cc = new A1440ContactorController(this);
			cc.doDisconnect();
		} else if (name.equals("ARM")) {
			System.out.println("Received ARM message: " + value);
			A1440ContactorController cc = new A1440ContactorController(this);
			cc.doArm();
		} else {
			System.out.println("Received message: name = " + name + ", value = " + value);
			getA1440Registry().setRegister(name, value);
		}

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
		result.add("Software7E1");
		//result.add("FailOnUnitMismatch");
		return result;
	}

	public String getProtocolVersion() {
		return "$Revision: 1.1 $";
	}

	public String getFirmwareVersion() throws IOException, UnsupportedException {
		if (this.firmwareVersion == null) {
			this.firmwareVersion = (String)getA1440Registry().getRegister(this.a1440Registry.FIRMWAREID);
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
			this.flagIEC1107Connection = new FlagIEC1107Connection(inputStream, outputStream, this.iIEC1107TimeoutProperty,
					this.iProtocolRetriesProperty, this.iForceDelay, this.iEchoCancelling, 1, this.software7E1);
			this.a1440Registry = new A1440Registry(this, this);
			this.a1440Profile = new A1440Profile(this, this, this.a1440Registry);

		} catch (ConnectionException e) {
			if (logger != null) {
				logger.severe("A1440: init(...), " + e.getMessage());
			}
		}

	}

	/**
	 * @throws IOException
	 */
	public void connect() throws IOException {
		try {
			if ((getFlagIEC1107Connection().getHhuSignOn() == null) && (isDataReadout())) {
				this.dataReadout = cleanDataReadout(this.flagIEC1107Connection.dataReadout(this.strID, this.nodeId));
				// A1440 doesn't respond after sending a break in dataReadoutMode, so disconnect without sending break
				this.flagIEC1107Connection.disconnectMACWithoutBreak();
			}

			this.flagIEC1107Connection.connectMAC(this.strID, this.strPassword, this.iSecurityLevel, this.nodeId);

			if ((getFlagIEC1107Connection().getHhuSignOn() != null) && (isDataReadout())) {
				this.dataReadout = cleanDataReadout(getFlagIEC1107Connection().getHhuSignOn().getDataReadout());
			}

		} catch (FlagIEC1107ConnectionException e) {
			throw new IOException(e.getMessage());
		}


		validateSerialNumber();
		this.a1440ObisCodeMapper.initObis();

		if (this.extendedLogging >= 2) {
			getMeterInfo();
		}
		if (this.extendedLogging >= 1) {
			getRegistersInfo();
		}

	}

	private byte[] cleanDataReadout(byte[] dro) {
		if (DEBUG >= 1) {
			sendDebug("cleanDataReadout()  INPUT dro = " + new String(dro), 2);
		}

		for (int i = 0; i < dro.length; i++) {
			if (((i+3) < dro.length) && (dro[i] == '&')) {
				if (dro[i+3] == '(') {
					dro[i] = '*';
				}
			}
		}
		if (DEBUG >= 1) {
			sendDebug("cleanDataReadout() OUTPUT dro = " + new String(dro), 2);
		}
		return dro;
	}

	public void disconnect() throws IOException {
		try {
			this.flagIEC1107Connection.disconnectMAC();
		} catch (FlagIEC1107ConnectionException e) {
			getLogger().severe("disconnect() error, " + e.getMessage());
		}
	}

	public int getNumberOfChannels() throws UnsupportedException, IOException {
		if (this.requestHeader == 1) {
			return getA1440Profile().getProfileHeader(this.loadProfileNumber).getNrOfChannels();
		} else {
			return getProtocolChannelMap().getNrOfProtocolChannels();
		}
	}

	public int getISecurityLevel() {
		return this.iSecurityLevel;
	}

	public int getProfileInterval() throws UnsupportedException, IOException {
		if (this.requestHeader == 1) {
			return getA1440Profile().getProfileHeader(this.loadProfileNumber).getProfileInterval();
		} else {
			return this.profileInterval;
		}
	}

	// Implementation of interface ProtocolLink
	public FlagIEC1107Connection getFlagIEC1107Connection() {
		return this.flagIEC1107Connection;
	}

	public TimeZone getTimeZone() {
		return this.timeZone;
	}

	public boolean isIEC1107Compatible() {
		return true;
	}

	public String getPassword() {
		return this.strPassword;
	}

	public byte[] getDataReadout() {
		return this.dataReadout;
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
		return this.channelMap;
	}

	public void release() throws IOException {}

	public Logger getLogger() {
		return this.logger;
	}

	static Map exceptionInfoMap = new HashMap();
	static {
		exceptionInfoMap.put("ERROR", "Request could not execute!");
		exceptionInfoMap.put("ERROR01", "A1440 ERROR 01, invalid command!");
		exceptionInfoMap.put("ERROR06", "A1440 ERROR 06, invalid command!");
	}

	public String getExceptionInfo(String id) {
		String exceptionInfo = (String) exceptionInfoMap.get(ProtocolUtils.stripBrackets(id));
		if (exceptionInfo != null) {
			return id + ", " + exceptionInfo;
		} else {
			return "No meter specific exception info for " + id;
		}
	}

	public int getNrOfRetries() {
		return this.iProtocolRetriesProperty;
	}

	/**
	 * Getter for property requestHeader.
	 * 
	 * @return Value of property requestHeader.
	 */
	public boolean isRequestHeader() {
		return this.requestHeader == 1;
	}

	public ProtocolChannelMap getProtocolChannelMap() {
		return this.protocolChannelMap;
	}


	/* Translate the obis codes to edis codes, and read */
	public RegisterValue readRegister(ObisCode obis) throws IOException {
		DataParser dp = new DataParser(getTimeZone());
		Date eventTime = null;
		Date toTime = null;
		String fs = "";
		String toTimeString = "";
		byte[] data;
		byte[] timeStampData;

		try {

			sendDebug("readRegister() obis: " + obis.toString(), 2);
			// it is not possible to translate the following edis code in this way
			if( "1.1.0.1.2.255".equals(obis.toString())) {
				return new RegisterValue(obis, readTime());
			}

			if( "1.1.0.0.0.255".equals(obis.toString())) {
				return new RegisterValue(obis, getMeterSerial());
			}
			if( "1.1.0.2.0.255".equals(obis.toString())) {
				return new RegisterValue(obis, getFirmwareVersion());
			}

			if( "1.1.0.0.1.255".equals(obis.toString())) {
				return new RegisterValue(obis, readSpecialRegister((String)this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
			}
			if( "1.1.0.0.2.255".equals(obis.toString())) {
				return new RegisterValue(obis, readSpecialRegister((String)this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
			}
			if( "1.1.0.0.3.255".equals(obis.toString())) {
				return new RegisterValue(obis, readSpecialRegister((String)this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
			}
			if( "1.1.0.0.4.255".equals(obis.toString())) {
				return new RegisterValue(obis, readSpecialRegister((String)this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
			}
			if( "1.1.0.0.5.255".equals(obis.toString())) {
				return new RegisterValue(obis, readSpecialRegister((String)this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
			}
			if( "1.1.0.0.6.255".equals(obis.toString())) {
				return new RegisterValue(obis, readSpecialRegister((String)this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
			}
			if( "1.1.0.0.7.255".equals(obis.toString())) {
				return new RegisterValue(obis, readSpecialRegister((String)this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
			}
			if( "1.1.0.0.8.255".equals(obis.toString())) {
				return new RegisterValue(obis, readSpecialRegister((String)this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
			}
			if( "1.1.0.0.9.255".equals(obis.toString())) {
				return new RegisterValue(obis, readSpecialRegister((String)this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
			}
			if( "1.1.0.0.10.255".equals(obis.toString())) {
				return new RegisterValue(obis, readSpecialRegister((String)this.a1440ObisCodeMapper.getObisMap().get(obis.toString())));
			}

			if( obis.getF() != 255 ) {
				int f = getBillingCount() - Math.abs(obis.getF());
				fs = "*" + ProtocolUtils.buildStringDecimal(f, 2);
			}
			String edis = obis.getC() + "." + obis.getD() + "." + obis.getE() + fs;
			try {
				data = read(edis);
			} catch (IOException e1) {
				if (DEBUG >= 3) {
					e1.printStackTrace();
				}
				throw e1;
			}

			// try to read the time stamp, and us it as the register toTime.
			try {
				String billingPoint = "";
				if ("1.1.0.1.0.255".equalsIgnoreCase(obis.toString())) {
					billingPoint = "*" + ProtocolUtils.buildStringDecimal(getBillingCount(), 2);
				} else {
					billingPoint = fs;
				}
				VDEWTimeStamp vts = new VDEWTimeStamp(getTimeZone());
				timeStampData = read("0.1.2" + billingPoint);
				toTimeString = dp.parseBetweenBrackets(timeStampData);
				vts.parse(toTimeString);
				toTime = vts.getCalendar().getTime();
			} catch (Exception e) {}


			// read and parse the value an the unit ()if exists) of the register
			String temp = dp.parseBetweenBrackets(data, 0, 0);
			Unit readUnit = null;
			if (temp.indexOf('*') != -1) {
				readUnit = Unit.get(temp.substring(temp.indexOf('*') + 1));
				temp = temp.substring(0, temp.indexOf('*'));
				sendDebug("ReadUnit: " + readUnit, 3);
			}

			BigDecimal bd = new BigDecimal(temp);

			// Read the eventTime (timestamp after the register data)
			try {
				String dString = dp.parseBetweenBrackets(data, 0, 1);
				if( "0000000000".equals(dString) ) {
					throw new NoSuchRegisterException();
				}
				VDEWTimeStamp vts = new VDEWTimeStamp(getTimeZone());
				vts.parse(dString);
				eventTime = vts.getCalendar().getTime();
			} catch (DataParseException e) {
				if (DEBUG >= 3) {
					e.printStackTrace();
				}
			} catch (NoSuchRegisterException e) {
				if (DEBUG >= 3) {
					e.printStackTrace();
				}
				return new RegisterValue(obis, null, null, null);
			}

			Quantity q = null;
			if (obis.getUnitElectricity(this.scaler).isUndefined()) {
				q = new Quantity(bd, obis.getUnitElectricity(0));
			} else {
				if (readUnit != null) {
					if (!readUnit.equals(obis.getUnitElectricity(this.scaler))) {
						String message = "Unit or scaler from obiscode is different from register Unit in meter!!! ";
						message += " (Unit from meter: " + readUnit;
						message += " -  Unit from obiscode: " + obis.getUnitElectricity(this.scaler) + ")\n";

						sendDebug(message);
						getLogger().info(message);
						if (this.failOnUnitMismatch == 1) {
							throw new InvalidPropertyException(message);
						}
					}
				}
				q = new Quantity(bd, obis.getUnitElectricity(this.scaler));
			}

			return new RegisterValue(obis, q, eventTime, toTime);

		} catch (NoSuchRegisterException e) {
			String m = "ObisCode " + obis.toString() + " is not supported!";
			if (DEBUG >= 3) {
				e.printStackTrace();
			}
			throw new NoSuchRegisterException(m);
		} catch (InvalidPropertyException e) {
			String m = "getMeterReading() error, " + e.getMessage();
			if (DEBUG >= 3) {
				e.printStackTrace();
			}
			throw new InvalidPropertyException(m);
		} catch (FlagIEC1107ConnectionException e) {
			String m = "getMeterReading() error, " + e.getMessage();
			if (DEBUG >= 3) {
				e.printStackTrace();
			}
			throw new IOException(m);
		} catch (IOException e) {
			String m = "getMeterReading() error, " + e.getMessage();
			if (DEBUG >= 3) {
				e.printStackTrace();
			}
			throw new IOException(m);
		} catch (NumberFormatException e) {
			String m = "ObisCode " + obis.toString() + " is not supported!";
			if (DEBUG >= 3) {
				e.printStackTrace();
			}
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
			this.flagIEC1107Connection.sendRawCommandFrame(FlagIEC1107Connection.READ5, byteArrayOutputStream
					.toByteArray());
			data = this.flagIEC1107Connection.receiveRawData();
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
		String reginfo = (String) this.a1440ObisCodeMapper.getObisMap().get(obisCode.toString());
		if (reginfo == null) {
			reginfo = obisCode.getDescription();
		}
		return new RegisterInfo("" + reginfo);
	}



	private void getRegistersInfo() throws IOException {
		StringBuffer rslt = new StringBuffer();

		Iterator i = this.a1440ObisCodeMapper.getObisMap().keySet().iterator();
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

		getLogger().info(rslt.toString());
	}

	private void getMeterInfo() throws IOException {
		String returnString = "";
		if (this.iSecurityLevel < 1) {
			returnString = "Set the SecurityLevel > 0 to show more information about the meter.\n";
		} else {
			returnString += " Meter ID1: " + readSpecialRegister(A1440ObisCodeMapper.ID1) + "\n";
			returnString += " Meter ID2: " + readSpecialRegister(A1440ObisCodeMapper.ID2) + "\n";
			returnString += " Meter ID3: " + readSpecialRegister(A1440ObisCodeMapper.ID3) + "\n";
			returnString += " Meter ID4: " + readSpecialRegister(A1440ObisCodeMapper.ID4) + "\n";
			returnString += " Meter ID5: " + readSpecialRegister(A1440ObisCodeMapper.ID5) + "\n";
			returnString += " Meter ID6: " + readSpecialRegister(A1440ObisCodeMapper.ID6) + "\n";

			returnString += " Meter IEC1107 ID:" + readSpecialRegister(A1440ObisCodeMapper.IEC1107_ID) + "\n";
			returnString += " Meter IECII07 address (optical):    " + readSpecialRegister(A1440ObisCodeMapper.IEC1107_ADDRESS_OP) + "\n";
			returnString += " Meter IECII07 address (electrical): " + readSpecialRegister(A1440ObisCodeMapper.IEC1107_ADDRESS_EL) + "\n";

		}
		getLogger().info(returnString);
	}

	// ********************************************************************************************************
	// implementation of the HHUEnabler interface
	public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
		enableHHUSignOn(commChannel, isDataReadout());
	}

	public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
		HHUSignOn hhuSignOn = (HHUSignOn) new IEC1107HHUConnection(commChannel, this.iIEC1107TimeoutProperty,
				this.iProtocolRetriesProperty, 300, this.iEchoCancelling);
		hhuSignOn.setMode(HHUSignOn.MODE_PROGRAMMING);
		hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_NORMAL);
		hhuSignOn.enableDataReadout(datareadout);
		getFlagIEC1107Connection().setHHUSignOn(hhuSignOn);
	}

	public byte[] getHHUDataReadout() {
		return getFlagIEC1107Connection().getHhuSignOn().getDataReadout();
	}

	public A1440Registry getA1440Registry() {
		return this.a1440Registry;
	}

	public A1440Profile getA1440Profile() {
		return this.a1440Profile;
	}

	int getBillingCount() throws IOException{
		if( this.billingCount == null ){

			if (isDataReadout()) {
				sendDebug("Requesting getBillingCount() dataReadOut: " + getDataReadout().length, 2);
				DataDumpParser ddp = new DataDumpParser(getDataReadout());
				this.billingCount = new int [] {ddp.getBillingCounter()};
			} else {

				String data;
				try {
					data = new String( read("0.1.0") );
				} catch (NoSuchRegisterException e) {
					if (!isDataReadout()) {
						throw e;
					}
					data = "()";
				}

				int start = data.indexOf('(') + 1;
				int stop = data.indexOf(')');
				String v = data.substring( start, stop );

				try {
					this.billingCount = new int [] { Integer.parseInt(v) };
				} catch (NumberFormatException e) {
					this.billingCount = new int [] {0};
					sendDebug("Unable to read billingCounter. Defaulting to 0!");
				}
			}

		}
		return this.billingCount[0];
	}

	private String getMeterSerial() throws IOException {
		if (this.meterSerial == null) {
			this.meterSerial = (String)getA1440Registry().getRegister(this.a1440Registry.SERIAL);
		}
		return this.meterSerial;
	}

	protected void validateSerialNumber() throws IOException {
		if ((this.serialNumber == null) || ("".compareTo(this.serialNumber)==0)) {
			return;
		}
		if (this.serialNumber.compareTo(getMeterSerial()) == 0) {
			return;
		}
		throw new IOException("SerialNumber mismatch! meter sn="+getMeterSerial()+", configured sn="+this.serialNumber);
	}

	/**
	 * Implementation of methods in MessageProtocol
	 */

	public void applyMessages(List messageEntries) throws IOException {
		this.a1440Messages.applyMessages(messageEntries);
	}

	public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
		return this.a1440Messages.queryMessage(messageEntry);
	}

	public List getMessageCategories() {
		return this.a1440Messages.getMessageCategories();
	}

	public String writeMessage(Message msg) {
		return this.a1440Messages.writeMessage(msg);
	}

	public String writeTag(MessageTag tag) {
		return this.a1440Messages.writeTag(tag);
	}

	public String writeValue(MessageValue value) {
		return this.a1440Messages.writeValue(value);
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
		if (registerName.equals(A1440ObisCodeMapper.ID1)) {
			return new String(ProtocolUtils.convert2ascii(((String)getA1440Registry().getRegister(A1440Registry.ID1)).getBytes()));
		}
		if (registerName.equals(A1440ObisCodeMapper.ID2)) {
			return new String(ProtocolUtils.convert2ascii(((String)getA1440Registry().getRegister(A1440Registry.ID2)).getBytes()));
		}
		if (registerName.equals(A1440ObisCodeMapper.ID3)) {
			return new String(ProtocolUtils.convert2ascii(((String)getA1440Registry().getRegister(A1440Registry.ID3)).getBytes()));
		}
		if (registerName.equals(A1440ObisCodeMapper.ID4)) {
			return new String(ProtocolUtils.convert2ascii(((String)getA1440Registry().getRegister(A1440Registry.ID4)).getBytes()));
		}
		if (registerName.equals(A1440ObisCodeMapper.ID5)) {
			return new String(ProtocolUtils.convert2ascii(((String)getA1440Registry().getRegister(A1440Registry.ID5)).getBytes()));
		}
		if (registerName.equals(A1440ObisCodeMapper.ID6)) {
			return new String(ProtocolUtils.convert2ascii(((String)getA1440Registry().getRegister(A1440Registry.ID6)).getBytes()));
		}

		if (registerName.equals(A1440ObisCodeMapper.IEC1107_ID)) {
			return new String(ProtocolUtils.convert2ascii(((String)getA1440Registry().getRegister(A1440Registry.IEC1107_ID)).getBytes()));
		}
		if (registerName.equals(A1440ObisCodeMapper.IEC1107_ADDRESS_OP)) {
			return new String(ProtocolUtils.convert2ascii(((String)getA1440Registry().getRegister(A1440Registry.IEC1107_ADDRESS_OP)).getBytes()));
		}
		if (registerName.equals(A1440ObisCodeMapper.IEC1107_ADDRESS_EL)) {
			return new String(ProtocolUtils.convert2ascii(((String)getA1440Registry().getRegister(A1440Registry.IEC1107_ADDRESS_EL)).getBytes()));
		}
		if (registerName.equals(A1440ObisCodeMapper.FIRMWAREID)) {
			return getFirmwareVersion();
		}

		if (registerName.equals(A1440ObisCodeMapper.FIRMWARE)) {
			String fw = "";
			String hw = "";
			String dev = "";
			String fwdev = "";

			if (this.iSecurityLevel < 1) {
				return "Unknown (SecurityLevel to low)";
			}
			fwdev = (String)getA1440Registry().getRegister(A1440Registry.FIRMWARE);
			hw = (String)getA1440Registry().getRegister(A1440Registry.HARDWARE);

			if ((fwdev != null) && (fwdev.length() >= 30)) {
				fw = fwdev.substring(0, 10);
				dev = fwdev.substring(10, 30);
				fw = new String(ProtocolUtils.convert2ascii(fw.getBytes())).trim();
				dev = new String(ProtocolUtils.convert2ascii(dev.getBytes())).trim();
			} else {
				fw = "Unknown";
				dev = "Unknown";
			}

			if (hw != null) {
				hw = new String(ProtocolUtils.convert2ascii(hw.getBytes())).trim();
			} else {
				hw = "Unknown";
			}

			return dev + " " + "v" + fw + " " + hw;
		}

		return "";
	}

	private void sendDebug(String string, int i) {
		if (DEBUG >= i) {
			sendDebug(string);
		}
	}

}
