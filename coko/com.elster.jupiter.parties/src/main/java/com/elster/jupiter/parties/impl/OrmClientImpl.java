package com.elster.jupiter.parties.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.cache.TypeCache;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRole;

import static com.elster.jupiter.parties.impl.TableSpecs.*;


class OrmClientImpl implements OrmClient {
	
	private final DataModel dataModel;
	
	OrmClientImpl(DataModel dataModel) {
		this.dataModel = dataModel;
	}
	
	@Override
	public DataModel getDataModel() {
		return dataModel;
	}

	@Override
	public DataMapper<Party> getPartyFactory() {
		return dataModel.getDataMapper(Party.class, PartyImpl.implementers, PRT_PARTY.name());
	}
	
	@Override
	public DataMapper<PartyRepresentationImpl> getPartyRepresentationFactory() {
		return dataModel.getDataMapper(PartyRepresentationImpl.class, PartyRepresentationImpl.class, PRT_PARTYREP.name());
	}

	@Override
	public DataMapper<PartyInRole> getPartyInRoleFactory() {
		return dataModel.getDataMapper(PartyInRole.class, PartyInRoleImpl.class,PRT_PARTYINROLE.name());
	}

	@Override
	public TypeCache<PartyRole> getPartyRoleFactory() {
		return Bus.getCache().getTypeCache(PartyRole.class, PartyRoleImpl.class , PRT_PARTYROLE.name());
	}
	
	@Override
	public void install(boolean executeDdl,boolean saveMappings) {
		dataModel.install(executeDdl,saveMappings);		
	}
}
