/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard.impl;

import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.http.whiteboard.StartPage;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/pages")
public class PageResource {
    @Inject
    private WhiteBoardImpl whiteBoard;

    @GET
    @Produces(MediaType.APPLICATION_JSON + "; charset=UTF-8")
    public List<PageInfo> getPages() {
        List<PageInfo> result = new ArrayList<>();
        for (HttpResource each : whiteBoard.getResources()) {
            StartPage startPage = each.getStartPage();
            if (startPage != null) {
                PageInfo info = new PageInfo();
                info.name = startPage.getName();
                info.basePath = whiteBoard.getAlias(each.getAlias());
                info.startPage = startPage.getHtmlPath();
                if (startPage.getIconPath() != null) {
                    info.icon = startPage.getIconPath();
                }
                if (startPage.getMainController() != null) {
                    info.mainController = startPage.getMainController();
                }
                if (startPage.getScripts() != null && !startPage.getScripts().isEmpty()) {
                    info.scripts = startPage.getScripts();
                }
                if (startPage.getTranslationComponents() != null && !startPage.getTranslationComponents().isEmpty()) {
                    info.translationComponents = startPage.getTranslationComponents();
                }
                if (startPage.getStyleSheets() != null && !startPage.getStyleSheets().isEmpty()) {
                    info.styleSheets = startPage.getStyleSheets();
                }
                if (startPage.getDependencies() != null && !startPage.getDependencies().isEmpty()) {
                    info.dependencies = startPage.getDependencies();
                }
                result.add(info);
            }
        }
        return result;
    }

}