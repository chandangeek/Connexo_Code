/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.cps.PersistentDomainExtension;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecBuilder;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.protocol.api.ConnectionType;
import com.energyict.mdc.protocol.api.DeviceProtocolAdapter;
import com.energyict.mdc.protocol.pluggable.ConnectionTypePluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.impl.adapters.TranslationKeys;
import com.energyict.mdc.upl.DeviceCachingSupport;
import com.energyict.mdc.upl.DeviceProtocolCapabilities;
import com.energyict.mdc.upl.cache.CachingProtocol;
import com.energyict.mdc.upl.cache.DeviceProtocolCache;
import com.energyict.mdc.upl.meterdata.Device;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;
import com.energyict.protocol.HHUEnabler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.TimeZone;
import java.util.stream.Collectors;

public abstract class DeviceProtocolAdapterImpl implements DeviceProtocolAdapter, DeviceCachingSupport {

    public static final String DEFAULT_TIMEZONE = "GMT";

    private final DataModel dataModel;
    private final PropertySpecService propertySpecService;
    private final Thesaurus thesaurus;
    private final ProtocolPluggableService protocolPluggableService;
    private final SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory;
    private final CapabilityAdapterMappingFactory capabilityAdapterMappingFactory;

    /**
     * Gets the instance of the {@link CachingProtocol}.
     *
     * @return the cachingProtocol
     */
    public abstract CachingProtocol getCachingProtocol();

    /**
     * Gets the instance of the {@link HHUEnabler}.
     *
     * @return the hhuEnabler
     */
    public abstract HHUEnabler getHhuEnabler();

    protected DeviceProtocolAdapterImpl(PropertySpecService propertySpecService, ProtocolPluggableService protocolPluggableService, Thesaurus thesaurus, SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory, DataModel dataModel, CapabilityAdapterMappingFactory capabilityAdapterMappingFactory) {
        super();
        this.propertySpecService = propertySpecService;
        this.thesaurus = thesaurus;
        this.protocolPluggableService = protocolPluggableService;
        this.securitySupportAdapterMappingFactory = securitySupportAdapterMappingFactory;
        this.dataModel = dataModel;
        this.capabilityAdapterMappingFactory = capabilityAdapterMappingFactory;
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
        return connectionTypePluggableClasses
                .stream()
                .map(ConnectionTypePluggableClass::getConnectionType)
                .collect(Collectors.toList());
    }

    @Override
    public void setCache(Serializable cacheObject) {
        if (cacheObject != null && cacheObject instanceof DeviceProtocolCache) {
            ((DeviceProtocolCache) cacheObject).setContentChanged(false);
        }
        getCachingProtocol().setCache(cacheObject);
    }

    @Override
    public Serializable getCache() {
        return getCachingProtocol().getCache();
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
            setCache((Serializable) this.protocolPluggableService.unMarshallDeviceProtocolCache(deviceCacheAdapter.getLegacyJsonCache()).orElse(null));
        }
    }

    @Override
    public DeviceProtocolCache getDeviceCache() {
        DeviceProtocolCacheAdapter deviceCacheAdapter = new DeviceProtocolCacheAdapter();
        String legacyJsonCache = this.protocolPluggableService.marshallDeviceProtocolCache(getCache());
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
        Integer mapping = this.capabilityAdapterMappingFactory.getCapabilitiesMappingForDeviceProtocol(getProtocolClass().getCanonicalName());
        if (mapping != null) {
            return getCapabilitesListFromFlags(mapping);
        } else {
            return Collections.singletonList(DeviceProtocolCapabilities.PROTOCOL_SESSION);  //Default, if there's no mapping available
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
        TimeZone[] timeZones = Arrays.asList(TimeZone.getAvailableIDs()).stream().map(TimeZone::getTimeZone).toArray(TimeZone[]::new);
        PropertySpecBuilder<TimeZone> builder = this.propertySpecService
                .timezoneSpec()
                .named(LegacyProtocolProperties.DEVICE_TIMEZONE_PROPERTY_NAME, TranslationKeys.DEVICE_TIME_ZONE)
                .fromThesaurus(this.thesaurus)
                .addValues(timeZones)
                .setDefaultValue(TimeZone.getTimeZone(DEFAULT_TIMEZONE))
                .markEditable();
        if (required) {
            builder.markRequired();
        }
        return builder.finish();
    }

    private PropertySpec nodeAddressPropertySpec(boolean required) {
        PropertySpecBuilder<String> builder = this.propertySpecService
                .stringSpec()
                .named(com.energyict.mdc.upl.MeterProtocol.Property.NODEID.getName(), TranslationKeys.NODE_ID)
                .fromThesaurus(thesaurus);
        if (required) {
            builder.markRequired();
        }
        return builder.finish();
    }

    private PropertySpec deviceIdPropertySpec(boolean required) {
        PropertySpecBuilder<String> builder = this.propertySpecService
                .stringSpec()
                .named(com.energyict.mdc.upl.MeterProtocol.Property.ADDRESS.getName(), TranslationKeys.ADDRESS)
                .fromThesaurus(thesaurus);
        if (required) {
            builder.markRequired();
        }
        return builder.finish();
    }

    private PropertySpec callHomeIdPropertySpec(boolean required) {
        PropertySpecBuilder<String> builder = this.propertySpecService
                .stringSpec()
                .named(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, TranslationKeys.CALL_HOME_ID)
                .fromThesaurus(thesaurus);
        if (required) {
            builder.markRequired();
        }
        return builder.finish();
    }

    protected abstract AbstractDeviceProtocolSecuritySupportAdapter getSecuritySupportAdapter();

    @Override
    public Optional<CustomPropertySet<Device, ? extends PersistentDomainExtension<Device>>> getCustomPropertySet() {
        return this.getSecuritySupportAdapter().getCustomPropertySet();
    }

    public DeviceProtocolSecurityPropertySet getLegacyTypedPropertiesAsSecurityPropertySet(TypedProperties typedProperties) {
        return getSecuritySupportAdapter().getLegacyTypedPropertiesAsSecurityPropertySet(typedProperties);
    }

}
