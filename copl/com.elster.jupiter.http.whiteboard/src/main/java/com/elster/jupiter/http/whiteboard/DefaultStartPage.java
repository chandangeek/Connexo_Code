package com.elster.jupiter.http.whiteboard;

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
    private final Map<String,String> dependencies;


    public DefaultStartPage(String name, String iconPath, String htmlPath, String mainController, List<Script> scripts, List<String> translationComponents, List<String> styleSheets, Map<String,String> dependencies){
        this.htmlPath = htmlPath;
        this.iconPath = iconPath;
        this.name = name;
        this.mainController = mainController;
        this.scripts = scripts;
        this.translationComponents = translationComponents;
        this.styleSheets = styleSheets;
        this.dependencies = dependencies;
    }

    public DefaultStartPage(String name, String iconPath, String htmlPath, String mainController, List<Script> scripts, List<String> translationComponents, List<String> styleSheets){
        this(name, iconPath, htmlPath, mainController, scripts,translationComponents,styleSheets,null);
    }

    public DefaultStartPage(String name, String iconPath, String htmlPath, String mainController, List<Script> scripts, List<String> translationComponents) {
        this(name, iconPath, htmlPath, mainController, scripts,translationComponents,null);
    }

    public DefaultStartPage(String name, String iconPath, String htmlPath, String mainController, List<Script> scripts) {
        this(name, iconPath, htmlPath, mainController, scripts,null);
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

    @Override
    public List<String> getTranslationComponents() {
        return translationComponents;
    }

    @Override
    public List<String> getStyleSheets() {
        return styleSheets;
    }

    @Override
    public Map<String,String> getDependencies() {
        return dependencies;
    }
}
