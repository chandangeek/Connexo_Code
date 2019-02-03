package com.elster.jupiter.orm.audit;

import com.elster.jupiter.orm.TableAudit;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.TableImpl;
import com.elster.jupiter.orm.impl.TableSqlGenerator;

import java.security.Principal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AuditTrailDataWriter<T> {
    static final String AUDIT_CONTEXT = "AUDIT_TRAIL_CONTEXT";
    private final DataMapperImpl<T> dataMapper;
    private Object object;
    private Instant instant;
    private UnexpectedNumberOfUpdatesException.Operation operation;
    private boolean isTouch;

    public AuditTrailDataWriter(DataMapperImpl<T> dataMapper, Object object, Instant instant, UnexpectedNumberOfUpdatesException.Operation operation, boolean isTouch) {
        this.dataMapper = dataMapper;
        this.object = object;
        this.instant = instant;
        this.operation = operation;
        this.isTouch = isTouch;
    }

    public void audit() throws SQLException {
        if (getTable().hasAudit() && doJournal(getColumns())) {
            getAuditDomain(object, instant, operation);
        }
    }

    private List<ColumnImpl> getColumns() {
        return getTable().getRealColumns().collect(Collectors.toList());
    }

    private boolean doJournal(List<ColumnImpl> columns) {
        return columns.stream().anyMatch(ColumnImpl::alwaysJournal);
    }

    private TableImpl<? super T> getTable() {
        return dataMapper.getTable();
    }

    private Connection getConnection(boolean tranactionRequired) throws SQLException {
        return getTable().getDataModel().getConnection(tranactionRequired);
    }

    private TableSqlGenerator getSqlGenerator() {
        return dataMapper.getSqlGenerator();
    }

    private String getCurrentUserName() {
        Principal principal = getTable().getDataModel().getPrincipal();
        return principal == null ? null : principal.getName();
    }

    private void getAuditDomain(Object object, Instant now, UnexpectedNumberOfUpdatesException.Operation operation) throws SQLException {

        try (Connection connection = getConnection(true)) {
            TableAudit tableAudit = getTable().getTableAudit();
            List<Object> pkColumns = tableAudit.getDomainPkValues(object);
            List<DomainContextIdentifier> contextAuditIdentifiers = getContextAuditIdentifiers();
            resolveIncompleteContextIdentifier(object);
            contextAuditIdentifiers.stream()
                    .filter(contextIdentifierEntry -> {
                        return
                                (contextIdentifierEntry.getDomain().compareToIgnoreCase(tableAudit.getDomain()) == 0) &&
                                        (contextIdentifierEntry.getContext().compareToIgnoreCase(tableAudit.getContext()) == 0) &&
                                        (contextIdentifierEntry.getPkColumn() == getPkColumnByIndex(pkColumns, 0));
                    })
                    .findFirst()
                    .map(domainContextIdentifier -> {
                        try {
                            updateAuditDomain(now, domainContextIdentifier);
                        } catch (SQLException e) {
                        }
                        return domainContextIdentifier;
                    })
                    .map(DomainContextIdentifier::getId)
                    .orElseGet(() -> {
                        try {
                            if (isTouch == false) {
                                Long nextVal = getNext(connection, "ADT_AUDIT_TRAILID");
                                updateContextAuditIdentifiers(new DomainContextIdentifier().setId(nextVal)
                                        .setDomain(tableAudit.getDomain())
                                        .setContext(tableAudit.getContext())
                                        .setPkColumn(getPkColumnByIndex(pkColumns, 0))
                                        .setOperation(operation.ordinal())
                                        .setObject(object)
                                        .setTableAudit(tableAudit)
                                        .setReverseReferenceMapValue(getPkColumnByIndex(tableAudit.getContextPkValues(object), 0)));
                                persistAuditDomain(object, now, operation, nextVal, pkColumns);
                                return nextVal;
                            }
                            return 0L;
                        } catch (SQLException e) {
                            throw new UnderlyingSQLFailedException(e);
                        }
                    });
        }
    }

    private long getPkColumnByIndex(List<Object> pkColumns, int index) {
        if (pkColumns.size() > index) {
            return Long.parseLong(pkColumns.get(index).toString());
        }
        return 0;
    }

    private long getPkColumnBy(DomainContextIdentifier domainContextIdentifier) {
        if (domainContextIdentifier.getPkColumn() == 0) {
            domainContextIdentifier.setPkColumn(getPkColumnByIndex(getTable().getTableAudit().getDomainPkValues(object), 0));
        }
        return domainContextIdentifier.getPkColumn();
    }

    private void persistAuditDomain(Object object, Instant now, UnexpectedNumberOfUpdatesException.Operation operation, Long nextVal, List<Object> pkColumns) throws SQLException {

        TableAudit tableAudit = getTable().getTableAudit();
        String auditLog = getSqlGenerator().auditTrailSql();
        try (Connection connection = getConnection(true)) {
            try (PreparedStatement statement = connection.prepareStatement(auditLog)) {

                //ID, DOMAIN, CONTEXT, MODTIMESTART, MODTIMEEND, TABLENAME, PKCOLUMN, OPERATION, CREATETIME, USERNAME
                int index = 1;
                statement.setLong(index++, nextVal); // ID
                statement.setString(index++, tableAudit.getDomain()); // domain
                statement.setString(index++, tableAudit.getContext()); // context
                statement.setLong(index++, now.toEpochMilli()); // MODTIMESTART
                statement.setLong(index++, now.toEpochMilli()); // MODTIMEEND
                statement.setString(index++, tableAudit.getTouchTable().getName()); // table name
                statement.setLong(index++, getPkColumnByIndex(pkColumns, 0));
                statement.setLong(index++, operation.ordinal()); // operation
                statement.setLong(index++, now.toEpochMilli()); // create time
                statement.setString(index++, getCurrentUserName()); //user name
                statement.execute();
            }
        }
    }

    private void updateAuditDomain(Instant now, DomainContextIdentifier domainContextIdentifier) throws SQLException {
        Long nextVal = domainContextIdentifier.getId();
        String auditLog = getSqlGenerator().updateAuditTrailSql();
        try (Connection connection = getConnection(true)) {
            try (PreparedStatement statement = connection.prepareStatement(auditLog)) {
                int index = 1;
                statement.setLong(index++, now.toEpochMilli()); // MODTIMEEND
                statement.setLong(index++, getPkColumnBy(domainContextIdentifier)); // PKCOLUMN
                statement.setLong(index++, nextVal); // ID
                statement.execute();
            }
        }
    }

    private List<DomainContextIdentifier> getContextAuditIdentifiers() {
        Object contextAudit = getTable().getDataModel().getOrmService().getTransactionService().getTransactionProperties().getProperty(AUDIT_CONTEXT);
        return Optional.ofNullable(contextAudit).map(obj -> (List<DomainContextIdentifier>) obj)
                .orElseGet(() -> {
                    List<DomainContextIdentifier> domainContext = new ArrayList<>();
                    getTable().getDataModel().getOrmService().getTransactionService().getTransactionProperties().setProperty(AUDIT_CONTEXT, domainContext);
                    return domainContext;
                });
    }

    private void updateContextAuditIdentifiers(DomainContextIdentifier domainContextIdentifier) {
        Object contextAudit = getTable().getDataModel().getOrmService().getTransactionService().getTransactionProperties().getProperty(AUDIT_CONTEXT);
        Optional.ofNullable(contextAudit)
                .map(obj -> (List<DomainContextIdentifier>) obj)
                .ifPresent(domainContextIdentifiers -> {
                    domainContextIdentifiers.add(domainContextIdentifier);
                    getTable().getDataModel().getOrmService().getTransactionService().getTransactionProperties().removeProperty(AUDIT_CONTEXT);
                    getTable().getDataModel().getOrmService().getTransactionService().getTransactionProperties().setProperty(AUDIT_CONTEXT, domainContextIdentifiers);
                });
    }

    private void resolveIncompleteContextIdentifier(Object object) {
        List<DomainContextIdentifier> contextAuditIdentifiers = getContextAuditIdentifiers();

        contextAuditIdentifiers.stream()
                .filter(contextIdentifierEntry -> contextIdentifierEntry.getPkColumn() == 0)
                .forEach(contextIdentifierEntry -> {
                    TableAudit tableAudit = getTable().getTableAudit();
                    if (contextIdentifierEntry.getTableAudit().getTouchTable().getName().compareToIgnoreCase(getTable().getName()) == 0) {
                        contextIdentifierEntry.getTableAudit().getReverseReferenceMap(object).ifPresent(reverseReference -> {
                            if (reverseReference.longValue() == contextIdentifierEntry.getReverseReferenceMapValue().longValue()) {
                                contextIdentifierEntry
                                        .setPkColumn(getPkColumnByIndex(tableAudit.getDomainPkValues(object), 0));

                            }
                        });
                    }

                });
    }

    private long getNext(Connection connection, String sequence) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select " + sequence + ".nextval from dual")) {
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }
}
