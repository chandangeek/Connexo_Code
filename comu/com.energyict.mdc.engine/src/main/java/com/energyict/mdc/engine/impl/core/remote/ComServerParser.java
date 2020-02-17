/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.core.remote;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.comserver.ComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.engine.JSONTypeMapperProvider;

import com.energyict.mdc.engine.config.impl.ComPortImpl;
import com.energyict.mdc.engine.config.impl.ComServerImpl;
import com.energyict.mdc.engine.exceptions.DataAccessException;
import com.energyict.mdc.engine.impl.ObjectMapperServiceImpl;
import com.energyict.mdc.engine.impl.core.RemoteComServerQueryJSonPropertyNames;
import com.energyict.mdc.engine.impl.MessageSeeds;
import com.energyict.mdc.engine.impl.tools.Equality;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

/**
 * Parses a JSONObject that was returned by a remote query to a {@link ComServer}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-11 (09:52)
 */
public class ComServerParser {

    public ComServerParser(){
        injectDependencies();
    }

    private void injectDependencies() {
    }

    public ComServer parse(JSONObject queryResult) throws JSONException {
        if (queryResult.has(RemoteComServerQueryJSonPropertyNames.SINGLE_OBJECT_VALUE)) {
            Object comServerJSon = queryResult.get(RemoteComServerQueryJSonPropertyNames.SINGLE_OBJECT_VALUE);
            return this.parseQueryResult((JSONObject) comServerJSon);
        } else {        //The DAO returned a null ComServer
            return null;
        }
    }

    ComServer parseQueryResult(JSONObject comServerJSon) {
        try {
            convertAllClassNamesFor(comServerJSon);
            Class<? extends ComServer> remoteComServerClass = this.getClassFor(comServerJSon);
            ComServerImpl comServer =  (ComServerImpl) ObjectMapperFactory.getObjectMapper().readValue(new StringReader(comServerJSon.toString()), remoteComServerClass);
            return initializeParentFields(comServer);
        } catch (IOException e) {
            throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
        } catch (JSONException e) {
            throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
        } catch(ClassNotFoundException e){
            throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
        }
    }

    /**
     * Initialize the ComServer parent field on the comports. It was not sent over JSON.
     */
    private ComServer initializeParentFields(ComServer comServer) {
        for (ComPort comPort : comServer.getComPorts()) {
            ((ComPortImpl) comPort).setComServer(comServer);
        }
        return comServer;
    }

    private void convertAllClassNamesFor(Object objectJSON) throws JSONException {
        try {
            JSONTypeMapperProvider.instance.get().getJSONTypeMapper().convertAllClassNamesFor(objectJSON);
        } catch (ClassNotFoundException e) {
            throw new DataAccessException(e, MessageSeeds.JSON_PARSING_ERROR);
        }
    }

    private Class<? extends ComServer> getClassFor(JSONObject comServerJSon) throws JSONException, ClassNotFoundException {
        String xmlType = comServerJSon.getString(ObjectMapperServiceImpl.JSONTypeMapper.TYPE_ATTRIBUTE);
        for (Class<? extends ComServer> knownImplementationClass : this.knownComServerImplementationClasses()) {
            if (Equality.equalityHoldsFor(knownImplementationClass.getName()).and(xmlType)) {
                return (Class<? extends ComServer>) Class.forName(xmlType);
            }
        }
        throw new ApplicationException("The ComServer returned by the remote query API is neither online, remote nor offline but was " + xmlType);
    }

    private Set<Class<? extends ComServer>> knownComServerImplementationClasses() {
        Set<Class<? extends ComServer>> knownImplementationClasses = new HashSet<>();
        knownImplementationClasses.add(com.energyict.mdc.engine.config.impl.OnlineComServerImpl.class);
        knownImplementationClasses.add(com.energyict.mdc.engine.config.impl.RemoteComServerImpl.class);
        knownImplementationClasses.add(com.energyict.mdc.engine.config.impl.OfflineComServerImpl.class);
        return knownImplementationClasses;
    }

}