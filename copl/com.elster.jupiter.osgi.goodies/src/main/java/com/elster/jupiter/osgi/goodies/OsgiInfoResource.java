package com.elster.jupiter.osgi.goodies;


import javax.inject.Inject;
import javax.ws.rs.*;

import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;

@Path("/bundles")
public class OsgiInfoResource {
	
	@Inject
	private PackageAdmin admin;
	@Inject
	private BundleContext context;

    @GET
    @Path("/{bundleId}.svg")
    @Produces("image/svg+xml")
    public byte[] getViz(@PathParam("bundleId") int bundleId) {
    	Analyzer analyzer = new Analyzer();
		analyzer.build(context,admin);
		String result = analyzer.generate(bundleId);
		return new GraphvizInterface().toSvg(result);
    }
    
    @GET
    @Path("/jupiter.svg")
    @Produces("image/svg+xml")
    public byte[] getViz() {
    	Analyzer analyzer = new Analyzer();
		analyzer.build(context,admin);
		String result = analyzer.generateJupiter();
		return new GraphvizInterface().toSvg(result);
    }
    

}
