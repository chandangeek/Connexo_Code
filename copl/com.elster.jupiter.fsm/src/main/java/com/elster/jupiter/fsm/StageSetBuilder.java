/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.fsm;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface StageSetBuilder {
    StageSetBuilder stage(String name);

    StageSet add();
}
