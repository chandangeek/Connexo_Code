package com.elster.jupiter.orm;

import aQute.bnd.annotation.ProviderType;

public interface TableAudit {

    Table<?> getTable();

    String getCategory();

    String getSubCategory();

    ForeignKeyConstraint getForeignKeyConstraint();

    String getObjectReferences(Object object);

    String getObjectIndentifier(Object object);

    Table<?> getTouchTable();

    @ProviderType
    interface Builder {

        Builder category(String category);

        Builder subCategory(String category);

        TableAudit references(ForeignKeyConstraint foreignKeyConstraint);
    }

}
