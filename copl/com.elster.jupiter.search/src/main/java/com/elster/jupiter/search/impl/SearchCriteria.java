package com.elster.jupiter.search.impl;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface SearchCriteria extends Entity {

    String getUser();

    String getCriteria();

    String getDomain();

    String getName();
}
