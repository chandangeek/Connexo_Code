/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.common.NotFoundException;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Provides code reuse opportunities for {@link RequestType}s
 * that relate to a business object.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (16:50)
 */
abstract class IdBusinessObjectRequestType implements RequestType {

    @Override
    public boolean canParse (String name) {
        return this.getBusinessObjectTypeName().equalsIgnoreCase(name);
    }

    protected abstract String getBusinessObjectTypeName ();

    @Override
    public Request parse(String parameterString) throws BusinessObjectParseException {
        try {
            return newRequestAccording(parameterString);
        }
        catch (NotFoundException e) {
            throw new BusinessObjectIdParseException(parameterString, this.getBusinessObjectTypeName(), e);
        }
    }

    protected Request newRequestAccording(String parameterString) throws BusinessObjectParseException {
        Set<Long> ids = this.parseIds(parameterString);
        if (ids.isEmpty()) {
            return this.newRequestForAll();
        }
        else {
            return this.newRequestFor(ids);
        }
    }

    protected abstract Request newRequestForAll ();

    protected abstract Request newRequestFor (Set<Long> ids);

    private long parseId (String id) throws BusinessObjectIdParseException {
        try {
            return Long.parseLong(id);
        }
        catch (NumberFormatException e) {
            throw new BusinessObjectIdParseException(id, this.getBusinessObjectTypeName(), e);
        }
    }

    private Set<Long> parseIds (String commaSeparatedListOfIds) throws BusinessObjectIdParseException {
        Set<Long> ids = new HashSet<>();
        StringTokenizer tokenizer = new StringTokenizer(commaSeparatedListOfIds, ", ", false);
        while (tokenizer.hasMoreTokens()) {
            ids.add(this.parseId(tokenizer.nextToken()));
        }
        return ids;
    }

}