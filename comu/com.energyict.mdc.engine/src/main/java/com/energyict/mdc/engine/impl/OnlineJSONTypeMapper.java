package com.energyict.mdc.engine.impl;

/**
 * Provides an implementation for the  {@link ObjectMapperServiceImpl.JSONTypeMapper}
 * interface that is well suited for the online ComServer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2017-01-17 (11:57)
 */
public class OnlineJSONTypeMapper implements ObjectMapperServiceImpl.JSONTypeMapper {

    @Override
    public Class classForName(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

    @Override
    public void convertAllClassNamesFor(Object objectJSON) {
    }

}