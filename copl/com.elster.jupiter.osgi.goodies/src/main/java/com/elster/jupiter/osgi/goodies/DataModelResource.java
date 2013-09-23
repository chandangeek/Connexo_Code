package com.elster.jupiter.osgi.goodies;


import javax.ws.rs.*;
import javax.ws.rs.Path;
import javax.ws.rs.core.*;

import org.osgi.framework.Bundle;

import com.elster.jupiter.orm.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Path("/datamodels")
public class DataModelResource {

	@GET
	@Produces(MediaType.APPLICATION_JSON) 
	public String[] getDataModels() {
		return new String[] { "ORM" , "USR" , "PRT" , "IDS" , "MTR" };
	}
	
	@GET
    @Path("/{componentName}.svg")
    @Produces("image/svg+xml")
    public byte[] getViz(@PathParam("componentName") String componentName) {
		DataModel dataModel = OsgiInfoApplication.ormService.getDataModel(componentName).get();
		String result = generate(dataModel);
    	return new GraphvizInterface().toSvg(result);
    }
	
	@GET
    @Path("/{componentName}/tables/{tableName}.svg")
    @Produces("image/svg+xml")
    public byte[] getViz(@PathParam("componentName") String componentName, @PathParam("tableName") String tableName) {
		DataModel dataModel = OsgiInfoApplication.ormService.getDataModel(componentName).get();
		Table table = dataModel.getTable(tableName);
		String result = generate(table);
    	return new GraphvizInterface().toSvg(result);
    }
	
	private String generate(DataModel model) {
		StringBuilder builder = new StringBuilder("digraph datamodel {\n");
		Set<String> tables = new HashSet<>();		
		for (Table table : model.getTables()) {
			addTable(table,tables,builder);
			for (ForeignKeyConstraint tableConstraint : table.getForeignKeyConstraints()) {
				addTable(tableConstraint.getReferencedTable(),tables,builder);
				builder.append(table.getName() + "->" + tableConstraint.getReferencedTable().getName() + ";\n");
			}
		}
		builder.append("}");
		return builder.toString();
	}

	private String generate(Table table) {
		StringBuilder builder = new StringBuilder("digraph table {\n");
		builder.append("rankdir=\"LR\";\n");	
		Set<String> tables = new HashSet<>();
		addTable(table, tables, builder);
		builder.append(table.getName() + ";\n");
		Set<String> aspects = new HashSet<>();
		for (Column column : table.getColumns()) {
			String node = getNodeName(column);
			builder.append(node + "[shape=plaintext label=\"" + column.getName() + "\"];\n");
			builder.append(table.getName() + "->" + node + " [style=invis];\n");			
			String mapperNode = "\"" + table.getName() + "." + column.getName() + "." + column.getFieldName() +  "\"";
			aspects.add(mapperNode);
			builder.append(mapperNode + " [shape=plaintext label=\"" + column.getFieldName() + "\"];\n");
			builder.append(node + "->" + mapperNode + ";\n");
		}
		builder.append("{rank=same;");
		for (Column column : table.getColumns()) {
			builder.append(getNodeName(column) + ";");			
		}
		builder.append("}\n");		
		for (TableConstraint constraint : table.getConstraints()) {
			builder.append(constraint.getName() + " [label=\"" + getLabel(constraint) + "\"];\n");
			builder.append(table.getName() + "->" + constraint.getName() + ";\n");
			for (Column column : constraint.getColumns()) {
				builder.append(constraint.getName() + "->" + getNodeName(column) + ";\n");
			}
			if (constraint.isForeignKey()) {
				ForeignKeyConstraint foreignKey = (ForeignKeyConstraint) constraint;
				String mapperNode = "\"" + foreignKey.getName() + "." + foreignKey.getFieldName()  +  "\"";
				aspects.add(mapperNode);
				builder.append(mapperNode + " [shape=plaintext label=\"" + foreignKey.getFieldName() + "\"];\n");
				builder.append(foreignKey.getName() + "->" + mapperNode + ";\n");
				addTable(foreignKey.getReferencedTable(), tables, builder);
				String dir = foreignKey.getReverseFieldName() == null ? "forward" : "both"; 
				builder.append(mapperNode + "->" + foreignKey.getReferencedTable().getName() + "[dir=" + dir + "];\n");
			}
		}
		builder.append("{rank=same;");
		for (String aspect : aspects) {
			builder.append(aspect + ";");			
		}
		builder.append("}\n");
		builder.append("}");
		System.out.println(builder.toString());
		return builder.toString();
	}
	
	private String getNodeName(Column column) {
		return "\"" + column.getTable().getName() + "." + column.getName() + "\"";
	}
	
	private String getLabel(TableConstraint constraint) {
		if (constraint.isForeignKey()) {
			return "Foreign Key";
		} 
		if (constraint.isPrimaryKey()) {
			return "Primary Key";
		}
		if (constraint.isUnique()) {
			return "Unique";
		}
		return "Unknown Constraint";
	}
	
	private void addTable(Table table , Set<String> tables , StringBuilder builder) {
		if (tables.contains(table.getName())) {
			return;
		}
		tables.add(table.getName());
		builder.append(table.getName() + "[shape=box URL=\"/api/goodies/datamodels/" + table.getComponentName() + "/tables/" + table.getName() + ".svg\"];\n");		
	}
}
