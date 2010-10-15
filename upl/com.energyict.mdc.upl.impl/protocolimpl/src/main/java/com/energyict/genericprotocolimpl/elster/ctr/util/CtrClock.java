package com.energyict.genericprotocolimpl.elster.ctr.util;

import com.energyict.genericprotocolimpl.elster.ctr.GprsRequestFactory;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.DateAndTimeCategory;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 14-okt-2010
 * Time: 16:27:23
 */
public class CtrClock extends AbstractUtilObject {

    private final TimeZone timeZone;

    public CtrClock(GprsRequestFactory requestFactory, Logger logger, TimeZone timeZone) {
        super(requestFactory, logger);
        if (timeZone == null) {
            this.timeZone = TimeZone.getDefault();
            getLogger().warning("No timezone given. Using default timeZone: [" + this.timeZone.getID() + "]");
        } else {
            this.timeZone = timeZone;
        }
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTime(Date timeDate) throws CTRException {
    }

    public Date getTime() throws CTRException {
        AttributeType attributeType = new AttributeType(3);
        AbstractCTRObject object = getRequestFactory().queryRegisters(attributeType, "8.0.0", "1.0.0").get(0);
        if (!(object instanceof DateAndTimeCategory)) {
            throw new CTRException("Expected DateAndTimeCategory object!");
        } else {
            DateAndTimeCategory dateAndTime = (DateAndTimeCategory) object;
            CTRAbstractValue<BigDecimal>[] values = dateAndTime.getValue();
            int ptr = 0;
            int year = values[ptr++].getValue().intValue() + 2000;
            int month = values[ptr++].getValue().intValue() - 1;
            int day = values[ptr++].getValue().intValue();
            ptr++; // Day of week
            int hour = values[ptr++].getValue().intValue();
            int min = values[ptr++].getValue().intValue();
            int sec = values[ptr++].getValue().intValue();

            Calendar cal = Calendar.getInstance(timeZone);
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, day);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, min);
            cal.set(Calendar.SECOND, sec);
            cal.set(Calendar.MILLISECOND, 0);

            return cal.getTime();

        }
    }

}
