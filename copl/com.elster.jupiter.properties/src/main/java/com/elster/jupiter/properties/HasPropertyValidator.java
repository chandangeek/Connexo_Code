/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

import com.elster.jupiter.util.exception.MessageSeed;

public interface HasPropertyValidator<T> {
    MessageSeed invalidMessage();
    Object getReferenceValue();
}

