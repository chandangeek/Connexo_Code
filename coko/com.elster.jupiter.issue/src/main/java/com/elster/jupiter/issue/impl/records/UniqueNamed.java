/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.impl.records;

import com.elster.jupiter.util.HasName;

public interface UniqueNamed extends HasName {
    boolean validateUniqueName(boolean caseSensitive);
}
