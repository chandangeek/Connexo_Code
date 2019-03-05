/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.orm.audit;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.TableAudit;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.TableImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class TableAuditImpl implements TableAudit {

    List<ForeignKeyConstraint> foreignKeyConstraints = new ArrayList<ForeignKeyConstraint>();
    private Integer domainContext;
    private Optional<String> reverseReferenceMap = Optional.empty();
    private Optional<String> domainForeignKey = Optional.empty();
    private List<String> contextReferenceColumns = new ArrayList();
    private Optional<String> domainReferenceColumn = Optional.empty();

    private final Reference<TableImpl<?>> table = ValueReference.absent();

    TableAuditImpl init(TableImpl<?> table, String name) {
        this.table.set(table);
        return this;
    }

    static TableAuditImpl from(TableImpl<?> table, String name) {
        return new TableAuditImpl().init(table, name);
    }

    @Override
    public TableImpl<?> getTable() {
        return table.get();
    }

    @Override
    public Integer getDomainContext() {
        return domainContext;
    }

    @Override
    public List<Object> getDomainPkValues(Object object) {
        Optional<String> foreignKeyName = domainForeignKey;

        if (domainReferenceColumn.isPresent()){
            Object finalObject = object;
            return getTable().getColumn(domainReferenceColumn.get())
                            .map(columnImpl -> getPkColumnReference(Collections.singletonList(columnImpl), finalObject))
                            .orElseGet(Collections::emptyList);
        }

        if (foreignKeyConstraints.size() == 0 || !foreignKeyName.isPresent() || foreignKeyName.get().isEmpty()) {
            return getPkColumnReference(getTable().getPrimaryKeyColumns(), object);
        }

        try {
            for (ForeignKeyConstraint foreignKeyConstraint : foreignKeyConstraints) {
                String fieldName = foreignKeyConstraint.getFieldName();
                Optional<?> reference = ((Reference<?>) (((TableImpl) foreignKeyConstraint.getReferencedTable()).getDomainMapper().getField(object.getClass(), fieldName)
                        .get(object))).getOptional();
                if (!reference.isPresent()) {
                    return Collections.emptyList();
                }
                object = reference.get();
                if (foreignKeyConstraint.getName().compareToIgnoreCase(foreignKeyName.get()) == 0) {
                    return getPkColumnReference(foreignKeyConstraint.getReferencedTable().getPrimaryKeyColumns(), object);
                }
            }
            throw new IllegalStateException("");
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("");
        }
    }

    @Override
    public List<Object> getContextPkValues(Object object) {
       return contextReferenceColumns.stream()
                    .filter(column -> column.length() > 0)
                    .map(column -> getTable().getColumn(column)
                            .map(columnImpl -> getPkColumnReference(Collections.singletonList(columnImpl), object))
                            .orElseGet(Collections::emptyList))
               .flatMap(List::stream)
               .collect(Collectors.toList());
    }

    @Override
    public List<Object> getResersePkValues(Object object) {
        return getPkColumnReference(getTable().getPrimaryKeyColumns(), object);
    }

    @Override
    public Optional<Long> getReverseReferenceMap(Object object) {
        try {
            if (reverseReferenceMap.isPresent() && reverseReferenceMap.get().length() != 0) {
                Object referenceValue = ((TableImpl) getTouchTable()).getDomainMapper().getField(object.getClass(), reverseReferenceMap.get()).get(object);
                return Optional.of(Long.parseLong(referenceValue.toString()));
            }

        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("");
        }
        return Optional.empty();
    }

    private String getReference(List<? extends Column> columns, Object object) {
        return "{" + columns
                .stream()
                .map(column -> column.getName() + ":" + ((ColumnImpl) column).domainValue(object))
                .collect(Collectors.joining(",")) + "}";
    }

    private Object getDomainShortReference(List<? extends Column> columns, Object object) {
        if (columns.size() > 1) {
            return new Object();
        }
        return columns.stream()
                .findFirst()
                .map(column -> ((ColumnImpl) column).domainValue(object))
                .orElseGet(Object::new);
    }

    private List<Object> getPkColumnReference(List<? extends Column> columns, Object object) {
        return columns.stream()
                .sorted((o1, o2) -> ((Column) o1).getName().compareToIgnoreCase(o2.getName()))
                .map(column -> ((ColumnImpl) column).domainValue(object))
                .collect(Collectors.toList());
    }

    private String getObjectReference(Object object, Optional<String> foreignKeyName) {
        if ((foreignKeyConstraints.size() == 0) || (!foreignKeyName.isPresent()) || (foreignKeyName.isPresent() && foreignKeyName.get().isEmpty())) {
            return getReference(getTable().getPrimaryKeyColumns(), object);
        }
        try {
            for (ForeignKeyConstraint foreignKeyConstraint : foreignKeyConstraints) {
                String fieldName = foreignKeyConstraint.getFieldName();
                object = ((Reference<?>) (((TableImpl) foreignKeyConstraint.getReferencedTable()).getDomainMapper().getField(object.getClass(), fieldName)
                        .get(object))).getOptional().get();

                if (foreignKeyConstraint.getName().compareToIgnoreCase(foreignKeyName.get()) == 0) {
                    return getReference(foreignKeyConstraint.getReferencedTable().getPrimaryKeyColumns(), object);
                }
            }
            throw new IllegalStateException("");
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("");
        }
    }

    private Object getDomainShortReference(Object object, Optional<String> foreignKeyName) {
        if ((foreignKeyConstraints.size() == 0) || (!foreignKeyName.isPresent()) || (foreignKeyName.isPresent() && foreignKeyName.get().isEmpty())) {
            return getDomainShortReference(getTable().getPrimaryKeyColumns(), object);
        }
        try {
            for (ForeignKeyConstraint foreignKeyConstraint : foreignKeyConstraints) {
                String fieldName = foreignKeyConstraint.getFieldName();
                object = ((Reference<?>) (((TableImpl) foreignKeyConstraint.getReferencedTable()).getDomainMapper().getField(object.getClass(), fieldName)
                        .get(object))).getOptional().get();

                if (foreignKeyConstraint.getName().compareToIgnoreCase(foreignKeyName.get()) == 0) {
                    return getDomainShortReference(foreignKeyConstraint.getReferencedTable().getPrimaryKeyColumns(), object);
                }
            }
            throw new IllegalStateException("");
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException("");
        }
    }

    @Override
    public Table<?> getTouchTable() {
        if (domainForeignKey.isPresent() && !domainForeignKey.get().isEmpty()) {
            return getForeignKeyConstraintsByName(domainForeignKey.get()).map(foreignKeyConstraint -> (TableImpl) foreignKeyConstraint.getReferencedTable()).orElse(getTable());
        }

        if (foreignKeyConstraints.size() > 0) {
            return foreignKeyConstraints.get(foreignKeyConstraints.size() - 1).getReferencedTable();
        }
        return getTable();
    }

    private Optional<ForeignKeyConstraint> getForeignKeyConstraintsByName(String name) {
        return foreignKeyConstraints.stream()
                .filter(foreignKeyConstraint -> foreignKeyConstraint.getName().compareToIgnoreCase(name) == 0).findFirst();
    }

    public static class BuilderImpl implements TableAudit.Builder {
        private final TableAuditImpl tableAudit;

        public BuilderImpl(TableImpl<?> table, String name) {
            this.tableAudit = TableAuditImpl.from(table, name);
        }

        @Override
        public Builder domainReferences(String... foreignKeyConstraintList) {
            Table<?> table = tableAudit.getTable();
            for (String foreignKeyConstraint : foreignKeyConstraintList) {
                String tableName = table.getName();
                ForeignKeyConstraint fkc = table.getForeignKeyConstraints().stream()
                        .filter(f -> f.getName().compareToIgnoreCase(foreignKeyConstraint) == 0)
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("Cannot locate " + foreignKeyConstraint + " foreign key constraint in " + tableName + " table"));
                tableAudit.foreignKeyConstraints.add(fkc);
                table = fkc.getReferencedTable();
            }

            if (foreignKeyConstraintList.length!=0) {
                tableAudit.domainForeignKey = Optional.of(foreignKeyConstraintList[foreignKeyConstraintList.length - 1]);
            }
            return this;
        }

        @Override
        public Builder reverseReferenceMap(String reverseReferenceMap) {
            tableAudit.reverseReferenceMap = Optional.of(reverseReferenceMap);
            return this;
        }

        @Override
        public Builder contextReferenceColumn(String... contextReferenceColumn) {
            tableAudit.contextReferenceColumns.addAll(Arrays.stream(contextReferenceColumn).collect(Collectors.toList()));
            return this;
        }



        @Override
        public Builder domainReferenceColumn(String domainReferenceColumn) {
            tableAudit.domainReferenceColumn = Optional.of(domainReferenceColumn);
            return this;
        }

        @Override
        public TableAudit build() {
            return add();
        }

        @Override
        public Builder domainContext(Integer domainContext) {
            tableAudit.domainContext = domainContext;
            return this;
        }

        public TableAudit add() {
            tableAudit.getTable().add(tableAudit);
            return tableAudit;
        }

    }
}
