/*
 * PACTMode.java
 *
 * Created on 8 april 2004, 10:49
 */

package com.energyict.protocolimpl.pact.core.common;

/**
 *
 * @author  Koen
 */
public class PACTMode {
    
    private static final int PACT_STANDARD=0x00;
    private static final int PAKNET=0x01;
    private static final int PACTLAN=0x02;
    int pactMode=0;
    /** Creates a new instance of PACTMode */
    public PACTMode(int pakNet, int pactLan) {
        if (pakNet != 0) pactMode|=PAKNET;
        if (pactLan != 0) pactMode|=PACTLAN;
    }
    public boolean isPACTStandard() {
        return (!isPAKNET());  
    }
    public boolean isPAKNET() {
        return ((getPactMode()&PAKNET) == PAKNET);  
    }
    public boolean isPACTLAN() {
        return ((getPactMode()&PACTLAN) == PACTLAN);  
    }
    
    /** Getter for property pactMode.
     * @return Value of property pactMode.
     *
     */
    public int getPactMode() {
        return pactMode;
    }

    
}
