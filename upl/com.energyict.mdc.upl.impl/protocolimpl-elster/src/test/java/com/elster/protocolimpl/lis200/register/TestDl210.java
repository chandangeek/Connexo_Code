package com.elster.protocolimpl.lis200.register;

import com.elster.protocolimpl.lis200.DL210;
import com.elster.protocolimpl.lis200.objects.ClockObject;
import com.elster.protocolimpl.lis200.registers.HistoricalArchive;
import com.energyict.mdc.upl.nls.NlsService;
import com.energyict.mdc.upl.properties.PropertySpecService;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * test case for historical register reading for class DL210
 * User: heuckeg
 * Date: 20.04.11
 * Time: 16:34
 */
public class TestDl210 extends DL210 {

    public TestDl210(PropertySpecService propertySpecService, NlsService nlsService) {
        super(propertySpecService, nlsService);
    }

    // *******************************************************************************************
    // * I R e g i s t e r R e a d a b l e
    // *******************************************************************************************/
    @Override
    public int getBeginOfDay() {
        return 6;
    }

    @Override
    public HistoricalArchive getHistoricalArchive(int instance) {
        if (instance == 1) {
            return new HistoricalArchive(new MyDl210MonthlyArchive(this, 1));
        } else {
            return null;
        }
    }

    // *******************************************************************************************
    // * P r o t o c o l L i n k
    // *******************************************************************************************/
    @Override
    public Date getCurrentDate() {

        /* for test use a fixed date */
        Calendar c = ClockObject.parseCalendar("2011-04-15,13:10:10", false, getTimeZone());
        return c.getTime();
    }

    @Override
    public TimeZone getTimeZone() {
        return TimeZone.getTimeZone("GMT+1");
    }

    @Override
    public int getMeterIndex() {
        return 1;
    }
}