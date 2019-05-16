package com.elster.jupiter.soap.whiteboard.cxf.impl;

import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointConfiguration;
import com.elster.jupiter.soap.whiteboard.cxf.EndPointOccurrence;
import com.elster.jupiter.soap.whiteboard.cxf.WebService;

import java.time.Instant;

public class EndPointOccurrenceImpl implements EndPointOccurrence {
    Instant startTime;
    Instant endTime;
    String requestName;
    //WebService webService;
    private Reference<EndPointConfiguration> endPointConfiguration = Reference.empty();
    String status;

    public enum Fields {
        startTime("startTime"),
        endTime("endTime"),
        requestName("requestName"),
        webService("webService"),
        endPointConfiguration("endPointConfiguration"),
        status("status");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        public String fieldName() {
            return javaFieldName;
        }
    }

    public EndPointOccurrenceImpl(){

    }

    public EndPointOccurrenceImpl(Instant startTime,
                                  String requestName,
                                  WebService webService,
                                  String endPointName)
    {
        this.startTime = startTime;
        this.requestName = requestName;
        this.webService = webService;
        this.endPointName = endPointName;

    }

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
    public WebService getWebService(){
        return webService;
    };
    @Override
    public String getWebServiceEndPointName(){
        return endPointName;
    };
    @Override
    public String getStatus(){
        return status;
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

    public void  setWebService(WebService webService){
        this.webService = webService;
    };

    public  void setWebServiceEndPointName(String endPointName){
        this.endPointName = endPointName;
    };

    public void setStatus(String status){
        this.status = status;
    };


}
