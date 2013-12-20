package com.energyict.mdc.dynamic;

import com.energyict.mdc.common.ApplicationException;
import org.joda.time.DateTimeConstants;

import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-11-29 (17:34)
 */
public class DateAndTimeFactory extends AbstractValueFactory<Date> {

    private static final Logger LOGGER = Logger.getLogger(DateAndTimeFactory.class.getName());

    @Override
    public Class<Date> getValueType() {
        return Date.class;
    }

    @Override
    public String getDatabaseTypeName () {
        return "number";
    }

    @Override
    public int getJdbcType() {
        return java.sql.Types.INTEGER;
    }

    @Override
    public Date valueFromDatabase (Object object) throws SQLException {
        return this.valueFromDatabase((Number) object);
    }

    private Date valueFromDatabase (Number number) {
        if (number != null) {
            return new Date(number.longValue() * DateTimeConstants.MILLIS_PER_SECOND);
        }
        else {
            return null;
        }
    }

    @Override
    public Object valueToDatabase (Date object) {
        if (object != null) {
            return object.getTime() / DateTimeConstants.MILLIS_PER_SECOND;
        }
        else {
            return null;
        }
    }

    @Override
    public Date fromStringValue(String stringValue) {
        if (stringValue == null || stringValue.isEmpty()) {
            return null;
        }
        else {
            try {
                return this.valueFromDatabase(new Long(stringValue));
            }
            catch (NumberFormatException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
                throw new ApplicationException(e);
            }
        }
    }

    @Override
    public String toStringValue(Date object) {
        if (object != null) {
            return valueToDatabase(object).toString();
        }
        else {
            return "";
        }
    }

}