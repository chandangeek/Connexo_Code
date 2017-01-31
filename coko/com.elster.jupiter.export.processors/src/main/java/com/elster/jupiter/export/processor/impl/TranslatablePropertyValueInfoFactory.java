/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.export.processor.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.ValueFactory;
import com.elster.jupiter.util.sql.SqlBuilder;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Insert your comments here.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2016-01-13 (16:13)
 */
class TranslatablePropertyValueInfoFactory implements ValueFactory<TranslatablePropertyValueInfo> {

    private final Thesaurus thesaurus;

    TranslatablePropertyValueInfoFactory(Thesaurus thesaurus) {
        super();
        this.thesaurus = thesaurus;
    }

    @Override
    public TranslatablePropertyValueInfo fromStringValue(String stringValue) {
        return new TranslatablePropertyValueInfo(stringValue, this.thesaurus.getFormat(FormatterProperties.separatorValueFrom(stringValue)).format());
    }

    @Override
    public String toStringValue(TranslatablePropertyValueInfo object) {
        return object.getId();
    }

    @Override
    public Class<TranslatablePropertyValueInfo> getValueType() {
        return TranslatablePropertyValueInfo.class;
    }

    @Override
    public TranslatablePropertyValueInfo valueFromDatabase(Object object) {
        return this.fromStringValue((String) object);
    }

    @Override
    public Object valueToDatabase(TranslatablePropertyValueInfo object) {
        return this.toStringValue(object);
    }

    @Override
    public void bind(PreparedStatement statement, int offset, TranslatablePropertyValueInfo value) throws SQLException {
        statement.setString(offset, this.toStringValue(value));
    }

    @Override
    public void bind(SqlBuilder builder, TranslatablePropertyValueInfo value) {
        builder.addObject(this.toStringValue(value));
    }

}