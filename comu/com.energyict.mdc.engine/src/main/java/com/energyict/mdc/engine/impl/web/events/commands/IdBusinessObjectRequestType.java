package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.IdBusinessObject;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Provides code reuse opportunities for {@link RequestType}s
 * that relate to an {@link IdBusinessObject}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (16:50)
 */
public abstract class IdBusinessObjectRequestType implements RequestType {

    @Override
    public boolean canParse (String name) {
        return name.equalsIgnoreCase(this.getBusinessObjectTypeName());
    }

    protected abstract String getBusinessObjectTypeName ();

    @Override
    public Request parse (String parameterString) throws BusinessObjectIdParseException {
        try {
            Set<Integer> ids = this.parseIds(parameterString);
            if (ids.isEmpty()) {
                return this.newRequestForAll();
            }
            else {
                return this.newRequestFor(ids);
            }
        }
        catch (NotFoundException e) {
            throw new BusinessObjectIdParseException(parameterString, this.getBusinessObjectTypeName(), e);
        }
    }

    protected abstract Request newRequestForAll ();

    protected abstract Request newRequestFor (Set<Integer> ids);

    private int parseId (String id) throws BusinessObjectIdParseException {
        try {
            return Integer.parseInt(id);
        }
        catch (NumberFormatException e) {
            throw new BusinessObjectIdParseException(id, this.getBusinessObjectTypeName(), e);
        }
    }

    private Set<Integer> parseIds (String commaSeparatedListOfIds) throws BusinessObjectIdParseException {
        Set<Integer> ids = new HashSet<Integer>();
        StringTokenizer tokenizer = new StringTokenizer(commaSeparatedListOfIds, ",", false);
        while (tokenizer.hasMoreTokens()) {
            ids.add(this.parseId(tokenizer.nextToken()));
        }
        return ids;
    }

}