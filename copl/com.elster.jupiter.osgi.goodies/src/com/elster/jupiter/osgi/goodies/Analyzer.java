package com.elster.jupiter.osgi.goodies;

import java.util.*;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.*;

@SuppressWarnings("deprecation")
public class Analyzer {
	final private List<BundleInfo> bundleInfos = new ArrayList<>();
	private String[] includeFilters;
	private String[] excludeFilters;
	
	void build(BundleContext context , PackageAdmin admin) {
		for (Bundle bundle : context.getBundles()) {
			ExportedPackage[] exports = admin.getExportedPackages(bundle);
			bundleInfos.add(new BundleInfo(bundle,exports));
		}
	}
	
	void generateBundleGraph(String[] includeFilters , String[] excludeFilters , boolean includeRoot)  {
		this.includeFilters = includeFilters;
		this.excludeFilters = excludeFilters;
		System.out.println();
		System.out.println("digraph osgi {");
		for (BundleInfo each : bundleInfos) {
			if (includeRoot || each.getBundle().getBundleId() != 0) {
				generate(each);
			}
		}
		System.out.println("}");
	}
	
	private boolean include(String name) {
		if (excludeFilters != null) {
			for ( String each : excludeFilters) {
				if (name.matches(each))
					return false;
			}
		}
		if (includeFilters == null || includeFilters.length == 0) {
			return true;
		}
		for ( String each : includeFilters) {
			if (name.matches(each)) {
				return true;
			}
		}
		return false;
	}
	
	void generate(BundleInfo bundleInfo) {		
		for ( Bundle dependent : bundleInfo.getDependents()) {
			if (include(dependent.getSymbolicName())) {
				System.out.println("\t\"" + dependent.getSymbolicName() + "\" -> \"" + bundleInfo.getBundle().getSymbolicName() + "\";");
			}
		}	
	}
}
