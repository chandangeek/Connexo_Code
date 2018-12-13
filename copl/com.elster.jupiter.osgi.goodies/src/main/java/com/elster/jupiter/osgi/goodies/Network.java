/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.osgi.goodies;

import java.util.ArrayList;
import java.util.List;

public class Network {
	public List<Node> nodes = new ArrayList<>();
	public List<Link> links = new ArrayList<>();
	
	int indexOf(Node node) {
		return nodes.indexOf(node);
	}
	
	Node getNode(String name) {
		for (Node each : nodes) {
			if (each.name.equals(name)) {
				return each;
			}
		}
		return null;
	}
	
	Node add(String name, String group) {
		Node result = new Node(name,group);
		nodes.add(result);
		return result;
	}
	
	Link add(Node source, Node target, int value) {
		Link result = new Link(source,target,value);
		links.add(result);
		return result;
	}
	
	public class Node {
		public final String name;
		public final String group;
		
		Node(String name, String group) {
			this.name = name;
			this.group = group;
		}
	
	}
	
	public class Link {
		public final int source;
		public final int target;
		public final int value;
		
		Link (Node source , Node target , int value) {
			this.source = indexOf(source);
			this.target = indexOf(target);
			this.value = value;
		}
	}
}
