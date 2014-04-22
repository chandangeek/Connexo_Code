package com.elster.jupiter.orm.schema;

import com.elster.jupiter.orm.DataModel;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 15/04/2014
 * Time: 17:30
 */
public interface SchemaInfoProvider {
    public interface TableSpec {
        void addTo(DataModel model);
    }

    List<? extends TableSpec> getSchemaInfoTableSpec();
}
