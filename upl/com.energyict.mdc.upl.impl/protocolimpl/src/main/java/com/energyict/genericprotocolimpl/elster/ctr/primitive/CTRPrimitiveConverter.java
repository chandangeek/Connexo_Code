package com.energyict.genericprotocolimpl.elster.ctr.primitive;

import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.object.*;
import com.energyict.protocol.ProtocolUtils;

import java.math.BigDecimal;

/**
 * Created by IntelliJ IDEA.
 * User: khe
 * Date: 21-sep-2010
 * Time: 11:10:27
 */
public class CTRPrimitiveConverter {

    public CTRPrimitiveConverter() {}

    public byte[] convertId(CTRObjectID id) {
        int x = id.getX();
        int y = id.getY();
        int z = id.getZ();

        byte Byte1 = (byte) ((byte) x & 0xFF);
        byte Byte2 = (byte) ((((byte) y & 0xFF) << 4) & 0xF0);
        byte Byte3 = (byte) ((byte) z & 0xFF);

        return new byte[]{Byte1, (byte) (Byte2+Byte3)};  //To change body of created methods use File | Settings | File Templates.
    }
}