package com.elster.jupiter.orm.audit;

import com.elster.jupiter.orm.Column;
import com.elster.jupiter.orm.ForeignKeyConstraint;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.TableAudit;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.TableImpl;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TableAuditImpl implements TableAudit {

    private ForeignKeyConstraint foreignKeyConstraint = null;
    private String category;
    private String subCategory;
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
    public String getCategory() {
        return category;
    }

    @Override
    public String getSubCategory() {
        return subCategory;
    }

    public ForeignKeyConstraint getForeignKeyConstraint() {
        return foreignKeyConstraint;
    }

    @Override
    public String getObjectReferences(Object object) {
        return "{" + Optional.ofNullable(foreignKeyConstraint)
                .map(foreignKeyConstraint -> (List<ColumnImpl>) foreignKeyConstraint.getColumns())
                .orElse(getTable().getPrimaryKeyColumns())
                .stream()
                .map(column -> column.getName() + ":" + ((ColumnImpl) column).domainValue(object))
                .collect(Collectors.joining(",")) + "}";
    }

    @Override
    public String getObjectIndentifier(Object object) {
        Stream<Column> columns = Stream.concat(getTable().getPrimaryKeyColumns().stream(), Arrays.stream(getTable().getVersionColumns()));
        return "{" + columns
                .map(column -> column.getName() + ":" + ((ColumnImpl) column).domainValue(object))
                .collect(Collectors.joining(",")) + "}";
    }

    public Table<?> getTouchTable() {
        if (foreignKeyConstraint != null) {
            return foreignKeyConstraint.getReferencedTable();
        }
        return getTable();
    }


    public static class BuilderImpl implements TableAudit.Builder {
        private final TableAuditImpl tableAudit;

        public BuilderImpl(TableImpl<?> table, String name) {
            this.tableAudit = TableAuditImpl.from(table, name);
        }

        @Override
        public TableAudit references(ForeignKeyConstraint foreignKeyConstraint) {
            tableAudit.foreignKeyConstraint = foreignKeyConstraint;
            return add();
        }

        @Override
        public Builder category(String category) {
            tableAudit.category = category;
            return this;
        }

        @Override
        public Builder subCategory(String subCategory) {
            tableAudit.subCategory = subCategory;
            return this;
        }

        public TableAudit add() {
            tableAudit.getTable().add(tableAudit);
            return tableAudit;
        }

    }
}
