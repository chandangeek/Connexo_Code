package com.energyict.mdc.device.topology;

import com.energyict.mdc.common.device.data.Device;
import com.energyict.mdc.device.topology.impl.*;

import java.util.*;
import java.util.logging.*;

/**
 * Builds a mesh-type G3 PLC topology by combining 3 data sources:
 *  1. path segments        => from path requests
 *  2. G3 neighbors tables  => from topology readout
 *  3. physical link        => from join notifications or other simple info
 */
public class G3TopologyBuilder {

    private final TopologyService topologyService;
    private final G3TopologyImpl g3Topology;
    private final Set<Device> slaves;

    private Set<Device> physicalLinks;

    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private Device gateway;

    public G3TopologyBuilder(TopologyService topologyService, Device gateway) {
        this.topologyService = topologyService;
        this.gateway = gateway;

        this.g3Topology = new G3TopologyImpl();
        this.slaves = new HashSet<>();
        this.physicalLinks = new HashSet<>();
    }


    public G3Topology build() {
        logger.info("Building G3-Topology for gateway "+gateway.getName()+" ("+gateway.getSerialNumber()+")");

        findPhysicallyConnectedDevices();

        processPathSegments();
        processG3Neighbors();
        processPhysicalLinks();

        return g3Topology;
    }

    private void findPhysicallyConnectedDevices() {
        physicalLinks.addAll(topologyService.findPhysicalConnectedDevices(gateway));
        logger.info("Found "+physicalLinks.size()+" physically connected devices to "+gateway.getSerialNumber());
    }


    /**
     * Parse all known path segments (obtained via a path request on gateway)
     * The identified devices are added to the slaves list
     */
    private void processPathSegments() {
        logger.info("Adding path segments");

        topologyService.getUniqueG3CommunicationPathSegments(gateway)
                .forEach(this::processPathSegment);
    }

    private void processPathSegment(G3CommunicationPathSegment segment) {
        deviceFound(segment.getSource());
        deviceFound(segment.getTarget());

        if (segment.getNextHopDevice().isPresent()) {
            deviceFound(segment.getNextHopDevice().get());
        }

        log("Processing path segment", segment);

        G3Neighbor g3n = buildSegmentLink(segment);
        g3Topology.addG3NeighborLink(g3n);
    }

    protected void log(String message, G3CommunicationPathSegment segment){
        StringBuilder sb = new StringBuilder();

        sb.append(message);
        sb.append(" slave(target)=").append(segment.getTarget().getSerialNumber());
        sb.append(" parent(source)=").append(segment.getSource().getSerialNumber());
        if (segment.getNextHopDevice().isPresent()){
            sb.append(" nextHop=").append(segment.getNextHopDevice().get().getSerialNumber());
        }
        sb.append(" cost=").append(segment.getCost());
        sb.append(" ttl=").append(segment.getTimeToLive());

        logger.info(sb.toString());
    }

    /**
     * Process any known G3-neighbors links (obtained via G3-Neighbor-Table readout)
     */
    private void processG3Neighbors() {
        Set<G3Neighbor> g3Neighborhood = buildG3Neighborhood();

        g3Neighborhood.forEach(this::processG3neighbor);
    }

    /**
     * Add a G3 neighbor link to the topology.
     * First the link is reversed, since in DB is stored as master->slave, and we need slave->parent
     */
    private void processG3neighbor(G3Neighbor g3Neighbor) {
        deviceFound(g3Neighbor.getDevice());
        deviceFound(g3Neighbor.getNeighbor());

        G3Neighbor g3link = reverseLink(g3Neighbor);
        g3Topology.addG3NeighborLink(g3link);
    }

    /**
     * Checks if any of the physical links are reflected in the topology.
     * This is achieved by adding a dummy G3 link if the device was not process yet,
     * most probably was linked by a join request and there are no other details up to now.
     */
    private void processPhysicalLinks() {
        physicalLinks.forEach(this::processPhysicalLink);
    }

    private void processPhysicalLink(Device device) {
        if (!slaves.contains(device)){
            logger.info("Physically linked device not processed yet: "+device.getName()+" ("+device.getSerialNumber()+")");

            G3Neighbor g3n = buildPhysicalLink(device);
            g3Topology.addG3NeighborLink(g3n);
        }
    }

    private G3Neighbor buildPhysicalLink(Device device) {
        return topologyService.newG3Neighbor(device, gateway, null, null, null, G3NodeState.UNKNOWN);
    }

    private G3Neighbor buildSegmentLink(G3CommunicationPathSegment segment) {
        return topologyService.newG3Neighbor(segment.getTarget(), segment.getSource(), null, null, null, G3NodeState.AVAILABLE);
    }

    /**
     * Reverses a G3 link from database to be applicable for WebGUI.
     * In the database the links are stored master-->slave, but in the gui we show them as slave-->master(parent)
     *
     * @param g3n the G3 link neighbor
     * @return
     */
    private G3Neighbor reverseLink(G3Neighbor g3n) {
        logger.finest("Reversing original G3 link: "+g3n.getDevice().getSerialNumber()+" -> "+g3n.getNeighbor().getSerialNumber());
        return  topologyService.reverseCloneG3Neighbor(g3n);
    }


    private Set<G3Neighbor> buildG3Neighborhood() {
        Set<Device> processedDevices = new HashSet<>();
        Set<G3Neighbor> neighborhood = new HashSet<>();
        Set<Device> queue = new HashSet<>();

        queue.addAll(slaves);
        queue.addAll(physicalLinks);
        queue.add(gateway);

        logger.info("Building G3 links neighborhood for "+queue.size()+" devices");

        while (queue.size() > 0) {
            Set<Device> devicesToDelete = new HashSet<>();
            Set<Device> devicesToAdd = new HashSet<>();

            for (Device device : queue) {
                devicesToDelete.add(device);
                processedDevices.add(device);

                List<G3Neighbor> neighbors = topologyService.findG3Neighbors(device);
                logger.info("\tdevice " + device.getSerialNumber()+" has " + neighbors.size() + " G3 links.");

                for (G3Neighbor g3Link : neighbors) {
                    logger.info("\t\tadding link " + g3Link.getDevice().getSerialNumber() + " -> " + g3Link.getNeighbor().getSerialNumber() + " to list.");
                    neighborhood.add(g3Link);

                    //next iteration will search further links originating from this device if not already processed
                    if (!processedDevices.contains(g3Link.getNeighbor())) {
                        devicesToAdd.add(g3Link.getNeighbor());
                    } else{
                        logger.info("\t\tnode already processed: "+g3Link.getNeighbor().getSerialNumber() + "");
                    }
                }
            }
            queue.addAll(devicesToAdd);
            queue.removeAll(devicesToDelete);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Returning a neighborhood of ").append(neighborhood.size()).append(" G3 links");
        neighborhood.forEach(n -> {
            sb.append("\n");
            sb.append("device=").append(n.getDevice().getSerialNumber()).append("\t");
            sb.append("neighbor=").append(n.getNeighbor().getSerialNumber()).append("\t");
            sb.append("id=").append(n.getNeighbor().getId()).append("\t");
            sb.append("shortAddress=").append(n.getShortAddress()).append("\t");
            sb.append("phase=").append(n.getPhaseInfo()).append("\t");
            sb.append("lqi=").append(n.getLinkQualityIndicator()).append("\t");
            sb.append("panId=").append(n.getMacPANId());
        });
        logger.info(sb.toString());
        return neighborhood;
    }



    /**
     * Adds to the known devices set an identified slave
     * @param device an device found on various sources
     */
    private void deviceFound(Device device) {
        if (notGateway(device)){
            slaves.add(device);
        }
    }


    /**
     * Check if a device is actually the gateway
     */
    private boolean isGateway(Device device) {
        return device.getmRID().equals(gateway.getmRID());
    }


    /**
     * Check if a device is not the gateway
     */
    private boolean notGateway(Device device) {
        return !isGateway(device);
    }
}