package com.elster.jupiter.ids.impl;

import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.IntervalLengthUnit;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.TimeSeriesEntry;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.SqlDialect;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.Interval;
import com.elster.jupiter.util.time.UtcInstant;
import com.google.common.base.Optional;
import com.google.inject.Provider;

import javax.inject.Inject;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;

@LiteralSql
public class VaultImpl implements Vault {

    private static final int ID_COLUMN_INDEX = 1;
    private static final int FROM_COLUMN_INDEX = 2;
    private static final int TO_COLUMN_INDEX = 3;
    private static final int WHEN_COLUMN_INDEX = 2;
    
    //persistent fields
	private String componentName;
	private long id;
	private String description;
	private UtcInstant minTime;
	private UtcInstant maxTime;
	private int slotCount;
	private boolean localTime;
	private boolean regular;
	private boolean journal;
	private boolean partition;
	private boolean active;
	private long version;
	private UtcInstant createTime;
	private UtcInstant modTime; 
	@SuppressWarnings("unused")
	private String userName;
	
	private final DataModel dataModel;
	private final Clock clock;
	private final Provider<TimeSeriesImpl> timeSeriesProvider;
	
    @Inject
	VaultImpl(DataModel dataModel, Clock clock,Provider<TimeSeriesImpl> timeSeriesProvider)  {
    	this.dataModel = dataModel;
    	this.clock = clock;
    	this.timeSeriesProvider = timeSeriesProvider;
	}
	
	VaultImpl init(String componentName , long id , String description , int slotCount , boolean regular) {
		this.componentName = componentName;
		this.id = id;
		this.description = description;
		this.slotCount = slotCount;
		this.regular = regular;
		this.localTime = regular;
		this.journal = true;
		this.partition = false;
		this.active = false;		
		this.minTime = new UtcInstant(0);
		return this;
	}

	@Override 
	public String getComponentName() {
		return componentName;
	}
	
	@Override
	public long getId() {
		return id;
	}

	public Date getCreateDate() {
		return createTime == null ? null : createTime.toDate();
	}
	
	public Date getModDate() {
		return modTime == null ? null : modTime.toDate();
	}
	
	@Override
	public String toString() {
		return "Vault " + id + ": " + description + "(version: " + version + " created: " + getCreateDate() + " modified: " + getModDate() + ")" ;
	}

	@Override
	public String getDescription() {
		return description;
	}
	
	@Override
	public void setDescription(String description) {
		this.description = description;
		dataModel.update(this , "description" );
	}
	
	@Override
	public Date getMinDate() {
		return minTime.toDate();
	}
	
	@Override
	public Date getMaxDate() {
		return maxTime == null ? null : maxTime.toDate();
	}

	@Override
	public int getSlotCount() {
		return slotCount;
	}
	
	
	@Override
	public boolean hasJournal() {
		return journal;		
	}
	
	@Override
	public boolean isRegular() {
		return regular;
	}
	
	@Override
	public boolean hasLocalTime() {
		return localTime;
	}
	
	@Override
	public boolean isPartitioned() {
		return partition;
	}
	
	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void activate(Date to) {
		try {
			doActivate(to);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
		this.active = true;
		this.maxTime = new UtcInstant(to);
		dataModel.update(this,"active","maxTime");
	}
	
	private void doActivate(Date to) throws SQLException {
		try (Connection connection = getConnection(true)) {	
			try (Statement statement = connection.createStatement()) {
				statement.execute(createTableDdl(to,false));
				if (hasJournal()) {
					statement.execute(createTableDdl(to,true));
				}				
			}			
		}		
	}
		
	private String createTableDdl(Date to , boolean journal) {
		StringBuilder builder = new StringBuilder("create table ");
		builder.append(getTableName(journal));
		builder.append("(TIMESERIESID NUMBER NOT NULL,UTCSTAMP NUMBER NOT NULL,VERSIONCOUNT NUMBER NOT NULL,RECORDTIME NUMBER NOT NULL");
		if (journal) {
			builder.append(",JOURNALTIME NUMBER NOT NULL");
		}
		if (hasLocalTime()) {
			builder.append(",LOCALDATE DATE NOT NULL");
		}
		for (int i = 0 ; i < getSlotCount() ; i++) {
			builder.append(",SLOT");
			builder.append(i);
			builder.append(" NUMBER");
		}
		builder.append(",CONSTRAINT ");
		builder.append(getTablePrimaryKeyConstraintName(journal));
		builder.append(" PRIMARY KEY(TIMESERIESID,UTCSTAMP");
		if (journal) {
			builder.append(",VERSIONCOUNT");
		}
		builder.append("))");
        if (isOracle()) {
            builder.append(" ORGANIZATION INDEX COMPRESS 1 ");
        }
        if (isPartitioned()) {
			builder.append("PARTITION BY RANGE(utcstamp) (PARTITION ");
			builder.append(getPartitionName(to));
			builder.append(" VALUES LESS THAN (");
			builder.append(to.getTime() + 1L);
			builder.append("))");
		}
		return builder.toString();			
	}
	
	private String getTableName() {
		return getTableName(false);
	}
	
	private String getJournalTableName() {
		return getTableName(true);
	}
	
	private String getBaseName(boolean journal) {
		return journal ? "VAULTJRNL" : "VAULT";
	}
	private String getTableName(boolean journal) {
		return "IDS_" + getBaseName(journal) + "_" + getComponentName() + "_" + getId();
	}
	
	private String getTablePrimaryKeyConstraintName(boolean journal) {
		return "IDS_PK" + getBaseName(journal) + "_" + getComponentName() + "_" + getId();
	}
		
	
	private String getPartitionName(Date to) {
		DateFormat df = new  SimpleDateFormat("'P'yyyy_MM_dd'T'HH_mm'Z'");
		df.setTimeZone(TimeZone.getTimeZone("UTC"));
		return df.format(to);
	}	
	
	@Override
	public void addPartition(Date to) {
		if (!isActive() || !to.after(getMaxDate())) {
			throw new IllegalArgumentException();
		}
		if (isPartitioned()) {
			try {
				doAddPartition(to);
			} catch (SQLException ex) {
				throw new UnderlyingSQLFailedException(ex);
			}
		}
		this.maxTime = new UtcInstant(to);
		dataModel.update(this,"maxTime");
	}
	
	private void doAddPartition(Date to) throws SQLException {	
		try (Connection connection = getConnection(true)) {	
			try (Statement statement = connection.createStatement()) {
				statement.execute(addTablePartitionDdl(to, getTableName()));
				if (hasJournal()) {
					statement.execute(addTablePartitionDdl(to, getJournalTableName()));
				}					
			}			
		}
	}
	
	
	private String addTablePartitionDdl(Date to , String table) {
		StringBuilder builder = new StringBuilder("alter table ");
		builder.append(table);
		builder.append(" add partition ");
		builder.append(getPartitionName(to));
		builder.append(" values less than (");
		builder.append(to.getTime() + 1L);
		builder.append(")");		
		return builder.toString();		
	}

	@Override
	public TimeSeriesImpl createRegularTimeSeries(RecordSpec spec, TimeZone timeZone, int intervalLength, IntervalLengthUnit unit, int hourOffset) {
		TimeSeriesImpl timeSeries = timeSeriesProvider.get().init(this, spec,timeZone, intervalLength , unit, hourOffset);
		timeSeries.persist();		
		return timeSeries;
	}

	@Override
	public TimeSeriesImpl createIrregularTimeSeries(RecordSpec spec, TimeZone timeZone) {
		TimeSeriesImpl timeSeries = timeSeriesProvider.get().init(this, spec,timeZone);
		timeSeries.persist();		
		return timeSeries;
	}
	
	@Override
	public boolean isValidDateTime(Date date) {
		long when = date.getTime();
		return isActive() && this.minTime.getTime() < when && when <= this.maxTime.getTime();
	}

	boolean add(TimeSeriesImpl timeSeries, Date timeStamp,boolean overrule, Object[] values) {
		if (!isValidDateTime(timeStamp)) {
			throw new IllegalArgumentException();
		}		
		try {
			return doAdd(timeSeries,timeStamp.getTime(),overrule,values);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
	}
	
	boolean doAdd(TimeSeriesImpl timeSeries , long when , boolean overrule , Object[] values) throws SQLException {
		TimeSeriesEntry timeSeriesEntry = doGet(timeSeries,when);
		if (timeSeriesEntry == null) {
			doInsert(timeSeries,when,values);
			return true;
		} else {
			if (overrule) {
				long now = clock.now().getTime();
				if (hasJournal()) {
					journal(timeSeries,when,now);
				}					
				doUpdate(timeSeries,when,values,now);
				return true;
			} else {
				return false;
			}
		}
	}
	
	private StringBuilder selectSql(TimeSeries timeSeries) {
		StringBuilder builder = selectSql(timeSeries.getRecordSpec());
		builder.append(" TIMESERIESID = ?");	
		return builder;
	}
	
	private String findSql(TimeSeries timeSeries) {
		StringBuilder builder = selectSql(timeSeries);
		builder.append(" AND UTCSTAMP = ?");
		return builder.toString();
	}
	
	private TimeSeriesEntry doGet(TimeSeriesImpl timeSeries, long when) throws SQLException {
		try (Connection connection = getConnection(false)) {
			try (PreparedStatement statement = connection.prepareStatement(findSql(timeSeries))) {
				statement.setLong(1,timeSeries.getId());
				statement.setLong(2, when);
				try (ResultSet rs = statement.executeQuery()) {
					return rs.next() ? new TimeSeriesEntryImpl(timeSeries,rs) : null;
				}
			}
		}		
	}
	
	
	private String insertSql(TimeSeries timeSeries) {
		return insertSql(timeSeries.getRecordSpec());
	}
	
	private void doInsert(TimeSeriesImpl timeSeries, long when , Object[] values) throws SQLException {
		try (Connection connection = getConnection(true)) {
			try (PreparedStatement statement = connection.prepareStatement(insertSql(timeSeries))) {
				int offset = 1;
				statement.setLong(offset++,timeSeries.getId());
				statement.setLong(offset++, when);
				statement.setLong(offset++, clock.now().getTime());
				if (hasLocalTime()) {
					Calendar cal = timeSeries.getStartCalendar(new Date(when));
					statement.setTimestamp(offset++,new Timestamp(cal.getTime().getTime()),cal);
				}
				int i = 0;
				for (FieldSpec fieldSpec : timeSeries.getRecordSpec().getFieldSpecs()) {
					((FieldSpecImpl) fieldSpec).bind(statement , offset++ , values[i++]);
				}
				statement.executeUpdate();				
			}
		}		
	}
	
	private String updateSql(TimeSeries timeSeries) {		
		return updateSql(timeSeries.getRecordSpec());
	}
	
	private void doUpdate(TimeSeriesImpl timeSeries, long when , Object[] values , long now) throws SQLException {
		try (Connection connection = getConnection(true)) {
			try (PreparedStatement statement = connection.prepareStatement(updateSql(timeSeries))) {
				int offset = 1;
				statement.setLong(offset++, now);
				int i = 0;
				for (FieldSpec fieldSpec : timeSeries.getRecordSpec().getFieldSpecs()) {
					((FieldSpecImpl) fieldSpec).bind(statement , offset++ , values[i++]);
				}
				statement.setLong(offset++,timeSeries.getId());
				statement.setLong(offset++, when);
				statement.executeUpdate();
			}
		}			
	}
	
	String journalSql() {
		StringBuilder builder = new StringBuilder("insert into ");
		builder.append(getJournalTableName());
		builder.append("(TIMESERIESID,UTCSTAMP,VERSIONCOUNT,RECORDTIME,JOURNALTIME");
		if (hasLocalTime()) {
			builder.append(",LOCALDATE");
		}
		for (int i = 0 ; i < getSlotCount() ; i++) {
			builder.append(",SlOT");
			builder.append(i);
		}
		builder.append(") (SELECT TIMESERIESID,UTCSTAMP,VERSIONCOUNT,RECORDTIME,?");
		if (hasLocalTime()) {
			builder.append(",LOCALDATE");
		}
		for (int i = 0 ; i < getSlotCount() ; i++) {
			builder.append(",SlOT");
			builder.append(i);
		}
		builder.append(" FROM ");
		builder.append(getTableName());
		builder.append(" WHERE TIMESERIESID = ? AND UTCSTAMP = ?)");
		return builder.toString();
	}
	
	void journal(TimeSeries timeSeries, long when,long now) throws SQLException {
		try (Connection connection = getConnection(true)) {
			try (PreparedStatement statement = connection.prepareStatement(journalSql())) {
				int offset = 1;
				statement.setLong(offset++, now);
				statement.setLong(offset++,timeSeries.getId());
				statement.setLong(offset++, when);
				statement.executeUpdate();
			}
		}	
	}

	List<TimeSeriesEntry> getEntries(TimeSeriesImpl timeSeries, Interval interval) {
		try {
			return doGetEntries(timeSeries, interval);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
	}
	
	Optional<TimeSeriesEntry> getEntry(TimeSeriesImpl timeSeries,Date when) {
		try {
			return Optional.fromNullable(doGetEntry(timeSeries,when));
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
	}
	
	private String rangeSql(TimeSeries timeSeries) {
		StringBuilder builder = selectSql(timeSeries);
        if (isRegular()) {
            builder.append(" AND UTCSTAMP > ? and UTCSTAMP <= ? order by UTCSTAMP");
        } else {
            builder.append(" AND UTCSTAMP >= ? and UTCSTAMP <= ? order by UTCSTAMP");
        }
        return builder.toString();
	}
	
	private String entrySql(TimeSeries timeSeries) {
		StringBuilder builder = selectSql(timeSeries);
		builder.append(" AND UTCSTAMP = ? ");
		return builder.toString();
	}
	
	private List<TimeSeriesEntry> doGetEntries(TimeSeriesImpl timeSeries, Interval interval) throws SQLException {
		List<TimeSeriesEntry> result = new ArrayList<>();
		try (Connection connection = getConnection(false)) {
			try (PreparedStatement statement = connection.prepareStatement(rangeSql(timeSeries))) {
				statement.setLong(ID_COLUMN_INDEX,timeSeries.getId());
				statement.setLong(FROM_COLUMN_INDEX, interval.getStart().getTime());
				statement.setLong(TO_COLUMN_INDEX, interval.getEnd().getTime());
				try (ResultSet rs = statement.executeQuery()) {
					while(rs.next()) {
						result.add(new TimeSeriesEntryImpl(timeSeries,rs));
					}
				}
			}
		}
		return result;
	}
	
	private TimeSeriesEntry doGetEntry(TimeSeriesImpl timeSeries, Date when) throws SQLException {
		try (Connection connection = getConnection(false)) {
			try (PreparedStatement statement = connection.prepareStatement(entrySql(timeSeries))) {
				statement.setLong(ID_COLUMN_INDEX,timeSeries.getId());
				statement.setLong(WHEN_COLUMN_INDEX, when.getTime());
				try (ResultSet rs = statement.executeQuery()) {
					return rs.next() ? new TimeSeriesEntryImpl(timeSeries, rs) : null;					
				}
			}
		}
	}
	
		
	StringBuilder selectSql(RecordSpec recordSpec) {
		StringBuilder builder = new StringBuilder("select timeseriesid , utcstamp , versioncount , recordtime ");
		List<? extends FieldSpec> fieldSpecs = recordSpec.getFieldSpecs();
		for (int i = 0 ; i < fieldSpecs.size() ; i++) {
			builder.append(",SLOT");
			builder.append(i);
		}
		builder.append(" FROM ");
		builder.append(getTableName());
		builder.append(" WHERE");
		return builder;
	}
	
	String insertSql(RecordSpec recordSpec) {
		StringBuilder builder = new StringBuilder("insert into ");
		builder.append(getTableName());
		builder.append(" (");
		builder.append("TIMESERIESID,UTCSTAMP,VERSIONCOUNT,RECORDTIME");
		if (hasLocalTime()) {
			builder.append(",LOCALDATE");
		}		
		List<? extends FieldSpec> fieldSpecs = recordSpec.getFieldSpecs();
		for (int i = 0 ; i < fieldSpecs.size() ; i++) {
			builder.append(",SLOT");
			builder.append(i);
		}
		builder.append(") VALUES (?,?,1,?");
		if (hasLocalTime()) {
			builder.append(",?");			
		}
		for (int i = 0 ; i < fieldSpecs.size() ; i++) {
			builder.append(",?");			
		}
		builder.append(")");
		return builder.toString();
	}
	
	String updateSql(RecordSpec recordSpec) {
		StringBuilder builder = new StringBuilder("update ");
		builder.append(getTableName());
		builder.append(" SET VERSIONCOUNT = VERSIONCOUNT + 1, RECORDTIME = ?");
		List<? extends FieldSpec> fieldSpecs = recordSpec.getFieldSpecs();
		for (int i = 0 ; i < fieldSpecs.size() ; i++) {
			builder.append(",SLOT");
			builder.append(i);
			builder.append(" = ?");
		}
		builder.append(" WHERE TIMESERIESID = ? and UTCSTAMP = ?");
		return builder.toString();
	}

	@Override
	public final boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof VaultImpl)) {
            return false;
        }
        VaultImpl o = (VaultImpl) other;
        return this.componentName.equals(o.componentName) && this.id == o.id;
    }
	
	@Override
	public final int hashCode() {
        return Objects.hash(componentName, id);
	}
		
	private Connection getConnection(boolean transactionRequired) throws SQLException {
		return dataModel.getConnection(transactionRequired);
	}
	
	public void persist() {
		dataModel.persist(this);
	}
	
	private boolean isOracle() {
		return dataModel.getSqlDialect().equals(SqlDialect.ORACLE);
	}

	List<TimeSeriesEntry> getEntriesBefore(TimeSeriesImpl timeSeries, Date when, int entryCount) {
		try {
			return doGetEntriesBefore(timeSeries,when,entryCount);
		} catch (SQLException ex) {
			throw new UnderlyingSQLFailedException(ex);
		}
	}
	
	private List<TimeSeriesEntry> doGetEntriesBefore(TimeSeriesImpl timeSeries, Date when, int entryCount) throws SQLException {
		List<TimeSeriesEntry> result = new ArrayList<>();
		try (Connection connection = getConnection(false)) {
			try (PreparedStatement statement = connection.prepareStatement(entriesBeforeSql(timeSeries))) {
				statement.setLong(ID_COLUMN_INDEX,timeSeries.getId());
				statement.setLong(WHEN_COLUMN_INDEX, when.getTime());
				statement.setInt(3, entryCount);
				try (ResultSet rs = statement.executeQuery()) {
					while (rs.next()) {
						result.add(new TimeSeriesEntryImpl(timeSeries, rs));
					}
				}
			}
		}
		return result;
	}
	
	private String entriesBeforeSql(TimeSeries timeSeries) {
		StringBuilder base = selectSql(timeSeries);
		base.append(" AND UTCSTAMP < ? ORDER BY UTCSTAMP DESC ");
		StringBuilder builder = new StringBuilder ("SELECT * from (");
		builder.append(base.toString());
		builder.append(") where ROWNUM <= ? ");
		return builder.toString();
	}
}
