package com.elster.jupiter.orm;

public interface DataModelUpgrader {

    void upgrade(DataModel dataModel, Version version);
}
