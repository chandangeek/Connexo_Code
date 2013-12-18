package com.elster.jupiter.parties.impl;

import com.elster.jupiter.orm.DataMapper;
import com.elster.jupiter.orm.QueryExecutor;
import com.elster.jupiter.orm.cache.TypeCache;
import com.elster.jupiter.parties.Party;
import com.elster.jupiter.parties.PartyInRole;
import com.elster.jupiter.parties.PartyRepresentation;
import com.elster.jupiter.parties.PartyRole;

public interface OrmClient {
    DataMapper<Party> getPartyFactory();
    DataMapper<PartyRepresentation> getPartyRepresentationFactory();
    DataMapper<PartyInRole> getPartyInRoleFactory();
    TypeCache<PartyRole> getPartyRoleFactory();
    QueryExecutor<Party> getPartyQuery();
    void install(boolean executeDdl, boolean saveMappings);
}
