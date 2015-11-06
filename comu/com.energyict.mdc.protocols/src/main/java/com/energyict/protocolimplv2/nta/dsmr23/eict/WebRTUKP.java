package com.energyict.protocolimplv2.nta.dsmr23.eict;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.ComChannelType;
import com.energyict.mdc.io.SerialComChannel;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedDataFactory;
import com.energyict.mdc.protocol.api.device.data.CollectedFirmwareVersion;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOnV2;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.protocols.impl.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.protocols.impl.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.protocols.impl.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.protocols.mdc.protocoltasks.TcpDeviceProtocolDialect;

import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.elster.garnet.SerialDeviceProtocolDialect;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;
import com.energyict.protocolimplv2.security.DsmrSecuritySupport;

import javax.inject.Inject;
import javax.inject.Provider;
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
 * <p/>
 * Copyrights EnergyICT
 * Date: 18/10/13
 * Time: 11:40
 * Author: khe
 */
public class WebRTUKP extends AbstractDlmsProtocol {

    private final Thesaurus thesaurus;

    @Inject
    public WebRTUKP(
            Clock clock, Thesaurus thesaurus, PropertySpecService propertySpecService, SocketService socketService,
            SerialComponentService serialComponentService, IssueService issueService,
            TopologyService topologyService, MdcReadingTypeUtilService readingTypeUtilService,
            IdentificationService identificationService, CollectedDataFactory collectedDataFactory,
            MeteringService meteringService, LoadProfileFactory loadProfileFactory,
            Provider<DsmrSecuritySupport> dsmrSecuritySupportProvider) {
        super(clock, propertySpecService, socketService, serialComponentService, issueService, topologyService,
                readingTypeUtilService, identificationService, collectedDataFactory, meteringService, loadProfileFactory,
                dsmrSecuritySupportProvider);
        this.thesaurus = thesaurus;
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
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Arrays.asList(
            new OutboundTcpIpConnectionType(this.thesaurus, getPropertySpecService(), getSocketService()),
            new SioOpticalConnectionType(getSerialComponentService(), this.thesaurus),
            new RxTxOpticalConnectionType(getSerialComponentService(), this.thesaurus));
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
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return getDsmr23Messaging().format(propertySpec, messageAttribute);
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new SerialDeviceProtocolDialect(getPropertySpecService()), new TcpDeviceProtocolDialect(getPropertySpecService()));
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
        return "EnergyICT WebRTU KP DLMS (NTA DSMR2.3)";
    }

    @Override
    public CollectedFirmwareVersion getFirmwareVersions() {
        CollectedFirmwareVersion firmwareVersionsCollectedData = getCollectedDataFactory().createFirmwareVersionsCollectedData(this.offlineDevice.getDeviceIdentifier());
        List<CollectedRegister> collectedRegisters = getRegisterFactory().readRegisters(Collections.singletonList(getFirmwareRegister()));
        firmwareVersionsCollectedData.setActiveMeterFirmwareVersion(collectedRegisters.get(0).getText());
        return firmwareVersionsCollectedData;
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
            public DeviceIdentifier<?> getDeviceIdentifier() {
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
