package com.elster.jupiter.osgi.goodies;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;

import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.associations.RefAny;

@SuppressWarnings("deprecation")
@Path("/bundles")
public class OsgiInfoResource {
	
	@Inject
	private PackageAdmin admin;
	@Inject
	private BundleContext context;
	@Inject
	private OrmService ormService;

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
    
    @GET
    @Path("/jupiter.json")
    @Produces(MediaType.APPLICATION_JSON)
    public DependencyWheel getNetwork() {
    	Analyzer analyzer = new Analyzer();
		analyzer.build(context,admin);
		return analyzer.getWheel();
    }

    @GET
    @Path("/browse/{component}/{table}/{key:.+}")
    @Produces("image/svg+xml")
    public byte[] getObjectGraph(@PathParam("component") String component, @PathParam("table") String table , @PathParam("key") String key) {
    	String[] keyParts = key.split("/");
    	Object[] keyValue = new Object[keyParts.length];
    	for (int i = 0 ; i < keyParts.length ; i++) {
    		try {
    			keyValue[i] = Long.parseLong(keyParts[i]);
    		} catch (NumberFormatException ex) {
    			keyValue[i] = keyParts[i];
    		}
    	}
    	RefAny refAny = ormService.createRefAny(component, table, keyValue);
    	StringBuilder builder = new StringBuilder("digraph osgi {\n");
    	Node root = new Node(ormService,refAny);
    	Map<RefAny,Node> inventory = new HashMap<>();
    	root.init(inventory);
    	root.append(builder, inventory);
    	builder.append("}\n");
    	return new GraphvizInterface().toSvg(builder.toString());
    }
    
}
