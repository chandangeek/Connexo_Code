package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.common.IdBusinessObject;

/**
 * Models the exceptional situation that occurs when
 * a parameter of a {@link Request} that is expected
 * to be the unique identifier of a {@link IdBusinessObject}
 * fails to parse to a integer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-05 (10:40)
 */
public class BusinessObjectIdParseException extends RequestParseException {

    public BusinessObjectIdParseException (String id, String categoryName, NumberFormatException e) {
        super(id + " cannot represent the unique identifier of a " + categoryName + " because it is not numerical", e);
    }

    public BusinessObjectIdParseException (String id, String categoryName, NotFoundException e) {
        super("The " + categoryName + " with id " + id + " could not be found", e);
    }

}