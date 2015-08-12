package com.elster.jupiter.cps.impl;

import com.elster.jupiter.cps.CustomPropertySet;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.Table;

import javax.validation.constraints.Size;

/**
 * An domain class that will be extended by the {@link CustomPropertySet}s
 * used in the test classes of this bundle.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2015-08-12 (10:20)
 */
public class TestDomain {

    public enum FieldNames {
        NAME("name", "NAME"),
        DESCRIPTION("description", "DESC");

        FieldNames(String javaName, String databaseName) {
            this.javaName = javaName;
            this.databaseName = databaseName;
        }

        private final String javaName;
        private final String databaseName;

        public String javaName() {
            return javaName;
        }

        public String databaseName() {
            return databaseName;
        }
    }

    // For persistence framework
    public TestDomain() {
        super();
    }

    // For testing purposes
    public TestDomain(long id) {
        this();
        this.id = id;
    }

    public static void install(OrmService ormService) {
        DataModel dataModel = ormService.newDataModel("T01", "Test Domain, for testing purposes only");
        Table<TestDomain> table = dataModel.addTable("T01_TESTDOMAIN", TestDomain.class).map(TestDomain.class);
        Column id = table.addAutoIdColumn();
        table
            .column(FieldNames.NAME.databaseName())
            .varChar()
            .map(FieldNames.NAME.javaName())
            .notNull()
            .add();
        table
            .column(FieldNames.DESCRIPTION.databaseName())
            .varChar()
            .map(FieldNames.DESCRIPTION.javaName())
            .add();
        table
            .primaryKey("PK_TESTDOMAIN")
            .on(id)
            .add();
        dataModel.register();
        dataModel.install(true, true);
    }

    @SuppressWarnings("unused")
    private long id;
    @Size(max= Table.NAME_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "FieldTooLong")
    private String name;
    @Size(max= Table.DESCRIPTION_LENGTH, groups = { Save.Create.class, Save.Update.class }, message = "FieldTooLong")
    private String description;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}