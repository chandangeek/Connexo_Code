/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.common.NotFoundException;

/**
 * Models the exceptional situation that occurs when
 * a parameter of a {@link Request} that is expected
 * to be the unique identifier of a business object
 * fails to parse to a integer.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-05 (10:40)
 */
class BusinessObjectIdParseException extends BusinessObjectParseException {

    BusinessObjectIdParseException(String id, String categoryName, NumberFormatException e) {
        super(id + " cannot represent the unique identifier of a " + categoryName + " because it is not numerical", e);
    }

    BusinessObjectIdParseException(String id, String categoryName, NotFoundException e) {
        super("The " + categoryName + " with id " + id + " could not be found", e);
    }

}