package com.energyict.mdc.dynamic.relation.impl;

import java.time.Clock;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-17 (10:38)
 */
public interface ServiceLocator {

    public OrmClient getOrmClient();

    public Clock clock();

}