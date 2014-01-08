package com.energyict.mdc.dynamic.relation.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.dynamic.relation.Constraint;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationType;

/**
 * Models the database tables that hold the data of the following entities:
 * <ul>
 * <li>{@link RelationType}</li>
 * <li>{@link RelationAttributeType}</li>
 * <li>{@link Constraint}</li>
 * </ul>
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-12-17 (10:01)
 */
public enum TableSpecs {

    EISRELATIONTYPE(RelationType.class) {
        @Override
        void describeTable(Table table) {
            table.map(RelationTypeImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.primaryKey("CDR_PK_RELATIONTYPE").on(idColumn).add();
            table.column("NAME").type("varchar2(24)").notNull().map("name").add();
            table.column("DISPLAYNAME").type("varchar2(256)").map("displayName").add();
            table.column("ACTIVE").number().notNull().conversion(ColumnConversion.NUMBER2BOOLEAN).map("active").add();
            table.column("SYSTEM").number().notNull().conversion(ColumnConversion.NUMBER2BOOLEAN).map("system").add();
            table.column("HASTIMERESOLUTION").number().notNull().conversion(ColumnConversion.NUMBER2BOOLEAN).map("hasTimeResolution").add();
            table.column("LOCKATTRIBUTEID").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("lockAttributeTypeId").add();
            table.column("MOD_DATE").number().notNull().conversion(ColumnConversion.DATE2DATE).insert("sysdate").update("sysdate").map("modDate").add();
        }
    },
    EISRELATIONATTRIBUTETYPE(RelationAttributeType.class) {
        @Override
        void describeTable(Table table) {
            table.map(RelationAttributeTypeImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.primaryKey("CDR_PK_ATTRTYPE").on(idColumn).add();
            table.column("NAME").type("varchar2(30)").notNull().map("name").add();
            table.column("DISPLAYNAME").type("varchar2(256)").map("displayName").add();
            table.column("VALUEFACTORY").type("varchar2(512)").notNull().map("valueFactoryClassName").add();
            Column relationTypeColumn = table.column("RELATIONTYPEID").number().notNull().add();
            table.column("OBJECTFACTORYID").number().conversion(ColumnConversion.NUMBER2INT).map("objectFactoryId").add();
            table.column("LARGESTRING").number().notNull().conversion(ColumnConversion.NUMBER2BOOLEAN).map("largeString").add();
            table.column("REQUIRED").number().notNull().conversion(ColumnConversion.NUMBER2BOOLEAN).map("required").add();
            table.column("NAVIGATABLE").number().notNull().conversion(ColumnConversion.NUMBER2BOOLEAN).map("navigatable").add();
            table.column("HIDDEN").number().notNull().conversion(ColumnConversion.NUMBER2BOOLEAN).map("hidden").add();
            table.column("ROLENAME").type("varchar2(80)").map("roleName").add();
            table.foreignKey("FK_RAT_RELATIONTYPE").on(relationTypeColumn).references(EISRELATIONTYPE.name()).
                    map("relationType").reverseMap("attributeTypes").composition().add();
        }
    },
    EISCONSTRAINT(Constraint.class) {
        @Override
        void describeTable(Table table) {
            table.map(ConstraintImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.primaryKey("CDR_PK_CONSTRAINT").on(idColumn).add();
            table.column("NAME").type("varchar2(80)").notNull().map("name").add();
            Column relationTypeColumn = table.column("RELATIONTYPEID").number().add();
            table.column("REJECTVIOLATIONS").number().notNull().conversion(ColumnConversion.NUMBER2BOOLEAN).map("rejectViolations").add();
            table.column("MOD_DATE").number().notNull().conversion(ColumnConversion.DATE2DATE).insert("sysdate").update("sysdate").map("modDate").add();
            table.foreignKey("FK_CONSTRAINT_RELATIONTYPE").on(relationTypeColumn).references(EISRELATIONTYPE.name()).
                    map("relationType").reverseMap("constraints").composition().add();
        }
    },
    EISCONSTRAINTMEMBER(ConstraintMember.class) {
        @Override
        void describeTable(Table table) {
            table.map(ConstraintMember.class);
            Column constraintColumn = table.column("CONSTRAINTID").number().notNull().add();
            Column attributeTypeColumn = table.column("ATTRIBUTETYPEID").number().notNull().map("attributeTypeId").add();
            table.primaryKey("CDR_PK_CONSTRAINT").on(constraintColumn, attributeTypeColumn).add();
            table.foreignKey("FK_CONSTRAINTMEMBER_CONSTRAINT").on(constraintColumn).references(EISCONSTRAINT.name()).
                    map("constraint").reverseMap("members").composition().add();
            table.foreignKey("FK_CONSTRAINTMEMBER_ATTRIBUTE").
                    on(constraintColumn).references(EISRELATIONATTRIBUTETYPE.name()).
                    map("attributeTypeId").add();
        }
    };

    private Class apiClass;

    TableSpecs (Class apiClass) {
        this.apiClass = apiClass;
    }

    public void addTo(DataModel component) {
        Table table = component.addTable(name(), this.apiClass);
        describeTable(table);
    }

    abstract void describeTable(Table table);

}