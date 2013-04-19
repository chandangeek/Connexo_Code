package com.elster.jupiter.osgi.goodies;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.packageadmin.*;

@SuppressWarnings("deprecation")
public class Activator implements BundleActivator {

	public void start(BundleContext bundleContext) throws Exception {
		try {
			ServiceReference<PackageAdmin> reference = bundleContext.getServiceReference(PackageAdmin.class);
			//printRequired(bundleContext, bundleContext.getService(reference));
			analyze(bundleContext, bundleContext.getService(reference));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void stop(BundleContext bundleContext) throws Exception {
	}

	private void analyze(BundleContext bundleContext , PackageAdmin admin) {
		Analyzer analyzer = new Analyzer();
		analyzer.build(bundleContext,admin);
		analyzer.generateBundleGraph(new String [] {".*"}, new String[] {".*test.*" , "javax.*" ,".*eclipse.*" , ".*felix.*"}, false);
	}	
	
	@SuppressWarnings("unused")
	private void printRequired (BundleContext bundleContext , PackageAdmin admin) { 
		RequiredBundle[] requiredBundles = admin.getRequiredBundles(null);
		for (RequiredBundle required : requiredBundles) {
			System.out.println(required.getSymbolicName());
			for (Bundle each : required.getRequiringBundles()) {
				System.out.println("\t" + each);
			}
		}
	}
}

