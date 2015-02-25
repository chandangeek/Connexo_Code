package com.energyict.protocolimplv2.nta.dsmr50.elster.am540.messages;

import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.cosem.CosemObjectFactory;
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
import com.energyict.protocolimplv2.messages.ContactorDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
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
                    if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetTMRTTL)) {
                        setTMRTTL(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMaxFrameRetries)) {
                        setMaxFrameRetries(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetNeighbourTableEntryTTL)) {
                        setNeighbourTableEntryTTL(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetHighPriorityWindowSize)) {
                        setHighPriorityWindowSize(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetCSMAFairnessLimit)) {
                        setCSMAFairnessLimit(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetBeaconRandomizationWindowLength)) {
                        setBeaconRandomizationWindowLength(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMacA)) {
                        setMacA(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMacK)) {
                        setMacK(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMinimumCWAttempts)) {
                        setMinimumCWAttempts(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMaxBe)) {
                        setMaxBe(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMaxCSMABackOff)) {
                        setMaxCSMABackOff(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMinBe)) {
                        setMinBe(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMaxNumberOfHopsAttributeName)) {
                        setMaxNumberOfHops(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetWeakLQIValueAttributeName)) {
                        setWeakLQIValue(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetSecurityLevel)) {
                        setSecurityLevelpendingMessage(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetRoutingConfiguration)) {
                        setRoutingConfiguration(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetBroadCastLogTableEntryTTLAttributeName)) {
                        setBroadCastLogTableEntryTTL(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMaxJoinWaitTime)) {
                        setMaxJoinWaitTime(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetPathDiscoveryTime)) {
                        setPathDiscoveryTime(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMetricType)) {
                        setMetricType(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetCoordShortAddress)) {
                        setCoordShortAddress(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetDisableDefaultRouting)) {
                        setDisableDefaultRouting(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetDeviceType)) {
                        setDeviceType(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.WritePlcG3Timeout)) {
                        writePlcG3Timeout(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.CLOSE_RELAY)) {
                        closeRelay(pendingMessage);
                    } else if (pendingMessage.getSpecification().equals(ContactorDeviceMessage.OPEN_RELAY)) {
                        openRelay(pendingMessage);
                    } else {
                        collectedMessage = null;
                        dsmr40Messages.add(pendingMessage); // These messages are not specific for AM540, but can be executed by the super (= Dsmr 4.0) messageExecutor
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

    private void setTMRTTL(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeTMRTTL(getIntegerAttribute(pendingMessage));
    }

    private void setMaxFrameRetries(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMaxFrameRetries(getIntegerAttribute(pendingMessage));
    }

    private void setNeighbourTableEntryTTL(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeNeighbourTableEntryTTL(getIntegerAttribute(pendingMessage));
    }

    private void setHighPriorityWindowSize(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeHighPriorityWindowSize(getIntegerAttribute(pendingMessage));
    }

    private void setCSMAFairnessLimit(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeCSMAFairnessLimit(getIntegerAttribute(pendingMessage));
    }

    private void setBeaconRandomizationWindowLength(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeBeaconRandomizationWindowLength(getIntegerAttribute(pendingMessage));
    }

    private void setMacA(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMacA(getIntegerAttribute(pendingMessage));
    }

    private void setMacK(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMacK(getIntegerAttribute(pendingMessage));
    }

    private void setMinimumCWAttempts(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMinCWAttempts(getIntegerAttribute(pendingMessage));
    }

    private void setMaxBe(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMaxBE(getIntegerAttribute(pendingMessage));
    }

    private void setMaxCSMABackOff(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMaxCSMABackOff(getIntegerAttribute(pendingMessage));
    }

    private void setMinBe(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMinBE(getIntegerAttribute(pendingMessage));
    }

    private void setMaxNumberOfHops(OfflineDeviceMessage pendingMessage) throws IOException {
        int value = getIntegerAttribute(pendingMessage);
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeMaxHops(value);
    }

    private void setWeakLQIValue(OfflineDeviceMessage pendingMessage) throws IOException {
        int value = getIntegerAttribute(pendingMessage);
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeWeakLqiValue(value);
    }

    private void setSecurityLevelpendingMessage(OfflineDeviceMessage pendingMessage) throws IOException {
        int value = getIntegerAttribute(pendingMessage);
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeSecurityLevel(value);
    }

    private void setRoutingConfiguration(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();

        int adp_net_traversal_time = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_net_traversal_time).getDeviceMessageAttributeValue());
        int adp_routing_table_entry_TTL = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_routing_table_entry_TTL).getDeviceMessageAttributeValue());
        int adp_Kr = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Kr).getDeviceMessageAttributeValue());
        int adp_Km = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Km).getDeviceMessageAttributeValue());
        int adp_Kc = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Kc).getDeviceMessageAttributeValue());
        int adp_Kq = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Kq).getDeviceMessageAttributeValue());
        int adp_Kh = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Kh).getDeviceMessageAttributeValue());
        int adp_Krt = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Krt).getDeviceMessageAttributeValue());
        int adp_RREQ_retries = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_RREQ_retries).getDeviceMessageAttributeValue());
        int adp_RREQ_RERR_wait = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_RREQ_RERR_wait).getDeviceMessageAttributeValue());
        int adp_Blacklist_table_entry_TTL = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Blacklist_table_entry_TTL).getDeviceMessageAttributeValue());
        boolean adp_unicast_RREQ_gen_enable = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_unicast_RREQ_gen_enable).getDeviceMessageAttributeValue());
        boolean adp_RLC_enabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_RLC_enabled).getDeviceMessageAttributeValue());
        int adp_add_rev_link_cost = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_add_rev_link_cost).getDeviceMessageAttributeValue());

        cof.getSixLowPanAdaptationLayerSetup().writeRoutingConfiguration(
                adp_net_traversal_time,
                adp_routing_table_entry_TTL,
                adp_Kr,
                adp_Km,
                adp_Kc,
                adp_Kq,
                adp_Kh,
                adp_Krt,
                adp_RREQ_retries,
                adp_RREQ_RERR_wait,
                adp_Blacklist_table_entry_TTL,
                adp_unicast_RREQ_gen_enable,
                adp_RLC_enabled,
                adp_add_rev_link_cost
        );
    }

    private void setBroadCastLogTableEntryTTL(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeBroadcastLogTableTTL(getIntegerAttribute(pendingMessage));
    }

    private void setMaxJoinWaitTime(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeMaxJoinWaitTime(getIntegerAttribute(pendingMessage));
    }

    private void setPathDiscoveryTime(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writePathDiscoveryTime(getIntegerAttribute(pendingMessage));
    }

    private void setMetricType(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeMetricType(getIntegerAttribute(pendingMessage));
    }

    private void setCoordShortAddress(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeCoordShortAddress(getIntegerAttribute(pendingMessage));
    }

    private void setDisableDefaultRouting(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeDisableDefaultRouting(getBooleanAttribute(pendingMessage));
    }

    private void setDeviceType(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeDeviceType(getIntegerAttribute(pendingMessage));
    }

    private void writePlcG3Timeout(OfflineDeviceMessage pendingMessage) throws IOException {
        int timeoutInMinutes = getIntegerAttribute(pendingMessage);
        final CosemObjectFactory cof = getProtocol().getDlmsSession().getCosemObjectFactory();
        cof.getData(PLC_G3_TIMEOUT_OBISCODE).setValueAttr(new Unsigned16(timeoutInMinutes));
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