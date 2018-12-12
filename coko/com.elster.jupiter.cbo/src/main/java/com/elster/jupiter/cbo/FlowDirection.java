/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

public enum FlowDirection {
		NOTAPPLICABLE (0,"Not applicable"),
		FORWARD (1,"Forward"),
		LAGGING (2,"Lagging"),
		LEADING (3,"Leading"),
		NET (4,"Net"),
		Q1PLUSQ2  (5,"Q1+Q2"),
		Q1PLUSQ3 (7,"Q1+Q3"),
		Q1PLUQQ4 (8,"Q1+Q4"),
		Q1MINUSQ4 (9,"Q1-Q4"),
		Q2PLUSQ3 (10,"Q2+Q3"),
		Q2PLUSQ4  (11,"Q2+Q4"),
		Q2MINUSQ3 (12,"Q2-Q3"),
		Q3PLUSQ4 (13,"Q3+Q4"),
		Q3MINUSQ2 (14,"Q3-Q2"),
		Q1 (15,"Q1"),
		Q2 (16,"Q2"),
		Q3 (17,"Q3"),
		Q4 (18,"Q4"),
		REVERSE (19,"Reverse"),
		TOTAL (20,"Total"),
		TOTALBYPHASE (21,"Total by phase");
	
	
	private final int id;
	private final String description;
	
	FlowDirection(int id,String description) {
		this.id = id;
		this.description = description;
	}
	
	public static FlowDirection get(int id) {
		for (FlowDirection each : values()) {
			if (each.id == id) {
                return each;
            }
		}
        throw new IllegalEnumValueException(FlowDirection.class, id);
	}
	
	
	@Override
	public String toString() {
		return "Flow Direction " + id + " : " + description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public boolean isApplicable() {
		return id != 0;
	}

    public int getId() {
        return id;
    }
}
