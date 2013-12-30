package com.elster.jupiter.orm.associations;

public class Temporals {
	public static <T extends Effectivity> TemporalReference<T> absent() {
		return new TemporalListReference<>();
	}
	
	public static <T extends Effectivity> TemporalList<T> emptyList() {
		return new TemporalArrayList<>();
	}
}
