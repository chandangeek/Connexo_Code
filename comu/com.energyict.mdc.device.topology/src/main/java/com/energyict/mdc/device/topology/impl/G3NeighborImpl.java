/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.topology.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.topology.G3Neighbor;
import com.energyict.mdc.device.topology.G3NodeState;
import com.energyict.mdc.device.topology.Modulation;
import com.energyict.mdc.device.topology.ModulationScheme;
import com.energyict.mdc.device.topology.PhaseInfo;

import javax.inject.Inject;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

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
    private G3NodeState state;
    private long macPANId;
    private String nodeAddress;
    private int shortAddress;
    private Instant lastUpdate;
    private Instant lastPathRequest;
    private long roundTrip;
    private int linkCost;

    @Inject
    public G3NeighborImpl(DataModel dataModel, Clock clock) {
        super(dataModel, clock);
    }

    G3NeighborImpl createFor(Device device, Device neighbor, ModulationScheme modulationScheme, Modulation modulation, PhaseInfo phaseInfo, G3NodeState g3NodeState) {
        this.init(device, neighbor, modulationScheme, modulation);
        this.phaseInfo = phaseInfo;
        this.state = g3NodeState;
        return this;
    }

    G3NeighborImpl reverseClone(G3Neighbor original) {
        this.init(original.getNeighbor(), original.getDevice(), original.getModulationScheme(), original.getModulation());
        setAll(original);

        return this;
    }

    private void setAll(G3Neighbor original) {
        this.phaseInfo = original.getPhaseInfo();
        this.state = original.getState();
        this.lastPathRequest = original.getLastPathRequest();
        this.lastUpdate = original.getLastUpdate();
        this.linkCost = original.getLinkCost();
        this.linkQualityIndicator = original.getLinkQualityIndicator();
        this.macPANId = original.getMacPANId();
        this.phaseInfo = original.getPhaseInfo();
        this.nodeAddress = original.getNodeAddress();
        this.roundTrip = original.getRoundTrip();
        this.timeToLive = original.getTimeToLive().getSeconds();
        this.toneMap = original.getToneMap();
        this.txGain = original.getTxGain();
        this.txResolution = original.getTxResolution();
        this.txCoefficient = original.getTxCoefficient();
        this.toneMapTimeToLive = original.getToneMapTimeToLive().getSeconds();
        this.shortAddress = original.getShortAddress();
    }

    void setPhaseInfo(PhaseInfo phaseInfo) {
        this.phaseInfo = phaseInfo;
    }

    void setState(G3NodeState state) {
        this.state = state;
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
    public G3NodeState getState() {
        return state;
    }

    @Override
    public long getMacPANId() {
        return macPANId;
    }

    void setMacPANId(long macPANId) {
        this.macPANId = macPANId;
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
    public Instant getLastUpdate() {
        return lastUpdate;
    }

    void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    @Override
    public Instant getLastPathRequest() {
        return lastPathRequest;
    }

    void setLastPathRequest(Instant lastPathRequest) {
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

    /**
     * Updates with the most relevant information
     *
     * @param g3Neighbor
     * @return
     */
    @Override
    public void merge(G3Neighbor g3Neighbor) {

        if (g3Neighbor.getNodeAddress()!=null && !g3Neighbor.getNodeAddress().isEmpty()) {
            this.setNodeAddress(g3Neighbor.getNodeAddress());
        }

        if (g3Neighbor.getShortAddress()>0) {
            this.setShortAddress(g3Neighbor.getShortAddress());
        }

        if (g3Neighbor.getState() != null && !g3Neighbor.getState().equals(G3NodeState.UNKNOWN)) {
            this.setState(g3Neighbor.getState());
        }

        if (g3Neighbor.getModulation() != null && !g3Neighbor.getModulation().equals(Modulation.UNKNOWN)) {
            this.setModulation(g3Neighbor.getModulation());
        }

        if (g3Neighbor.getPhaseInfo() != null && !g3Neighbor.getPhaseInfo().equals(PhaseInfo.UNKNOWN)) {
            this.setPhaseInfo(g3Neighbor.getPhaseInfo());
        }

        if (g3Neighbor.getLinkQualityIndicator()>0) {
            this.setLinkQualityIndicator(g3Neighbor.getLinkQualityIndicator());
        }

        if (g3Neighbor.getLinkCost() > 0) {
            this.setLinkCost(g3Neighbor.getLinkCost());
        }

        if (this.getLastPathRequest()==null) {
            this.setLastPathRequest(g3Neighbor.getLastPathRequest());
        } else {
            if (g3Neighbor.getLastPathRequest() != null
                    && g3Neighbor.getLastPathRequest().isAfter(this.getLastPathRequest())) {
                this.setLastPathRequest(g3Neighbor.getLastPathRequest());
            }
        }

        if (this.getLastUpdate()==null) {
            this.setLastUpdate(g3Neighbor.getLastUpdate());
        } else {
            if (g3Neighbor.getLastUpdate() != null
                    && g3Neighbor.getLastUpdate().isAfter(this.getLastUpdate())) {
                this.setLastUpdate(g3Neighbor.getLastUpdate());
            }
        }

        if (g3Neighbor.getToneMap() > 0) {
            this.setToneMap(g3Neighbor.getToneMap());
        }

        if (g3Neighbor.getTxGain()>0) {
            this.setTxGain(g3Neighbor.getTxGain());
        }

        if (g3Neighbor.getTxResolution()>0) {
            this.setTxResolution(g3Neighbor.getTxResolution());
        }

        if (g3Neighbor.getTxCoefficient()>0) {
            this.setTxCoefficient(g3Neighbor.getTxCoefficient());
        }

        if (g3Neighbor.getToneMapTimeToLive()!=null && !g3Neighbor.getToneMapTimeToLive().isZero()) {
            this.setToneMapTimeToLiveFromSeconds(g3Neighbor.getToneMapTimeToLive().getSeconds());
        }

        if (g3Neighbor.getMacPANId()>0) {
            this.setMacPANId(g3Neighbor.getMacPANId());
        }

    }

}