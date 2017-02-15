package com.energyict.protocolimpl.edmi.mk10;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
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
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;

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
public class MK10 extends AbstractProtocol implements SerialNumberSupport {

	private static final int DEBUG = 0;
	private static final boolean USE_HARD_INFO = true;

	private MK10Connection mk10Connection = null;
	private CommandFactory commandFactory = null;
	private ObisCodeFactory obisCodeFactory = null;
	MK10Profile mk10Profile = null;
	private int loadSurveyNumber = 0;
	private boolean pushProtocol = false;
	private boolean logOffDisabled = true;
	private boolean fullDebugLogging = false;

	public MK10(PropertySpecService propertySpecService, NlsService nlsService) {
		super(propertySpecService, nlsService);
	}

	@Override
	protected void doConnect() throws IOException {
		sendDebug("doConnect()");
		if (!isPushProtocol()) {
			getCommandFactory().enterCommandLineMode();
		}
		getCommandFactory().logon(getInfoTypeDeviceID(),getInfoTypePassword());
	}

	@Override
	protected void doDisconnect() throws IOException {
		sendDebug("doDisConnect()");
		if (!isLogOffDisabled()) {
			getCommandFactory().exitCommandLineMode();
		} else {
			sendDebug("logOffDisabled = " + isLogOffDisabled() + " Ignoring disconnect call.");
		}
	}

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(
		        UPLPropertySpecFactory
                        .specBuilder("LoadSurveyNumber", true, PropertyTranslationKeys.EDMI_LOAD_SURVEY_NUMBER, this.getPropertySpecService()::integerSpec)
                        .addValues( 1, 2)
                        .markExhaustive()
                        .finish());
        propertySpecs.add(this.integerSpec("DisableLogOff", PropertyTranslationKeys.EDMI_DISABLE_LOG_OFF, false));
        propertySpecs.add(this.stringSpec("PushProtocol", PropertyTranslationKeys.EDMI_PUSH_PROTOCOL, false));
        propertySpecs.add(this.stringSpec("FullDebug", PropertyTranslationKeys.EDMI_FULL_DEBUG, false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        sendDebug("setProperties()");
        super.setUPLProperties(properties);
		setInfoTypeNodeAddress(properties.getTypedProperty(NODEID.getName(), "1"));
		setLoadSurveyNumber(Integer.parseInt(((String) properties.getTypedProperty("LoadSurveyNumber")).trim())-1);
		setLogOffDisabled(Integer.parseInt(properties.getTypedProperty("DisableLogOff", "0").trim()));
        setPushProtocol("1".equalsIgnoreCase(properties.getTypedProperty("PushProtocol", "0").trim()));
        setFullDebugLogging("1".equalsIgnoreCase(properties.getTypedProperty("FullDebug", "0")));
	}

    @Override
    protected String defaultForcedDelayPropertyValue() {
        return "0";
    }

    @Override
	public int getProfileInterval() throws IOException {
		sendDebug("getProfileInterval()");
		return mk10Profile.getProfileInterval();
	}

    @Override
	public int getNumberOfChannels() throws IOException {
		sendDebug("getNumberOfChannels()");
		return mk10Profile.getNumberOfChannels();
	}

    @Override
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

    @Override
	public Date getTime() throws IOException {
		sendDebug("getTime()");
		TimeInfo ti = new TimeInfo(this);
		return ti.getTime();
	}

    @Override
	public void setTime() throws IOException {
		sendDebug("setTime()");
		TimeInfo ti = new TimeInfo(this);
		ti.setTime();
	}

    @Override
	public String getProtocolVersion() {
		sendDebug("getProtocolVersion()");
		return "$Date: Wed Dec 28 16:35:58 2016 +0100 $";
	}

    @Override
	public String getFirmwareVersion() throws IOException {
		sendDebug("getFirmwareVersion()");
		return "Equipment model id:"+getCommandFactory().getReadCommand(MK10Register.SYSTEM_MODEL_ID).getRegister().getString()+"\n"+ // Equipment model id
		"Software version:"+getCommandFactory().getReadCommand(MK10Register.SYSTEM_SOFTWARE_VERSION).getRegister().getString()+"\n"+ // Software version
		"Software revision:"+getCommandFactory().getReadCommand(MK10Register.SYSTEM_SOFTWARE_REVISION).getRegister().getString()+"\n"+ // Software revision
		"Bootloader revision:"+getCommandFactory().getReadCommand(MK10Register.SYSTEM_BOOTLOADER_REVISION).getRegister().getString()+"\n"+ // Software revision
		"Serial number:"+getSerialNumber(); // serial number
	}

    @Override
	public String getSerialNumber()  {
        try {
            return getCommandFactory().getReadCommand(MK10Register.SYSTEM_SERIALNUMBER).getRegister().getString(); // Serial number
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries()+1);
        }
    }

    @Override
	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
		sendDebug("getProfileData()");
		return mk10Profile.getProfileData(from, to, includeEvents);
	}

    @Override
	public RegisterValue readRegister(ObisCode obisCode) throws IOException {
		ObisCodeMapper ocm = new ObisCodeMapper(this);
		return ocm.getRegisterValue(obisCode);
	}

    @Override
	public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
		return ObisCodeMapper.getRegisterInfo(obisCode);
	}

    @Override
	protected String getRegistersInfo(int extendedLogging) throws IOException {
		sendDebug("getRegistersInfo()");
		return getObicCodeFactory().getRegisterInfoDescription();
	}

    @Override
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