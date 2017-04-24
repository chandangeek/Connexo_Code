/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ObisCodeExtensions.java
 *
 * Created on 6 november 2006, 8:31
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.base;

/**
 *
 * @author Koen
 */
public class ObisCodeExtensions {
   
    // Obiscodes NOT defined in the ObisCode class
    static public final int CODE_D_TIME_INTEGRAL2=9;
    
    
    // delivered is the same as export
    // received is the same as import
    
    static public final int OBISCODE_C_VOLTSQUARE=128;
    static public final int OBISCODE_C_AMPSQUARE=129;
    static public final int OBISCODE_C_TIMEOFOCCURANCE=130;
    static public final int OBISCODE_C_VA_DELIVERED_ARITHMATIC=131; // Itron/Schlumberger Sentinel
    static public final int OBISCODE_C_VA_RECEIVED_ARITHMATIC=132; // Itron/Schlumberger Sentinel
    static public final int OBISCODE_C_VA_LAG=133; // Itron/Schlumberger Sentinel
    static public final int OBISCODE_C_Q_DELIVERED=134; // Itron/Schlumberger Sentinel
    static public final int OBISCODE_C_Q_RECEIVED=135; // Itron/Schlumberger Sentinel
    static public final int OBISCODE_C_REACTIVE_NET=136; // Itron/Schlumberger Sentinel (reactive import - reactive export)
    static public final int OBISCODE_C_REACTIVE_NET_DELIVERED=137; // Itron/Schlumberger Sentinel (Q1 - Q4) (during active delivered)
    static public final int OBISCODE_C_REACTIVE_NET_RECEIVED=138; // Itron/Schlumberger Sentinel (Q2 - Q3) (during active received)
    static public final int OBISCODE_C_TOTALIZER=139; // Itron/Schlumberger Sentinel the totalizer number is set with the B field
    static public final int OBISCODE_C_POWERFACTOR_ARITHMATIC=140; // Itron/Schlumberger Sentinel
    static public final int OBISCODE_C_ACTIVE_NET=141; // Itron/Schlumberger Q1000 (active import - active export)
    static public final int OBISCODE_C_ACTIVE_NET_PHASE1=142; // Itron/Schlumberger Q1000 (active import - active export)
    static public final int OBISCODE_C_ACTIVE_NET_PHASE2=143; // Itron/Schlumberger Q1000 (active import - active export)
    static public final int OBISCODE_C_ACTIVE_NET_PHASE3=144; // Itron/Schlumberger Q1000 (active import - active export)
    static public final int OBISCODE_C_REACTIVE_NET_PHASE1=145; // Itron/Schlumberger Q1000 (reactive import - reactive export)
    static public final int OBISCODE_C_REACTIVE_NET_PHASE2=146; // Itron/Schlumberger Q1000 (reactive import - reactive export)
    static public final int OBISCODE_C_REACTIVE_NET_PHASE3=147; // Itron/Schlumberger Q1000 (reactive import - reactive export)
    static public final int OBISCODE_C_VOLTSQUARE_PHASE1=148; // Itron/Schlumberger Q1000
    static public final int OBISCODE_C_VOLTSQUARE_PHASE2=149; // Itron/Schlumberger Q1000
    static public final int OBISCODE_C_VOLTSQUARE_PHASE3=150; // Itron/Schlumberger Q1000
    static public final int OBISCODE_C_AMPSQUARE_PHASE1=151; // Itron/Schlumberger Q1000
    static public final int OBISCODE_C_AMPSQUARE_PHASE2=152; // Itron/Schlumberger Q1000
    static public final int OBISCODE_C_AMPSQUARE_PHASE3=153; // Itron/Schlumberger Q1000
    
    // from C=200 manufacturer specific
    
    
    static public final int OBISCODE_D_HIGHESTPEAK=128; // 128..132 max 5 highest peaks (maximum demands or previous max demands))
    
    static public final int OBISCODE_D_COINCIDENT=133; // 133..142 max 10 coincident demand valmues
    
    static public final int OBISCODE_D_CONTINUOUS_CUMULATIVE_DEMAND=143; // 143..152 max 10 continuous cumulative demand values
    
    static public final int OBISCODE_D_PEAK_INSTANTANEOUS=153; // 154..157 reserved
    
    static public final int OBISCODE_D_TIMEOFOCCURANCE=158; // 159..162 reserved
    
    static public final int OBISCODE_D_PREVIOUS_MAX=163; // Itron/Schlumberger Vectron 
    
    static public final int OBISCODE_D_CURRENT_AVERAGE1_ARITHMETIC=168; // Itron/Schlumberger Q1000 used for VA value (vectorial -> D=4)
    static public final int OBISCODE_D_CURRENT_INSTANTANEOUS_ARITHMETIC=169; // Itron/Schlumberger Q1000 used for VA value (vectorial -> D=7)
    static public final int OBISCODE_D_CURRENT_TIME_INTEGRAL1_ARITHMETIC=170; // Itron/Schlumberger Q1000 used for VA value (vectorial -> D=8)
    
//    static public final int OBISCODE_D_CURRENT_PERCENT_THD_METHOD1=175; // Itron/Schlumberger Q1000
//    static public final int OBISCODE_D_CURRENT_PERCENT_THD_METHOD2=176; // Itron/Schlumberger Q1000
//
//    static public final int OBISCODE_D_CURRENT_AVERAGE1_FUNDAMENTAL=180; // Itron/Schlumberger Q1000
//    static public final int OBISCODE_D_CURRENT_INSTANTANEOUS_FUNDAMENTAL=181; // Itron/Schlumberger Q1000
//    static public final int OBISCODE_D_CURRENT_TIME_INTEGRAL1_FUNDAMENTAL=182; // Itron/Schlumberger Q1000
//    
    
    
    // from D=200 manufacturer specific
    
    
    
    static public final int OBISCODE_E_VALUEATDEMANDRESET=128;    
    
    
    // from E=200 manufacturer specific
    
    
    /** Creates a new instance of ObisCodeExtensions */
    public ObisCodeExtensions() {
    }
    
}
