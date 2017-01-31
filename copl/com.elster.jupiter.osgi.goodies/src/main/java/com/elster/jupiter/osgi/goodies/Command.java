/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.osgi.goodies;

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.packageadmin.PackageAdmin;

@SuppressWarnings("deprecation")
@Component (
	name="com.elster.jupiter.osgi.goodies",service=Command.class,
	property = { "osgi.command.scope=jupiter" , "osgi.command.function=generate" } )
public class Command {
	private volatile BundleContext bundleContext;
	private volatile PackageAdmin admin;

	@SuppressWarnings("unused")
	public void generate(String...strings) {
		Analyzer analyzer = new Analyzer();
		analyzer.build(bundleContext, admin);
		analyzer.generateBundleGraph(strings, null, false);
	}

	@Reference
	public void setPackageAdmin(PackageAdmin admin) {
		this.admin = admin;
	}

	@Activate
	public void activate(BundleContext context) {
		this.bundleContext = context;
	}

}