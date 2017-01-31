/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.osgi.goodies;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.associations.RefAny;
import com.elster.jupiter.orm.associations.Reference;

public class Node {
	private final RefAny base;
	private final List<Association> associations = new ArrayList<>();
	private OrmService ormService;
	
	Node(OrmService ormService, RefAny base) {
		this.ormService = ormService;
		this.base = base;
	}
	
	void init(Map<RefAny,Node> visited) {
		visited.put(base,this);
		List<Node> newNodes = new ArrayList<>();
		Object target = base.get();
		Class<?> clazz = target.getClass();
		while (clazz != Object.class) {
			for (Field field : clazz.getDeclaredFields()) {
				if (field.getType() == Reference.class) {
					field.setAccessible(true);
					Reference<?> ref = null;
					try {
						ref = (Reference<?>) field.get(target);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (ref.isPresent()) {
						RefAny candidate = asRefAny(ref.get());
						if (visited.containsKey(candidate)) {
							Node oldNode = visited.get(candidate);
							if (!oldNode.associates(this)) {
								associations.add(new Association(field.getName(),this,oldNode));
							}
						} else {
							Node newNode = new Node(ormService, candidate);
							associations.add(new Association(field.getName(), this, newNode));
							newNodes.add(newNode);
						}
					}
				}
				if (field.getType() == List.class) {
					field.setAccessible(true);
					List<?> details = null;
					try {
						details = (List<?>) field.get(target);
					} catch (IllegalArgumentException | IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (details == null) {
						System.out.println("Empty list for field " + field.getName() + "in object " + target);
						details = new ArrayList<>();
					}
					int i = 0;
					for (Object each : details) {
						RefAny candidate = asRefAny(each);
						if (visited.containsKey(candidate)) {
							Node oldNode = visited.get(candidate);
							if (!oldNode.associates(this)) {
								associations.add(new Association(field.getName() + "(" + i + ")" , this, oldNode));
							}
						} else {
							Node newNode = new Node(ormService, candidate);
							associations.add(new Association(field.getName() + "(" + i + ")",this, newNode));
							newNodes.add(newNode);
						}
						i++;
					}
				}
			}
			clazz = clazz.getSuperclass();
		}
		for (Node each : newNodes) {
			each.init(visited);
		}
	}
	
	boolean associates(Node other) {
		for (Association each : associations) {
			if (each.getTo() == other) {
				return true;
			}
		}
		return false;
	}
	
	private RefAny asRefAny(Object value) {
		return ormService.getDataModel("ORM").get().asRefAny(value);
	}
	
	void append(StringBuilder builder, Map<RefAny,Node> inventory) {
		if (inventory.containsKey(base)) {
			inventory.remove(base);
			
			builder.append("\t");
			builder.append(quote(toString()));
			builder.append(" [shape=box label=\"");
			builder.append(base.get().getClass().getSimpleName().replace("Impl",""));
			builder.append("\\n");
			for (Object part : base.getPrimaryKey()) {
				builder.append(escape("" + part));
				builder.append("/");
			}
			builder.deleteCharAt(builder.length() - 1);
			builder.append("\"");
			builder.append(" URL=\"/api/goodies/bundles/browse/");
			builder.append(base.getComponent());
			builder.append("/");
			builder.append(base.getTableName());
			builder.append("/");
			for (Object part : base.getPrimaryKey()) {
				builder.append(escape("" + part));
				builder.append("/");
			}
			builder.deleteCharAt(builder.length() - 1); 
			builder.append("\"]\n");
			for (Association association : associations) {
				association.append(builder,inventory);
			}
		}
	}
	
	@Override
	public String toString() {
		return base.get().toString();
	}
	
	static String escape(String in) {
		return in.replace("\"","\\\"");
	}
	
	static String quote(String in) {
		return 
			"\"" + escape(in) + "\"";
	}
}

