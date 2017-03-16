/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.http.whiteboard;

import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public final class BundleResolver implements Resolver {
	private final Bundle bundle;
	
	public BundleResolver(BundleContext bundleContext) {
		this.bundle = bundleContext.getBundle();
	}

	@Override
	public URL getResource(String name) {
		return bundle.getResource(name);
	}

}
