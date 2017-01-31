/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

import java.io.IOException;

/**
 * An object that may hold IO related resources such as
 * file or socket handles until it is closed.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-05-10 (11:58)
 */
public interface IOResource {

    /**
     * Closes the resource(s) acquired by this object.
     */
    void close() throws IOException;

}