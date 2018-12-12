/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.osgi.goodies;

public class LinkInfo {
	public int source;
	public int target;
	public int value = 1;
	
	public LinkInfo() {
	}
	
	public LinkInfo(int source , int target) {
		this.source = source;
		this.target = target;
	}
}
