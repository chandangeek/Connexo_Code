package com.energyict.mdc.upl.tasks;

/**
 * Defines what should be done with the received Topology.
 *
 * @author gna
 * @see #UPDATE
 * @see #VERIFY
 * @since 19/04/12 - 14:32
 */
public enum TopologyAction {

    /**
     * read the Topology from the device and update with the state in the DB
     */
    UPDATE(1) {
        @Override
        public String toString () {
            return "topologyAction.update";
        }
    },
    /**
     * read the Topology from the device and create a proper BusinessEvent if the DB Topology is different.
     */
    VERIFY(2) {
        @Override
        public String toString () {
            return "topologyAction.verify";
        }
    };

    private final int action;

    TopologyAction(final int action) {
        this.action = action;
    }

    public int getAction() {
        return action;
    }

    public static TopologyAction valueFromDb(final int action) {
        for (TopologyAction topologyAction : values()) {
            if (topologyAction.getAction() == action) {
                return topologyAction;
            }
        }
        throw new IllegalArgumentException("Unknown topology action: " + action);
    }

}