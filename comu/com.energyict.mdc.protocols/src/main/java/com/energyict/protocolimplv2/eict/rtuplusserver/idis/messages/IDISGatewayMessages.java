/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.eict.rtuplusserver.idis.messages;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.TimeOfDay;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageConstants;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageStatus;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;

import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.AXDRTime;
import com.energyict.dlms.cosem.DataAccessResultCode;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.cosem.FirewallSetup;
import com.energyict.dlms.cosem.GatewaySetup;
import com.energyict.dlms.cosem.MasterboardSetup;
import com.energyict.dlms.cosem.NetworkManagement;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.eict.rtuplusserver.idis.registers.IDISGatewayRegisters;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @author sva
 * @since 15/10/2014 - 15:43
 */
public class IDISGatewayMessages implements DeviceMessageSupport {

    private static final ObisCode DEFAULT_DISCONNECTOR_OBISCODE = ObisCode.fromString("0.0.96.3.10.255");

    private final Set<DeviceMessageId> supportedMessages = EnumSet.of(
            DeviceMessageId.LOGGING_CONFIGURATION_DEVICE_MESSAGE_SET_SERVER_LOG_LEVEL,
            DeviceMessageId.LOGGING_CONFIGURATION_DEVICE_MESSAGE_SET_WEB_PORTAL_LOG_LEVEL,
            DeviceMessageId.NETWORK_CONNECTIVITY_SET_OPERATING_WINDOW_START_TIME,
            DeviceMessageId.NETWORK_CONNECTIVITY_SET_OPERATING_WINDOW_END_TIME,
            DeviceMessageId.NETWORK_CONNECTIVITY_CLEAR_WHITE_LIST,
            DeviceMessageId.NETWORK_CONNECTIVITY_ENABLE_WHITE_LIST,
            DeviceMessageId.NETWORK_CONNECTIVITY_DISABLE_WHITE_LIST,
            DeviceMessageId.NETWORK_CONNECTIVITY_ENABLE_OPERATING_WINDOW,
            DeviceMessageId.NETWORK_CONNECTIVITY_DISABLE_OPERATING_WINDOW,
            DeviceMessageId.NETWORK_CONNECTIVITY_SET_NETWORK_MANAGEMENT_PARAMETERS,
            DeviceMessageId.NETWORK_CONNECTIVITY_RUN_METER_DISCOVERY,
            DeviceMessageId.NETWORK_CONNECTIVITY_RUN_ALARM_METER_DISCOVERY,
            DeviceMessageId.NETWORK_CONNECTIVITY_RUN_REPEATER_CALL,
            DeviceMessageId.FIREWALL_ACTIVATE_FIREWALL,
            DeviceMessageId.FIREWALL_DEACTIVATE_FIREWALL,
            DeviceMessageId.FIREWALL_CONFIGURE_FW_GPRS,
            DeviceMessageId.FIREWALL_CONFIGURE_FW_LAN,
            DeviceMessageId.FIREWALL_CONFIGURE_FW_WAN,
            DeviceMessageId.FIREWALL_SET_FW_DEFAULT_STATE,
            DeviceMessageId.CONFIGURATION_CHANGE_CONFIGURE_MASTER_BOARD_PARAMETERS,
            DeviceMessageId.OUTPUT_CONFIGURATION_WRITE_OUTPUT_STATE,
            DeviceMessageId.CLOCK_SET_SYNCHRONIZE_TIME,
            DeviceMessageId.DEVICE_ACTIONS_REBOOT_DEVICE,
            DeviceMessageId.DEVICE_ACTIONS_REBOOT_APPLICATION
    );

    private final DlmsSession session;
    private final IssueService issueService;
    private final CollectedDataFactory collectedDataFactory;

    public IDISGatewayMessages(DlmsSession session, IssueService issueService, CollectedDataFactory collectedDataFactory) {
        this.session = session;
        this.issueService = issueService;
        this.collectedDataFactory = collectedDataFactory;
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return supportedMessages;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.collectedDataFactory.createCollectedMessageList(pendingMessages);
        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                switch (pendingMessage.getDeviceMessageId()) {
                    case LOGGING_CONFIGURATION_DEVICE_MESSAGE_SET_SERVER_LOG_LEVEL: {
                        setServerLogLevel(pendingMessage);
                    }
                    case LOGGING_CONFIGURATION_DEVICE_MESSAGE_SET_WEB_PORTAL_LOG_LEVEL: {
                        setWebPortalLogLevel(pendingMessage);
                    }
                    case NETWORK_CONNECTIVITY_SET_OPERATING_WINDOW_START_TIME: {
                        setOperatingWindowStartTime(pendingMessage);
                    }
                    case NETWORK_CONNECTIVITY_SET_OPERATING_WINDOW_END_TIME: {
                        setOperatingWindowEndTime(pendingMessage);
                    }
                    case NETWORK_CONNECTIVITY_CLEAR_WHITE_LIST: {
                        clearWhitelist(pendingMessage);
                    }
                    case NETWORK_CONNECTIVITY_ENABLE_WHITE_LIST: {
                        enableDisableWhitelist(pendingMessage);
                    }
                    case NETWORK_CONNECTIVITY_DISABLE_WHITE_LIST: {
                        enableDisableWhitelist(pendingMessage);
                    }
                    case NETWORK_CONNECTIVITY_ENABLE_OPERATING_WINDOW: {
                        enableDisableOperatingWindow(pendingMessage);
                    }
                    case NETWORK_CONNECTIVITY_DISABLE_OPERATING_WINDOW: {
                        enableDisableOperatingWindow(pendingMessage);
                    }
                    case NETWORK_CONNECTIVITY_SET_NETWORK_MANAGEMENT_PARAMETERS: {
                        setNetworkMgmtParameters(pendingMessage);
                    }
                    case NETWORK_CONNECTIVITY_RUN_METER_DISCOVERY: {
                        runMeterDiscovery(pendingMessage);
                    }
                    case NETWORK_CONNECTIVITY_RUN_ALARM_METER_DISCOVERY: {
                        runAlarmMeterDiscovery(pendingMessage);
                    }
                    case NETWORK_CONNECTIVITY_RUN_REPEATER_CALL: {
                        runRepeaterCall(pendingMessage);
                    }
                    case CONFIGURATION_CHANGE_CONFIGURE_MASTER_BOARD_PARAMETERS: {
                        configureMasterboardParameters(pendingMessage);
                    }
                    case FIREWALL_ACTIVATE_FIREWALL: {
                        activateOrDeactivateFirewall(pendingMessage);
                    }
                    case FIREWALL_DEACTIVATE_FIREWALL: {
                        activateOrDeactivateFirewall(pendingMessage);
                    }
                    case FIREWALL_CONFIGURE_FW_GPRS: {
                        setPortConfiguration(pendingMessage);
                    }
                    case FIREWALL_CONFIGURE_FW_LAN: {
                        setPortConfiguration(pendingMessage);
                    }
                    case FIREWALL_CONFIGURE_FW_WAN: {
                        setPortConfiguration(pendingMessage);
                    }
                    case FIREWALL_SET_FW_DEFAULT_STATE: {
                        setFirewallDefaultState(pendingMessage);
                    }
                    case OUTPUT_CONFIGURATION_WRITE_OUTPUT_STATE: {
                        changeOutputState(pendingMessage);
                    }
                    case CLOCK_SET_SYNCHRONIZE_TIME: {
                        forceClock(pendingMessage);
                    }
                    case DEVICE_ACTIONS_REBOOT_DEVICE: {
                        rebootDevice(pendingMessage);
                    }
                    case DEVICE_ACTIONS_HARD_RESET_DEVICE: {
                        hardResetDevice(pendingMessage);
                    }
                    case DEVICE_ACTIONS_REBOOT_APPLICATION: {
                        restartApplication(pendingMessage);
                    }
                    default: {
                        collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                        collectedMessage.setFailureInformation(ResultType.NotSupported, messageUnsupported(pendingMessage));
                    }
                }
            }
            catch (IOException e) {
                if (IOExceptionHandler.isUnexpectedResponse(e, session)) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.InCompatible, messageFailed(pendingMessage, e));
                }   //Else: throw communication exception
            }
            catch (IndexOutOfBoundsException | NumberFormatException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(ResultType.InCompatible, messageFailed(pendingMessage, e));
            }
            result.addCollectedMessages(collectedMessage);
        }
        return result;
    }

    //// Logger settings ////

    private void setServerLogLevel(OfflineDeviceMessage pendingMessage) throws IOException {
        int logLevel = getIntegerMessageAttributeValue(pendingMessage, DeviceMessageConstants.logLevel);
        this.session.getCosemObjectFactory().getLoggerSettings().writeServerLogLevel(new TypeEnum(logLevel));
    }

    private void setWebPortalLogLevel(OfflineDeviceMessage pendingMessage) throws IOException {
        int logLevel = getIntegerMessageAttributeValue(pendingMessage, DeviceMessageConstants.logLevel);
        this.session.getCosemObjectFactory().getLoggerSettings().writeWebPortalLogLevel(new TypeEnum(logLevel));
    }


    //// Gateway setup ////

    private void setOperatingWindowStartTime(OfflineDeviceMessage pendingMessage) throws IOException {
        String startTime = getMessageAttributeValue(pendingMessage, DeviceMessageConstants.startTime);
        final GatewaySetup gatewaySetup = this.session.getCosemObjectFactory().getGatewaySetup();
        final AXDRTime axdrTime = new AXDRTime();
        axdrTime.setTime(startTime);
        gatewaySetup.writeOperatingWindowStartTime(axdrTime.getOctetString());
    }

    private void setOperatingWindowEndTime(OfflineDeviceMessage pendingMessage) throws IOException {
        String startTime = getMessageAttributeValue(pendingMessage, DeviceMessageConstants.endTime);
        final GatewaySetup gatewaySetup = this.session.getCosemObjectFactory().getGatewaySetup();
        final AXDRTime axdrTime = new AXDRTime();
        axdrTime.setTime(startTime);
        gatewaySetup.writeOperatingWindowStartTime(axdrTime.getOctetString());
    }

    private void clearWhitelist(OfflineDeviceMessage pendingMessage) throws IOException {
        final GatewaySetup gatewaySetup = this.session.getCosemObjectFactory().getGatewaySetup();
        gatewaySetup.clearWhitelist();
    }

    private void enableDisableWhitelist(OfflineDeviceMessage pendingMessage) throws IOException {
        final GatewaySetup gatewaySetup = this.session.getCosemObjectFactory().getGatewaySetup();
        if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.NETWORK_CONNECTIVITY_ENABLE_WHITE_LIST)) {
            gatewaySetup.activateWhitelist();
        } else {
            gatewaySetup.deactivateWhitelist();
        }
    }

    private void enableDisableOperatingWindow(OfflineDeviceMessage pendingMessage) throws IOException {
        final GatewaySetup gatewaySetup = this.session.getCosemObjectFactory().getGatewaySetup();
        if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.NETWORK_CONNECTIVITY_ENABLE_OPERATING_WINDOW)) {
            gatewaySetup.activateOperatingWindow();
        } else {
            gatewaySetup.deactivateOperatingWindow();
        }
    }


    ///// Network management ////
    private void runMeterDiscovery(OfflineDeviceMessage pendingMessage) throws IOException {
        try {
            this.session.getCosemObjectFactory().getNetworkManagement().runMeterDiscovery();
        } catch (DataAccessResultException e) {
            if (e.getCode().getResultCode() == DataAccessResultCode.HARDWARE_FAULT.getResultCode()) {
                throw new DataAccessResultException(e.getDataAccessResult(), e.getMessage() + ", probably because the IDIS interface is not available");
            } else if (e.getCode().getResultCode() == DataAccessResultCode.TEMPORARY_FAILURE.getResultCode()) {
                throw new DataAccessResultException(e.getDataAccessResult(), e.getMessage() + ", probably because the action is performed outside the operating window or the discovery process/repeater call is already running");
            }
            throw e;
        }
    }

    private void runAlarmMeterDiscovery(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getNetworkManagement().runAlarmMeterDiscovery();
    }

    private void runRepeaterCall(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getNetworkManagement().runRepeaterCall();
    }

    private void setNetworkMgmtParameters(OfflineDeviceMessage pendingMessage) throws IOException {
        int discoverDuration = getIntegerMessageAttributeValue(pendingMessage, DeviceMessageConstants.discoverDuration);
        int discoverInterval = getIntegerMessageAttributeValue(pendingMessage, DeviceMessageConstants.discoverInterval);
        int repeaterCallInterval = getIntegerMessageAttributeValue(pendingMessage, DeviceMessageConstants.repeaterCallInterval);
        int repeaterCallThreshold = getIntegerMessageAttributeValue(pendingMessage, DeviceMessageConstants.repeaterCallThreshold);
        int repeaterCallTimeslots = getIntegerMessageAttributeValue(pendingMessage, DeviceMessageConstants.repeaterCallTimeslots);

        final NetworkManagement networkManagement = this.session.getCosemObjectFactory().getNetworkManagement();
        Structure structure = new Structure();
        structure.addDataType(new Unsigned32(discoverDuration));
        structure.addDataType(new Unsigned8(discoverInterval));
        structure.addDataType(new Unsigned32(repeaterCallInterval));
        structure.addDataType(new Unsigned16(repeaterCallThreshold));
        structure.addDataType(new Unsigned8(repeaterCallTimeslots));

        networkManagement.writeNetworkMgmtParameters(structure);
    }


    //// Masterboard setup ////

    private void configureMasterboardParameters(OfflineDeviceMessage pendingMessage) throws IOException {
        int localMacAddress = getIntegerMessageAttributeValue(pendingMessage, DeviceMessageConstants.localMacAddress);
        int maxCredit = getIntegerMessageAttributeValue(pendingMessage, DeviceMessageConstants.maxCredit);
        int zeroCrossDelay = getIntegerMessageAttributeValue(pendingMessage, DeviceMessageConstants.zeroCrossDelay);
        int synchronisationBit = getIntegerMessageAttributeValue(pendingMessage, DeviceMessageConstants.synchronisationBit);

        final MasterboardSetup masterboardSetup = this.session.getCosemObjectFactory().getMasterboardSetup();
        masterboardSetup.writeMasterboardConfigParameters(
                localMacAddress != -1 ? new Unsigned16(localMacAddress) : null,
                maxCredit != -1 ? new Unsigned8(maxCredit) : null,
                zeroCrossDelay != -1 ? new Unsigned16(zeroCrossDelay) : null,
                synchronisationBit != -1 ? new Unsigned8(synchronisationBit) : null
        );
    }


    //// Firewall configuration ////

    private void activateOrDeactivateFirewall(OfflineDeviceMessage pendingMessage) throws IOException {
        if (pendingMessage.getDeviceMessageId().equals(DeviceMessageId.FIREWALL_ACTIVATE_FIREWALL)) {
            this.session.getCosemObjectFactory().getFirewallSetup().activate();
        } else {
            this.session.getCosemObjectFactory().getFirewallSetup().deactivate();
        }
    }

    private void setFirewallDefaultState(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean isDefaultEnabled = getBooleanDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.defaultEnabled);
        this.session.getCosemObjectFactory().getFirewallSetup().setEnabledByDefault(isDefaultEnabled);
    }

    private void setPortConfiguration(OfflineDeviceMessage pendingMessage) throws IOException {
        boolean isDLMSAllowed = getBooleanDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.EnableDLMS);
        boolean isHTTPAllowed = getBooleanDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.EnableHTTP);
        boolean isSSHAllowed = getBooleanDeviceMessageAttributeValue(pendingMessage, DeviceMessageConstants.EnableSSH);

        switch (pendingMessage.getDeviceMessageId()) {
            case FIREWALL_CONFIGURE_FW_WAN: {
                this.session.getCosemObjectFactory().getFirewallSetup().setWANPortSetup(new FirewallSetup.InterfaceFirewallConfiguration(isDLMSAllowed, isHTTPAllowed, isSSHAllowed));
            } case FIREWALL_CONFIGURE_FW_LAN: {
                this.session.getCosemObjectFactory().getFirewallSetup().setLANPortSetup(new FirewallSetup.InterfaceFirewallConfiguration(isDLMSAllowed, isHTTPAllowed, isSSHAllowed));
            } case FIREWALL_CONFIGURE_FW_GPRS: {
                this.session.getCosemObjectFactory().getFirewallSetup().setGPRSPortSetup(new FirewallSetup.InterfaceFirewallConfiguration(isDLMSAllowed, isHTTPAllowed, isSSHAllowed));
            }
        }
    }


    //// Digital IO's ////

    private void changeOutputState(OfflineDeviceMessage pendingMessage) throws IOException {
        int outputId = getIntegerMessageAttributeValue(pendingMessage, DeviceMessageConstants.outputId);
        boolean newState = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.newState).getDeviceMessageAttributeValue());

        final ObisCode obisCode = ProtocolTools.setObisCodeField(DEFAULT_DISCONNECTOR_OBISCODE, 1, (byte) outputId);
        final Disconnector disconnector = this.session.getCosemObjectFactory().getDisconnector(obisCode);
        if (newState) {
            disconnector.remoteReconnect();
        } else {
            disconnector.remoteDisconnect();
        }
    }


    //// RTU+Server maintenance ////

    private void forceClock(OfflineDeviceMessage pendingMessage) throws IOException {
        Calendar cal = Calendar.getInstance(session.getTimeZone());
        Date currentTime = new Date();
        cal.setTime(currentTime);
        session.getCosemObjectFactory().getClock(IDISGatewayRegisters.CLOCK_OBIS_CODE).setAXDRDateTimeAttr(new AXDRDateTime(cal));
    }

    private void rebootDevice(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getLifeCycleManagement().rebootDevice();
    }

    private void hardResetDevice(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getLifeCycleManagement().hardReset();
    }

    private void restartApplication(OfflineDeviceMessage pendingMessage) throws IOException {
        this.session.getCosemObjectFactory().getLifeCycleManagement().restartApplication();
    }

    private String getMessageAttributeValue(OfflineDeviceMessage pendingMessage, String attributeName) {
        return MessageConverterTools.getDeviceMessageAttribute(
                pendingMessage,
                attributeName).getDeviceMessageAttributeValue();
    }

    private int getIntegerMessageAttributeValue(OfflineDeviceMessage pendingMessage, String attributeName) {
        return Integer.parseInt(
                MessageConverterTools.getDeviceMessageAttribute(
                        pendingMessage,
                        attributeName).getDeviceMessageAttributeValue()
        );
    }


    private boolean getBooleanDeviceMessageAttributeValue(OfflineDeviceMessage pendingMessage, String attributeName) {
        return ProtocolTools.getBooleanFromString(
                MessageConverterTools.getDeviceMessageAttribute(
                        pendingMessage,
                        attributeName).getDeviceMessageAttributeValue()
        );
    }

    private CollectedMessage createCollectedMessage(OfflineDeviceMessage message) {
        return this.collectedDataFactory.createCollectedMessage(message.getIdentifier());
    }

    private Issue messageFailed(OfflineDeviceMessage pendingMessage, Exception e) {
        return messageFailed(pendingMessage, e.getMessage());
    }

    private Issue messageFailed(OfflineDeviceMessage pendingMessage, String message) {
        return this.issueService.newWarning(
                pendingMessage,
                MessageSeeds.DEVICEMESSAGE_FAILED,
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName(),
                message);
    }

    private Issue messageUnsupported(OfflineDeviceMessage pendingMessage) {
        return this.issueService.newWarning(
                pendingMessage,
                MessageSeeds.DEVICEMESSAGE_NOT_SUPPORTED,
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName());
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return this.collectedDataFactory.createEmptyCollectedMessageList();  //Nothing to do here
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.startTime) || propertySpec.getName().equals(DeviceMessageConstants.endTime)) {
            TimeOfDay timeOfDay = (TimeOfDay) messageAttribute;
            return String.valueOf(timeOfDay.getHours() + ":" + timeOfDay.getMinutes() + ":" + timeOfDay.getSeconds());
        } else {
            return propertySpec.toString(); // The default fall-back option, works for (Hex)String, BigDecimal, Boolean, ...
        }
    }
}
