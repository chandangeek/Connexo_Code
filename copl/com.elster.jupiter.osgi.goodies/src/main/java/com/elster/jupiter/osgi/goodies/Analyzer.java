/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.osgi.goodies;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.packageadmin.ExportedPackage;
import org.osgi.service.packageadmin.PackageAdmin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("deprecation")
public class Analyzer {
	private final Map<Long,BundleInfo> bundleInfos = new HashMap<>();
	private String[] includeFilters;
	private String[] excludeFilters;

	void build(BundleContext context, PackageAdmin admin) {
		for (Bundle bundle : context.getBundles()) {
			ExportedPackage[] exports = admin.getExportedPackages(bundle);
			BundleInfo bundleInfo = new BundleInfo(bundle, exports);
			bundleInfos.put(bundle.getBundleId(),bundleInfo);
		}
		for (BundleInfo info : bundleInfos.values()) {
			for (BundleInfo other : bundleInfos.values()) {
				if (info != other) {
					Set<String> visited = new HashSet<>();
					for (ExportedPackage exportPackage : other.getExports()) {
						if (!visited.contains(exportPackage.getName())) {
							for (Bundle bundle : exportPackage.getImportingBundles()) {
								if (bundle.getSymbolicName().equals(info.getBundle().getSymbolicName())) {
									info.addImportPackage(exportPackage);
									break;
								}
							}
							visited.add(exportPackage.getName());
						}
					}
				}
			}
		}
	}

    private String[] copyMutableMember(String[] injectedValue) {
        if (injectedValue == null) {
            return null;
        } else {
            String[] copied = new String[injectedValue.length];
            System.arraycopy(injectedValue, 0, copied, 0, injectedValue.length);
            return copied;
        }
    }

	void generateBundleGraph(String[] includeFilters , String[] excludeFilters , boolean includeRoot)  {
		this.includeFilters = this.copyMutableMember(includeFilters);
		this.excludeFilters = this.copyMutableMember(excludeFilters);
		System.out.println();
		System.out.println("digraph osgi {");
		for (BundleInfo each : bundleInfos.values()) {
			if (includeRoot || each.getBundle().getBundleId() != 0) {
				if (include(each.getBundle().getSymbolicName())) {
					generate(each);
				}
			}
		}
		System.out.println("}");
	}

	String generate(String[] includeFilters , String[] excludeFilters , boolean includeRoot)  {
        this.includeFilters = this.copyMutableMember(includeFilters);
        this.excludeFilters = this.copyMutableMember(excludeFilters);
		StringBuilder builder = new StringBuilder();
		builder.append("digraph osgi {\n");
		for (BundleInfo each : bundleInfos.values()) {
			if (includeRoot || each.getBundle().getBundleId() != 0) {
				if (include(each.getBundle().getSymbolicName())) {
					generate(each, builder);
				}
			}
		}
		builder.append("}\n");
		return builder.toString();
	}

	private boolean include(String name) {
		if (excludeFilters != null) {
			for ( String each : excludeFilters) {
				if (name.matches(each)) {
					return false;
				}
			}
		}
		if (includeFilters == null || includeFilters.length == 0) {
			return true;
		}
		for ( String each : includeFilters) {
			if (name.matches(each)) {
				return true;
			}
		}
		return false;
	}

	private static String[] jupiterExcludes = {
		".*util.*",
		".*oracle.*",
		".*orm.*",
		"com.elster.jupiter.security.thread",
		"com.elster.jupiter.pubsub",
		"com.elster.jupiter.transaction",
	};


	String generateJupiter() {
		return this.generate(new String[] {"com\\.elster\\.jupiter\\..*" }, jupiterExcludes ,false);
	}

	void generate(BundleInfo bundleInfo) {
		for ( Bundle dependent : bundleInfo.getDependents()) {
			if (include(dependent.getSymbolicName())) {
				System.out.println("\t\"" + dependent.getSymbolicName() + "\" -> \"" + bundleInfo.getBundle().getSymbolicName() + "\";");
			}
		}
	}

	void generate(BundleInfo bundleInfo,StringBuilder builder) {
		for ( Bundle dependent : bundleInfo.getDependents()) {
			if (include(dependent.getSymbolicName())) {
				builder
                    .append("\t\"")
                    .append(dependent.getSymbolicName())
                    .append("\" -> \"")
                    .append(bundleInfo.getBundle().getSymbolicName())
                    .append("\";").append("\n");
			}
		}
	}



	String generate(long bundleId) {
		StringBuilder builder = new StringBuilder("digraph osgi {\n");
		builder.append("rankdir=\"LR\";\n");
		final String[] ranks = { "Importing Bundles" , "Exported Packages" , "Selected Bundle" , "Imported Packages" , "Exporting Bundles" };
		builder.append("{ node[shape=plaintext];\n");
		boolean firstTime = true;
		for (String rank : ranks) {
			if (firstTime) {
				firstTime = false;
			} else {
				builder.append("->");
			}
			builder
                .append("\"")
                .append(rank)
                .append("\"");
		}
		builder.append("}\n");
		Set<Long> bundleIds = new HashSet<>();
		Map<String,Set<String>> nodeMap = new HashMap<>();
		for (String rank : ranks) {
			nodeMap.put(rank, new HashSet<>());
		}
		BundleInfo bundleInfo = get(bundleId);
		String bundleNodeName = nodeName(bundleInfo.getBundle(), bundleIds, builder);
		nodeMap.get(ranks[2]).add(bundleNodeName);
		for (ExportedPackage exportPackage : bundleInfo.getExports()) {
			String nodeName = nodeName(exportPackage,builder);
			nodeMap.get(ranks[1]).add(nodeName);
			builder
                .append("\t\"")
                .append(nodeName)
                .append("\" -> \"")
                .append(bundleNodeName)
                .append("\";\n");
			for (Bundle bundle : exportPackage.getImportingBundles()) {
				if (!bundle.equals(bundleInfo.getBundle())) {
					String importingBundleNodeName = nodeName(bundle,bundleIds,builder);
					nodeMap.get(ranks[0]).add(importingBundleNodeName);
					builder
                        .append("\t\"")
                        .append(importingBundleNodeName)
                        .append("\" -> \"")
                        .append(nodeName)
                        .append("\";\n");
				}
			}
		}
		for (ExportedPackage exportPackage : bundleInfo.getImports()) {
			String nodeName = nodeName(exportPackage,builder);
			nodeMap.get(ranks[3]).add(nodeName);
			builder
                .append("\t\"")
                .append(bundleNodeName)
                .append("\" -> \"")
                .append(nodeName)
                .append("\";\n");
			String exportingBundleNodeName = nodeName(exportPackage.getExportingBundle(),bundleIds,builder);
			nodeMap.get(ranks[4]).add(exportingBundleNodeName);
			builder
                .append("\t\"")
                .append(nodeName)
                .append("\" -> \"")
                .append(exportingBundleNodeName)
                .append("\";\n");
		}
		Set<String> badBundles = new HashSet<>();
		Iterator<String> it = nodeMap.get(ranks[4]).iterator();
		while (it.hasNext()) {
			String exportingBundle = it.next();
			if (nodeMap.get(ranks[0]).remove(exportingBundle)) {
				badBundles.add(exportingBundle);
				it.remove();
			}
		}
		for (String rank : ranks) {
			Set<String> nodes = nodeMap.get(rank);
			if (!nodes.isEmpty()) {
				builder
                    .append("{rank=same;\"")
                    .append(rank)
                    .append("\";");
				for (String node : nodes) {
					builder
                        .append("\"")
                        .append(node)
                        .append("\";");
				}
				builder.append("}\n");
			}
		}
		if (!badBundles.isEmpty()) {
			builder
                .append("BadBundles [shape=plaintext label=\"Importing and Exporting Bundles\"];\n")
                .append("BadBundles->\"")
                .append(ranks[1])
                .append("\";\n")
			    .append("\"")
                .append(ranks[3])
                .append("\"->BadBundles;\n")
			    .append("{rank=same;BadBundles;");
			for (String node : badBundles) {
				builder
                    .append("\"")
                    .append(node)
                    .append("\";");
			}
			builder.append("}\n");
		}
		builder.append("}\n");
		return builder.toString();
	}

	private String nodeName(ExportedPackage exportPackage , StringBuilder builder) {
		String nodeName = "P" + exportPackage.getExportingBundle().getBundleId() + exportPackage.getName() + exportPackage.getVersion();
		builder
            .append("\t\"")
            .append(nodeName)
            .append("\" [shape=box label=\"")
            .append(exportPackage.getName())
            .append("\\n")
            .append(exportPackage.getVersion())
            .append("\"];\n");
		return nodeName;
	}

	private String nodeName(Bundle bundle , Set<Long> bundleIds, StringBuilder builder) {
		String result = "B" + bundle.getBundleId();
		if (!bundleIds.contains(bundle.getBundleId())) {
			builder
                .append("\t")
                .append(result)
                .append(" [")
			    .append("label=\"")
                .append(bundle.getSymbolicName())
                .append("\\n")
                .append(bundle.getVersion())
                .append("\" ")
			    .append("URL=\"/api/goodies/bundles/")
                .append(bundle.getBundleId())
                .append(".svg\"")
			    .append("];\n");
			bundleIds.add(bundle.getBundleId());
		}
		return result;
	}

	BundleInfo get(long id) {
		return bundleInfos.get(id);
	}

	Network getNetwork() {
		Network result = new Network();
		List<String> groups = new ArrayList<>();
		for (BundleInfo each : bundleInfos.values()) {
			if (each.getBundle().getSymbolicName().startsWith("com.elster.jupiter")) {
				result.add(each.getBundle().getSymbolicName(), "" + getGroup(each.getBundle().getSymbolicName(),groups));
			}
		}
		for (BundleInfo each : bundleInfos.values()) {
			if (each.getBundle().getSymbolicName().startsWith("com.elster.jupiter")) {
				Network.Node source = result.getNode(each.getBundle().getSymbolicName());
				for (Bundle dependent : each.getDependents()) {
					if (dependent.getSymbolicName().startsWith("com.elster.jupiter")) {
						Network.Node target = result.getNode(dependent.getSymbolicName());
						result.add(source, target, source.group.equals(target.group) ? 1 : 10);
					}
				}
			}
		}
		return result;
	}

	private int getGroup(String symbolicName, List<String> groups) {
		if (symbolicName.contains(".rest")) {
			return 10;
		}
		if (symbolicName.contains(".extjs")) {
			return 11;
		}
		if (symbolicName.contains(".oracle")) {
			return 12;
		}
		String[] parts = symbolicName.split("\\.");
		return parts.length;
 	}

	DependencyWheel getWheel() {
		DependencyWheel result = new DependencyWheel();
		for (BundleInfo each : bundleInfos.values()) {
			//if (each.getBundle().getSymbolicName().startsWith("com.elster")) {
				result.add(each.getBundle().getSymbolicName());
			//}
		}
		result.initMatrix();
		for (BundleInfo each : bundleInfos.values()) {
			//if (each.getBundle().getSymbolicName().startsWith("com.elster")) {
				for (Bundle dependent : each.getDependents()) {
					//if (dependent.getSymbolicName().startsWith("com.elster")) {
						result.setDependency(dependent.getSymbolicName(),each.getBundle().getSymbolicName());
					//}
				}
			//}
		}
		return result;
	}
}
