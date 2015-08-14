package com.energyict.protocolimplv2.nta.dsmr50.elster.am540.messages;

import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.am500.messages.mbus.IDISMBusMessageExecutor;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.messages.PLCConfigurationDeviceMessageExecutor;
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.abstractnta.messages.AbstractMessageExecutor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sva
 * @since 6/01/2015 - 9:58
 */
public class AM540MessageExecutor extends AbstractMessageExecutor {

    private static final ObisCode PLC_G3_TIMEOUT_OBISCODE = ObisCode.fromString("0.0.94.33.10.255");
    public static final ObisCode RELAY_CONTROL_DEFAULT_OBISCODE = ObisCode.fromString("0.0.96.3.10.255");

    private AbstractMessageExecutor dsmr50MessageExecutor;
    private AbstractMessageExecutor mbusMessageExecutor;
    private PLCConfigurationDeviceMessageExecutor plcConfigurationDeviceMessageExecutor;

    public AM540MessageExecutor(AbstractDlmsProtocol protocol) {
        super(protocol);
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = MdcManager.getCollectedDataFactory().createCollectedMessageList(pendingMessages);

        List<OfflineDeviceMessage> dsmr40Messages = new ArrayList<>();
        List<OfflineDeviceMessage> mbusMessages = new ArrayList<>();

        // First try to execute all G3 PLC related messages
        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = null;
            int mBusChannelId = getProtocol().getPhysicalAddressFromSerialNumber(pendingMessage.getDeviceSerialNumber());
            if (mBusChannelId > 0) {
                mbusMessages.add(pendingMessage);
            } else {
                collectedMessage = createCollectedMessage(pendingMessage);
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
                try {
                    boolean messageExecuted = getPLCConfigurationDeviceMessageExecutor().executePendingMessage(pendingMessage, collectedMessage);
                    if (!messageExecuted) { // if it was not a PLC message
                        if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CLOSE_RELAY)) {
                            closeRelay(pendingMessage);
                        } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.OPEN_RELAY)) {
                            openRelay(pendingMessage);
                        } else {
                            collectedMessage = null;
                            dsmr40Messages.add(pendingMessage); // These messages are not specific for AM540, but can be executed by the super (= Dsmr 4.0) messageExecutor
                        }
                    }
                } catch (IOException e) {
                    if (IOExceptionHandler.isUnexpectedResponse(e, getProtocol().getDlmsSession())) {
                        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                        collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                        collectedMessage.setDeviceProtocolInformation(e.getMessage());
                    }   //Else: throw communication exception
                } catch (IndexOutOfBoundsException | NumberFormatException e) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.InCompatible, createMessageFailedIssue(pendingMessage, e));
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                }
            }
            if (collectedMessage != null) {
                result.addCollectedMessage(collectedMessage);
            }
        }

        // Then delegate all other messages to the Dsmr 5.0 message executor
        result.addCollectedMessages(getDsmr50MessageExecutor().executePendingMessages(dsmr40Messages));

        // Then delegate all MBus related messages to the Mbus message executor
        result.addCollectedMessages(getMbusMessageExecutor().executePendingMessages(mbusMessages));

        return result;
    }

    private void openRelay(OfflineDeviceMessage pendingMessage) throws IOException {
        int relayNumber = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        ObisCode obisCode = ProtocolTools.setObisCodeField(RELAY_CONTROL_DEFAULT_OBISCODE, 1, (byte) relayNumber);
        getCosemObjectFactory().getDisconnector(obisCode).remoteDisconnect();
    }

    private void closeRelay(OfflineDeviceMessage pendingMessage) throws IOException {
        int relayNumber = Integer.valueOf(pendingMessage.getDeviceMessageAttributes().get(0).getDeviceMessageAttributeValue());
        ObisCode obisCode = ProtocolTools.setObisCodeField(RELAY_CONTROL_DEFAULT_OBISCODE, 1, (byte) relayNumber);
        getCosemObjectFactory().getDisconnector(obisCode).remoteReconnect();
    }

    private PLCConfigurationDeviceMessageExecutor getPLCConfigurationDeviceMessageExecutor() {
        if (plcConfigurationDeviceMessageExecutor == null) {
            plcConfigurationDeviceMessageExecutor = new PLCConfigurationDeviceMessageExecutor(getProtocol().getDlmsSession(), getProtocol().getOfflineDevice());
        }
        return plcConfigurationDeviceMessageExecutor;
    }

    public AbstractMessageExecutor getDsmr50MessageExecutor() {
        if (dsmr50MessageExecutor == null) {
            dsmr50MessageExecutor = new Dsmr50MessageExecutor(getProtocol());
        }
        return dsmr50MessageExecutor;
    }

    public AbstractMessageExecutor getMbusMessageExecutor() {
        if (mbusMessageExecutor == null) {
            mbusMessageExecutor = new IDISMBusMessageExecutor(getProtocol());
        }
        return mbusMessageExecutor;
    }
}