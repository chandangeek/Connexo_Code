/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RegisterFactory.java
 *
 * Created on 23 maart 2006, 17:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edmi.mk6.registermapping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author koen
 */
public class RegisterFactory {
    
    static List list=new ArrayList();
    static {
        list.add(new RegisterInf(0xE390, 22,"L1 active export"));   
        list.add(new RegisterInf(0xE391, 42,"L2 active export"));   
        list.add(new RegisterInf(0xE392, 62,"L3 active export"));   
        list.add(new RegisterInf(0xE393, 2,"Total active export"));   
        
        list.add(new RegisterInf(0xE394, 21,"L1 active import")); 
        list.add(new RegisterInf(0xE395, 41,"L2 active import")); 
        list.add(new RegisterInf(0xE396, 61,"L3 active import")); 
        list.add(new RegisterInf(0xE397, 1,"Total active import")); 
        
        list.add(new RegisterInf(0xE398, 24,"L1 reactive export")); 
        list.add(new RegisterInf(0xE399, 44,"L2 reactive export"));
        list.add(new RegisterInf(0xE39A, 64,"L3 reactive export"));
        list.add(new RegisterInf(0xE39B, 4,"Total reactive export"));
        
        list.add(new RegisterInf(0xE39C, 23,"L1 reactive import"));
        list.add(new RegisterInf(0xE39D, 43,"L2 reactive import"));
        list.add(new RegisterInf(0xE39E, 63,"L3 reactive import"));
        list.add(new RegisterInf(0xE39F, 3,"Total reactive import"));
        
        list.add(new RegisterInf(0xE490, 38,"L1 active Q2"));
        list.add(new RegisterInf(0xE491, 58,"L2 active Q2"));
        list.add(new RegisterInf(0xE492, 78,"L3 active Q2"));
        list.add(new RegisterInf(0xE493, 18,"Total active Q2"));
        
        list.add(new RegisterInf(0xE494, 37,"L1 active Q1"));
        list.add(new RegisterInf(0xE495, 57,"L2 active Q1"));
        list.add(new RegisterInf(0xE496, 77,"L3 active Q1"));
        list.add(new RegisterInf(0xE497, 17,"Total active Q1"));
        
        list.add(new RegisterInf(0xE590, 39,"L1 active Q3"));
        list.add(new RegisterInf(0xE591, 59,"L2 active Q3"));
        list.add(new RegisterInf(0xE592, 79,"L3 active Q3"));
        list.add(new RegisterInf(0xE593, 19,"Total active Q3"));
        
        list.add(new RegisterInf(0xE594, 40,"L1 active Q4"));
        list.add(new RegisterInf(0xE595, 60,"L2 active Q4"));
        list.add(new RegisterInf(0xE596, 80,"L3 active Q4"));
        list.add(new RegisterInf(0xE597, 20,"Total active Q4"));
        
        list.add(new RegisterInf(0xE59C, 26,"L1 reactive Q2"));
        list.add(new RegisterInf(0xE59D, 46,"L2 reactive Q2"));
        list.add(new RegisterInf(0xE59E, 66,"L3 reactive Q2"));
        list.add(new RegisterInf(0xE59F, 6,"Total reactive Q2"));
        
        list.add(new RegisterInf(0xE49C, 25,"L1 reactive Q1"));
        list.add(new RegisterInf(0xE49D, 45,"L2 reactive Q1"));
        list.add(new RegisterInf(0xE49E, 65,"L3 reactive Q1"));
        list.add(new RegisterInf(0xE49F, 5,"Total reactive Q1"));
        
        list.add(new RegisterInf(0xE598, 27,"L1 reactive Q3"));
        list.add(new RegisterInf(0xE599, 47,"L2 reactive Q3"));
        list.add(new RegisterInf(0xE59A, 67,"L3 reactive Q3"));
        list.add(new RegisterInf(0xE59B, 7,"Total reactive Q3"));
        
        list.add(new RegisterInf(0xE598, 28,"L1 reactive Q4"));
        list.add(new RegisterInf(0xE599, 48,"L2 reactive Q4"));
        list.add(new RegisterInf(0xE59A, 68,"L3 reactive Q4"));
        list.add(new RegisterInf(0xE59B, 8,"Total reactive Q4"));
        
        list.add(new RegisterInf(0xE3E0, 30,"L1 apparent export"));
        list.add(new RegisterInf(0xE3E1, 50,"L2 apparent export"));
        list.add(new RegisterInf(0xE3E2, 70,"L3 apparent export"));
        list.add(new RegisterInf(0xE3E3, 10,"Total apparent export"));
        
        list.add(new RegisterInf(0xE3E4, 29,"L1 apparent import"));
        list.add(new RegisterInf(0xE3E5, 49,"L2 apparent import"));
        list.add(new RegisterInf(0xE3E6, 69,"L3 apparent import"));
        list.add(new RegisterInf(0xE3E7, 9,"Total apparent import"));
        
        list.add(new RegisterInf(0xE4E0, 134,"L1 apparent Q2")); // manufacturer
        list.add(new RegisterInf(0xE4E1, 135,"L2 apparent Q2")); // manufacturer
        list.add(new RegisterInf(0xE4E2, 136,"L3 apparent Q2")); // manufacturer
        list.add(new RegisterInf(0xE4E3, 137,"Total apparent Q2")); // manufacturer
        
        list.add(new RegisterInf(0xE4E4, 138,"L1 apparent Q1")); // manufacturer
        list.add(new RegisterInf(0xE4E5, 139,"L2 apparent Q1")); // manufacturer
        list.add(new RegisterInf(0xE4E6, 140,"L3 apparent Q1")); // manufacturer
        list.add(new RegisterInf(0xE4E7, 141,"Total apparent Q1")); // manufacturer
        
        list.add(new RegisterInf(0xE5E0, 142,"L1 apparent Q3")); // manufacturer
        list.add(new RegisterInf(0xE5E1, 143,"L2 apparent Q3")); // manufacturer
        list.add(new RegisterInf(0xE5E2, 144,"L3 apparent Q3")); // manufacturer
        list.add(new RegisterInf(0xE5E3, 145,"Total apparent Q3")); // manufacturer
        
        list.add(new RegisterInf(0xE5E4, 146,"L1 apparent Q4")); // manufacturer
        list.add(new RegisterInf(0xE5E5, 147,"L2 apparent Q4")); // manufacturer
        list.add(new RegisterInf(0xE5E6, 148,"L3 apparent Q4")); // manufacturer
        list.add(new RegisterInf(0xE5E7, 149,"Total apparent Q4")); // manufacturer
    } // static
    
    
    
    /** Creates a new instance of RegisterFactory */
    public RegisterFactory() {
    }
    
    // returns -1 if not existing
    static public int getObisCField(int edmiEnergyRegisterId) {
        Iterator it = list.iterator();
        while(it.hasNext()) {
            RegisterInf ri = (RegisterInf)it.next();
            if (ri.getEdmiEnergyRegisterId() == edmiEnergyRegisterId) {
				return ri.getObisCField();
			}
        }
        return -1;
    }
    
    static public RegisterInf getRegisterInf(int edmiEnergyRegisterId) {
        Iterator it = list.iterator();
        while(it.hasNext()) {
            RegisterInf ri = (RegisterInf)it.next();
            if (ri.getEdmiEnergyRegisterId() == edmiEnergyRegisterId) {
				return ri;
			}
        }
        return null;
    }
    
    // returns null if not existing
    static public String getDescription(int edmiEnergyRegisterId) {
        Iterator it = list.iterator();
        while(it.hasNext()) {
            RegisterInf ri = (RegisterInf)it.next();
            if (ri.getEdmiEnergyRegisterId() == edmiEnergyRegisterId) {
				return ri.getDescription();
			}
        }
        return null;
    }
    
}
