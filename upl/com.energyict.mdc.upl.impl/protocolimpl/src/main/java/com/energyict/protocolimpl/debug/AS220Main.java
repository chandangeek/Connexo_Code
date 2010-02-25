package com.energyict.protocolimpl.debug;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dialer.coreimpl.OpticalDialer;
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
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.base.DebuggingObserver;
import com.energyict.protocolimpl.dlms.as220.AS220;
import com.energyict.protocolimpl.dlms.as220.emeter.AS220Messaging;
import com.energyict.protocolimpl.utils.ProtocolTools;

public class AS220Main {

	private static final ObisCode	DEVICE_ID1_OBISCODE		= ObisCode.fromString("0.0.96.0.0.255");
	private static final ObisCode	DEVICE_ID2_OBISCODE		= ObisCode.fromString("0.0.96.1.0.255");
	private static final ObisCode	DEVICE_ID3_OBISCODE		= ObisCode.fromString("0.0.96.2.0.255");
	private static final ObisCode	DEVICE_ID4_OBISCODE		= ObisCode.fromString("0.0.96.3.0.255");
	private static final ObisCode	DEVICE_ID5_OBISCODE		= ObisCode.fromString("0.0.96.4.0.255");

	private static final String		DISCONNECT_EMETER		= "<" + AS220Messaging.DISCONNECT_EMETER + ">1</" + AS220Messaging.DISCONNECT_EMETER + ">";
	private static final String		CONNECT_EMETER			= "<" + AS220Messaging.CONNECT_EMETER + ">1</" + AS220Messaging.CONNECT_EMETER + ">";
	private static final String		ARM_EMETER				= "<" + AS220Messaging.ARM_EMETER + ">1</" + AS220Messaging.ARM_EMETER + ">";

	private static final String		RESCAN_PLCBUS			= "<" + AS220Messaging.RESCAN_PLCBUS + ">1</" + AS220Messaging.RESCAN_PLCBUS + ">";
	private static final String		FORCE_SET_CLOCK			= "<" + AS220Messaging.FORCE_SET_CLOCK + ">1</" + AS220Messaging.FORCE_SET_CLOCK + ">";

	private static final String		OBSERVER_FILENAME		= "c:\\logging\\AS220Main\\communications.log";
	private static final Level		LOG_LEVEL				= Level.ALL;
	protected static final TimeZone	DEFAULT_TIMEZONE		= TimeZone.getTimeZone("GMT+01");

	protected static final String	COMPORT					= "COM5";
	protected static final int		BAUDRATE				= 115200;
	protected static final int		DATABITS				= SerialCommunicationChannel.DATABITS_8;
	protected static final int		PARITY					= SerialCommunicationChannel.PARITY_NONE;
	protected static final int		STOPBITS				= SerialCommunicationChannel.STOPBITS_1;

	protected static final int		DELAY_BEFORE_DISCONNECT	= 100;

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

	/**
	 * @return an {@link OpticalDialer}
	 */
	public static Dialer getOpticalDialer(){
		if(dialer == null){
			dialer = DialerFactory.getOpticalDialer().newDialer();
			dialer.setStreamObservers(new DebuggingObserver(null, true));
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

	private static Properties getCommonProperties() {
		Properties properties = new Properties();

		properties.setProperty("MaximumTimeDiff", "300");
		properties.setProperty("MinimumTimeDiff", "1");
		properties.setProperty("CorrectTime", "0");

		properties.setProperty("Retries", "5");
		properties.setProperty("Timeout", "20000");
		properties.setProperty("ForcedDelay", "100");

		properties.setProperty("SecurityLevel", "1:" + SecurityContext.SECURITYPOLICY_BOTH);
		properties.setProperty("ProfileInterval", "900");
		properties.setProperty("Password", "00000000");
		properties.setProperty("SerialNumber", "35021373");

		properties.setProperty("AddressingMode", "-1");
		properties.setProperty("Connection", "3");
		properties.setProperty("ClientMacAddress", "2");
		properties.setProperty("ServerLowerMacAddress", "1");
		properties.setProperty("ServerUpperMacAddress", "1");

		properties.setProperty("ProfileType", "1");

		properties.setProperty(LocalSecurityProvider.DATATRANSPORT_AUTHENTICATIONKEY, "D0D1D2D3D4D5D6D7D8D9DADBDCDDDEDF");
		properties.setProperty(LocalSecurityProvider.DATATRANSPORTKEY, "000102030405060708090A0B0C0D0E0F");

		return properties;
	}

	private static Properties getOpticalProperties() {
		Properties properties = getCommonProperties();
		properties.setProperty("SecurityLevel", "1:" + SecurityContext.SECURITYPOLICY_NONE);
		properties.setProperty("AddressingMode", "2");
		properties.setProperty("Connection", "0");
		properties.setProperty("ClientMacAddress", "1");
		properties.setProperty("ServerLowerMacAddress", "17");
		properties.setProperty("ServerUpperMacAddress", "1");
		properties.setProperty("OpticalBaudrate", "5");
		return properties;
	}


	public static ProfileData readProfile(boolean incluideEvents) throws IOException {
		Calendar from = Calendar.getInstance(DEFAULT_TIMEZONE);
		from.add(Calendar.DAY_OF_YEAR, -10);
		ProfileData pd = getAs220().getProfileData(from.getTime(), incluideEvents);
		log(pd);
		return pd;
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

	public static void forceSetClock() throws IOException {
		getAs220().queryMessage(new MessageEntry(FORCE_SET_CLOCK, ""));
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
			getAs220().setProperties(getCommonProperties());
			getAs220().init(getDialer().getInputStream(), getDialer().getOutputStream(), DEFAULT_TIMEZONE, getLogger());
			getAs220().connect();


			List<String> codes = new ArrayList<String>();
			codes.add("0.0.26.0.0.255");
			codes.add("0.0.26.1.0.255");
			codes.add("0.0.26.2.0.255");
			codes.add("0.0.26.3.0.255");
			codes.add("0.0.26.5.0.255");

			for (Iterator iterator = codes.iterator(); iterator.hasNext();) {
				String code = (String) iterator.next();
				for (int i = 0; i <= 20; i++) {
					try {
						ObisCode obis = ProtocolTools.setObisCodeField(ObisCode.fromString(code), 5, (byte) i);
						System.out.println(getAs220().translateRegister(obis) + " = " + getAs220().readRegister(obis).getText());
					} catch (Exception e) {}
				}
				System.out.println();
			}

//			log("FirmwareVersion :" + getAs220().getFirmwareVersion());
//			((AS220Messaging)getAs220().getMessaging()).upgradeDevice(getFirmware18ByteArray());
//			((AS220Messaging)getAs220().getMessaging()).upgradeDevice(getFirmware19ByteArray());
//			((AS220Messaging)getAs220().getMessaging()).upgradeDevice(getFirmware19ByteArray());
//			log("FirmwareVersion :" + getAs220().getPassiveFirmwareVersion());
//			getAs220().getCosemObjectFactory().getImageTransferSN().imageActivation();
//			log(getAs220().getFirmwareVersion());
//			log("Passive : " + getAs220().getPassiveFirmwareVersion());
//			getAs220().getCosemObjectFactory().getImageTransferSN().verifyAndRetryImage();
//			getAs220().getCosemObjectFactory().getImageTransferSN().readImageTransferStatus();
//			log(getAs220().getFirmwareVersion());

//			log(getAs220().getgMeter().getProfileData(new Date(0), new Date(), false));

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
		System.out.println(getAs220().readRegister(ObisCode.fromString("0.0.26.5.0.255")) + "\r\n");
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

	protected static void log(Object message) {
		getLogger().log(Level.INFO, message == null ? "null" : message.toString());
	}

	private static byte[] getFirmware18ByteArray() throws IOException {
        	File file = new File(AS220Main.class.getClassLoader().getResource("com/energyict/protocolimpl/dlms/as220/debug/firmware18b64.bin").getFile());
        	FileInputStream fis = new FileInputStream(file);
        	byte[] content = new byte[(int) file.length()];
        	fis.read(content);
        	fis.close();
        	return content;
        }

	private static byte[] getFirmware19ByteArray() throws IOException {
        	File file = new File(AS220Main.class.getClassLoader().getResource("com/energyict/protocolimpl/dlms/as220/debug/firmware17022010B64.bin").getFile());
        	FileInputStream fis = new FileInputStream(file);
        	byte[] content = new byte[(int) file.length()];
        	fis.read(content);
        	fis.close();
        	return content;
        }


}
