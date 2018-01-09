/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface StorerStats {

    int getEntryCount();

    int getInsertCount();

    int getUpdateCount();

    int getDeleteCount();

    long getExecuteTime();
}
