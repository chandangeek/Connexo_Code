package com.energyict.protocolimplv2.elster.garnet.structure.field.bitMaskField;

import com.energyict.protocolimplv2.elster.garnet.common.field.AbstractBitMaskField;
import com.energyict.protocolimplv2.elster.garnet.exception.ParsingException;

import java.util.BitSet;

/**
 * @author sva
 * @since 23/05/2014 - 15:56
 */
public class MeterModel extends AbstractBitMaskField<MeterModel> {

    public static final int LENGTH = 2; // The length expressed in nr of bits

    private BitSet modelMask;
    private int modelCode;
    private Model model;

    public MeterModel() {
        this.modelMask = new BitSet(LENGTH);
        this.model = Model.UNKNOWN;
    }

    public MeterModel(Model model) {
        this.model = model;
    }

    public BitSet getBitMask() {
        return modelMask;
    }

    public MeterModel parse(BitSet bitSet, int posInMask) throws ParsingException {
        int startPos = posInMask * LENGTH;
        modelMask = bitSet.get(startPos, startPos + LENGTH);
        modelCode = convertBitSetToInt(modelMask);
        model = Model.fromVersionCode(modelCode);
        return this;
    }

    @Override
    public int getLength() {
        return LENGTH;
    }

    public int getModelCode() {
        return modelCode;
    }

    public String getModelInfo() {
        if (!this.model.equals(Model.UNKNOWN)) {
            return model.getModelInfo();
        } else {
            return (model.getModelInfo() + " " + model);
        }
    }

    private enum Model {
        UNDEFINED(0, "Undefined"),
        A102C(1, "A102C"),
        A200L3(2, "A200L3"),
        A1052(3, "A1052"),
        UNKNOWN(-1, "Unknown model");

        private final int versionCode;
        private final String versionInfo;

        private Model(int versionCode, String versionInfo) {
            this.versionCode = versionCode;
            this.versionInfo = versionInfo;
        }

        public String getModelInfo() {
            return versionInfo;
        }

        public int getVersionCode() {
            return versionCode;
        }

        public static Model fromVersionCode(int versionCode) {
            for (Model version : Model.values()) {
                if (version.getVersionCode() == versionCode) {
                    return version;
                }
            }
            return Model.UNKNOWN;
        }
    }
}