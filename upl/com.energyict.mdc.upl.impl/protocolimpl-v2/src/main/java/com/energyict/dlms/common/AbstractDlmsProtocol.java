package com.energyict.dlms.common;

import com.energyict.cpo.PropertySpec;
import com.energyict.cpo.TypedProperties;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.protocolimplv2.DlmsSession;
import com.energyict.dlms.protocolimplv2.DlmsSessionProperties;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.protocol.DeviceProtocol;
import com.energyict.mdc.protocol.DeviceProtocolCache;
import com.energyict.mdc.protocol.security.AuthenticationDeviceAccessLevel;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityCapabilities;
import com.energyict.mdc.protocol.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.security.EncryptionDeviceAccessLevel;
import com.energyict.mdw.offline.OfflineDevice;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.security.DlmsSecuritySupport;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Abstract implementation of a DLMS DeviceProtocol.
 *
 * @author: sva
 * @since: 30/10/12 (10:13)
 */
public abstract class AbstractDlmsProtocol implements DeviceProtocol, HHUEnabler {

    private final DeviceProtocolSecurityCapabilities securityCapabilities = new DlmsSecuritySupport();

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

    protected abstract DlmsSessionProperties getDlmsSessionProperties();

    /**
     * Initialization method right after we are connected to the physical device.
     */
    protected abstract void initAfterConnect();

    @Override
    public void init(OfflineDevice offlineDevice, ComChannel comChannel) {
        this.offlineDevice = offlineDevice;
        getDlmsSessionProperties().setSerialNumber(offlineDevice.getSerialNumber());
        this.comChannel = comChannel;
    }

    @Override
    public void logOn() {
        getDlmsSession().connect();
        checkCacheObjects();
        initAfterConnect();
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
     */
    public abstract int requestConfigurationChanges();

    /**
     * Request Association buffer list out of the meter.
     */
    protected void requestConfiguration() {
        try {
            if (getDlmsSession().getReference() == ProtocolLink.LN_REFERENCE) {
                getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDlmsSession().getCosemObjectFactory().getAssociationLN().getBuffer());
            } else if (getDlmsSession().getReference() == ProtocolLink.SN_REFERENCE) {
                getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDlmsSession().getCosemObjectFactory().getAssociationSN().getBuffer());
            } else {
                throw new IllegalArgumentException("Invalid reference method, only 0 and 1 are allowed.");
            }
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, getDlmsSession());
        }
    }

    @Override
    public void setTime(Date timeToSet) {
        try {
            getDlmsSession().getCosemObjectFactory().getClock().setAXDRDateTimeAttr(new AXDRDateTime(timeToSet));
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, this.getDlmsSession());
        }
    }

    @Override
    public Date getTime() {
        try {
            return getDlmsSession().getCosemObjectFactory().getClock().getDateTime();
        } catch (IOException e) {
            throw IOExceptionHandler.handle(e, this.getDlmsSession());
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
        return getDlmsSessionProperties().isBulkRequest();
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
        getDlmsSessionProperties().addProperties(dialectProperties); // this will add the properties to the existing properties
    }

    @Override
    public void addProperties(TypedProperties properties) {
        getDlmsSessionProperties().addProperties(properties); // this will add the properties to the existing properties
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
            dlmsSession = new DlmsSession(comChannel, getDlmsSessionProperties());
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
        return getDlmsSessionProperties().getTimeZone();
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
