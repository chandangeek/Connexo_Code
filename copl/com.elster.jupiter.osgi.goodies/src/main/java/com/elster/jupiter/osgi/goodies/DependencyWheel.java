/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.osgi.goodies;

import java.util.ArrayList;
import java.util.List;

public class DependencyWheel {
	public List<String> packageNames = new ArrayList<>();
	public int[][] matrix;
	
	void add(String name) {
		packageNames.add(name);
	}
	
	void initMatrix() {
		matrix = new int[packageNames.size()][];
		for (int i = 0 ; i < packageNames.size() ;i++) {
			matrix[i] = new int[packageNames.size()];
		}
	}
	
	void setDependency(String from, String to) {
		matrix[packageNames.indexOf(from)][packageNames.indexOf(to)] = 1;
	}
} 
