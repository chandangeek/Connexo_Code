/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

/**
 * Models a type of registration request that can be sent
 * by a client application that is interested in receiving
 * {@link com.energyict.mdc.engine.events.ComServerEvent}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-15 (16:00)
 */
public interface RequestType {

    /**
     * Tests if this RequestType can parse a client application event registration request.
     * When <code>true</code> is returned, the {@link RequestParser}
     * will delegate the parsing of the request to this RequestType
     * and will therefore call the {@link #parse(String)} method.
     *
     * @param name The name as it appears in the client application event registration request
     * @return A flag that indicates if this RequestType can parse the client application event registration request
     */
    boolean canParse(String name);

    /**
     * Parses a {@link Request} from the specified string representation
     * of the parameters sent by the interested client application.
     *
     * @param parameterString The string representation of the request parameters
     * @return The Request
     * @throws RequestParseException Indicates a failure to parse the request
     */
    Request parse(String parameterString) throws RequestParseException;

}