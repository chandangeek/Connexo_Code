/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.conditions;

/**
 * Abstract superclass for Conditions that evaluate independently from other Conditions.
 */
public abstract class Leaf implements Condition {

	@Override
    public final Condition and(Condition condition) {
        if (TRUE == condition) {
            return this;
        }
        if (FALSE == condition) {
            return FALSE;
        }
		return new And(this, condition);
	}

	@Override
    public final Condition or(Condition condition) {
        if (TRUE == condition) {
            return TRUE;
        }
        if (FALSE == condition) {
            return this;
        }
		return new Or(this, condition);
	}

	@Override
    public final Condition not() {
		return new Not(this);
	}
	
	@Override
	public boolean implies(Condition condition) {
		return this.equals(condition);
	}
}
