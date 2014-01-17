package com.energyict.dlms.common;

import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpec;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.HHUEnabler;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;
import com.energyict.mdc.protocol.api.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.security.EncryptionDeviceAccessLevel;
import com.energyict.protocolimplv2.comchannels.ComChannelInputStreamAdapter;
import com.energyict.protocolimplv2.comchannels.ComChannelOutputStreamAdapter;
import com.energyict.protocolimplv2.security.DlmsSecuritySupport;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Abstract implementation of a DLMS DeviceProtocol.
 *
 * @author sva
 * @since 30/10/12 (10:13)
 */
public abstract class AbstractDlmsProtocol implements DeviceProtocol, HHUEnabler {

    private PropertySpecService propertySpecService;
    private DeviceProtocolSecurityCapabilities securityCapabilities;

    /**
     * The used {@link com.energyict.dlms.DlmsSession}
     */
    protected DlmsSession dlmsSession;

    /**
     * The {@link DLMSCache} of the current RTU
     */
    private DLMSCache dlmsCache;

    /**
     * The offline rtu
     */
    private OfflineDevice offlineDevice;

    /**
     * The ComChannel
     */
    private ComChannel comChannel;

    private Logger logger;

    /**
     * Get the firmware version of the meter
     *
     * @return the version of the meter firmware
     */
    protected abstract String getFirmwareVersion();

    protected abstract DlmsProtocolProperties getProtocolProperties();

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    @Override
    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
        this.securityCapabilities = new DlmsSecuritySupport(propertySpecService);
    }

    /**
     * Initialization method right after we are connected to the physical device.
     */
    protected abstract void initAfterConnect();

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        this.comChannel = comChannel;
    }

    @Override
    public void logOn() {
        try {
            getDlmsSession().connect();
            checkCacheObjects();
            initAfterConnect();
        } catch (IOException e) {
            throw CommunicationException.protocolConnectFailed(e);
        }
    }

    @Override
    public void daisyChainedLogOn() {
        logOn();
    }

    @Override
    public void terminate() {
    }

    @Override
    public void logOff() {
        getDlmsSession().disconnect();
    }

    @Override
    public void daisyChainedLogOff() {
        logOff();
    }

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced
     */
    protected void checkCacheObjects() {
        int configNumber = -1;
        boolean changed = false;
        try {
            if (this.dlmsCache != null && this.dlmsCache.getObjectList() != null) { // the dlmsCache exists
                getDlmsSession().getMeterConfig().setInstantiatedObjectList(this.dlmsCache.getObjectList());

                getLogger().info("Checking the configuration parameters.");
                configNumber = requestConfigurationChanges();

                if (this.dlmsCache.getConfProgChange() != configNumber) {
                    getLogger().info("Meter configuration has changed, configuration is forced to be read.");
                    requestConfiguration();
                    changed = true;
                }

            } else { // cache does not exist
                this.dlmsCache = new DLMSCache();
                getLogger().info("Cache does not exist, configuration is forced to be read.");
                requestConfiguration();
                configNumber = requestConfigurationChanges();
                changed = true;
            }
        } catch (IOException e) {
            throw new CommunicationException(e);
        } finally {
            if (changed) {
                this.dlmsCache.saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
                this.dlmsCache.setConfProgChange(configNumber);
            }
        }
    }

    /**
     * Read the number of configuration changes in the meter
     * The number should increase if something in the configuration or firmware changed. This can cause the objectlist to change.
     * <br>
     * <i>This method may be overridden to fetch the version in a getWithListRequest</i>
     *
     * @return the number of configuration changes.
     * @throws IOException
     */
    public int requestConfigurationChanges() throws IOException {
        try {
            return (int) getDlmsSession().getCosemObjectFactory().getCosemObject(getDlmsSession().getMeterConfig().getConfigObject().getObisCode()).getValue();
        } catch (IOException e) {
            getLogger().log(Level.FINEST, e.getMessage());
            throw new IOException("Could not retrieve the configuration change parameter" + e);
        }
    }

    /**
     * Request Association buffer list out of the meter.
     *
     * @throws IOException if something fails during the request or the parsing of the buffer
     */
    protected void requestConfiguration() throws IOException {

        try {
            if (getDlmsSession().getReference() == ProtocolLink.LN_REFERENCE) {
                getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDlmsSession().getCosemObjectFactory().getAssociationLN().getBuffer());
            } else if (getDlmsSession().getReference() == ProtocolLink.SN_REFERENCE) {
                getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDlmsSession().getCosemObjectFactory().getAssociationSN().getBuffer());
            } else {
                throw new IllegalArgumentException("Invalid reference method, only 0 and 1 are allowed.");
            }
        } catch (IOException e) {
            getLogger().log(Level.FINEST, e.getMessage());
            throw new IOException("Requesting configuration failed." + e);
        }
    }

    @Override
    public void setTime(Date timeToSet) {
        try {
            getDlmsSession().getCosemObjectFactory().getClock().setAXDRDateTimeAttr(new AXDRDateTime(timeToSet));
        } catch (IOException e) {
            getLogger().log(Level.FINEST, e.getMessage());
            throw new CommunicationException(e);
        }
    }

    @Override
    public Date getTime() {
        try {
            return getDlmsSession().getCosemObjectFactory().getClock().getDateTime();
        } catch (IOException e) {
            getLogger().log(Level.FINEST, e.getMessage());
            throw new CommunicationException(e);
        }
    }

    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        this.dlmsCache = (DLMSCache) deviceProtocolCache;
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        return dlmsCache;
    }

    /**
     * Tests if the Device wants to use the bulkRequests
     *
     * @return true if the Device wants to use BulkRequests, false otherwise
     */
    public boolean supportsBulkRequests() {
        return getProtocolProperties().isBulkRequest();
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        enableHHUSignOn(commChannel, false);
    }

    /**
     * @param commChannel       The SerialCommunicationChannel
     * @param enableDataReadout enable or disable the data readout
     * @throws ConnectionException
     * @Override This is an empty implementation, and you should override this method if you're using the HHUSignon
     */
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean enableDataReadout) throws ConnectionException {
    }

    /**
     * @return empty byte array (new byte[0])
     * @Override Override this method to use the HHUData readout
     */
    public byte[] getHHUDataReadout() {
        return new byte[0];
    }

    @Override
    public void addDeviceProtocolDialectProperties(TypedProperties dialectProperties) {
        getProtocolProperties().addProperties(dialectProperties); // this will add the properties to the existing properties
    }

    @Override
    public void copyProperties(TypedProperties properties) {
        getProtocolProperties().addProperties(properties); // this will add the properties to the existing properties
    }

    /**
     * Get the protocol logger, or create one if not initialized yet.
     *
     * @return
     */
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    public DlmsSession getDlmsSession() {
        if (dlmsSession == null) {
            dlmsSession = new DlmsSession(new ComChannelInputStreamAdapter(comChannel),
                    new ComChannelOutputStreamAdapter(comChannel),
                    getLogger(),
                    getProtocolProperties(),
                    getTimeZone());
        }
        return dlmsSession;
    }

    public DLMSCache getDlmsCache() {
        return dlmsCache;
    }

    public OfflineDevice getOfflineDevice() {
        return offlineDevice;
    }

    public ComChannel getComChannel() {
        return comChannel;
    }

    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone(getProtocolProperties().getTimeZone());
    }

    @Override
    public void setSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        //TODO provide proper functionality so your protocol can make proper use of the security properties
    }

    @Override
    public final List<PropertySpec> getSecurityProperties() {
        return securityCapabilities.getSecurityProperties();
    }

    @Override
    public final String getSecurityRelationTypeName() {
        return securityCapabilities.getSecurityRelationTypeName();
    }

    @Override
    public final List<AuthenticationDeviceAccessLevel> getAuthenticationAccessLevels() {
        return securityCapabilities.getAuthenticationAccessLevels();
    }

    @Override
    public final List<EncryptionDeviceAccessLevel> getEncryptionAccessLevels() {
        return securityCapabilities.getEncryptionAccessLevels();
    }

    @Override
    public final PropertySpec getSecurityPropertySpec(String name) {
        return securityCapabilities.getSecurityPropertySpec(name);
    }

}
