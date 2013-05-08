package com.elster.jupiter.util.conditions;

public interface Condition {
	public static final Condition TRUE = Constant.TRUE;
	
	Condition and(Condition condition);
	Condition or(Condition condition);
	Condition not();
	void visit(Visitor visitor);
}
