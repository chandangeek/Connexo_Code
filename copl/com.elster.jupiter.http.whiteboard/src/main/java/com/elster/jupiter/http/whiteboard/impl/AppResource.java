package com.elster.jupiter.http.whiteboard.impl;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.List;

@Path("/apps")
public class AppResource {
	@Inject
	private WhiteBoard whiteBoard;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<AppInfo> getPages() {
		List<AppInfo> result = new ArrayList<>();

        result.add(appInfo("Lorem ipsum", "connexo", "/apps/mdc/index.html"));
        result.add(appInfo("Dolor amet", "devices", "/apps/system/index.html"));
        result.add(appInfo("Alea iacta est", null, "/apps/validation/index.html"));

		return result;
	}

    private AppInfo appInfo(String name, String icon, String url) {
        AppInfo appInfo = new AppInfo();
        appInfo.name = name;
        appInfo.icon = icon;
        appInfo.url = url;
        return appInfo;
    }
}
