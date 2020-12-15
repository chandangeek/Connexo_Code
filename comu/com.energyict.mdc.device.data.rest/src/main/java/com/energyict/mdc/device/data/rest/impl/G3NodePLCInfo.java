/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.device.topology.G3Neighbor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class G3NodePLCInfo {
    public static final String N_A = "n/a";
    public String nodeAddress;
    public long shortAddress;
    public Instant lastUpdate;
    public Instant lastPathRequest;
    public String state;
    public String modulationScheme;
    public String modulation;
    public int linkQualityIndicator;
    public String phaseInfo;
    public long roundTrip;
    public long linkCost;
    public long macPANId;
    public long txGain;
    public long txResolution;
    public long txCoefficient;
    public long toneMap;
    public String parentName;
    public String parentSerialNumber;

    public static G3NodePLCInfo from(G3Neighbor g3Neighbor, Thesaurus thesaurus) {
        G3NodePLCInfo info = new G3NodePLCInfo();
        info.nodeAddress = g3Neighbor.getNodeAddress();
        info.shortAddress = g3Neighbor.getShortAddress();
        info.lastUpdate = g3Neighbor.getLastUpdate();
        info.lastPathRequest = g3Neighbor.getLastPathRequest();
        info.state = g3Neighbor.getState()!=null? thesaurus.getFormat(g3Neighbor.getState()).format() : N_A ;
        info.modulationScheme =  g3Neighbor.getModulationScheme()!=null ? thesaurus.getFormat(g3Neighbor.getModulationScheme()).format() : N_A;
        info.modulation = g3Neighbor.getModulation() !=null ? g3Neighbor.getModulation().toString() : N_A;
        info.linkQualityIndicator = g3Neighbor.getLinkQualityIndicator();
        info.phaseInfo = g3Neighbor.getPhaseInfo() !=null ? g3Neighbor.getPhaseInfo().toString() : N_A;
        info.roundTrip = g3Neighbor.getRoundTrip();
        info.linkCost = g3Neighbor.getLinkCost();
        info.macPANId = g3Neighbor.getMacPANId();
        info.txGain = g3Neighbor.getTxGain();
        info.txResolution = g3Neighbor.getTxResolution();
        info.txCoefficient = g3Neighbor.getTxCoefficient();
        info.toneMap = g3Neighbor.getToneMap();
        if (g3Neighbor.getNeighbor()!=null) {
            info.parentName = g3Neighbor.getNeighbor().getName();
            info.parentSerialNumber = g3Neighbor.getNeighbor().getSerialNumber();
        } else {
            info.parentName = N_A;
            info.parentSerialNumber = N_A;
        }
        return info;
    }
}
