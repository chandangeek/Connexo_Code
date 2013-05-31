package com.elster.jupiter.util.conditions;

import com.elster.jupiter.util.sql.SqlFragment;

public class FragmentExpression extends Leaf {
	
	private final SqlFragment fragment; 
	
	FragmentExpression(SqlFragment fragment) {
		this.fragment = fragment;
	}
	
	public SqlFragment getFragment() {
		return fragment;
	}
	
	@Override
	public void visit(Visitor visitor) {
		visitor.visitFragmentExpression(this);		
	}
}
