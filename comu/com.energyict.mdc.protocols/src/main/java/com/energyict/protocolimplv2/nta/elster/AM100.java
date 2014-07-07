package com.energyict.protocolimplv2.nta.elster;

import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.mdc.dynamic.OptionalPropertySpecFactory;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceFunction;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.DeviceProtocolDialect;
import com.energyict.mdc.protocol.api.ManufacturerInformation;
import com.energyict.mdc.protocol.api.exceptions.CommunicationException;
import com.energyict.mdc.protocol.api.tasks.support.DeviceLoadProfileSupport;
import com.energyict.mdc.protocol.api.tasks.support.DeviceLogBookSupport;
import com.energyict.mdc.protocol.api.tasks.support.DeviceMessageSupport;
import com.energyict.mdc.protocol.api.tasks.support.DeviceRegisterSupport;
import com.energyict.protocolimplv2.common.TempDeviceMessageSupport;
import com.energyict.protocolimplv2.nta.abstractnta.AbstractNtaProtocol;
import com.energyict.protocolimplv2.nta.dsmr23.Dsmr23LogBookFactory;
import com.energyict.protocolimplv2.nta.dsmr23.Dsmr23RegisterFactory;
import com.energyict.protocolimplv2.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.protocols.mdc.protocoltasks.Dsmr23DeviceProtocolDialect;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

/**
 * The AM100 implementation of the NTA spec
 *
 * @author sva
 * @since 30/10/12 (9:58)
 */
public class AM100 extends AbstractNtaProtocol {

    private static final String PROP_FORCEDTOREADCACHE = "ForcedToReadCache";

    private static final Boolean DEFAULT_FORCEDTOREADCACHE = false;

    private DeviceRegisterSupport registerFactory;

    private DeviceLoadProfileSupport loadProfileBuilder;

    private DeviceLogBookSupport logBookFactory;

    @Override
    public AXDRDateTimeDeviationType getDateTimeDeviationType() {
        return AXDRDateTimeDeviationType.Negative;
    }

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced.<br>
     * <br>
     * <p/>
     * The AM100 module does not have the checkConfigParameter in his objectlist, thus to prevent reading the
     * objectlist each time we read the device, we will go for the following approach:<br>
     * 1/ check if the cache exists, if it does exist, go to step 2, if not go to step 3    <br>
     * 2/ is the custom property forcedToReadCache enabled? If yes then go to step 3, else exit    <br>
     * 3/ readout the objectlist    <br>
     */
    @Override
    protected void checkCacheObjects() {
        try {
            if (getDlmsCache() != null) {
                if ((getDlmsCache().getObjectList() == null) || isForcedToReadCache()) {
                    getLogger().log(Level.INFO, isForcedToReadCache() ? "ForcedToReadCache property is true, reading cache!" : "Cache does not exist, configuration is forced to be read.");
                    requestConfiguration();
                    getDlmsCache().saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
                } else {
                    getLogger().log(Level.INFO, "Cache exist, will not be read!");
                }
            } else { // cache does not exist
                setDeviceCache(new DLMSCache());
                getLogger().info("Cache does not exist, configuration is forced to be read.");
                requestConfiguration();
                getDlmsCache().saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
            }
        } catch (IOException e) {
            throw new CommunicationException(e);
        }
    }

    @Override
    public DeviceRegisterSupport getRegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new Dsmr23RegisterFactory(this);
        }
        return registerFactory;
    }

    @Override
    public DeviceLoadProfileSupport getLoadProfileBuilder() {
        if (loadProfileBuilder == null ){
            loadProfileBuilder = new LoadProfileBuilder(this);
        }
        return loadProfileBuilder;
    }

    @Override
    public DeviceLogBookSupport getDeviceLogBookFactory() {
        if (logBookFactory == null) {
            logBookFactory = new Dsmr23LogBookFactory(this);
        }
        return logBookFactory;
    }

    @Override
    public DeviceMessageSupport getMessageProtocol() {
        return new TempDeviceMessageSupport();
    }

    @Override
    public String getProtocolDescription() {
        return "Elster AS220/AS1440 AM100 NTA";
    }

    @Override
    public DeviceFunction getDeviceFunction() {
        return null;
    }

    @Override
    public ManufacturerInformation getManufacturerInformation() {
        return null;
    }

    @Override
    public String getVersion() {
        return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
    }

    @Override
    public List<PropertySpec> getPropertySpecs () {
        List<PropertySpec> optionalSpecs = new ArrayList<>();
        optionalSpecs.add(forcedToReadCachePropertySpec());
        return optionalSpecs;
    }

    private PropertySpec forcedToReadCachePropertySpec() {
        return OptionalPropertySpecFactory.newInstance().booleanPropertySpec(PROP_FORCEDTOREADCACHE);
    }

    private boolean isForcedToReadCache() {
        return (Boolean) getProtocolProperties().getTypedProperties().getProperty(PROP_FORCEDTOREADCACHE, DEFAULT_FORCEDTOREADCACHE);
    }

    @Override
    public List<DeviceProtocolDialect> getDeviceProtocolDialects() {
        List<DeviceProtocolDialect> protocolDialects = new ArrayList<>();
        protocolDialects.add(new Dsmr23DeviceProtocolDialect());
        return protocolDialects;
    }

    @Override
    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_MASTER, DeviceProtocolCapabilities.PROTOCOL_SESSION);
    }

    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        return new ArrayList<>();
    }

    @Override
    public String format(PropertySpec propertySpec, Object messageAttribute) {
        return "";  //Todo change body of implemented methods use File | Settings | File Templates.
    }
}