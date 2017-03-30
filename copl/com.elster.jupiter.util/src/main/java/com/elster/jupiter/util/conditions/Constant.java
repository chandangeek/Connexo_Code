/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.conditions;

/**
 * Enumeration of TRUE and FALSE Conditions
 */
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
		
		@Override
		public String toString() {
			return "TRUE";
		}

		@Override
		public boolean implies(Condition condition) {
			return false;
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
		
		@Override
		public String toString() {
			return "FALSE";
		}

		@Override
		public boolean implies(Condition condition) {
			return false;
		}

	}
}
