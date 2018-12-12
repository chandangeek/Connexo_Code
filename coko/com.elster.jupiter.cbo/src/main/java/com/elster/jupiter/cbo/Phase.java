/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

import com.elster.jupiter.util.Checks;

public enum Phase {
    NOTAPPLICABLE(0),
    PHASEN(16),
    PHASENTOGND(17),
    PHASEC(32),
    PHASECN(33),
    PHASECA(40),
    PHASECAN(41),
    PHASEB(64),
    PHASEBN(65),
    PHASEBC(66),
    PHASEBA(72),
    PHASEBCN(97),
    PHASEA(128),
    PHASEAN(129),
    PHASEAB(132),
    PHASEAA(136),
    PHASEABN(193),
    PHASEABC(224),
    PHASEABCN(225),
    PHASES2(256),
    PHASES2N(257),
    PHASES1(512),
    PHASES1N(513),
    PHASES12(768),
    PHASES12N(769),
    PHASETHREEWIREWYE(1248),
    PHASEFOURWIREWYE(1249),
    PHASETHREEWIREDELTA(2272),
    PHASEFOURWIREDELTA(2273),
    PHASEFOURWIREHLDELTA(6369),
    PHASEFOURWIREOPENDELTA(10465),
    PHASENETWORKED(17153);

    private final boolean n2;
    private final boolean c2;
    private final boolean b2;
    private final boolean a2;
    private final boolean n1;
    private final boolean c1;
    private final boolean b1;
    private final boolean a1;
    private final boolean s2;
    private final boolean s1;
    private final boolean wye;
    private final boolean delta;
    private final boolean highLeg;
    private final boolean open;
    private final boolean networked;

    Phase(int value) {
        int mask = 1;
        this.n2 = (value & mask) != 0;
        mask <<= 1;
        this.c2 = (value & mask) != 0;
        mask <<= 1;
        this.b2 = (value & mask) != 0;
        mask <<= 1;
        this.a2 = (value & mask) != 0;
        mask <<= 1;
        this.n1 = (value & mask) != 0;
        mask <<= 1;
        this.c1 = (value & mask) != 0;
        mask <<= 1;
        this.b1 = (value & mask) != 0;
        mask <<= 1;
        this.a1 = (value & mask) != 0;
        mask <<= 1;
        this.s2 = (value & mask) != 0;
        mask <<= 1;
        this.s1 = (value & mask) != 0;
        mask <<= 1;
        this.wye = (value & mask) != 0;
        mask <<= 1;
        this.delta = (value & mask) != 0;
        mask <<= 1;
        this.highLeg = (value & mask) != 0;
        mask <<= 1;
        this.open = (value & mask) != 0;
        mask <<= 1;
        this.networked = (value & mask) != 0;
    }

    public static Phase get(int id) {
        for (Phase each : values()) {
            if (each.getId() == id) {
                return each;
            }
        }
        throw new IllegalArgumentException("" + id);
    }

    public int getId() {
        int value = shiftAndAdd(0, networked);
        value = shiftAndAdd(value, open);
        value = shiftAndAdd(value, highLeg);
        value = shiftAndAdd(value, delta);
        value = shiftAndAdd(value, wye);
        value = shiftAndAdd(value, s1);
        value = shiftAndAdd(value, s2);
        value = shiftAndAdd(value, a1);
        value = shiftAndAdd(value, b1);
        value = shiftAndAdd(value, c1);
        value = shiftAndAdd(value, n1);
        value = shiftAndAdd(value, a2);
        value = shiftAndAdd(value, b2);
        value = shiftAndAdd(value, c2);
        return shiftAndAdd(value, n2);
    }


    private int shiftAndAdd(int value, boolean add) {
        value <<= 1;
        if (add) {
            value++;
        }
        return value;
    }

    public String getBaseDescription() {
        String base = "";

        if (s1) {
            base += "S1";
        }
        if (s2) {
            base += "S2";
        }
        if (a1) {
            base += "A";
        }
        if (b1) {
            base += "B";
        }
        if (c1) {
            base += "C";
        }
        if (n1) {
            base += "N";
        }
        if (a2) {
            base += "A";
        }
        if (b2) {
            base += "B";
        }
        if (c2) {
            base += "C";
        }
        if (n2) {
            base += "N";
        }
        if (wye) {
            base = !n2 ? "threeWireWye" : "fourWireWye";
        }
        if (delta) {
            if (!n2) {
                base = "threeWireDelta";
            } else {
                if (!open) {
                    base = !highLeg ? "fourWireDelta" : "fourWireHLDelta";
                } else {
                    base = "fourWireOpenDelta";
                }
            }
        }
        if (networked) {
            base = "networked";
        }
        if (Checks.is(base).emptyOrOnlyWhiteSpace()) {
            base = "NotApplicable";
        }
        return base;
    }

    public String getDescription() {
        return "Phase-" + getBaseDescription();
    }

    @Override
    public String toString() {
        return "Phase " + getBaseDescription();
    }

    public boolean isApplicable() {
        return s1 || s2 || a1 || b1 || c1 || n1;
    }

}
