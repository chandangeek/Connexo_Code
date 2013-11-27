package com.elster.jupiter.ids.plumbing;

import com.elster.jupiter.ids.FieldSpec;
import com.elster.jupiter.ids.RecordSpec;
import com.elster.jupiter.ids.TimeSeries;
import com.elster.jupiter.ids.Vault;
import com.elster.jupiter.ids.impl.FieldSpecImpl;
import com.elster.jupiter.ids.impl.RecordSpecImpl;
import com.elster.jupiter.ids.impl.TimeSeriesImpl;
import com.elster.jupiter.ids.impl.VaultImpl;
import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.SqlDialect;

import java.sql.Connection;
import java.sql.SQLException;

import static com.elster.jupiter.ids.plumbing.TableSpecs.*;

public class OrmClientImpl implements OrmClient {
	
	private final DataModel dataModel;
	
	public OrmClientImpl(DataModel dataModel) {
		this.dataModel = dataModel;
	}

	@Override
	public DataMapper<Vault> getVaultFactory() {
		return dataModel.getDataMapper(Vault.class, VaultImpl.class, IDS_VAULT.name());
	}

	@Override
	public DataMapper<RecordSpec> getRecordSpecFactory() {
		return dataModel.getDataMapper(RecordSpec.class, RecordSpecImpl.class, IDS_RECORDSPEC.name());
	}
	
	@Override
	public DataMapper<FieldSpec> getFieldSpecFactory() {
		return dataModel.getDataMapper(FieldSpec.class, FieldSpecImpl.class, IDS_FIELDSPEC.name());
	}
	
	@Override
	public DataMapper<TimeSeries> getTimeSeriesFactory() {	
		return dataModel.getDataMapper(TimeSeries.class , TimeSeriesImpl.class, IDS_TIMESERIES.name());
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
