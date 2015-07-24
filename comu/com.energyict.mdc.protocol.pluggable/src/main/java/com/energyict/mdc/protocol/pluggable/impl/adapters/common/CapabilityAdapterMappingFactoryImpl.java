package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Copyrights EnergyICT
 * Date: 15/04/13
 * Time: 11:19
 */
public class CapabilityAdapterMappingFactoryImpl implements CapabilityAdapterMappingFactory {

    private Map<String, Integer> cache;

    @Inject
    public CapabilityAdapterMappingFactoryImpl(DataModel dataModel) {
        this(dataModel.mapper(DeviceCapabilityMapping.class));
    }

    private CapabilityAdapterMappingFactoryImpl(DataMapper<DeviceCapabilityMapping> mapper) {
        super();
        this.cache =
                mapper
                    .find()
                    .stream()
                    .collect(Collectors.toMap(
                            DeviceCapabilityMapping::getDeviceProtocolJavaClassName,
                            DeviceCapabilityMapping::getDeviceProtocolCapabilities));
    }

    @Override
    public Integer getCapabilitiesMappingForDeviceProtocol(String deviceProtocolJavaClassName) {
        return this.cache.get(deviceProtocolJavaClassName);
    }

}