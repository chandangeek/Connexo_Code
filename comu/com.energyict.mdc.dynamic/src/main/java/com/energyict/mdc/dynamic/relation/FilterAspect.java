package com.energyict.mdc.dynamic.relation;

import aQute.bnd.annotation.ProviderType;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-14 (16:53)
 */
@ProviderType
public interface FilterAspect {

    public String getName();

    public String getDisplayName();

    public String getColumnName();

    public int getJdbcType();

    public Class getValueType();

    public Object valueToDb(Object object);

}