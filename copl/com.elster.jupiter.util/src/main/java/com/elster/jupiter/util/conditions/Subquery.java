/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.conditions;

import com.elster.jupiter.util.sql.SqlFragment;

public interface Subquery {
	SqlFragment toFragment();
}
