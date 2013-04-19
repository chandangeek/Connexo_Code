package com.elster.jupiter.ids.impl;

import java.util.Date;
import java.util.*;
import java.sql.*;

import com.elster.jupiter.ids.*;
import com.elster.jupiter.orm.PersistenceException;

class TimeSeriesDataStorerImpl implements TimeSeriesDataStorer {
	final private boolean overrules;
	final private Map<RecordSpecInVault,SlaveTimeSeriesDataStorer> storerMap = new HashMap<>();
	final StorerStatsImpl stats = new StorerStatsImpl();
	final private Map<Long, TimeSeries> lockedTimeSeriesMap = new HashMap<>();

	public TimeSeriesDataStorerImpl(boolean overrules) {
		this.overrules = overrules;
	}
	
	@Override
	public boolean overrules() {
		return overrules;
	}

	@Override
	public void add(TimeSeries timeSeries, Date timeStamp, Object... values) {
		if (!timeSeries.isValidDateTime(timeStamp)) {
			throw new IllegalArgumentException();
		}
		TimeSeriesEntryImpl entry = new TimeSeriesEntryImpl(timeSeries, timeStamp, values);
		RecordSpecInVault recordSpecInVault = new RecordSpecInVault(timeSeries);
		SlaveTimeSeriesDataStorer slaveStorer = storerMap.get(recordSpecInVault);
		if (slaveStorer == null) {
			slaveStorer = new SlaveTimeSeriesDataStorer(entry);
			storerMap.put(recordSpecInVault,slaveStorer);
		} else {
			slaveStorer.add(entry);
		}
		stats.add(entry);
	}
			
	@Override
	public StorerStats execute() {		
		try {
			stats.start();
			doExecute();			
		} catch (SQLException ex) {
			throw new PersistenceException(ex);
		} finally {
			stats.stop();
		}
		return stats;
	}
	
	private List<TimeSeries> getAllTimeSeries() {
		List<TimeSeries> result = new ArrayList<>();
		for (SlaveTimeSeriesDataStorer storer : storerMap.values()) {
			result.addAll(storer.getAllTimeSeries());
		}
		Collections.sort(result,getTimeSeriesComparator());
		return result;
	}
	
	private Comparator<TimeSeries> getTimeSeriesComparator() {
		return new Comparator<TimeSeries>() {
			public int compare(TimeSeries t1 , TimeSeries t2) {
				long t1Id = t1.getId();
				long t2Id = t2.getId();
				return t1Id == t2Id ? 0 : ((t1Id < t2Id) ? -1 : 1); 
			}
		};
	}
	
	private void doExecute() throws SQLException {		
		for (TimeSeries timeSeries : getAllTimeSeries())   {
			long key = timeSeries.getId();
			lockedTimeSeriesMap.put(key,((TimeSeriesImpl) timeSeries).lock());
		}
		for(SlaveTimeSeriesDataStorer storer : storerMap.values()) {
			storer.updateTimeSeries(lockedTimeSeriesMap);
			storer.execute(stats,overrules());			
		}	
	}
		
	private static class RecordSpecInVault {
		private final Vault vault;
		private final RecordSpec recordSpec;
		
		RecordSpecInVault(TimeSeries timeSeries) {
			this.vault = timeSeries.getVault();
			this.recordSpec = timeSeries.getRecordSpec();
		}
		
		@Override
		public boolean equals(Object other) {
			try {
				RecordSpecInVault o = (RecordSpecInVault) other;
				return this.vault.equals(o.vault) && this.recordSpec.equals(o.recordSpec);
			} catch (ClassCastException ex) {
				return false;
			}
		}
		
		@Override
		public int hashCode() {
			return vault.hashCode() ^ recordSpec.hashCode();
		}			
	}
	
	private static class SlaveTimeSeriesDataStorer {
		final Map<Long, SingleTimeSeriesStorer> storerMap = new HashMap<>();
		final VaultImpl vault;
		final RecordSpec recordSpec;
		
		SlaveTimeSeriesDataStorer(TimeSeriesEntryImpl entry) {
			SingleTimeSeriesStorer storer = new SingleTimeSeriesStorer(entry);
			storerMap.put(entry.getTimeSeries().getId(),storer);
			vault = (VaultImpl) entry.getTimeSeries().getVault();
			recordSpec = entry.getTimeSeries().getRecordSpec();
		}
		
		List<TimeSeries> getAllTimeSeries() {
			List<TimeSeries> result = new ArrayList<>();
			for (SingleTimeSeriesStorer each : storerMap.values()) {
				result.add(each.getTimeSeries());
			}
			return result;
		}
		
		void add(TimeSeriesEntryImpl entry) {
			Long key = entry.getTimeSeries().getId();
			SingleTimeSeriesStorer storer = storerMap.get(key);
			if (storer == null) {
				storer = new SingleTimeSeriesStorer(entry);
				storerMap.put(key,storer);
			} else {
				storer.add(entry);
			}			
		}
				
		String selectSql(Collection<SingleTimeSeriesStorer> storers) {
			StringBuilder builder = vault.selectSql(recordSpec);
			String separator = " ";
			for (SingleTimeSeriesStorer storer : storers) {
				builder.append(separator);
				storer.appendWhereClause(builder);
				separator = " or ";
			}
			builder.append(" ORDER BY TIMESERIESID , UTCSTAMP ");
			return builder.toString();
		}
		
		String insertSql() {
			return vault.insertSql(recordSpec);
		}
		
		String updateSql() {
			return vault.updateSql(recordSpec);
		}
		
		String journalSql() {
			return vault.journalSql();
		}
		
		void updateTimeSeries(Map<Long,TimeSeries> timeSeriesMap) {
			for (SingleTimeSeriesStorer storer : storerMap.values()) {
				storer.updateTimeSeries(timeSeriesMap.get(storer.getTimeSeries().getId()));
			}
		}
		
		void setOldEntries(Connection connection) throws SQLException {
			Collection<SingleTimeSeriesStorer> storers = storerMap.values();
			try (PreparedStatement statement = connection.prepareStatement(selectSql(storers))) {
				int offset = 1;
				for (SingleTimeSeriesStorer storer : storers) {
					offset = storer.bindWhere(statement, offset);						
				}
				try (ResultSet rs = statement.executeQuery()) {
					SingleTimeSeriesStorer storer = null;
					while (rs.next()) {				
						long key = rs.getLong(1);
						if (storer == null || storer.getTimeSeries().getId() != key) {
							storer = storerMap.get(key);
						}
						storer.add(rs);
					}
				}
			}
		}
		
		void addInserts(Connection connection,long now) throws SQLException {
			try (PreparedStatement statement = connection.prepareStatement(insertSql())) {
				for (SingleTimeSeriesStorer storer : storerMap.values()) {
					storer.addInserts(statement,now);						
				}
				statement.executeBatch();
			}
		}
		
		void addUnJournaledUpdates(Connection connection, long now) throws SQLException {
			try (PreparedStatement statement = connection.prepareStatement(updateSql())) {
				for (SingleTimeSeriesStorer storer : storerMap.values()) {
					storer.addUpdates(statement,now);
				}
				statement.executeBatch();
			}	
		}
		
		void addJournaledUpdates(Connection connection, long now) throws SQLException {
			try (PreparedStatement updateStatement = connection.prepareStatement(updateSql())) {
				try (PreparedStatement journalStatement = connection.prepareStatement(journalSql())) {
					for (SingleTimeSeriesStorer storer : storerMap.values()) {
						storer.addUpdates(updateStatement,journalStatement,now);
					}				
					journalStatement.executeBatch();
					updateStatement.executeBatch();
				}
			}	
		}
		
		void execute(StorerStatsImpl stats , boolean overrules) throws SQLException {
			try (Connection connection = Bus.getConnection(true)) {
				setOldEntries(connection);
				long now = System.currentTimeMillis();
				addInserts(connection,now);
				if (overrules) {
					if (vault.hasJournal()) {
						addJournaledUpdates(connection,now);						
					} else {
						addUnJournaledUpdates(connection,now);
					}
				}				
			}
			for ( SingleTimeSeriesStorer storer : storerMap.values()) {
				storer.updateTimeSeries();
				stats.addCount(storer.insertCount,storer.updateCount);				
			}
		}
	}

	private static class SingleTimeSeriesStorer {
		final private List<TimeSeriesEntryImpl> newEntries = new ArrayList<>();
		final private List<TimeSeriesEntryImpl> oldEntries = new ArrayList<>();
		private Date minDate;
		private Date maxDate;
		private int insertCount = 0;
		private int updateCount = 0;
		private TimeSeries timeSeries;
		
		SingleTimeSeriesStorer(TimeSeriesEntryImpl entry) {
			newEntries.add(entry);
			timeSeries = entry.getTimeSeries();
		}
		
		void add(TimeSeriesEntryImpl entry) {
			for (TimeSeriesEntryImpl each : newEntries) {
				if (each.getTimeStamp().equals(entry.getTimeStamp())) {
					throw new IllegalArgumentException();
				}
			}
			newEntries.add(entry);
		}
		
		void updateTimeSeries(TimeSeries timeSeries) {
			this.timeSeries = timeSeries;
		}
		
		TimeSeries getTimeSeries() {
			return timeSeries;
		}
		
		Date getFirstDate() {
			Date result = null;
			for ( TimeSeriesEntryImpl entry : newEntries) {
				if (result == null || entry.getTimeStamp().before(result)) {
					result = entry.getTimeStamp();
				} 
			}
			return result;
		}
		
		Date getLastDate() {
			Date result = null;
			for ( TimeSeriesEntryImpl entry : newEntries) {
				if (result == null || entry.getTimeStamp().after(result)) {
					result = entry.getTimeStamp();
				} 
			}
			return result;
		}
		
		void appendWhereClause(StringBuilder builder) {
			builder.append(" (TIMESERIESID = ? AND UTCSTAMP between ? and ?)");
		}
		
		int bindWhere(PreparedStatement statement,int offset) throws SQLException {
			statement.setLong(offset++,getTimeSeries().getId());
			statement.setLong(offset++,getFirstDate().getTime());
			statement.setLong(offset++,getLastDate().getTime());
			return offset;
		}
	
		void add(ResultSet rs) throws SQLException {
			oldEntries.add(new TimeSeriesEntryImpl(getTimeSeries(),rs));
		}
		
		boolean isInsert(TimeSeriesEntryImpl entry) {
			for (TimeSeriesEntryImpl oldEntry : oldEntries) {
				if (oldEntry.getTimeStamp().equals(entry.getTimeStamp())) {
					return false;
				}					
			}
			return true;
		}
		
		boolean isUpdate(TimeSeriesEntryImpl entry) {
			for (TimeSeriesEntryImpl oldEntry : oldEntries) {
				if (entry.getTimeStamp().equals(oldEntry.getTimeStamp())) {
					return !entry.matches(oldEntry);
				}
			}
			return false;
		}
		
		void addInserts(PreparedStatement statement, long now) throws SQLException {
			for (TimeSeriesEntryImpl entry : newEntries) {
				if (isInsert(entry)) {
					entry.insert(statement,now);
					insertCount++;
					statement.addBatch();
				}
				if (minDate == null || entry.getTimeStamp().before(minDate)) {
					minDate = entry.getTimeStamp();
				}
				if (maxDate == null || entry.getTimeStamp().after(maxDate)) {
					maxDate = entry.getTimeStamp();
				}
			}			
		}
		
		void addUpdates(PreparedStatement statement, long now) throws SQLException {
			addUpdates(statement,null,now);
		}
		
		void addUpdates(PreparedStatement updateStatement, PreparedStatement journalStatement , long now) throws SQLException {
			for (TimeSeriesEntryImpl entry : newEntries) {
				if (isUpdate(entry)) {
					if (journalStatement != null) {
						entry.journal(journalStatement,now);
						journalStatement.addBatch();
					}
					entry.update(updateStatement,now);
					updateCount++;
					updateStatement.addBatch();
				}
			}
		}
		
		void updateTimeSeries() {
			if (insertCount + updateCount > 0) {
				((TimeSeriesImpl) getTimeSeries()).updateRange(minDate,maxDate);
			}
		}
	}
	
}	
