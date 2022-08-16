/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.schema;

import aQute.bnd.annotation.ConsumerType;

@ConsumerType
public interface ExistingSequence {
    String getName();
}
