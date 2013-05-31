package com.elster.jupiter.util.conditions;

public class Exists extends Leaf {

	private final Subquery subquery;
	
	Exists(Subquery subquery) {
		this.subquery = subquery;
	}
	
	@Override
	public void visit(Visitor visitor) {
		visitor.visitExists(this);
		
	}

	public Subquery getSubquery() {
		return subquery;
	}

}
