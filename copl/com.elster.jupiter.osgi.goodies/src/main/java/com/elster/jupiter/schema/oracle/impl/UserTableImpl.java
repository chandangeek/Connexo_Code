package com.elster.jupiter.schema.oracle.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.elster.jupiter.schema.oracle.UserTable;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

public class UserTableImpl implements UserTable {

	private String name;
	private List<UserColumnImpl> columns = new ArrayList<>();
	private List<UserConstraintImpl> constraints = new ArrayList<>();
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public List<UserColumnImpl> getColumns() {
		return ImmutableList.copyOf(columns);
	}

	public List<UserConstraintImpl> getConstraints() {
		return ImmutableList.copyOf(constraints);
	}
	
	public UserColumnImpl getColumn(String name) {
		for (UserColumnImpl column : columns) {
			if (column.getName().equals(name)) {
				return column;
			}
		}
		return null;
	}
	
	@Override
	public String toString() {
		return "Table: " + name;
	}

	@Override
	public List<String> generate() {
		List<String> code = new ArrayList<>();
		code.add(Joiner.on(" ").join(name,"{"));
		code.add("\t@Override");
		code.add("\tpublic void addTo(DataModel dataModel) {");
		code.add(Joiner.on("").join("\t\tTable<Api.class> table = dataModel.add(", name, ".name(), Api.class);"));
		code.add("\t\ttable.map(Implementation.class);");
		for (UserColumnImpl column : columns) {
			column.generate(code);
		}
		for (UserConstraintImpl constraint : constraints) {
			constraint.generate(code);
		}
		code.add("\t}");
		code.add("},");
		return code;
	}

	public List<UserColumnImpl> getPrimaryKeyColumns() {
		for (UserConstraintImpl constraint : constraints) {
			if (constraint.isPrimaryKey()) {
				return constraint.getColumns();
			}
		}
		return Collections.emptyList();
	}
}
