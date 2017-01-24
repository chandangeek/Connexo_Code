package com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractBitMaskField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

import java.util.BitSet;

/**
 * @author sva
 * @since 23/05/2014 - 15:56
 */
public class AffectedMeterMask extends AbstractBitMaskField<AffectedMeterMask> {

    public static final int LENGTH = 1; // The length expressed in nr of bits

    private BitSet maskBitSet;
    private int maskCode;
    private Mask mask;

    public AffectedMeterMask() {
        this.maskBitSet = new BitSet(LENGTH);
        this.mask = Mask.UNKNOWN;
    }

    public BitSet getBitMask() {
        return maskBitSet;
    }

    @Override
    public AffectedMeterMask parse(BitSet bitSet, int posInMask) throws ParsingException {
        int startPos = posInMask * LENGTH;
        maskBitSet = bitSet.get(startPos, startPos + LENGTH);
        maskCode = convertBitSetToInt(maskBitSet);
        mask = Mask.fromMaskCode(maskCode);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getMaskCode() {
        return maskCode;
    }

    public String getMaskInfo() {
        if (!this.mask.equals(Mask.UNKNOWN)) {
            return mask.getMaskInfo();
        } else {
            return (mask.getMaskInfo() + " " + maskCode);
        }
    }

    public enum Mask {
        NOT_SELECTED(0, "Not selected"),
        SELECTED(1, "Selected"),
        UNKNOWN(-1, "Unknown mask");

        private final int maskCode;
        private final String maskInfo;

        private Mask(int maskCode, String maskInfo) {
            this.maskCode = maskCode;
            this.maskInfo = maskInfo;
        }

        public String getMaskInfo() {
            return maskInfo;
        }

        public int getMaskCode() {
            return maskCode;
        }

        public static Mask fromMaskCode(int statusCode) {
            for (Mask mask : Mask.values()) {
                if (mask.getMaskCode() == statusCode) {
                    return mask;
                }
            }
            return Mask.UNKNOWN;
        }
    }
}