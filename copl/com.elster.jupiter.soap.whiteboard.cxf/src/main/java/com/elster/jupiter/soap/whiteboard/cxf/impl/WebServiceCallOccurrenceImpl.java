package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.WebServiceCallOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.util.HasId;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Objects;

public class WebServiceCallOccurrenceImpl implements WebServiceCallOccurrence, HasId {

    private long id;
    Instant startTime;
    Instant endTime;
    String requestName;
    //WebService webService;
    private Reference<EndPointConfiguration> endPointConfiguration = Reference.empty();
    String status;
    String applicationName;
    private String payload;
    private DataModel dataModel;

    public enum Fields {
        ID("id"),
        startTime("startTime"),
        endTime("endTime"),
        requestName("requestName"),
        endPointConfiguration("endPointConfiguration"),
        status("status"),
        applicationName("applicationName"),
        payload("payload");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    @Inject
    public WebServiceCallOccurrenceImpl(){

    }

    public WebServiceCallOccurrenceImpl(DataModel dataModel,
                                        Instant startTime,
                                        String requestName,
                                        String applicationName,
                                        EndPointConfiguration endPointConfiguration)
    {
        this(dataModel, startTime, requestName, applicationName, endPointConfiguration, null);
    }


    public WebServiceCallOccurrenceImpl(DataModel dataModel,
                                        Instant startTime,
                                        String requestName,
                                        String applicationName,
                                        EndPointConfiguration endPointConfiguration,
                                        String payload)
    {
        this.dataModel = dataModel;
        this.startTime = startTime;
        this.requestName = requestName;
        this.applicationName = applicationName;
        this.status = null; // TODO: add status
        this.endPointConfiguration.set(endPointConfiguration);
        this.payload = payload;
    }

    @Override
    public long getId(){return this.id;}

    @Override
    public Instant getStartTime(){
        return startTime;
    };

    @Override
    public Instant getEndTime(){
        return endTime;
    };
    @Override
    public String getRequest(){
        return requestName;
    };

    @Override
    public String getStatus(){
        return status;
    };
    @Override
    public String getApplicationName(){
        return applicationName;
    };

    @Override
    public String getPayload() {
        return this.payload;
    }

    @Override
    public EndPointConfiguration getEndPointConfiguration(){
        return endPointConfiguration.get();
    };

    @Override
    public void  setStartTime(Instant startTime){
        this.startTime = startTime;
    };

    @Override
    public void setEndTime(Instant endTime){
        this.endTime = endTime;
    };

    @Override
    public void setRequest(String requestName){
        this.requestName = requestName;
    };

    @Override
    public void setStatus(String status){
        this.status = status;
    };

    public void setApplicationName(String applicationName){
        this.applicationName = applicationName;
    };

    @Override
    public void log(LogLevel logLevel, String message){
        getEndPointConfiguration().log(logLevel, message);
    }

    @Override
    public void log(String message, Exception exception){
        getEndPointConfiguration().log(message, exception);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WebServiceCallOccurrenceImpl that = (WebServiceCallOccurrenceImpl) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public void setPayload(String payload) {
        this.payload = payload;
    }



    @Override
    public void save(){
        if (this.getId() > 0) {
            Save.UPDATE.save(this.dataModel, this, Save.Update.class);
        } else {
            Save.CREATE.save(this.dataModel, this, Save.Create.class);
        }
    }
}
