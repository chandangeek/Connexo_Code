/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.associations;

/*
 * used to initialize temporal relations
 */
public class Temporals {
	public static <T extends Effectivity> TemporalReference<T> absent() {
		return new TemporalListReference<>();
	}
	
	public static <T extends Effectivity> TemporalList<T> emptyList() {
		return new TemporalArrayList<>();
	}
}
