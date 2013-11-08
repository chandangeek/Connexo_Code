/*
 * AsservissementClient.java
 *
 * Created on 21 februari 2007, 13:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaranplus.core;

import com.energyict.protocolimpl.edf.trimarandlms.axdr.TrimaranDataContainer;

import java.io.IOException;


/**
 *
 * @author Koen
 */
public class AsservissementClient extends AbstractTrimaranObject {
    
   
    private int KDC; // Integer8, - le coefficient de preavis de depassement KDC, exprime en %, avec une valeur entre 80 et 100 ;
    private int KDCD; // Integer8, - le coefficient de degagement de preavis de depassement KDCD, exprime en %, avec une valeur entre 70 et 100
    private int Sorcli; // Integer8 - le numero permettant de choisir le type de programmation des contacts de sortie Clients C2 e C10, avec une valeur entre 1 et 4.
    
    /** Creates a new instance of AsservissementClient */
    public AsservissementClient(TrimaranObjectFactory trimaranObjectFactory) {
        super(trimaranObjectFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("AsservissementClient:\n");
        strBuff.append("   KDC="+getKDC()+"\n");
        strBuff.append("   KDCD="+getKDCD()+"\n");
        strBuff.append("   sorcli="+getSorcli()+"\n");
        return strBuff.toString();
    }        
 
    protected int getVariableName() {
        return 16;
    }
    
    protected byte[] prepareBuild() throws IOException {
        return null;
    }
    
    protected void parse(byte[] data) throws IOException {
        int offset=0;
        TrimaranDataContainer dc = new TrimaranDataContainer();
        dc.parseObjectList(data, getTrimaranObjectFactory().getTrimaran().getLogger());
        setKDC(dc.getRoot().getInteger(offset++));
        setKDCD(dc.getRoot().getInteger(offset++));
        setSorcli(dc.getRoot().getInteger(offset++));
    }

    public int getKDC() {
        return KDC;
    }

    public void setKDC(int KDC) {
        this.KDC = KDC;
    }

    public int getKDCD() {
        return KDCD;
    }

    public void setKDCD(int KDCD) {
        this.KDCD = KDCD;
    }

    public int getSorcli() {
        return Sorcli;
    }

    public void setSorcli(int Sorcli) {
        this.Sorcli = Sorcli;
    }
    
}
