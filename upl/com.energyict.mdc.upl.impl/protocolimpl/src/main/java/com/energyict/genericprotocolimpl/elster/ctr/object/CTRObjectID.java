package com.energyict.genericprotocolimpl.elster.ctr.object;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 10:52:48
 * To change this template use File | Settings | File Templates.
 */
public class CTRObjectID {
    private int x;
    private int y;
    private int z;

    public CTRObjectID(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public String toString() {
        return Integer.toString(x) + "." + Integer.toString(y) + "." + Integer.toString(z);
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
