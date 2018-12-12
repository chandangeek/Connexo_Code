package com.energyict.mdc.upl.meterdata;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import java.util.Date;

/**
 * Straightforward ValueObject for a Topology neighbour
 * <p>
 *
 * Date: 1/5/15
 * Time: 9:49 AM
 */
public class TopologyNeighbour {

    private final DeviceIdentifier neighbour;
    private final int modulationSchema;
    private final long toneMap;
    private final int modulation;
    private final int txGain;
    private final int txRes;
    private final int txCoeff;
    private final int lqi;
    private final int phaseDifferential;
    private final int tmrValidTime;
    private final int neighbourValidTime;
    private long macPANId;
    private String nodeAddress;
    private int shortAddress;
    private Date lastUpdate;
    private Date lastPathRequest;
    private int state;
    private long roundTrip;
    private int linkCost;

    public TopologyNeighbour(DeviceIdentifier neighbour, int modulationSchema, long toneMap, int modulation,
                             int txGain, int txRes, int txCoeff, int lqi, int phaseDifferential, int tmrValidTime,
                             int neighbourValidTime, long macPANId, String nodeAddress, int shortAddress, Date lastUpdate,
                             Date lastPathRequest, int state, long roundTrip, int linkCost) {
        this.neighbour = neighbour;
        this.modulationSchema = modulationSchema;
        this.toneMap = toneMap;
        this.modulation = modulation;
        this.txGain = txGain;
        this.txRes = txRes;
        this.txCoeff = txCoeff;
        this.lqi = lqi;
        this.phaseDifferential = phaseDifferential;
        this.tmrValidTime = tmrValidTime;
        this.neighbourValidTime = neighbourValidTime;
        this.macPANId = macPANId;
        this.nodeAddress = nodeAddress;
        this.shortAddress = shortAddress;
        this.lastUpdate = lastUpdate;
        this.lastPathRequest = lastPathRequest;
        this.state = state;
        this.roundTrip = roundTrip;
        this.linkCost = linkCost;
    }

    public DeviceIdentifier getNeighbour() {
        return neighbour;
    }

    public int getModulationSchema() {
        return modulationSchema;
    }

    public long getToneMap() {
        return toneMap;
    }

    public int getModulation() {
        return modulation;
    }

    public int getTxGain() {
        return txGain;
    }

    public int getTxRes() {
        return txRes;
    }

    public int getTxCoeff() {
        return txCoeff;
    }

    public int getLqi() {
        return lqi;
    }

    public int getPhaseDifferential() {
        return phaseDifferential;
    }

    public int getTmrValidTime() {
        return tmrValidTime;
    }

    public int getNeighbourValidTime() {
        return neighbourValidTime;
    }

    public long getMacPANId() {
        return macPANId;
    }

    public String getNodeAddress() {
        return nodeAddress;
    }

    public int getShortAddress() {
        return shortAddress;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public Date getLastPathRequest() {
        return lastPathRequest;
    }

    public int getState() {
        return state;
    }

    public long getRoundTrip() {
        return roundTrip;
    }

    public int getLinkCost() {
        return linkCost;
    }
}