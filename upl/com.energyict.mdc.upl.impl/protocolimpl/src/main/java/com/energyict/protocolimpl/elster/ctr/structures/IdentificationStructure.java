package com.energyict.protocolimpl.elster.ctr.structures;

import com.energyict.protocolimpl.elster.ctr.structures.fields.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 12-aug-2010
 * Time: 10:55:59
 */
public class IdentificationStructure implements Structure {

    public static final int LENGTH = 128;

    private final PDRValue pdrValue;
    private final AllPAValue allPAValue;
    private final NCGValue ncgValue;
    private final EMSizeValue emSizeValue;
    private final PucS pucS;
    private final VisValue visValue;
    private final AnContValue anContValue;
    private final NemValue nemValue;
    private final NeaValue neaValue;
    private final NetValue netValue;
    private final SDValue sdValue;

    public IdentificationStructure(byte[] rawPacket, int structureOffset) {
        int offset = structureOffset;

        pdrValue = new PDRValue(rawPacket, offset);
        offset += PDRValue.LENGTH;

        allPAValue = new AllPAValue(rawPacket, offset);
        offset += AllPAValue.LENGTH;

        ncgValue = new NCGValue(rawPacket, offset);
        offset += NCGValue.LENGTH;

        emSizeValue = new EMSizeValue(rawPacket, offset);
        offset += EMSizeValue.LENGTH;

        pucS = new PucS(rawPacket, offset);
        offset += PucS.LENGTH;

        visValue = new VisValue(rawPacket, offset);
        offset += VisValue.LENGTH;

        anContValue = new AnContValue(rawPacket, offset);
        offset += AnContValue.LENGTH;

        nemValue = new NemValue(rawPacket, offset);
        offset += NemValue.LENGTH;

        neaValue = new NeaValue(rawPacket, offset);
        offset += NeaValue.LENGTH;

        netValue = new NetValue(rawPacket, offset);
        offset += NetValue.LENGTH;

        sdValue = new SDValue(rawPacket, offset);
        offset += SDValue.LENGTH;

    }

    public PDRValue getPdrValue() {
        return pdrValue;
    }

    public AllPAValue getAllPAValue() {
        return allPAValue;
    }

    public NCGValue getNcgValue() {
        return ncgValue;
    }

    public EMSizeValue getEmSizeValue() {
        return emSizeValue;
    }

    public PucS getPucS() {
        return pucS;
    }

    public VisValue getVisValue() {
        return visValue;
    }

    public AnContValue getAnContValue() {
        return anContValue;
    }

    public NemValue getNemValue() {
        return nemValue;
    }

    public NeaValue getNeaValue() {
        return neaValue;
    }

    public NetValue getNetValue() {
        return netValue;
    }

    public SDValue getSdValue() {
        return sdValue;
    }

    public byte[] getBytes() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try {
            bytes.write(pdrValue.getBytes());
            bytes.write(allPAValue.getBytes());
            bytes.write(ncgValue.getBytes());
            bytes.write(emSizeValue.getBytes());
            bytes.write(pucS.getBytes());
            bytes.write(visValue.getBytes());
            bytes.write(anContValue.getBytes());
            bytes.write(nemValue.getBytes());
            bytes.write(neaValue.getBytes());
            bytes.write(netValue.getBytes());
            bytes.write(sdValue.getBytes());
            bytes.write(new byte[LENGTH - bytes.size()]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes.toByteArray();
    }

    @Override
    public String toString() {
        return "IdentificationStructure{" +
                "pdrValue=" + pdrValue +
                ", allPAValue=" + allPAValue +
                ", ncgValue=" + ncgValue +
                ", emSizeValue=" + emSizeValue +
                ", pucS=" + pucS +
                ", visValue=" + visValue +
                ", anContValue=" + anContValue +
                ", nemValue=" + nemValue +
                ", neaValue=" + neaValue +
                ", netValue=" + netValue +
                ", sdValue=" + sdValue +
                ", length=" + getBytes().length +
                '}';
    }
}
