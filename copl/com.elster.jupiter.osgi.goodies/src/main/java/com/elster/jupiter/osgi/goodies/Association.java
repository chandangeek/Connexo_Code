/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.osgi.goodies;

import java.util.Map;

import com.elster.jupiter.orm.associations.RefAny;

public class Association {
	private final String name;
	private final Node from;
	private final Node to;
	
	Association(String name, Node from , Node to) {
		this.name = name;
		this.from = from;
		this.to = to;
	}

	public void append(StringBuilder builder,Map<RefAny,Node> inventory) {
		builder.append("\t");
		builder.append(Node.quote(from.toString()));
		builder.append(" -> ");
		builder.append(Node.quote(to.toString()));
		builder.append(" [label=");
		builder.append(Node.quote(name));
		builder.append("]\n");
		to.append(builder,inventory);
	}
	
	Node getTo() {
		return to;
	}
	
}
