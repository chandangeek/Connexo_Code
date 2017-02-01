/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.engine.impl.core.RunningComServer;

import java.util.Set;
import java.util.StringTokenizer;

/**
 * Provides an implementation for the {@link RequestType} interface
 * for {@link com.energyict.mdc.engine.config.ComPort}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (16:55)
 */
class ComPortRequestType extends IdBusinessObjectRequestType {

    private final RunningComServer comServer;

    ComPortRequestType(RunningComServer comServer) {
        this.comServer = comServer;
    }

    @Override
    protected String getBusinessObjectTypeName () {
        return "comport";
    }

    @Override
    protected Request newRequestForAll () {
        return new AllComPortsRequest();
    }

    @Override
    protected Request newRequestFor (Set<Long> ids) {
        return new ComPortRequest(this.comServer, ids);
    }

    @Override
    protected Request newRequestAccording(String parameterString) throws BusinessObjectParseException {
        try{
            //As the parameterString could not be parsed to a List of long,
            // We consider the parameterString being a comma separated list of comport names
            StringTokenizer tokenizer = new StringTokenizer(parameterString, ",", false);
            String[] comportNames = new String[tokenizer.countTokens()];
            int i = 0;
            while (tokenizer.hasMoreTokens()) {
                comportNames[i++] = tokenizer.nextToken().replaceAll("'", "").trim();
            }
            if (comportNames.length == 0) {
                return this.newRequestForAll();
            }
            return new ComPortRequest(this.comServer, comportNames);
        } catch (NotFoundException e) {
            throw new BusinessObjectParseException(e.getMessage(), e);
        }
    }

}