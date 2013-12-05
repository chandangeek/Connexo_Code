package com.energyict.protocolimpl.cynet;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Network class, contains basic info about a network.
 *
 * @author alex
 */
public final class Network {

	/** The subnets of this network. */
	private final Network[] subnets;

	/** The super network of this network. */
	private final Network supernet;

	/** The slave nodes for this network. */
	private final List<NetworkNode> slaveNodes = new ArrayList<NetworkNode>();

	/**
	 * The master node(s) for this network. There should only be one, but if the
	 * routing table has overlaps, you can have more. This would be a bug in the
	 * routing.
	 */
	private Set<NetworkNode> masterNodes = new HashSet<NetworkNode>();

	/** The span of the network. */
	private final int span;

	/**
	 * Create a new network using the given super net and span.
	 *
	 * @param superNet
	 *            The super net.
	 * @param span
	 *            The span.
	 */
	Network(final Network superNet, final int span) {
		this.supernet = superNet;
		this.span = span;

		switch (span) {
		case 1:
			this.subnets = new Network[2];
			break;
		case 2:
			this.subnets = new Network[4];
			break;
		case 3:
			this.subnets = new Network[8];
			break;
		default:
			throw new IllegalArgumentException(
					"Cannot have a network span of [" + span + "]");
		}
	}

	/**
	 * Returns the sub net at the given net number.
	 *
	 * @param netNumber
	 *            The net number.
	 *
	 * @return The particular subnet, null if no such subnet known.
	 */
	public final Network getSubnet(final int netNumber) {
		return this.subnets[netNumber];
	}

	/**
	 * Adds the given master in a subnet with the given index as net number.
	 *
	 * @param master
	 *            The master of the new network.
	 * @param index
	 *            The index of the network at the given level.
	 */
	final void addSubnet(final NetworkNode master, final int index) {
		this.subnets[index] = new Network(this, this.span);

		if (master != null) {
			this.subnets[index].setMasterNode(master);
		}
	}

	/**
	 * Returns the master node of the network.
	 *
	 * @return The master node of the network.
	 */
	public final Set<NetworkNode> getNetworkMasters() {
		return this.masterNodes;
	}

	/**
	 * Sets the master node ID.
	 *
	 * @param masterNodeId
	 *            The master node ID.
	 */
	final void setMasterNode(final NetworkNode masterNodeId) {
		this.masterNodes.add(masterNodeId);
	}

	/**
	 * Adds the given slave to the list of slave nodes of this network.
	 *
	 * @param slave
	 *            The slave to add.
	 */
	final void addSlaveNode(final NetworkNode slave) {
		this.slaveNodes.add(slave);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final String toString() {
		final StringBuilder builder = new StringBuilder();

		final StringBuilder indentBuilder = new StringBuilder();

		for (int i = 0; i < this.getDepth(); i++) {
			indentBuilder.append("   ");
		}

		final String indent = indentBuilder.toString();

		if (this.masterNodes.size() == 0) {
			builder.append(indent).append("No known network master\n");
		} else if (this.masterNodes.size() == 1) {
			builder.append(indent).append("Network Master [").append(
					this.masterNodes.iterator().next().getManufacturerID().toHexString())
					.append("]\n");
		} else {
			builder.append("Conflicting network masters [");

			for (final NetworkNode master : this.masterNodes) {
				builder.append(master.getManufacturerID().toString()).append(", ");
			}

			builder.deleteCharAt(builder.length() - 1);
			builder.deleteCharAt(builder.length() - 1);

			builder.append("]\n");
		}

		for (final NetworkNode slave : this.slaveNodes) {
			builder.append(indent).append("Slave [").append(slave.getManufacturerID().toHexString())
					.append("]\n");
		}

		builder.append(indent).append("Sub networks\n");

		for (int i = 0; i < this.subnets.length; i++) {
			final Network subnet = this.subnets[i];

			if (subnet != null) {
				builder.append("\r\n");
				builder.append(indent).append("Subnet index [").append(i).append("]\r\n");
				builder.append(subnet.toString());
				builder.append("\r\n");
			}
		}

		return builder.toString();
	}

	/**
	 * Returns the depth.
	 *
	 * @return The depth.
	 */
	final int getDepth() {
		if (this.supernet == null) {
			return 0;
		} else {
			return 1 + this.supernet.getDepth();
		}
	}

	/**
	 * Returns an array containing all the slave nodes that are present on this
	 * network.
	 *
	 * @return An array containing all the slave nodes that are present on this
	 *         network.
	 */
	public final NetworkNode[] getSlaveNodes() {
		return this.slaveNodes.toArray(new NetworkNode[this.slaveNodes
				.size()]);
	}

	/**
	 * Returns the span this network is configured with.
	 *
	 * @return The span this network is configured with.
	 */
	public final int getSpan() {
		return this.span;
	}

	/**
	 * Returns the number of submaster slots.
	 *
	 * @return	The number of submaster slots.
	 */
	public final int getNumberOfSubmasterSlots() {
		return 2 << (this.span - 1);
	}

	/**
	 * Returns all the reachable nodes from this network.
	 *
	 * @return All the reachable nodes from this network.
	 */
	public final NetworkNode[] getAllNodes() {
		final List<NetworkNode> nodes = new ArrayList<NetworkNode>();

		for (final NetworkNode slave : this.slaveNodes) {
			nodes.add(slave);
		}

		for (final NetworkNode master : this.masterNodes) {
			nodes.add(master);
		}

		for (int i = 0; i < this.subnets.length; i++) {
			final Network subnet = this.subnets[i];

			if (subnet != null) {
				final NetworkNode[] subnetNodes = subnet.getAllNodes();

				for (final NetworkNode subnetNode : subnetNodes) {
					nodes.add(subnetNode);
				}
			}
		}

		return nodes.toArray(new NetworkNode[nodes.size()]);
	}

	/**
	 * Returns true if this is the central master's network.
	 *
	 * @return True if this is the network of the central master, false if it is
	 *         not.
	 */
	public final boolean isCentralMasterNetwork() {
		return this.supernet == null;
	}

	/**
	 * Returns all the known subnets.
	 *
	 * @return All the known subnets.
	 */
	public final Network[] getSubnets() {
		return this.subnets;
	}

	/**
	 * Returns the supernet
	 *
	 * @return the supernet of this network
	 */
	public final Network getSupernet(){
		return this.supernet;
	}
}
