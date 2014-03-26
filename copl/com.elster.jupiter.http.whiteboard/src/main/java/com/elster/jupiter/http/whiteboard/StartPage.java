package com.elster.jupiter.http.whiteboard;

import java.util.List;
import java.util.Map;

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
