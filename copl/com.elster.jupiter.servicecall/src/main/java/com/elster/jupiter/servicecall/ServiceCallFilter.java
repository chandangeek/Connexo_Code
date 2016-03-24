package com.elster.jupiter.servicecall;

import java.time.Instant;
import java.util.List;

public interface ServiceCallFilter {

    void setReference(String reference);

    void setTypes(List<String> types);

    void setStates(List<String> states);

    void setReceivedDateFrom(Instant receivedDateFrom);

    void setReceivedDateTo(Instant receivedDateTo);

    void setModificationDateFrom(Instant modificationDateFrom);

    void setModificationDateTo(Instant modificationDateTo);

    void setParent(ServiceCall serviceCall);

    String getReference();

    List<String> getTypes();

    List<String> getStates();

    Instant getReceivedDateFrom();

    Instant getReceivedDateTo();

    Instant getModificationDateFrom();

    Instant getModificationDateTo();

    ServiceCall getParent();
}
