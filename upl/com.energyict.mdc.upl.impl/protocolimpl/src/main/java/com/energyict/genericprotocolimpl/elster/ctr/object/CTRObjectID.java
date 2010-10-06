package com.energyict.genericprotocolimpl.elster.ctr.object;

/**
 * Copyrights EnergyICT
 * Date: 21-sep-2010
 * Time: 10:52:48
 */
public class CTRObjectID {

    private final int x;
    private final int y;
    private final int z;

    public CTRObjectID(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public CTRObjectID(String objectId) {
        String[] id = objectId.split("\\.");
        if (id.length != 3) {
            throw new IllegalArgumentException("Invalid objectId: [" + objectId + "]. ObjectId should be formatted as: 'x.y.z'");
        }
        try {
            this.x = Integer.valueOf(id[0], 16).intValue();
            this.y = Integer.valueOf(id[1], 16).intValue();
            this.z = Integer.valueOf(id[2], 16).intValue();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid objectId: [" + objectId + "]. Format should be 'X.Y.Z', where X, Y and Z are 0-9 or A-F");
        }
    }

    @Override
    public String toString() {
        String toStringValue = Integer.toHexString(x) + "." + Integer.toHexString(y) + "." + Integer.toHexString(z);
        return toStringValue.toUpperCase();
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }


}
