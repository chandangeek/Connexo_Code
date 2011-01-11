package com.energyict.protocolimpl.cynet;
/**
 * Represents a route.
 * 
 * @author alex
 */
public final class RouteAddress {

    /** Indicates whether the node on this route is a master. */
    private final boolean master;

    /** The level of this route. */
    private final int level;

    /**
     * The span used, this determines the depth of the hierarchy and also the
     * number of possible sub masters per level.
     */
    private final int span;

    /** The path. */
    private final int[] path;

    /**
     * Create a new route address based on a hex string and the span.
     * 
     * @param hexString
     *            The hex string.
     * @param span
     *            The span.
     */
    RouteAddress(final String hexString, final int span) {
        this(Long.parseLong(hexString, 16), span);
    }

    /**
     * Create a new routing address using the 4 octets.
     * 
     * @param octet1
     *            First octet.
     * @param octet2
     *            Second octet.
     * @param octet3
     *            Third octet.
     * @param octet4
     *            Last octet.
     * @param span
     *            The span.
     */
    RouteAddress(final int octet1, final int octet2, final int octet3, final int octet4, final int span) {
        this(((octet1 << 24) + (octet2 << 16) + (octet3 << 8) + octet4), span);
    }

    /**
     * Creates a new route based on the value of the route.
     * 
     * @param value
     *            The value.
     * @param span
     *            The span.
     */
    RouteAddress(final long value, final int span) {
        assert span >= 1 && span <= 3 : "Span must be 1, 2 or 3 !";
        
        // Create the path.
        this.path = new int[Double.valueOf(Math.floor(24 / span)).intValue()];
        this.master = ((int) (value >> 31)) == 0;
        
        this.level = (int) ((value >> 24) & 0x1F);
        this.span = span;

        final int pathNumber = (int) (value & 0x00FFFFFF);

        int start = 0;

        switch (span) {
        case 1:
            start = 1;
            break;
        case 2:
            start = 3;
            break;
        case 3:
            start = 7;
            break;
        default:
            throw new IllegalArgumentException("Span should be one of 1, 2 or 3, the value supplied [" + span
                    + "] is not supported...");

        }

        for (int i = 0; i < path.length; i++) {
            final int mask = (start << (i * span));
            final int pathElement = (pathNumber & mask) >> (i * span);

            this.path[path.length - (i + 1)] = pathElement;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        final StringBuilder builder = new StringBuilder();
        
        builder.append("Level [").append(this.level).append("]\n");
        builder.append("Master [").append(this.master ? "Yes" : "No").append("]\n");
        builder.append("Path [");

        for (int i = 0; i < path.length; i++) {
            builder.append(path[i]).append(", ");
        }

        builder.deleteCharAt(builder.length() - 1);
        builder.deleteCharAt(builder.length() - 1);

        builder.append("]\n");

        return builder.toString();
    }

    /**
     * Check if the address points to a master node.
     * 
     * @return True if the address points to a master node, false if not.
     */
    final boolean isMaster() {
        return this.master;
    }

    /**
     * Returns the level.
     * 
     * @return The level this node has.
     */
    final int getLevel() {
        return this.level;
    }

    /**
     * Returns the sequence number of the network at the given level.
     * 
     * @param level
     *            The level.
     * @return The sequence number of the network at the given level.
     */
    final int getNumberAtLevel(final int level) {
        return this.path[level - 1];
    }

    /**
     * Returns the span.
     * 
     * @return The span.
     */
    final int getSpan() {
        return this.span;
    }
}
