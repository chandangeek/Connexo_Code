/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface ShouldHaveUniqueName {
    boolean hasUniqueName();
}
