/*
 * Class13PreviousSeasonBillingData.java
 *
 * Created on 13 juli 2005, 15:18
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.alpha.alphaplus.core.classes;



/**
 *
 * @author Koen
 */
public class Class13PreviousSeasonBillingData extends ClassBillingData {
    
    ClassIdentification classIdentification = new ClassIdentification(13,212,false);
        
    /** Creates a new instance of Class11BillingData */
    public Class13PreviousSeasonBillingData(ClassFactory classFactory) {
        super(classFactory);
    }
    
    protected ClassIdentification getClassIdentification() {
        return classIdentification; 
    }    
}