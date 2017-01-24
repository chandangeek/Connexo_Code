
package com.energyict.protocolimpl.edmi.mk10;

import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.edmi.mk10.command.CommandFactory;
import com.energyict.protocolimpl.edmi.mk10.command.TimeInfo;
import com.energyict.protocolimpl.edmi.mk10.registermapping.MK10Register;
import com.energyict.protocolimpl.edmi.mk10.registermapping.ObisCodeFactory;
import com.energyict.protocolimpl.edmi.mk10.registermapping.ObisCodeMapper;
import com.energyict.protocolimpl.edmi.mk10.streamfilters.MK10PushInputStream;
import com.energyict.protocolimpl.edmi.mk10.streamfilters.MK10PushOutputStream;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * @author  jme
 *
 * Changes:
 *
 * jme: 17/11/2008 -> Fixed meter event bugs (wrong mappings from event number to description string)
 * jme: 17/11/2008 -> Fixed bug in load profile raw data conversion (negative units & overflow)
 * jme: 17/11/2008 -> Fixed bug in load profile (start date <-> first entry) first entry can be different from 0 !!!
 * jme: 16/12/2008 -> Switched export/import for registers and load surveys according IEC and not ANSI
 * jme: 18/12/2008 -> Removed MISSING flag when only incomplete interval and no missing data
 * jme: 07/01/2009 -> Added register readings for Abs values
 * jme: 13/01/2009 -> Minor changes to support push protocol
 * jme: 19/01/2009 -> Fixed issue with events. Do not read events when firstentry > lastentry
 * jme: 19/01/2009 -> Fixed issue with registers (rates). Rate can be 0 for unified rate and 1 to 8 (and not 1 to 7 !!!).
 * jme: 19/01/2009 -> Hard coded some information for the most used registers.
 * jme: 20/01/2009 -> Fixed register scaling and decimal point.
 * jme: 21/01/2009 -> Added custom property to disable log-off after communication to prevent modem disconnect.
 * gna: 24/02/2009 -> Added the units and extra scaler for instantaneous values.
 * jme: 14/01/2010 -> Fixed bug in load profile. Protocol was unable to read all the data from the meter (CRM ticket 12855)
 * gna: 28/01/2010 -> Changed loadProfileReadout (Mantis 6113)
 * jme: 31/05/2010 -> COMMUNICATION-14 - EDMI MK10 & MK7 Registers Query (CRM 13712): Fixed register addresses from billing registers
 * jme: 09/07/2010 -> COMMUNICATION-59 - Fixed timeouts when udp packets were > 1024 bytes.
 * sva: 29/10/2012 -> EISERVERSG-1200 - The Generic MK10Push inbound protocol is deprecated, it should be replaced by the MK10InboundDeviceProtocol (doing the inbound discovery), combined with the regular MK10 protocol.
 **/
public class MK10 extends AbstractProtocol {

	@Override
	public String getProtocolDescription() {
		return "EDMI MK10 [Pull] CommandLine";
	}

	private static final int DEBUG				= 0;
	private static final boolean USE_HARD_INFO 	= true;

	private MK10Connection mk10Connection		= null;
	private CommandFactory commandFactory		= null;
	private ObisCodeFactory obisCodeFactory		= null;
	MK10Profile mk10Profile						= null;
	private int loadSurveyNumber				= 0;
	private boolean pushProtocol				= false;
	private boolean logOffDisabled				= true;
    private boolean fullDebugLogging            = false;

    @Inject
	public MK10(PropertySpecService propertySpecService) {
	    super(propertySpecService);
	}

	protected void doConnect() throws IOException {
		sendDebug("doConnect()");
		if (!isPushProtocol()) {
			getCommandFactory().enterCommandLineMode();
		}
		getCommandFactory().logon(getInfoTypeDeviceID(),getInfoTypePassword());
	}

	protected void doDisConnect() throws IOException {
		sendDebug("doDisConnect()");
		if (!isLogOffDisabled()) {
			getCommandFactory().exitCommandLineMode();
		} else {
			sendDebug("logOffDisabled = " + isLogOffDisabled() + " Ignoring disconnect call.");
		}
	}

	// This method is never used.
	// The protocol can't verify the serial number because the correct serial number is needed to communicate with the device
	protected void validateSerialNumber() throws IOException {
		sendDebug("doValidateProperties()");
		if ((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber())==0)) {
			return;
		}
		String sn = getSerialNumber();
		if (sn.compareTo(getInfoTypeSerialNumber()) == 0) {
			return;
		}
		throw new IOException("SerialNumber mismatch! meter sn="+sn+", configured sn="+getInfoTypeSerialNumber());
	}

	protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
		sendDebug("doValidateProperties()");
		setInfoTypeNodeAddress(properties.getProperty(MeterProtocol.NODEID,"1"));
		validateLoadSurveyNumber(properties.getProperty("LoadSurveyNumber"));
		setLoadSurveyNumber(Integer.parseInt(properties.getProperty("LoadSurveyNumber").trim())-1);
		setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","0").trim()));
		setLogOffDisabled(Integer.parseInt(properties.getProperty("DisableLogOff","0").trim()));
        setPushProtocol(properties.getProperty("PushProtocol", "0").trim().equalsIgnoreCase("1"));
        setFullDebugLogging(properties.getProperty("FullDebug", "0").equalsIgnoreCase("1"));
	}

	public int getProfileInterval() throws IOException {
		sendDebug("getProfileInterval()");
		return mk10Profile.getProfileInterval();
	}

	public int getNumberOfChannels() throws IOException {
		sendDebug("getNumberOfChannels()");
		return mk10Profile.getNumberOfChannels();
	}

	protected List doGetOptionalKeys() {
		sendDebug("doGetOptionalKeys()");
		List result = new ArrayList();
		result.add("LoadSurveyNumber");
		result.add("DisableLogOff");
        result.add("PushProtocol");
        result.add("FullDebug");
		return result;
	}

	protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
		sendDebug("doInit()");
        InputStream mk10InputStream = isPushProtocol()
                ? new MK10PushInputStream(inputStream, isFullDebugLogging() ? getLogger() : null)
                : inputStream;

        OutputStream mk10OutputStream = isPushProtocol()
                ? new MK10PushOutputStream(outputStream, isFullDebugLogging() ? getLogger() : null)
                : outputStream;

        mk10Connection = new MK10Connection(mk10InputStream, mk10OutputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber());
		commandFactory = new CommandFactory(this);
		mk10Profile = new MK10Profile(this);

		return getMk10Connection();
	}
	public Date getTime() throws IOException {
		sendDebug("getTime()");
		TimeInfo ti = new TimeInfo(this);
		return ti.getTime();
	}

	public void setTime() throws IOException {
		sendDebug("setTime()");
		TimeInfo ti = new TimeInfo(this);
		ti.setTime();
	}

    /** Protocol version **/
    public String getProtocolVersion() {
		sendDebug("getProtocolVersion()");
		return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
	}

	public String getFirmwareVersion() throws IOException {
		sendDebug("getFirmwareVersion()");
		return "Equipment model id:"+getCommandFactory().getReadCommand(MK10Register.SYSTEM_MODEL_ID).getRegister().getString()+"\n"+ // Equipment model id
		"Software version:"+getCommandFactory().getReadCommand(MK10Register.SYSTEM_SOFTWARE_VERSION).getRegister().getString()+"\n"+ // Software version
		"Software revision:"+getCommandFactory().getReadCommand(MK10Register.SYSTEM_SOFTWARE_REVISION).getRegister().getString()+"\n"+ // Software revision
		"Bootloader revision:"+getCommandFactory().getReadCommand(MK10Register.SYSTEM_BOOTLOADER_REVISION).getRegister().getString()+"\n"+ // Software revision
		"Serial number:"+getSerialNumber(); // serial number
	}

	public String getSerialNumber() throws IOException {
		return getCommandFactory().getReadCommand(MK10Register.SYSTEM_SERIALNUMBER).getRegister().getString(); // Serial number
	}

	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
		sendDebug("getProfileData()");
		return mk10Profile.getProfileData(from, to, includeEvents);
	}

	public RegisterValue readRegister(ObisCode obisCode) throws IOException {
		ObisCodeMapper ocm = new ObisCodeMapper(this);
		return ocm.getRegisterValue(obisCode);
	}

	public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
		return ObisCodeMapper.getRegisterInfo(obisCode);
	}

	protected String getRegistersInfo(int extendedLogging) throws IOException {
		sendDebug("getRegistersInfo()");
		return getObicCodeFactory().getRegisterInfoDescription();
	}

	public TimeZone getTimeZone() {
		return ProtocolUtils.getWinterTimeZone(super.getTimeZone());
	}

	public MK10Connection getMk10Connection() {
		return mk10Connection;
	}

	public CommandFactory getCommandFactory() {
		return commandFactory;
	}

	public ObisCodeFactory getObicCodeFactory() throws IOException {
		if (obisCodeFactory==null) {
			obisCodeFactory = new ObisCodeFactory(this);
		}
		return obisCodeFactory;
	}

	public int getLoadSurveyNumber() {
		return loadSurveyNumber;
	}

	private void setLoadSurveyNumber(int loadSurveyNr) {
		this.loadSurveyNumber = loadSurveyNr;
		sendDebug("setLoadSurveyNumber(): " + String.valueOf(this.loadSurveyNumber));
	}

	private void validateLoadSurveyNumber(String value) throws MissingPropertyException, InvalidPropertyException {
		if (value == null) {
			throw new MissingPropertyException("No LoadSurveyNumber property found! Must be 1 or 2 for the EDMI MK10 meter.");
		}
		if (!value.trim().equalsIgnoreCase("1") && !value.trim().equalsIgnoreCase("2")) {
			throw new InvalidPropertyException("Wrong LoadSurveyNumber value: " + value + "! Must be 1 or 2 for the EDMI MK10 meter.");
		}
	}

	public void sendDebug(String str){
		if (DEBUG >= 1) {
			str = " [MK10] > " + str;
			Logger log = getLogger();
			if (log != null) {
				getLogger().info(str);
			}
			else {
				System.out.println(str);
			}
		}
	}

	public boolean useHardCodedInfo() {
		return USE_HARD_INFO;
	}

	public boolean isPushProtocol() {
		return pushProtocol;
	}

	/**
	 * Setter for the pushProtocol field. This field is set to true if the protocol is used with an inbound UDP connection
	 * @param pushProtocol
	 */
	public void setPushProtocol(boolean pushProtocol) {
		this.pushProtocol = pushProtocol;
	}

	public boolean isLogOffDisabled() {
		return logOffDisabled;
	}

	public void setLogOffDisabled(int logOffDisabled ) {
		this.logOffDisabled = (logOffDisabled == 1);
	}

    public boolean isFullDebugLogging() {
        return fullDebugLogging;
    }

    public void setFullDebugLogging(boolean fullDebugLogging) {
        this.fullDebugLogging = fullDebugLogging;
    }
}
