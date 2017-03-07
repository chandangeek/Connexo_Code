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
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.edmi.common.CommandLineProtocol;
import com.energyict.protocolimpl.edmi.common.command.CommandFactory;
import com.energyict.protocolimpl.edmi.common.command.TimeInfo;
import com.energyict.protocolimpl.edmi.common.connection.ExtendedCommandLineConnection;
import com.energyict.protocolimpl.edmi.common.connection.MiniECommandLineConnection;
import com.energyict.protocolimpl.edmi.mk6.registermapping.ObisCodeFactory;
import com.energyict.protocolimpl.edmi.mk6.registermapping.ObisCodeMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

/**
 * @author Koen
 * @beginchanges KV|17052006|Check for duplicates
 * KV|14112007|Fix to use the correct first record timestamp
 * GN|05082009|If the FailWhenChannelConfigMisMatch flag is check it always failed when you didn't set the StatusConfigFlag property
 * JM|22092009|Added custom property to disable log-off after communication to prevent modem disconnect.
 * @endchanges
 */
public class MK6 extends AbstractProtocol implements Serializable, CommandLineProtocol {

    private static final int DEBUG = 0;

    /**
     * Generated SerialVersionUID
     */
    private static final long serialVersionUID = 4668911907276635756L;
    private transient ExtendedCommandLineConnection commandLineConnection = null;
    private CommandFactory commandFactory = null;
    private ObisCodeFactory obisCodeFactory = null;
    private MK6Profile mk6Profile = null;
    private String eventLogName;
    private String loadSurveyName;
    private int statusFlagChannel;
    private boolean logOffDisabled = true;
    private TimeZone timeZone;
    private boolean useOldProfileFromDate;
    private int connectionType;
    private static final int MINI_E_CONNECTION_TYPE = 1;

    /**
     * Creates a new instance of MK6
     */
    public MK6() {
    }

    protected void doConnect() throws IOException {
        if (!isMiniEConnection()) {
            getCommandFactory().enterCommandLineMode();
        }
        getCommandFactory().logon(getInfoTypeDeviceID(), getInfoTypePassword());
    }

    protected void doDisConnect() throws IOException {
        if (!isLogOffDisabled()) {
            getCommandFactory().exitCommandLineMode();
        } else {
            getLogger().finer("logOffDisabled = " + isLogOffDisabled() + " Ignoring disconnect call.");
        }
    }

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeNodeAddress(properties.getProperty(MeterProtocol.NODEID, "1"));
        setEventLogName(properties.getProperty("EventLogName", "Event Log"));
        setLoadSurveyName(properties.getProperty("LoadSurveyName", "Load_Survey"));
        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay", "0").trim()));
        setStatusFlagChannel(Integer.parseInt(properties.getProperty("StatusFlagChannel", "0").trim()));
        setLogOffDisabled(Integer.parseInt(properties.getProperty("DisableLogOff", "0").trim()));
        setUseOldProfileFromDate(properties.getProperty("UseOldProfileFromDate", "0").equalsIgnoreCase("1"));
        setConnectionType(Integer.parseInt(properties.getProperty("MiniEConnection", "0")));
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
        result.add("MiniEConnection");
        return result;
    }

    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws
            IOException {
        if (isMiniEConnection()) {
            commandLineConnection = new MiniECommandLineConnection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber());
        } else {
            this.commandLineConnection = new ExtendedCommandLineConnection(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber());
        }

        this.commandFactory = new CommandFactory(this);
        this.mk6Profile = new MK6Profile(this);
        return getCommandLineConnection();
    }

    public Date getTime() throws IOException {
        TimeInfo ti = new TimeInfo(this);
        return ti.getTime();
    }

    public void setTime() throws IOException {
        TimeInfo ti = new TimeInfo(this);
        ti.setTime(new Date());
    }

    /**
     * The protocol verison
     **/
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:24:26 +0200 (Thu, 26 Nov 2015)$";
    }

    public String getFirmwareVersion() throws IOException {
        return "Equipment model id:" + getCommandFactory().getReadCommand(0xF000).getRegister().getString() + "\n" + // Equipment model id
                "Software revision:" + getCommandFactory().getReadCommand(0xF003).getRegister().getString() + "\n" + // software version
                "Last version nr:" + getCommandFactory().getReadCommand(0xFC18).getRegister().getString() + "\n" + // last version number
                "Last revision nr:" + getCommandFactory().getReadCommand(0xFC19).getRegister().getString() + "\n" + // last revision number
                "Software revision nr:" + getCommandFactory().getReadCommand(0xF090).getRegister().getString() + "\n" + // software revision number
                "Serial number:" + getSerialNumber(); // serial number
    }

    public String getSerialNumber() {
        return getCommandFactory().getReadCommand(0xF002).getRegister().getString(); // Serial number
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return this.mk6Profile.getProfileData(from, to, includeEvents);
    }


    /*******************************************************************************************
     * R e g i s t e r P r o t o c o l  i n t e r f a c e
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
     *
     * @return the TimeZone
     */
    public TimeZone getTimeZone() {
        if (this.timeZone == null) {
            this.timeZone = ProtocolUtils.getWinterTimeZone(super.getTimeZone());
        }
        return this.timeZone;
    }

    public ExtendedCommandLineConnection getCommandLineConnection() {
        return this.commandLineConnection;
    }

    public CommandFactory getCommandFactory() {
        return this.commandFactory;
    }

    public ObisCodeFactory getObicCodeFactory() throws IOException {
        if (this.obisCodeFactory == null) {
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
        return this.statusFlagChannel == 1;
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
     * @return the useOldProfileFromDate
     */
    public boolean useOldProfileFromDate() {
        return useOldProfileFromDate;
    }

    @Override
    public boolean useExtendedCommand() {
        return true;
    }

    @Override
    public int getMaxNrOfRetries() {
        return getInfoTypeRetries();
    }

    @Override
    public String getConfiguredSerialNumber() {
        return getInfoTypeSerialNumber();
    }

    /**
     * @param useOldProfileFromDate the useOldProfileFromDate to set
     */
    public void setUseOldProfileFromDate(boolean useOldProfileFromDate) {
        this.useOldProfileFromDate = useOldProfileFromDate;
    }

    public void setConnectionType(int connectionType) {
        this.connectionType = connectionType;
    }

    public boolean isMiniEConnection() {
        return this.connectionType == MINI_E_CONNECTION_TYPE;
    }

    @Override
    public boolean useHardCodedInfo() {
        return false;
    }

}
