package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.topology.*;

import java.util.*;
import java.util.logging.*;
import java.util.stream.*;

public class G3TopologyImpl implements G3Topology {

    HashMap<String, Optional<G3Neighbor>> topology = new HashMap<>();

    private final Logger logger = Logger.getLogger(this.getClass().getName());

    public void setGateway(Device gateway) {
        logger.info("G3-Topology initialized for gateway "+gateway.getName()+" ("+gateway.getSerialNumber()+")");
    }

    @Override
    public void addG3NeighborLink(G3Neighbor g3Neighbor) {
        log("Adding G3 link", g3Neighbor);
        topology.put(getLinkHash(g3Neighbor), Optional.of(g3Neighbor));
    }

    @Override
    public List<G3Neighbor> getReferences() {
        logger.info("Final reference list: ");
        topology.values().forEach(g3n -> {log("\t - ", g3n.get());});

        return topology.values().stream()
                .map(e -> e.orElse(null))
                .filter(e -> e!=null)
                .collect(Collectors.toList());
    }

    private String getLinkHash(G3Neighbor g3Neighbor) {
        return getLinkHash(g3Neighbor.getDevice(), g3Neighbor.getNeighbor());
    }

    public String getLinkHash(Device slave, Device parent) {
        return slave.getmRID()+":"+parent.getmRID();
    }


    protected void log(String message, G3Neighbor g3n){
        StringBuilder sb = new StringBuilder();

        sb.append(message);
        sb.append(" slave(device)=").append(g3n.getDevice().getSerialNumber());
        sb.append(" parent(neighbor)=").append(g3n.getNeighbor().getSerialNumber());
        sb.append(" LQI=").append(g3n.getLinkQualityIndicator());
        sb.append(" modulation=").append(g3n.getModulation());
        sb.append(" phase=").append(g3n.getPhaseInfo());

        logger.info(sb.toString());
    }

}
