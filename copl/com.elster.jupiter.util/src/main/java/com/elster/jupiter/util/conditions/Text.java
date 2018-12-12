/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.conditions;

public class Text extends Leaf {
	
	private final String text; 
	
	Text(String text) {
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	@Override
	public void visit(Visitor visitor) {
		visitor.visitText(this);		
	}
	
	@Override 
	public String toString() {
		return "Text: " + text;
	}
}
