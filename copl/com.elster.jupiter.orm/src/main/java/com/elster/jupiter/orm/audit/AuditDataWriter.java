package com.elster.jupiter.orm.audit;

import com.elster.jupiter.orm.TableAudit;
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

public class AuditDataWriter<T> {
    private final DataMapperImpl<T> dataMapper;
    private Object object;
    private Instant instant;
    private UnexpectedNumberOfUpdatesException.Operation operation;

    public AuditDataWriter(DataMapperImpl<T> dataMapper, Object object, Instant instant, UnexpectedNumberOfUpdatesException.Operation operation) {
        this.dataMapper = dataMapper;
        this.object = object;
        this.instant = instant;
        this.operation = operation;
    }

    public void audit() throws SQLException {
        if (getTable().hasAudit() && doJournal(getColumns())) {
            long domainId = getAuditDomain(object, instant, operation);
            persistAuditContext(object, instant, operation, domainId);
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

    private long getAuditDomain(Object object, Instant now, UnexpectedNumberOfUpdatesException.Operation operation) throws SQLException {

        try (Connection connection = getConnection(true)) {
            TableAudit tableAudit = getTable().getTableAudit();
            List<DomainContextIdentifier> contextAuditIdentifiers = getContextAuditIdentifiers();
            return contextAuditIdentifiers.stream()
                    .filter(contextIdentifierEntry ->
                            (contextIdentifierEntry.getDomain().compareToIgnoreCase(tableAudit.getTouchTable().getName()) == 0) &&
                                    (contextIdentifierEntry.getContext().compareToIgnoreCase(tableAudit.getContext()) == 0) &&
                                    (contextIdentifierEntry.getReference().compareToIgnoreCase(tableAudit.getDomainReferences(object)) == 0) &&
                                    (contextIdentifierEntry.getOperation() == operation.ordinal()))
                    .findFirst()
                    .map(DomainContextIdentifier::getId)
                    .orElseGet(() -> {
                        try {
                            Long nextVal = getNext(connection, "ADT_AUDIT_LOGID");

                            updateContextAuditIdentifiers(new DomainContextIdentifier().setId(nextVal)
                                    .setDomain(tableAudit.getTouchTable().getName())
                                    .setContext(tableAudit.getContext())
                                    .setReference(tableAudit.getDomainReferences(object))
                                    .setOperation(operation.ordinal()));
                            persistAuditDomain(object, now, operation, nextVal);
                            return nextVal;
                        } catch (SQLException e) {
                        }
                        return 0L;
                    });
        }
    }

    private void persistAuditDomain(Object object, Instant now, UnexpectedNumberOfUpdatesException.Operation operation, Long nextVal) throws SQLException {

        TableAudit tableAudit = getTable().getTableAudit();
        String auditLog = getSqlGenerator().auditSql();
        try (Connection connection = getConnection(true)) {
            try (PreparedStatement statement = connection.prepareStatement(auditLog)) {

                int index = 1;
                statement.setLong(index++, nextVal); // ID
                statement.setString(index++, tableAudit.getTouchTable().getName()); // table name
                statement.setString(index++, tableAudit.getDomainReferences(object)); // domain
                statement.setString(index++, tableAudit.getDomainShortReference(object).toString()); // short domain references
                statement.setString(index++, tableAudit.getDomain()); // domain
                statement.setString(index++, tableAudit.getContext()); // context
                statement.setLong(index++, operation.ordinal()); // operation
                statement.setLong(index++, now.toEpochMilli()); // create time
                statement.setString(index++, getCurrentUserName()); //user name
                statement.execute();
            }
        }
    }

    private void persistAuditContext(Object object, Instant now, UnexpectedNumberOfUpdatesException.Operation operation, long domainId) throws SQLException {

        TableAudit tableAudit = getTable().getTableAudit();
        String auditLogSql = getSqlGenerator().auditLogSql();
        try (Connection connection = getConnection(true)) {
            try (PreparedStatement statement = connection.prepareStatement(auditLogSql)) {
                int index = 1;
                Long nextVal = getNext(connection, "ADT_AUDIT_LOGID");

                statement.setLong(index++, nextVal);
                statement.setLong(index++, domainId);
                statement.setString(index++, getTable().getName());
                statement.setString(index++, tableAudit.getObjectIndentifier(object));
                statement.execute();
            }
        }
    }

    private List<DomainContextIdentifier> getContextAuditIdentifiers() {
        Object contextAudit = getTable().getDataModel().getOrmService().getTransactionService().getTransactionProperties().getProperty("AUDIT_CONTEXT");
        return Optional.ofNullable(contextAudit).map(obj -> (List<DomainContextIdentifier>) obj)
                .orElseGet(() -> {
                    List<DomainContextIdentifier> domainContext = new ArrayList<>();
                    getTable().getDataModel().getOrmService().getTransactionService().getTransactionProperties().setProperty("AUDIT_CONTEXT", domainContext);
                    return domainContext;
                });
    }

    private void updateContextAuditIdentifiers(DomainContextIdentifier domainContextIdentifier) {
        Object contextAudit = getTable().getDataModel().getOrmService().getTransactionService().getTransactionProperties().getProperty("AUDIT_CONTEXT");
        Optional.ofNullable(contextAudit)
                .map(obj -> (List<DomainContextIdentifier>) obj)
                .ifPresent(domainContextIdentifiers -> {
                    domainContextIdentifiers.add(domainContextIdentifier);
                    getTable().getDataModel().getOrmService().getTransactionService().getTransactionProperties().removeProperty("AUDIT_CONTEXT");
                    getTable().getDataModel().getOrmService().getTransactionService().getTransactionProperties().setProperty("AUDIT_CONTEXT", domainContextIdentifiers);
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
