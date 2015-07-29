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
public class MessageAdapterMappingFactoryImpl implements MessageAdapterMappingFactory {

    private Map<String, String> cache;

    @Inject
    public MessageAdapterMappingFactoryImpl(DataModel dataModel) {
        this(dataModel.mapper(MessageAdapterMapping.class));
    }

    private MessageAdapterMappingFactoryImpl(DataMapper<MessageAdapterMapping> mapper) {
        super();
        this.cache =
                mapper
                    .find()
                    .stream()
                    .collect(Collectors.toMap(
                            MessageAdapterMapping::getDeviceProtocolJavaClassName,
                            MessageAdapterMapping::getMessageAdapterJavaClassName));
    }

    public String getMessageMappingJavaClassNameForDeviceProtocol(String deviceProtocolJavaClassName) {
        return this.cache.get(deviceProtocolJavaClassName);
    }

}