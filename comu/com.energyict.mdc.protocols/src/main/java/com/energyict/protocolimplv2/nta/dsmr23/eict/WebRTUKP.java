package com.energyict.protocolimplv2.nta.dsmr23.eict;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.ComChannelType;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.MessageSeeds;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.BreakerStatus;
import com.energyict.mdc.protocol.api.device.data.CollectedBreakerStatus;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOnV2;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedTopology;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.protocolimplv2.common.MyOwnPrivateRegister;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.DlmsProperties;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.securitysupport.DsmrSecuritySupport;
import com.energyict.protocols.impl.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.protocols.impl.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.protocols.impl.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.protocols.mdc.protocoltasks.SerialDeviceProtocolDialect;
import com.energyict.protocols.mdc.protocoltasks.TcpDeviceProtocolDialect;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * General error handling principle:
 * A DataAccessResultException, a ProtocolException or an ExceptionResponseException indicate we received an error from the meter.
 * E.g. the requested object does not exist, or it is not allowed to be read/written, etc.
 * These exceptions need to be caught and handled first! They all extend from IOException.
 * After that, all remaining IOExceptions are related to communication problems (e.g. timeout, connection broken,...).
 * <p>
 * Copyrights EnergyICT
 * Date: 18/10/13
 * Time: 11:40
 * Author: khe
 */
public class WebRTUKP extends AbstractDlmsProtocol {

    @Inject
    public WebRTUKP(
            Clock clock, Thesaurus thesaurus, PropertySpecService propertySpecService, SocketService socketService,
            SerialComponentService serialComponentService, IssueService issueService,
            TopologyService topologyService, MdcReadingTypeUtilService readingTypeUtilService,
            IdentificationService identificationService, CollectedDataFactory collectedDataFactory,
            MeteringService meteringService, LoadProfileFactory loadProfileFactory,
            Provider<DsmrSecuritySupport> dsmrSecuritySupportProvider) {
        super(clock, thesaurus, propertySpecService, socketService, serialComponentService, issueService, topologyService,
                readingTypeUtilService, identificationService, collectedDataFactory, meteringService, loadProfileFactory,
                dsmrSecuritySupportProvider);
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsProperties().setSerialNumber(offlineDevice.getSerialNumber());

        HHUSignOnV2 hhuSignOn = null;
        if (ComChannelType.SERIAL_COM_CHANNEL.is(comChannel) || ComChannelType.OPTICAL_COM_CHANNEL.is(comChannel)) {
            hhuSignOn = getHHUSignOn((SerialComChannel) comChannel);
        }
        setDlmsSession(new DlmsSession(comChannel, getDlmsProperties(), hhuSignOn, getProperDeviceId()));
    }

    private HHUSignOnV2 getHHUSignOn(SerialComChannel serialPortComChannel) {
        HHUSignOnV2 hhuSignOn = new IEC1107HHUSignOn(serialPortComChannel, getDlmsProperties());
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(false);
        return hhuSignOn;
    }

    private String getProperDeviceId() {
        String deviceId = getDlmsProperties().getDeviceId();
        if (deviceId != null && !deviceId.isEmpty()) {
            return deviceId;
        } else {
            return "!"; // the Kamstrup device requires a '!' sign in the IEC1107 signOn
        }
    }

    @Override
    public DlmsProperties getDlmsProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new WebRTUKPProperties(this.propertySpecService, this.thesaurus);
        }
        return dlmsProperties;
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.asList(
                new OutboundTcpIpConnectionType(this.getThesaurus(), this.getPropertySpecService(), getSocketService()),
                new SioOpticalConnectionType(getSerialComponentService(), this.getThesaurus()),
                new RxTxOpticalConnectionType(getSerialComponentService(), this.getThesaurus()));
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getDeviceLogBookFactory().getLogBookData(logBooks);
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return getDsmr23Messaging().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getDsmr23Messaging().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getDsmr23Messaging().updateSentMessages(sentMessages);
    }

    @Override
    public CollectedBreakerStatus getBreakerStatus() {
        CollectedBreakerStatus result = super.getBreakerStatus();

        try {
            Disconnector disconnector = getCosemObjectFactory().getDisconnector();
            TypeEnum controlState = disconnector.doReadControlState();
            switch (controlState.getValue()) {
                case 0:
                    result.setBreakerStatus(BreakerStatus.DISCONNECTED);
                    break;
                case 1:
                    result.setBreakerStatus(BreakerStatus.CONNECTED);
                    break;
                case 2:
                    result.setBreakerStatus(BreakerStatus.ARMED);
                    break;
                default:
                    OfflineRegister source = new MyOwnPrivateRegister(getOfflineDevice(), Disconnector.getDefaultObisCode());
                    result.setFailureInformation(ResultType.InCompatible, getIssueService().newProblem(source, MessageSeeds.COULD_NOT_READ_BREAKER_STATE, "received value '" + controlState.getValue() + "', expected either 0, 1 or 2."));
                    break;
            }
        } catch (IOException e) {
            if (IOExceptionHandler.isUnexpectedResponse(e, getDlmsSession())) {
                OfflineRegister source = new MyOwnPrivateRegister(getOfflineDevice(), Disconnector.getDefaultObisCode());
                result.setFailureInformation(ResultType.InCompatible, getIssueService().newProblem(source, MessageSeeds.COULD_NOT_READ_BREAKER_STATE, e));
            }
        }
        return result;
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return getDsmr23Messaging().format(propertySpec, messageAttribute);
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(
                new SerialDeviceProtocolDialect(this.getThesaurus(), this.getPropertySpecService()),
                new TcpDeviceProtocolDialect(this.getThesaurus(), this.getPropertySpecService()));
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterFactory().readRegisters(registers);
    }

    @Override
    public CollectedTopology getDeviceTopology() {
        return getMeterTopology().getCollectedDeviceTopology();
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-09-19 17:48:43 +0200 (Fri, 19 Sep 2014) $";
    }

    @Override
    public String getProtocolDescription() {
        return "EnergyICT WebRTU KP DLMS (NTA DSMR2.3) V2";
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        List<CollectedRegister> collectedRegisters = getRegisterFactory().readRegisters(Collections.singletonList(getFirmwareRegister()));
        CollectedFirmwareVersion firmwareVersionsCollectedData = getCollectedDataFactory().createFirmwareVersionsCollectedData(this.offlineDevice.getDeviceIdentifier());
        if (!collectedRegisters.isEmpty() && collectedRegisters.get(0).getResultType().equals(ResultType.Supported) && collectedRegisters.get(0).getText() != null) {
            firmwareVersionsCollectedData.setActiveMeterFirmwareVersion(collectedRegisters.get(0).getText());
            return firmwareVersionsCollectedData;
        }
        collectedRegisters.stream().forEach(each -> each.getIssues().stream().forEach(issue -> firmwareVersionsCollectedData.setFailureInformation(each.getResultType(), issue)));
        firmwareVersionsCollectedData.setFailureInformation(ResultType.DataIncomplete, createCouldNotReadoutFirmwareVersionIssue(getFirmwareRegister()));
        return firmwareVersionsCollectedData;
    }

    protected Issue createCouldNotReadoutFirmwareVersionIssue(OfflineRegister firmwareRegister) {
        return getIssueService().newProblem(
                firmwareRegister,
                com.energyict.mdc.protocol.api.MessageSeeds.COULD_NOT_READ_FIRMWARE_VERSION,
                firmwareRegister.getObisCode());
    }

    private OfflineRegister getFirmwareRegister() {
        return new OfflineRegister() {

            // Module Version identification
            private ObisCode firmwareObisCode = ObisCode.fromString("1.1.0.2.0.255");

            @Override
            public long getRegisterId() {
                return 0;
            }

            @Override
            public ObisCode getObisCode() {
                return firmwareObisCode;
            }

            @Override
            public boolean inGroup(long registerGroupId) {
                return false;
            }

            @Override
            public boolean inAtLeastOneGroup(Collection<Long> registerGroupIds) {
                return false;
            }

            @Override
            public Unit getUnit() {
                return Unit.getUndefined();
            }

            @Override
            public String getDeviceMRID() {
                return null;
            }

            @Override
            public String getDeviceSerialNumber() {
                return WebRTUKP.this.getOfflineDevice().getSerialNumber();
            }

            @Override
            public ObisCode getAmrRegisterObisCode() {
                return firmwareObisCode;
            }

            @Override
            public DeviceIdentifier getDeviceIdentifier() {
                return WebRTUKP.this.getOfflineDevice().getDeviceIdentifier();
            }

            @Override
            public ReadingType getReadingType() {
                return null;
            }

            @Override
            public BigDecimal getOverFlowValue() {
                return null;
            }

            @Override
            public boolean isText() {
                return true;
            }
        };
    }

}