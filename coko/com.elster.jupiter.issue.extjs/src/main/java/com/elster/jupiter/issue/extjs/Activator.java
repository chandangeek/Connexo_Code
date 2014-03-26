package com.elster.jupiter.issue.extjs;

import com.elster.jupiter.http.whiteboard.DefaultStartPage;
import com.elster.jupiter.http.whiteboard.FileResolver;
import com.elster.jupiter.http.whiteboard.HttpResource;
import com.elster.jupiter.http.whiteboard.Script;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Activator implements BundleActivator {
	public static final String HTTP_RESOURCE_ALIAS = "/issue";
    private volatile ServiceRegistration<HttpResource> registration;
	
	public void start(BundleContext bundleContext) throws Exception {
		List<Script> scripts = new ArrayList<>();
		
		DefaultStartPage issueStartPage = new DefaultStartPage("Isu", "", "/index.html", "Isu.controller.Main", scripts, Arrays.asList("ISU"));
        HttpResource resource = new HttpResource(HTTP_RESOURCE_ALIAS, "/js/issue" , new BundleResolver(bundleContext), issueStartPage);
        registration = bundleContext.registerService(HttpResource.class, resource , null);
	}
	
	public void stop(BundleContext bundleContext) throws Exception {
		registration.unregister();
	}

}
