package com.energyict.protocolimpl.cynet;

/**
 * This is the manufacturer ID. It consists of 4 octets, which are 32 bits.
 * 
 * @author alex
 * 
 */
public final class ManufacturerId {
	
	/** The central master manufacturer ID. */
	public static final ManufacturerId CENTRAL_MASTER = new ManufacturerId(0, 0, 0, 0);

    /** The first octet. */
    private final int octet1;

    /** The second octet. */
    private final int octet2;

    /** The third octet. */
    private final int octet3;

    /** The fourth octet. */
    private final int octet4;

    /**
     * Create a new instance. Please note that this class is immutable.
     * 
     * @param octet1
     *            The first octet (0 - 255)
     * @param octet2
     *            The second octet (0 - 255)
     * @param octet3
     *            The third octet (0 - 255)
     * @param octet4
     *            The fourth octet (0 - 255)
     */
    public ManufacturerId(final int octet1, final int octet2, final int octet3,
            final int octet4) {
        assert octet1 >= 0 && octet1 <= 255 : "Octet 1 value is restricted between 0 and 255";
        assert octet2 >= 0 && octet2 <= 255 : "Octet 2 value is restricted between 0 and 255";
        assert octet3 >= 0 && octet3 <= 255 : "Octet 3 value is restricted between 0 and 255";
        assert octet4 >= 0 && octet4 <= 255 : "Octet 4 value is restricted between 0 and 255";

        this.octet1 = octet1;
        this.octet2 = octet2;
        this.octet3 = octet3;
        this.octet4 = octet4;
    }

    /**
     * Create a new manufacturer ID based on the given hex string.
     * 
     * @param hexString
     *            The hex string to create a manufacturer ID for.
     */
    public ManufacturerId(final String hexString) {
        final String[] parts = hexString.split("\\.");
        
        if (parts.length != 4) {
        	throw new IllegalArgumentException("Cannot parse manufacturer ID [" + hexString + "], needs to be a dotted 4 number address in hex !");
        }
        
        this.octet1 = Integer.parseInt(parts[0], 16);
        this.octet2 = Integer.parseInt(parts[1], 16);
        this.octet3 = Integer.parseInt(parts[2], 16);
        this.octet4 = Integer.parseInt(parts[3], 16);
    }

    /**
     * Creates a new address by splitting up the given long in the 4 octets
     * required.
     * 
     * @param longAddress
     *            The long to split up.
     */
    public ManufacturerId(final long longAddress) {
        this.octet1 = (int) ((longAddress >> 24) & 0xFF);
        this.octet2 = (int) ((longAddress >> 16) & 0xFF);
        this.octet3 = (int) ((longAddress >> 8) & 0xFF);
        this.octet4 = (int) (longAddress & 0xFF);
    }

    /**
     * Returns the first octet of the manufacturer ID.
     * 
     * @return The first octet of the manufacturer ID.
     */
    public final int getOctet1() {
        return this.octet1;
    }

    /**
     * Returns the second octet of the manufacturer ID.
     * 
     * @return The second octet of the manufacturer ID.
     */
    public final int getOctet2() {
        return this.octet2;
    }

    /**
     * Returns the third octet of the manufacturer ID.
     * 
     * @return The third octet of the manufacturer ID.
     */
    public final int getOctet3() {
        return this.octet3;
    }

    /**
     * Returns the fourth octet of the manufacturer ID.
     * 
     * @return The fourth octet of the manufacturer ID.
     */
    public final int getOctet4() {
        return this.octet4;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(this.octet1);
        stringBuilder.append(".");
        stringBuilder.append(this.octet2);
        stringBuilder.append(".");
        stringBuilder.append(this.octet3);
        stringBuilder.append(".");
        stringBuilder.append(this.octet4);

        return stringBuilder.toString();
    }

    /**
     * Same as toString, but prints the octets in hex.
     * 
     * @return Same as toString, but in hex octets.
     */
    public final String toHexString() {
        final StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Integer.toHexString(this.octet1));
        stringBuilder.append(".");
        stringBuilder.append(Integer.toHexString(this.octet2));
        stringBuilder.append(".");
        stringBuilder.append(Integer.toHexString(this.octet3));
        stringBuilder.append(".");
        stringBuilder.append(Integer.toHexString(this.octet4));

        return stringBuilder.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + octet1;
        result = prime * result + octet2;
        result = prime * result + octet3;
        result = prime * result + octet4;
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean equals(final Object obj) {
        if (this == obj) {
			return true;
		}
        if (obj == null) {
			return false;
		}
        if (getClass() != obj.getClass()) {
			return false;
		}
        final ManufacturerId other = (ManufacturerId) obj;
        if (octet1 != other.octet1) {
			return false;
		}
        if (octet2 != other.octet2) {
			return false;
		}
        if (octet3 != other.octet3) {
			return false;
		}
        if (octet4 != other.octet4) {
			return false;
		}
        return true;
    }
}
