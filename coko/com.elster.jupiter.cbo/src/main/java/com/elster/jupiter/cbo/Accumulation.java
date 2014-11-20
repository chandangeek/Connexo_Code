package com.elster.jupiter.cbo;

public enum Accumulation {
	NOTAPPLICABLE (0,"NotApplicable",false),
	BULKQUANTITY (1,"BulkQuantity",true),
	CONTINUOUSCUMULATIVE (2,"ContinuousCumulative",true),
	CUMULATIVE (3,"Cumulative",true),
	DELTADELTA (4,"DeltaDelta",false),
	INDICATING(6,"Indicating",false),
	SUMMATION(9,"Summation",true),
	TIMEDELAY(10,"TimeDelay",false),
	INSTANTANEOUS(12,"Instantaneous",false),
	LATCHINGQUANTITY(13,"LatchingQuantity",true),
	BOUNDEDQUANTITY(14,"BoundedQuantity", true);
	
	private final int id;
	private final String description;
	private final boolean cumulative;
	
	Accumulation(int id, String description, boolean cumulative) {
		this.id = id;
		this.description = description;
		this.cumulative = cumulative;
	}
	
	public static Accumulation get(int id) {
		for (Accumulation each : values()) {
			if (each.id == id) {
                return each;
            }
		}
        throw new IllegalEnumValueException(Accumulation.class, id);
	}
	
	@Override
	public String toString() {
		return "Accumulation " + id + " : " + description;
	}
	
	public int getId() {
		return id;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean isApplicable() {
		return id != 0;
	}
	
	public boolean isCumulative() {
		return cumulative;
	}
}
