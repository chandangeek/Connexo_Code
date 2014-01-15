package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

/**
 * Copyrights EnergyICT
 * Date: 15/04/13
 * Time: 11:20
 */
public class DefaultCapabilityAdapterMappingFactoryProvider implements DeviceCapabilityAdapterMappingFactoryProvider {

    private CapabilityAdapterMappingFactoryImpl capabilityAdapterMappingFactory;

    @Override
    public CapabilityAdapterMappingFactoryImpl getCapabilityAdapterMappingFactory() {
        /*
       We like to keep the implementation cached, so we can cache the mappings and don't
       need to fetch them from the DB each time we need them
        */
        if (this.capabilityAdapterMappingFactory == null) {
            this.capabilityAdapterMappingFactory = new CapabilityAdapterMappingFactoryImpl();
        }
        return this.capabilityAdapterMappingFactory;
    }
}
