package com.energyict.mdc.device.topology.impl;

import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.topology.*;

import java.util.*;
import java.util.logging.*;
import java.util.stream.*;

public class G3TopologyImpl implements G3Topology {

    HashMap<String, Optional<G3Neighbor>> topology = new HashMap<>();

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private Device gateway;

    public G3TopologyImpl(Device gateway) {
        setGateway(gateway);
    }


    public void setGateway(Device gateway) {
        logger.info("G3-Topology initialized for gateway "+gateway.getName()+" ("+gateway.getSerialNumber()+")");
        this.gateway = gateway;
    }

    @Override
    public void addG3NeighborLink(G3Neighbor g3Neighbor) {
        log("Adding G3 link", g3Neighbor);

        Optional<G3Neighbor> existingNeighbor = Optional.empty();
        String linkHash = getLinkHash(g3Neighbor);

        if (topology.containsKey(linkHash)) {
            existingNeighbor = topology.get(linkHash);
        }

        if (existingNeighbor.isPresent()){
            logger.info("- hash already exists "+linkHash+" merging data");

            G3Neighbor mergedNeighbor = existingNeighbor.get();
            mergedNeighbor.merge(g3Neighbor);
            topology.put(linkHash, Optional.of(mergedNeighbor));
        } else {
            logger.info("- hash added "+linkHash);
            topology.put(linkHash, Optional.of(g3Neighbor));
        }

    }

    @Override
    public List<G3Neighbor> getReferences() {
        logger.info("Preliminary reference list: ");
        topology.values().forEach(g3n -> log("\t - ", g3n.orElse(null)));

        Collection<G3Neighbor> uniqueReferences = removeDuplicates();

        logger.info("Final reference list: ");
        uniqueReferences.forEach(g3n -> log("\t - ", g3n));
        return new ArrayList<>(uniqueReferences);
    }

    /**
     * Check if there are duplicate links, e.g. links originating from the same slave but with 2 parents
     * This can happen because of 3 x sources of data (physical links, paths, G3-links)
     * For each duplicate a wild guess is performed to keep the best information collected
     */
    private Collection<G3Neighbor> removeDuplicates() {
        HashMap<String, G3Neighbor> uniqueLinks =new HashMap<>();

        topology.values().forEach(g3Neighbor -> {
            g3Neighbor.ifPresent(neighbor -> { // skip empty entries
                // we have a good value already for this starting point
                if (! uniqueLinks.containsKey(g3Neighbor.get().getDevice().getmRID())) {
                    G3Neighbor bestValue = g3Neighbor.get();

                    // update best running value
                    topology.values().stream()
                            .map(e -> e.orElse(null))                     // remove empty values
                            .filter(Objects::nonNull)
                            .filter(g3n -> sameOriginator(g3n, neighbor))       // find duplicates (links with the same slave)
                            .forEach(bestValue::merge);

                    uniqueLinks.put(neighbor.getDevice().getmRID(), bestValue);
                }
            });
        });

        return uniqueLinks.values();
    }


    /**
     * Checks if two G3 links have the same originator (slave device)
     */
    private boolean sameOriginator(G3Neighbor n1, G3Neighbor n2) {
        if (n1==null || n2==null) {
            return false;
        }

        if (n1.getDevice() == null || n2.getDevice() == null ){
            return false;
        }

        return n1.getDevice().getId() == n2.getDevice().getId();
    }



    /**
     * For cases when for a node we have more links, will try to detect which option needs to be presented
     *      - if one has parent the gateway and the other has another hop, will keep the hop (gateway is default)
     *      - if one has better values collected (topology data), use that
     */    private boolean betterInformation(G3Neighbor original, G3Neighbor duplicate) {
        if (isGateway(duplicate.getNeighbor()) && !isGateway(original.getNeighbor())){
            return false; // the original is a hop, while the other is a default link to the gateway
        }

        if (isGateway(original.getNeighbor()) && !isGateway(duplicate.getNeighbor())){
            return true; // the duplicate seems to be a hop
        }

        if (duplicate.getLinkQualityIndicator() > original.getLinkQualityIndicator()){
            return true; // the duplicate has better information
        }

        return false; // keep the original
    }


    /**
     * Detect if a some device is the gateway itself
     */
    private boolean isGateway(Device someDevice) {
        if (someDevice==null){
            return false;
        }
        return someDevice.getId()==gateway.getId();
    }



    private String getLinkHash(G3Neighbor g3Neighbor) {
        return getLinkHash(g3Neighbor.getDevice(), g3Neighbor.getNeighbor());
    }

    public String getLinkHash(Device slave, Device parent) {
        return slave.getmRID()+":"+parent.getmRID();
    }


    protected void log(String message, G3Neighbor g3n){
        if (g3n==null){
            logger.info(message+": null");
            return;
        }

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
