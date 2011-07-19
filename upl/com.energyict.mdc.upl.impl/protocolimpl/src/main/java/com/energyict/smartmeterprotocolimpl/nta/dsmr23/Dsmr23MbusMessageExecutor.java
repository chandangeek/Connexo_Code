package com.energyict.smartmeterprotocolimpl.nta.dsmr23;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.Environment;
import com.energyict.dlms.DLMSMeterConfig;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.genericprotocolimpl.common.GenericMessageExecutor;
import com.energyict.genericprotocolimpl.common.messages.MessageHandler;
import com.energyict.genericprotocolimpl.nta.messagehandling.NTAMessageHandler;
import com.energyict.mdw.core.*;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.protocol.MessageEntry;
import com.energyict.protocol.MessageResult;
import com.energyict.protocolimpl.dlms.common.DlmsSession;
import com.energyict.protocolimpl.messages.RtuMessageConstant;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;

import java.io.IOException;
import java.sql.SQLException;
import java.util.TimeZone;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 18-jul-2011
 * Time: 8:38:27
 */
public class Dsmr23MbusMessageExecutor extends GenericMessageExecutor {

    private final AbstractSmartNtaProtocol protocol;
    private final DlmsSession dlmsSession;

    private boolean success;

    public Dsmr23MbusMessageExecutor(final AbstractSmartNtaProtocol protocol) {
        this.protocol = protocol;
        this.dlmsSession = this.protocol.getDlmsSession();
    }

    public MessageResult executeMessageEntry(MessageEntry msgEntry) {
        String content = msgEntry.getContent();
        MessageHandler messageHandler = new NTAMessageHandler();
        String serialNumber = msgEntry.getSerialNumber();
        success = true;
        try {
            importMessage(content, messageHandler);

            boolean connect = messageHandler.getType().equals(RtuMessageConstant.CONNECT_LOAD);
            boolean disconnect = messageHandler.getType().equals(RtuMessageConstant.DISCONNECT_LOAD);
            boolean connectMode = messageHandler.getType().equals(RtuMessageConstant.CONNECT_CONTROL_MODE);
            boolean decommission = messageHandler.getType().equals(RtuMessageConstant.MBUS_DECOMMISSION);
            boolean mbusEncryption = messageHandler.getType().equals(RtuMessageConstant.MBUS_ENCRYPTION_KEYS);
            boolean mbusCorrected = messageHandler.getType().equals(RtuMessageConstant.MBUS_CORRECTED_VALUES);
            boolean mbusUnCorrected = messageHandler.getType().equals(RtuMessageConstant.MBUS_UNCORRECTED_VALUES);

            if (connect) {
                doConnectMessage(messageHandler, serialNumber);
            } else if (disconnect) {
                doDisconnectMessage(messageHandler, serialNumber);
            } else if (connectMode) {
                setConnectMode(messageHandler, serialNumber);
            } else if (decommission) {
                doDecommission(messageHandler, serialNumber);
            } else if (mbusEncryption) {
                setMbusEncrytpionKeys(messageHandler, serialNumber);
            } else if (mbusCorrected) {
                setMbusCorrected(messageHandler, serialNumber);
            } else if (mbusUnCorrected) {
                setMbusUncorrected(messageHandler, serialNumber);
            } else {
                success = false;
            }
        } catch (BusinessException e) {
            log(Level.SEVERE, "Message failed : " + e.getMessage());
            success = false;
        } catch (IOException e) {
            log(Level.SEVERE, "Message failed : " + e.getMessage());
            success = false;
        } finally {
            if (success) {
                log(Level.INFO, "Message has finished.");
                return MessageResult.createSuccess(msgEntry);
            } else {
                return MessageResult.createFailed(msgEntry);
            }
        }
    }

    private void setMbusUncorrected(final MessageHandler messageHandler, final String serialNumber) throws IOException {
        log(Level.INFO, "Handling MbusMessage Set loadprofile to unCorrected values");
        MBusClient mc = getCosemObjectFactory().getMbusClient(getMeterConfig().getMbusClient(getMbusAddress(serialNumber)).getObisCode(), MbusClientAttributes.VERSION9);
        Array capDef = new Array();
        Structure struct = new Structure();
        OctetString dib = new OctetString(new byte[]{(byte) 0x0C});
        struct.addDataType(dib);
        OctetString vib = new OctetString(new byte[]{(byte) 0x93, (byte) 0x3A});
        struct.addDataType(vib);
        capDef.addDataType(struct);
        mc.writeCaptureDefinition(capDef);
    }

    private void setMbusCorrected(final MessageHandler messageHandler, final String serialNumber) throws IOException {
        log(Level.INFO, "Handling MbusMessage  Set loadprofile to corrected values");
        MBusClient mc = getCosemObjectFactory().getMbusClient(getMeterConfig().getMbusClient(getMbusAddress(serialNumber)).getObisCode(), MbusClientAttributes.VERSION9);
        Array capDef = new Array();
        Structure struct = new Structure();
        OctetString dib = new OctetString(new byte[]{0x0C});
        struct.addDataType(dib);
        OctetString vib = new OctetString(new byte[]{0x13});
        struct.addDataType(vib);
        capDef.addDataType(struct);
        mc.writeCaptureDefinition(capDef);
    }

    private void setMbusEncrytpionKeys(final MessageHandler messageHandler, final String serialNumber) throws IOException {
        log(Level.INFO, "Handling MbusMessage Set encryption keys");

        String openKey = messageHandler.getOpenKey();
        String transferKey = messageHandler.getTransferKey();

        MBusClient mbusClient = getCosemObjectFactory().getMbusClient(getMeterConfig().getMbusClient(getMbusAddress(serialNumber)).getObisCode(), MbusClientAttributes.VERSION9);

        if (openKey == null) {
            mbusClient.setEncryptionKey("");
        } else if (transferKey != null) {
            mbusClient.setTransportKey(convertStringToByte(transferKey));
            mbusClient.setEncryptionKey(convertStringToByte(openKey));
        } else {
            throw new IOException("Transfer key may not be empty when setting the encryption keys.");
        }
    }

    private void doDecommission(final MessageHandler messageHandler, final String serialNumber) throws IOException, BusinessException, SQLException {
        log(Level.INFO, "Handling MbusMessage Decommission MBus device");

        MBusClient mbusClient = getCosemObjectFactory().getMbusClient(getMeterConfig().getMbusClient(getMbusAddress(serialNumber)).getObisCode(), MbusClientAttributes.VERSION9);
        mbusClient.deinstallSlave();

        //Need to clear the gateWay
        //TODO this is not fully compliant with the HTTP comserver ...
        Rtu mbus = getRtuFromDatabaseBySerialNumber(serialNumber);
        if (mbus != null) {
            RtuShadow shadow = mbus.getShadow();
            shadow.setGatewayId(0);
            mbus.update(shadow);
        }
    }

    private void setConnectMode(final MessageHandler messageHandler, final String serialNumber) throws IOException {
        log(Level.INFO, "Handling MbusMessage ConnectControl mode");
        String mode = messageHandler.getConnectControlMode();

        if (mode != null) {
            try {
                int modeInt = Integer.parseInt(mode);

                if ((modeInt >= 0) && (modeInt <= 6)) {
                    Disconnector connectorMode = getCosemObjectFactory().getDisconnector(getMeterConfig().getMbusDisconnectControl(getMbusAddress(serialNumber)).getObisCode());
                    connectorMode.writeControlMode(new TypeEnum(modeInt));

                } else {
                    throw new IOException("Mode is not a valid entry for message, value must be between 0 and 6");
                }

            } catch (NumberFormatException e) {
                throw new IOException("Mode is not a valid entry for message.");
            }
        } else {
            // should never get to the else, can't leave message empty
            throw new IOException("Message can not be empty");
        }
    }

    private void doDisconnectMessage(final MessageHandler messageHandler, final String serialNumber) throws IOException {
        log(Level.INFO, "Handling MbusMessage Disconnect");

        if (!messageHandler.getDisconnectDate().equals("")) {    // use the disconnectControlScheduler

            Array executionTimeArray = convertUnixToDateTimeArray(messageHandler.getDisconnectDate());
            SingleActionSchedule sasDisconnect = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getMbusDisconnectControlSchedule(getMbusAddress(serialNumber)).getObisCode());

            ScriptTable disconnectorScriptTable = getCosemObjectFactory().getScriptTable(getMeterConfig().getMbusDisconnectorScriptTable(getMbusAddress(serialNumber)).getObisCode());
            byte[] scriptLogicalName = disconnectorScriptTable.getObjectReference().getLn();
            Structure scriptStruct = new Structure();
            scriptStruct.addDataType(new OctetString(scriptLogicalName));
            scriptStruct.addDataType(new Unsigned16(1));    // method '1' is the 'remote_disconnect' method

            sasDisconnect.writeExecutedScript(scriptStruct);
            sasDisconnect.writeExecutionTime(executionTimeArray);

        } else { // immediate disconnect
            Disconnector connector = getCosemObjectFactory().getDisconnector(getMeterConfig().getMbusDisconnectControl(getMbusAddress(serialNumber)).getObisCode());
            connector.remoteDisconnect();
        }
    }

    private void doConnectMessage(MessageHandler messageHandler, String serialNumber) throws IOException {
        log(Level.INFO, "Handling MbusMessage Connect");

        if (!messageHandler.getConnectDate().equals("")) {    // use the disconnectControlScheduler

            Array executionTimeArray = convertUnixToDateTimeArray(messageHandler.getConnectDate());
            SingleActionSchedule sasConnect = getCosemObjectFactory().getSingleActionSchedule(getMeterConfig().getMbusDisconnectControlSchedule(getMbusAddress(serialNumber)).getObisCode());

            ScriptTable disconnectorScriptTable = getCosemObjectFactory().getScriptTable(getMeterConfig().getMbusDisconnectorScriptTable(getMbusAddress(serialNumber)).getObisCode());
            byte[] scriptLogicalName = disconnectorScriptTable.getObjectReference().getLn();
            Structure scriptStruct = new Structure();
            scriptStruct.addDataType(new OctetString(scriptLogicalName));
            scriptStruct.addDataType(new Unsigned16(2));     // method '2' is the 'remote_connect' method

            sasConnect.writeExecutedScript(scriptStruct);
            sasConnect.writeExecutionTime(executionTimeArray);

        } else { // immediate connect
            Disconnector connector = getCosemObjectFactory().getDisconnector(getMeterConfig().getMbusDisconnectControl(getMbusAddress(serialNumber)).getObisCode());
            connector.remoteReconnect();
        }
    }

    private byte[] convertStringToByte(String string) throws IOException {
        try {
            byte[] b = new byte[string.length() / 2];
            int offset = 0;
            for (int i = 0; i < b.length; i++) {
                b[i] = (byte) Integer.parseInt(string.substring(offset, offset += 2), 16);
            }
            return b;
        } catch (NumberFormatException e) {
            throw new IOException("String " + string + " can not be formatted to byteArray");
        }
    }

    private DLMSMeterConfig getMeterConfig() {
        return this.dlmsSession.getMeterConfig();
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return this.dlmsSession.getCosemObjectFactory();
    }

    @Override
    public void doMessage(final RtuMessage rtuMessage) throws BusinessException, SQLException {
        //nothing to do
    }

    @Override
    protected TimeZone getTimeZone() {
        return this.dlmsSession.getTimeZone();
    }

    private void log(final Level level, final String msg) {
        this.dlmsSession.getLogger().log(level, msg);
    }

    private int getMbusAddress(String serialNumber){
        return this.protocol.getPhysicalAddressFromSerialNumber(serialNumber) - 1;
    }

    /*****************************************************************************/
    /* These methods require database access ...  TODO we should do this using the framework ...
    /*****************************************************************************/

    /**
     * Short notation for MeteringWarehouse.getCurrent()
     */
    public MeteringWarehouse mw() {
        MeteringWarehouse result = MeteringWarehouse.getCurrent();
        if (result == null) {
            return new MeteringWarehouseFactory().getBatch(false);
        } else {
            return result;
        }
    }

    private Rtu getRtuFromDatabaseBySerialNumber(String serialNumber) {
        return mw().getRtuFactory().findBySerialNumber(serialNumber).get(0);
    }
}
