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

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.edmi.mk6.command.CommandFactory;
import com.energyict.protocolimpl.edmi.mk6.command.TimeInfo;
import com.energyict.protocolimpl.edmi.mk6.registermapping.ObisCodeFactory;
import com.energyict.protocolimpl.edmi.mk6.registermapping.ObisCodeMapper;

import java.io.*;
import java.util.*;

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
public class MK6 extends AbstractProtocol implements Serializable{

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
	
	/** Creates a new instance of MK6 */
	public MK6() {
	}

	protected void doConnect() throws IOException {
		getCommandFactory().enterCommandLineMode();
		getCommandFactory().logon(getInfoTypeDeviceID(),getInfoTypePassword());
	}

	protected void doDisConnect() throws IOException {
		if (!isLogOffDisabled()) {
			getCommandFactory().exitCommandLineMode();
		} else {
			getLogger().finer("logOffDisabled = " + isLogOffDisabled() + " Ignoring disconnect call.");
		}
	}

	protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
		setInfoTypeNodeAddress(properties.getProperty(MeterProtocol.NODEID,"1"));
		setEventLogName(properties.getProperty("EventLogName","Event Log"));
		setLoadSurveyName(properties.getProperty("LoadSurveyName","Load_Survey"));
		setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay","0").trim()));
		setStatusFlagChannel(Integer.parseInt(properties.getProperty("StatusFlagChannel","0").trim()));
		setLogOffDisabled(Integer.parseInt(properties.getProperty("DisableLogOff","0").trim()));
		setUseOldProfileFromDate(properties.getProperty("UseOldProfileFromDate","0").equalsIgnoreCase("1"));
	}

	public int getProfileInterval() throws IOException {
		return this.mk6Profile.getProfileInterval();
	}

	public int getNumberOfChannels() throws IOException {
		return this.mk6Profile.getNumberOfChannels();
	}

	protected List doGetOptionalKeys() {
		List result = new ArrayList();
		result.add("EventLogName");
		result.add("LoadSurveyName");
		result.add("StatusFlagChannel");
		result.add("DisableLogOff");
		result.add("UseOldProfileFromDate");
		return result;
	}

	protected ProtocolConnection doInit(InputStream inputStream,OutputStream outputStream,int timeoutProperty,int protocolRetriesProperty,int forcedDelay,int echoCancelling,int protocolCompatible,Encryptor encryptor,HalfDuplexController halfDuplexController) throws IOException {
		this.mk6Connection = new MK6Connection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber());
		this.commandFactory = new CommandFactory(this);
		this.mk6Profile = new MK6Profile(this);
		return getMk6Connection();
	}
	public Date getTime() throws IOException {
		TimeInfo ti = new TimeInfo(this);
		return ti.getTime();
	}

	public void setTime() throws IOException {
		TimeInfo ti = new TimeInfo(this);
		ti.setTime();
	}

	public String getProtocolVersion() {
		return "$Date$";
	}

	public String getFirmwareVersion() throws IOException {
		return "Equipment model id:"+getCommandFactory().getReadCommand(0xF000).getRegister().getString()+"\n"+ // Equipment model id
		"Software revision:"+getCommandFactory().getReadCommand(0xF003).getRegister().getString()+"\n"+ // software version
		"Last version nr:"+getCommandFactory().getReadCommand(0xFC18).getRegister().getString()+"\n"+ // last version number
		"Last revision nr:"+getCommandFactory().getReadCommand(0xFC19).getRegister().getString()+"\n"+ // last revision number
		"Software revision nr:"+getCommandFactory().getReadCommand(0xF090).getRegister().getString()+"\n"+ // software revision number
		"Serial number:"+getSerialNumber(); // serial number
	}

	public String getSerialNumber() throws IOException {
		return getCommandFactory().getReadCommand(0xF002).getRegister().getString(); // Serial number
	}

	public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
		return this.mk6Profile.getProfileData(from, to, includeEvents);
	}


	/*******************************************************************************************
     R e g i s t e r P r o t o c o l  i n t e r f a c e
	 *******************************************************************************************/
	public RegisterValue readRegister(ObisCode obisCode) throws IOException {
		ObisCodeMapper ocm = new ObisCodeMapper(this);
		return ocm.getRegisterValue(obisCode);
	}

	public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
		return ObisCodeMapper.getRegisterInfo(obisCode);
	}

	protected String getRegistersInfo(int extendedLogging) throws IOException {
		return getObicCodeFactory().getRegisterInfoDescription();
	}

	/**
	 * Get the timeZone
	 * @return the TimeZone
	 */
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
