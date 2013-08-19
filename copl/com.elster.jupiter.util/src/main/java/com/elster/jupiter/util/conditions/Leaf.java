package com.elster.jupiter.util.conditions;

import static com.elster.jupiter.util.conditions.Constant.*;

/**
 * Abstract superclass for Conditions that evaluate independently from other Conditions.
 */
public abstract class Leaf implements Condition {

	@Override
	final public Condition and(Condition condition) {
        if (TRUE == condition) {
            return this;
        }
        if (FALSE == condition) {
            return FALSE;
        }
		return new And(this, condition);
	}

	@Override
	final public Condition or(Condition condition) {
        if (TRUE == condition) {
            return TRUE;
        }
        if (FALSE == condition) {
            return this;
        }
		return new Or(this, condition);
	}

	@Override
	final public Condition not() {
		return new Not(this);
	}
}
