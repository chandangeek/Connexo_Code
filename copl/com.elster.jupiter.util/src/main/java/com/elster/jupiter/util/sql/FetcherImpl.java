/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.util.sql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

class FetcherImpl<T> implements Fetcher<T> {
	
	private final TupleParser<T> tupleParser;
	private final ResultSet resultSet;
	
	FetcherImpl(ResultSet resultSet, TupleParser<T> tupleParser) {
		this.resultSet = resultSet;
		this.tupleParser = tupleParser;
	}
	
	@Override
	public void close() {
		try {
			Statement statement = resultSet.getStatement();
			Connection connection = statement.getConnection();
			close(resultSet);
			close(statement);
			close(connection);
		} catch (SQLException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	private void close(AutoCloseable toBeClosed) {
		try {
			toBeClosed.close();
		} catch (Exception ex) {
			Logger.getLogger(getClass().getName()).log(Level.SEVERE,"Exception in close",ex);
		}
	}

	@Override
	public Iterator<T> iterator() {
		return new FetcherIterator();
	}
	
	private class FetcherIterator implements Iterator<T> {
		private T nextValue;
	
		@Override
		public boolean hasNext() {
			if (nextValue == null) {
				advance();
			}
			return nextValue != null;
		}
	

		private void advance() {
			try {
				if  (resultSet.next()) {
					nextValue = Objects.requireNonNull(tupleParser.construct(resultSet));
				} else {
					nextValue = null;
				}
			} catch (SQLException ex) {
				throw new RuntimeException(ex);
			}
		}
	
		@Override
		public T next() {
			if (nextValue == null) {
				advance();
			}
			if (nextValue == null) {
				throw new NoSuchElementException();
			}
			T result = nextValue;
			nextValue = null;
			return result;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
