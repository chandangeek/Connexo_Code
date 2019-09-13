package com.elster.jupiter.issue.share;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.elster.jupiter.issue.impl.module.TranslationKeys;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.groups.EndDeviceGroup;
import com.elster.jupiter.metering.groups.MeteringGroupsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.properties.PropertySpecService;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.EndDeviceGroupPropertyFactory;
import com.elster.jupiter.util.sql.SqlBuilder;

public abstract class AbstractCloseIssueAction extends AbstractIssueAction {

    public static final String PROPERTY_NAME = "CloseIssueAction.excludedGroups";

    public static final String VALUE_SEPARATOR = ",";

    public static final Logger LOG = Logger.getLogger(AbstractCloseIssueAction.class.getName());

    private final MeteringGroupsService meteringGroupsService;
    private final Clock clock;

    protected AbstractCloseIssueAction(DataModel dataModel, Thesaurus thesaurus,
            PropertySpecService propertySpecService, MeteringGroupsService meteringGroupsService, Clock clock) {
        super(dataModel, thesaurus, propertySpecService);
        this.meteringGroupsService = meteringGroupsService;
        this.clock = clock;
    }

    protected PropertySpec getExcludedGroupsPropertySpec() {
        return getPropertySpecService().specForValuesOf(new EndDeviceGroupValueFactory())
                .named(PROPERTY_NAME, TranslationKeys.CLOSE_ACTION_PROPERTY_EXCLUDED_GROUPS)
                .describedAs(TranslationKeys.CLOSE_ACTION_PROPERTY_EXCLUDED_GROUPS).fromThesaurus(getThesaurus())
                .markMultiValued(VALUE_SEPARATOR).finish();
    }

    protected boolean isEndDeviceExcludedFromAutoClosure(final EndDevice endDevice,
            final Map<String, Object> properties) {
        if (endDevice != null) {
            final List<EndDeviceGroup> excludedGroups = getExcludedDeviceGroupsFromParameters(properties);
            final Instant now = clock.instant();
            if (excludedGroups != null && !excludedGroups.isEmpty()) {
                for (EndDeviceGroup endDeviceGroup : excludedGroups) {
                    if (endDeviceGroup.isMember(endDevice, now)) {
                        LOG.log(Level.INFO,
                                this.getThesaurus()
                                        .getFormat(TranslationKeys.CLOSE_ACTION_DEVICE_EXCLUDED_FROM_AUTOCLOSURE)
                                        .format(endDevice.getName()));
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private List<EndDeviceGroup> getExcludedDeviceGroupsFromParameters(Map<String, Object> properties) {
        final Object value = properties.get(PROPERTY_NAME);
        return (List<EndDeviceGroup>) value;
    }

    private class EndDeviceGroupValueFactory implements ValueFactory<EndDeviceGroup>, EndDeviceGroupPropertyFactory {

        @Override
        public EndDeviceGroup fromStringValue(String stringValue) {
            return meteringGroupsService.findEndDeviceGroup(Long.parseLong(stringValue)).orElse(null);
        }

        @Override
        public String toStringValue(EndDeviceGroup object) {
            return String.valueOf(object.getId());
        }

        @Override
        public Class<EndDeviceGroup> getValueType() {
            return EndDeviceGroup.class;
        }

        @Override
        public EndDeviceGroup valueFromDatabase(Object object) {
            return this.fromStringValue((String) object);
        }

        @Override
        public Object valueToDatabase(EndDeviceGroup object) {
            return this.toStringValue(object);
        }

        @Override
        public void bind(PreparedStatement statement, int offset, EndDeviceGroup value) throws SQLException {
            if (value != null) {
                statement.setObject(offset, valueToDatabase(value));
            } else {
                statement.setNull(offset, Types.VARCHAR);
            }
        }

        @Override
        public void bind(SqlBuilder builder, EndDeviceGroup value) {
            if (value != null) {
                builder.addObject(valueToDatabase(value));
            } else {
                builder.addNull(Types.VARCHAR);
            }
        }
    }
}
