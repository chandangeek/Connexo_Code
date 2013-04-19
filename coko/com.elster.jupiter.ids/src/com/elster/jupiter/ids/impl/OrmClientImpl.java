package com.elster.jupiter.ids.impl;

import java.sql.*;

import com.elster.jupiter.ids.*;
import com.elster.jupiter.orm.*;

import static com.elster.jupiter.ids.impl.TableSpecs.*;

class OrmClientImpl implements OrmClient {
	
	private static final String COMPONENTNAME = "IDS";
	
	private final OrmService service;
	
	OrmClientImpl(OrmService service) {
		this.service = service;
	}

	
	@Override
	public DataMapper<Vault> getVaultFactory() {
		return service.getDataMapper(Vault.class, VaultImpl.class , COMPONENTNAME , IDS_VAULT.name());
	}

	@Override
	public DataMapper<RecordSpec> getRecordSpecFactory() {
		return service.getDataMapper(RecordSpec.class, RecordSpecImpl.class , COMPONENTNAME , IDS_RECORDSPEC.name());
	}
	
	@Override
	public DataMapper<FieldSpec> getFieldSpecFactory() {
		return service.getDataMapper(FieldSpec.class, FieldSpecImpl.class , COMPONENTNAME , IDS_FIELDSPEC.name());
	}
	
	@Override
	public DataMapper<TimeSeries> getTimeSeriesFactory() {	
		return service.getDataMapper(TimeSeries.class , TimeSeriesImpl.class, COMPONENTNAME , IDS_TIMESERIES.name());
	}
	
	@Override
	public Connection getConnection(boolean transactionRequired) throws SQLException {
		return this.service.getConnection(transactionRequired);
	}
	
	@Override
	public void install(boolean executeDdl,boolean saveMappings) {
		service.install(createComponent(),executeDdl,saveMappings);		
	}
	
	private Component createComponent() {
		Component result = service.newComponent(COMPONENTNAME,"Interval Data Store");
		for (TableSpecs spec : TableSpecs.values()) {
			spec.addTo(result);			
		}
		return result;
	}
				
}
