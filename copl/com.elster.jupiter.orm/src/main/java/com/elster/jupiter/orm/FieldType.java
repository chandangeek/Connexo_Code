/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm;

public enum FieldType {
	/**
	 * Field maps to single column
	 */
	SIMPLE,
	/**
	 * Field maps to multiple columns
	 */
	COMPLEX, 
	/**
	 * Child end of foreign key 
	 */
	ASSOCIATION,
	/**
	 * Parent end of foreign key
	 */
	REVERSEASSOCIATION, 
	/*
	 * Parent end of foreign key. Contains current version in a time relation
	 */
	CURRENTASSOCIATION;
}
