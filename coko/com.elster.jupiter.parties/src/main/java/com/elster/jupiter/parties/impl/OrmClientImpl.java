package com.elster.jupiter.parties.impl;

import static com.elster.jupiter.parties.impl.TableSpecs.PRT_PARTY;
import static com.elster.jupiter.parties.impl.TableSpecs.PRT_PARTYINROLE;
import static com.elster.jupiter.parties.impl.TableSpecs.PRT_PARTYREP;
import static com.elster.jupiter.parties.impl.TableSpecs.PRT_PARTYROLE;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.cache.ComponentCache;
import com.elster.jupiter.orm.cache.TypeCache;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyRole;


class OrmClientImpl implements OrmClient {
	
	private final DataModel dataModel;
	private final ComponentCache cache;
	
	OrmClientImpl(DataModel dataModel, ComponentCache cache) {
		this.dataModel = dataModel;
		this.cache = cache;
	}

	@Override
	public DataMapper<Party> getPartyFactory() {
		return dataModel.getDataMapper(Party.class, PRT_PARTY.name());
	}
	
	@Override
	public DataMapper<PartyRepresentation> getPartyRepresentationFactory() {
		return dataModel.getDataMapper(PartyRepresentation.class, PRT_PARTYREP.name());
	}

	@Override
	public DataMapper<PartyInRole> getPartyInRoleFactory() {
		return dataModel.getDataMapper(PartyInRole.class, PRT_PARTYINROLE.name());
	}

	@Override
	public TypeCache<PartyRole> getPartyRoleFactory() {
		return cache.getTypeCache(PartyRole.class, PRT_PARTYROLE.name());
	}
	
	@Override
	public void install(boolean executeDdl,boolean saveMappings) {
		dataModel.install(executeDdl,saveMappings);		
	}

	@Override
	public QueryExecutor<Party> getPartyQuery() {
		return getPartyFactory().with(getPartyInRoleFactory(),getPartyRepresentationFactory());
	}
	
}
