package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This factory will load and cache a list of {@link SecuritySupportAdapterMapping}s.
 * The factory is not foreseen to create mappings,
 * only for fetching them. These mappings
 * are created during migration.
 *
 * Copyrights EnergyICT
 * Date: 12/04/13
 * Time: 11:03
 */
public class SecuritySupportAdapterMappingFactoryImpl implements SecuritySupportAdapterMappingFactory {

    private Map<String, String> cache;

    @Inject
    public SecuritySupportAdapterMappingFactoryImpl(DataModel dataModel) {
        this(dataModel.mapper(SecuritySupportAdapterMapping.class));
    }

    private SecuritySupportAdapterMappingFactoryImpl(DataMapper<SecuritySupportAdapterMapping> mapper) {
        super();
        this.cache =
            mapper
                .find()
                .stream()
                .collect(Collectors.toMap(
                        SecuritySupportAdapterMapping::getDeviceProtocolJavaClassName,
                        SecuritySupportAdapterMapping::getSecuritySupportJavaClassName));
    }

    @Override
    public String getSecuritySupportJavaClassNameForDeviceProtocol(String deviceProtocolJavaClassName) {
        return this.cache.get(deviceProtocolJavaClassName);
    }

}