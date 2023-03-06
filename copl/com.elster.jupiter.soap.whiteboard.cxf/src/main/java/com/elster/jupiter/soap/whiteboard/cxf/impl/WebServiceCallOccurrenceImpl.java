package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.nls.LocalizedException;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.LiteralSql;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceService;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceStatus;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.Checks;
import com.elster.jupiter.util.HasId;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.sql.SqlBuilder;

import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.elster.jupiter.util.conditions.Where.where;

@LiteralSql
public class WebServiceCallOccurrenceImpl implements WebServiceCallOccurrence, HasId {
    private final DataModel dataModel;
    private final TransactionService transactionService;
    private final WebServiceCallOccurrenceService webServiceCallOccurrenceService;
    private final WebServicesService webServicesService;
    private final ThreadPrincipalService threadPrincipalService;
    private final Thesaurus thesaurus;

    private long id;
    private Reference<EndPointConfiguration> endPointConfiguration = Reference.empty();
    private Instant startTime;
    private Instant endTime;
    private String requestName;
    private WebServiceCallOccurrenceStatus status;
    private String applicationName;
    private String appServerName;
    private String payload;

    public enum Fields {
        ID("id"),
        START_TIME("startTime"),
        END_TIME("endTime"),
        REQUEST_NAME("requestName"),
        ENDPOINT_CONFIGURATION("endPointConfiguration"),
        STATUS("status"),
        APPLICATION_NAME("applicationName"),
        PAYLOAD("payload"),
        APP_SERVER_NAME("appServerName");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @Inject
    public WebServiceCallOccurrenceImpl(DataModel dataModel,
                                        TransactionService transactionService,
                                        WebServiceCallOccurrenceService webServiceCallOccurrenceService, WebServicesService webServicesService,
                                        ThreadPrincipalService threadPrincipalService, Thesaurus thesaurus) {
        this.dataModel = dataModel;
        this.transactionService = transactionService;
        this.webServiceCallOccurrenceService = webServiceCallOccurrenceService;
        this.webServicesService = webServicesService;
        this.threadPrincipalService = threadPrincipalService;
        this.thesaurus = thesaurus;
    }

    public WebServiceCallOccurrenceImpl init(Instant startTime,
                                             String requestName,
                                             String applicationName,
                                             EndPointConfiguration endPointConfiguration) {
        return init(startTime, requestName, applicationName, endPointConfiguration, null);
    }


    public WebServiceCallOccurrenceImpl init(Instant startTime,
                                             String requestName,
                                             String applicationName,
                                             EndPointConfiguration endPointConfiguration,
                                             String payload) {
        this.startTime = startTime;
        this.requestName = requestName;
        this.applicationName = applicationName;
        this.status = WebServiceCallOccurrenceStatus.ONGOING;
        this.endPointConfiguration.set(endPointConfiguration);
        this.payload = payload;
        this.appServerName = threadPrincipalService.getAppServerName().orElse(null);
        return this;
    }

    @Override
    public long getId() {
        return this.id;
    }

    @Override
    public Instant getStartTime() {
        return startTime;
    }

    @Override
    public Optional<Instant> getEndTime() {
        return Optional.ofNullable(endTime);
    }

    @Override
    public Optional<String> getRequest() {
        return Optional.ofNullable(requestName);
    }

    @Override
    public WebServiceCallOccurrenceStatus getStatus() {
        return status;
    }

    @Override
    public Optional<String> getApplicationName() {
        return Optional.ofNullable(applicationName);
    }

    @Override
    public Optional<String> getPayload() {
        return Optional.ofNullable(payload);
    }

    @Override
    public Optional<String> getAppServerName() {
        return Optional.ofNullable(appServerName);
    }

    @Override
    public EndPointConfiguration getEndPointConfiguration() {
        return endPointConfiguration.get();
    }

    @Override
    public void setEndTime(Instant endTime) {
        this.endTime = endTime;
    }

    @Override
    public void setRequest(String requestName) {
        this.requestName = requestName;
    }

    @Override
    public void setStatus(WebServiceCallOccurrenceStatus status) {
        this.status = status;
    }

    @Override
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @Override
    public void setPayload(String payload) {
        this.payload = payload;
    }

    @Override
    public void log(LogLevel logLevel, String message) {
        getEndPointConfiguration().log(logLevel, message, this);
    }

    @Override
    public void log(String message, Exception exception) {
        getEndPointConfiguration().log(message, exception, this);
    }

    @Override
    public boolean equals(Object o) {
        return this == o
                || o instanceof WebServiceCallOccurrenceImpl
                && id == ((WebServiceCallOccurrenceImpl) o).id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public void save() {
        if (id > 0) {
            Save.UPDATE.save(this.dataModel, this, Save.Update.class);
        } else {
            Save.CREATE.save(this.dataModel, this, Save.Create.class);
        }
    }

    @Override
    public void retry() {
        EndPointConfiguration endPointConfiguration = getEndPointConfiguration();
        if (!endPointConfiguration.isInbound()) {
            Optional<EndPointProvider> endPointProviderOptional = webServicesService.getProvider(endPointConfiguration.getWebServiceName());
            if (endPointProviderOptional.isPresent() && endPointProviderOptional.get() instanceof OutboundEndPointProvider) {
                log(LogLevel.INFO, "Retrying web service call occurrence.");
                ((OutboundEndPointProvider) endPointProviderOptional.get()).using(requestName).toEndpoints(endPointConfiguration).sendRawXml(payload);
            } else {
                log(LogLevel.SEVERE, "Couldn't retry web service call occurrence: web service is either not registered in the system or doesn't support retry.");
            }
        }
    }

    @Override
    public void saveRelatedAttribute(String key, String value) {

        validateEntry(new AbstractMap.SimpleEntry<>(key, value));

        if (key != null && !Checks.is(value).emptyOrOnlyWhiteSpace()) {
            transactionService.runInIndependentTransaction(() -> {
                String valueToSave = value.trim();
                String[] fieldNames = {WebServiceCallRelatedAttributeImpl.Fields.ATTR_KEY.fieldName(), WebServiceCallRelatedAttributeImpl.Fields.ATTR_VALUE.fieldName()};
                String[] values = {key, valueToSave};
                Optional<WebServiceCallRelatedAttributeImpl> relatedAttribute = dataModel.mapper(WebServiceCallRelatedAttributeImpl.class)
                        .getUnique(fieldNames, values);
                if (!relatedAttribute.isPresent()) {
                    relatedAttribute = Optional.of(dataModel.getInstance(WebServiceCallRelatedAttributeImpl.class));
                    relatedAttribute.get().init(key, valueToSave);
                    relatedAttribute.get().save();
                }

                WebServiceCallRelatedAttributeBindingImpl relatedObjectBinding = dataModel.getInstance(WebServiceCallRelatedAttributeBindingImpl.class);
                relatedObjectBinding.init(this, relatedAttribute.get());
                relatedObjectBinding.save();
            });
        }
    }

    @Override
    public void cancel() {
        log(LogLevel.INFO, "Cancelling web service call occurrence.");
        webServicesService.cancelOccurrence(this.id);
    }

    @Override
    public void saveRelatedAttributes(SetMultimap<String, String> values) {
        if (values.isEmpty()) {
            return;
        }

        validateValues(values);

        List<WebServiceCallRelatedAttributeBindingImpl> relatedAttributeBindingList = new ArrayList<>();
        Optional<Condition> condition = values.entries().stream()
                .filter(entry -> !Checks.is(entry.getValue()).emptyOrOnlyWhiteSpace() && entry.getKey() != null)
                .map(entry -> where(WebServiceCallRelatedAttributeImpl.Fields.ATTR_KEY.fieldName()).isEqualTo(entry.getKey())
                        .and(where(WebServiceCallRelatedAttributeImpl.Fields.ATTR_VALUE.fieldName()).isEqualTo(entry.getValue().trim())))
                .reduce(Condition::or);

        if (condition.isPresent()) {
            List<WebServiceCallRelatedAttributeImpl> finalCreatedRelatedAttributeList;
            /* Find all related attributes that has been already created */
            List<WebServiceCallRelatedAttributeImpl> createdRelatedAttributeList = dataModel.query(WebServiceCallRelatedAttributeImpl.class).select(condition.get());
            List<SqlBuilder> sqlQueries = new ArrayList<>();
            /* Find all related attributes that should be created */
            values.entries().forEach(entry -> {
                if (entry.getKey() != null && !Checks.is(entry.getValue()).emptyOrOnlyWhiteSpace()) {
                    WebServiceCallRelatedAttributeImpl relatedAttribute = Optional.of(dataModel.getInstance(WebServiceCallRelatedAttributeImpl.class)).get();
                    relatedAttribute.init(entry.getKey(), entry.getValue().trim());
                    if (!createdRelatedAttributeList.contains(relatedAttribute)) {
                        sqlQueries.add(oneRelatedAttributeInsertSql(relatedAttribute));
                    }
                }
            });

            if (sqlQueries.isEmpty()) {
                finalCreatedRelatedAttributeList = createdRelatedAttributeList;
            } else {
                /* Create related attributes that hasn't been created yet */
                transactionService.runInIndependentTransaction(() -> {
                    try (Connection connection = this.dataModel.getConnection(true)) {
                        SqlBuilder sqlBuilder = new SqlBuilder("begin ");
                        sqlQueries.forEach(sqlBuilder::add);
                        sqlBuilder.append(" end;");
                        try (PreparedStatement statement = sqlBuilder.prepare(connection)) {
                            statement.execute();
                        }
                    } catch (SQLException e) {
                        throw new UnderlyingSQLFailedException(e);
                    }
                });
                finalCreatedRelatedAttributeList = dataModel.query(WebServiceCallRelatedAttributeImpl.class).select(condition.get());
            }

            transactionService.runInIndependentTransaction(() -> {
                finalCreatedRelatedAttributeList.forEach(obj -> {
                    WebServiceCallRelatedAttributeBindingImpl relatedAttributeBinding = dataModel.getInstance(WebServiceCallRelatedAttributeBindingImpl.class);
                    relatedAttributeBinding.init(this, obj);
                    relatedAttributeBindingList.add(relatedAttributeBinding);
                });

                dataModel.mapper(WebServiceCallRelatedAttributeBindingImpl.class).persist(relatedAttributeBindingList);
            });
        }
    }

    private void validateValues(SetMultimap<String, String> values) {
        values.entries().forEach(this::validateEntry);
    }

    private void validateEntry(Entry<String, String> entry) {
        if (entry.getValue().length() > Table.NAME_LENGTH) {
            throw new LocalizedException(thesaurus, MessageSeeds.FIELD_IS_TOO_LONG, webServiceCallOccurrenceService.translateAttributeType(entry.getKey())) {};
        }
    }

    private static SqlBuilder oneRelatedAttributeInsertSql(WebServiceCallRelatedAttributeImpl attribute) {
        SqlBuilder oneInsertSql = new SqlBuilder("begin insert into ");
        oneInsertSql.append(TableSpecs.WS_OCC_RELATED_ATTR.name());
        oneInsertSql.openBracket();
        oneInsertSql.append(Arrays.stream(WebServiceCallRelatedAttributeImpl.Fields.values())
                .map(WebServiceCallRelatedAttributeImpl.Fields::name)
                .collect(Collectors.joining(", ")));
        oneInsertSql.closeBracket();
        oneInsertSql.append(" values (");
        oneInsertSql.append(TableSpecs.WS_OCC_RELATED_ATTR.name() + "ID.nextval,");
        oneInsertSql.addObject(attribute.getKey());
        oneInsertSql.append(",");
        oneInsertSql.addObject(attribute.getValue());
        oneInsertSql.append("); commit; exception when DUP_VAL_ON_INDEX then rollback; end;");
        return oneInsertSql;
    }

    @Override
    public Finder<EndPointLog> getLogs() {
        return DefaultFinder.of(EndPointLog.class, where("occurrence").isEqualTo(this), dataModel)
                .defaultSortColumn("timestamp", false);
    }
}
