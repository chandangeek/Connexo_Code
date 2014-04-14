package com.elster.jupiter.schema.oracle.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.schema.oracle.UserTable;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
		code.add("\t\tTable<Api> table = dataModel.add(name(), Api.class);");
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

    @Override
    public void addTo(DataModel dataModel, Optional<String> journalTableName) {
        Table table = dataModel.addTable(getName(), Object.class);
        if (journalTableName.isPresent()) {
            table.setJournalTableName(journalTableName.get());
        }
        for (UserColumnImpl column : columns) {
            column.addTo(table);
        }
        for (UserConstraintImpl constraint : constraints) {
            constraint.addTo(table);
        }
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
