package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.DefaultFinder;
import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointLog;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.OutboundEndPointProvider;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrenceStatus;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallRelatedObject;
import com.elster.jupiter.soap.whiteboard.cxf.WebServicesService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.util.HasId;

import com.google.common.collect.SetMultimap;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import static com.elster.jupiter.util.conditions.Where.where;

public class WebServiceCallOccurrenceImpl implements WebServiceCallOccurrence, HasId {
    private final DataModel dataModel;
    private final TransactionService transactionService;
    private final WebServicesService webServicesService;

    private long id;
    private Reference<EndPointConfiguration> endPointConfiguration = Reference.empty();
    private Instant startTime;
    private Instant endTime;
    private String requestName;
    private WebServiceCallOccurrenceStatus status;
    private String applicationName;
    private String payload;

    public enum Fields {
        ID("id"),
        START_TIME("startTime"),
        END_TIME("endTime"),
        REQUEST_NAME("requestName"),
        ENDPOINT_CONFIGURATION("endPointConfiguration"),
        STATUS("status"),
        APPLICATION_NAME("applicationName"),
        PAYLOAD("payload");

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
                                        WebServicesService webServicesService) {
        this.dataModel = dataModel;
        this.transactionService = transactionService;
        this.webServicesService = webServicesService;
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

    private void createRelatedObject( String key, String value){
        if (key != null && value != null) {
            String[] fieldName = {"key", "value"};
            String[] values = {key, value};
            Optional<WebServiceCallRelatedObject> relatedObject = dataModel.mapper(WebServiceCallRelatedObject.class)
                    .getUnique(fieldName, values);
            if (!relatedObject.isPresent()) {
                relatedObject = Optional.of(dataModel.getInstance(WebServiceCallRelatedObjectImpl.class));
                relatedObject.get().init(key, value);
                relatedObject.get().save();
            }

            WebServiceCallRelatedObjectBindingImpl relatedObjectBinding = dataModel.getInstance(WebServiceCallRelatedObjectBindingImpl.class);
            relatedObjectBinding.init(this, relatedObject.get());
            relatedObjectBinding.save();
        }
    }

    @Override
    public void createRelatedObjectIndependantTransaction( String key, String value){
        transactionService.runInIndependentTransaction(()-> {
            createRelatedObject(key, value);
        });
    }

    @Override
    public void createRelatedObjects(SetMultimap<String,String> values){

        transactionService.runInIndependentTransaction(()-> {
            values.keys().forEach(key -> {
                values.get(key).forEach(value -> {
                    createRelatedObject(key, value);
                });

            });
        });
    }




    @Override
    public Finder<EndPointLog> getLogs() {
        return DefaultFinder.of(EndPointLog.class, where("occurrence").isEqualTo(this), dataModel)
                .defaultSortColumn("timestamp", false);
    }
}
