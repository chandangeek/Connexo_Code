package com.energyict.mdc.engine;

import com.energyict.mdc.engine.impl.ObjectMapperServiceImpl;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Copyrights EnergyICT
 * Date: 1/09/14
 * Time: 16:12
 */
public interface JSONTypeMapperProvider {

    public static final AtomicReference<JSONTypeMapperProvider> instance = new AtomicReference<>();

    ObjectMapperServiceImpl.JSONTypeMapper getJSONTypeMapper();
}


