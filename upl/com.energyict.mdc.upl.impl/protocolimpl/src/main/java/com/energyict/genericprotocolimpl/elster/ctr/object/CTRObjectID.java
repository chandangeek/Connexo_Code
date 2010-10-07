package com.energyict.genericprotocolimpl.elster.ctr.object;

import com.energyict.genericprotocolimpl.elster.ctr.common.AbstractField;
import com.energyict.genericprotocolimpl.elster.ctr.common.CTRParsingException;
import com.energyict.genericprotocolimpl.elster.ctr.primitive.CTRPrimitiveParser;

/**
 * Copyrights EnergyICT
 * Date: 21-sep-2010
 * Time: 10:52:48
 */
public class CTRObjectID extends AbstractField<CTRObjectID> {

    public static final int LENGTH = 2;

    private int x;
    private int y;
    private int z;

    public CTRObjectID() {
        this(0, 0, 0);
    }


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
            this.x = Integer.valueOf(id[0], 16).intValue() & 0x0FF;
            this.y = Integer.valueOf(id[1], 16).intValue() & 0x00F;
            this.z = Integer.valueOf(id[2], 16).intValue() & 0x00F;
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

    public byte[] getBytes() {
        int idValue = z & 0x0F;
        idValue += (y * 16) & 0x000F0;
        idValue += (x * 256) & 0x0FF00;
        return getBytesFromInt(idValue, LENGTH);
    }

    public CTRObjectID parse(byte[] rawData, int offset) throws CTRParsingException {
        CTRPrimitiveParser parser = new CTRPrimitiveParser();
        CTRObjectID id = parser.parseId(rawData, offset);
        this.x = id.getX();
        this.y = id.getY();
        this.z = id.getZ();
        return this;
    }
}
