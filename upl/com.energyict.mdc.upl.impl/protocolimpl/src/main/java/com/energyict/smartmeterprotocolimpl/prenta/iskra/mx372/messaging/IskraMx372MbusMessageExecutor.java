package com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.messaging;

import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.mdc.upl.messages.legacy.MessageEntry;
import com.energyict.messaging.LegacyLoadProfileRegisterMessageBuilder;
import com.energyict.messaging.LegacyPartialLoadProfileMessageBuilder;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.generic.MessageParser;
import com.energyict.protocolimpl.generic.ParseUtils;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.smartmeterprotocolimpl.prenta.iskra.mx372.IskraMx372;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 24/01/12
 * Time: 16:38
 */
public class IskraMx372MbusMessageExecutor extends MessageParser {

    IskraMx372 iskraMx372;

    private ObisCode valveState = ObisCode.fromString("0.0.128.30.31.255");
    private ObisCode valveControl = ObisCode.fromString("0.0.128.30.30.255");

    public IskraMx372MbusMessageExecutor(IskraMx372 iskraMx372Protocol) {
        this.iskraMx372 = iskraMx372Protocol;
    }

    @Override
    protected TimeZone getTimeZone() {
        return iskraMx372.getDlmsSession().getTimeZone();
    }

    /**
     * Indicates that each message has to be executed by the protocol.
     *
     * @param messageEntry a definition of which message needs to be sent
     * @return a state of the message which was just sent
     * @throws java.io.IOException if a logical error occurs
     */
    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        MessageResult msgResult = null;
        try {
            if (isItThisMessage(messageEntry, RtuMessageConstant.DISCONNECT_LOAD)) {
                infoLog("Sending disconnectLoad message for meter with serialnumber: " + messageEntry.getSerialNumber());
                connectDisconnectDevice(messageEntry, false);
                infoLog("DisconnectLoad message successful.");
            } else if (isItThisMessage(messageEntry, RtuMessageConstant.CONNECT_LOAD)) {
                infoLog("Sending connectLoad message for meter with serialnumber: " + messageEntry.getSerialNumber());
                connectDisconnectDevice(messageEntry, true);
                infoLog("ConnectLoad message successful.");
            } else if (isItThisMessage(messageEntry, RtuMessageConstant.MBUS_SET_VIF)) {
                infoLog("Sending MbusSetVif message for meter with serialnumber: " + messageEntry.getSerialNumber());
                mbusSetVif(messageEntry);
                infoLog("MbusSetVif message successful.");
            } else if (isItThisMessage(messageEntry, LegacyLoadProfileRegisterMessageBuilder.getMessageNodeTag())) {
                infoLog("Sending LoadProfileRegister message for meter with serialnumber: " + messageEntry.getSerialNumber());
                msgResult = doReadLoadProfileRegisters(messageEntry);
            } else if (isItThisMessage(messageEntry, LegacyPartialLoadProfileMessageBuilder.getMessageNodeTag())) {
                infoLog("Sending PartialLoadProfile message for meter with serialnumber: " + messageEntry.getSerialNumber());
                msgResult = doReadPartialLoadProfile(messageEntry);
            } else {
                msgResult = MessageResult.createFailed(messageEntry, "Message not supported by the protocol.");
            }

            if (msgResult == null) {
                msgResult = MessageResult.createSuccess(messageEntry);
            } else if (msgResult.isFailed()) {
                iskraMx372.getLogger().severe("Message failed : " + msgResult.getInfo());
            }
        } catch (Exception e) {
            msgResult = MessageResult.createFailed(messageEntry, e.getMessage());
            iskraMx372.getLogger().severe("Message failed : " + e.getMessage());
        }
        return msgResult;
    }

    /**
     * Checks if the given MessageEntry contains the corresponding MessageTag
     *
     * @param messageEntry the given messageEntry
     * @param messageTag   the tag to check
     * @return true if this is the message, false otherwise
     */
    private boolean isItThisMessage(MessageEntry messageEntry, String messageTag) {
        return messageEntry.getContent().indexOf(messageTag) >= 0;
    }

    private void connectDisconnectDevice(MessageEntry messageEntry, boolean connect) throws IOException {
        Unsigned8 channel = new Unsigned8(getMbusAddress(messageEntry.getSerialNumber()) + 1);
        iskraMx372.getCosemObjectFactory().getData(valveControl).setValueAttr(channel);
        Unsigned8 state = new Unsigned8(connect ? 1 : 0);
        iskraMx372.getCosemObjectFactory().getData(valveState).setValueAttr(state);
    }

    private void mbusSetVif(MessageEntry messageEntry) throws IOException {
        String vif = getMessageValue(messageEntry.getContent(), RtuMessageConstant.MBUS_SET_VIF);
        if (vif.length() != 16) {
            throw new IOException("VIF must be 8 characters long.");
        }
        ObisCode obisCode = ObisCode.fromString("0." + (getMbusAddress(messageEntry.getSerialNumber()) + 1) + ".128.50.30.255");
        byte[] berEncodedByteArray = OctetString.fromByteArray(ParseUtils.hexStringToByteArray(vif)).getBEREncodedByteArray();
        iskraMx372.getCosemObjectFactory().getGenericWrite(obisCode, 2, 1).write(berEncodedByteArray);
    }

    private MessageResult doReadLoadProfileRegisters(final MessageEntry msgEntry) throws SAXException, IOException {
        return iskraMx372.getMessageProtocol().doReadLoadProfileRegisters(msgEntry);
    }

    private MessageResult doReadPartialLoadProfile(final MessageEntry msgEntry) throws IOException, SAXException {
        return iskraMx372.getMessageProtocol().doReadPartialLoadProfile(msgEntry);
    }

    private int getMbusAddress(String serialNumber) throws IOException {
        return iskraMx372.getPhysicalAddressFromSerialNumber(serialNumber);
    }

    /**
     * Log the given message to the logger with the INFO level
     *
     * @param messageToLog
     */
    private void infoLog(String messageToLog) {
        iskraMx372.getLogger().info(messageToLog);
    }
}
