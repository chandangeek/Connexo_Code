/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.osgi.goodies;

public class NodeInfo {
	public String name;
	public int group = 1;
	
	public NodeInfo()  {
	}
	
	public NodeInfo(String name) {
		this.name = name;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof NodeInfo) {
			return name.equals(((NodeInfo) other).name);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
