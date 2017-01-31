/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.multisense.api.redknee;

public enum TimeAttribute {
    NOTAPPLICABLE(0, 0),
    MINUTE10(1, 10),
    MINUTE15(2, 15),
    MINUTE1(3, 1),
    HOUR24(4, 60 * 24),
    MINUTE30(5, 30),
    MINUTE5(6, 5),
    MINUTE60(7, 60),
    MINUTE2(10, 2),
    MINUTE3(14, 3),
    PRESENT(15, 0),
    PREVIOUS(16, 0),
    MINUTE20(31, 20),
    FIXEDBLOCK60MIN(50, 60),
    FIXEDBLOCK30MIN(51, 30),
    FIXEDBLOCK20MIN(52, 20),
    FIXEDBLOCK15MIN(53, 15),
    FIXEDBLOCK10MIN(54, 10),
    FIXEDBLOCK5MIN(55, 5),
    FIXEDBLOCK1MIN(56, 1),
    ROLLING60_30(57, 60),
    ROLLING60_20(58, 60),
    ROLLING60_15(59, 60),
    ROLLING60_12(60, 60),
    ROLLING60_10(61, 60),
    ROLLING60_6(62, 60),
    ROLLING60_5(63, 60),
    ROLLING60_4(64, 60),
    ROLLING30_15(65, 30),
    ROLLING30_10(66, 30),
    ROLLING30_6(67, 30),
    ROLLING30_5(68, 30),
    ROLLING30_3(69, 30),
    ROLLING30_2(70, 30),
    ROLLING15_5(71, 15),
    ROLLING15_3(72, 15),
    ROLLING15_1(73, 15),
    ROLLING10_5(74, 10),
    ROLLING10_2(75, 10),
    ROLLING10_1(76, 10),
    ROLLING5_1(77, 5),
    MINUTE12(78, 12),
    HOUR2(79, 120),
    HOUR4(80, 240),
    HOUR6(81, 360),
    HOUR12(82, 720),
    HOUR3(83, 180),
    SPECIFIEDINTERVAL(100, 0),
    SPECIFIEDFIXEDBLOCK(101, 0),
    SPECIFIEDROLLINGBLOCK(102, 0);

    private final int id;
    private final int minutes;

    TimeAttribute(int id, int minutes) {
        this.id = id;
        this.minutes = minutes;
    }

    public int getMinutes() {
        return minutes;
    }

    public static TimeAttribute get(int id) {
        for (TimeAttribute each : values()) {
            if (each.id == id) {
                return each;
            }
        }
        throw new IllegalArgumentException("Unknown measuring period");
    }

    public int getId() {
        return id;
    }
}
