package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;

/**
 * Copyrights EnergyICT
 * Date: 15/04/13
 * Time: 11:19
 */
public class CapabilityAdapterMappingFactoryImpl implements CapabilityAdapterMappingFactory {

    private DataMapper<DeviceCapabilityMapping> mapper;

    public CapabilityAdapterMappingFactoryImpl(DataModel dataModel) {
        this(dataModel.mapper(DeviceCapabilityMapping.class));
    }

    private CapabilityAdapterMappingFactoryImpl(DataMapper<DeviceCapabilityMapping> mapper) {
        super();
        this.mapper = mapper;
    }

    @Override
    public Integer getCapabilitiesMappingForDeviceProtocol(String deviceProtocolJavaClassName) {
        DeviceCapabilityMapping mapping = this.mapper.getUnique("deviceProtocolJavaClassName", deviceProtocolJavaClassName).get();
        if (mapping == null) {
            return null;
        }
        else {
            return mapping.getDeviceProtocolCapabilities();
        }
    }

}