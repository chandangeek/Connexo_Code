package com.energyict.protocolimplv2.dlms.g3;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.ExceptionResponseException;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.mdc.channels.ComChannelType;
import com.energyict.mdc.exceptions.ComServerExecutionException;
import com.energyict.mdc.messages.DeviceMessageSpec;
import com.energyict.mdc.meterdata.*;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.capabilities.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.mdc.tasks.ConnectionTypeImpl;
import com.energyict.mdc.tasks.DeviceProtocolDialect;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.mdw.offline.OfflineDeviceMessage;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.LogBookReader;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.dlms.g3.G3DeviceInfo;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dialects.NoParamsDeviceProtocolDialect;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.g3.cache.G3Cache;
import com.energyict.protocolimplv2.dlms.g3.events.LogBookFactory;
import com.energyict.protocolimplv2.dlms.g3.profile.ProfileDataFactory;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DConfigurationSupport;
import com.energyict.protocolimplv2.dlms.g3.properties.AS330DProperties;
import com.energyict.protocolimplv2.dlms.g3.registers.RegisterFactory;
import com.energyict.protocolimplv2.identifiers.DeviceIdentifierById;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.security.AS330DSecuritySupport;
import com.energyict.protocolimplv2.security.DeviceProtocolSecurityPropertySetImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Protocol that reads out the G3 e-meter connected to an RTU3 gateway / concentrator (for the G3 international project).
 * <p/>
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 8/06/2015 - 16:05
 */
public class AS330D extends AbstractDlmsProtocol {

    //TODO mirror logical device ID & real logical device ID  + switch between both when necessary
    //TODO Create 2 DlmsSessions, 1 to the mirror logical device, and 1 to the gateway transparent logical device.
    //TODO Both use the same security keys, but a different ServerUpperMacAddress: either MIRROR_LOGICAL_DEVICE_ID or GATEWAY_LOGICAL_DEVICE_ID
    //TODO both sessions need to read out the FC first, this is in the same register

    private static final ObisCode FRAMECOUNTER_OBISCODE = ObisCode.fromString("0.3.43.0.0.255");
    private static final BigDecimal PUBLIC_CLIENT = BigDecimal.valueOf(16);

    private RegisterFactory registerFactory;
    private LogBookFactory logBookFactory;
    private ProfileDataFactory profileDataFactory;

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());

        //If no type is provided, use 'SocketComChannel', resulting in the TCP/IP connection layer.
        if (!comChannel.getProperties().hasValueFor(ComChannelType.TYPE)) {
            TypedProperties comChannelProperties = ConnectionTypeImpl.createTypeProperty(ComChannelType.SocketComChannel);
            comChannel.addProperties(comChannelProperties);
        }

        DlmsSession publicDlmsSession = createPublicDlmsSession(comChannel);
        readFrameCounter(publicDlmsSession);

        setDlmsSession(new DlmsSession(comChannel, getDlmsSessionProperties()));    //Session to the mirror logical device
    }

    private DlmsSession createPublicDlmsSession(ComChannel comChannel) {
        TypedProperties clone = getDlmsSessionProperties().getProperties().clone();
        clone.setProperty(DlmsProtocolProperties.CLIENT_MAC_ADDRESS, PUBLIC_CLIENT);
        AS330DProperties publicClientProperties = new AS330DProperties(false);  //Use gateway logical device ID
        publicClientProperties.addProperties(clone);
        publicClientProperties.setSecurityPropertySet(new DeviceProtocolSecurityPropertySetImpl(0, 0, clone));    //SecurityLevel 0:0

        DlmsSession publicDlmsSession = new DlmsSession(comChannel, publicClientProperties);
        publicDlmsSession.assumeConnected(publicClientProperties.getMaxRecPDUSize(), publicClientProperties.getConformanceBlock());
        return publicDlmsSession;
    }

    /**
     * First read out the frame counter for the management client, using the public client.
     * Note that this happens without setting up an association, since the it's pre-established for the public client.
     * Note that we use the gateway logical device ID for this, because the FC is only in the actual e-meter, not in the mirorred meter data.
     */
    //TODO ok, we now read out the FC using the  gateway logical device ID. Now do this for everything that is not meterdata (and thus not cached in the DC)
    protected void readFrameCounter(DlmsSession publicDlmsSession) {

        //Read out the frame counter using the public client, it has a pre-established association
        long frameCounter;
        try {
            Structure frameCountersStructure = publicDlmsSession.getCosemObjectFactory().getData(FRAMECOUNTER_OBISCODE).getValueAttr().getStructure();
            if (frameCountersStructure != null && frameCountersStructure.nrOfDataTypes() >= 1) {
                frameCounter = frameCountersStructure.getDataType(0).longValue();
            } else {
                frameCounter = new Random().nextInt();
            }
        } catch (DataAccessResultException | ProtocolException e) {
            frameCounter = new Random().nextInt();
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, publicDlmsSession);
        }

        getDlmsSessionProperties().getSecurityProvider().setInitialFrameCounter(frameCounter + 1);
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {   //Physical slave
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public void logOn() {
        connectWithRetries();
        checkCacheObjects();
        //No MBus slave devices
    }

    @Override
    public String getSerialNumber() {
        final G3DeviceInfo g3DeviceInfo = new G3DeviceInfo(getDlmsSession().getCosemObjectFactory());
        try {
            return g3DeviceInfo.getSerialNumber();
        } catch (DataAccessResultException | ProtocolException | ExceptionResponseException e) {
            throw MdcManager.getComServerExceptionFactory().createUnexpectedResponse(e);   //Received error code from the meter, instead of the expected value
        } catch (IOException e) {
            throw MdcManager.getComServerExceptionFactory().createNumberOfRetriesReached(e, getDlmsSessionProperties().getRetries() + 1);
        }
    }

    public AS330DProperties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new AS330DProperties();
        }
        return (AS330DProperties) dlmsProperties;
    }

    @Override
    protected ConfigurationSupport getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new AS330DConfigurationSupport();
        }
        return dlmsConfigurationSupport;
    }

    @Override
    protected DeviceProtocolSecurityCapabilities getSecuritySupport() {
        if (dlmsSecuritySupport == null) {
            dlmsSecuritySupport = new AS330DSecuritySupport();
        }
        return dlmsSecuritySupport;
    }

    /**
     * No slave meters are supported, it's only the e-meter
     */
    @Override
    public CollectedTopology getDeviceTopology() {
        return MdcManager.getCollectedDataFactory().createCollectedTopology(new DeviceIdentifierById(getOfflineDevice().getId()));
    }

    /**
     * Return the updated cache object to the ComServer so it can be stored in the database.
     * At the end of the communication session.
     */
    @Override
    public G3Cache getDeviceCache() {
        if (this.dlmsCache == null || !(this.dlmsCache instanceof G3Cache)) {
            this.dlmsCache = new G3Cache();
        }
        return (G3Cache) this.dlmsCache;
    }

    /**
     * The ComServer framework provides the cache object from the database.
     * At the start of the communication session
     */
    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        if ((deviceProtocolCache != null) && (deviceProtocolCache instanceof G3Cache)) {
            this.dlmsCache = (G3Cache) deviceProtocolCache;
        }
    }

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced
     */
    protected void checkCacheObjects() {
        boolean readCache = getDlmsSessionProperties().isReadCache();
        if ((getDeviceCache().getObjectList() == null) || readCache) {
            getLogger().info("Reading out object list");
            readObjectList();
            getDeviceCache().saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
        } else {
            getLogger().info("Cache exist, object list will not be read!");
        }
        getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDeviceCache().getObjectList());
    }

    /**
     * Add extra retries to the association request.
     * If the request was rejected because by the meter the previous association was still open, this retry mechanism will solve the problem.
     */
    private void connectWithRetries() {

        //TODO revise this release mechanism
        try {
            getDlmsSession().getAso().releaseAssociation();
        } catch (Throwable e) {
            //Ignore
        }
        try {
            getDlmsSession().getAso().releaseAssociation();
        } catch (Throwable e) {
            //Ignore
        }
        getDlmsSession().getAso().setAssociationState(0);


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
    public List<ConnectionType> getSupportedConnectionTypes() {
        return Collections.emptyList();     //This protocol does not manage the connections, it's a physical slave for the RTU3 gateway / DC
    }

    @Override
    public String getProtocolDescription() {
        return "Elster AS330D DLMS (G3 Linky) V2";
    }

    @Override
    public List<CollectedLoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) {
        return getProfileDataFactory().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    @Override
    public List<CollectedLoadProfile> getLoadProfileData(List<LoadProfileReader> loadProfiles) {
        return getProfileDataFactory().getLoadProfileData(loadProfiles);
    }

    private ProfileDataFactory getProfileDataFactory() {
        if (profileDataFactory == null) {
            profileDataFactory = new ProfileDataFactory(getDlmsSession(), getDeviceCache());
        }
        return profileDataFactory;
    }

    @Override
    public List<CollectedLogBook> getLogBookData(List<LogBookReader> logBooks) {
        return getLogBookFactory().getLogBookData(logBooks);
    }

    @Override
    public List<DeviceMessageSpec> getSupportedMessages() {
        return new ArrayList<>();
    }

    @Override
    public CollectedMessageList executePendingMessages(List<OfflineDeviceMessage> pendingMessages) {
        return MdcManager.getCollectedDataFactory().createEmptyCollectedMessageList();
    }

    @Override
    public CollectedMessageList updateSentMessages(List<OfflineDeviceMessage> sentMessages) {
        return MdcManager.getCollectedDataFactory().createEmptyCollectedMessageList();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return "";
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        return Arrays.<DeviceProtocolDialect>asList(new NoParamsDeviceProtocolDialect());   //Dialect properties are managed by the master protocol
    }

    @Override
    public List<CollectedRegister> readRegisters(List<OfflineRegister> registers) {
        return getRegisterFactory().readRegisters(registers);
    }

    private LogBookFactory getLogBookFactory() {
        if (logBookFactory == null) {
            logBookFactory = new LogBookFactory(getDlmsSession());
        }
        return logBookFactory;
    }

    private RegisterFactory getRegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new RegisterFactory(getDlmsSession());
        }
        return registerFactory;
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }
}