/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface HasmRID {

    /**
     * The master Id is any free human readable and unique of the object.
     */
    String getmRID();

}
