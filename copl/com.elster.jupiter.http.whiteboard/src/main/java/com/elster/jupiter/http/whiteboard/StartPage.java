package com.elster.jupiter.http.whiteboard;

import java.util.List;

public interface StartPage {
	String getHtmlPath();
	String getIconPath();
	String getName();
    String getMainController();
    List<Script> getScripts();
    List<String> getTranslationComponents();
}
