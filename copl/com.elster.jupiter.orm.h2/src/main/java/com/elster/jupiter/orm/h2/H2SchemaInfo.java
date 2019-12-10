/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.h2;

import com.elster.jupiter.orm.schema.SchemaInfoProvider;
import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

@Component(name = "com.elster.jupiter.orm.schema.h2")
public class H2SchemaInfo implements SchemaInfoProvider {

    @Override
    public List<? extends TableSpec> getSchemaInfoTableSpec() {
        return Arrays.asList(H2TableSpecs.values());
    }
}
