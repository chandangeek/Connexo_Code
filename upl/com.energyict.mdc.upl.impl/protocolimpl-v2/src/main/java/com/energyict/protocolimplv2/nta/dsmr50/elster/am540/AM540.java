package com.energyict.protocolimplv2.nta.dsmr50.elster.am540;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connection.HHUSignOnV2;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.common.DlmsProtocolProperties;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.channels.serial.optical.rxtx.RxTxOpticalConnectionType;
import com.energyict.mdc.channels.serial.optical.serialio.SioOpticalConnectionType;
import com.energyict.mdc.exceptions.ComServerExecutionException;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.CollectedLoadProfile;
import com.energyict.mdc.meterdata.CollectedLoadProfileConfiguration;
import com.energyict.mdc.meterdata.CollectedLogBook;
import com.energyict.mdc.meterdata.CollectedMessageList;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.v2migration.MigrateFromV1Protocol;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdc.tasks.SerialDeviceProtocolDialect;
import com.energyict.mdc.tasks.TcpDeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocolimpl.dlms.idis.AM540ObjectList;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.idis.topology.IDISMeterTopology;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.protocolimplv2.nta.dsmr40.common.profiles.Dsmr40LoadProfileBuilder;
import com.energyict.protocolimplv2.nta.dsmr50.Dsmr50ConfigurationSupport;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.logbooks.Dsmr50LogBookFactory;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.messages.AM540MessageExecutor;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.messages.AM540Messaging;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.profiles.AM540LoadProfileBuilder;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.registers.Dsmr50RegisterFactory;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40Properties;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.AM540Cache;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * V2 version of the AM540 protocol.
 * This version adds breaker & relais support and other IDIS features.
 *
 * @author khe
 * @since 17/12/2014 - 14:30
 */
public class AM540 extends AbstractDlmsProtocol implements MigrateFromV1Protocol {

    private Dsmr50LogBookFactory dsmr50LogBookFactory;
    private AM540Messaging am540Messaging;
    private long initialFrameCounter = -1;
    private IDISMeterTopology meterTopology;
    private LoadProfileBuilder loadProfileBuilder;
    private Dsmr50RegisterFactory registerFactory;

    public AM540() {
        super();
    }

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());

        HHUSignOnV2 hhuSignOn = null;
        if (ComChannelType.SerialComChannel.is(comChannel) || ComChannelType.OpticalComChannel.is(comChannel)) {
            hhuSignOn = getHHUSignOn((SerialPortComChannel) comChannel);
        }
        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties(), hhuSignOn, "P07210"));
    }

    private HHUSignOnV2 getHHUSignOn(SerialPortComChannel serialPortComChannel) {
        HHUSignOnV2 hhuSignOn = new IEC1107HHUSignOn(serialPortComChannel, getDlmsSessionProperties());
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(false);
        return hhuSignOn;
    }

    /**
     * Connect to the device and check the cached object list.
     */
    @Override
    public void logOn() {
        connectWithRetries();
        checkCacheObjects();
        if (!getOfflineDevice().getAllSlaveDevices().isEmpty()) {
            getMeterTopology().searchForSlaveDevices();
        }
    }

    @Override
    public DLMSCache getDeviceCache() {
        if (this.dlmsCache == null || !(this.dlmsCache instanceof AM540Cache)) {
            this.dlmsCache = new AM540Cache();
        }
        ((AM540Cache) this.dlmsCache).setFrameCounter(getDlmsSession().getAso().getSecurityContext().getFrameCounter() + 1);     //Save this for the next session
        return this.dlmsCache;
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        if ((deviceProtocolCache != null) && (deviceProtocolCache instanceof AM540Cache)) {
            this.dlmsCache = (AM540Cache) deviceProtocolCache;
            this.initialFrameCounter = ((AM540Cache) this.dlmsCache).getFrameCounter();
        }
    }

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced
     */
    protected void checkCacheObjects() {
        boolean readCache = getDlmsSessionProperties().isReadCache();
        if ((((DLMSCache) getDeviceCache()).getObjectList() == null) || (readCache)) {
            if (readCache) {
                getLogger().info("ForcedToReadCache property is true, reading cache!");
                readObjectList();
                ((DLMSCache) getDeviceCache()).saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
            } else {
                getLogger().info("Cache does not exist, using hardcoded copy of object list");
                UniversalObject[] objectList = new AM540ObjectList().getObjectList();
                ((DLMSCache) getDeviceCache()).saveObjectList(objectList);
            }
        } else {
            getLogger().info("Cache exist, will not be read!");
        }
        getDlmsSession().getMeterConfig().setInstantiatedObjectList(((DLMSCache) getDeviceCache()).getObjectList());
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        super.setSecurityPropertySet(deviceProtocolSecurityPropertySet);
        this.getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(initialFrameCounter == -1 ? 1 : initialFrameCounter);    //Set the frameCounter from last session (which has been loaded from cache)
    }

    public Dsmr50Properties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new Dsmr50Properties();
        }
        return (Dsmr50Properties) dlmsProperties;
    }

    /**
     * Add extra retries to the association request.
     * If the request was rejected because by the meter the previous association was still open, this retry mechanism will solve the problem.
     */
    private void connectWithRetries() {
        int tries = 0;
        while (true) {
            ComServerExecutionException exception;
            try {
                if (getDlmsSession().getAso().getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
                    getDlmsSession().createAssociation((int) getDlmsSessionProperties().getAARQTimeout());
                }
                return;
            } catch (ComServerExecutionException e) {
                if (e.getCause() != null && e.getCause() instanceof DataAccessResultException) {
                    throw e;        //Throw real errors, e.g. unsupported security mechanism, wrong password...
                } else if (MdcManager.getComServerExceptionFactory().isConnectionCommunicationException(e)) {
                    throw e;
                }
                exception = e;
            }

            //Release and retry the AARQ in case of ACSE exception
            if (++tries > getDlmsSessionProperties().getAARQRetries()) {
                getLogger().severe("Unable to establish association after [" + tries + "/" + (getDlmsSessionProperties().getAARQRetries() + 1) + "] tries.");
                throw MdcManager.getComServerExceptionFactory().createProtocolConnectFailed(exception);
            } else {
                getLogger().info("Unable to establish association after [" + tries + "/" + (getDlmsSessionProperties().getAARQRetries() + 1) + "] tries. Sending RLRQ and retry ...");
                try {
                    getDlmsSession().getAso().releaseAssociation();
                } catch (ComServerExecutionException e) {
                    // Absorb exception: in 99% of the cases we expect an exception here ...
                }
                getDlmsSession().getAso().setAssociationState(ApplicationServiceObject.ASSOCIATION_DISCONNECTED);
            }
        }
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = new ArrayList<>();
        result.add(new SioOpticalConnectionType());
        result.add(new RxTxOpticalConnectionType());
        return result;
    }

    /**
     * A collection of general DSMR50 properties.
     * These properties are not related to the security or the protocol dialects.
     */
    protected ConfigurationSupport getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new Dsmr50ConfigurationSupport();
        }
        return dlmsConfigurationSupport;
    }

    @Override
    public String getProtocolDescription() {
        return "Elster AM540 DLMS (NTA DSMR5.0) V2";
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfileReaders) {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfileReaders);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfileReaders) {
        return getLoadProfileBuilder().getLoadProfileData(loadProfileReaders);
    }

    protected LoadProfileBuilder getLoadProfileBuilder() {
        if (this.loadProfileBuilder == null) {
            this.loadProfileBuilder = new AM540LoadProfileBuilder(this);
            ((Dsmr40LoadProfileBuilder) loadProfileBuilder).setCumulativeCaptureTimeChannel(getDlmsSessionProperties().isCumulativeCaptureTimeChannel());
        }
        return loadProfileBuilder;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBookReaders) {
        return getDsmr50LogBookFactory().getLogBookData(logBookReaders);
    }

    private Dsmr50LogBookFactory getDsmr50LogBookFactory() {
        if (dsmr50LogBookFactory == null) {
            dsmr50LogBookFactory = new Dsmr50LogBookFactory(this);
        }
        return dsmr50LogBookFactory;
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return getAM540Messaging().getSupportedMessages();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> list) {
        return getAM540Messaging().executePendingMessages(list);
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> list) {
        return getAM540Messaging().updateSentMessages(list);
    }

    @Override
    public String format(PropertySpec propertySpec, Object o) {
        return getAM540Messaging().format(propertySpec, o);
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new SerialDeviceProtocolDialect(), new TcpDeviceProtocolDialect());
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterFactory().readRegisters(registers);
    }

    private Dsmr50RegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new Dsmr50RegisterFactory(this);
        }
        return registerFactory;
    }

    public AM540Messaging getAM540Messaging() {
        if (this.am540Messaging == null) {
            this.am540Messaging = new AM540Messaging(new AM540MessageExecutor(this));
        }
        return this.am540Messaging;
    }

    @Override
    public String getSerialNumber() {
        if (getDlmsSessionProperties().useEquipmentIdentifierAsSerialNumber()) {
            return getMeterInfo().getEquipmentIdentifier();
        } else {
            return getMeterInfo().getSerialNr();
        }
    }

    @Override
    public IDISMeterTopology getMeterTopology() {
        if (meterTopology == null) {
            meterTopology = new IDISMeterTopology(this);
        }
        return meterTopology;
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public TypedProperties formatLegacyProperties(TypedProperties legacyProperties) {
        TypedProperties result = TypedProperties.empty();

        // Map 'ServerMacAddress' to 'ServerUpperMacAddress' and 'ServerLowerMacAddress'
        Object serverMacAddress = legacyProperties.getProperty(DlmsProtocolProperties.SERVER_MAC_ADDRESS);
        if (serverMacAddress != null) {
            String[] macAddress = ((String) serverMacAddress).split(":");
            if (macAddress.length >= 1) {
                String upperMacAddress = macAddress[0];
                if (upperMacAddress.toLowerCase().equals("x")) {
                    result.setProperty(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, new BigDecimal(-1));
                } else {
                    result.setProperty(DlmsProtocolProperties.SERVER_UPPER_MAC_ADDRESS, mapToBigDecimal(upperMacAddress));
                }
            }

            if (macAddress.length >= 2) {
                String lowerMacAddress = macAddress[1];
                if (lowerMacAddress.toLowerCase().equals("x")) {
                    result.setProperty(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, new BigDecimal(-1));
                } else {
                    result.setProperty(DlmsProtocolProperties.SERVER_LOWER_MAC_ADDRESS, mapToBigDecimal(lowerMacAddress));
                }
            }
        }

        // Map 'ForcedToReadCache' to 'ReadCache'
        Object readCache = legacyProperties.getProperty(Dsmr40Properties.PROPERTY_FORCED_TO_READ_CACHE);
        if (readCache != null) {
            result.setProperty(Dsmr50Properties.READCACHE_PROPERTY, ProtocolTools.getBooleanFromString((String) readCache));
        }

        return result;
    }

    private BigDecimal mapToBigDecimal(String text) {
        try {
            return new BigDecimal(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}