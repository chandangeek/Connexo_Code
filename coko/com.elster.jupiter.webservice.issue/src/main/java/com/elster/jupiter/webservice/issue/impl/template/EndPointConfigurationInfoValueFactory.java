/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.webservice.issue.impl.template;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.EndPointConfigurationPropertyFactory;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfigurationService;
import com.elster.jupiter.util.sql.SqlBuilder;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class EndPointConfigurationInfoValueFactory implements ValueFactory<HasIdAndName>, EndPointConfigurationPropertyFactory {
    private final Thesaurus thesaurus;
    private final EndPointConfigurationService endPointConfigurationService;

    @Inject
    public EndPointConfigurationInfoValueFactory(Thesaurus thesaurus, EndPointConfigurationService endPointConfigurationService) {
        this.thesaurus = thesaurus;
        this.endPointConfigurationService = endPointConfigurationService;
    }

    @Override
    public HasIdAndName fromStringValue(String stringValue) {
        long epcId = Long.parseLong(stringValue);
        EndPointConfiguration endPointConfiguration = endPointConfigurationService.getEndPointConfiguration(epcId)
                .orElseThrow(() -> new IllegalArgumentException("Couldn't find endpoint with id " + epcId + '.'));
        return new EndPointConfigurationInfo(endPointConfiguration, thesaurus);
    }

    @Override
    public String toStringValue(HasIdAndName object) {
        return String.valueOf(object.getId());
    }

    @Override
    public Class<HasIdAndName> getValueType() {
        return HasIdAndName.class;
    }

    @Override
    public HasIdAndName valueFromDatabase(Object object) {
        return this.fromStringValue((String) object);
    }

    @Override
    public Object valueToDatabase(HasIdAndName object) {
        return this.toStringValue(object);
    }

    @Override
    public void bind(PreparedStatement statement, int offset, HasIdAndName value) throws SQLException {
        if (value != null) {
            statement.setObject(offset, valueToDatabase(value));
        } else {
            statement.setNull(offset, Types.VARCHAR);
        }
    }

    @Override
    public void bind(SqlBuilder builder, HasIdAndName value) {
        if (value != null) {
            builder.addObject(valueToDatabase(value));
        } else {
            builder.addNull(Types.VARCHAR);
        }
    }
}
