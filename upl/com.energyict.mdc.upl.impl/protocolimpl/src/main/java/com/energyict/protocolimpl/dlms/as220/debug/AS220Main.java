package com.energyict.protocolimpl.dlms.as220.debug;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.SecurityContext;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.GenericRead;
import com.energyict.dlms.cosem.Register;
import com.energyict.genericprotocolimpl.common.LocalSecurityProvider;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocolimpl.base.DebuggingObserver;
import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimpl.dlms.as220.AS220Messaging;
import com.energyict.protocolimpl.utils.ProtocolTools;

public class AS220Main {

	private static final ObisCode	DEVICE_ID1_OBISCODE			= ObisCode.fromString("0.0.96.0.0.255");
	private static final ObisCode	DEVICE_ID2_OBISCODE			= ObisCode.fromString("0.0.96.1.0.255");
	private static final ObisCode	DEVICE_ID3_OBISCODE			= ObisCode.fromString("0.0.96.2.0.255");
	private static final ObisCode	DEVICE_ID4_OBISCODE			= ObisCode.fromString("0.0.96.3.0.255");
	private static final ObisCode	DEVICE_ID5_OBISCODE			= ObisCode.fromString("0.0.96.4.0.255");

	private static final String		DISCONNECT_EMETER	= "<" + AS220Messaging.DISCONNECT_EMETER + ">1</" + AS220Messaging.DISCONNECT_EMETER + ">";
	private static final String		CONNECT_EMETER		= "<" + AS220Messaging.CONNECT_EMETER + ">1</" + AS220Messaging.CONNECT_EMETER + ">";
	private static final String		ARM_EMETER			= "<" + AS220Messaging.ARM_EMETER + ">1</" + AS220Messaging.ARM_EMETER + ">";

	private static final String		DISCONNECT_GMETER	= "<" + AS220Messaging.DISCONNECT_GMETER + ">1</" + AS220Messaging.DISCONNECT_GMETER + ">";
	private static final String		CONNECT_GMETER		= "<" + AS220Messaging.CONNECT_GMETER + ">1</" + AS220Messaging.CONNECT_GMETER + ">";
	private static final String		ARM_GMETER			= "<" + AS220Messaging.ARM_GMETER + ">1</" + AS220Messaging.ARM_GMETER + ">";

	private static final String		OBSERVER_FILENAME	= "c:\\logging\\AS220Main\\communications.log";
	private static final Level		LOG_LEVEL			= Level.ALL;
	private static final TimeZone	DEFAULT_TIMEZONE	= TimeZone.getTimeZone("GMT+01");

	private static final String		COMPORT				= "COM5";
	private static final int		BAUDRATE			= 115200;
	private static final int		DATABITS			= SerialCommunicationChannel.DATABITS_8;
	private static final int		PARITY				= SerialCommunicationChannel.PARITY_NONE;
	private static final int		STOPBITS			= SerialCommunicationChannel.STOPBITS_1;

	private static final int		DELAY_BEFORE_DISCONNECT	= 100;

	private static AS220 as220 = null;
	private static Dialer dialer = null;
	private static Logger logger = null;

	public static AS220 getAs220() {
		if (as220 == null) {
			as220 = new AS220();
			log("Created new instance of " + as220.getClass().getCanonicalName() + " [" + as220.getProtocolVersion() + "]");
		}
		return as220;
	}

	public static Dialer getDialer() {
		if (dialer == null) {
			dialer = DialerFactory.getDirectDialer().newDialer();
			dialer.setStreamObservers(new DebuggingObserver(OBSERVER_FILENAME, false));
		}
		return dialer;
	}

	public static Logger getLogger() {
		 if (logger == null) {
			 logger = Logger.getLogger(AS220Main.class.getCanonicalName());
			 logger.setLevel(LOG_LEVEL);
		 }
		 return logger;
	}

	private static Properties getProperties() {
		Properties properties = new Properties();

		properties.setProperty("MaximumTimeDiff", "300");
		properties.setProperty("MinimumTimeDiff", "1");
		properties.setProperty("CorrectTime", "0");

		properties.setProperty("Retries", "5");
		properties.setProperty("Timeout", "20000");

		properties.setProperty("SecurityLevel", "1:" + SecurityContext.SECURITYPOLICY_BOTH);
		properties.setProperty("ProfileInterval", "900");
		properties.setProperty("Password", "00000000");
		properties.setProperty("SerialNumber", "35015023");

		properties.setProperty("AddressingMode", "-1");
		properties.setProperty("Connection", "3");
		properties.setProperty("ClientMacAddress", "2");
		properties.setProperty("ServerLowerMacAddress", "1");
		properties.setProperty("ServerUpperMacAddress", "1");

		properties.setProperty(LocalSecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY, "D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
		properties.setProperty(LocalSecurityProvider.DATATRANSPORTKEY, "000102030405060708090A0B0C0D0E0F");

		return properties;
	}

	public static void readProfile(boolean incluideEvents) throws IOException {
		Calendar from = Calendar.getInstance(DEFAULT_TIMEZONE);
		from.add(Calendar.YEAR, -5);
		log(getAs220().getProfileData(from.getTime(), incluideEvents));
	}

	public static void readRegisters() {
		UniversalObject[] universalObjects = getAs220().getMeterConfig().getInstantiatedObjectList();
		for (UniversalObject uo : universalObjects) {
			if (uo.getClassID() == Register.CLASSID) {
				try {
					System.out.println(getAs220().readRegister(uo.getObisCode()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void pulseContactor() throws IOException {
		getAs220().queryMessage(new MessageEntry(DISCONNECT_EMETER, "1"));
		getAs220().queryMessage(new MessageEntry(ARM_EMETER, "2"));
		getAs220().queryMessage(new MessageEntry(CONNECT_EMETER, "3"));
	}

	public static void pulseValve() throws IOException {
		getAs220().queryMessage(new MessageEntry(DISCONNECT_GMETER, "1"));
		getAs220().queryMessage(new MessageEntry(ARM_GMETER, "2"));
		getAs220().queryMessage(new MessageEntry(CONNECT_GMETER, "3"));
	}

	public static void readObiscodes() throws IOException {
		UniversalObject[] uo = getAs220().getMeterConfig().getInstantiatedObjectList();
		for (UniversalObject universalObject : uo) {
			log(universalObject.getObisCode() + " = " + DLMSClassId.findById(universalObject.getClassID()));
		}
	}

	public static void readDataObjects() {
		UniversalObject[] uo = getAs220().getMeterConfig().getInstantiatedObjectList();
		for (UniversalObject universalObject : uo) {
			if (universalObject.getClassID() == DLMSClassId.DATA.getClassId()) {
				try {
					logger.log(Level.INFO, universalObject.getObisCode() + " = " + getAs220().getCosemObjectFactory().getData(universalObject.getObisCode()).getDataContainer().doPrintDataContainer());
				} catch (IOException e) {}
			}
		}
	}

	public static void getAndSetTime() throws IOException {
		Date date = getAs220().getTime();
		log(date);
		getAs220().setTime();
		date = getAs220().getTime();
		log(date);
	}

	public static void main(String[] args) throws LinkException, IOException, InterruptedException {

		getDialer().init(COMPORT);
		getDialer().getSerialCommunicationChannel().setParams(BAUDRATE, DATABITS, PARITY, STOPBITS);
		getDialer().connect();

		try {
			getAs220().setProperties(getProperties());
			getAs220().init(getDialer().getInputStream(), getDialer().getOutputStream(), DEFAULT_TIMEZONE, getLogger());
			getAs220().connect();


			final ObisCode	plc_sfsk_setup	= ObisCode.fromString("0.0.26.0.0.255");
			System.out.println(getAs220().readRegister(plc_sfsk_setup));

			//readRegisters();

		} finally {
			ProtocolTools.delay(DELAY_BEFORE_DISCONNECT);
			log("Done. Closing connections. \n");
			getAs220().disconnect();
			getDialer().disConnect();
		}

	}

	private static void doTest() throws IOException {

		System.out.println(getAs220().getCosemObjectFactory().getSFSKPhyMacSetup(ObisCode.fromString("0.0.26.0.0.255")).toString());

		GenericRead gr = getAs220().getCosemObjectFactory().getGenericRead(ObisCode.fromString("0.0.26.0.0.255"), 0x18);
		System.out.println(gr);

//		UniversalObject[] uo = getAs220().getMeterConfig().getInstantiatedObjectList();
//		for (int i = 0; i < uo.length; i++) {
//			System.out.println(uo[i].toString() + " - " + uo[i].getBaseName());
//		}
//
//		gr = getAs220().getCosemObjectFactory().getGenericRead(ObisCode.fromString("1.0.0.2.0.255"), 8);
//		System.out.println(gr);

//		System.out.println(getAs220().readRegister(DEVICE_ID1_OBISCODE));
//		System.out.println(getAs220().readRegister(DEVICE_ID2_OBISCODE));
//		System.out.println(getAs220().readRegister(DEVICE_ID3_OBISCODE));
//		System.out.println(getAs220().readRegister(DEVICE_ID4_OBISCODE));
//		System.out.println(getAs220().readRegister(DEVICE_ID5_OBISCODE));

//		examineObisCode(ObisCode.fromString("1.0.0.2.0.255"));
//		examineObisCode(ObisCode.fromString("0.0.42.0.0.255"));
//		examineObisCode(ObisCode.fromString("1.0.96.63.11.255"));
//		examineObisCode(ObisCode.fromString("1.0.0.0.0.255"));
//		examineObisCode(ObisCode.fromString("0.0.23.1.0.255"));
//		examineObisCode(ObisCode.fromString("1.0.96.63.1.255"));
//		examineObisCode(ObisCode.fromString("0.0.96.63.10.255"));
//		examineObisCode(ObisCode.fromString("0.0.96.15.0.255"));
//		examineObisCode(ObisCode.fromString("0.0.96.15.1.255"));
//		examineObisCode(ObisCode.fromString("1.0.96.5.1.255"));
//		examineObisCode(ObisCode.fromString("0.0.96.50.0.255"));
//		examineObisCode(ObisCode.fromString("0.0.96.3.10.255"));

//		System.out.println(getAs220().readRegister(As220ObisCodeMapper.NR_CONFIGCHANGES_OBISCODE));
//		System.out.println(getAs220().readRegister(As220ObisCodeMapper.ERROR_REGISTER_OBISCODE));
//		System.out.println(getAs220().readRegister(As220ObisCodeMapper.ALARM_REGISTER_OBISCODE));
//		System.out.println(getAs220().readRegister(As220ObisCodeMapper.FILTER_REGISTER_OBISCODE));
//		System.out.println(getAs220().readRegister(As220ObisCodeMapper.LOGICAL_DEVICENAME_OBISCODE));

	}

	private static void examineObisCode(ObisCode obisCode) {
		System.out.println();
		System.out.println(obisCode + " = " + obisCode.getDescription());
		for (int i = 0; i < 0x70; i += 8) {
			try {
				GenericRead gr = getAs220().getCosemObjectFactory().getGenericRead(obisCode, i);
				AbstractDataType dataType = AXDRDecoder.decode(gr.getResponseData());
				String value = ProtocolTools.getHexStringFromBytes(gr.getResponseData());
				System.out.println(i + " = " + dataType.getClass().getSimpleName() + " " + value);
			} catch (IOException e) {

			}
		}
		System.out.println();
	}

	private static void log(Object message) {
		getLogger().log(Level.INFO, message == null ? "null" : message.toString());
	}

}
