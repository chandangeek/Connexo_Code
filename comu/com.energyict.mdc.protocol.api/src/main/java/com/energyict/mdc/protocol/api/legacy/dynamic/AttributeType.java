/*
 * AttributeType.java
 *
 * Created on 21 augustus 2003, 8:39
 */

package com.energyict.mdc.protocol.api.legacy.dynamic;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.common.IdBusinessObjectFactory;
import com.energyict.mdc.common.NamedBusinessObject;
import com.energyict.mdc.dynamic.DynamicAttributeOwner;

import java.sql.SQLException;

/**
 * @author Karel
 */
public interface AttributeType extends NamedBusinessObject {

    public ValueDomain getDomain();

    public ValueFactory getValueFactory();

    public Class getValueType();

    public String getDisplayValueType();

    public boolean isValidValue(Object obj);

    public String getDisplayName();

    public String getCustomDisplayName();

    public String getValueFactoryClassName();

    public Object valueFromDb(Object object) throws SQLException;

    public Object valueToDb(Object object);

    public Object valueFromWS(Object object) throws BusinessException;

    public Object valueToWS(Object object);

    public String getDbType();

    public int getJdbcType();

    public String getStructType();

    public boolean requiresIndex();

    public String getIndexType();

    public int getOrdinal();

    public void updateOrdinal(int ordinal) throws SQLException, BusinessException;

    public Seed getEditorSeed(DynamicAttributeOwner model);

    public boolean getLargeString();

    public IdBusinessObjectFactory getObjectFactory();
}
