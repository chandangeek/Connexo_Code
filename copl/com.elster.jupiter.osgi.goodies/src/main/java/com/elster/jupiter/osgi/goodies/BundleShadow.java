/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.osgi.goodies;

import javax.xml.bind.annotation.XmlRootElement;

import org.osgi.framework.Bundle;

@XmlRootElement
public class BundleShadow {
	public String symbolicName;
	public String version;
	
	public BundleShadow() {		
	}
	
	public BundleShadow(Bundle bundle) {
		symbolicName = bundle.getSymbolicName();
		version = bundle.getVersion().toString();
	}
}
