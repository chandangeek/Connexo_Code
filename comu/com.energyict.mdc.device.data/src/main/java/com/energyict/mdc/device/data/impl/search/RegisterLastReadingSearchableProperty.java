package com.energyict.mdc.device.data.impl.search;

import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;
import com.energyict.mdc.device.data.Register;

import javax.inject.Inject;
import java.time.Instant;

public class RegisterLastReadingSearchableProperty extends AbstractDateSearchableProperty<RegisterLastReadingSearchableProperty> {
    static final String PROPERTY_NAME = "device.register.last.reading";

    @Inject
    public RegisterLastReadingSearchableProperty(PropertySpecService propertySpecService, Thesaurus thesaurus) {
        super(RegisterLastReadingSearchableProperty.class, propertySpecService, thesaurus);
    }

    @Override
    public String getName() {
        return PROPERTY_NAME;
    }

    @Override
    public String getDisplayName() {
        return getThesaurus().getFormat(PropertyTranslationKeys.REGISTER_LAST_READING).format();
    }

    @Override
    public void appendJoinClauses(JoinClauseBuilder builder) {
        builder.addEndDevice();
    }

    /**
        see {@link ChannelLastValueSearchableProperty#toSqlFragment(String, Condition, Instant)}
        and {@link com.energyict.mdc.device.data.impl.DeviceImpl#getLastReadingsFor(Register, Meter)}
     */
    @Override
    public SqlFragment toSqlFragment(Condition condition, Instant now) {
        SqlBuilder builder = new SqlBuilder();
        builder.append(JoinClauseBuilder.Aliases.END_DEVICE + ".id IN (");
        builder.append("select MTR_METERACTIVATION.METERID from MTR_CHANNEL " +
                "right join MTR_METERACTIVATION on MTR_METERACTIVATION.ID = MTR_CHANNEL.METERACTIVATIONID AND MTR_METERACTIVATION.STARTTIME >= ");
        builder.addLong(now.toEpochMilli());
        builder.append(" AND MTR_METERACTIVATION.ENDTIME < ");
        builder.addLong(now.toEpochMilli());
        builder.append(" left join IDS_TIMESERIES on MTR_CHANNEL.TIMESERIESID = IDS_TIMESERIES.ID " +
                "right join MDS_MEASUREMENTTYPE on MDS_MEASUREMENTTYPE.READINGTYPE = MTR_CHANNEL.BULKQUANTITYREADINGTYPEMRID OR MDS_MEASUREMENTTYPE.READINGTYPE = MTR_CHANNEL.MAINREADINGTYPEMRID " +
                "right join DTC_REGISTERSPEC on DTC_REGISTERSPEC.REGISTERTYPEID = MDS_MEASUREMENTTYPE.ID " +
                "where ");
        builder.add(toSqlFragment("IDS_TIMESERIES.LASTTIME", condition, now));
        builder.closeBracket();
        return builder;
    }
}
