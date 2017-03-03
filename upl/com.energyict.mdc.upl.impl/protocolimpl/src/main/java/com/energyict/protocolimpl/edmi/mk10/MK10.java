package com.energyict.protocolimpl.edmi.mk10;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.MeterProtocol;
import com.energyict.protocol.MissingPropertyException;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.edmi.common.CommandLineProtocol;
import com.energyict.protocolimpl.edmi.common.command.CommandFactory;
import com.energyict.protocolimpl.edmi.common.command.CommandResponseException;
import com.energyict.protocolimpl.edmi.common.command.TimeInfo;
import com.energyict.protocolimpl.edmi.common.connection.ExtendedCommandLineConnection;
import com.energyict.protocolimpl.edmi.common.connection.MiniECommandLineConnection;
import com.energyict.protocolimpl.edmi.mk10.registermapping.MK10Register;
import com.energyict.protocolimpl.edmi.mk10.registermapping.ObisCodeFactory;
import com.energyict.protocolimpl.edmi.mk10.registermapping.ObisCodeMapper;
import com.energyict.protocolimpl.edmi.mk10.streamfilters.MK10PushInputStream;
import com.energyict.protocolimpl.edmi.mk10.streamfilters.MK10PushOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

/**
 * @author jme
 *         <p/>
 *         Changes:
 *         <p/>
 *         jme: 17/11/2008 -> Fixed meter event bugs (wrong mappings from event number to description string)
 *         jme: 17/11/2008 -> Fixed bug in load profile raw data conversion (negative units & overflow)
 *         jme: 17/11/2008 -> Fixed bug in load profile (start date <-> first entry) first entry can be different from 0 !!!
 *         jme: 16/12/2008 -> Switched export/import for registers and load surveys according IEC and not ANSI
 *         jme: 18/12/2008 -> Removed MISSING flag when only incomplete interval and no missing data
 *         jme: 07/01/2009 -> Added register readings for Abs values
 *         jme: 13/01/2009 -> Minor changes to support push protocol
 *         jme: 19/01/2009 -> Fixed issue with events. Do not read events when firstentry > lastentry
 *         jme: 19/01/2009 -> Fixed issue with registers (rates). Rate can be 0 for unified rate and 1 to 8 (and not 1 to 7 !!!).
 *         jme: 19/01/2009 -> Hard coded some information for the most used registers.
 *         jme: 20/01/2009 -> Fixed register scaling and decimal point.
 *         jme: 21/01/2009 -> Added custom property to disable log-off after communication to prevent modem disconnect.
 *         gna: 24/02/2009 -> Added the units and extra scaler for instantaneous values.
 *         jme: 14/01/2010 -> Fixed bug in load profile. Protocol was unable to read all the data from the meter (CRM ticket 12855)
 *         gna: 28/01/2010 -> Changed loadProfileReadout (Mantis 6113)
 *         jme: 31/05/2010 -> COMMUNICATION-14 - EDMI MK10 & MK7 Registers Query (CRM 13712): Fixed register addresses from billing registers
 *         jme: 09/07/2010 -> COMMUNICATION-59 - Fixed timeouts when udp packets were > 1024 bytes.
 *         sva: 29/10/2012 -> EISERVERSG-1200 - The Generic MK10Push inbound protocol is deprecated, it should be replaced by the MK10InboundDeviceProtocol (doing the inbound discovery), combined with the regular MK10 protocol.
 **/
public class MK10 extends AbstractProtocol implements SerialNumberSupport, CommandLineProtocol {

    private static final boolean USE_HARD_INFO = true;
    private ExtendedCommandLineConnection commandLineConnection = null;
    private CommandFactory commandFactory = null;
    private ObisCodeFactory obisCodeFactory = null;
    MK10Profile mk10Profile = null;
    private int loadSurveyNumber = 0;
    private boolean pushProtocol = false;
    private boolean logOffDisabled = true;
    private boolean fullDebugLogging = false;
    private int connectionType;
    private static final int MINI_E_CONNECTION_TYPE = 1;
    private ObisCodeMapper obisCodeMapper;

    /**
     * Creates a new instance of MK10
     */
    public MK10() {
    }

    protected void doConnect() throws IOException {
        if (!isPushProtocol() && !isMiniEConnection()) {
            getCommandFactory().enterCommandLineMode();
        }
        getCommandFactory().logon(getInfoTypeDeviceID(), getInfoTypePassword());
    }

    protected void doDisConnect() throws IOException {
        if (!isLogOffDisabled()) {
            getCommandFactory().exitCommandLineMode();
        }
    }

    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        setInfoTypeNodeAddress(properties.getProperty(MeterProtocol.NODEID, "1"));
        validateLoadSurveyNumber(properties.getProperty("LoadSurveyNumber"));
        setLoadSurveyNumber(Integer.parseInt(properties.getProperty("LoadSurveyNumber").trim()) - 1);
        setForcedDelay(Integer.parseInt(properties.getProperty("ForcedDelay", "0").trim()));
        setLogOffDisabled(Integer.parseInt(properties.getProperty("DisableLogOff", "0").trim()));
        setPushProtocol(properties.getProperty("PushProtocol", "0").trim().equalsIgnoreCase("1"));
        setFullDebugLogging(properties.getProperty("FullDebug", "0").equalsIgnoreCase("1"));
        setConnectionType(Integer.parseInt(properties.getProperty("MiniEConnection", "0")));
    }

    public int getProfileInterval() throws UnsupportedException, IOException {
        return mk10Profile.getProfileInterval();
    }

    public int getNumberOfChannels() throws UnsupportedException, IOException {
        return mk10Profile.getNumberOfChannels();
    }

    protected List doGetOptionalKeys() {
        List result = new ArrayList();
        result.add("LoadSurveyNumber");
        result.add("DisableLogOff");
        result.add("PushProtocol");
        result.add("FullDebug");
        result.add("MiniEConnection");
        return result;
    }

    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws
            IOException {
        InputStream mk10InputStream = isPushProtocol()
                ? new MK10PushInputStream(inputStream, isFullDebugLogging() ? getLogger() : null)
                : inputStream;

        OutputStream mk10OutputStream = isPushProtocol()
                ? new MK10PushOutputStream(outputStream, isFullDebugLogging() ? getLogger() : null)
                : outputStream;
        if (isMiniEConnection()) {
            commandLineConnection = new MiniECommandLineConnection(mk10InputStream, mk10OutputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber());
        } else {
            commandLineConnection = new ExtendedCommandLineConnection(mk10InputStream, mk10OutputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber());
        }
        commandFactory = new CommandFactory(this);
        mk10Profile = new MK10Profile(this);

        return commandLineConnection;
    }

    public Date getTime() throws IOException {
        TimeInfo ti = new TimeInfo(this);
        return ti.getTime();
    }

    public void setTime() throws IOException {
        TimeInfo ti = new TimeInfo(this);
        ti.setTime(new Date());
    }

    public String getProtocolVersion() {
        return "$Date: Mon Nov 30 10:55:54 2015 +0100 $";
    }

    public String getFirmwareVersion() throws IOException, UnsupportedException {
        return "Equipment model id:" + getCommandFactory().getReadCommand(MK10Register.SYSTEM_MODEL_ID).getRegister().getString() + "\n" + // Equipment model id
                "Software version:" + getCommandFactory().getReadCommand(MK10Register.SYSTEM_SOFTWARE_VERSION).getRegister().getString() + "\n" + // Software version
                "Software revision:" + getCommandFactory().getReadCommand(MK10Register.SYSTEM_SOFTWARE_REVISION).getRegister().getString() + "\n" + // Software revision
                "Bootloader revision:" + getCommandFactory().getReadCommand(MK10Register.SYSTEM_BOOTLOADER_REVISION).getRegister().getString() + "\n" + // Software revision
                "Serial number:" + getSerialNumber(); // serial number
    }

    public String getSerialNumber() {
        return getCommandFactory().getReadCommand(MK10Register.SYSTEM_SERIALNUMBER).getRegister().getString(); // Serial number
    }

    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException, UnsupportedException {
        return mk10Profile.getProfileData(from, to, includeEvents);
    }

    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
            return getObisCodeMapper().getRegisterValue(obisCode);
        } catch (CommunicationException e) {
            if (e.getCause() instanceof CommandResponseException && ((CommandResponseException) e.getCause()).getResponseCANCode() == 3) {
                throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
            } else {
                throw e; // Rethrow the original communication exception
            }
        }
    }

    private ObisCodeMapper getObisCodeMapper() {
        if (obisCodeMapper == null) {
            obisCodeMapper = new ObisCodeMapper(this);
        }
        return obisCodeMapper;
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

    protected String getRegistersInfo(int extendedLogging) throws IOException {
        return getObisCodeFactory().getRegisterInfoDescription();
    }

    public TimeZone getTimeZone() {
        return ProtocolUtils.getWinterTimeZone(super.getTimeZone());
    }

    @Override
    public ExtendedCommandLineConnection getCommandLineConnection() {
        return commandLineConnection;
    }

    public CommandFactory getCommandFactory() {
        return commandFactory;
    }

    public ObisCodeFactory getObisCodeFactory() {
        if (obisCodeFactory == null) {
            obisCodeFactory = new ObisCodeFactory(this);
        }
        return obisCodeFactory;
    }

    public int getLoadSurveyNumber() {
        return loadSurveyNumber;
    }

    private void setLoadSurveyNumber(int loadSurveyNr) {
        this.loadSurveyNumber = loadSurveyNr;
    }

    private void validateLoadSurveyNumber(String value) throws MissingPropertyException, InvalidPropertyException {
        if (value == null) {
            throw new MissingPropertyException("No LoadSurveyNumber property found! Must be 1 or 2 for the EDMI MK10 meter.");
        }
        if (!value.trim().equalsIgnoreCase("1") && !value.trim().equalsIgnoreCase("2")) {
            throw new InvalidPropertyException("Wrong LoadSurveyNumber value: " + value + "! Must be 1 or 2 for the EDMI MK10 meter.");
        }
    }

    @Override
    public boolean useOldProfileFromDate() {
        return false;
    }

    @Override
    public boolean useExtendedCommand() {
        return false;
    }

    @Override
    public int getMaxNrOfRetries() {
        return getInfoTypeRetries();
    }

    @Override
    public String getConfiguredSerialNumber() {
        return getInfoTypeSerialNumber();
    }

    public boolean useHardCodedInfo() {
        return USE_HARD_INFO;
    }

    public boolean isPushProtocol() {
        return pushProtocol;
    }

    /**
     * Setter for the pushProtocol field. This field is set to true if the protocol is used with an inbound UDP connection
     *
     * @param pushProtocol
     */
    public void setPushProtocol(boolean pushProtocol) {
        this.pushProtocol = pushProtocol;
    }

    public boolean isLogOffDisabled() {
        return logOffDisabled;
    }

    public void setLogOffDisabled(int logOffDisabled) {
        this.logOffDisabled = (logOffDisabled == 1);
    }

    public boolean isFullDebugLogging() {
        return fullDebugLogging;
    }

    public void setFullDebugLogging(boolean fullDebugLogging) {
        this.fullDebugLogging = fullDebugLogging;
    }

    public void setConnectionType(int connectionType) {
        this.connectionType = connectionType;
    }

    public boolean isMiniEConnection() {
        return this.connectionType == MINI_E_CONNECTION_TYPE;
    }
}
