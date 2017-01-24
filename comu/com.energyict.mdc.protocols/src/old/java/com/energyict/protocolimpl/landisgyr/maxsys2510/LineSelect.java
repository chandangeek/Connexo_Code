package com.energyict.protocolimpl.landisgyr.maxsys2510;

import com.energyict.cbo.Unit;

class LineSelect {

    static final int V1 = 1;
    static final int V2 = 2;
    static final int V3 = 4;
    static final int V4 = 8;

    private static LineSelect all [] = new LineSelect[86];

    static final LineSelect PLUS_WH_RECEIVED
        = create(1, Unit.get("Wh"), "+ watthours received", true, true, true, true);

    static final LineSelect PLUS_WH_DELIVERED
        = create(2, Unit.get("Wh"), "+ watthours delivered", true, true, true, true);

    static final LineSelect PLUS_Q3_VARH_CAPACITIVE_LEADING
        = create(3, Unit.get("varh"), "+ quadrant 3 varhours, capacitive leading", true, true, true, true);

    static final LineSelect PLUS_Q2_VARH_INDUCTIVE_LAGGING
        = create(4, Unit.get("varh"), "+ quadrant 2 varhours, inductive lagging", true, true, true, true);

    static final LineSelect PLUS_Q4_VARH_CAPACITIVE_LEADING
        = create(5, Unit.get("varh"), "+ quadrant 4 varhours, capacitive leading", true, true, true, true);

    static final LineSelect PLUS_Q1_VARH_INDUCTIVE_LAGGING
        = create(6, Unit.get("varh"), "+ quadrant 1 varhours, inductive lagging ", true, true, true, true);

    static final LineSelect MIN_WATTHOURS_RECEIVED
        = create(7, Unit.get("Wh"), "- watthours received", true, true, true, false);

    static final LineSelect MIN_WATTHOURS_DELIVERED
        = create(8, Unit.get("Wh"), "- watthours delivered", true, true, true, false);

    static final LineSelect MIN_Q3_VARHOURS_CAPACITIVE_LEADING
        =  create(9, Unit.get("varh"), "- quadrant 3 varhours, capacitive leading", true, true, true, false);

    static final LineSelect MIN_Q2_VARHOURS_INDUCTIVE_LAGGING
        = create(10, Unit.get("varh"), "- quadrant 2 varhours, inductive lagging", true, true, true, false);

    static final LineSelect MIN_Q4_VARHOURS_CAPACITIVE_LEADING
        = create(11, Unit.get("varh"), "- quadrant 4 varhours, capacitive leading", true, true, true, false);

    static final LineSelect MIN_Q1_VARHOURS_INDUCTIVE_LAGGING
        = create(12, Unit.get("varh"), "- quadrant 1 varhours, inductive lagging", true, true, true, false);

    static final LineSelect V2H_PHASE_A
        = create(13, Unit.get("V2"), "V2h - phase A", true, true, true, true);

    static final LineSelect V2H_PHASE_B
        = create(14, Unit.get("V2"), "V2h - phase B", true, true, true, true);

    static final LineSelect V2H_PHASE_C
        = create(15, Unit.get("V2"), "V2h - phase C", true, true, true, true);

    static final LineSelect I2H_PHASE_A
        = create(16, Unit.get("A2"), "I2h - phase A", true, true, true, true);

    static final LineSelect I2H_PHASE_B
        = create(17, Unit.get("A2"), "I2h - phase B", true, true, true, true);

    static final LineSelect I2H_PHASE_C
        = create(18, Unit.get("A2"), "I2h - phase C", true, true, true, true);

    static final LineSelect I2H_NEUTRAL_CURRENT
        = create(19, Unit.get("A2"), "I2h - neutral current", true, true, true, true);

    static final LineSelect WH_RECEIVED_A
        = create(20, Unit.get("Wh"), "Watthours received, phase A", true, false, false, true);

    static final LineSelect WH_RECEIVED_B
        = create(21, Unit.get("Wh"), "Watthours received, phase B", true, false, false, true);

    static final LineSelect WH_RECEIVED_C
        = create(22, Unit.get("Wh"), "Watthours received, phase C", true, false, false, true);

    static final LineSelect WH_DELIVERED_A
        = create(23, Unit.get("Wh"), "Watthours delivered, phase A", true, false, false, true);

    static final LineSelect WH_DELIVERED_B
        = create(24, Unit.get("Wh"), "Watthours delivered, phase B", true, false, false, true);

    static final LineSelect WH_DELIVERED_C
        = create(25, Unit.get("Wh"), "Watthours delivered, phase C", true, false, false, true);

    static final LineSelect Q3_A
        = create(26, Unit.get("varh"), "Quadrant 3 varhrs, capacitive leading, phase A", true, false, false, true);

    static final LineSelect Q3_B
        = create(27, Unit.get("varh"), "Quadrant 3 varhrs, capacitive leading, phase B", true, false, false, true);

    static final LineSelect Q3_C
        = create(28, Unit.get("varh"), "Quadrant 3 varhrs, capacitive leading, phase C", true, false, false, true);

    static final LineSelect Q2_A
        = create(29, Unit.get("varh"), "Quadrant 2 varhrs, inductive lagging, phase A", true, false, false, true);

    static final LineSelect Q2_B
        = create(30, Unit.get("varh"), "Quadrant 2 varhrs, inductive lagging, phase B ", true, false, false, true);

    static final LineSelect Q2_C
        = create(31, Unit.get("varh"), "Quadrant 2 varhrs, inductive lagging, phase C", true, false, false, true);

    static final LineSelect Q4_A
        = create(32, Unit.get("varh"), "Quadrant 4 varhrs, capacitive leading, phase A", true, false, false, true);

    static final LineSelect Q4_B
        = create(33, Unit.get("varh"), "Quadrant 4 varhrs, capacitive leading, phase B", true, false, false, true);

    static final LineSelect Q4_C
        = create(34, Unit.get("varh"), "Quadrant 4 varhrs, capacitive leading, phase C", true, false, false, true);

    static final LineSelect Q1_A
        = create(35, Unit.get("varh"), "Quadrant 1 varhrs, inductive lagging, phase A", true, false, false, true);

    static final LineSelect Q1_B
        = create(36, Unit.get("varh"), "Quadrant 1 varhrs, inductive lagging, phase B", true, false, false, true);

    static final LineSelect Q1_C
        = create(37, Unit.get("varh"), "Quadrant 1 varhrs, inductive lagging, phase C", true, false, false, true);

    static final LineSelect THD_VOLTS_SQUARED_HOURS_PHASE_A
        = create(38, Unit.get("V2"), "THD Volts Squared Hours, phase A", true, false, false, false);

    static final LineSelect THD_VOLTS_SQUARED_HOURS_PHASE_B
        = create(39, Unit.get("V2"), "THD Volts Squared Hours, phase B", true, false, false, false);

    static final LineSelect THD_VOLTS_SQUARED_HOURS_PHASE_C
        = create(40, Unit.get("V2"), "THD Volts Squared Hours, phase C", true, false, false, false);

    static final LineSelect THD_AMPS_SQUARED_HOURS_PHASE_A
        = create(41, Unit.get("A2"), "THD Amps Squared Hours, phase A", true, false, false, false);

    static final LineSelect THD_AMPS_SQUARED_HOURS_PHASE_B
        = create(42, Unit.get("A2"), "THD Amps Squared Hours, phase B", true, false, false, false);

    static final LineSelect THD_AMPS_SQUARED_HOURS_PHASE_C
        = create(43, Unit.get("A2"), "THD Amps Squared Hours, phase C ", true, false, false, false);

    static final LineSelect THD_AMPS_SQUARED_HOURS_NEUTRAL
        = create(44, Unit.get("A2"), "THD Amps Squared Hours, neutral", true, false, false, false);

    static final LineSelect THD_WH_RECEIVED_PHASE_A
        = create(45, Unit.get("Wh"), "THD Watthours received, phase A", true, false, false, false);

    static final LineSelect THD_WH_RECEIVED_PHASE_B
        = create(46, Unit.get("Wh"), "THD Watthours received, phase B", true, false, false, false);

    static final LineSelect THD_WH_RECEIVED_PHASE_C
        = create(47, Unit.get("Wh"), "THD Watthours received, phase C", true, false, false, false);

    static final LineSelect THD_WH_DELIVERED_PHASE_A
        = create(48, Unit.get("Wh"), "THD Watthours delivered, phase A", true, false, false, false);

    static final LineSelect THD_WH_DELIVERED_PHASE_B
        = create(49, Unit.get("Wh"), "THD Watthours delivered, phase B", true, false, false, false);

    static final LineSelect THD_WH_DELIVERED_PHASE_C
        = create(50, Unit.get("Wh"), "THD Watthours delivered, phase C", true, false, false, false);

    static final LineSelect THD_QUADRANT_3_VARH_CAPACITIVE_LEAD_PH_A
        = create(51, Unit.get("varh"), "THD Quadrant 3 varhours, capacitive lead, ph A", true, false, false, false);

    static final LineSelect THD_QUADRANT_3_VARH_CAPACITIVE_LEAD_PH_B
        = create(52, Unit.get("varh"), "THD Quadrant 3 varhours, capacitive lead, ph B", true, false, false, false);

    static final LineSelect THD_QUADRANT_3_VARH_CAPACITIVE_LEAD_PH_C
        = create(53, Unit.get("varh"), "THD Quadrant 3 varhours, capacitive lead, ph C", true, false, false, false);

    static final LineSelect THD_QUADRANT_2_VARH_INDUCTIVE_LAG_PH_A
        = create(54, Unit.get("varh"), "THD Quadrant 2 varhours, inductive lag, ph A", true, false, false, false);

    static final LineSelect THD_QUADRANT_2_VARH_INDUCTIVE_LAG_PH_B
        = create(55, Unit.get("varh"), "THD Quadrant 2 varhours, inductive lag, ph B", true, false, false, false);

    static final LineSelect THD_QUADRANT_2_VARH_INDUCTIVE_LAG_PH_C
        = create(56, Unit.get("varh"), "THD Quadrant 2 varhours, inductive lag, ph C", true, false, false, false);

    static final LineSelect THD_QUADRANT_4_VARH_CAPACITIVE_LEAD_PH_A
        = create(57, Unit.get("varh"), "THD Quadrant 4 varhours, capacitive lead, ph A", true, false, false, false);

    static final LineSelect THD_QUADRANT_4_VARH_CAPACITIVE_LEAD_PH_B
        = create(58, Unit.get("varh"), "THD Quadrant 4 varhours, capacitive lead, ph B", true, false, false, false);

    static final LineSelect THD_QUADRANT_4_VARH_CAPACITIVE_LEAD_PH_C
        = create(59, Unit.get("varh"), "THD Quadrant 4 varhours, capacitive lead, ph C", true, false, false, false);

    static final LineSelect THD_QUADRANT_1_VARH_INDUCTIVE_LAG_PH_A
        = create(60, Unit.get("varh"), "THD Quadrant 1 varhours, inductive lag, ph A", true, false, false, false);

    static final LineSelect THD_QUADRANT_1_VARH_INDUCTIVE_LAG_PH_B
        = create(61, Unit.get("varh"), "THD Quadrant 1 varhours, inductive lag, ph B", true, false, false, false);

    static final LineSelect THD_QUADRANT_1_VARH_INDUCTIVE_LAG_PH_C
        = create(62, Unit.get("varh"), "THD Quadrant 1 varhours, inductive lag, ph C", true, false, false, false);

    static final LineSelect WH_IRON_LOSS_DELIVERED
        = create(63, Unit.get("Wh"), "Watthours Iron Loss delivered", true, true, true, false);

    static final LineSelect WH_COPPER_LOSS_DELIVERED
        = create(64, Unit.get("Wh"), "Watthours Copper loss delivered", true, true, true, false);

    static final LineSelect WH_IRON_LOSS_RECEIVED
        = create(65, Unit.get("Wh"), "Watthours Iron Loss received", true, true, true, false);

    static final LineSelect WH_COPPER_LOSS_RECEIVED
        = create(66, Unit.get("Wh"), "Watthours Copper loss received", true, true, true, false);

    static final LineSelect VARH_IRON_LOSS_DELIVERED
        = create(67, Unit.get("varh"), "Varhours Iron Loss delivered ", true, true, true, false);

    static final LineSelect VARH_COPPER_LOSS_DELIVERED
        = create(68, Unit.get("varh"), "Varhours Copper loss delivered", true, true, true, false);

    static final LineSelect VARH_IRON_LOSS_RECEIVED
        = create(69, Unit.get("varh"), "Varhours Iron loss received", true, true, true, false);

    static final LineSelect VARH_COPPER_LOSS_RECEIVED
        = create(70, Unit.get("varh"), "Varhours Copper loss received", true, true, true, false);

    static final LineSelect VOLTMINAMP_HOURS_RMS_PHASE_A
        = create(71, Unit.get("vah"), "Volt-amp Hours RMS, phase A", true, false, false, true);

    static final LineSelect VOLTMINAMP_HOURS_RMS_PHASE_B
        = create(72, Unit.get("vah"), "Volt-amp Hours RMS, phase B", true, false, false, true);

    static final LineSelect VOLTMINAMP_HOURS_RMS_PHASE_C
        = create(73, Unit.get("vah"), "Volt-amp Hours RMS, phase C", true, false, false, true);

    static final LineSelect FREQUENCY_HERTZ_HOURS
        = create(74, Unit.getUndefined(), "Frequency - Hertz Hours", true, false, false, true);

    static final LineSelect AUXILIARY_INPUT_1
        = create(75, Unit.getUndefined(), "Auxiliary input 1", true, true, true, true);

    static final LineSelect AUXILIARY_INPUT_2
        = create(76, Unit.getUndefined(), "Auxiliary input 2", true, true, true, true);

    static final LineSelect AUXILIARY_INPUT_3
        = create(77, Unit.getUndefined(), "Auxiliary input 3", true, true, true, true);

    static final LineSelect AUXILIARY_INPUT_4
        = create(78, Unit.getUndefined(), "Auxiliary input 4", true, true, true, true);

    static final LineSelect AUXILIARY_INPUT_5
        = create(79, Unit.getUndefined(), "Auxiliary input 5", true, true, false, true);

    static final LineSelect AUXILIARY_INPUT_6
        = create(80, Unit.getUndefined(), "Auxiliary input 6", true, true, false, true);

    static final LineSelect AUXILIARY_INPUT_7
        = create(81, Unit.getUndefined(), "Auxiliary input 7", true, true, false, true);

    static final LineSelect AUXILIARY_INPUT_8
        = create(82, Unit.getUndefined(), "Auxiliary input 8", true, true, false, true);

    static final LineSelect VOLTAGE_PHASES
        = create(83, Unit.getUndefined(), "Voltage Phases", true, true, true, true);

    static final LineSelect PHASE_C
        = create(84, Unit.getUndefined(), "Phase C", true, true, true, true);

    static final LineSelect PHASE_B
        = create(85, Unit.getUndefined(), "Phase B", true, true, true, true);

    static final LineSelect PHASE_A
        = create(86, Unit.getUndefined(), "Phase A", false, true, true, false);

    static LineSelect create(
            int id, Unit unit, String description,
            boolean v1, boolean v2, boolean v3, boolean v4) {

        LineSelect ls = new LineSelect(id, unit, description, v1, v2, v3, v4);
        all[id-1] = ls;
        return ls;
    }

    int id;
    Unit unit;
    int support;
    String description;

    LineSelect(int id, Unit unit, String description, boolean v1, boolean v2, boolean v3, boolean v4) {
        this.id = id;
        this.unit = unit;
        this.description = description;

        if (v1)
            support = support | V1;
        if (v2)
            support = support | V2;
        if (v3)
            support = support | V3;
        if (v4)
            support = support | V4;

    }

    int getId() {
        return id;
    }

    Unit getUnit() {
        return unit;
    }

    String getDescription( ){
        return description;
    }

    static LineSelect get(int index) {
        if (index < 1 || index > all.length)
            return null;
        return all[index - 1];
    }

    boolean supportV1() {
        return (support & V1) > 0;
    }

    boolean supportV2() {
        return (support & V2) > 0;
    }

    boolean supportV3() {
        return (support & V3) > 0;
    }

    boolean supportV4() {
        return (support & V4) > 0;
    }

    public String toString() {
        return description;
    }

}
