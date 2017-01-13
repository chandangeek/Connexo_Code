package com.energyict.protocolimpl.dlms.Z3;

import com.energyict.mdc.upl.NoSuchRegisterException;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.messages.legacy.Message;
import com.energyict.mdc.upl.messages.legacy.MessageAttribute;
import com.energyict.mdc.upl.messages.legacy.MessageAttributeSpec;
import com.energyict.mdc.upl.messages.legacy.MessageCategorySpec;
import com.energyict.mdc.upl.messages.legacy.MessageElement;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.mdc.upl.messages.legacy.MessageSpec;
import com.energyict.mdc.upl.messages.legacy.MessageTag;
import com.energyict.mdc.upl.messages.legacy.MessageTagSpec;
import com.energyict.mdc.upl.messages.legacy.MessageValue;
import com.energyict.mdc.upl.messages.legacy.MessageValueSpec;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.MissingPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecBuilderWizard;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.HDLCConnection;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.TCPIPConnection;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.Integer16;
import com.energyict.dlms.axrdencoding.Integer32;
import com.energyict.dlms.axrdencoding.Integer64;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.mdw.core.Device;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MessageResult;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterProtocol;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.base.PluggableMeterProtocol;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;
import static com.energyict.mdc.upl.MeterProtocol.Property.RETRIES;
import static com.energyict.mdc.upl.MeterProtocol.Property.SECURITYLEVEL;
import static com.energyict.mdc.upl.MeterProtocol.Property.TIMEOUT;

/**
 * @author gna
 *
 * This protocol is a copy of the generic one.
 * The message getBudget is deleted and replaced by the standard registers readout.
 *
 * Changes:
 * GNA|22012009| Changed the default profileInterval to 900s instead of 0s because you can not select 0s as an interval.
 *
 */
@Deprecated
public class DLMSZ3Messaging extends PluggableMeterProtocol implements MessageProtocol, ProtocolLink, RegisterProtocol, Constant {

    private final PropertySpecService propertySpecService;
    /**
     * Properties
     */
    private int securityLevel;    // 0: No Authentication - 1: Low Level - 2: High Level
    private int connectionMode; // 0: DLMS/HDLC - 1: DLMS/TCPIP
    private int clientMacAddress;
    private int serverLowerMacAddress;
    private int serverUpperMacAddress;
    private int timeout;
    private int forceDelay;
    private int retries;
    private int addressingMode;
    private int requestTimeZone;
    private String password;

    private CosemObjectFactory cosemObjectFactory;
    //    private CommunicationProfile commProfile;
    private DLMSConnection dlmsConnection;
    private DLMSMeterConfig dlmsMeterConfig;
    private AARQ aarq;
    private Logger logger;
    private Clock clock;
    private Device rtu;
    private TimeZone timeZone;

    public DLMSZ3Messaging(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {
        try {
            this.logger = logger;
            this.timeZone = timeZone;
            this.cosemObjectFactory = new CosemObjectFactory(this);

            if (this.connectionMode == 0) {
                this.dlmsConnection = new HDLCConnection(inputStream, outputStream, this.timeout, this.forceDelay, this.retries, this.clientMacAddress, this.serverLowerMacAddress, this.serverUpperMacAddress, this.addressingMode);
            } else {
                this.dlmsConnection = new TCPIPConnection(inputStream, outputStream, this.timeout, this.forceDelay, this.retries, this.clientMacAddress, this.serverLowerMacAddress, getLogger());
            }

            this.dlmsMeterConfig = DLMSMeterConfig.getInstance("EZ3");

        } catch (DLMSConnectionException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }

    }

    @Override
    public ApplicationServiceObject getAso() {
        return null;      //Not used
    }

    public void log(Level level, String tekst) {
        this.logger.log(level, tekst);
    }

    public Device getMeter() {
        return this.rtu;
    }

    @Override
    public void connect() throws IOException {
        try {
            getDLMSConnection().connectMAC();
            getDLMSConnection().setIskraWrapper(1);
            aarq = new AARQ(this.securityLevel, this.password, getDLMSConnection());
        } catch (DLMSConnectionException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public void disconnect() throws IOException {
        try {
            getDLMSConnection().disconnectMAC();
        } catch (DLMSConnectionException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public DLMSConnection getDLMSConnection() {
        return this.dlmsConnection;
    }

    public CosemObjectFactory getCosemObjectFactory() {
        return this.cosemObjectFactory;
    }

    @Override
    public String getFirmwareVersion() {
        return null;
    }

    @Override
    public String getRegister(String name) throws IOException {
        return Long.toString(getCosemObjectFactory().getRegister(ObisCode.fromString(name)).getValue());
    }

    private Clock getClock() throws IOException {
        if (this.clock == null) {
            this.clock = getCosemObjectFactory().getClock(ObisCode.fromString("0.0.1.0.0.255"));
        }
        return this.clock;
    }

    @Override
    public Date getTime() throws IOException {
        return getClock().getDateTime();
    }

    @Override
    public void setTime() throws IOException {
        DateTime dateTime = new DateTime(Calendar.getInstance(getTimeZone()));
        getClock().setTimeAttr(dateTime);
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return Arrays.asList(
                this.stringSpec(PASSWORD.getName()),
                this.integerSpec(SECURITYLEVEL.getName()),
                this.integerSpec("ConnectionMode"),
                this.integerSpec("ClientMacAddress"),
                this.integerSpec("ServerLowerMacAddress"),
                this.integerSpec("ServerUpperMacAddress"),
                this.integerSpec(TIMEOUT.getName()),
                this.integerSpec(RETRIES.getName()),
                this.integerSpec("ForceDelay"),
                this.integerSpec("AddressingMode"),
                this.integerSpec("RequestTimeZone"));
    }

    private <T> PropertySpec spec(String name, Supplier<PropertySpecBuilderWizard.NlsOptions<T>> optionsSupplier) {
        return UPLPropertySpecFactory.specBuilder(name, false, optionsSupplier).finish();
    }

    private PropertySpec stringSpec(String name) {
        return this.spec(name, this.propertySpecService::stringSpec);
    }

    private PropertySpec integerSpec(String name) {
        return this.spec(name, this.propertySpecService::integerSpec);
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws InvalidPropertyException, MissingPropertyException {
        this.password = properties.getTypedProperty(PASSWORD.getName(), "");
        this.securityLevel = Integer.parseInt(properties.getTypedProperty(SECURITYLEVEL.getName(), "0"));
        this.connectionMode = Integer.parseInt(properties.getTypedProperty("ConnectionMode", "1"));
        this.clientMacAddress = Integer.parseInt(properties.getTypedProperty("ClientMacAddress", "16"));
        this.serverLowerMacAddress = Integer.parseInt(properties.getTypedProperty("ServerLowerMacAddress", "1"));
        this.serverUpperMacAddress = Integer.parseInt(properties.getTypedProperty("ServerUpperMacAddress", "17"));
        // if HDLC set default timeout to 10s, if TCPIP set default timeout to 60s
        this.timeout = Integer.parseInt(properties.getTypedProperty(TIMEOUT.getName(), (this.connectionMode == 0) ? "10000" : "60000"));
        this.forceDelay = Integer.parseInt(properties.getTypedProperty("ForceDelay", "100"));
        this.retries = Integer.parseInt(properties.getTypedProperty(RETRIES.getName(), "3"));
        this.addressingMode = Integer.parseInt(properties.getTypedProperty("AddressingMode", "2"));
        this.requestTimeZone = Integer.parseInt(properties.getTypedProperty("RequestTimeZone", "0"));
    }

    @Override
    public void setRegister(String name, String value) throws IOException {
        String type = value.substring(0, value.indexOf(" "));
        String dataStr = value.substring(value.indexOf(" ") + 1, value.length());

        int typeInt = 0;

        try {
            typeInt = Integer.parseInt(type);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new IOException("Type is not correct.");
        }

        if (typeInt != AxdrType.NULL.getTag()) {

            final AxdrType axdrType = AxdrType.fromTag((byte) (typeInt & 0x0FF));
            switch (axdrType) {

                case BOOLEAN: {
                    byte[] data = new byte[]{axdrType.getTag(), (byte) Integer.parseInt(dataStr)};
                    getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(data);
                }
                ;
                break;

                case INTEGER: {
                    Integer8 integer = new Integer8(Integer.parseInt(dataStr));
                    getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(integer.getBEREncodedByteArray());
                }
                ;
                break;

                case LONG: {
                    Integer16 integer = new Integer16(Integer.parseInt(dataStr));
                    getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(integer.getBEREncodedByteArray());
                }
                ;
                break;

                case DOUBLE_LONG: {
                    Integer32 integer = new Integer32(Integer.parseInt(dataStr));
                    getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(integer.getBEREncodedByteArray());
                }
                ;
                break;

                case LONG64: {
                    Integer64 integer = new Integer64(Integer.parseInt(dataStr));
                    getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(integer.getBEREncodedByteArray());
                }
                ;
                break;

                case LONG_UNSIGNED: {
                    Unsigned16 integer = new Unsigned16(Integer.parseInt(dataStr));
                    getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(integer.getBEREncodedByteArray());
                }
                ;
                break;

                case UNSIGNED: {
                    Unsigned8 integer = new Unsigned8(Integer.parseInt(dataStr));
                    getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(integer.getBEREncodedByteArray());
                }
                ;
                break;

                case DOUBLE_LONG_UNSIGNED: {
                    Unsigned32 integer = new Unsigned32(Integer.parseInt(dataStr));
                    getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(integer.getBEREncodedByteArray());
                }
                ;
                break;

                case OCTET_STRING: {
                    OctetString octString = OctetString.fromString(dataStr);
                    getCosemObjectFactory().getGenericWrite(ObisCode.fromString(name), 2).write(octString.getBEREncodedByteArray());
                }
                ;
                break;

                case TIME: {

                }
                ;
                break;

            }
        }
    }

    public String getVersion() {
        return getProtocolVersion();
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2009-01-19 16:26:22 +0100 (ma, 19 jan 2009) $";
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public DLMSMeterConfig getMeterConfig() {
        return this.dlmsMeterConfig;
    }

    @Override
    public List getMessageCategories() {
        List<MessageCategorySpec> theCategories = new ArrayList<>();
        MessageCategorySpec catPrePaid = new MessageCategorySpec("PrePaid");
        MessageCategorySpec catConnectControl = new MessageCategorySpec("Connect/Disconnect");
        MessageCategorySpec catLoadLimit = new MessageCategorySpec("Load Limiting");

        // Prepaid related messages
        catPrePaid.addMessageSpec(addConfigurePrepaid("Configure prepaid functionality", RtuMessageConstant.PREPAID_CONFIGURED, false));
        catPrePaid.addMessageSpec(addBudgetMsg("Add prepaid credit", RtuMessageConstant.PREPAID_ADD, false));
//        msgSpec = addNoValueMsg("Read prepaid credit", RtuMessageConstant.PREPAID_READ, false);
//        catPrePaid.addMessageSpec(msgSpec);
        catPrePaid.addMessageSpec(addNoValueMsg("Enable prepaid functionality", RtuMessageConstant.PREPAID_ENABLE, false));
        catPrePaid.addMessageSpec(addNoValueMsg("Disable prepaid functionality", RtuMessageConstant.PREPAID_DISABLE, false));

        // Disconnect related messages
        catConnectControl.addMessageSpec(addConnectControlMsg("Disconnect", RtuMessageConstant.DISCONNECT_LOAD, false));
        catConnectControl.addMessageSpec(addConnectControlMsg("Connect", RtuMessageConstant.CONNECT_LOAD, false));

        // Load Limiting related messages
        catLoadLimit.addMessageSpec(addNoValueMsg("Enable load limiting", RtuMessageConstant.LOAD_LIMIT_ENABLE, false));
        catLoadLimit.addMessageSpec(addNoValueMsg("Disable load limiting", RtuMessageConstant.LOAD_LIMIT_DISABLE, false));
        catLoadLimit.addMessageSpec(addParametersLoadLimit("Configure load limiting", RtuMessageConstant.LOAD_LIMIT_CONFIGURE, false));

        theCategories.add(catPrePaid);
        theCategories.add(catConnectControl);
        theCategories.add(catLoadLimit);
        return theCategories;
    }

    private MessageSpec addBudgetMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec(" "));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.PREPAID_BUDGET, true));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addNoValueMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addConfigurePrepaid(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec(" "));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.PREPAID_BUDGET, false));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.PREPAID_THRESHOLD, true));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.PREPAID_READ_FREQUENCY, true));
        for (int i = 1; i < 9; i++) {
            tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.PREPAID_MULTIPLIER + i, false));
        }
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addParametersLoadLimit(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec(" "));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_READ_FREQUENCY, true));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_THRESHOLD, true));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_DURATION, true));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_D1_INVERT, false));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_D2_INVERT, false));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.LOAD_LIMIT_ACTIVATE_NOW, false));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    private MessageSpec addConnectControlMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        tagSpec.add(new MessageValueSpec(" "));
        tagSpec.add(new MessageAttributeSpec(RtuMessageConstant.DIGITAL_OUTPUT, true));
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    @Override
    public String writeTag(MessageTag msgTag) {
        StringBuilder builder = new StringBuilder();

        // a. Opening tag
        builder.append("<");
        builder.append(msgTag.getName());

        // b. Attributes
        for (Iterator<MessageAttribute> it = msgTag.getAttributes().iterator(); it.hasNext(); ) {
            MessageAttribute att = it.next();
            if (att.getValue() == null || att.getValue().isEmpty()) {
                continue;
            }
            builder.append(" ").append(att.getSpec().getName());
            builder.append("=").append('"').append(att.getValue()).append('"');
        }
        if (msgTag.getSubElements().isEmpty()) {
            builder.append("/>");
            return builder.toString();
        }
        builder.append(">");
        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext(); ) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag()) {
                builder.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if (value == null || value.isEmpty()) {
                    return "";
                }
                builder.append(value);
            }
        }

        // d. Closing tag
        builder.append("</");
        builder.append(msgTag.getName());
        builder.append(">");

        return builder.toString();
    }

    @Override
    public String writeValue(MessageValue msgValue) {
        return msgValue.getValue();
    }

    @Override
    public void applyMessages(List rtuMessages) throws IOException {
    }

    @Override
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        MessageHandler messageHandler = new MessageHandler();
        String content = messageEntry.getContent();
        boolean success = false;
        importMessage(content, messageHandler);
        boolean disConnect = messageHandler.getType().equals(RtuMessageConstant.DISCONNECT_LOAD);
        boolean connect = messageHandler.getType().equals(RtuMessageConstant.CONNECT_LOAD);
        boolean prepaidConfiguration = messageHandler.getType().equals(RtuMessageConstant.PREPAID_CONFIGURED);
        boolean prepaidAdd = messageHandler.getType().equals(RtuMessageConstant.PREPAID_ADD);
        boolean prepaidEnable = messageHandler.getType().equals(RtuMessageConstant.PREPAID_ENABLE);
        boolean prepaidDisable = messageHandler.getType().equals(RtuMessageConstant.PREPAID_DISABLE);
        boolean loadLimitConfiguration = messageHandler.getType().equals(RtuMessageConstant.LOAD_LIMIT_CONFIGURE);
        boolean loadLimitEnable = messageHandler.getType().equals(RtuMessageConstant.LOAD_LIMIT_ENABLE);
        boolean loadLimitDisable = messageHandler.getType().equals(RtuMessageConstant.LOAD_LIMIT_DISABLE);

        if (disConnect) {
            // Execute the message
            String digOut = messageHandler.getResult();
            if ("1".equals(digOut) || "2".equals(digOut)) {
                getCosemObjectFactory().getData(digitalOutputObisCode[Integer.parseInt(digOut) - 1]).setValueAttr(new BooleanObject(false));
                success = true;

            } else {
                String error = "Disonnect message does not contain a valid digital output: " + digOut + ".";
                log(Level.INFO, error);
            }
        } else if (connect) {
            // Execute the message
            String digOut = messageHandler.getResult();
            if ("1".equals(digOut) || "2".equals(digOut)) {
                getCosemObjectFactory().getData(digitalOutputObisCode[Integer.parseInt(digOut) - 1]).setValueAttr(new BooleanObject(true));
                success = true;

            } else {
                String error = "Disonnect message does not contain a valid digital output: " + digOut + ".";
                log(Level.INFO, error);
            }
        } else if (prepaidEnable) {
            getCosemObjectFactory().getRegister(prepaidStateObisCode).setValueAttr(new BooleanObject(true));
            success = true;
        } else if (prepaidDisable) {
            getCosemObjectFactory().getRegister(prepaidStateObisCode).setValueAttr(new BooleanObject(false));
            success = true;
        } else if (prepaidConfiguration) {
                /*
                 * Note: after the configuration setting we also enable the prepaid configuration!
                 */
            // The Budget register
            if (messageHandler.getBudget() != null) {
                getCosemObjectFactory().getRegister(prepaidSetBudgetObisCode).setValueAttr(new Integer32(Integer.valueOf(messageHandler.getBudget()).intValue()));
            }

            // The Threshold register
            getCosemObjectFactory().getRegister(prepaidThresholdObisCode).setValueAttr(new Unsigned32(Long.valueOf(messageHandler.getThreshold()).longValue()));

            // The ReadFrequency register
            getCosemObjectFactory().getRegister(prepaidReadFrequencyObisCode).setValueAttr(new Unsigned32(Long.valueOf(messageHandler.getReadFrequency()).longValue()));

            // The Multiplier registers
            for (int i = 0; i < 8; i++) {
                if (messageHandler.getMultiplier(i) != null) {
                    getCosemObjectFactory().getRegister(prepaidMultiplierObisCode[i]).setValueAttr(new Integer32(Integer.valueOf(messageHandler.getMultiplier(i)).intValue()));
                }
            }

            // Enabling the prepaid configuration
            getCosemObjectFactory().getRegister(prepaidStateObisCode).setValueAttr(new BooleanObject(true));

            success = true;
        } else if (prepaidAdd) {
                /*
                 * Note: after the configuration setting we also enable the prepaid configuration!
                 */

            getCosemObjectFactory().getRegister(prepaidAddBudgetObisCode).setValueAttr(new Unsigned32(Long.valueOf(messageHandler.getBudget()).longValue()));

            // Enabling the prepaid configuration
            getCosemObjectFactory().getRegister(prepaidStateObisCode).setValueAttr(new BooleanObject(true));

            success = true;
        } else if (loadLimitConfiguration) {
            // The Threshold register
            getCosemObjectFactory().getRegister(loadLimitThresholdObisCode).setValueAttr(new Unsigned32(Long.valueOf(messageHandler.getLLThreshold()).longValue()));

            // The ReadFrequency register
            getCosemObjectFactory().getRegister(loadLimitReadFrequencyObisCode).setValueAttr(new Unsigned32(Long.valueOf(messageHandler.getLLReadFrequency()).longValue()));

            // The Duration register
            getCosemObjectFactory().getRegister(loadLimitDurationObisCode).setValueAttr(new Unsigned32(Long.valueOf(messageHandler.getLLDuration()).longValue()));

            if (messageHandler.getLLD1Invert() != null) {
                if ("1".equalsIgnoreCase(messageHandler.getLLD1Invert()) || "0".equalsIgnoreCase(messageHandler.getLLD1Invert())) {
                    getCosemObjectFactory().getRegister(loadLimitOutputLogicObisCode[0]).setValueAttr(new BooleanObject(messageHandler.getLLD1Invert().equals(Integer.toString(1))));
                } else {
                    String error = "Configure LoadLimit message does not contain a valid digital output inverter (1): " + messageHandler.getLLD1Invert() + ", only 1(true) or 0(false) alowed.";
                    log(Level.INFO, error);
                }
            }

            if (messageHandler.getLLD2Invert() != null) {
                if ("1".equalsIgnoreCase(messageHandler.getLLD2Invert()) || "0".equalsIgnoreCase(messageHandler.getLLD2Invert())) {
                    getCosemObjectFactory().getRegister(loadLimitOutputLogicObisCode[1]).setValueAttr(new BooleanObject(messageHandler.getLLD2Invert().equals(Integer.toString(1))));
                } else {
                    String error = "Configure LoadLimit message does not contain a valid digital output inverter (2): " + messageHandler.getLLD2Invert() + ", only 1(true) or 0(false) alowed.";
                    log(Level.INFO, error);
                }
            }

            if (messageHandler.getActivateNow() != null) {
                if ("1".equalsIgnoreCase(messageHandler.getActivateNow()) || "0".equalsIgnoreCase(messageHandler.getActivateNow())) {
                    getCosemObjectFactory().getRegister(loadLimitStateObisCode).setValueAttr(new BooleanObject(messageHandler.getActivateNow().equals(Integer.toString(1))));
                } else {
                    String error = "Configure LoadLimit message does not contain a valid activateNow value: " + messageHandler.getActivateNow() + ", only 1(true) or 0(false) alowed.";
                    log(Level.INFO, error);
                }
            }

            success = true;
        } else if (loadLimitEnable) {
            getCosemObjectFactory().getRegister(loadLimitStateObisCode).setValueAttr(new BooleanObject(true));
            success = true;
        } else if (loadLimitDisable) {
            getCosemObjectFactory().getRegister(loadLimitStateObisCode).setValueAttr(new BooleanObject(false));
            success = true;
        } else {
            success = false;
        }

        if (success) {
            return MessageResult.createSuccess(messageEntry);
        } else {
            return MessageResult.createFailed(messageEntry);
        }
    }

    private void importMessage(String message, DefaultHandler handler) {
        try {

            byte[] bai = message.getBytes();
            InputStream i = new ByteArrayInputStream(bai);

            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(i, handler);

        } catch (ParserConfigurationException | SAXException | IOException thrown) {
            thrown.printStackTrace();
            throw new IllegalArgumentException(thrown);
        }
    }

    @Override
    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        try {
            if (obisCode.toString().equalsIgnoreCase(prepaidSetBudgetObisCode.toString())) {
                Register register = getCosemObjectFactory().getRegister(obisCode);
                Date billingDate = getTime();
                return new RegisterValue(obisCode, new Quantity(BigDecimal.valueOf(register.getValue()), Unit.get(BaseUnit.WATTHOUR)), register.getBillingDate(), null, null);
            } else {
                throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported!");
            }
        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                throw new IOException(e.getMessage());
            }
            throw new NoSuchRegisterException("Problems while reading register " + obisCode.toString() + ": " + e.getMessage());
        }

    }

    @Override
    public int getReference() {
        return 0;
    }

    @Override
    public int getRoundTripCorrection() {
        return 0;
    }

    @Override
    public StoredValues getStoredValues() {
        return null;
    }

    @Override
    public TimeZone getTimeZone() {
        try {
            return isRequestTimeZone() ? TimeZone.getTimeZone(Integer.toString(getClock().getTimeZone())) : this.timeZone;
        } catch (IOException e) {
            e.printStackTrace();
            getLogger().log(Level.INFO, "Could not verify meterTimeZone so EIServer timeZone is used.");
            return this.timeZone;
        }
    }

    @Override
    public boolean isRequestTimeZone() {
        return (this.requestTimeZone == 1);
    }

    @Override
    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo("RegisterInfo");
    }

    @Override
    public Quantity getMeterReading(int channelId) throws IOException {
        return null;
    }

    @Override
    public Quantity getMeterReading(String name) throws
            IOException {
        return null;
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return 0;
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws UnsupportedException {
        throw new UnsupportedException("LoadProfile not supported.");
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws UnsupportedException {
        throw new UnsupportedException("LoadProfile not supported.");
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws UnsupportedException {
        throw new UnsupportedException("LoadProfile not supported.");
    }

    @Override
    public int getProfileInterval() throws IOException {
        return 900;
    }

    @Override
    public void initializeDevice() throws IOException {
    }

    @Override
    public void release() throws IOException {
    }

}