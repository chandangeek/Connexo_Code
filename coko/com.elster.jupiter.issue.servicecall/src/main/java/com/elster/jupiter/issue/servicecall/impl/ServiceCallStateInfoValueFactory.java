/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.servicecall.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.ServiceCallStateInfoPropertyFactory;
import com.elster.jupiter.servicecall.DefaultState;
import com.elster.jupiter.util.sql.SqlBuilder;

import javax.xml.bind.annotation.XmlRootElement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

public class ServiceCallStateInfoValueFactory implements ValueFactory<HasIdAndName>, ServiceCallStateInfoPropertyFactory {

    private Thesaurus thesaurus;
    static final String SEPARATOR = ":";

    ServiceCallStateInfoValueFactory (Thesaurus thesaurus) {
        this.thesaurus = thesaurus;
    }

    @Override
    public HasIdAndName fromStringValue(String stringValue) {
        return new DefaultStateInfo(Integer.valueOf(stringValue), thesaurus);
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

@XmlRootElement
class DefaultStateInfo extends HasIdAndName {

    private transient DefaultState defaultState;
    private transient Thesaurus thesaurus;

    DefaultStateInfo(DefaultState defaultState, Thesaurus thesaurus) {
        this.defaultState = defaultState;
        this.thesaurus = thesaurus;
    }

    DefaultStateInfo(int id, Thesaurus thesaurus) {
        this.defaultState = DefaultState.values()[id];
        this.thesaurus = thesaurus;
    }


    @Override
    public Long getId() {
        return (long) defaultState.ordinal();
    }

    @Override
    public String getName() {
        return defaultState.getDisplayName(thesaurus);
    }

    @Override
    public boolean equals(Object o) {
        if (this != o) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + Long.hashCode(defaultState.ordinal());
        return result;
    }
}
