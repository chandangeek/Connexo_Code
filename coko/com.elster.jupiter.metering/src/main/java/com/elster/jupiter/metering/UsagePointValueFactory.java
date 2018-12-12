/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.metering;

import com.elster.jupiter.properties.AbstractValueFactory;

public class UsagePointValueFactory extends AbstractValueFactory<UsagePointValueFactory.UsagePointReference> {

    private final MeteringService meteringService;

    public UsagePointValueFactory(MeteringService meteringService) {
        this.meteringService = meteringService;
    }

    @Override
    protected int getJdbcType() {
        return java.sql.Types.VARCHAR;
    }

    @Override
    public UsagePointReference fromStringValue(String stringValue) {
        return meteringService.findUsagePointById(Long.parseLong(stringValue)).map(UsagePointReference::new).orElse(null);
    }

    @Override
    public String toStringValue(UsagePointReference object) {
        return String.valueOf(object.getUsagePoint().getId());
    }

    @Override
    public Class<UsagePointReference> getValueType() {
        return UsagePointReference.class;
    }

    @Override
    public UsagePointReference valueFromDatabase(Object object) {
        return this.fromStringValue((String) object);
    }

    @Override
    public Object valueToDatabase(UsagePointReference object) {
        return this.toStringValue(object);
    }

    public static class UsagePointReference {

        private final UsagePoint usagePoint;

        public UsagePointReference(UsagePoint usagePoint) {
            this.usagePoint = usagePoint;
        }

        public UsagePoint getUsagePoint() {
            return usagePoint;
        }
    }
}
