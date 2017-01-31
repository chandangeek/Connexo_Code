/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.osgi.goodies;

import java.util.*;

import javax.xml.bind.annotation.XmlRootElement;

import org.osgi.framework.Bundle;

@XmlRootElement
public class BundleInfos {
	public List<NodeInfo> nodes = new ArrayList<>();
	public List<LinkInfo> links = new ArrayList<>();
	
	void add(NodeInfo nodeInfo) {
		nodes.add(nodeInfo);
	}
	
	void link(Bundle source, Bundle target) {
		links.add(new LinkInfo(nodes.indexOf(new NodeInfo(source.getSymbolicName())),nodes.indexOf(new NodeInfo(target.getSymbolicName()))));
	}
}
