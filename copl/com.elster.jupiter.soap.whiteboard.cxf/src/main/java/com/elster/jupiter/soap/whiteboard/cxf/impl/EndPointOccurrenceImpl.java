package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.LogLevel;
import com.elster.jupiter.soap.whiteboard.cxf.WebService;
import com.elster.jupiter.util.HasId;

import javax.inject.Inject;
import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.Objects;

public class EndPointOccurrenceImpl implements EndPointOccurrence , HasId {

    private long id;
    Instant startTime;
    Instant endTime;
    String requestName;
    //WebService webService;
    private Reference<EndPointConfiguration> endPointConfiguration = Reference.empty();
    String status;
    String applicationName;
    private byte[] payload;

    public enum Fields {
        ID("id"),
        startTime("startTime"),
        endTime("endTime"),
        requestName("requestName"),
        //webService("webService"),
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
    public EndPointOccurrenceImpl(){

    }

    public EndPointOccurrenceImpl(Instant startTime,
                                  String requestName,
                                  String applicationName,
                                  EndPointConfiguration endPointConfiguration,
                                  byte[] payload)
    {
        this.startTime = startTime;
        this.requestName = requestName;
        this.applicationName = applicationName;
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
    public EndPointConfiguration getEndPointConfiguration(){
        return endPointConfiguration.get();
    };

    public void  setStartTime(Instant startTime){
        this.startTime = startTime;
    };

    public void setEndTime(Instant endTime){
        this.endTime = endTime;
    };

    public void setRequest(String requestName){
        this.requestName = requestName;
    };

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
        EndPointOccurrenceImpl that = (EndPointOccurrenceImpl) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    @Override
    public byte[] getPayload() {
        return this.payload;
    }
}
