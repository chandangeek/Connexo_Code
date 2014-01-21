package com.energyict.mdw.dynamicattributes;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.dynamic.DynamicAttributeOwner;
import com.energyict.mdc.protocol.api.legacy.dynamic.AttributeType;
import com.energyict.mdc.protocol.api.legacy.dynamic.Seed;
import com.energyict.mdc.protocol.api.legacy.dynamic.ValueDomain;
import oracle.jdbc.OraclePreparedStatement;
import oracle.sql.CLOB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LargeStringFactory extends AbstractValueFactory<String> {

    private static final Logger LOGGER = Logger.getLogger(LargeStringFactory.class.getName());

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType) {
        return getEditorSeed(model, attType, attType.getName());
    }

    public Seed getEditorSeed(DynamicAttributeOwner model, AttributeType attType, String aspect) {
        return getEditorSeed(getDefaultEditorFactoryClassName(), "getStringEditor", model, attType, aspect);
    }

    public String getDbType() {
        return "CLOB";
    }

    public String valueFromDb(Object object, ValueDomain domain) {
        if (object != null) {
            CLOB clob = (CLOB) object;
            Reader reader = null;
            BufferedReader myReader = null;

            try {
                reader = clob.getCharacterStream();
                myReader = new BufferedReader(reader);
                StringBuilder result = new StringBuilder();
                String line = myReader.readLine();
                while (line != null) {
                    result.append(line);
                    result.append(System.getProperty("line.separator"));
                    line = myReader.readLine();
                }
                return result.toString();
            } catch (SQLException | IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new ApplicationException(e);
            } finally {
                if (myReader != null) {
                    try {
                        myReader.close();
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }
        } else {
            return null;
        }
    }

    public Object valueToDb(String object) {
        if (object != null) {
            return object;
        }
        return "";
    }

    @Override
    public void setObject(PreparedStatement preparedStatement, int offset, String value) throws SQLException {
        if (preparedStatement instanceof OraclePreparedStatement) {
            OraclePreparedStatement oraclePreparedStatement = (OraclePreparedStatement) preparedStatement;
            oraclePreparedStatement.setStringForClob(offset, value);
        } else {
            preparedStatement.setCharacterStream(offset, new StringReader(value), value.length());
        }
    }

    public String valueFromWS(Object object, ValueDomain domain) throws BusinessException {
        return (String) object;
    }

    public Object valueToWS(String object) {
        return object;
    }

    public Class<String> getValueType() {
        return String.class;
    }

    public int getJdbcType() {
        return java.sql.Types.CLOB;
    }

    protected String doGetHtmlString(String object) {
        return object;
    }

    public boolean isStringLike() {
        return true;
    }

    public String fromStringValue(String stringValue, ValueDomain domain) {
        if (stringValue == null) {
            return "";
        }
        else {
            return stringValue;
        }
    }

    public String toStringValue(String object) {
        return object;
    }

}