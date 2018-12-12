/*
 * UseFramework.java
 *
 * Created on 18 juli 2006, 13:46
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.osiframework;

/**
 *
 * @author Koen
 */
public class UseFramework {
    
    /** Creates a new instance of UseFramework */
    public UseFramework() {
    }
    
    private void start() {
        try {
            
            LayerManager lm = new LayerManager();
            lm.init();
            lm.getApplicationLayer().sendCommand(new byte[]{0,1,2});
            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        UseFramework uf = new UseFramework();
        uf.start();
    }
    
}
