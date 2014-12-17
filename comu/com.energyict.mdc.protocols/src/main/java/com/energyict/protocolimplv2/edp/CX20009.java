package com.energyict.protocolimplv2.edp;

import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.io.ComChannel;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.io.SerialComponentService;
import com.energyict.mdc.io.SocketService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.CollectedDataFactoryProvider;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.LoadProfileReader;
import com.energyict.mdc.protocol.api.LogBookReader;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfileConfiguration;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedTopology;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.device.offline.OfflineDeviceMessage;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.protocol.api.messaging.DeviceMessageId;

import com.elster.jupiter.properties.PropertySpec;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.protocolimplv2.edp.logbooks.LogbookReader;
import com.energyict.protocolimplv2.edp.messages.EDPMessageExecutor;
import com.energyict.protocolimplv2.edp.messages.EDPMessaging;
import com.energyict.protocolimplv2.edp.registers.RegisterReader;
import com.energyict.protocolimplv2.elster.garnet.TcpDeviceProtocolDialect;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractDlmsProtocol;
import com.energyict.protocols.impl.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.protocols.impl.channels.serial.direct.rxtx.RxTxPlainSerialConnectionType;
import com.energyict.protocols.impl.channels.serial.direct.serialio.SioPlainSerialConnectionType;
import com.energyict.protocols.mdc.protocoltasks.EDPSerialDeviceProtocolDialect;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Copyrights EnergyICT
 * Date: 5/02/14
 * Time: 11:34
 * Author: khe
 */
public class CX20009 extends AbstractDlmsProtocol {

    private LogbookReader logbookReader = null;
    private RegisterReader registerReader;
    private EDPMessaging edpMessaging;

    @Inject
    public CX20009(PropertySpecService propertySpecService, SocketService socketService, SerialComponentService serialComponentService, IssueService issueService, TopologyService topologyService, MdcReadingTypeUtilService readingTypeUtilService, IdentificationService identificationService) {
        super(propertySpecService, socketService, serialComponentService, issueService, topologyService, readingTypeUtilService, identificationService);
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsProperties().setSerialNumber(offlineDevice.getSerialNumber());
        setDlmsSession(new DlmsSession(comChannel, getDlmsProperties()));
    }

    /**
     * Class that holds all DLMS device properties (general, dialect & security related)
     */
    @Override
    protected EDPProperties getDlmsProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new EDPProperties(getPropertySpecService());
        }
        return (EDPProperties) dlmsProperties;
    }

    @Override
    public void logOn() {
        try {
            getDlmsSession().connect();
            checkCacheObjects();
        } catch (CommunicationException e) {
            logOff();
            getDlmsSession().connect();
            checkCacheObjects();
        }
    }

    @Override
    protected void checkCacheObjects() {
        if (getDeviceCache() == null) {
            setDeviceCache(new DLMSCache());
        }
        DLMSCache dlmsCache = (DLMSCache) getDeviceCache();
        if (dlmsCache.getObjectList() == null || getDlmsProperties().isReadCache()) {
            readObjectList();
            dlmsCache.saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());  // save object list in cache
        } else {
            getDlmsSession().getMeterConfig().setInstantiatedObjectList(dlmsCache.getObjectList());
        }
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-06-02 13:26:25 +0200 (Mon, 02 Jun 2014) $";
    }

    /**
     * Direct electrical communication (RS485 interface) or TCP/IP (GPRS modem)
     */
    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = new ArrayList<>();
        result.add(new OutboundTcpIpConnectionType(getPropertySpecService(), getSocketService()));
        result.add(new SioPlainSerialConnectionType(getSerialComponentService()));
        result.add(new RxTxPlainSerialConnectionType(getSerialComponentService()));
        return result;
    }

    /**
     * Only disconnect the connection layer, the meter does not support release requests
     */
    @Override
    public void logOff() {
        if (getDlmsSession() != null) {
            getDlmsSession().getDlmsV2Connection().disconnectMAC();
        }
    }

    @Override
    public String getProtocolDescription() {
        return "SagemCom CX2000-9 DLMS";
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_SESSION);
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
        return getLogbookReader().getLogBookData(logBooks);
    }

    @Override
    public Set<DeviceMessageId> getSupportedMessages() {
        return getMessaging().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return getMessaging().executePendingMessages(pendingMessages);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return getMessaging().updateSentMessages(sentMessages);
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return getMessaging().format(propertySpec, messageAttribute);
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new EDPSerialDeviceProtocolDialect(getPropertySpecService()), new TcpDeviceProtocolDialect(getPropertySpecService()));
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterReader().readRegisters(registers);
    }

    /**
     * No slave meters are supported, it's only the e-meter
     */
    @Override
    public CollectedTopology getDeviceTopology() {
        return CollectedDataFactoryProvider.instance.get().getCollectedDataFactory().createCollectedTopology(getOfflineDevice().getDeviceIdentifier());
    }

    private LogbookReader getLogbookReader() {
        if (logbookReader == null) {
            logbookReader = new LogbookReader(this, this.getIssueService());
        }
        return logbookReader;
    }

    private EDPMessaging getMessaging() {
        if (edpMessaging == null) {
            edpMessaging = new EDPMessaging(new EDPMessageExecutor(this, this.getIssueService(), this.getReadingTypeUtilService()));
        }
        return edpMessaging;
    }

    public RegisterReader getRegisterReader() {
        if (registerReader == null) {
            registerReader = new RegisterReader(this, this.getIssueService());
        }
        return registerReader;
    }
}