package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.util.HasName;

public interface HasUniqueName extends HasName{
    boolean isValidName(boolean caseSensitive);
}
