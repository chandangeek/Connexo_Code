package com.energyict.protocolimpl.EMCO;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilder;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.nls.PropertyTranslationKeys;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 17/01/12
 * Time: 15:22
 */
public class FP93 extends AbstractProtocol implements MessageProtocol {

    private final MessageProtocol messageProtocol;
    private FP93Connection connection;
    private ObisCodeMapper obisCodeMapper;
    private EventLog eventLog;
    private String unitInformationBlock;
    private int deviceID;

    public FP93(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
        this.messageProtocol = new FP93Messages(this);
    }

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

    @Override
    protected void doDisconnect() throws IOException {
        // No disconnect needed.
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        super.getUPLPropertySpecs()
                .stream()
                .filter(propertySpec -> !propertySpec.getName().equals(ADDRESS.getName()))
                .forEach(propertySpecs::add);
        PropertySpecService propertySpecService = this.getPropertySpecService();
        PropertySpecBuilder<BigDecimal> specBuilder = UPLPropertySpecFactory.specBuilder(ADDRESS.getName(), false, PropertyTranslationKeys.EMCO_ADDRESS, () -> propertySpecService.boundedBigDecimalSpec(BigDecimal.ONE, BigDecimal.valueOf(99999)));
        propertySpecs.add(specBuilder.finish());
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
        try {
            this.deviceID = Integer.parseInt(getInfoTypeDeviceID());
        } catch (NumberFormatException e) {
            throw new InvalidPropertyException(e, this.getClass().getSimpleName() + ": validation of properties failed before, the Device ID should be a 1 to 5 digit number.");
        }
    }

    @Override
    protected ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {
        connection = new FP93Connection(this, inputStream, outputStream, timeoutProperty, protocolRetriesProperty);
        return connection;
    }

    @Override
    public Date getTime() throws IOException {
        return getEventLog().getTimeFromTimeStampRegister(70);
    }

    @Override
    public void setTime() throws IOException {
        //ToDo - replace if device supports clock synchronisation
    }

    public int getDeviceID() {
        return deviceID;
    }

    @Override
    public String getProtocolDescription() {
        return "EMCO FP93 Steam Meter";
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2012-03-09 09:42:11 +0100 (vr, 09 mrt 2012) $";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return unitInformationBlock.substring(0, unitInformationBlock.lastIndexOf("-"));
    }

    public FP93Connection getConnection() {
        return connection;
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return 0;
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        RegisterMapping registerMapping = getObisCodeMapper().searchRegisterMapping(obisCode);
        return new RegisterInfo("[" + registerMapping.getObjectId() + "] " + registerMapping.getDescription());
    }

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

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return getEventLog().readEvents();
    }

    @Override
    public void applyMessages(List<MessageEntry> messageEntries) throws IOException {
        this.messageProtocol.applyMessages(messageEntries);
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return this.messageProtocol.queryMessage(messageEntry);
    }

    @Override
    public List getMessageCategories() {
        return this.messageProtocol.getMessageCategories();
    }

    @Override
    public String writeMessage(Message msg) {
        return this.messageProtocol.writeMessage(msg);
    }

    @Override
    public String writeTag(MessageTag tag) {
        return this.messageProtocol.writeTag(tag);
    }

    @Override
    public String writeValue(MessageValue value) {
        return this.messageProtocol.writeValue(value);
    }

    @Override
    public Logger getLogger() {
        return super.getLogger();
    }

}