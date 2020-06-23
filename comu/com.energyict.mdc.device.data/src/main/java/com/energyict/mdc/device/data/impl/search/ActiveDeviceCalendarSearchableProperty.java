/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.calendar.CalendarService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.nls.TranslationKey;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.dynamic.PropertySpecService;

import javax.inject.Inject;
import java.time.Instant;


public class ActiveDeviceCalendarSearchableProperty extends AbstractDeviceCalendarSearchableProperty {

    public static final String FIELD_NAME = "device.calendar";

    @Inject
    public ActiveDeviceCalendarSearchableProperty(CalendarService calendarService, PropertySpecService mdcPropertySpecService,
                                                  Thesaurus thesaurus) {
        super(calendarService, mdcPropertySpecService, thesaurus);
    }


    @Override
    protected TranslationKey getNameTranslationKey() {
        return PropertyTranslationKeys.DEVICE_ACTIVE_TIME_OF_USE;
    }

    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder sqlBuilder = new SqlBuilder();
        sqlBuilder.append(" dev.id IN ");
        sqlBuilder.openBracket();
        sqlBuilder.append(" SELECT actcal.device FROM ddc_active_calendar actcal ");
        sqlBuilder.append(" JOIN dtc_devicetypecalendarusage cusage ON cusage.id = actcal.allowed_calendar ");
        sqlBuilder.append("AND actcal.starttime <= ");
        sqlBuilder.addLong(now.toEpochMilli());
        sqlBuilder.append(" AND actcal.endtime > ");
        sqlBuilder.addLong(now.toEpochMilli());
        sqlBuilder.append(" WHERE ");
        sqlBuilder.add(this.toSqlFragment("cusage.calendar", condition, now));
        sqlBuilder.closeBracket();
        return sqlBuilder;
    }


    @Override
    protected String getFieldName() {
        return FIELD_NAME;
    }

}