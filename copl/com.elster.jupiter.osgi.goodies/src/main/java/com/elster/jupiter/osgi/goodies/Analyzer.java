package com.elster.jupiter.osgi.goodies;

import java.util.*;

import org.osgi.framework.*;
import org.osgi.service.packageadmin.*;

@SuppressWarnings("deprecation")
public class Analyzer {
	final Map<Long,BundleInfo> bundleInfos = new HashMap<>();
	private String[] includeFilters;
	private String[] excludeFilters;
	
	void build(BundleContext context , PackageAdmin admin) {
		for (Bundle bundle : context.getBundles()) {
			ExportedPackage[] exports = admin.getExportedPackages(bundle);
			BundleInfo bundleInfo = new BundleInfo(bundle,exports);
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
	
	void generateBundleGraph(String[] includeFilters , String[] excludeFilters , boolean includeRoot)  {
		this.includeFilters = includeFilters;
		this.excludeFilters = excludeFilters;
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
		this.includeFilters = includeFilters;
		this.excludeFilters = excludeFilters;
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
				if (name.matches(each))
					return false;
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
				builder.append("\t\"" + dependent.getSymbolicName() + "\" -> \"" + bundleInfo.getBundle().getSymbolicName() + "\";" + "\n");
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
			builder.append("\"" + rank + "\"");			
		}
		builder.append("}\n");
		Set<Long> bundleIds = new HashSet<>();
		Map<String,Set<String>> nodeMap = new HashMap<>();
		for (String rank : ranks) {
			nodeMap.put(rank,new HashSet<String>());
		}
		BundleInfo bundleInfo = get(bundleId);
		String bundleNodeName = nodeName(bundleInfo.getBundle(), bundleIds, builder);
		nodeMap.get(ranks[2]).add(bundleNodeName);
		for (ExportedPackage exportPackage : bundleInfo.getExports()) {		
			String nodeName = nodeName(exportPackage,builder);
			nodeMap.get(ranks[1]).add(nodeName);
			builder.append("\t\"" + nodeName + "\" -> \"" + bundleNodeName + "\";\n");			
			for (Bundle bundle : exportPackage.getImportingBundles()) {
				if (!bundle.equals(bundleInfo.getBundle())) {
					String importingBundleNodeName = nodeName(bundle,bundleIds,builder);
					nodeMap.get(ranks[0]).add(importingBundleNodeName);
					builder.append("\t\"" + importingBundleNodeName + "\" -> \"" + nodeName + "\";\n");
				}
			}			
		}	
		for (ExportedPackage exportPackage : bundleInfo.getImports()) {
			String nodeName = nodeName(exportPackage,builder);
			nodeMap.get(ranks[3]).add(nodeName);
			builder.append("\t\"" + bundleNodeName + "\" -> \"" + nodeName + "\";\n");
			String exportingBundleNodeName = nodeName(exportPackage.getExportingBundle(),bundleIds,builder);
			nodeMap.get(ranks[4]).add(exportingBundleNodeName);
			builder.append("\t\"" + nodeName + "\" -> \"" + exportingBundleNodeName + "\";\n");			
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
				builder.append("{rank=same;\"" + rank + "\";");
				for (String node : nodes) {
					builder.append("\"" + node + "\";");
				}
				builder.append("}\n");
			}
		}
		if (!badBundles.isEmpty()) {
			builder.append("BadBundles [shape=plaintext label=\"Importing and Exporting Bundles\"];\n");
			builder.append("BadBundles->\"" + ranks[1] + "\";\n");
			builder.append("\"" + ranks[3] + "\"->BadBundles;\n");
			builder.append("{rank=same;BadBundles;");
			for (String node : badBundles) {
				builder.append("\"" + node + "\";");
			}
			builder.append("}\n");					
		}
		System.out.println(builder.toString());
		return builder.toString();
	}
	
	private String nodeName(ExportedPackage exportPackage , StringBuilder builder) {
		String nodeName = "P" + exportPackage.getExportingBundle().getBundleId() + exportPackage.getName() + exportPackage.getVersion();
		builder.append("\t\"" + nodeName + "\" [shape=box label=\"" + exportPackage.getName() + "\\n" + exportPackage.getVersion() + "\"];\n");
		return nodeName;
	}
	
	private String nodeName(Bundle bundle , Set<Long> bundleIds, StringBuilder builder) {
		String result = "B" + bundle.getBundleId();
		if (!bundleIds.contains(bundle.getBundleId())) {
			builder.append("\t" + result + " [");
			builder.append("label=\"" + bundle.getSymbolicName() + "\\n" + bundle.getVersion() + "\" ");
			builder.append("URL=\"/api/goodies/bundles/" + bundle.getBundleId() + ".svg\"");
			builder.append("];\n");
			bundleIds.add(bundle.getBundleId());
		}
		return result;
	}
	
	BundleInfo get(String symbolicName) {
		return bundleInfos.get(symbolicName);
	}
	
	BundleInfo get(long id) {
		return bundleInfos.get(id);
	}

}
