package com.elster.jupiter.ids.impl;

import java.sql.*;

import com.elster.jupiter.ids.*;
import com.elster.jupiter.orm.DataMapper;

interface OrmClient {
	DataMapper<Vault> getVaultFactory();
	DataMapper<RecordSpec> getRecordSpecFactory();
	DataMapper<FieldSpec> getFieldSpecFactory();
	DataMapper<TimeSeries> getTimeSeriesFactory();
	void install(boolean executeDdl , boolean storeMappings);
	Connection getConnection(boolean transactionRequired) throws SQLException;	
}