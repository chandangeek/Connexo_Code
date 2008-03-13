/*
 * RandomGenerator.java
 *
 * Created on 15 februari 2007, 14:37
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.base;

import com.energyict.protocol.*;
import java.util.*;

/**
 *
 * @author Koen
 */
public class RandomGenerator { 
    
    
    
    /** Creates a new instance of RandomGenerator */
    private RandomGenerator() {
    }
    
    static public byte[] getRandomSequence() {
        Random rnd = new Random();
        long val = rnd.nextLong();
        byte[] randomSequence = new byte[8];
        for (int i=0;i<8;i++) {
            randomSequence[7-i] = (byte)(val >> (8 * i));
        }
        return randomSequence;
    }
    
    public static void main(String[] args) {
        System.out.println(ProtocolUtils.outputHexString(RandomGenerator.getRandomSequence()));
    }
    
}
