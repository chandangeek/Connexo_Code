package com.energyict.protocolimpl.cynet;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents the network topology as seen from the central master.
 * 
 * @author alex
 */
public final class NetworkTopology {
	
	/** Logger instance used in this class. */
	private static final Logger logger = Logger.getLogger(NetworkTopology.class.getName());

    /** The root of the network. */
    private final Network root;

    /**
     * Create a new instance using the given manufacturer ID as master
     * identifier.
     * 
     * @param master
     *            The identifier of the master.
     */
    NetworkTopology(final ManufacturerId master) {
        this.root = new Network(null, 3);
        this.root.setMasterNode(new NetworkNode(master, this.root));
    }
    
    /**
     * Creates a new topology based on the routing table. This is the routing table as it is returned by the module.
     * 
     * @param 		routingTable			The routing table.
     * @param		centralMaster			The central master node of the network.
     * @param		signalStrengthListener	This is only applicable when we are running on the master. Used to measure signal strengths. Can be null
     * 										when the routing table is not requested on the master itself.
     * 
     * @return		A parsed network topology.
     */
    public static final NetworkTopology parse(final ManufacturerId centralMaster, final String routingTable, final SignalStrengthListener signalStrengthListener) {
    	final NetworkTopology topology = new NetworkTopology(centralMaster);
    	
        final String[] networkMembers = routingTable.split("\n");

        for (String networkMember : networkMembers) {
            networkMember = networkMember.trim();

            if (!networkMember.equals("OK")) {
                final String[] memberData = networkMember.split(",");

                if (memberData.length >= 2) {
                    final String manufacturerIdString = memberData[0].trim();
                    final String routingString = memberData[1].trim();

                    if (logger.isLoggable(Level.INFO)) {
	                    logger.info("Adding node with routing string ["
	                            + routingString + "] and manufacturer ID ["
	                            + manufacturerIdString + "]");
                    }

                    final ManufacturerId nodeId = new ManufacturerId(Long.parseLong(manufacturerIdString, 16));
                    SignalStrength signalStrength = SignalStrength.UNKNOWN;
                    if ((memberData.length >= 3) && (memberData[2] != null)) {
                        try {
                            Integer rssi = Integer.valueOf(memberData[2].trim());
                            signalStrength = SignalStrength.byRSSIValue(rssi);
                        } catch (NumberFormatException e) {
                            logger.warning("Cannot parse rssi [" + memberData[2] + "]: " + e.getMessage());
                        }
                    }

                    topology.addNode(new RouteAddress(Long.parseLong(routingString, 16), 3), nodeId, (signalStrengthListener != null ? signalStrengthListener.getSignalStrength(nodeId) : signalStrength));
                } else {
                    logger.warning("Cannot parse [" + networkMember
                            + "] into network data, splitting records using comma delimiter yields [" + memberData.length + "] parts, contents of array [" + memberData + "]");
                }
            }
        }

        return topology;
    }

    /**
     * Adds the given node to the network.
     * 
     * @param route
     *            The route to the node (physical address).
     * @param nodeId
     *            The node ID. (logical address).
     * @param	signalStrength	Ths signal strength, if -1 it is ignored.
     */
    final void addNode(final RouteAddress route, final ManufacturerId nodeId, final SignalStrength signalStrength) {
    	
        if (route.isMaster()) {
            if (route.getLevel() != 0) {
                Network current = root;

                for (int i = 1; i <= route.getLevel(); i++) {
                    if (current.getSubnet(route.getNumberAtLevel(i)) == null) {
                        // Add the subnet...
                        current.addSubnet(null, route.getNumberAtLevel(i));
                    }

                    current = current.getSubnet(route.getNumberAtLevel(i));
                }
            	
                final NetworkNode node = new NetworkNode(nodeId, current);
            	node.setSignalStrength(signalStrength);
            	
                current.setMasterNode(node);
            }
        } else {
            if (route.getLevel() == 0) {
            	final NetworkNode node = new NetworkNode(nodeId, this.root);
            	node.setSignalStrength(signalStrength);
            	
                root.addSlaveNode(node);
            } else {
                Network current = root;

                for (int i = 1; i <= route.getLevel(); i++) {
                    if (current.getSubnet(route.getNumberAtLevel(i)) == null) {
                        // Add the subnet...
                        current.addSubnet(null, route.getNumberAtLevel(i));
                    }

                    current = current.getSubnet(route.getNumberAtLevel(i));
                }
                
            	final NetworkNode node = new NetworkNode(nodeId, current);
            	node.setSignalStrength(signalStrength);
                current.addSlaveNode(node);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        return this.root.toString();
    }

    /**
     * Returns the root network of the topology.
     * 
     * @return The root network of the topology.
     */
    public final Network getRoot() {
        return this.root;
    }
}
