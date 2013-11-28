package com.elster.jupiter.http.whiteboard.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;

import org.osgi.service.component.annotations.*;
import org.osgi.service.http.*;

import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.http.whiteboard.StartPage;
import com.google.common.collect.ImmutableSet;

@Component(name = "com.elster.jupiter.http.whiteboard", service=Application.class, property = {"alias=/apps"})
@Path("/pages")
public class WhiteBoard extends Application {
	private volatile HttpService httpService;
	private List<HttpResource> resources = Collections.synchronizedList(new ArrayList<HttpResource>());
	
	public WhiteBoard() {
	}
	
	@Reference
	public void setHttpService(HttpService httpService) {
		this.httpService = httpService;
	}
	
	@Reference(name = "ZResource" , cardinality = ReferenceCardinality.MULTIPLE , policy = ReferencePolicy.DYNAMIC)
	public void addResource(HttpResource resource) {
		HttpContext httpContext = new HttpContextImpl(resource.getResolver());
		try {
			httpService.registerResources(getAlias(resource.getAlias()),resource.getLocalName(), httpContext);
			resources.add(resource);
		} catch (NamespaceException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}
	
	public void removeResource(HttpResource resource) {		
		httpService.unregister(getAlias(resource.getAlias()));
		resources.remove(resource);
	}
	
	private String getAlias(String name) {
		return "/js" + name;
	}
	
	@Override
	public Set<Object> getSingletons() {
		return ImmutableSet.<Object>of(this);
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<PageInfo> getPages() {
		List<PageInfo> result = new ArrayList<>();
		synchronized (resources) {
			for (HttpResource each : resources) {
				StartPage startPage = each.getStartPage();
				if (startPage != null) {
					PageInfo info = new PageInfo();
					info.name = startPage.getName();
					info.url = "/js" + each.getAlias() + startPage.getHtmlPath();
					if (startPage.getIconPath() != null) {
						info.icon = "/js" + each.getAlias() + startPage.getIconPath();
					}
					result.add(info);
				}				
			}
		}
		return result;
	}
	

}
