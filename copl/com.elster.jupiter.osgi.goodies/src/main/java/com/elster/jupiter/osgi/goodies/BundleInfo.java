/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.osgi.goodies;

import org.osgi.framework.Bundle;
import org.osgi.service.packageadmin.ExportedPackage;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("deprecation")
public class BundleInfo {
	private final Bundle bundle;
	private final ExportedPackage[] exports;
	private final Set<Bundle> dependents = new HashSet<>();
	private final Set<ExportedPackage> importPackages = new HashSet<>();

	BundleInfo(Bundle bundle, ExportedPackage[] exports) {
		this.bundle = bundle;
		if (exports == null) {
			this.exports = new ExportedPackage[0];
		} else {
			this.exports = new ExportedPackage[exports.length];
			System.arraycopy(exports, 0, this.exports, 0, exports.length);
		}
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

	public Set<ExportedPackage> getImports() {
		return Collections.unmodifiableSet(importPackages);
	}
	public Set<Bundle> getDependents() {
		return Collections.unmodifiableSet(dependents);
	}

	void addImportPackage(ExportedPackage importPackage) {
		this.importPackages.add(importPackage);
	}

	public String getNodeName() {
		return "B" + getBundle().getBundleId();
	}

}