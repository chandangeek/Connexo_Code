/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.*;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.util.Date;

/**
 * Provides an implementation for the {@link G3Neighbor} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-12-15 (14:38)
 */
public class G3NeighborImpl extends PLCNeighborImpl implements G3Neighbor {

    private int txGain;
    private int txResolution;
    private int txCoefficient;
    private int linkQualityIndicator;
    private long timeToLive;
    private long toneMap;
    private long toneMapTimeToLive;
    private PhaseInfo phaseInfo;
    private G3NodeState g3NodeState;
    private String nodeAddress;
    private int shortAddress;
    private Date lastUpdate;
    private Date lastPathRequest;
    private long roundTrip;
    private int linkCost;

    @Inject
    public G3NeighborImpl(DataModel dataModel, Clock clock) {
        super(dataModel, clock);
    }

    G3NeighborImpl createFor(Device device, Device neighbor, ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo, G3NodeState g3NodeState) {
        this.init(device, neighbor, modulationScheme, modulation);
        this.phaseInfo = phaseInfo;
        this.g3NodeState = g3NodeState;
        return this;
    }

    void setPhaseInfo(PhaseInfo phaseInfo) {
        this.phaseInfo = phaseInfo;
    }

    void setG3NodeState(G3NodeState g3NodeState) {
        this.g3NodeState = g3NodeState;
    }

    @Override
    public int getTxGain() {
        return txGain;
    }

    void setTxGain(int txGain) {
        this.txGain = txGain;
    }

    @Override
    public int getTxResolution() {
        return txResolution;
    }

    void setTxResolution(int txResolution) {
        this.txResolution = txResolution;
    }

    @Override
    public int getTxCoefficient() {
        return txCoefficient;
    }

    void setTxCoefficient(int txCoefficient) {
        this.txCoefficient = txCoefficient;
    }

    @Override
    public int getLinkQualityIndicator() {
        return linkQualityIndicator;
    }

    void setLinkQualityIndicator(int linkQualityIndicator) {
        this.linkQualityIndicator = linkQualityIndicator;
    }

    @Override
    public Duration getTimeToLive() {
        return Duration.ofSeconds(timeToLive);
    }

    void setTimeToLiveFromSeconds(long seconds) {
        this.timeToLive = seconds;
    }

    @Override
    public long getToneMap() {
        return toneMap;
    }

    void setToneMap(long toneMap) {
        this.toneMap = toneMap;
    }

    @Override
    public Duration getToneMapTimeToLive() {
        return Duration.ofSeconds(toneMapTimeToLive);
    }

    void setToneMapTimeToLiveFromSeconds(long seconds) {
        this.toneMapTimeToLive = seconds;
    }

    @Override
    public PhaseInfo getPhaseInfo() {
        return phaseInfo;
    }

    @Override
    public G3NodeState getG3NodeState() {
        return g3NodeState;
    }

    @Override
    public String getNodeAddress() {
        return nodeAddress;
    }

    void setNodeAddress(String nodeAddress) {
        this.nodeAddress = nodeAddress;
    }

    @Override
    public int getShortAddress() {
        return shortAddress;
    }

    void setShortAddress(int shortAddress) {
        this.shortAddress = shortAddress;
    }

    @Override
    public Date getLastUpdate() {
        return lastUpdate;
    }

    void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public Date getLastPathRequest() {
        return lastPathRequest;
    }

    void setLastPathRequest(Date lastPathRequest) {
        this.lastPathRequest = lastPathRequest;
    }

    @Override
    public long getRoundTrip() {
        return roundTrip;
    }

    void setRoundTrip(long roundTrip) {
        this.roundTrip = roundTrip;
    }

    @Override
    public int getLinkCost() {
        return linkCost;
    }

    void setLinkCost(int linkCost) {
        this.linkCost = linkCost;
    }

}