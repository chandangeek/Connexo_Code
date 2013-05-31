package com.elster.jupiter.ids.plumbing;

import java.sql.*;

import com.elster.jupiter.ids.*;
import com.elster.jupiter.orm.DataMapper;

public interface OrmClient {
	DataMapper<Vault> getVaultFactory();
	DataMapper<RecordSpec> getRecordSpecFactory();
	DataMapper<FieldSpec> getFieldSpecFactory();
	DataMapper<TimeSeries> getTimeSeriesFactory();
	void install(boolean executeDdl , boolean storeMappings);
	Connection getConnection(boolean transactionRequired) throws SQLException;	
}