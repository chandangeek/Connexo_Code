/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DefaultStartPage implements StartPage {

    private final String htmlPath;
    private final String iconPath;
    private final String name;
    private final String mainController;
    private final List<Script> scripts;
    private final List<String> translationComponents;
    private final List<String> styleSheets;
    private final Map<String, String> dependencies;

    public DefaultStartPage(String name, String iconPath, String htmlPath, String mainController, List<Script> scripts, List<String> translationComponents, List<String> styleSheets, Map<String, String> dependencies) {
        this.htmlPath = htmlPath;
        this.iconPath = iconPath;
        this.name = name;
        this.mainController = mainController;
        this.scripts = copyList(scripts);
        this.translationComponents = copyList(translationComponents);
        this.styleSheets = copyList(styleSheets);
        this.dependencies = copyMap(dependencies);
    }

    private static <T> List<T> copyList(List<T> list) {
        if (list != null) {
            return new ArrayList<>(list);
        } else {
            return null;
        }
    }

    private static <K, V> Map<K, V> copyMap(Map<K, V> map) {
        if (map != null) {
            return new HashMap<>(map);
        } else {
            return null;
        }
    }

    public DefaultStartPage(String name, String iconPath, String htmlPath, String mainController, List<Script> scripts, List<String> translationComponents, List<String> styleSheets) {
        this(name, iconPath, htmlPath, mainController, scripts, translationComponents, styleSheets, null);
    }

    public DefaultStartPage(String name, String iconPath, String htmlPath, String mainController, List<Script> scripts, List<String> translationComponents) {
        this(name, iconPath, htmlPath, mainController, scripts, translationComponents, null);
    }

    public DefaultStartPage(String name, String iconPath, String htmlPath, String mainController, List<Script> scripts) {
        this(name, iconPath, htmlPath, mainController, scripts, null);
    }

    public DefaultStartPage(String name, String iconPath, String htmlPath, String mainController) {
        this(name, iconPath, htmlPath, mainController, null);
    }

    public DefaultStartPage(String name, String iconPath, String htmlPath) {
        this(name, iconPath, htmlPath, null);
    }

    public DefaultStartPage(String name, String iconPath) {
        this(name, iconPath, "/index.html");
    }

    public DefaultStartPage(String name) {
        this(name, null);
    }

    @Override
    public String getHtmlPath() {
        return htmlPath;
    }

    @Override
    public String getIconPath() {
        return iconPath;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getMainController() {
        return mainController;
    }

    @Override
    public List<Script> getScripts() {
        return this.unmodifiableList(scripts);
    }

    @Override
    public List<String> getTranslationComponents() {
        return this.unmodifiableList(translationComponents);
    }

    @Override
    public List<String> getStyleSheets() {
        return this.unmodifiableList(styleSheets);
    }

    @Override
    public Map<String, String> getDependencies() {
        return this.unmodifiableMap(dependencies);
    }

    private <T> List<T> unmodifiableList(List<T> list) {
        if (list != null) {
            return Collections.unmodifiableList(list);
        } else {
            return null;
        }
    }

    private <K, V> Map<K, V> unmodifiableMap(Map<K, V> map) {
        if (map != null) {
            return Collections.unmodifiableMap(map);
        } else {
            return null;
        }
    }

}