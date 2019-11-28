package com.energyict.mdc.engine.impl.core.remote;

import com.energyict.mdc.engine.JSONTypeMapperProvider;
import com.energyict.mdc.engine.impl.ObjectMapperServiceImpl;

/**
 * Copyrights EnergyICT
 * Date: 1/09/14
 * Time: 16:35
 */
public class RemoteJSONTypeMapperProvider implements JSONTypeMapperProvider {
    @Override
    public ObjectMapperServiceImpl.JSONTypeMapper getJSONTypeMapper() {
        return new RemoteJSONTypeMapper();
    }
}
