package com.elster.jupiter.issue.share.entity.values.factories;

import com.elster.jupiter.issue.share.entity.values.EndPointValue;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.WebServicesEndPointFactory;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.util.sql.SqlBuilder;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class EndPointValueFactory implements ValueFactory<EndPointValue>, WebServicesEndPointFactory {

    private final EndPointConfigurationService endPointConfigurationService;

    public EndPointValueFactory(final EndPointConfigurationService endPointConfigurationService) {
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Override
    public EndPointValue fromStringValue(String stringValue) {
        return endPointConfigurationService.findEndPointConfigurations()
                .stream()
                .filter(p -> p.getId() == Long.valueOf(stringValue))
                .findFirst()
                .map(EndPointValue::new)
                .orElse(null);
    }

    @Override
    public String toStringValue(EndPointValue endPoint) {
        return String.valueOf(endPoint.getId());
    }

    @Override
    public Class<EndPointValue> getValueType() {
        return EndPointValue.class;
    }

    @Override
    public EndPointValue valueFromDatabase(Object object) {
        return this.fromStringValue((String) object);
    }

    @Override
    public Object valueToDatabase(EndPointValue object) {
        return this.toStringValue(object);
    }

    @Override
    public void bind(PreparedStatement statement, int offset, EndPointValue value) throws SQLException {
        if (value != null) {
            statement.setObject(offset, valueToDatabase(value));
        } else {
            statement.setNull(offset, Types.VARCHAR);
        }
    }

    @Override
    public void bind(SqlBuilder builder, EndPointValue value) {
        if (value != null) {
            builder.addObject(this.valueToDatabase(value));
        } else {
            builder.addNull(Types.VARCHAR);
        }
    }
}