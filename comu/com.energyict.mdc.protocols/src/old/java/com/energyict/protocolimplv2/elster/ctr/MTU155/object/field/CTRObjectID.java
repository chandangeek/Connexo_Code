package com.energyict.protocolimplv2.elster.ctr.MTU155.object.field;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.primitive.CTRPrimitiveParser;

/**
 * Copyrights EnergyICT
 * Date: 21-sep-2010
 * Time: 10:52:48
 */
public class CTRObjectID extends AbstractField<CTRObjectID> {

    private int x;
    private int y;
    private int z;
    public static final int LENGTH = 2;

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

    public int getLength() {
        return 2;
    }

    public byte[] getBytes() {
        int idValue = z & 0x0F;
        idValue += (y * 16) & 0x000F0;
        idValue += (x * 256) & 0x0FF00;
        return getBytesFromInt(idValue, getLength());
    }

    public CTRObjectID parse(byte[] rawData, int offset) throws CTRParsingException {
        CTRPrimitiveParser parser = new CTRPrimitiveParser();
        CTRObjectID id = parser.parseId(rawData, offset);
        this.x = id.getX();
        this.y = id.getY();
        this.z = id.getZ();
        return this;
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

    @Override
    public String toString() {
        String toStringValue = Integer.toHexString(x) + "." + Integer.toHexString(y) + "." + Integer.toHexString(z);
        return toStringValue.toUpperCase();
    }

    /**
     *
     * @param idAsString
     * @return
     */
    public boolean is(String idAsString) {
        return idAsString.equalsIgnoreCase(toString());
    }
    
}
