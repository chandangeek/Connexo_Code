package com.elster.jupiter.servicecall;

import aQute.bnd.annotation.ConsumerType;

import java.time.Instant;
import java.util.List;

@ConsumerType
public interface ServiceCallFilter {

    String getReference();
    void setReference(String reference);

    List<String> getTypes();
    void setTypes(List<String> types);

    List<String> getStates();
    void setStates(List<String> states);

    Instant getReceivedDateFrom();
    void setReceivedDateFrom(Instant receivedDateFrom);

    Instant getReceivedDateTo();
    void setReceivedDateTo(Instant receivedDateTo);

    Instant getModificationDateFrom();
    void setModificationDateFrom(Instant modificationDateFrom);

    Instant getModificationDateTo();
    void setModificationDateTo(Instant modificationDateTo);

    ServiceCall getParent();
    void setParent(ServiceCall serviceCall);

    Object getTargetObject();
    void setTargetObject(Object object);

}