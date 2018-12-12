/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.ids;

public interface FieldSpec {
	RecordSpec getRecordSpec();
	String getName();
	FieldType getType();
}
