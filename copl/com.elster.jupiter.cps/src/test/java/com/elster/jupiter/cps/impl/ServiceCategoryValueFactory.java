/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.impl;

import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;


/**
 * Provides an implementation for the {@link ValueFactory} interface
 * for {@link ServiceCategoryForTestingPurposes}.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-11-18 (09:11)
 */
public class ServiceCategoryValueFactory implements ValueFactory<ServiceCategoryForTestingPurposes> {

    @Override
    public Class<ServiceCategoryForTestingPurposes> getValueType() {
        return ServiceCategoryForTestingPurposes.class;
    }

    @Override
    public ServiceCategoryForTestingPurposes fromStringValue(String stringValue) {
        return ServiceCategoryForTestingPurposes.valueOf(stringValue);
    }

    @Override
    public String toStringValue(ServiceCategoryForTestingPurposes serviceCategory) {
        return serviceCategory.name();
    }

    @Override
    public ServiceCategoryForTestingPurposes valueFromDatabase(Object object) {
        BigDecimal ordinal = (BigDecimal) object;
        return ServiceCategoryForTestingPurposes.values()[ordinal.intValue()];
    }

    @Override
    public Object valueToDatabase(ServiceCategoryForTestingPurposes serviceCategory) {
        return serviceCategory.ordinal();
    }

    @Override
    public void bind(PreparedStatement statement, int offset, ServiceCategoryForTestingPurposes serviceCategory) throws SQLException {
        statement.setInt(offset, serviceCategory.ordinal());
    }

    @Override
    public void bind(SqlBuilder builder, ServiceCategoryForTestingPurposes serviceCategory) {
        builder.addInt(serviceCategory.ordinal());
    }

}