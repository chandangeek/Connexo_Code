package com.elster.jupiter.util.conditions;

public enum Constant implements Condition {
	TRUE {
		@Override
		public Condition and(Condition condition) {
			return condition;
		}

		@Override
		public Condition or(Condition condition) {			
			return this;
		}

		@Override
		public Condition not() {
			return FALSE; 
		}

		@Override
		public void visit(Visitor visitor) {
			visitor.visitTrue(this);
		}		
	},
	FALSE {
		@Override
		public Condition and(Condition condition) {
			return this;
		}

		@Override
		public Condition or(Condition condition) {
			return condition;
		}

		@Override
		public Condition not() {			
			return TRUE; 
		}

		@Override
		public void visit(Visitor visitor) {
			visitor.visitFalse(this);
		}		

	}
}
