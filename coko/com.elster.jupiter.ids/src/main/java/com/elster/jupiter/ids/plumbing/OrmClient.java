package com.elster.jupiter.ids.plumbing;

import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.orm.DataMapper;

import java.sql.Connection;
import java.sql.SQLException;

public interface OrmClient {
	DataMapper<Vault> getVaultFactory();
	DataMapper<RecordSpec> getRecordSpecFactory();
	DataMapper<FieldSpec> getFieldSpecFactory();
	DataMapper<TimeSeries> getTimeSeriesFactory();
	void install(boolean executeDdl , boolean storeMappings);
	Connection getConnection(boolean transactionRequired) throws SQLException;

    boolean isOracle();
}