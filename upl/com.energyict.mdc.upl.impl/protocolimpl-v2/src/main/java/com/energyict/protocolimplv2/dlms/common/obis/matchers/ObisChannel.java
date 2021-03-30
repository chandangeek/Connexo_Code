package com.energyict.protocolimplv2.dlms.common.obis.matchers;

import com.energyict.obis.ObisCode;

public enum ObisChannel {

    A() {
        @Override
        public boolean equals(ObisCode o1, ObisCode o2) {
            return o1.getA() == o2.getA();
        }

        @Override
        public int getValue(ObisCode obisCode) {
            return obisCode.getA();
        }

        @Override
        public ObisCode getDeviceValue(ObisCode obisCode) {
            return new ObisCode(0, obisCode.getB(), obisCode.getC(), obisCode.getD(), obisCode.getE(), obisCode.getF(), obisCode.isRelativeBillingPeriod());
        }
    }, B() {
        @Override
        public boolean equals(ObisCode o1, ObisCode o2) {
            return o1.getB() == o2.getB();
        }

        @Override
        public int getValue(ObisCode obisCode) {
            return obisCode.getB();
        }

        @Override
        public ObisCode getDeviceValue(ObisCode obisCode) {
            return new ObisCode(obisCode.getA(), 0, obisCode.getC(), obisCode.getD(), obisCode.getE(), obisCode.getF(), obisCode.isRelativeBillingPeriod());
        }
    }, C() {
        @Override
        public boolean equals(ObisCode o1, ObisCode o2) {
            return o1.getC() == o2.getC();
        }

        @Override
        public int getValue(ObisCode obisCode) {
            return obisCode.getC();
        }

        @Override
        public ObisCode getDeviceValue(ObisCode obisCode) {
            return new ObisCode(obisCode.getA(), obisCode.getB(), 0, obisCode.getD(), obisCode.getE(), obisCode.getF(), obisCode.isRelativeBillingPeriod());
        }
    }, D() {
        @Override
        public boolean equals(ObisCode o1, ObisCode o2) {
            return o1.getD() == o2.getD();
        }

        @Override
        public int getValue(ObisCode obisCode) {
            return obisCode.getD();
        }

        @Override
        public ObisCode getDeviceValue(ObisCode obisCode) {
            return new ObisCode(obisCode.getA(), obisCode.getB(), obisCode.getC(), 0, obisCode.getE(), obisCode.getF(), obisCode.isRelativeBillingPeriod());
        }
    }, E() {
        @Override
        public boolean equals(ObisCode o1, ObisCode o2) {
            return o1.getE() == o2.getE();
        }

        @Override
        public int getValue(ObisCode obisCode) {
            return obisCode.getE();
        }

        @Override
        public ObisCode getDeviceValue(ObisCode obisCode) {
            return new ObisCode(obisCode.getA(), obisCode.getB(), obisCode.getC(), obisCode.getD(), 0, obisCode.getF(), obisCode.isRelativeBillingPeriod());
        }
    }, F() {
        @Override
        public boolean equals(ObisCode o1, ObisCode o2) {
            return o1.getF() == o2.getF();
        }

        @Override
        public int getValue(ObisCode obisCode) {
            return obisCode.getF();
        }

        @Override
        public ObisCode getDeviceValue(ObisCode obisCode) {
            return new ObisCode(obisCode.getA(), obisCode.getB(), obisCode.getC(), obisCode.getD(), obisCode.getE(), 0, obisCode.isRelativeBillingPeriod());
        }
    };

    public abstract boolean equals(ObisCode o1, ObisCode o2);

    /**
     * This is all due to the fact that CXO does not support attribute no therefore we put it into one of the fields
     * @param obisCode coming from CXO
     * @return value for the ignored field
     */
    public abstract int getValue(ObisCode obisCode);

    public abstract ObisCode getDeviceValue(ObisCode obisCode);
}
