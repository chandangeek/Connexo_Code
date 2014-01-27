package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;

import javax.inject.Inject;

/**
 * Copyrights EnergyICT
 * Date: 15/04/13
 * Time: 11:19
 */
public class MessageAdapterMappingFactoryImpl implements MessageAdapterMappingFactory {

    private DataMapper<MessageAdapterMapping> mapper;

    @Inject
    public MessageAdapterMappingFactoryImpl(DataModel dataModel) {
        this(dataModel.mapper(MessageAdapterMapping.class));
    }

    private MessageAdapterMappingFactoryImpl(DataMapper<MessageAdapterMapping> mapper) {
        super();
        this.mapper = mapper;
    }

    public String getMessageMappingJavaClassNameForDeviceProtocol(String deviceProtocolJavaClassName) {
        MessageAdapterMapping mapping = this.mapper.getUnique("deviceProtocolJavaClassName", deviceProtocolJavaClassName).orNull();
        if (mapping == null) {
            return null;
        }
        else {
            return mapping.getMessageAdapterJavaClassName();
        }
    }

}