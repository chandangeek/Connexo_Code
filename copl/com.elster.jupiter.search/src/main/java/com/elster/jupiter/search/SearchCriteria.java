package com.elster.jupiter.search;

import aQute.bnd.annotation.ProviderType;
import com.elster.jupiter.search.impl.Entity;

@ProviderType
public interface SearchCriteria extends Entity {

    String getUser();

    String getCriteria();

    String getDomain();

    String getName();

    long getId();
}
