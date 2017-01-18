package com.energyict.mdc.upl.meterdata;

import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

/**
 * Straightforward ValueObject for a Topology neighbour
 * <p>
 * Copyrights EnergyICT
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

    public TopologyNeighbour(DeviceIdentifier neighbour, int modulationSchema, long toneMap, int modulation, int txGain, int txRes, int txCoeff, int lqi, int phaseDifferential, int tmrValidTime, int neighbourValidTime) {
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
}