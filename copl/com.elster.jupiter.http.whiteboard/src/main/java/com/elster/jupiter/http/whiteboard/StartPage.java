/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Map;

@ProviderType
public interface StartPage {
	String getHtmlPath();
	String getIconPath();
	String getName();
    String getMainController();
    List<Script> getScripts();
    List<String> getTranslationComponents();
    List<String> getStyleSheets();
    Map<String,String> getDependencies();
}
