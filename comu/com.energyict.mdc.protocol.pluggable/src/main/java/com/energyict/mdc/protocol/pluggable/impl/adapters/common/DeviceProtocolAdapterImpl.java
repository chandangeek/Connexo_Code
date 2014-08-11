package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.energyict.dlms.DLMSCache;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.OptionalPropertySpecFactory;
import com.energyict.mdc.dynamic.PropertySpecFactory;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.dynamic.RequiredPropertySpecFactory;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolAdapter;
import com.energyict.mdc.protocol.api.DeviceProtocolCache;
import com.energyict.mdc.protocol.api.DeviceProtocolCapabilities;
import com.energyict.mdc.protocol.api.HHUEnabler;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.protocol.api.legacy.CachingProtocol;
import com.energyict.mdc.protocol.api.legacy.DeviceCachingSupport;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.mdc.protocol.api.legacy.SmartMeterProtocol;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.protocol.api.services.DeviceCacheMarshallingService;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Abstract adapter class that can provide general functionality for the {@link SmartMeterProtocol} and {@link MeterProtocol}
 * <p/>
 * Copyrights EnergyICT
 * Date: 31/08/12
 * Time: 14:48
 */
public abstract class DeviceProtocolAdapterImpl implements DeviceProtocolAdapter, DeviceCachingSupport {

    public static final String DEVICE_TIMEZONE_PROPERTY_NAME = "deviceTimeZone";
    public static final String CALL_HOME_ID_PROPERTY_NAME = "callHomeId";
    public static final String NETWORK_ID_PROPERTY_NAME = "networkId";

    private DataModel dataModel;
    private final DeviceCacheMarshallingService deviceCacheMarshallingService;
    private PropertySpecService propertySpecService;
    private ProtocolPluggableService protocolPluggableService;
    private SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory;

    /**
     * Gets the instance of the {@link CachingProtocol}
     *
     * @return the cachingProtocol
     */
    public abstract CachingProtocol getCachingProtocol();

    /**
     * Gets the instance of the {@link HHUEnabler}
     *
     * @return the hhuEnabler
     */
    public abstract HHUEnabler getHhuEnabler();

    protected DeviceProtocolAdapterImpl(ProtocolPluggableService protocolPluggableService, SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory, DataModel dataModel, DeviceCacheMarshallingService deviceCacheMarshallingService) {
        super();
        this.protocolPluggableService = protocolPluggableService;
        this.securitySupportAdapterMappingFactory = securitySupportAdapterMappingFactory;
        this.dataModel = dataModel;
        this.deviceCacheMarshallingService = deviceCacheMarshallingService;
    }

    public void setPropertySpecService(PropertySpecService propertySpecService) {
        this.propertySpecService = propertySpecService;
    }

    protected PropertySpecService getPropertySpecService() {
        return propertySpecService;
    }

    protected ProtocolPluggableService getProtocolPluggableService() {
        return protocolPluggableService;
    }

    protected SecuritySupportAdapterMappingFactory getSecuritySupportAdapterMappingFactory() {
        return securitySupportAdapterMappingFactory;
    }

    protected DataModel getDataModel() {
        return dataModel;
    }

    protected List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionTypePluggableClass> connectionTypePluggableClasses = this.getProtocolPluggableService().findAllConnectionTypePluggableClasses();
        List<ConnectionType> connectionTypes = new ArrayList<>(connectionTypePluggableClasses.size());
        for (ConnectionTypePluggableClass connectionTypePluggableClass : connectionTypePluggableClasses) {
            connectionTypes.add(connectionTypePluggableClass.getConnectionType());
        }
        return connectionTypes;
    }

    @Override
    public void setCache(Object cacheObject) {
        if(cacheObject != null && cacheObject instanceof DLMSCache){
            ((DLMSCache) cacheObject).setChanged(false);
        }
        getCachingProtocol().setCache(cacheObject);
    }

    @Override
    public Object getCache() {
        return getCachingProtocol().getCache();
    }

    @Override
    public Object fetchCache(int deviceId) throws SQLException, BusinessException {

        /*
       This method will never get called. All cache objects will be fetched during initialization of the task
        */

        return getCachingProtocol().fetchCache(deviceId);
    }

    @Override
    public void updateCache(int deviceId, Object cacheObject) throws SQLException, BusinessException {

        /*
       This method will never get called. All cache objects will be fetched during initialization of the task
        */

        getCachingProtocol().updateCache(deviceId, cacheObject);
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel) throws ConnectionException {
        if (getHhuEnabler() != null) {
            getHhuEnabler().enableHHUSignOn(commChannel);
        }
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean enableDataReadout) throws ConnectionException {
        if (getHhuEnabler() != null) {
            getHhuEnabler().enableHHUSignOn(commChannel, enableDataReadout);
        }
    }

    @Override
    public byte[] getHHUDataReadout() {
        if (getHhuEnabler() != null) {
            return getHhuEnabler().getHHUDataReadout();
        }
        return new byte[0];
    }


    @Override
    public void setDeviceCache(DeviceProtocolCache deviceProtocolCache) {
        if (deviceProtocolCache instanceof DeviceProtocolCacheAdapter) {
            DeviceProtocolCacheAdapter deviceCacheAdapter = (DeviceProtocolCacheAdapter) deviceProtocolCache;
            setCache(deviceCacheMarshallingService.unMarshallCache(deviceCacheAdapter.getLegacyJsonCache()));
        }
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        DeviceProtocolCacheAdapter deviceCacheAdapter = new DeviceProtocolCacheAdapter();
        String legacyJsonCache = deviceCacheMarshallingService.marshall(getCache());
        deviceCacheAdapter.setLegacyJsonCache(legacyJsonCache);
        return deviceCacheAdapter;
    }

    protected List<PropertySpec> getAdapterOptionalProperties() {
        List<PropertySpec> propertySpecs = new ArrayList<>();
        propertySpecs.add(this.deviceTimeZonePropertySpec(false));
        propertySpecs.add(this.nodeAddressPropertySpec(false));
        propertySpecs.add(this.deviceIdPropertySpec(false));
        propertySpecs.add(this.callHomeIdPropertySpec(false));
        return propertySpecs;
    }

    public List<DeviceProtocolCapabilities> getDeviceProtocolCapabilities() {
        CapabilityAdapterMappingFactory factory = new CapabilityAdapterMappingFactoryImpl(this.dataModel);
        Integer mapping = factory.getCapabilitiesMappingForDeviceProtocol(getProtocolClass().getCanonicalName());
        if (mapping != null) {
            return getCapabilitesListFromFlags(mapping);
        }
        else {
            return Arrays.asList(DeviceProtocolCapabilities.PROTOCOL_SESSION);  //Default, if there's no mapping available
        }
    }

    /**
     * Either the class of the MeterProtocol or the SmartMeterProtocol instance, depending on the implementation
     */
    protected abstract Class getProtocolClass();

    /**
     * Bit0: slave capability
     * Bit1: session capability
     * Bit2: master capability
     */
    private List<DeviceProtocolCapabilities> getCapabilitesListFromFlags(Integer mapping) {
        List<DeviceProtocolCapabilities> result = new ArrayList<>();
        if ((mapping & 0x01) == 0x01) {
            result.add(DeviceProtocolCapabilities.PROTOCOL_SLAVE);
        }
        if ((mapping & 0x02) == 0x02) {
            result.add(DeviceProtocolCapabilities.PROTOCOL_SESSION);
        }
        if ((mapping & 0x04) == 0x04) {
            result.add(DeviceProtocolCapabilities.PROTOCOL_MASTER);
        }
        return result;
    }

    private PropertySpec deviceTimeZonePropertySpec(boolean required) {
        PropertySpecFactory factory;
        if (required) {
            factory = RequiredPropertySpecFactory.newInstance();
        }
        else {
            factory = OptionalPropertySpecFactory.newInstance();
        }
        return factory.stringPropertySpec(DEVICE_TIMEZONE_PROPERTY_NAME);
    }

    private PropertySpec nodeAddressPropertySpec(boolean required) {
        if (required) {
            return RequiredPropertySpecFactory.newInstance().stringPropertySpec(MeterProtocol.NODEID);
        }
        else {
            return OptionalPropertySpecFactory.newInstance().stringPropertySpec(MeterProtocol.NODEID);
        }
    }

    private PropertySpec deviceIdPropertySpec(boolean required) {
        if (required) {
            return RequiredPropertySpecFactory.newInstance().stringPropertySpec(MeterProtocol.ADDRESS);
        }
        else {
            return OptionalPropertySpecFactory.newInstance().stringPropertySpec(MeterProtocol.ADDRESS);
        }
    }

    private PropertySpec callHomeIdPropertySpec(boolean required) {
        if (required) {
            return RequiredPropertySpecFactory.newInstance().stringPropertySpec(CALL_HOME_ID_PROPERTY_NAME);
        }
        else {
            return OptionalPropertySpecFactory.newInstance().stringPropertySpec(CALL_HOME_ID_PROPERTY_NAME);
        }
    }

    protected abstract AbstractDeviceProtocolSecuritySupportAdapter getSecuritySupportAdapter();

    public DeviceProtocolSecurityPropertySet getLegacyTypedPropertiesAsSecurityPropertySet(TypedProperties typedProperties) {
        return getSecuritySupportAdapter().getLegacyTypedPropertiesAsSecurityPropertySet(typedProperties);
    }

}
