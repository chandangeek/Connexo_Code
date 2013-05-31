package com.elster.jupiter.util.conditions;

import com.elster.jupiter.util.sql.SqlFragment;

public interface Subquery {
	SqlFragment toFragment();
}
