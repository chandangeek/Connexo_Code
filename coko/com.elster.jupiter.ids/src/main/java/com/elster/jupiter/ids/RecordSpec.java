/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids;

import java.util.List;

public interface RecordSpec {
	String getComponentName();
	long getId();
	String getName();
    List<? extends FieldSpec> getFieldSpecs();
}
