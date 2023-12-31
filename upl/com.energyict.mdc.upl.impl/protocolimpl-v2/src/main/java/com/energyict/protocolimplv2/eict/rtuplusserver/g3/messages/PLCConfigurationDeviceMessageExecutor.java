package com.energyict.protocolimplv2.eict.rtuplusserver.g3.messages;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.G3NetworkManagement;
import com.energyict.dlms.cosem.GenericPlcIBSetup;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.upl.NotInObjectListException;
import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.eict.rtuplusserver.g3.properties.G3GatewayProperties;
import com.energyict.mdc.identifiers.DeviceIdentifierById;
import com.energyict.mdc.identifiers.DeviceMessageIdentifierById;
import com.energyict.mdc.identifiers.RegisterDataIdentifierByObisCodeAndDevice;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.PLCConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

import java.io.IOException;
import java.util.*;

/**
 * Helper class that groups all logic related to the execution of the (standard) PLC messages. <br/>
 * This class can be re-used by multiple protocols, who all need/use the same PLC messages.
 *
 * @author sva
 * @since 12/08/2015 - 9:50
 */
public class PLCConfigurationDeviceMessageExecutor {

    private static final int MAX_REGISTER_TEXT_SIZE = 3800;  //The register text field is 4000 chars maximum
    private static final ObisCode PLC_G3_TIMEOUT_OBISCODE = ObisCode.fromString("0.0.94.33.10.255");
    private static final ObisCode PLC_G3_KEEP_ALIVE_OBISCODE = ObisCode.fromString("0.0.94.33.11.255");
    private static final int HIGH_LQI_ID = 35;
    private static final int LOW_LQI_ID = 36;

    protected final DlmsSession session;
    private final OfflineDevice offlineDevice;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final boolean readOldObisCode;

    public PLCConfigurationDeviceMessageExecutor(DlmsSession session, OfflineDevice offlineDevice, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this(session, offlineDevice, false, collectedDataFactory, issueFactory);
    }

    public PLCConfigurationDeviceMessageExecutor(DlmsSession session, OfflineDevice offlineDevice, boolean readOldObisCode, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        this.session = session;
        this.offlineDevice = offlineDevice;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.readOldObisCode = readOldObisCode;
    }

    /**
     * Return the (possibly updated) collectedMessage, or null if the message is not supported here
     */
    public CollectedMessage executePendingMessage(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMaxNumberOfHopsAttributeName)) {
            setMaxNumberOfHops(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetWeakLQIValueAttributeName)) {
            setWeakLQIValue(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetSecurityLevel)) {
            setSecurityLevelpendingMessage(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetRoutingConfiguration)) {
            setRoutingConfiguration(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetBroadCastLogTableEntryTTLAttributeName) ||
                pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetBroadCastLogTableEntryTTLVersion1)) {
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
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.ResetPlcOfdmMacCounters)) {
            resetPlcOfdmMacCounters();
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetPanId)) {
            setPanId(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetToneMaskAttributeName)) {
            setToneMask(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetTMRTTL)) {
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
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.PathRequest)) {
            collectedMessage = doPathRequest(pendingMessage, collectedMessage);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.PathRequestWithTimeout)) {
            collectedMessage = doPathRequest(pendingMessage, collectedMessage);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetAutomaticRouteManagement)) {
            setAutomaticRouteManagement(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.EnableSNR)) {
            enableSNR(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetSNRPacketInterval)) {
            setSNRPacketInterval(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetSNRQuietTime)) {
            setSNRQuietTime(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetSNRPayload)) {
            setSNRPayload(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.EnableKeepAlive)) {
            enableKeepAlive(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetKeepAliveScheduleInterval)) {
            setKeepAliveScheduleInterval(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetKeepAliveBucketSize)) {
            setKeepAliveBucketSize(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMinInactiveMeterTime)) {
            setMinInactiveMeterTime(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetMaxInactiveMeterTime)) {
            setMaxInactiveMeterTime(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetKeepAliveRetries)) {
            setKeepAliveFailCount(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetKeepAliveTimeout)) {
            setKeepAliveDelayBetweenPings(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.EnableG3PLCInterface)) {
            enableG3PLCInterface(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.WritePlcG3Timeout)) {
            writePlcG3Timeout(pendingMessage);
        } else if (pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.ConfigurePLcG3KeepAlive)) {
            configurePlcG3KeepAlive(pendingMessage);
        } else if(pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetHighLowLQI)){
            setHighLowLQI(pendingMessage);
        }else if(pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetAdpLBPAssociationSetup_7_Parameters)){
            setAdpLBPAssociationSetup7Params(pendingMessage);
        }else if(pendingMessage.getSpecification().equals(PLCConfigurationDeviceMessage.SetAdpLBPAssociationSetup_5_Parameters)){
            setAdpLBPAssociationSetup5Params(pendingMessage);
        } else {   //Unsupported message
            return null;
        }
        return collectedMessage;
    }

    protected CollectedMessage doPathRequest(OfflineDeviceMessage pendingMessage, CollectedMessage collectedMessage) throws IOException {
        PathRequestFeedback pathRequestFeedback = pathRequest(pendingMessage);
        collectedMessage = createCollectedMessageWithRegisterData(pendingMessage, pathRequestFeedback.getRegisters());
        collectedMessage.setDeviceProtocolInformation(pathRequestFeedback.getFeedback());
        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);
        return collectedMessage;
    }

    private void setMaxNumberOfHops(OfflineDeviceMessage pendingMessage) throws IOException {
        int value = getSingleIntegerAttribute(pendingMessage);
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeMaxHops(value);
    }

    private void setWeakLQIValue(OfflineDeviceMessage pendingMessage) throws IOException {
        int value = getSingleIntegerAttribute(pendingMessage);
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeWeakLqiValue(value);
    }

    private void setSecurityLevelpendingMessage(OfflineDeviceMessage pendingMessage) throws IOException {
        int value = getSingleIntegerAttribute(pendingMessage);
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeSecurityLevel(value);
    }

    private void setHighLowLQI(OfflineDeviceMessage pendingMessage) throws IOException {
        int highLQI = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.highLQIValueAttributeName).getValue());
        int lowLQI = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.lowLQIValueAttributeName).getValue());

        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        GenericPlcIBSetup genericPlcIBSetup;
        if(readOldObisCode){
            genericPlcIBSetup = cof.getGenericPlcIBSetup();
        }else {
            genericPlcIBSetup = cof.getGenericPlcIBSetup(GenericPlcIBSetup.getNewObisCode());
        }
        Array values = new Array();
        values.addDataType(getLQIStructure(HIGH_LQI_ID, highLQI));
        values.addDataType(getLQIStructure(LOW_LQI_ID, lowLQI));
        genericPlcIBSetup.writeRawIBValues(values);
    }

    private void setAdpLBPAssociationSetup7Params(OfflineDeviceMessage pendingMessage) throws IOException {
        ObisCode adpLBPAssociationSetupObisCode = ObisCode.fromString("0.0.94.33.14.255");
        CosemObjectFactory cosemObjectFactory = this.session.getCosemObjectFactory();
        Structure structure = getAdpLBPAssociationsSturcture(7, pendingMessage);
        cosemObjectFactory.writeObject(adpLBPAssociationSetupObisCode, 1, 2, structure.getBEREncodedByteArray());
    }

    private void setAdpLBPAssociationSetup5Params(OfflineDeviceMessage pendingMessage) throws IOException {
        ObisCode adpLBPAssociationSetupObisCode = ObisCode.fromString("0.0.94.33.14.255");
        CosemObjectFactory cosemObjectFactory = this.session.getCosemObjectFactory();
        Structure structure = getAdpLBPAssociationsSturcture(5, pendingMessage);
        cosemObjectFactory.writeObject(adpLBPAssociationSetupObisCode, 1, 2, structure.getBEREncodedByteArray());
    }

    private Structure getAdpLBPAssociationsSturcture(int paramNumber, OfflineDeviceMessage pendingMessage){
        Structure structure = new Structure();
        int associationMaxRetry = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.Association_max_retry).getValue());
        int associationRandWaitTimeStep = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.Association_rand_wait_time_step).getValue());
        boolean associationAltPAN = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.Association_alt_PAN).getValue());
        int joinLQIThreshold = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.Join_LQI_threshold).getValue());
        int activeScanDuration = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.Active_scan_duration).getValue());
        structure.addDataType(new Unsigned8(associationMaxRetry));
        structure.addDataType(new Unsigned8(associationRandWaitTimeStep));
        structure.addDataType(new BooleanObject(associationAltPAN));
        structure.addDataType(new Unsigned8(joinLQIThreshold));
        structure.addDataType(new Unsigned8(activeScanDuration));

        if(paramNumber > 5){
            int highLQI = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.highLQIValueAttributeName).getValue());
            int lowLQI = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.lowLQIValueAttributeName).getValue());
            structure.addDataType(new Unsigned8(lowLQI));
            structure.addDataType(new Unsigned8(highLQI));
        }
        return structure;
    }

    private Structure getLQIStructure(int id, int value){
        Structure structure = new Structure();
        structure.addDataType(new Unsigned32(id));
        structure.addDataType(new OctetString(ProtocolTools.getBytesFromHexString(ProtocolTools.getHexStringFromInt(value))));
        return structure;
    }

    private void setRoutingConfiguration(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();

        int adp_net_traversal_time = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_net_traversal_time).getValue());
        int adp_routing_table_entry_TTL = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_routing_table_entry_TTL).getValue());
        int adp_routing_tuple_TTL = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_routing_tuple_TTL).getValue());
        int adp_Kr = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Kr).getValue());
        int adp_Km = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Km).getValue());
        int adp_Kc = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Kc).getValue());
        int adp_Kq = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Kq).getValue());
        int adp_Kh = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Kh).getValue());
        int adp_Krt = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Krt).getValue());
        int adp_RREQ_retries = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_RREQ_retries).getValue());
        int adp_RREQ_RERR_wait = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_RREQ_RERR_wait).getValue());
        int adp_Blacklist_table_entry_TTL = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_Blacklist_table_entry_TTL).getValue());
        boolean adp_unicast_RREQ_gen_enable = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_unicast_RREQ_gen_enable).getValue());
        boolean adp_RLC_enabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_RLC_enabled).getValue());
        int adp_add_rev_link_cost = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.adp_add_rev_link_cost).getValue());

        cof.getSixLowPanAdaptationLayerSetup().writeRoutingConfiguration(
                isICVersion0(pendingMessage),
                adp_net_traversal_time,
                adp_routing_table_entry_TTL,
                adp_routing_tuple_TTL,
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

    /**
     * Flag if this is a IC version 0 or greater.
     *  On version 1 adp_routing_tuple_TTL does not exist
     * @param pendingMessage
     */
    protected boolean isICVersion0(OfflineDeviceMessage pendingMessage) {
        return Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.icVersion0).getValue());
    }

    private void setBroadCastLogTableEntryTTL(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeBroadcastLogTableTTL(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMaxJoinWaitTime(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeMaxJoinWaitTime(getSingleIntegerAttribute(pendingMessage));
    }

    private void setPathDiscoveryTime(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writePathDiscoveryTime(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMetricType(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeMetricType(getSingleIntegerAttribute(pendingMessage));
    }

    private void setCoordShortAddress(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeCoordShortAddress(getSingleIntegerAttribute(pendingMessage));
    }

    private void setDisableDefaultRouting(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeDisableDefaultRouting(getSingleBooleanAttribute(pendingMessage));
    }

    private void setDeviceType(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getSixLowPanAdaptationLayerSetup().writeDeviceType(getSingleIntegerAttribute(pendingMessage));
    }

    private void resetPlcOfdmMacCounters() throws IOException {
        this.session.getCosemObjectFactory().getPLCOFDMType2PHYAndMACCounters().reset();
    }

    private void setPanId(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getPLCOFDMType2MACSetup().writePANID(getSingleIntegerAttribute(pendingMessage));
    }

    private void setToneMask(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        boolean[] toneMask = toBooleanArray(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
        cof.getPLCOFDMType2MACSetup().writeToneMask(toneMask);
    }

    /**
     * Converts a list of 1's and 0's into a boolean array, e.g. 10101 = [true, false, true, false, true]
     */
    private boolean[] toBooleanArray(final String value) {
        final String cleanBooleanString = value.toUpperCase().replaceAll("[^0-1]", "");
        boolean[] booleans = new boolean[cleanBooleanString.length()];
        for (int i = 0; i < booleans.length; i++) {
            booleans[i] = cleanBooleanString.charAt(i) == '1';
        }
        return booleans;
    }

    private void setTMRTTL(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeTMRTTL(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMaxFrameRetries(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMaxFrameRetries(getSingleIntegerAttribute(pendingMessage));
    }

    private void setNeighbourTableEntryTTL(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeNeighbourTableEntryTTL(getSingleIntegerAttribute(pendingMessage));
    }

    private void setHighPriorityWindowSize(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeHighPriorityWindowSize(getSingleIntegerAttribute(pendingMessage));
    }

    private void setCSMAFairnessLimit(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeCSMAFairnessLimit(getSingleIntegerAttribute(pendingMessage));
    }

    private void setBeaconRandomizationWindowLength(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeBeaconRandomizationWindowLength(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMacA(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMacA(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMacK(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMacK(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMinimumCWAttempts(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMinCWAttempts(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMaxBe(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMaxBE(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMaxCSMABackOff(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMaxCSMABackOff(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMinBe(OfflineDeviceMessage pendingMessage) throws IOException {
        final CosemObjectFactory cof = this.session.getCosemObjectFactory();
        cof.getPLCOFDMType2MACSetup().writeMinBE(getSingleIntegerAttribute(pendingMessage));

    }

    private PathRequestFeedback pathRequest(OfflineDeviceMessage pendingMessage) throws IOException {
        String macAddressesString = pendingMessage.getDeviceMessageAttributes().get(0).getValue();
        final G3NetworkManagement topologyManagement = getG3NetworkManagement();
        List<String> macAddresses = Arrays.asList(macAddressesString.split(";"));

        StringBuilder pingFailed = new StringBuilder();
        StringBuilder pingSuccess = new StringBuilder();
        StringBuilder pathFailed = new StringBuilder();
        long aarqTimeout = ((G3GatewayProperties) session.getProperties()).getAarqTimeout();
        long normalTimeout = session.getProperties().getTimeout();
        long pingTimeout = (aarqTimeout == 0 ? normalTimeout : aarqTimeout);
        long fullRoundTripTimeout = 30000 + pingTimeout;
        int numberPingFailed = 0;
        int numberPathFailed = 0;
        int success = 0;
        Map<String, String> allPaths = new HashMap<>();   //Remember the full path for every meter

        for (String macAddress : macAddresses) {
            session.getDLMSConnection().setTimeout(fullRoundTripTimeout);     //The ping request can take a long time, increase the timeout of the DLMS connection
            Integer ping;
            try {
                session.getLogger().info("Executing ping request to meter " + macAddress);
                ping = topologyManagement.pingNode(macAddress, (int) (pingTimeout / 1000));
            } catch (DataAccessResultException e) {
                ping = null;
                session.getLogger().warning("Meter " + macAddress + " is not registered to this concentrator! Will not execute path request for this meter.");
            } finally {
                session.getDLMSConnection().setTimeout(normalTimeout);
            }
            if (ping == null) {
                numberPingFailed++;
                logFailedPingRequest(pingFailed, macAddress);
            } else if (ping > 0) {
                logSuccessfulPingRequest(pingSuccess, macAddress, ping);
                session.getLogger().info("Ping request for meter " + macAddress + " was successful (" + ping + " ms).");
                try {
                    session.getLogger().info("Executing path request to meter " + macAddress);
                    session.getDLMSConnection().setTimeout(fullRoundTripTimeout);
                    String fullPath = topologyManagement.requestPath(macAddress);   //If successful, it will be added in the topology of the RTU+Server
                    allPaths.put(macAddress, fullPath);
                    success++;
                    session.getLogger().info("Path request for meter " + macAddress + " was successful.");
                } catch (DataAccessResultException e) {
                    numberPathFailed++;
                    session.getLogger().warning("Path request for meter " + macAddress + " failed.");
                    logFailedPathRequest(pathFailed, macAddress);
                } finally {
                    session.getDLMSConnection().setTimeout(normalTimeout);
                }
            } else if (ping <= 0) {
                numberPingFailed++;
                session.getLogger().info("Ping failed for meter " + macAddress + ". Will not execute path request for this meter.");
                logFailedPingRequest(pingFailed, macAddress);
            }
        }

        String allInfo = (pingSuccess.length() == 0 ? "" : (pingSuccess + ". ")) + (pingFailed.length() == 0 ? "" : (pingFailed + ". ")) + (pathFailed.length() == 0 ? "" : (pathFailed + "."));

        List<CollectedRegister> collectedRegisters = convertPathInfoToRegisters(allPaths);
        if (pingFailed.toString().length() == 0 && pathFailed.toString().length() == 0) {
            session.getLogger().info("Message result: ping and path requests were successful for every meter.");
        }

        return new PathRequestFeedback(allInfo, collectedRegisters);
    }

    private List<CollectedRegister> convertPathInfoToRegisters(Map<String, String> allPaths) {
        List<CollectedRegister> result = new ArrayList<>();
        List<String> allDescriptions = new ArrayList<>();
        StringBuilder currentBuilder = createNewBuilder();    //Start with this builder

        for (String macAddress : allPaths.keySet()) {
            //Prevent that the description size goes over 4000 chars.
            if (currentBuilder.length() > MAX_REGISTER_TEXT_SIZE) {
                allDescriptions.add(currentBuilder.toString());      //Add the generated descriptions to the list
                currentBuilder = createNewBuilder();                 //Continue with a new StringBuilder
            }
            currentBuilder.append(allPaths.get(macAddress));
            currentBuilder.append("\n\r");
        }
        allDescriptions.add(currentBuilder.toString());     //Add the last used builder too

        for (int index = 0; index < allDescriptions.size(); index++) {
            ObisCode topologyObisCode = ProtocolTools.setObisCodeField(G3NetworkManagement.getDefaultObisCode(), 1, (byte) (index + 1));
            DeviceIdentifierById deviceIdentifier = new DeviceIdentifierById(offlineDevice.getId());
            RegisterDataIdentifierByObisCodeAndDevice registerDataIdentifier = new RegisterDataIdentifierByObisCodeAndDevice(topologyObisCode, deviceIdentifier);
            CollectedRegister collectedRegister = this.collectedDataFactory.createDefaultCollectedRegister(registerDataIdentifier);
            collectedRegister.setCollectedData(allDescriptions.get(index));
            collectedRegister.setReadTime(new Date());
            result.add(collectedRegister);
        }

        return result;
    }

    private StringBuilder createNewBuilder() {
        StringBuilder sb = new StringBuilder();
        sb.append("Date:MAC_Address:Forth_path:Back_path");
        sb.append("\n\r");
        return sb;
    }

    private void logFailedPingRequest(StringBuilder pingFailed, String macAddress) {
        if (pingFailed.toString().isEmpty()) {
            pingFailed.append("Ping failed for: ");
            pingFailed.append(macAddress);
        } else {
            pingFailed.append(", ");
            pingFailed.append(macAddress);
        }
    }

    private void logSuccessfulPingRequest(StringBuilder pingSuccess, String macAddress, int pingTime) {
        if (pingSuccess.toString().isEmpty()) {
            pingSuccess.append("Ping successful for: ");
            pingSuccess.append(macAddress);
            if (pingTime > 1) {
                pingSuccess.append(" (").append(pingTime).append(" ms)");
            }
        } else {
            pingSuccess.append(", ");
            pingSuccess.append(macAddress);
            if (pingTime > 1) {
                pingSuccess.append(" (").append(pingTime).append(" ms)");
            }
        }
    }

    private void logFailedPathRequest(StringBuilder pathFailed, String macAddress) {
        if (pathFailed.toString().isEmpty()) {
            pathFailed.append("Path request failed for: ");
            pathFailed.append(macAddress);
        } else {
            pathFailed.append(", ");
            pathFailed.append(macAddress);
        }
    }

    private void setAutomaticRouteManagement(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean pingEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.pingEnabled).getValue());
        boolean routeRequestEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.routeRequestEnabled).getValue());
        boolean pathRequestEnabled = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.pathRequestEnabled).getValue());

        getG3NetworkManagement().setAutomaticRouteManagement(routeRequestEnabled, pingEnabled, pathRequestEnabled);
    }

    private void enableSNR(OfflineDeviceMessage pendingMessage) throws IOException {
        getG3NetworkManagement().enableSNR(getSingleBooleanAttribute(pendingMessage));
    }

    private void setSNRPacketInterval(OfflineDeviceMessage pendingMessage) throws IOException {
        getG3NetworkManagement().setSNRPacketInterval(getSingleIntegerAttribute(pendingMessage));
    }

    private void setSNRQuietTime(OfflineDeviceMessage pendingMessage) throws IOException {
        getG3NetworkManagement().setSNRQuietTime(getSingleIntegerAttribute(pendingMessage));
    }

    private void setSNRPayload(OfflineDeviceMessage pendingMessage) throws IOException {
        byte[] payLoad = ProtocolTools.getBytesFromHexString(pendingMessage.getDeviceMessageAttributes().get(0).getValue(), "");
        getG3NetworkManagement().setSNRPayload(payLoad);
    }

    private void enableKeepAlive(OfflineDeviceMessage pendingMessage) throws IOException {
        getG3NetworkManagement().enableKeepAlive(getSingleBooleanAttribute(pendingMessage));
    }

    private void setKeepAliveScheduleInterval(OfflineDeviceMessage pendingMessage) throws IOException {
        getG3NetworkManagement().setKeepAliveScheduleInterval(getSingleIntegerAttribute(pendingMessage));
    }

    @Deprecated
    private void setKeepAliveBucketSize(OfflineDeviceMessage pendingMessage) throws IOException {
        getG3NetworkManagement().setKeepAliveBucketSize(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMinInactiveMeterTime(OfflineDeviceMessage pendingMessage) throws IOException {
        getG3NetworkManagement().setMinInactiveMeterTime(getSingleIntegerAttribute(pendingMessage));
    }

    private void setMaxInactiveMeterTime(OfflineDeviceMessage pendingMessage) throws IOException {
        getG3NetworkManagement().setMaxInactiveMeterTime(getSingleIntegerAttribute(pendingMessage));
    }

    protected G3NetworkManagement getG3NetworkManagement() throws NotInObjectListException {
        return this.session.getCosemObjectFactory().getG3NetworkManagement();
    }

    private void setKeepAliveFailCount(OfflineDeviceMessage pendingMessage) throws IOException {
        getG3NetworkManagement().setKeepAliveFailCount(getSingleIntegerAttribute(pendingMessage));
    }

    private void setKeepAliveDelayBetweenPings(OfflineDeviceMessage pendingMessage) throws IOException {
        getG3NetworkManagement().setKeepAliveDelayBetweenPings(getSingleIntegerAttribute(pendingMessage));
    }

    private void enableG3PLCInterface(OfflineDeviceMessage pendingMessage) throws IOException {
        getG3NetworkManagement().enableG3Interface(getSingleBooleanAttribute(pendingMessage));
    }

    private void writePlcG3Timeout(OfflineDeviceMessage pendingMessage) throws IOException {
        int timeoutInMinutes = getSingleIntegerAttribute(pendingMessage);
        this.session.getCosemObjectFactory().getData(PLC_G3_TIMEOUT_OBISCODE).setValueAttr(new Unsigned16(timeoutInMinutes));
    }

    private void configurePlcG3KeepAlive(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean enable = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.EnableKeepAlive).getValue());
        int startTime = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.keepAliveStartTime).getValue());
        int sendPeriod = Integer.valueOf(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.keepAliveSendPeriod).getValue());

        Structure structure = new Structure();
        structure.addDataType(new BooleanObject(enable));
        structure.addDataType(new Unsigned16(startTime));
        structure.addDataType(new Unsigned8(sendPeriod));
        this.session.getCosemObjectFactory().getData(PLC_G3_KEEP_ALIVE_OBISCODE).setValueAttr(structure);
    }

    private int getSingleIntegerAttribute(OfflineDeviceMessage pendingMessage) {
        return Integer.parseInt(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
    }

    private boolean getSingleBooleanAttribute(OfflineDeviceMessage pendingMessage) {
        return Boolean.parseBoolean(pendingMessage.getDeviceMessageAttributes().get(0).getValue());
    }

    protected CollectedMessage createCollectedMessage(OfflineDeviceMessage message) {
        return this.collectedDataFactory.createCollectedMessage(new DeviceMessageIdentifierById(message.getDeviceMessageId(), message.getDeviceIdentifier()));
    }

    protected CollectedMessage createCollectedMessageWithRegisterData(OfflineDeviceMessage message, List<CollectedRegister> registers) {
        return this.collectedDataFactory.createCollectedMessageWithRegisterData(new DeviceIdentifierById(message.getDeviceId()), new DeviceMessageIdentifierById(message.getDeviceMessageId(), message.getDeviceIdentifier()), registers);
    }

    protected Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, Exception e) {
        return createMessageFailedIssue(pendingMessage, e.getMessage());
    }

    protected Issue createMessageFailedIssue(OfflineDeviceMessage pendingMessage, String message) {
        return this.issueFactory.createWarning(pendingMessage, "DeviceMessage.failed",
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName(),
                message);
    }

    protected Issue createUnsupportedWarning(OfflineDeviceMessage pendingMessage) throws IOException {
        return this.issueFactory.createWarning(pendingMessage, "DeviceMessage.notSupported",
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName());
    }

    public class PathRequestFeedback {

        private final String feedback;
        private final List<CollectedRegister> registers;

        public PathRequestFeedback(String feedback, List<CollectedRegister> registers) {
            this.feedback = feedback;
            this.registers = registers;
        }

        public String getFeedback() {
            return feedback;
        }

        public List<CollectedRegister> getRegisters() {
            return registers;
        }
    }
}