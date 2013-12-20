package com.energyict.mdc.dynamic;

import com.energyict.mdc.common.ApplicationException;

import java.sql.SQLException;
import java.sql.Types;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:28)
 */
public class DateFactory extends AbstractValueFactory<Date> {

    private static final Logger LOGGER = Logger.getLogger(DateFactory.class.getName());

    public Class<Date> getValueType() {
        return Date.class;
    }

    @Override
    public String getDatabaseTypeName () {
        return "date";
    }

    public int getJdbcType() {
        return Types.DATE;
    }

    @Override
    public Date valueFromDatabase (Object object) throws SQLException {
        if (object != null) {
            return new Date(((Date) object).getTime());
        }
        else {
            return null;
        }
    }

    @Override
    public Object valueToDatabase (Date object) {
        if (object == null) {
            return null;
        }
        else {
            return new java.sql.Date(object.getTime());
        }
    }

    @Override
    public Date fromStringValue(String stringValue) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if (stringValue == null || stringValue.isEmpty()) {
                return null;
            }
            else {
                return format.parse(stringValue);
            }
        } catch (ParseException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(), e);
            throw new ApplicationException(e);
        }
    }

    @Override
    public String toStringValue(Date object) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        if (object == null) {
            return "";
        }
        else {
            return format.format(object);
        }
    }

}