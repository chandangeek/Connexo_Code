package com.elster.jupiter.parties.impl;

import java.security.Principal;

import com.elster.jupiter.orm.cache.ComponentCache;

public interface ServiceLocator {
	OrmClient getOrmClient();
	Principal getPrincipal();
	ComponentCache getCache();
}
