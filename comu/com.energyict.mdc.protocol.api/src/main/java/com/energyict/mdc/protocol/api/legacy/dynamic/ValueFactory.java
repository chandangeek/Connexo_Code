/*
 * ValueFactory.java
 *
 * Created on 26 augustus 2003, 16:02
 */

package com.energyict.mdc.protocol.api.legacy.dynamic;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.dynamic.DynamicAttributeOwner;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 *
 * @author  Karel
 */
public interface ValueFactory<S> extends Serializable {

     public String getDbType();
     public boolean supportsReferentialIntegrity(RelationAttributeType attribType);
     public S valueFromDb(Object object, ValueDomain domain) throws java.sql.SQLException;
     public Object valueToDb(S object);
     public S valueFromWS(Object object, ValueDomain domain) throws BusinessException;
     public Object valueToWS(S object);
     public Class<S> getValueType();
     public int getJdbcType();
     public ValueAdapter getAdapter(DynamicAttributeOwner model , String aspect);
     public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType);
     public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType, String aspect);
     public Seed getCellRendererSeed();
     public String getHtmlString(S object);
     public boolean isStringLike();
     public boolean isNumeric();
     public boolean isTime();
     public boolean isReference();
     public String getStructType();
     public boolean requiresIndex();
     public String getIndexType();
     public void setObject(PreparedStatement preparedStatement, int offset, S value) throws SQLException;
     public S fromStringValue(String stringValue, ValueDomain domain);
     public String toStringValue(S object);

}
