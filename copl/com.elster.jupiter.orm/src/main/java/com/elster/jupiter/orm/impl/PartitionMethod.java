/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.impl;

enum PartitionMethod {
	NONE {
		@Override
		void visit(Visitor visitor, StringBuilder sb) {
			visitor.visitNone(sb);
		}
	},
	RANGE {
		@Override
		void visit(Visitor visitor, StringBuilder sb) {
			visitor.visitRange(sb);
		}
	},
	INTERVAL {
		@Override
		void visit(Visitor visitor, StringBuilder sb) {
			visitor.visitInterval(sb);
		}
	},
	REFERENCE {
		@Override
		void visit(Visitor visitor, StringBuilder sb) {
			visitor.visitReference(sb);
		}
	};
	
	abstract void visit(Visitor visitor, StringBuilder sb);
	
	interface Visitor {
		default void visitNone(StringBuilder sb) {			
		};
		void visitRange(StringBuilder sb);
		void visitInterval(StringBuilder sb);
		void visitReference(StringBuilder sb);
	}
	
}
