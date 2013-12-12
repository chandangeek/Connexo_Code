package com.elster.jupiter.cbo;

public enum Accumulation {
	NOTAPPLICABLE (0,"NotApplicable"),
	BULKQUANTITY (1,"BulkQuantity"),
	CONTINUOUSCUMULATIVE (2,"ContinuousCumulative"),
	CUMULATIVE (3,"Cumulative"),
	DELTADELTA (4,"DeltaDelta"),
	INDICATING(6,"Indicating"),
	SUMMATION(9,"Summation"),
	TIMEDELAY(10,"TimeDelay"),
	INSTANTANEOUS(12,"Instantaneous"),
	LATCHINGQUANTITY(13,"LatchingQuantity"),
	BOUNDEDQUANTITY(14,"BoundedQuantity");
	
	private final int id;
	private final String description;
	
	Accumulation(int id, String description) {
		this.id = id;
		this.description = description;
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
	
}
