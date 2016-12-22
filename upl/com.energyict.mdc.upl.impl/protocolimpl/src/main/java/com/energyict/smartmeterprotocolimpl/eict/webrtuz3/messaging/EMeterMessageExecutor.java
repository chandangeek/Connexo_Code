package com.energyict.smartmeterprotocolimpl.eict.webrtuz3.messaging;

import com.energyict.mdc.upl.messages.legacy.MessageEntry;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.generic.MessageParser;
import com.energyict.protocolimpl.generic.messages.MessageHandler;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.eict.webrtuz3.SlaveMeter;

import java.io.IOException;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 29-aug-2011
 * Time: 15:44:50
 */
public class EMeterMessageExecutor extends MessageParser {

    public static final ObisCode DISCONNECTOR_OBIS = ObisCode.fromString("0.0.96.3.10.255");
    public static final ObisCode DISCONNECTOR_SCRIPT_TABLE_OBIS = ObisCode.fromString("0.0.10.0.106.255");
    public static final ObisCode DISCONNECTOR_CTR_SCHEDULE_OBIS = ObisCode.fromString("0.0.15.0.1.255");

    private final SlaveMeter emeter;

    public EMeterMessageExecutor(final SlaveMeter emeter) {
        this.emeter = emeter;
    }

    @Override
    protected TimeZone getTimeZone() {
        return this.emeter.getTimeZone();
    }

    public MessageResult executeMessageEntry(final MessageEntry messageEntry) {
        boolean success = false;
        String content = messageEntry.getContent();
        MessageHandler messageHandler = new MessageHandler();
        try {
            importMessage(content, messageHandler);

            boolean connect = messageHandler.getType().equals(RtuMessageConstant.CONNECT_LOAD);
            boolean disconnect = messageHandler.getType().equals(RtuMessageConstant.DISCONNECT_LOAD);
            boolean connectMode = messageHandler.getType().equals(RtuMessageConstant.CONNECT_CONTROL_MODE);

            if (connect) {

                log(Level.INFO, "Handling message: Connect");

                if (!messageHandler.getConnectDate().equals("") && !messageHandler.getConnectDate().equals("0")) {    // use the disconnectControlScheduler

                    Array executionTimeArray = convertUnixToDateTimeArray(messageHandler.getConnectDate());
                    SingleActionSchedule sasConnect = getCosemObjectFactory().getSingleActionSchedule(getCorrectedObisCode(DISCONNECTOR_CTR_SCHEDULE_OBIS));

                    Structure scriptStruct = new Structure();
                    scriptStruct.addDataType(OctetString.fromByteArray(getCorrectedObisCode(DISCONNECTOR_SCRIPT_TABLE_OBIS).getLN()));
                    scriptStruct.addDataType(new Unsigned16(2));     // method '2' is the 'remote_connect' method

                    sasConnect.writeExecutedScript(scriptStruct);
                    sasConnect.writeExecutionTime(executionTimeArray);

                } else {    // immediate connect
                    Disconnector connector = getCosemObjectFactory().getDisconnector(getCorrectedObisCode(DISCONNECTOR_OBIS));
                    connector.remoteReconnect();
                }

                success = true;
            } else if (disconnect) {

                log(Level.INFO, "Handling message: Disconnect");

                if (!messageHandler.getDisconnectDate().equals("") && !messageHandler.getDisconnectDate().equals("0")) { // use the disconnectControlScheduler

                    Array executionTimeArray = convertUnixToDateTimeArray(messageHandler.getDisconnectDate());
                    SingleActionSchedule sasDisconnect = getCosemObjectFactory().getSingleActionSchedule(getCorrectedObisCode(DISCONNECTOR_CTR_SCHEDULE_OBIS));

                    Structure scriptStruct = new Structure();
                    scriptStruct.addDataType(OctetString.fromByteArray(getCorrectedObisCode(DISCONNECTOR_SCRIPT_TABLE_OBIS).getLN()));
                    scriptStruct.addDataType(new Unsigned16(1));    // method '1' is the 'remote_disconnect' method

                    sasDisconnect.writeExecutedScript(scriptStruct);
                    sasDisconnect.writeExecutionTime(executionTimeArray);

                } else {     // immediate disconnect
                    Disconnector disconnector = getCosemObjectFactory().getDisconnector(getCorrectedObisCode(DISCONNECTOR_OBIS));
                    disconnector.remoteDisconnect();
                }

                success = true;
            } else if (connectMode) {

                log(Level.INFO, "Handling message: ConnectControl mode");
                String mode = messageHandler.getConnectControlMode();

                if (mode != null) {
                    try {
                        int modeInt = Integer.parseInt(mode);

                        if ((modeInt >= 0) && (modeInt <= 6)) {
                            Disconnector connectorMode = getCosemObjectFactory().getDisconnector(getCorrectedObisCode(DISCONNECTOR_OBIS));
                            connectorMode.writeControlMode(new TypeEnum(modeInt));

                        } else {
                            throw new IOException("Mode is not a valid entry for message, value must be between 0 and 6");
                        }

                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        throw new IOException("Mode is not a valid entry for message");
                    }
                } else {
                    // should never get to the else, can't leave message empty
                    throw new IOException("Message can not be empty");
                }

                success = true;
            } else {
                success = false;
            }

        } catch (BusinessException e) {
            log(Level.INFO, "Messagehas failed. " + e.getMessage());
        } catch (ConnectionException e) {
            log(Level.INFO, "Messagehas failed. " + e.getMessage());
        } catch (IOException e) {
            log(Level.INFO, "Messagehas failed. " + e.getMessage());
        }
        if (success) {
            log(Level.INFO, "Message has finished.");
            return MessageResult.createSuccess(messageEntry);
        } else {
            return MessageResult.createFailed(messageEntry);
        }
    }

    private void log(Level level, String msg) {
        getLogger().log(level, msg);
    }

    private Logger getLogger() {
        return this.emeter.getLogger();
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return this.emeter.getCosemObjectFactory();
    }

    private DLMSMeterConfig getMeterConfig() {
        return this.emeter.getMeterConfig();
    }

    /**
     * Get the corrected obiscode to match the physical address in the B-field
     *
     * @param obisCode
     * @return
     */
    private ObisCode getCorrectedObisCode(ObisCode obisCode) {
        return ProtocolTools.setObisCodeField(obisCode, 1, (byte) emeter.getPhysicalAddress());
    }
}
