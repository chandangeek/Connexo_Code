/*
 * MK6.java
 *
 * Created on 17 maart 2006, 10:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6;

import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.edmi.mk6.command.CommandFactory;
import com.energyict.protocolimpl.edmi.mk6.command.TimeInfo;
import com.energyict.protocolimpl.edmi.mk6.registermapping.ObisCodeFactory;
import com.energyict.protocolimpl.edmi.mk6.registermapping.ObisCodeMapper;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.energyict.mdc.upl.MeterProtocol.Property.NODEID;

/**
 *
 * @author  Koen
 * @beginchanges
 * KV|17052006|Check for duplicates
 * KV|14112007|Fix to use the correct first record timestamp
 * GN|05082009|If the FailWhenChannelConfigMisMatch flag is check it always failed when you didn't set the StatusConfigFlag property
 * JM|22092009|Added custom property to disable log-off after communication to prevent modem disconnect.
 * @endchanges
 */
public class MK6 extends AbstractProtocol implements Serializable {

	/** Generated SerialVersionUID */
	private static final long serialVersionUID = 4668911907276635756L;
	private transient MK6Connection mk6Connection=null;
	private CommandFactory commandFactory=null;
	private ObisCodeFactory obisCodeFactory=null;
	private MK6Profile mk6Profile=null;
	private String eventLogName;
	private String loadSurveyName;
	private int statusFlagChannel;
	private boolean logOffDisabled = true;
	private TimeZone timeZone;
	private boolean useOldProfileFromDate;

	public MK6(PropertySpecService propertySpecService) {
		super(propertySpecService);
	}

	@Override
	protected void doConnect() throws IOException {
		getCommandFactory().enterCommandLineMode();
		getCommandFactory().logon(getInfoTypeDeviceID(),getInfoTypePassword());
	}

    @Override
	protected void doDisconnect() throws IOException {
		if (!isLogOffDisabled()) {
			getCommandFactory().exitCommandLineMode();
		} else {
			getLogger().finer("logOffDisabled = " + isLogOffDisabled() + " Ignoring disconnect call.");
		}
	}

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = super.getUPLPropertySpecs();
        propertySpecs.add(this.stringSpec("EventLogName", false));
        propertySpecs.add(this.stringSpec("LoadSurveyName", false));
        propertySpecs.add(this.integerSpec("StatusFlagChannel", false));
        propertySpecs.add(this.integerSpec("DisableLogOff", false));
        propertySpecs.add(this.stringSpec("UseOldProfileFromDate", false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
		setInfoTypeNodeAddress(properties.getTypedProperty(NODEID.getName(), "1"));
		setEventLogName(properties.getTypedProperty("EventLogName", "Event Log"));
		setLoadSurveyName(properties.getTypedProperty("LoadSurveyName", "Load_Survey"));
		setForcedDelay(Integer.parseInt(properties.getTypedProperty("ForcedDelay","0").trim()));
		setStatusFlagChannel(Integer.parseInt(properties.getTypedProperty("StatusFlagChannel", "0").trim()));
		setLogOffDisabled(Integer.parseInt(properties.getTypedProperty("DisableLogOff", "0").trim()));
		setUseOldProfileFromDate("1".equalsIgnoreCase(properties.getTypedProperty("UseOldProfileFromDate", "0")));
	}

    @Override
	public int getProfileInterval() throws IOException {
		return this.mk6Profile.getProfileInterval();
	}

    @Override
	public int getNumberOfChannels() throws IOException {
		return this.mk6Profile.getNumberOfChannels();
	}

    @Override
	protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
		this.mk6Connection = new MK6Connection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber());
		this.commandFactory = new CommandFactory(this);
		this.mk6Profile = new MK6Profile(this);
		return getMk6Connection();
	}

    @Override
	public Date getTime() throws IOException {
		TimeInfo ti = new TimeInfo(this);
		return ti.getTime();
	}

    @Override
	public void setTime() throws IOException {
		TimeInfo ti = new TimeInfo(this);
		ti.setTime();
	}

    @Override
    public String getProtocolVersion() {
		return "$Date: 2015-11-26 15:24:26 +0200 (Thu, 26 Nov 2015)$";
	}

    @Override
	public String getFirmwareVersion() throws IOException {
		return "Equipment model id:"+getCommandFactory().getReadCommand(0xF000).getRegister().getString()+"\n"+ // Equipment model id
		"Software revision:"+getCommandFactory().getReadCommand(0xF003).getRegister().getString()+"\n"+ // software version
		"Last version nr:"+getCommandFactory().getReadCommand(0xFC18).getRegister().getString()+"\n"+ // last version number
		"Last revision nr:"+getCommandFactory().getReadCommand(0xFC19).getRegister().getString()+"\n"+ // last revision number
		"Software revision nr:"+getCommandFactory().getReadCommand(0xF090).getRegister().getString()+"\n"+ // software revision number
		"Serial number:"+getSerialNumber(); // serial number
	}

	public String getSerialNumber() {
        try {
            return getCommandFactory().getReadCommand(0xF002).getRegister().getString(); // Serial number
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

    @Override
	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
		return this.mk6Profile.getProfileData(from, to, includeEvents);
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
		return getObicCodeFactory().getRegisterInfoDescription();
	}

    @Override
	public TimeZone getTimeZone() {
		if(this.timeZone == null){
			this.timeZone = ProtocolUtils.getWinterTimeZone(super.getTimeZone());
		}
		return this.timeZone;
	}

	public MK6Connection getMk6Connection() {
		return this.mk6Connection;
	}

	public CommandFactory getCommandFactory() {
		return this.commandFactory;
	}

	public ObisCodeFactory getObicCodeFactory() throws IOException {
		if (this.obisCodeFactory==null) {
			this.obisCodeFactory = new ObisCodeFactory(this);
		}
		return this.obisCodeFactory;
	}

	public String getEventLogName() {
		return this.eventLogName;
	}

	private void setEventLogName(String eventLogName) {
		this.eventLogName = eventLogName;
	}

	public String getLoadSurveyName() {
		return this.loadSurveyName;
	}

	private void setLoadSurveyName(String loadSurveyName) {
		this.loadSurveyName = loadSurveyName;
	}

	public boolean isStatusFlagChannel() {
		return this.statusFlagChannel==1;
	}

	public void setStatusFlagChannel(int statusFlagChannel) {
		this.statusFlagChannel = statusFlagChannel;
	}

	public boolean isLogOffDisabled() {
		return this.logOffDisabled;
	}

	public void setLogOffDisabled(int logOffDisabled) {
		this.logOffDisabled = (logOffDisabled == 1);
	}

	/**
	 * Protected setter for the MK6Connection
	 *
	 * @param connection - MK6Connection
	 */
	protected void setMK6Connection(MK6Connection connection){
		this.mk6Connection = connection;
	}

	/**
	 * @return the useOldProfileFromDate
	 */
	public boolean useOldProfileFromDate() {
		return useOldProfileFromDate;
	}

	/**
	 * @param useOldProfileFromDate the useOldProfileFromDate to set
	 */
	public void setUseOldProfileFromDate(boolean useOldProfileFromDate) {
		this.useOldProfileFromDate = useOldProfileFromDate;
	}

}