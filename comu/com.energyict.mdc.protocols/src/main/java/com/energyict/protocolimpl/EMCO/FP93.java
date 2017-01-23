package com.energyict.protocolimpl.EMCO;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.MessageResult;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.messaging.Message;
import com.energyict.mdc.protocol.api.messaging.MessageTag;
import com.energyict.mdc.protocol.api.messaging.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 17/01/12
 * Time: 15:22
 */
public class FP93 extends AbstractProtocol implements MessageProtocol{

    @Override
    public String getProtocolDescription() {
        return "EMCO FP93 Steam Meter";
    }

    private FP93Connection connection;
    private ObisCodeMapper obisCodeMapper;
    private EventLog eventLog;
    private String unitInformationBlock;
    private int deviceID;

    private final MessageProtocol messageProtocol;

    @Inject
    public FP93(PropertySpecService propertySpecService) {
        super(propertySpecService);
        this.messageProtocol = new FP93Messages(this);
    }

    /**
     * Abstract method to implement the logon and authentication.
     *
     * @throws java.io.IOException Exception thrown when the logon fails.
     */
    @Override
    protected void doConnect() throws IOException {
        // No logon or authentication needed.
        // The 'Unit information block' will be read out, to check whether or not the device is accessible.
        // In this request the Serial Number property is used, so this is an implicit serial number check too.
        try {
            ObisCode unitInformationBlockObis = getObisCodeMapper().searchRegisterMapping(94).getObisCode();
            unitInformationBlock = getObisCodeMapper().readRegister(unitInformationBlockObis).getText();
        } catch (Exception e) {
            throw new ProtocolConnectionException("Error during connect - Failed to read out the Unit Information Block. Probably wrong phone number or device ID entered.");
        }
    }

    /**
     * Abstract method to implement the logoff
     *
     * @throws java.io.IOException thrown when the logoff fails
     */
    @Override
    protected void doDisConnect() throws IOException {
        // No disconnect needed.
    }

    /**
     * Abstract method to add custom properties
     *
     * @param properties The properties map to get properties from.
     * @throws MissingPropertyException
     *          Thrown when a particular proiperty is mandatory.
     * @throws InvalidPropertyException
     *          Thrown when a particular property has an invalid value.
     */
    @Override
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        try {
            this.deviceID = Integer.parseInt(getInfoTypeDeviceID());
            if (deviceID> 99999) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException("Invalid Device ID. The Device ID should be a 1 to 5 digit number.");
        }
    }

    /**
     * Abstract method to add the optional custom properties to as string.
     *
     * @return List the optional custom properties list of Strings
     */
    @Override
    protected List doGetOptionalKeys() {
        return new ArrayList();
    }

    /**
     * Abstract method that implements the construction of all objects needed during the meter protocol session. Last construction is a ProtocolConnection.
     *
     * @param inputStream             Communication inputstream
     * @param outputStream            Communication outputstream
     * @param timeoutProperty         Protocol timeout property. Used to control the interframe timeout. Value of the custom property "Timeout"
     * @param protocolRetriesProperty Used to control the nr of retries whan a CRC, timeout, .. or other error happens during communication. Value of the custom property "Retries"
     * @param forcedDelay             A delay parameter that can be used in the communication classes for example to add delays between communication frames. Value of the custom property "ForcedDelay"
     * @param echoCancelling          Enable or disable echo cancelling. Value of the custom property "EchoCancelling"
     * @param protocolCompatible      Used to control protocol compatibility when the protocol is a member of a group protocols. Value of the custom property "ProtocolCompatible"
     * @param encryptor               Interface to control encryption
     * @param halfDuplexController    Interface to control the HalfDuplex behaviour
     * @return ProtocolConnection interface. Most of the time a connection class is build that implements the ProtocolConnection interface. Thet connection class contains the datalink and phy communication routiones.
     * @throws java.io.IOException Thrown when something goes wrong
     */
    @Override
    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {
        connection = new FP93Connection(this, inputStream, outputStream, timeoutProperty, protocolRetriesProperty);
        return connection;
    }

    /**
     * Override this method when requesting time from the meter is needed.
     *
     * @return Date object with the metertime
     * @throws java.io.IOException thrown when something goes wrong
     */
    @Override
    public Date getTime() throws IOException {
        return getEventLog().getTimeFromTimeStampRegister(70);
    }

    /**
     * Override this method when setting the time in the meter is needed
     *
     * @throws java.io.IOException thrown when something goes wrong
     */
    @Override
    public void setTime() throws IOException {
        //ToDo - replace if device supports clock synchronisation
    }

    /**
     * Getter for the device ID
     */
    public int getDeviceID() {
        return deviceID;
    }

    /**
     * Override this method to control the protocolversion This method is informational only.
     *
     * @return String with protocol version
     */
    @Override
    public String getProtocolVersion() {
         return "$Date: 2012-03-09 09:42:11 +0100 (vr, 09 mrt 2012) $";
    }

    /**
     * Override this method when requesting the meter firmware version is needed. This method is informational only.
     *
     * @return String with firmware version. This can also contain other important info of the meter.
     * @throws java.io.IOException thrown when something goes wrong
     * @throws UnsupportedException Thrown when that method is not supported
     */
    @Override
    public String getFirmwareVersion() throws IOException {
        return unitInformationBlock.substring(0, unitInformationBlock.lastIndexOf("-"));
    }

    public FP93Connection getConnection() {
        return connection;
    }

    /*
     * @As the FP93 doesn't support load profiles, this method will return 0.
     * @throws com.energyict.protocol.UnsupportedException
     *                             thrown when not supported
     * @throws java.io.IOException thrown when something goes wrong
     */
    @Override
    public int getNumberOfChannels() throws IOException {
        return 0;
    }


    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        RegisterMapping registerMapping = getObisCodeMapper().searchRegisterMapping(obisCode);
        return new RegisterInfo("[" + registerMapping.getObjectId() + "] " + registerMapping.getDescription());
    }

    /**
     * Override this method when requesting an obiscode mapped register from the meter.
     *
     * @param obisCode obiscode rmapped register to request from the meter
     * @return RegisterValue object
     * @throws java.io.IOException thrown when somethiong goes wrong
     */
    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
            if (ProtocolTools.setObisCodeField(obisCode, 1, (byte) 0).equals(ObisCode.fromString("0.0.10.0.1.255"))) {
                // SPECIAL RESET REGISTER - NOT ALLOWED TO READ OUT
                throw new NoSuchRegisterException(obisCode + " NOT SUPPORTED!");
            }
            return getObisCodeMapper().readRegister(obisCode);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error while reading register " + obisCode + ": " + e.getMessage());
            throw new NoSuchRegisterException();
        }
    }

    public ObisCodeMapper getObisCodeMapper() {
        if (obisCodeMapper == null) {
            obisCodeMapper = new ObisCodeMapper(this);
        }
        return obisCodeMapper;
    }

    public EventLog getEventLog() {
        if (eventLog == null) {
            eventLog = new EventLog(this);
        }
        return eventLog;
    }

    /**
     * Override this method to request the load profile from the meter from to.
     *
     * @param from          request from
     * @param to            request to
     * @param includeEvents eneble or disable requesting of meterevents
     * @return ProfileDta object
     * @throws IOException Thrown when something goes wrong
     * @throws UnsupportedException Thrown when not supported
     */
    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return getEventLog().readEvents();
    }

    //-------------------------------- MESSAGING --------------------------------//
    public void applyMessages(List messageEntries) throws IOException {
        this.messageProtocol.applyMessages(messageEntries);
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return this.messageProtocol.queryMessage(messageEntry);
    }

    public List getMessageCategories() {
        return this.messageProtocol.getMessageCategories();
    }

    public String writeMessage(Message msg) {
        return this.messageProtocol.writeMessage(msg);
    }

    public String writeTag(MessageTag tag) {
        return this.messageProtocol.writeTag(tag);
    }

    public String writeValue(MessageValue value) {
        return this.messageProtocol.writeValue(value);
    }
}