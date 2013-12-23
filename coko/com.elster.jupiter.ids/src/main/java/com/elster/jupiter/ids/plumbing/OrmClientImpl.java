package com.elster.jupiter.ids.plumbing;

import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.SqlDialect;

import java.sql.Connection;
import java.sql.SQLException;

public class OrmClientImpl implements OrmClient {
	
	private final DataModel dataModel;
	
	public OrmClientImpl(DataModel dataModel) {
		this.dataModel = dataModel;
	}

	@Override
	public DataMapper<Vault> getVaultFactory() {
		return dataModel.mapper(Vault.class);
	}

	@Override
	public DataMapper<RecordSpec> getRecordSpecFactory() {
		return dataModel.mapper(RecordSpec.class);
	}
	
	@Override
	public DataMapper<FieldSpec> getFieldSpecFactory() {
		return dataModel.mapper(FieldSpec.class);
	}
	
	@Override
	public DataMapper<TimeSeries> getTimeSeriesFactory() {	
		return dataModel.mapper(TimeSeries.class);
	}
	
	@Override
	public Connection getConnection(boolean transactionRequired) throws SQLException {
		return dataModel.getConnection(transactionRequired);
	}
	
	@Override
	public void install(boolean executeDdl,boolean saveMappings) {
		dataModel.install(executeDdl,saveMappings);		
	}

    @Override
    public boolean isOracle() {
        return dataModel.getSqlDialect().equals(SqlDialect.ORACLE);
    }
}
