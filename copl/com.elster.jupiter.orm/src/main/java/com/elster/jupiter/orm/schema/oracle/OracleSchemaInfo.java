package com.elster.jupiter.orm.schema.oracle;

import com.elster.jupiter.orm.schema.SchemaInfoProvider;

import org.osgi.service.component.annotations.Component;

import java.util.Arrays;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 17/04/2014
 * Time: 14:24
 */
@Component(name = "com.elster.jupiter.orm.schema.oracle")
public class OracleSchemaInfo implements SchemaInfoProvider {
    @Override
    public List<? extends TableSpec> getSchemaInfoTableSpec() {
        return Arrays.asList(OracleTableSpecs.values());
    }
}
