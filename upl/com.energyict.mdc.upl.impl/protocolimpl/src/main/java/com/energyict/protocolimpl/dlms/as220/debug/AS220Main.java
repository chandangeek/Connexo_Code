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
import com.energyict.protocolimpl.dlms.as220.emeter.AS220Messaging;
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

	private static final String		RESCAN_PLCBUS		= "<" + AS220Messaging.RESCAN_PLCBUS + ">1</" + AS220Messaging.RESCAN_PLCBUS + ">";


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
			//dialer = DialerFactory.get("IPDIALER").newDialer();
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
		//properties.setProperty("ForcedDelay", "500");

		properties.setProperty("SecurityLevel", "1:" + SecurityContext.SECURITYPOLICY_NONE);
		properties.setProperty("ProfileInterval", "900");
		properties.setProperty("Password", "00000000");
		properties.setProperty("SerialNumber", "35021370");

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
		from.add(Calendar.DAY_OF_YEAR, -10);
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

	public static void rescanPLCBus() throws IOException {
		getAs220().queryMessage(new MessageEntry(RESCAN_PLCBUS, ""));
	}

	public static void readObiscodes() throws IOException {
		UniversalObject[] uo = getAs220().getMeterConfig().getInstantiatedObjectList();
		for (UniversalObject universalObject : uo) {
			System.out.println(universalObject.getObisCode() + " = " + DLMSClassId.findById(universalObject.getClassID()) + " ["+universalObject.getBaseName()+"] " + universalObject.getObisCode().getDescription());
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

//		getDialer().init("10.0.2.127:10010");
//		getDialer().connect("10.0.2.127:10010", 10010);

//		getDialer().init("linux2:10010");
//		getDialer().connect("linux2:10010", 10010);

		try {
			getAs220().setProperties(getProperties());
			getAs220().init(getDialer().getInputStream(), getDialer().getOutputStream(), DEFAULT_TIMEZONE, getLogger());
			getAs220().connect();

			getAs220().geteMeter().getContactorController().doConnect();
			getAs220().geteMeter().getContactorController().doDisconnect();
			getAs220().geteMeter().getContactorController().doConnect();

			getAs220().getTime();
			getAs220().setTime();

			readProfile(true);

			rescanPLCBus();

			readSFSKObjects();

			readRegisters();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			ProtocolTools.delay(DELAY_BEFORE_DISCONNECT);
			log("Done. Closing connections. \n");
			getAs220().disconnect();
			getDialer().disConnect();
		}

	}

	/**
	 * @throws IOException
	 */
	private static void readSFSKObjects() throws IOException {
		System.out.println(getAs220().readRegister(ObisCode.fromString("0.0.26.0.0.255")) + "\r\n");
		System.out.println(getAs220().readRegister(ObisCode.fromString("0.0.26.1.0.255")) + "\r\n");
		System.out.println(getAs220().readRegister(ObisCode.fromString("0.0.26.2.0.255")) + "\r\n");
		System.out.println(getAs220().readRegister(ObisCode.fromString("0.0.26.3.0.255")) + "\r\n");
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
