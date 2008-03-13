package com.energyict.echelon;

import java.util.Iterator;
import java.util.TreeMap;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;

/**
 * Table 1; Source Code IDs for Meter Disply and Load Profiles 
 * @author fbo
 */

public class SourceCode {
    
    private final static TreeMap map = new TreeMap();
    
    final static SourceCode FWD_ACTIVE = 
        new SourceCode(0, "Fwd Active Wh L1, L2, L3", Unit.get(BaseUnit.WATTHOUR));
    
    final static SourceCode REV_ACTIVE = 
        new SourceCode(1, "Rev Active Wh L1, L2, L3", Unit.get(BaseUnit.WATTHOUR));
    
    final static SourceCode IMPORT_REACTIVE = 
        new SourceCode(2, "Import Reactive Varh L1, L2, L3", Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR));
    
    final static SourceCode EXPORT_REACTIVE = 
        new SourceCode(3, "Export_Reactive Varh L1, L2, L3", Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR));
    
    final static SourceCode POWER_OF_MINUTES = 
        new SourceCode(4, "Power_Off_Minutes", Unit.get(BaseUnit.MINUTE));
    
    final static SourceCode POWER_OUTAGE_QTY = 
        new SourceCode(5, "Power Outage Qty", Unit.getUndefined() );
    
    final static SourceCode FWD_PLUS_REV_ACTIVE = 
        new SourceCode(6,  "Fwd+Rev Active Wh L1, L2, L3", Unit.get(BaseUnit.WATTHOUR) );
    
    final static SourceCode FWD_MIN_REV_ACTIVE = 
        new SourceCode(7, "Fwd-Rev Active Wh L1, L2, L3", Unit.get(BaseUnit.WATTHOUR) );
    
    final static SourceCode FWD_ACTIVE_W = 
        new SourceCode(8,  "Fwd Active W L1, L2, L3", Unit.get(BaseUnit.WATT) );
    
    final static SourceCode REV_ACTIVE_W = 
        new SourceCode(9,  "Rev Active W L1, L2, L3", Unit.get(BaseUnit.WATT));
    
    final static SourceCode IMPORT_REACTIVE_VAR = 
        new SourceCode(10, "Import Reactive Var L1, L2, L3", Unit.get(BaseUnit.VOLTAMPEREREACTIVE));
    
    final static SourceCode EXPORT_REACTIVE_W = 
        new SourceCode(11, "Export Reactive Var L1, L2, L3", Unit.get(BaseUnit.VOLTAMPEREREACTIVE));
    
    final static SourceCode RMS_CURRENT_L1 = 
        new SourceCode(12, "RMS Current L1", Unit.get(BaseUnit.AMPERE));
    
    final static SourceCode RMS_CURRENT_L2 = 
        new SourceCode(13, "RMS Current L2", Unit.get(BaseUnit.AMPERE));
    
    final static SourceCode RMS_CURRENT_L3 = 
        new SourceCode(14, "RMS Current L3", Unit.get(BaseUnit.AMPERE));
    
    final static SourceCode RMS_VOLTAGE_L1 = 
        new SourceCode(15, "RMS Voltage L1", Unit.get(BaseUnit.VOLT));
    
    final static SourceCode RMS_VOLTAGE_L2 = 
        new SourceCode(16, "RMS Voltage L2", Unit.get(BaseUnit.VOLT));
    
    final static SourceCode RMS_VOLTAGE_L3 = 
        new SourceCode(17, "RMS Voltage L3", Unit.get(BaseUnit.VOLT));
    
    final static SourceCode POWER_FACTOR_L1 = 
        new SourceCode(18, "Power Factor L1", Unit.getUndefined());
    
    final static SourceCode FREQUENCY = 
        new SourceCode(19, "Frequency", Unit.get(BaseUnit.HERTZ, -3) );
    
    final static SourceCode VA_POWER = 
        new SourceCode(20, "VA Power", Unit.get(BaseUnit.VOLTAMPERE));
    
    final static SourceCode POWER_FACTOR_L2 = 
        new SourceCode(21, "Power Factor L2", Unit.getUndefined());
    
    final static SourceCode POWER_FACTOR_L3 = 
        new SourceCode(22, "Power Factor L3", Unit.getUndefined());
    
    final static SourceCode SIN_PHASE_ANGLE_L1 = 
        new SourceCode(23, "Sin phase angle L1", Unit.getUndefined());
    
    final static SourceCode SIN_PHASE_ANGLE_L2 = 
        new SourceCode(24, "Sin phase angle L2", Unit.getUndefined());
    
    final static SourceCode SIN_PHASE_ANGLE_L3 = 
        new SourceCode(25, "Sin phase angle L3", Unit.getUndefined());
    
    final static SourceCode CHANNEL_0_PULSE_INPUT = 
        new SourceCode(26, "Channel 0 pulse input", Unit.getUndefined());
    
    final static SourceCode CHANNEL_1_PULSE_INPUT = 
        new SourceCode(27, "Channel 1 pulse input", Unit.getUndefined());
    
    final static SourceCode ERROR_COUNTER = 
        new SourceCode(28, "Error Counter", Unit.getUndefined());
    
    final static SourceCode T0_FWD_ACTIVE = 
        new SourceCode(29, "T0 Fwd Active Wh L1, L2, L3", Unit.get(BaseUnit.WATTHOUR));
    
    final static SourceCode T0_REV_ACTIVE = 
        new SourceCode(30, "T0 Rev Active Wh L1, L2, L3", Unit.get(BaseUnit.WATTHOUR));
    
    final static SourceCode T0_IMPORT_REACTIVE = 
        new SourceCode(31, "T0 Import Reactive VArh L1, L2, L3", Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR));
    
    final static SourceCode T0_EXPORT_REACTIVE = 
        new SourceCode(32, "T0 Export Reactive VArh L1, L2, L3", Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR));
    
    final static SourceCode T0_POWER_OFF_MINUTES = 
        new SourceCode(33, "T0 Power Off Minutes", Unit.getUndefined());
    
    final static SourceCode T0_POWER_OUTAGE_QTY = 
        new SourceCode(34, "T0 Power Outage Qty", Unit.getUndefined());
    
    final static SourceCode T0_FWD_PLUS_REV_ACTIVE =
        new SourceCode(35, "T0 Fwd+Rev Active Wh L1, L2, L3", Unit.get(BaseUnit.WATTHOUR));
    
    final static SourceCode T0_FWD_MIN_REV_ACTIVE = 
        new SourceCode(36, "T0 Fwd-Rev Active Wh L1, L2, L3", Unit.get(BaseUnit.WATTHOUR));
    
    final static SourceCode T0_CHANNEL_0_PULSE_INPUT = 
        new SourceCode(37, "T0 Channel 0 pulse input", Unit.getUndefined());
    
    final static SourceCode T0_CHANNEL_1_PULSE_INPUT = 
        new SourceCode(38, "T0 Channel 1 pulse input", Unit.getUndefined());
    
    final static SourceCode T0_ERROR_COUNTER = 
        new SourceCode(39, "T0 Error Counter", Unit.getUndefined());
    
    final static SourceCode T1_FWD_ACTIVE = 
        new SourceCode(40, "T1 Fwd Active Wh L1, L2, L3", Unit.get(BaseUnit.WATTHOUR));
    
    final static SourceCode T1_REV_ACTIVE = 
        new SourceCode(41, "T1 Rev Active Wh L1, L2, L3", Unit.get(BaseUnit.WATTHOUR));
    
    final static SourceCode T1_IMPORT_REACTIVE = 
        new SourceCode(42, "T1 Import Reactive VArh L1, L2, L3", Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR));
    
    final static SourceCode T1_EXPORT_REACTIVE = 
        new SourceCode(43, "T1 Export Reactive VArh L1, L2, L3", Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR));
    
    final static SourceCode T1_POWER_OFF_MINUTES = 
        new SourceCode(44, "T1 Power Off Minutes", Unit.get(BaseUnit.MINUTE));
    
    final static SourceCode T1_POWER_OUTAGE_QTY = 
        new SourceCode(45, "T1 Power Outage Qty", Unit.getUndefined());
    
    final static SourceCode T1_FWD_PLUS_REV_ACTIVE = 
        new SourceCode(46, "T1 Fwd+Rev Active Wh L1, L2, L3", Unit.get(BaseUnit.WATTHOUR));
    
    final static SourceCode T1_FWD_MIN_REV_ACTIVE = 
        new SourceCode(47, "T1 Fwd-Rev Active Wh L1, L2, L3", Unit.get(BaseUnit.WATTHOUR));
    
    final static SourceCode T1_CHANNEL_0_PULSE_INPUT = 
        new SourceCode(48, "T1 Channel 0 pulse input", Unit.getUndefined());
    
    final static SourceCode T1_CHANNEL_1_PULSE_INPUT = 
        new SourceCode(49, "T1 Channel 1 pulse input", Unit.getUndefined());
    
    final static SourceCode T1_ERROR_C0UNTER = 
        new SourceCode(50, "T1 Error Counter", Unit.getUndefined());
    
    final static SourceCode T2_FWD_ACTIVE = 
        new SourceCode(51, "T2 Fwd Active Wh L1, L2, L3", Unit.get(BaseUnit.WATTHOUR));
    
    final static SourceCode T2_REV_ACTIVE = 
        new SourceCode(52, "T2 Rev Active Wh L1, L2, L3", Unit.get(BaseUnit.WATTHOUR));
    
    final static SourceCode T2_IMPORT_REACTIVE = 
        new SourceCode(53, "T2 Import Reactive VArh L1, L2, L3", Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR));
    
    final static SourceCode T2_EXPORT_REACTIVE = 
        new SourceCode(54, "T2 Export Reactive VArh L1, L2, L3", Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR));
    
    final static SourceCode T2_POWER_OFF_MINUTES = 
        new SourceCode(55, "T2 Power Off Minutes", Unit.get(BaseUnit.MINUTE));
    
    final static SourceCode T2_POWER_OUTAGE_QTY = 
        new SourceCode(56, "T2 Power Outage Qty", Unit.getUndefined());
    
    final static SourceCode T2_FWD_PLUS_REV_ACTIVE = 
        new SourceCode(57, "T2 Fwd+Rev Active", Unit.get(BaseUnit.WATTHOUR));
    
    final static SourceCode T2_FWD_REV_ACTIVE = 
        new SourceCode(58, "T2 Fwd-Rev Active", Unit.get(BaseUnit.WATTHOUR));
    
    final static SourceCode T2_CHANNEL_0_PULSE_INPUT = 
        new SourceCode(59, "T2 Channel 0 pulse input", Unit.get(BaseUnit.VOLT));
    
    final static SourceCode T2_CHANNEL_1_PULSE_INPUT = 
        new SourceCode(60, "T2 Channel 1 pulse input", Unit.getUndefined());
    
    final static SourceCode T2_ERROR_COUNTER = 
        new SourceCode(61, "T2 Error Counter", Unit.getUndefined());
    
    final static SourceCode T3_FWD_ACTIVE = 
        new SourceCode(62, "T3 Fwd Active Wh L1, L2, L3", Unit.get(BaseUnit.WATTHOUR));
    
    final static SourceCode T3_REV_ACTIVE = 
        new SourceCode(63, "T3 Rev Active Wh L1, L2, L3", Unit.get(BaseUnit.WATTHOUR));
    
    final static SourceCode T3_IMPORT_REACTIVE = 
        new SourceCode(64, "T3 Import Reactive VArh L1, L2, L3", Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR));
    
    final static SourceCode T3_EXPORT_REACTIVE = 
        new SourceCode(65, "T3 Export Reactive VArh L1, L2, L3", Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR));
    
    final static SourceCode T3_POWER_OFF_MINUTES = 
        new SourceCode(66, "T3 Power Off Minutes", Unit.get(BaseUnit.MINUTE));
    
    final static SourceCode T3_POWER_OUTAGE_QTY = 
        new SourceCode(67, "T3 Power Outage Qty", Unit.getUndefined());
    
    final static SourceCode T3_FWD_PLUS_REV_ACTIVE = 
        new SourceCode(68, "T3 Fwd+Rev Active Wh L1, L2, L3", Unit.get(BaseUnit.WATTHOUR));
    
    final static SourceCode T3_FWD_MIN_ACTIVE = 
        new SourceCode(69, "T3 Fwd-Rev Active Wh L1, L2, L3", Unit.get(BaseUnit.WATTHOUR));
    
    final static SourceCode T3_CHANNEL_0_PULSE_INPUT = 
        new SourceCode(70, "T3 Channel 0 pulse input", Unit.getUndefined());
    
    final static SourceCode T3_CHANNEL_1_PULSE_INPUT = 
        new SourceCode(71, "T3 Channel 1 pulse input", Unit.getUndefined());
    
    final static SourceCode T3_ERROR_COUNTER = 
        new SourceCode(72, "T3 Error Counter", Unit.getUndefined());
    
    private int id;
    private String description;
    private Unit unit;
    
    private SourceCode(int id, String description, Unit unit){
        this.id = id;
        this.description = description;
        this.unit = unit;
        map.put(new Integer(id), this);
    }
    
    public static SourceCode get(int id){
        return (SourceCode)map.get(new Integer(id));
    }
    
    public Unit getUnit(){
        return unit;
    }
    
    public String getDescription(){
        return description;
    }
    
    public String toString(){
        return "SourceCode[ " + id + " " + description + " " + unit + "]";
    }
    
    public static void main(String [] args){
        Iterator  i = map.values().iterator();
        while(i.hasNext()) {
            System.out.println(i.next());
        }
        
    }
}
