package com.elster.jupiter.orm.impl;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ColumnConversion;
import com.elster.jupiter.orm.IllegalTableMappingException;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.fields.impl.ColumnConversionImpl;

import javax.validation.constraints.Size;
import java.lang.reflect.Field;
import java.security.Principal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Objects;

public class ColumnImpl implements Column {
    // persistent fields

    private String name;
    @SuppressWarnings("unused")
    private int position;
    private String dbType;
    private boolean notNull;
    private ColumnConversionImpl conversion;
    private String fieldName;
    private String sequenceName;
    private boolean versionCount = false;
    private String insertValue;
    private String updateValue;
    private boolean skipOnUpdate;
    private String formula;

    // associations
    private final Reference<TableImpl<?>> table = ValueReference.absent();

    private ColumnImpl init(TableImpl<?> table, String name) {
        if (name.length() > ColumnConversion.CATALOGNAMELIMIT) {
            throw new IllegalTableMappingException("Table " + getName() + " : column name '" + name + "' is too long, max length is " + ColumnConversion.CATALOGNAMELIMIT + " actual length is " + name.length() + ".");
        }
        this.table.set(table);
        this.name = name;
        return this;
    }

    static ColumnImpl from(TableImpl<?> table, String name) {
        return new ColumnImpl().init(table, name);
    }

    private void validate() {
        if (!table.isPresent()) {
            throw new IllegalArgumentException("table must be present");
        }
        Objects.requireNonNull(name);
        if (!isVirtual()) {
            if (dbType == null) {
                throw new IllegalTableMappingException("Table " + getTable().getName() + " : column " + getName() + " was not assigned a DB type.");
            }
            Objects.requireNonNull(conversion);
        }
        if (skipOnUpdate && updateValue != null) {
            throw new IllegalTableMappingException("Table " + getTable().getName() + " : field " + getName() + " : updateValue must be null if skipOnUpdate");
        }
    }

    @Override
    public TableImpl<?> getTable() {
        return table.get();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getName(String alias) {
        return
                alias == null || alias.length() == 0 ? name : alias + "." + name;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public ColumnConversion getConversion() {
        return ColumnConversion.valueOf(conversion.name());
    }

    @Override
    public String toString() {
        return
                "Column " + name + " in table " + getTable().getQualifiedName();
    }

    @Override
    public boolean isPrimaryKeyColumn() {
        return getTable().isPrimaryKeyColumn(this);
    }

    public String getDbType() {
        return dbType;
    }

    @Override
    public boolean isAutoIncrement() {
        return sequenceName != null && sequenceName.length() > 0;
    }

    @Override
    public boolean isVersion() {
        return versionCount;
    }

    @Override
    public String getSequenceName() {
        return sequenceName;
    }

    @Override
    public String getQualifiedSequenceName() {
        return sequenceName == null ? null : getTable().getQualifiedName(sequenceName);
    }

    @Override
    public String getInsertValue() {
        return insertValue;
    }

    @Override
    public boolean hasInsertValue() {
        return insertValue != null && insertValue.length() > 0;
    }

    @Override
    public String getUpdateValue() {
        return updateValue;
    }

    @Override
    public boolean skipOnUpdate() {
        return skipOnUpdate;
    }

    @Override
    public boolean hasUpdateValue() {
        return updateValue != null && updateValue.length() > 0;
    }

    private ColumnConversionImpl.JsonConverter jsonConverter() {
        return getTable().getDataModel().getInstance(ColumnConversionImpl.JsonConverter.class);
    }

    public Object convertToDb(Object value) {
        if (conversion == ColumnConversionImpl.CHAR2JSON) {
            return jsonConverter().convertToDb(value);
        } else {
            return conversion.convertToDb(this, value);
        }
    }

    Object convertFromDb(ResultSet rs, int index) throws SQLException {
        switch (conversion) {
            case CHAR2JSON:
                return jsonConverter().convertFromDb(rs, index);
            case CHAR2ENUM:
            case NUMBER2ENUM:
            case NUMBER2ENUMPLUSONE:
                return createEnum(conversion.convertFromDb(this, rs, index));
            default:
                return conversion.convertFromDb(this, rs, index);
        }
    }

    private Class<?> getType() {
        if (fieldName != null) {
            return getTable().getMapperType().getType(fieldName);
        }
        ForeignKeyConstraintImpl constraint = getForeignKeyConstraint();
        int index = constraint.getColumns().indexOf(this);
        ColumnImpl primaryKeyColumn = constraint.getReferencedTable().getPrimaryKeyColumns().get(index);
        return primaryKeyColumn.getType();
    }

    private Object createEnum(Object value) {
        if (value == null) {
            return null;
        }
        Enum<?>[] enumConstants = (Enum<?>[]) getType().getEnumConstants();
        if (value instanceof Integer) {
            try {
                return enumConstants[(Integer) value];
            } catch (ArrayIndexOutOfBoundsException e) {
                throw new RuntimeException(" Table : " + getTable().getName() + " Column : " + this.getName() + " illegal value " + value + " for enum " + getType().getName(), e);
            }
        } else {
            for (Enum<?> each : enumConstants) {
                if (each.name().equals(value)) {
                    return each;
                }
            }
        }
        throw new IllegalArgumentException("" + value + " not appropriate for enum " + getType());
    }


    boolean isStandard() {
        return
                !isPrimaryKeyColumn() &&
                        !isVersion() &&
                        !hasInsertValue() &&
                        !skipOnUpdate() &&
                        !hasAutoValue(true) &&
                        !isDiscriminator() &&
                        !isVirtual();
    }

    boolean hasAutoValue(boolean update) {
        boolean auto = conversion == ColumnConversionImpl.NUMBER2NOW || conversion == ColumnConversionImpl.CHAR2PRINCIPAL;
        return update ? auto && !skipOnUpdate() : auto;
    }

    boolean hasIntValue() {
        return conversion == ColumnConversionImpl.NUMBER2INT || conversion == ColumnConversionImpl.NUMBER2INTNULLZERO;
    }

    @Override
    public boolean isEnum() {
        switch (conversion) {
            case NUMBER2ENUM:
            case NUMBER2ENUMPLUSONE:
            case CHAR2ENUM:
                return true;

            default:
                return false;
        }
    }

    public Object convert(String in) {
        if (isEnum()) {
            return createEnum(in);
        } else if (conversion == ColumnConversionImpl.CHAR2JSON) {
            return jsonConverter().convert(in);
        } else {
            return conversion.convert(this, in);
        }
    }

    @Override
    public boolean isNotNull() {
        return notNull;
    }

    @Override
    public boolean isDiscriminator() {
        return TYPEFIELDNAME.equals(fieldName);
    }

    boolean isForeignKeyPart() {
        return getForeignKeyConstraint() != null;
    }

    ForeignKeyConstraintImpl getForeignKeyConstraint() {
        for (ForeignKeyConstraintImpl constraint : getTable().getForeignKeyConstraints()) {
            for (ColumnImpl column : constraint.getColumns()) {
                if (this.equals(column)) {
                    return constraint;
                }
            }
        }
        return null;
    }

    private DomainMapper getDomainMapper() {
        return getTable().getDomainMapper();
    }

    Object domainValue(Object target) {
        if (isDiscriminator()) {
            return getTable().getMapperType().getDiscriminator(target.getClass());
        } else if (fieldName != null) {
            return getDomainMapper().get(target, fieldName);
        } else {
            return getForeignKeyConstraint().domainValue(this, target);
        }
    }

    void prepare(Object target, boolean update, Instant now) {
        if (conversion == ColumnConversionImpl.NUMBER2NOW && !(update && skipOnUpdate())) {
            getDomainMapper().set(target, fieldName, now);
        }
        if (conversion == ColumnConversionImpl.CHAR2PRINCIPAL && !(update && skipOnUpdate())) {
            getDomainMapper().set(target, fieldName, getCurrentUserName());
        }
        if (isVersion() && !update) {
            getDomainMapper().set(target, fieldName, 1);
        }
    }

    private String getCurrentUserName() {
        Principal principal = getTable().getDataModel().getPrincipal();
        return principal == null ? null : principal.getName();
    }

    void setDomainValue(Object target, Object value) {
        getDomainMapper().set(target, fieldName, value, getTable().getDataModel().getInjector());
    }

    void setDomainValue(Object target, ResultSet rs, int index) throws SQLException {
        setDomainValue(target, convertFromDb(rs, index));
    }

    void setObject(PreparedStatement statement, int index, Object target) throws SQLException {
        statement.setObject(index, convertToDb(domainValue(target)));
    }

    String getFormula() {
        return formula;
    }

    @Override
    public boolean isVirtual() {
        return formula != null;
    }

    boolean matchesForIndex(ColumnImpl column) {
        return getName().equalsIgnoreCase(column.getName()) &&
                (isVirtual() == column.isVirtual()) &&
                (!isVirtual() || getFormula().equalsIgnoreCase(column.getFormula()));
    }


    static class BuilderImpl implements Column.Builder {
        private final ColumnImpl column;
        private boolean setType = false;

        BuilderImpl(ColumnImpl column) {
            this.column = column;
            column.conversion = ColumnConversionImpl.NOCONVERSION;
        }

        @Override
        public Builder type(String type) {
            column.dbType = type;
            return this;
        }

        @Override
        public Builder map(String field) {
            column.fieldName = field;
            return this;
        }

        @Override
        public Builder conversion(ColumnConversion conversion) {
            column.conversion = ColumnConversionImpl.valueOf(conversion.name());
            return this;
        }

        @Override
        public Builder notNull() {
            return notNull(true);
        }

        @Override
        public Builder notNull(boolean value) {
            column.notNull = value;
            return this;
        }


        @Override
        public Builder sequence(String name) {
            column.sequenceName = name;
            return this;
        }

        @Override
        public Builder insert(String pseudoLiteral) {
            column.insertValue = pseudoLiteral;
            return this;
        }

        @Override
        public Builder update(String pseudoLiteral) {
            column.updateValue = pseudoLiteral;
            return this;
        }

        @Override
        public Builder version() {
            column.versionCount = true;
            return this;
        }

        @Override
        public Builder skipOnUpdate() {
            column.skipOnUpdate = true;
            return this;
        }

        @Override
        public Builder bool() {
            return this.type("CHAR(1)").notNull().conversion(ColumnConversion.CHAR2BOOLEAN);
        }

        @Override
        public Builder number() {
            return this.type("NUMBER");
        }

        @Override
        public Builder varChar() {
            this.setType = true;
            return this;
        }

        @Override
        public Builder varChar(int length) {
            if (length < 1) {
                throw new IllegalArgumentException("Illegal length: " + length);
            }
            if (length > 4000) {
                // may need to adjust for non oracle or oracle 12
                throw new IllegalArgumentException("" + length + " exceeds max varchar size");
            }
            return this.type("VARCHAR2(" + length + " CHAR)");
        }

        private void setType() {
            Field field = column.getTable().getField(column.getFieldName());
            if (field == null) {
                throw new IllegalStateException("No field found for column with field name '" + column.getFieldName() + "' in table '" + column.getTable().getName() + "'.");
            }
            Size size = field.getAnnotation(Size.class);
            if (size == null) {
                throw new IllegalStateException("No Size annotation for field '" + column.getFieldName() + "' in table '" + column.getTable().getName() + "'.");
            }
            this.varChar(size.max());
        }

        @Override
        public VirtualBuilder as(String formula) {
            column.formula = Objects.requireNonNull(formula);
            return new VirtualBuilderImpl(this);
        }

        @Override
        public Column add() {
            if (setType) {
                setType();
            }
            column.validate();
            return column.getTable().add(column);
        }
    }

    static class VirtualBuilderImpl implements Column.VirtualBuilder {

        private final BuilderImpl base;

        private VirtualBuilderImpl(BuilderImpl base) {
            this.base = base;
        }

        @Override
        public VirtualBuilder alias(String name) {
            base.map(name);
            return this;
        }

        @Override
        public Column add() {
            return base.add();
        }
    }
}

