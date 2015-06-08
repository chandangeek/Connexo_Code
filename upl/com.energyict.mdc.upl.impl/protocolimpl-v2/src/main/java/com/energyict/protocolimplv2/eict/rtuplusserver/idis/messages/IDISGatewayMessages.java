package com.energyict.protocolimplv2.eict.rtuplusserver.idis.messages;

import com.energyict.cbo.TimeOfDay;
import com.energyict.cpo.PropertySpec;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.axrdencoding.util.AXDRTime;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.messages.DeviceMessageStatus;
import com.energyict.mdc.meterdata.CollectedMessage;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.protocol.tasks.support.DeviceMessageSupport;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.eict.rtuplusserver.idis.registers.IDISGatewayRegisters;
import com.energyict.protocolimplv2.identifiers.DeviceMessageIdentifierById;
import com.energyict.protocolimplv2.messages.*;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author sva
 * @since 15/10/2014 - 15:43
 */
public class IDISGatewayMessages implements DeviceMessageSupport {

    private static final ObisCode DEFAULT_DISCONNECTOR_OBISCODE = ObisCode.fromString("0.0.96.3.10.255");
    private final DlmsSession session;
    private List<DeviceMessageSpec> supportedMessages;

    public IDISGatewayMessages(DlmsSession session) {
        this.session = session;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        if (supportedMessages == null) {
            supportedMessages = new ArrayList<>();

            // Logger settings
            supportedMessages.add(LoggingConfigurationDeviceMessage.SetServerLogLevel);
            supportedMessages.add(LoggingConfigurationDeviceMessage.SetWebPortalLogLevel);

            // Gateway setup
            supportedMessages.add(NetworkConnectivityMessage.SetOperatingWindowStartTime);
            supportedMessages.add(NetworkConnectivityMessage.SetOperatingWindowEndTime);
            supportedMessages.add(NetworkConnectivityMessage.ClearWhiteList);
            supportedMessages.add(NetworkConnectivityMessage.EnableWhiteList);
            supportedMessages.add(NetworkConnectivityMessage.DisableWhiteList);
            supportedMessages.add(NetworkConnectivityMessage.EnableOperatingWindow);
            supportedMessages.add(NetworkConnectivityMessage.DisableOperatingWindow);

            // Network management
            supportedMessages.add(NetworkConnectivityMessage.SetNetworkManagementParameters);
            supportedMessages.add(NetworkConnectivityMessage.RunMeterDiscovery);
            supportedMessages.add(NetworkConnectivityMessage.RunAlarmMeterDiscovery);
            supportedMessages.add(NetworkConnectivityMessage.RunRepeaterCall);

            // Masterboard setup
            supportedMessages.add(ConfigurationChangeDeviceMessage.ConfigureMasterBoardParameters);

            // Firewall configuration
            supportedMessages.add(FirewallConfigurationMessage.ActivateFirewall);
            supportedMessages.add(FirewallConfigurationMessage.DeactivateFirewall);
            supportedMessages.add(FirewallConfigurationMessage.ConfigureFWGPRS);
            supportedMessages.add(FirewallConfigurationMessage.ConfigureFWLAN);
            supportedMessages.add(FirewallConfigurationMessage.ConfigureFWWAN);
            supportedMessages.add(FirewallConfigurationMessage.SetFWDefaultState);

            // Digital IO's
            supportedMessages.add(OutputConfigurationMessage.WriteOutputState);

            // RTU+Server maintenance
            supportedMessages.add(ClockDeviceMessage.SyncTime);
            supportedMessages.add(DeviceActionMessage.REBOOT_DEVICE);
            supportedMessages.add(DeviceActionMessage.RebootApplication);
        }
        return supportedMessages;
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = MdcManager.getCollectedDataFactory().createCollectedMessageList(pendingMessages);
        for (OfflineDeviceMessage pendingMessage : pendingMessages) {
            CollectedMessage collectedMessage = createCollectedMessage(pendingMessage);
            collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.CONFIRMED);   //Optimistic
            try {
                if (pendingMessage.getSpecification().equals(LoggingConfigurationDeviceMessage.SetServerLogLevel)) {
                    setServerLogLevel(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(LoggingConfigurationDeviceMessage.SetWebPortalLogLevel)) {
                    setWebPortalLogLevel(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.SetOperatingWindowStartTime)) {
                    setOperatingWindowStartTime(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.SetOperatingWindowEndTime)) {
                    setOperatingWindowEndTime(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.ClearWhiteList)) {
                    clearWhitelist(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.EnableWhiteList)) {
                    enableDisableWhitelist(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.DisableWhiteList)) {
                    enableDisableWhitelist(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.EnableOperatingWindow)) {
                    enableDisableOperatingWindow(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.DisableOperatingWindow)) {
                    enableDisableOperatingWindow(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.SetNetworkManagementParameters)) {
                    setNetworkMgmtParameters(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.RunMeterDiscovery)) {
                    runMeterDiscovery(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.RunAlarmMeterDiscovery)) {
                    runAlarmMeterDiscovery(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.RunRepeaterCall)) {
                    runRepeaterCall(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ConfigurationChangeDeviceMessage.ConfigureMasterBoardParameters)) {
                    configureMasterboardParameters(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.ActivateFirewall)) {
                    activateOrDeactivateFirewall(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.DeactivateFirewall)) {
                    activateOrDeactivateFirewall(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.ConfigureFWGPRS)) {
                    setPortConfiguration(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.ConfigureFWLAN)) {
                    setPortConfiguration(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.ConfigureFWWAN)) {
                    setPortConfiguration(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.SetFWDefaultState)) {
                    setFirewallDefaultState(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(OutputConfigurationMessage.WriteOutputState)) {
                    changeOutputState(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(ClockDeviceMessage.SyncTime)) {
                    forceClock(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.REBOOT_DEVICE)) {
                    rebootDevice(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.HardResetDevice)) {
                    hardResetDevice(pendingMessage);
                } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.RebootApplication)) {
                    restartApplication(pendingMessage);
                } else {   //Unsupported message
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.NotSupported, messageUnsupported(pendingMessage));
                    collectedMessage.setDeviceProtocolInformation("Message is currently not supported by the protocol");
                }
            } catch (IOException e) {
                if (IOExceptionHandler.isUnexpectedResponse(e, session)) {
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.InCompatible, messageFailed(pendingMessage, e));
                    collectedMessage.setDeviceProtocolInformation(e.getMessage());
                }   //Else: throw communication exception
            } catch (IndexOutOfBoundsException | NumberFormatException e) {
                collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                collectedMessage.setFailureInformation(ResultType.InCompatible, messageFailed(pendingMessage, e));
                collectedMessage.setDeviceProtocolInformation(e.getMessage());
            }
            result.addCollectedMessage(collectedMessage);
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
        if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.EnableWhiteList)) {
            gatewaySetup.activateWhitelist();
        } else {
            gatewaySetup.deactivateWhitelist();
        }
    }

    private void enableDisableOperatingWindow(OfflineDeviceMessage pendingMessage) throws IOException {
        final GatewaySetup gatewaySetup = this.session.getCosemObjectFactory().getGatewaySetup();
        if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.EnableOperatingWindow)) {
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
        if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.ActivateFirewall)) {
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

        if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.ConfigureFWWAN)) {
            this.session.getCosemObjectFactory().getFirewallSetup().setWANPortSetup(new FirewallSetup.InterfaceFirewallConfiguration(isDLMSAllowed, isHTTPAllowed, isSSHAllowed));
        } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.ConfigureFWLAN)) {
            this.session.getCosemObjectFactory().getFirewallSetup().setLANPortSetup(new FirewallSetup.InterfaceFirewallConfiguration(isDLMSAllowed, isHTTPAllowed, isSSHAllowed));
        } else if (pendingMessage.getSpecification().equals(FirewallConfigurationMessage.ConfigureFWGPRS)) {
            this.session.getCosemObjectFactory().getFirewallSetup().setGPRSPortSetup(new FirewallSetup.InterfaceFirewallConfiguration(isDLMSAllowed, isHTTPAllowed, isSSHAllowed));
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
        return MdcManager.getCollectedDataFactory().createCollectedMessage(new DeviceMessageIdentifierById(message.getDeviceMessageId()));
    }

    private Issue messageFailed(OfflineDeviceMessage pendingMessage, Exception e) {
        return messageFailed(pendingMessage, e.getMessage());
    }

    private Issue messageFailed(OfflineDeviceMessage pendingMessage, String message) {
        return MdcManager.getIssueCollector().addWarning(pendingMessage, "DeviceMessage.failed",
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName(),
                message);
    }

    private Issue messageUnsupported(OfflineDeviceMessage pendingMessage) throws IOException {
        return MdcManager.getIssueCollector().addWarning(pendingMessage, "DeviceMessage.notSupported",
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName());
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return MdcManager.getCollectedDataFactory().createEmptyCollectedMessageList();  //Nothing to do here
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.startTime) || propertySpec.getName().equals(DeviceMessageConstants.endTime)) {
            TimeOfDay timeOfDay = (TimeOfDay) messageAttribute;
            return String.valueOf(timeOfDay.getHoursPart() + ":" + timeOfDay.getMinutesPart() + ":" + timeOfDay.getSeconds());
        } else {
            return messageAttribute.toString(); // The default fall-back option, works for (Hex)String, BigDecimal, Boolean, ...
        }
    }
}
