/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.fields.impl;

import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.util.conditions.Contains;
import com.elster.jupiter.util.conditions.ListOperator;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.streams.DecoratedStream.decorate;

public class ColumnContainsFragment extends ColumnFragment {

	private final Contains contains;

	public ColumnContainsFragment(ColumnImpl column , Contains contains, String alias) {
		super(column,alias);
		this.contains = contains;
	}
	
	@Override
	public int bind(PreparedStatement statement, int position) throws SQLException {
		for (Object value : contains.getCollection()) {
			position = bind(statement,position,value);
		}
		return position;
	}
	
	@SuppressWarnings("unused")
	@Override
	public String getText() {
		return contains.getCollection().isEmpty() ? (ListOperator.IN.equals(contains.getOperator()) ? "1=0" : "1=1") : decorate(contains.getCollection().stream()).partitionPer(1000)
				.map(this::getSqlText)
				.collect(Collectors.joining(" OR ", "(", ")"));
	}

	private String getSqlText(Collection collection) {
		StringBuilder builder = new StringBuilder();
		builder.append(getColumn().getName(getAlias()));
		builder.append(" ");
		builder.append(contains.getOperator().getSymbol());
		builder.append("  (");
		String separator = "";
		for (Object each : collection) {
			builder.append(separator);
			builder.append("?");
			separator = ",";
		}
		builder.append(") ");
		return builder.toString();
	}

}
