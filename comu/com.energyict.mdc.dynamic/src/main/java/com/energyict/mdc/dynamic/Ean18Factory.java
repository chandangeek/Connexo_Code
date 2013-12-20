package com.energyict.mdc.dynamic;

import com.energyict.mdc.common.ean.Ean18;
import com.energyict.mdc.common.ApplicationException;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:46)
 */
public class Ean18Factory extends AbstractValueFactory<Ean18> {

    private static final Logger LOGGER = Logger.getLogger(Ean18Factory.class.getName());

    @Override
    public Class<Ean18> getValueType () {
        return Ean18.class;
    }

    @Override
    public String getDatabaseTypeName () {
        return "char(18)";
    }

    @Override
    public int getJdbcType () {
        return java.sql.Types.CHAR;
    }

    @Override
    public Ean18 valueFromDatabase (Object object) throws SQLException {
        try {
            if (object == null || ((String) object).trim().isEmpty()) {
                return null;
            }
            else {
                return new Ean18((String) object);
            }
        }
        catch (ParseException ex) {
            throw new SQLException(ex.toString());
        }
    }

    @Override
    public Object valueToDatabase (Ean18 object) {
        if (object == null) {
            return null;
        }
        else {
            return object.toString();
        }
    }

    @Override
    public Ean18 fromStringValue (String stringValue) {
        try {
            if (stringValue == null || stringValue.length() != 18) {
                return null;
            }
            else {
                return new Ean18(stringValue);
            }
        }
        catch (ParseException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new ApplicationException(e);
        }
    }

    @Override
    public String toStringValue (Ean18 object) {
        if (object == null) {
            return "";
        }
        else {
            return object.toString();
        }
    }

}