package com.energyict.genericprotocolimpl.webrtuz3.messagehandling;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.genericprotocolimpl.common.CommonUtils;
import com.energyict.genericprotocolimpl.common.GenericMessageExecutor;
import com.energyict.genericprotocolimpl.common.messages.MessageHandler;
import com.energyict.genericprotocolimpl.common.messages.RtuMessageConstant;
import com.energyict.genericprotocolimpl.webrtuz3.EMeter;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.sql.SQLException;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author gna
 * @beginChanges GNA |15042009| The dataField in the TestMessage has a '0x' prefix for better visualization.
 * GNA |04062009| Activate now is not a message value of the FirmwareUpgrade anymore, functionality is changes as it works with the disconnector
 * if the activation date isn't filled in, then the activation takes place immediately
 * @endChanges
 */
public class EMeterMessageExecutor extends GenericMessageExecutor {

    public static final ObisCode DISCONNECTOR_OBIS = ObisCode.fromString("0.0.96.3.10.255");

    private EMeter eMeter;

    public EMeterMessageExecutor(EMeter webRTU) {
        this.eMeter = webRTU;
    }

    private EMeter getEmeter() {
        return this.eMeter;
    }

    public void doMessage(RtuMessage rtuMessage) throws BusinessException, SQLException {
        boolean success = false;
        String content = rtuMessage.getContents();
        MessageHandler messageHandler = new MessageHandler();
        try {
            importMessage(content, messageHandler);

            boolean connect = messageHandler.getType().equals(RtuMessageConstant.CONNECT_LOAD);
            boolean disconnect = messageHandler.getType().equals(RtuMessageConstant.DISCONNECT_LOAD);
            boolean connectMode = messageHandler.getType().equals(RtuMessageConstant.CONNECT_CONTROL_MODE);

            if (connect) {

                log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Connect");

                if (!messageHandler.getConnectDate().equals("")) {    // use the disconnectControlScheduler

                    Array executionTimeArray = convertUnixToDateTimeArray(messageHandler.getConnectDate());
                    SingleActionSchedule sasConnect = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getDisconnectControlSchedule().getObisCode());

                    ScriptTable disconnectorScriptTable = getCosemObjectFactory().getScriptTable(getMeterConfig().getDisconnectorScriptTable().getObisCode());
                    byte[] scriptLogicalName = disconnectorScriptTable.getObjectReference().getLn();
                    Structure scriptStruct = new Structure();
                    scriptStruct.addDataType(new OctetString(scriptLogicalName));
                    scriptStruct.addDataType(new Unsigned16(2));     // method '2' is the 'remote_connect' method

                    sasConnect.writeExecutedScript(scriptStruct);
                    sasConnect.writeExecutionTime(executionTimeArray);

                } else {    // immediate connect
                    Disconnector connector = getCosemObjectFactory().getDisconnector(getCorrectedObisCode(DISCONNECTOR_OBIS));
                    connector.remoteReconnect();
                }

                success = true;
            } else if (disconnect) {

                log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": Disconnect");

                if (!messageHandler.getDisconnectDate().equals("")) { // use the disconnectControlScheduler

                    Array executionTimeArray = convertUnixToDateTimeArray(messageHandler.getDisconnectDate());
                    SingleActionSchedule sasDisconnect = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getDisconnectControlSchedule().getObisCode());

                    ScriptTable disconnectorScriptTable = getCosemObjectFactory().getScriptTable(getMeterConfig().getDisconnectorScriptTable().getObisCode());
                    byte[] scriptLogicalName = disconnectorScriptTable.getObjectReference().getLn();
                    Structure scriptStruct = new Structure();
                    scriptStruct.addDataType(new OctetString(scriptLogicalName));
                    scriptStruct.addDataType(new Unsigned16(1));    // method '1' is the 'remote_disconnect' method

                    sasDisconnect.writeExecutedScript(scriptStruct);
                    sasDisconnect.writeExecutionTime(executionTimeArray);

                } else {     // immediate disconnect
                    Disconnector disconnector = getCosemObjectFactory().getDisconnector(getCorrectedObisCode(DISCONNECTOR_OBIS));
                    disconnector.remoteDisconnect();
                }

                success = true;
            } else if (connectMode) {

                log(Level.INFO, "Handling message " + rtuMessage.displayString() + ": ConnectControl mode");
                String mode = messageHandler.getConnectControlMode();

                if (mode != null) {
                    try {
                        int modeInt = Integer.parseInt(mode);

                        if ((modeInt >= 0) && (modeInt <= 6)) {
                            Disconnector connectorMode = getCosemObjectFactory().getDisconnector(getCorrectedObisCode(DISCONNECTOR_OBIS));
                            connectorMode.writeControlMode(new TypeEnum(modeInt));

                        } else {
                            throw new IOException("Mode is not a valid entry for message " + rtuMessage.displayString() + ", value must be between 0 and 6");
                        }

                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        throw new IOException("Mode is not a valid entry for message " + rtuMessage.displayString());
                    }
                } else {
                    // should never get to the else, can't leave message empty
                    throw new IOException("Message " + rtuMessage.displayString() + " can not be empty");
                }

                success = true;
            } else {
                success = false;
            }

        } catch (BusinessException e) {
            e.printStackTrace();
            log(Level.INFO, "Message " + rtuMessage.displayString() + " has failed. " + e.getMessage());
        } catch (ConnectionException e) {
            e.printStackTrace();
            log(Level.INFO, "Message " + rtuMessage.displayString() + " has failed. " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            log(Level.INFO, "Message " + rtuMessage.displayString() + " has failed. " + e.getMessage());
        } finally {
            if (success) {
                rtuMessage.confirm();
                log(Level.INFO, "Message " + rtuMessage.displayString() + " has finished.");
            } else {
                rtuMessage.setFailed();
            }
        }
    }

    private void log(Level level, String msg) {
        getLogger().log(level, msg);
    }

    private Logger getLogger() {
        return getEmeter().getLogger();
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return getEmeter().getCosemObjectFactory();
    }

    /**
     * Getter for the DLMSMeterConfig object
     * @return
     */
    private DLMSMeterConfig getMeterConfig() {
        return getEmeter().getMeterConfig();
    }

    /**
     * @return the current MeteringWarehouse
     */
    private MeteringWarehouse mw() {
        return CommonUtils.mw();
    }

    /**
     * Getter for the timeZone
     * @return
     */
    protected TimeZone getTimeZone() {
        return getEmeter().getTimeZone();
    }

    /**
     * Get the corrected obiscode to match the physical address in the B-field
     *
     * @param obisCode
     * @return
     */
    private ObisCode getCorrectedObisCode(ObisCode obisCode) {
        return ProtocolTools.setObisCodeField(obisCode, 1, (byte) getEmeter().getPhysicalAddress());
    }

}