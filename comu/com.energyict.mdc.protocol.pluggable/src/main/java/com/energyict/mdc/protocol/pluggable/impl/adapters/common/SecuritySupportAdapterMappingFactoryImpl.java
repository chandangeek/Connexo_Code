package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;

/**
 * This factory will load and cache a list of
 * {@link SecuritySupportAdapterMapping securitySupportAdapterMappings}.
 * The factory is not foreseen to create mappings,
 * only for fetching them. These mappings
 * are created during migration.
 *
 * Copyrights EnergyICT
 * Date: 12/04/13
 * Time: 11:03
 */
public class SecuritySupportAdapterMappingFactoryImpl implements SecuritySupportAdapterMappingFactory {

    private DataMapper<SecuritySupportAdapterMapping> mapper;

    @Inject
    public SecuritySupportAdapterMappingFactoryImpl(DataModel dataModel) {
        this(dataModel.mapper(SecuritySupportAdapterMapping.class));
    }

    private SecuritySupportAdapterMappingFactoryImpl(DataMapper<SecuritySupportAdapterMapping> mapper) {
        super();
        this.mapper = mapper;
    }

    @Override
    public String getSecuritySupportJavaClassNameForDeviceProtocol(String deviceProtocolJavaClassName) {
        SecuritySupportAdapterMapping mapping = this.mapper.getUnique("deviceProtocolJavaClassName", deviceProtocolJavaClassName).orNull();
        if (mapping == null) {
            return null;
        }
        else {
            return mapping.getSecuritySupportJavaClassName();
        }
    }

}