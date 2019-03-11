package com.elster.jupiter.orm.audit;

import com.elster.jupiter.orm.TableAudit;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.UnexpectedNumberOfUpdatesException;
import com.elster.jupiter.orm.impl.ColumnImpl;
import com.elster.jupiter.orm.impl.DataMapperImpl;
import com.elster.jupiter.orm.impl.DataMapperReader;
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
        if (getTable().hasAudit() && doJournal(getColumns()) && isAuditEnabled()) {
            DataMapperReader<? super T> reader = getTable().getDataMapper().getReader();
            if ((operation != UnexpectedNumberOfUpdatesException.Operation.UPDATE) ||
                    ((operation == UnexpectedNumberOfUpdatesException.Operation.UPDATE) &&
                        isSomethingChanged(object, reader.findByPrimaryKey(getTable().getPrimaryKey(object)).get(), getColumns()))) {
                getAuditDomain(object, instant, operation);
            }
        }
    }

    public boolean isSomethingChanged(Object object, Object oldObject, List<ColumnImpl> columns) throws SQLException {
        return columns.stream()
                .filter(ColumnImpl::alwaysJournal)
                .filter(column -> {
                    if (column.isMAC()) {
                        return false;
                    }
                    Object newValue = column.domainValue(object);
                    Object oldValue = column.domainValue(object);
                    return !(newValue == null ? oldValue == null : column.domainValue(object).equals(column.domainValue(oldObject)));
                })
                .count() > 0;
    }

    private boolean isAuditEnabled() {
        return getAuditEnabledProperty().toLowerCase().equals("true");
    }

    private String getAuditEnabledProperty() {
        return Optional.ofNullable(getTable().getDataModel().getOrmService().getEnableAuditing()).orElse("false");
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
            List<Object> pkDomainColumns = tableAudit.getDomainPkValues(object);
            List<Object> pkContextColumns = tableAudit.getContextPkValues(object);
            List<DomainContextIdentifier> contextAuditIdentifiers = getContextAuditIdentifiers();
            resolveIncompleteContextIdentifier(object);
            contextAuditIdentifiers.stream()
                    .filter(contextIdentifierEntry -> (contextIdentifierEntry.getDomainContext() == tableAudit.getDomainContext()) &&
                          //  (contextIdentifierEntry.getOperation() == operation.ordinal()) &&
                            (contextIdentifierEntry.getPkDomainColumn() == getPkColumnByIndex(pkDomainColumns, 0)) &&
                            (contextIdentifierEntry.getPkContextColumn() == getPkColumnByIndex(pkContextColumns, 0)))
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
                            if (!isTouch) {
                                Long nextVal = getNext(connection, "ADT_AUDIT_TRAILID");
                                updateContextAuditIdentifiers(new DomainContextIdentifier().setId(nextVal)
                                        .setDomainContext(tableAudit.getDomainContext())
                                        .setPkDomainColumn(getPkColumnByIndex(pkDomainColumns, 0))
                                //        .setPkContextColumn(getPkColumnByIndex(pkContextColumns, 0))
                                        .setOperation(operation.ordinal())
                                        .setObject(object)
                                        .setTableAudit(tableAudit)
                                        .setReverseReferenceMapValue(getPkColumnByIndex(tableAudit.getResersePkValues(object), 0)));
                                persistAuditDomain(object, now, operation, nextVal, pkDomainColumns, pkContextColumns);
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
        if (domainContextIdentifier.getPkDomainColumn() == 0) {
            domainContextIdentifier.setPkDomainColumn(getPkColumnByIndex(getTable().getTableAudit().getDomainPkValues(object), 0));
        }
        return domainContextIdentifier.getPkDomainColumn();
    }

    private void persistAuditDomain(Object object, Instant now, UnexpectedNumberOfUpdatesException.Operation operation, Long nextVal,
                                    List<Object> pkDomainColumns, List<Object> pkContextColumns) throws SQLException {

        TableAudit tableAudit = getTable().getTableAudit();
        String auditLog = getSqlGenerator().auditTrailSql();
        try (Connection connection = getConnection(true)) {
            try (PreparedStatement statement = connection.prepareStatement(auditLog)) {

                //IID, DOMAIN, MODTIMESTART, MODTIMEEND, PKDOMAIN, PKCONTEXT, OPERATION, CREATETIME, USERNAME
                int index = 1;
                statement.setLong(index++, nextVal); // ID
                statement.setLong(index++, tableAudit.getDomainContext()); // DOMAINCONTEXT
                statement.setLong(index++, now.toEpochMilli()); // MODTIMESTART
                statement.setLong(index++, now.toEpochMilli()); // MODTIMEEND
                statement.setLong(index++, getPkColumnByIndex(pkDomainColumns, 0));
                statement.setLong(index++, getPkColumnByIndex(pkContextColumns, 0));
                statement.setLong(index++, getPkColumnByIndex(pkContextColumns, 1));
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
                statement.setLong(index++, getPkColumnBy(domainContextIdentifier)); // PKCONTEXT
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
                .filter(contextIdentifierEntry -> contextIdentifierEntry.getPkDomainColumn() == 0)
                .forEach(contextIdentifierEntry -> {
                    TableAudit tableAudit = getTable().getTableAudit();
                    if (contextIdentifierEntry.getTableAudit().getTouchTable().getName().compareToIgnoreCase(getTable().getName()) == 0) {
                        contextIdentifierEntry.getTableAudit().getReverseReferenceMap(object).ifPresent(reverseReference -> {
                            if (reverseReference.longValue() == contextIdentifierEntry.getReverseReferenceMapValue().longValue()) {
                                contextIdentifierEntry
                                        .setPkDomainColumn(getPkColumnByIndex(tableAudit.getDomainPkValues(object), 0));
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
