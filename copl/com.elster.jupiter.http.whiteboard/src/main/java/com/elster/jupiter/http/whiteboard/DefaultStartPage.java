package com.elster.jupiter.http.whiteboard;

public final class DefaultStartPage implements StartPage {
	
	private final String htmlPath;
	private final String iconPath;
	private final String name;

	public DefaultStartPage(String name, String iconPath, String htmlPath) {
		this.name = name;
		this.iconPath = iconPath;
		this.htmlPath = htmlPath;
	}
	
	public DefaultStartPage(String name, String iconPath) {
		this(name,iconPath,"/index.html");
	}
	
	public DefaultStartPage(String name) {
		this(name,null);
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
}
