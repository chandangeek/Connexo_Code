package com.elster.jupiter.osgi.goodies;


import javax.ws.rs.*;
import javax.ws.rs.Path;

@Path("/bundles")
public class OsgiInfoResource {

    @GET
    @Path("/{bundleId}.svg")
    @Produces("image/svg+xml")
    public byte[] getViz(@PathParam("bundleId") int bundleId) {
    	Analyzer analyzer = new Analyzer();
		analyzer.build(OsgiInfoApplication.context,OsgiInfoApplication.admin);
		String result = analyzer.generate(bundleId);
		return new GraphvizInterface().toSvg(result);
    }

}
