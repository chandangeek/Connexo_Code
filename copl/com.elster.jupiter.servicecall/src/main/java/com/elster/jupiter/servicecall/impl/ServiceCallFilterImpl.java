package com.elster.jupiter.servicecall.impl;

import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class ServiceCallFilterImpl implements ServiceCallFilter {

    private String reference;
    private List<String> types = new ArrayList<>();
    private List<String> states = new ArrayList<>();
    private Instant receivedDateFrom;
    private Instant receivedDateTo;
    private Instant modificationDateFrom;
    private Instant modificationDateTo;
    private ServiceCall parent;

    @Override
    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public void setTypes(List<String> types) {
        this.types = types;
    }

    @Override
    public void setStates(List<String> states) {
        this.states = states;
    }

    @Override
    public void setReceivedDateFrom(Instant receivedDateFrom) {
        this.receivedDateFrom = receivedDateFrom;
    }

    @Override
    public void setReceivedDateTo(Instant receivedDateTo) {
        this.receivedDateTo = receivedDateTo;
    }

    @Override
    public void setModificationDateFrom(Instant modificationDateFrom) {
        this.modificationDateFrom = modificationDateFrom;
    }

    @Override
    public void setModificationDateTo(Instant modificationDateTo) {
        this.modificationDateTo = modificationDateTo;
    }

    @Override
    public void setParent(ServiceCall parent) {
        this.parent = parent;
    }

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public List<String> getTypes() {
        return types;
    }

    @Override
    public List<String> getStates() {
        return states;
    }

    @Override
    public Instant getReceivedDateFrom() {
        return receivedDateFrom;
    }

    @Override
    public Instant getReceivedDateTo() {
        return receivedDateTo;
    }

    @Override
    public Instant getModificationDateFrom() {
        return modificationDateFrom;
    }

    @Override
    public Instant getModificationDateTo() {
        return modificationDateTo;
    }

    @Override
    public ServiceCall getParent() {
        return parent;
    }
}
