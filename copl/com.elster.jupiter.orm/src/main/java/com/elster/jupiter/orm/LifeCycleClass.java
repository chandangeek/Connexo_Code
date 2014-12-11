package com.elster.jupiter.orm;

/*
 * describes the table's life cycle class
 * All tables of the same class use the same archiving strategy
 * 
 */
public enum LifeCycleClass {
	NONE,
	LOGGING;
}
