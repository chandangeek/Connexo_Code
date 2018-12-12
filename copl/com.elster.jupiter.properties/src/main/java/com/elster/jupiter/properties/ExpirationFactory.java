/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.properties;

public class ExpirationFactory extends AbstractValueFactory<Expiration> {

    @Override
    protected int getJdbcType() {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public Class<Expiration> getValueType() {
        return Expiration.class;
    }

    @Override
    public Expiration fromStringValue(String stringValue) {
        return new Expiration(Expiration.Type.fromString(stringValue));
    }

    @Override
    public String toStringValue(Expiration object) {
        switch(object.getType()) {
            case EXPIRED:
            case EXPIRES_1WEEK:
            case EXPIRES_1MONTH:
            case EXPIRES_3MONTHS:
                object.getType().getName();
            default:
                throw new IllegalArgumentException("Unsupported type of expiration: " + object.getType());
        }
    }

    @Override
    public Expiration valueFromDatabase(Object object) {
        if (object == null) {
            return null;
        }
        return fromStringValue((String)object);
    }

    @Override
    public Object valueToDatabase(Expiration object) {
        return toStringValue(object);
    }
}
