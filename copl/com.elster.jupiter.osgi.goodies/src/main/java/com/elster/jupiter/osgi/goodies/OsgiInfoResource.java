/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.osgi.goodies;


import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.associations.RefAny;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.PackageAdmin;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		analyzer.build(context, admin);
		String result = analyzer.generate(bundleId);
		return new GraphvizInterface().toSvg(result);
    }

    private enum OutputType {
        SVG {
            @Override
            public byte[] produce(String input) {
                return new GraphvizInterface().toSvg(input);
            }
        },
        PNG {
            @Override
            public byte[] produce(String input) {
                return new GraphvizInterface().toPng(input);
            }
        };

        public abstract byte[] produce(String input);
    }

    @GET
    @Path("/jupiter.svg")
    @Produces("image/svg+xml")
    public byte[] getViz(@Context UriInfo uriInfo) {
        return generateGraph(uriInfo, OutputType.SVG);
    }

    @GET
    @Path("/jupiter.png")
    @Produces("image/png")
    public byte[] getVizPng(@Context UriInfo uriInfo) {
        return generateGraph(uriInfo, OutputType.PNG);
    }

    private byte[] generateGraph(UriInfo uriInfo, OutputType type) {
        Analyzer analyzer = new Analyzer();
        analyzer.build(context, admin);
        String result = analyzer.generateJupiter();
        boolean tred = uriInfo.getQueryParameters().containsKey("tred");
        return type.produce(tred ? new GraphvizInterface().tred(result) : result);
    }

    @GET
    @Path("/jupiter.json")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public DependencyWheel getNetwork() {
    	Analyzer analyzer = new Analyzer();
		analyzer.build(context,admin);
		return analyzer.getWheel();
    }

    @GET
    @Path("/versions")
    @Produces(MediaType.APPLICATION_JSON+"; charset=UTF-8")
    public List<BundleVersion> getVersions() {
        List<BundleVersion> versions = new ArrayList<>();
        for (Bundle bundle : context.getBundles()) {
            versions.add(new BundleVersion(bundle.getSymbolicName(), bundle.getVersion().toString(),
                    bundle.getHeaders().get("Git-SHA-1"), bundle.getHeaders().get("Build-timestamp")));
        }
        Collections.sort(versions, (o1, o2) -> o1.bundleName.compareTo(o2.bundleName));
        return versions;
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