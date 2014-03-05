package com.elster.jupiter.http.whiteboard;

import java.util.List;

public final class DefaultStartPage implements StartPage {

    private final String htmlPath;
    private final String iconPath;
    private final String name;
    private final String mainController;
    private final List<Script> scripts;

    public DefaultStartPage(String name, String iconPath, String htmlPath, String mainController, List<Script> scripts) {
        this.htmlPath = htmlPath;
        this.iconPath = iconPath;
        this.name = name;
        this.mainController = mainController;
        this.scripts = scripts;
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
        return scripts;
    }
}
