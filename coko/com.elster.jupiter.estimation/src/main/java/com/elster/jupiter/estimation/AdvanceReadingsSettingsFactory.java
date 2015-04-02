package com.elster.jupiter.estimation;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.metering.ReadingType;
import com.elster.jupiter.properties.AbstractValueFactory;
import com.elster.jupiter.properties.ValueFactory;
import org.osgi.service.component.annotations.Component;

import java.math.BigDecimal;
import java.sql.SQLException;

@Component(name = "com.elster.jupiter.properties.AdvanceReadingsSettingsFactory", service = {ValueFactory.class}, immediate = true)
public class AdvanceReadingsSettingsFactory extends AbstractValueFactory<AdvanceReadingsSettings> {

    private MeteringService meteringService;

    public AdvanceReadingsSettingsFactory(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    public Class<AdvanceReadingsSettings> getValueType () {
        return AdvanceReadingsSettings.class;
    }

    @Override
    public String getDatabaseTypeName () {
        return "varchar2(256)";
    }

    @Override
    public int getJdbcType () {
        return java.sql.Types.VARCHAR;
    }


    @Override
    public AdvanceReadingsSettings valueFromDatabase (Object object) throws SQLException {
        return fromStringValue((String) object);
    }

    @Override
    public Object valueToDatabase (AdvanceReadingsSettings advanceReadingsSettings) {
        return toStringValue(advanceReadingsSettings);
    }

    @Override
    public AdvanceReadingsSettings fromStringValue (String stringValue) {
        if (NoneAdvanceReadingsSettings.NONE_ADVANCE_READINGS_SETTINGS.equals(stringValue)) {
            return new NoneAdvanceReadingsSettings();
        } else if (BulkAdvanceReadingsSettings.BULK_ADVANCE_READINGS_SETTINGS.equals(stringValue)) {
            return new BulkAdvanceReadingsSettings();
        } else {
            ReadingType readingType = meteringService.getReadingType(stringValue).orElse(null);
            return (readingType == null) ?
                    new NoneAdvanceReadingsSettings() :
                    new ReadingTypeAdvanceReadingsSettings(readingType);
        }
    }

    @Override
    public String toStringValue (AdvanceReadingsSettings advanceReadingsSettings) {
        return advanceReadingsSettings.toString();
    }

}
