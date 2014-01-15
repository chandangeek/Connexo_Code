package com.energyict.mdc.protocol.pluggable.impl.adapters.common;

/**
 * Copyrights EnergyICT
 * Date: 15/04/13
 * Time: 11:20
 */
public class DefaultMessageAdapterMappingFactoryProvider implements MessageAdapterMappingFactoryProvider {

    private MessageAdapterMappingFactoryImpl messageAdapterMappingFactory;

    @Override
    public MessageAdapterMappingFactory getMessageAdapterMappingFactory() {
        /*
       We like to keep the implementation cached, so we can cache the mappings and don't
       need to fetch them from the DB each time we need them
        */
        if (this.messageAdapterMappingFactory == null) {
            this.messageAdapterMappingFactory = new MessageAdapterMappingFactoryImpl();
        }
        return this.messageAdapterMappingFactory;
    }
}
