/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.issue.impl;

import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.properties.rest.ServiceCallInfoPropertyFactory;
import com.elster.jupiter.servicecall.ServiceCallService;
import com.elster.jupiter.servicecall.ServiceCallType;
import com.elster.jupiter.util.sql.SqlBuilder;

import javax.xml.bind.annotation.XmlRootElement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

class ServiceCallInfoValueFactory implements ValueFactory<HasIdAndName>, ServiceCallInfoPropertyFactory {

    private ServiceCallService serviceCallService;
    static final String SEPARATOR = ",";

    ServiceCallInfoValueFactory (ServiceCallService serviceCallService) {
        this.serviceCallService = serviceCallService;
    }

    @Override
    public HasIdAndName fromStringValue(String stringValue) {
        return serviceCallService.getServiceCallTypes().stream().filter(serviceCallType -> String.valueOf(serviceCallType.getId()).equals(stringValue))
                .map(ServiceCallTypeInfo::new).findFirst().orElse(null);
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
class ServiceCallTypeInfo extends HasIdAndName {

    private transient ServiceCallType serviceCallType;

    ServiceCallTypeInfo(ServiceCallType serviceCallType) {
        this.serviceCallType = serviceCallType;
    }

    @Override
    public Long getId() {
        return serviceCallType.getId();
    }

    @Override
    public String getName() {
        return serviceCallType.getName();
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
        result = 31 * result + Long.hashCode(serviceCallType.getId());
        return result;
    }
}
