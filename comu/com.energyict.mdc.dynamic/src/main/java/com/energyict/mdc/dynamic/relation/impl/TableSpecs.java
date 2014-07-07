package com.energyict.mdc.dynamic.relation.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.Table;
import com.energyict.mdc.dynamic.relation.Constraint;
import com.energyict.mdc.dynamic.relation.RelationAttributeType;
import com.energyict.mdc.dynamic.relation.RelationType;

import static com.elster.jupiter.orm.Table.*;

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

    CDR_RELATIONTYPE {
        @Override
        void addTo(DataModel dataModel) {
            Table<RelationType> table = dataModel.addTable(name(), RelationType.class);
            table.map(RelationTypeImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.primaryKey("PK_CDR_RELATIONTYPE").on(idColumn).add();
            table.column("NAME").varChar(24).notNull().map("name").add();
            table.column("DISPLAYNAME").varChar(SHORT_DESCRIPTION_LENGTH).map("displayName").add();
            table.column("ACTIVE").number().notNull().conversion(ColumnConversion.NUMBER2BOOLEAN).map("active").add();
            table.column("SYSTEM").number().notNull().conversion(ColumnConversion.NUMBER2BOOLEAN).map("system").add();
            table.column("HASTIMERESOLUTION").number().notNull().conversion(ColumnConversion.NUMBER2BOOLEAN).map("hasTimeResolution").add();
            table.column("LOCKATTRIBUTEID").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("lockAttributeTypeId").add();
            table.column("MOD_DATE").type("DATE").notNull().conversion(ColumnConversion.DATE2DATE).insert("sysdate").update("sysdate").map("modDate").add();
        }
    },
    CDR_RELATIONATTRIBUTETYPE {
        @Override
        void addTo(DataModel dataModel) {
            Table<RelationAttributeType> table = dataModel.addTable(name(), RelationAttributeType.class);
            table.map(RelationAttributeTypeImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.primaryKey("PK_CDR_RELATIONATTRIBUTETYPE").on(idColumn).add();
            table.column("NAME").varChar(30).notNull().map("name").add();
            table.column("DISPLAYNAME").varChar(SHORT_DESCRIPTION_LENGTH).map("displayName").add();
            table.column("VALUEFACTORY").varChar(512).notNull().map("valueFactoryClassName").add();
            Column relationType = table.column("RELATIONTYPEID").number().notNull().add();
            table.column("OBJECTFACTORYID").number().conversion(ColumnConversion.NUMBER2INT).map("objectFactoryId").add();
            table.column("LARGESTRING").number().notNull().conversion(ColumnConversion.NUMBER2BOOLEAN).map("largeString").add();
            table.column("REQUIRED").number().notNull().conversion(ColumnConversion.NUMBER2BOOLEAN).map("required").add();
            table.column("NAVIGATABLE").number().notNull().conversion(ColumnConversion.NUMBER2BOOLEAN).map("navigatable").add();
            table.column("HIDDEN").number().notNull().conversion(ColumnConversion.NUMBER2BOOLEAN).map("hidden").add();
            table.column("ROLENAME").varChar(NAME_LENGTH).map("roleName").add();
            table.foreignKey("FK_CDR_ATTRIBUTETYPE_RELTYPE").
                    on(relationType).
                    references(CDR_RELATIONTYPE.name()).
                    map("relationType").
                    reverseMap("attributeTypes").
                    composition().
                    add();
        }
    },
    CDR_CONSTRAINT {
        @Override
        void addTo(DataModel dataModel) {
            Table<Constraint> table = dataModel.addTable(name(), Constraint.class);
            table.map(ConstraintImpl.class);
            Column idColumn = table.addAutoIdColumn();
            table.primaryKey("PK_CDR_CONSTRAINT").on(idColumn).add();
            table.column("NAME").varChar(NAME_LENGTH).notNull().map("name").add();
            Column relationTypeColumn = table.column("RELATIONTYPEID").number().add();
            table.column("REJECTVIOLATIONS").number().notNull().conversion(ColumnConversion.NUMBER2BOOLEAN).map("rejectViolations").add();
            table.column("MOD_DATE").type("DATE").notNull().conversion(ColumnConversion.DATE2DATE).insert("sysdate").update("sysdate").map("modDate").add();
            table.foreignKey("FK_CDR_CONSTRAINT_RELATIONTYPE").
                    on(relationTypeColumn).
                    references(CDR_RELATIONTYPE.name()).
                    map("relationType").
                    reverseMap("constraints").
                    composition().
                    add();
        }
    },
    CDR_CONSTRAINTMEMBER {
        @Override
        void addTo(DataModel dataModel) {
            Table<ConstraintMember> table = dataModel.addTable(name(), ConstraintMember.class);
            table.map(ConstraintMember.class);
            Column constraintColumn = table.column("CONSTRAINTID").number().notNull().add();
            Column attributeTypeColumn = table.column("ATTRIBUTETYPEID").number().notNull().conversion(ColumnConversion.NUMBER2INT).map("attributeTypeId").add();
            table.primaryKey("PK_CDR_CONSTRAINTMEMBER").on(constraintColumn, attributeTypeColumn).add();
            table.foreignKey("FK_CDR_CONSTRMEMBER_CONSTRAINT").
                    on(constraintColumn).
                    references(CDR_CONSTRAINT.name()).
                    map("constraint").
                    reverseMap("members").
                    composition().
                    add();
            table.foreignKey("FK_CDR_CONSTRMEMBER_ATTRIBUTE").
                    on(attributeTypeColumn).
                    references(CDR_RELATIONATTRIBUTETYPE.name()).
                    map("attributeTypeId").
                    add();
        }
    };

    abstract void addTo(DataModel component);

}