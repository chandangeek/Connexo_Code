package com.elster.jupiter.osgi.goodies;

import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.*;

@SuppressWarnings("deprecation")
public class BundleInfo {
	final private Bundle bundle;
	final private ExportedPackage[] exports;
	final private Set<Bundle> dependents = new HashSet<>();
	
	BundleInfo(Bundle bundle,ExportedPackage[] exports) {
		this.bundle = bundle;
		this.exports = exports == null ? new ExportedPackage[0] : exports;
		for (ExportedPackage each : this.exports) {
			for (Bundle dependent : each.getImportingBundles()) {
				if (!dependent.equals(bundle)) {		
					dependents.add(dependent);
				}
			}
		}
	}

	public Bundle getBundle() {
		return bundle;
	}

	public ExportedPackage[] getExports() {
		return exports;
	}
	
	public Set<Bundle> getDependents() {
		return dependents;
	}
}
