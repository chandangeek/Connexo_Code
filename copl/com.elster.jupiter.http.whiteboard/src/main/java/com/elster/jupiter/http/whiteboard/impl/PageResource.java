package com.elster.jupiter.http.whiteboard.impl;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.http.whiteboard.StartPage;

@Path("/pages")
public class PageResource {
	@Inject
	private WhiteBoard whiteBoard;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<PageInfo> getPages() {
		List<PageInfo> result = new ArrayList<>();
		for (HttpResource each : whiteBoard.getResources()) {
			StartPage startPage = each.getStartPage();
			if (startPage != null) {
				PageInfo info = new PageInfo();
				info.name = startPage.getName();
				info.url = whiteBoard.getAlias(each.getAlias()) + startPage.getHtmlPath();
				if (startPage.getIconPath() != null) {
					info.icon = whiteBoard.getAlias(each.getAlias()) + startPage.getIconPath();
				}
				result.add(info);
			}				
		}
		return result;
	}
}
