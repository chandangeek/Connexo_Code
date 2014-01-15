package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

/**
 * Copyrights EnergyICT
 * Date: 11/04/13
 * Time: 15:39
 */
public class DefaultSecuritySupportAdapterMappingFactoryProvider implements SecuritySupportAdapterMappingFactoryProvider {

    private SecuritySupportAdapterMappingFactory securitySupportAdapterMappingFactory;

    @Override
    public SecuritySupportAdapterMappingFactory getSecuritySupportAdapterMappingFactory() {
        /*
         We like to keep the implementation cached, so we can cache the mappings and don't
         need to fetch them from the DB each time we need them
          */
        if (this.securitySupportAdapterMappingFactory == null) {
            this.securitySupportAdapterMappingFactory = new SecuritySupportAdapterMappingFactoryImpl();
        }
        return this.securitySupportAdapterMappingFactory;
    }
}
