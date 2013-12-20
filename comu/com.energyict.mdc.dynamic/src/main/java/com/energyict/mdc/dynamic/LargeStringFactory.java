package com.energyict.mdc.dynamic;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.common.SqlBuilder;
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

/**
 * Provides an implementation for the {@link ValueFactory} interface
 * for very large String values.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (16:57)
 */
public class LargeStringFactory extends StringFactory {

    private static final Logger LOGGER = Logger.getLogger(LargeStringFactory.class.getName());

    @Override
    public String getDatabaseTypeName () {
        return "CLOB";
    }

    @Override
    public int getJdbcType () {
        return java.sql.Types.CLOB;
    }

    public String valueFromDb (Object object) {
        if (object != null) {
            CLOB clob = (CLOB) object;
            Reader reader;
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
            }
            catch (SQLException | IOException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new ApplicationException(e);
            }
            finally {
                if (myReader != null) {
                    try {
                        myReader.close();
                    }
                    catch (IOException e) {
                        LOGGER.log(Level.SEVERE, e.getMessage(), e);
                    }
                }
            }
        }
        else {
            return null;
        }
    }

    @Override
    public void bind(PreparedStatement statement, int offset, String value) throws SQLException {
        if (statement instanceof OraclePreparedStatement) {
            OraclePreparedStatement oraclePreparedStatement = (OraclePreparedStatement) statement;
            oraclePreparedStatement.setStringForClob(offset, value);
        } else {
            statement.setCharacterStream(offset, new StringReader(value), value.length());
        }
    }

    @Override
    public void bind(SqlBuilder builder, String value) {
        builder.bindLargeString(value);
    }

    public Object valueToDb (String object) {
        if (object != null) {
            return object;
        }
        return "";
    }

}