/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cbo;

public enum TimeAttribute {
    NOTAPPLICABLE(0, "Not applicable", 0),
    MINUTE10(1, "10-minute", 10),
    MINUTE15(2, "15-minute", 15),
    MINUTE1(3, "1-minute", 1),
    HOUR24(4, "24-hour", 60 * 24),
    MINUTE30(5, "30-minute", 30),
    MINUTE5(6, "5-minute", 5),
    MINUTE60(7, "60-minute", 60),
    MINUTE2(10, "2-minute", 2),
    MINUTE3(14, "3-minute", 3),
    PRESENT(15, "Present", 0),
    PREVIOUS(16, "Previous", 0),
    MINUTE20(31, "20-minute", 20),
    FIXEDBLOCK60MIN(50, "60-Minute Fixed Block", 60),
    FIXEDBLOCK30MIN(51, "30-Minute Fixed Block", 30),
    FIXEDBLOCK20MIN(52, "20-Minute Fixed Block", 20),
    FIXEDBLOCK15MIN(53, "15-Minute Fixed Block", 15),
    FIXEDBLOCK10MIN(54, "10-Minute Fixed Block", 10),
    FIXEDBLOCK5MIN(55, "5-Minute Fixed Block", 5),
    FIXEDBLOCK1MIN(56, "1-Minute Fixed Block", 1),
    ROLLING60_30(57, "Rolling Block 60-Minute with 30-Minute Sub-intervals", 60),
    ROLLING60_20(58, "Rolling Block 60-Minute with 20-Minute Sub-intervals", 60),
    ROLLING60_15(59, "Rolling Block 60-Minute with 15-Minute Sub-intervals", 60),
    ROLLING60_12(60, "Rolling Block 60-Minute with 12-Minute Sub-intervals", 60),
    ROLLING60_10(61, "Rolling Block 60-Minute with 10-Minute Sub-intervals", 60),
    ROLLING60_6(62, "Rolling Block 60-Minute with 6-Minute Sub-intervals", 60),
    ROLLING60_5(63, "Rolling Block 60-Minute with 5-Minute Sub-intervals", 60),
    ROLLING60_4(64, "Rolling Block 60-Minute with 4-Minute Sub-intervals", 60),
    ROLLING30_15(65, "Rolling Block 30-Minute with 15-Minute Sub-intervals", 30),
    ROLLING30_10(66, "Rolling Block 30-Minute with 10-Minute Sub-intervals", 30),
    ROLLING30_6(67, "Rolling Block 30-Minute with 6-Minute Sub-intervals", 30),
    ROLLING30_5(68, "Rolling Block 30-Minute with 5-Minute Sub-intervals", 30),
    ROLLING30_3(69, "Rolling Block 30-Minute with 3-Minute Sub-intervals", 30),
    ROLLING30_2(70, "Rolling Block 30-Minute with 2-Minute Sub-intervals", 30),
    ROLLING15_5(71, "Rolling Block 15-Minute with 5-Minute Sub-intervals", 15),
    ROLLING15_3(72, "Rolling Block 15-Minute with 3-Minute Sub-intervals", 15),
    ROLLING15_1(73, "Rolling Block 15-Minute with 1-Minute Sub-intervals", 15),
    ROLLING10_5(74, "Rolling Block 10-Minute with 5-Minute Sub-intervals", 10),
    ROLLING10_2(75, "Rolling Block 10-Minute with 2-Minute Sub-intervals", 10),
    ROLLING10_1(76, "Rolling Block 10-Minute with 2-Minute Sub-intervals", 10),
    ROLLING5_1(77, "Rolling Block 5-Minute with 1-Minute Sub-intervals", 5),
    MINUTE12(78, "12-minute", 12),
    HOUR2(79, "2-hour", 120),
    HOUR4(80, "4-hour", 240),
    HOUR6(81, "6-hour", 360),
    HOUR12(82, "12-hour", 720),
    HOUR3(83, "3-hour", 180),
    SPECIFIEDINTERVAL(100, "Specified interval", 0),
    SPECIFIEDFIXEDBLOCK(101, "Specified fixed block", 0),
    SPECIFIEDROLLINGBLOCK(102, "Specified rolling block", 0);

    private final int id;
    private final String description;
    private final int minutes;

    TimeAttribute(int id, String description, int minutes) {
        this.id = id;
        this.description = description;
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
        throw new IllegalEnumValueException(TimeAttribute.class, id);
    }

    public static TimeAttribute getInterval(int interval) {
        switch (interval) {
            case 1:
                return MINUTE1;
            case 2:
                return MINUTE2;
            case 3:
                return MINUTE3;
            case 5:
                return MINUTE5;
            case 10:
                return MINUTE10;
            case 20:
                return MINUTE20;
            case 15:
                return MINUTE15;
            case 30:
                return MINUTE30;
            case 60:
                return MINUTE60;
            default:
                throw new IllegalArgumentException("" + interval);
        }
    }


    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return "TimeAttribute " + id + " : " + description;
    }

    public boolean isApplicable() {
        return id != 0;
    }

    public int getId() {
        return id;
    }
}
