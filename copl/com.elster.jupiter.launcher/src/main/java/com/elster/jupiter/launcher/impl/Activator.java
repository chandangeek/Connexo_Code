package com.elster.jupiter.launcher.impl;

import org.osgi.framework.*;

public class Activator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		System.out.println("Jupiter launch activated");
	}

	@Override
	public void stop(BundleContext context) throws Exception {		
	}

}
