package com.elster.jupiter.parties.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.cache.TypeCache;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRole;

public interface OrmClient {

    DataModel getDataModel();

    DataMapper<Party> getPartyFactory();

    DataMapper<PartyRepresentationImpl> getPartyRepresentationFactory();

    DataMapper<PartyInRole> getPartyInRoleFactory();

    TypeCache<PartyRole> getPartyRoleFactory();

    void install(boolean executeDdl, boolean saveMappings);
}
