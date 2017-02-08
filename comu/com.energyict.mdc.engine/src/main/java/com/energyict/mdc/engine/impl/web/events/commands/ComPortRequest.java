/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.web.events.commands;

import com.energyict.mdc.common.NotFoundException;
import com.energyict.mdc.engine.config.ComPort;
import com.energyict.mdc.engine.impl.core.RunningComServer;
import com.energyict.mdc.engine.impl.events.EventPublisher;

import com.google.common.base.Strings;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;

/**
 * Provides an implementation for the {@link Request} interface
 * that represents a request to register interest
 * in events that relate to a number of {@link ComPort}s.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-05 (09:59)
 */
class ComPortRequest extends IdBusinessObjectRequest {

    private final RunningComServer comServer;
    private List<ComPort> comPorts;

    ComPortRequest(RunningComServer comServer, long comPortId) {
        this(comServer, singleton(comPortId));
    }

    ComPortRequest(RunningComServer comServer, Set<Long> comPortIds) {
        super(comPortIds);
        this.comServer = comServer;
        this.validateComPortIds();
    }

    ComPortRequest(RunningComServer comServer, String... comPortNames) {
        super(null);
        this.comServer = comServer;
        this.validateComPortNames(Arrays.asList(comPortNames));
    }

    @Override
    public Set<Long> getBusinessObjectIds () {
        Set<Long> ids = super.getBusinessObjectIds();
        if (super.getBusinessObjectIds()== null){
            ids = this.comPorts.stream().map(ComPort::getId).distinct().collect(Collectors.toSet());
        }
        return ids;
    }

    private void validateComPortIds () {
        this.comPorts = this.getBusinessObjectIds()
                .stream()
                .map(this::findComPort)
                .collect(Collectors.toList());
    }

    private void validateComPortNames(List<String> comPortNames){
        this.comPorts = comPortNames
                .stream()
                .filter(((Predicate<? super String>) Strings::isNullOrEmpty).negate())
                .map(this::findComPort)
                .collect(Collectors.toList());
    }

    private ComPort findComPort (String comPortName) {
        return this.comServer.getComServer()
                .getComPorts()
                .stream()
                .filter(each -> each.getName().equals(comPortName))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("ComPort with name " + comPortName + " not found"));
    }

    private ComPort findComPort (long comPortId) {
        return this.comServer.getComServer()
                .getComPorts()
                .stream()
                .filter(each -> each.getId() == comPortId)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("ComPort with id " + comPortId + " not found"));
    }

    @Override
    public void applyTo (EventPublisher eventPublisher) {
        eventPublisher.narrowInterestToComPorts(null, this.comPorts);
    }

}