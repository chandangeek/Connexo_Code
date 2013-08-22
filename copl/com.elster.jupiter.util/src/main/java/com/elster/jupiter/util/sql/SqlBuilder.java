package com.elster.jupiter.util.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Class that assists in building sql statements.
 */
public final class SqlBuilder implements SqlFragment {
	
	private final StringBuilder builder;
	private final List<SqlFragment> fragments = new ArrayList<>();

    /**
     * Creates a new empty instance.
     */
	public SqlBuilder() {
		this.builder = new StringBuilder();
	}

    /**
     * Creates a new instance initialized with the given base.
     * @param base
     */
	public SqlBuilder(String base) {
		this.builder = new StringBuilder(base);
	}
	
	public void add(SqlFragment fragment) {
		builder.append(fragment.getText());
		fragments.add(fragment);
	}
	
	public void addObject(Object value) {
		add(new ObjectFragment(value));
	}
	
	public void addInt(int value) {
		add(new IntFragment(value));
	}
	
	public void addLong(long value) {
		add(new LongFragment(value));
	}
	
	public StringBuilder getBuffer() {
		return builder;
	}
	
	public PreparedStatement prepare(Connection connection ) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(builder.toString());
		boolean failed = true;
		try {
			bind(statement,1);
			failed = false;
		} finally {
			if (failed) {
				statement.close();
			}
		}
		return statement;
	}

	public void space() {
		builder.append(" ");		
	}
	
	public void append(String string) {
		builder.append(string);
	}
	
	public void spaceOpenBracket() {
		space();
		openBracket();
	}
	
	public void openBracket() {
		builder.append("(");
	}
	
	public void closeBracketSpace() {
		closeBracket();
		space();
	}
	
	public void closeBracket() {
		builder.append(")");
	}
	
	
	@Override
	public String toString() {
		return builder.toString();
	}

	@Override
	public int bind(PreparedStatement statement, int position) throws SQLException {
		for (SqlFragment each : fragments) {
			position = each.bind(statement, position);
		}
		return position;
	}

	@Override
	public String getText() {
		return builder.toString();
	}
	
	public SqlBuilder asPageBuilder(int from , int to) {
		SqlBuilder result = new SqlBuilder("select * from (select x.*, ROWNUM rnum from (");
		result.add(this);
		result.append(") x where ROWNUM <= ");
		result.addInt(to);
		result.append(") where rnum >= ");
		result.addInt(from);
		return result;
	}
	
	private abstract static class SimpleFragment implements SqlFragment {
		SimpleFragment() {
		}
		
		@Override
        public final String getText() {
			return " ? ";
		}
	}
	
	private static class ObjectFragment extends SimpleFragment {
		private final Object value;
		
		ObjectFragment(Object value) {
			this.value = value;
		}

		@Override
		public int bind(PreparedStatement statement, int position) throws SQLException {
			statement.setObject(position, value);
			return position + 1;
			
		}
		
	}
	
	private static class IntFragment extends SimpleFragment {
		 private final int value;
		 
		 IntFragment (int value) {
			 this.value = value;
		 }
		 

		@Override
		public int bind(PreparedStatement statement, int position) throws SQLException {
			statement.setInt(position, value);
			return position + 1;
		}
			
	}
	
	private static class LongFragment extends SimpleFragment {
		 private final long value;
		 
		 LongFragment (long value) {
			 this.value = value;
		 }

		@Override
		public int bind(PreparedStatement statement, int position) throws SQLException {
			statement.setLong(position, value);
			return position + 1;
		}

			
	}
}
