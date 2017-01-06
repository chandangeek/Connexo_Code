package com.energyict.protocolimplv2.eict.rtuplusserver.idis.messages;

import com.energyict.mdc.upl.issue.Issue;
import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.messages.DeviceMessage;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.messages.DeviceMessageStatus;
import com.energyict.mdc.upl.messages.OfflineDeviceMessage;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.meterdata.ResultType;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.properties.Converter;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.tasks.support.DeviceMessageSupport;

import com.energyict.cbo.TimeOfDay;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
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
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.eict.rtuplusserver.idis.registers.IDISGatewayRegisters;
import com.energyict.protocolimplv2.identifiers.DeviceMessageIdentifierById;
import com.energyict.protocolimplv2.messages.ClockDeviceMessage;
import com.energyict.protocolimplv2.messages.ConfigurationChangeDeviceMessage;
import com.energyict.protocolimplv2.messages.DeviceActionMessage;
import com.energyict.protocolimplv2.messages.DeviceMessageConstants;
import com.energyict.protocolimplv2.messages.FirewallConfigurationMessage;
import com.energyict.protocolimplv2.messages.LoggingConfigurationDeviceMessage;
import com.energyict.protocolimplv2.messages.NetworkConnectivityMessage;
import com.energyict.protocolimplv2.messages.OutputConfigurationMessage;
import com.energyict.protocolimplv2.messages.convertor.MessageConverterTools;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author sva
 * @since 15/10/2014 - 15:43
 */
public class IDISGatewayMessages implements DeviceMessageSupport {

    private static final ObisCode DEFAULT_DISCONNECTOR_OBISCODE = ObisCode.fromString("0.0.96.3.10.255");
    private final DlmsSession session;
    private final CollectedDataFactory collectedDataFactory;
    private final IssueFactory issueFactory;
    private final PropertySpecService propertySpecService;
    private final NlsService nlsService;
    private final Converter converter;

    public IDISGatewayMessages(DlmsSession session, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory, PropertySpecService propertySpecService, NlsService nlsService, Converter converter) {
        this.session = session;
        this.collectedDataFactory = collectedDataFactory;
        this.issueFactory = issueFactory;
        this.propertySpecService = propertySpecService;
        this.nlsService = nlsService;
        this.converter = converter;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return Arrays.asList(
            // Logger settings
            LoggingConfigurationDeviceMessage.SetServerLogLevel.get(this.propertySpecService, this.nlsService, this.converter),
            LoggingConfigurationDeviceMessage.SetWebPortalLogLevel.get(this.propertySpecService, this.nlsService, this.converter),

            // Gateway setup
            NetworkConnectivityMessage.SetOperatingWindowStartTime.get(this.propertySpecService, this.nlsService, this.converter),
            NetworkConnectivityMessage.SetOperatingWindowEndTime.get(this.propertySpecService, this.nlsService, this.converter),
            NetworkConnectivityMessage.ClearWhiteList.get(this.propertySpecService, this.nlsService, this.converter),
            NetworkConnectivityMessage.EnableWhiteList.get(this.propertySpecService, this.nlsService, this.converter),
            NetworkConnectivityMessage.DisableWhiteList.get(this.propertySpecService, this.nlsService, this.converter),
            NetworkConnectivityMessage.EnableOperatingWindow.get(this.propertySpecService, this.nlsService, this.converter),
            NetworkConnectivityMessage.DisableOperatingWindow.get(this.propertySpecService, this.nlsService, this.converter),

            // Network management
            NetworkConnectivityMessage.SetNetworkManagementParameters.get(this.propertySpecService, this.nlsService, this.converter),
            NetworkConnectivityMessage.RunMeterDiscovery.get(this.propertySpecService, this.nlsService, this.converter),
            NetworkConnectivityMessage.RunAlarmMeterDiscovery.get(this.propertySpecService, this.nlsService, this.converter),
            NetworkConnectivityMessage.RunRepeaterCall.get(this.propertySpecService, this.nlsService, this.converter),

            // Masterboard setup
            ConfigurationChangeDeviceMessage.ConfigureMasterBoardParameters.get(this.propertySpecService, this.nlsService, this.converter),

            // Firewall configuration
            FirewallConfigurationMessage.ActivateFirewall.get(this.propertySpecService, this.nlsService, this.converter),
            FirewallConfigurationMessage.DeactivateFirewall.get(this.propertySpecService, this.nlsService, this.converter),
            FirewallConfigurationMessage.ConfigureFWGPRS.get(this.propertySpecService, this.nlsService, this.converter),
            FirewallConfigurationMessage.ConfigureFWLAN.get(this.propertySpecService, this.nlsService, this.converter),
            FirewallConfigurationMessage.ConfigureFWWAN.get(this.propertySpecService, this.nlsService, this.converter),
            FirewallConfigurationMessage.SetFWDefaultState.get(this.propertySpecService, this.nlsService, this.converter),

            // Digital IO's
            OutputConfigurationMessage.WriteOutputState.get(this.propertySpecService, this.nlsService, this.converter),

            // RTU+Server maintenance
            ClockDeviceMessage.SyncTime.get(this.propertySpecService, this.nlsService, this.converter),
            DeviceActionMessage.REBOOT_DEVICE.get(this.propertySpecService, this.nlsService, this.converter),
            DeviceActionMessage.RebootApplication.get(this.propertySpecService, this.nlsService, this.converter));
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        CollectedMessageList result = this.collectedDataFactory.createCollectedMessageList(pendingMessages);
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
                    clearWhitelist();
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
                    runMeterDiscovery();
                } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.RunAlarmMeterDiscovery)) {
                    runAlarmMeterDiscovery();
                } else if (pendingMessage.getSpecification().equals(NetworkConnectivityMessage.RunRepeaterCall)) {
                    runRepeaterCall();
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
                    forceClock();
                } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.REBOOT_DEVICE)) {
                    rebootDevice();
                } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.HardResetDevice)) {
                    hardResetDevice();
                } else if (pendingMessage.getSpecification().equals(DeviceActionMessage.RebootApplication)) {
                    restartApplication();
                } else {   //Unsupported message
                    collectedMessage.setNewDeviceMessageStatus(DeviceMessageStatus.FAILED);
                    collectedMessage.setFailureInformation(ResultType.NotSupported, messageUnsupported(pendingMessage));
                    collectedMessage.setDeviceProtocolInformation("Message is currently not supported by the protocol");
                }
            } catch (IOException e) {
                if (DLMSIOExceptionHandler.isUnexpectedResponse(e, session.getProperties().getRetries() + 1)) {
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

    private void clearWhitelist() throws IOException {
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
    private void runMeterDiscovery() throws IOException {
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

    private void runAlarmMeterDiscovery() throws IOException {
        this.session.getCosemObjectFactory().getNetworkManagement().runAlarmMeterDiscovery();
    }

    private void runRepeaterCall() throws IOException {
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
        structure.addDataType(new Unsigned8(discoverDuration));
        structure.addDataType(new Unsigned16(discoverInterval));
        structure.addDataType(new Unsigned16(repeaterCallInterval));
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

    private void changeOutputState(OfflineDeviceMessage pendingMessage) throws IOException {
        int outputId = getIntegerMessageAttributeValue(pendingMessage, DeviceMessageConstants.outputId);
        boolean newState = Boolean.parseBoolean(MessageConverterTools.getDeviceMessageAttribute(pendingMessage, DeviceMessageConstants.newState).getValue());

        final ObisCode obisCode = ProtocolTools.setObisCodeField(DEFAULT_DISCONNECTOR_OBISCODE, 1, (byte) outputId);
        final Disconnector disconnector = this.session.getCosemObjectFactory().getDisconnector(obisCode);
        if (newState) {
            disconnector.remoteReconnect();
        } else {
            disconnector.remoteDisconnect();
        }
    }

    private void forceClock() throws IOException {
        Calendar cal = Calendar.getInstance(session.getTimeZone());
        Date currentTime = new Date();
        cal.setTime(currentTime);
        session.getCosemObjectFactory().getClock(IDISGatewayRegisters.CLOCK_OBIS_CODE).setAXDRDateTimeAttr(new AXDRDateTime(cal));
    }

    private void rebootDevice() throws IOException {
        this.session.getCosemObjectFactory().getLifeCycleManagement().rebootDevice();
    }

    private void hardResetDevice() throws IOException {
        this.session.getCosemObjectFactory().getLifeCycleManagement().hardReset();
    }

    private void restartApplication() throws IOException {
        this.session.getCosemObjectFactory().getLifeCycleManagement().restartApplication();
    }

    private String getMessageAttributeValue(OfflineDeviceMessage pendingMessage, String attributeName) {
        return MessageConverterTools.getDeviceMessageAttribute(
                pendingMessage,
                attributeName).getValue();
    }

    private int getIntegerMessageAttributeValue(OfflineDeviceMessage pendingMessage, String attributeName) {
        return Integer.parseInt(
                MessageConverterTools.getDeviceMessageAttribute(
                        pendingMessage,
                        attributeName).getValue()
        );
    }


    private boolean getBooleanDeviceMessageAttributeValue(OfflineDeviceMessage pendingMessage, String attributeName) {
        return ProtocolTools.getBooleanFromString(
                MessageConverterTools.getDeviceMessageAttribute(
                        pendingMessage,
                        attributeName).getValue()
        );
    }

    private CollectedMessage createCollectedMessage(OfflineDeviceMessage message) {
        return this.collectedDataFactory.createCollectedMessage(new DeviceMessageIdentifierById(message.getDeviceMessageId()));
    }

    private Issue messageFailed(OfflineDeviceMessage pendingMessage, Exception e) {
        return messageFailed(pendingMessage, e.getMessage());
    }

    private Issue messageFailed(OfflineDeviceMessage pendingMessage, String message) {
        return this.issueFactory.createWarning(pendingMessage, "DeviceMessage.failed",
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName(),
                message);
    }

    private Issue messageUnsupported(OfflineDeviceMessage pendingMessage) {
        return this.issueFactory.createWarning(pendingMessage, "DeviceMessage.notSupported",
                pendingMessage.getDeviceMessageId(),
                pendingMessage.getSpecification().getCategory().getName(),
                pendingMessage.getSpecification().getName());
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return this.collectedDataFactory.createEmptyCollectedMessageList();  //Nothing to do here
    }

    @Override
    public String format(OfflineDevice offlineDevice, OfflineDeviceMessage offlineDeviceMessage, com.energyict.mdc.upl.properties.PropertySpec propertySpec, Object messageAttribute) {
        if (propertySpec.getName().equals(DeviceMessageConstants.startTime) || propertySpec.getName().equals(DeviceMessageConstants.endTime)) {
            TimeOfDay timeOfDay = (TimeOfDay) messageAttribute;
            return String.valueOf(timeOfDay.getHoursPart() + ":" + timeOfDay.getMinutesPart() + ":" + timeOfDay.getSeconds());
        } else {
            return messageAttribute.toString(); // The default fall-back option, works for (Hex)String, BigDecimal, Boolean, ...
        }
    }

    @Override
    public Optional<String> prepareMessageContext(Device device, OfflineDevice offlineDevice, DeviceMessage deviceMessage) {
        return Optional.empty();
    }

}