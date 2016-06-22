package com.elster.jupiter.servicecall.rest.impl;

import com.elster.jupiter.servicecall.ServiceCall;
import com.elster.jupiter.servicecall.ServiceCallFilter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class ServiceCallFilterImpl implements ServiceCallFilter {

    private String reference;
    private List<String> types = new ArrayList<>();
    private List<String> states = new ArrayList<>();
    private Instant receivedDateFrom;
    private Instant receivedDateTo;
    private Instant modificationDateFrom;
    private Instant modificationDateTo;
    private ServiceCall parent;
    private Object targetObject;

    @Override
    public String getReference() {
        return reference;
    }

    @Override
    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public List<String> getTypes() {
        return Collections.unmodifiableList(types);
    }

    @Override
    public void setTypes(List<String> types) {
        this.types = new ArrayList<>(types);
    }

    @Override
    public List<String> getStates() {
        return Collections.unmodifiableList(states);
    }

    @Override
    public void setStates(List<String> states) {
        this.states = new ArrayList<>(states);
    }

    @Override
    public Instant getReceivedDateFrom() {
        return receivedDateFrom;
    }

    @Override
    public void setReceivedDateFrom(Instant receivedDateFrom) {
        this.receivedDateFrom = receivedDateFrom;
    }

    @Override
    public Instant getReceivedDateTo() {
        return receivedDateTo;
    }

    @Override
    public void setReceivedDateTo(Instant receivedDateTo) {
        this.receivedDateTo = receivedDateTo;
    }

    @Override
    public Instant getModificationDateFrom() {
        return modificationDateFrom;
    }

    @Override
    public void setModificationDateFrom(Instant modificationDateFrom) {
        this.modificationDateFrom = modificationDateFrom;
    }

    @Override
    public Instant getModificationDateTo() {
        return modificationDateTo;
    }

    @Override
    public void setModificationDateTo(Instant modificationDateTo) {
        this.modificationDateTo = modificationDateTo;
    }

    @Override
    public ServiceCall getParent() {
        return parent;
    }

    @Override
    public void setParent(ServiceCall parent) {
        this.parent = parent;
    }

    @Override
    public Object getTargetObject() {
        return targetObject;
    }

    @Override
    public void setTargetObject(Object object) {
        this.targetObject = object;
    }

}