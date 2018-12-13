/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.Script;

import java.util.List;
import java.util.Map;

public class PageInfo {
	
	public String name;
	public String basePath;
    public String startPage;
	public String icon;
    public String mainController;
    public List<Script> scripts;
    public List<String> translationComponents;
    public List<String> styleSheets;
    public Map<String,String> dependencies;
}
