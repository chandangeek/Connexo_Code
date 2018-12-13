/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.util.HasName;

public interface HasUniqueName extends HasName{
    boolean isValidName(boolean caseSensitive);
}
