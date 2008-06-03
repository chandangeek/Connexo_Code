/*
 * CourbeChargePartielle.java
 *
 * Created on 21 februari 2007, 13:15
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaran2p.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.edf.trimarandlms.axdr.DataContainer;


/**
 *
 * @author Koen
 */
public class CourbeChargePartielle1 extends AbstractTrimaranObject {
    
    final int DEBUG=0;
    
    private int[] values;
    
    /** Creates a new instance of TemplateVariableName */
    public CourbeChargePartielle1(TrimaranObjectFactory trimaranObjectFactory) {
        super(trimaranObjectFactory);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("CourbeChargePartielle1:\n");
        for (int i=0;i<getValues().length;i++) {
            strBuff.append("       values["+i+"]=0x"+Integer.toHexString(getValues()[i])+"\n");
        }
        return strBuff.toString();
    }
 
    protected int getVariableName() {
        return 400;
    }
    
    protected byte[] prepareBuild() throws IOException {
        
        return null;
    }
    
    protected void parse(byte[] data) throws IOException {
    	
//    	System.out.println("GN_DEBUG> write to file");
//      	File file = new File("c://TEST_FILES/CourbeCharge_par1.bin");
//      	FileOutputStream fos = new FileOutputStream(file);
//      	fos.write(data);
//      	fos.close();
    	
        int offset=0;
        DataContainer dc = new DataContainer();
//        dc.parseObjectList(data, getTrimaranObjectFactory().getTrimaran().getLogger());
        dc.parseObjectList(data, null);
        
        if (DEBUG>=1) System.out.println("CourbeChargePartielle1, parse, "+ProtocolUtils.outputHexString(data));
        if (DEBUG>=1) System.out.println("CourbeChargePartielle1, parse, dc.getRoot().getNrOfElements()="+dc.getRoot().getNrOfElements());
        if (DEBUG>=1) System.out.println("CourbeChargePartielle1, parse, "+dc.print2strDataContainer());
        
        setValues(new int[dc.getRoot().getNrOfElements()]);
        for (int i=0;i<getValues().length;i++) {
            getValues()[i] = dc.getRoot().getInteger(i) & 0xffff;
        }
    }

    public int[] getValues() {
        return values;
    }

    public void setValues(int[] values) {
        this.values = values;
    }
    
    public static void main(String arg[]){
		try {
			
			CourbeChargePartielle1 ccp1 = new CourbeChargePartielle1(null);
			
			FileInputStream fis;
			File file = new File("c://TEST_FILES/CourbeCharge_par1.bin");
			fis = new FileInputStream(file);
			byte[] data=new byte[(int)file.length()];
			fis.read(data);
			fis.close();       
			
			ccp1.parse(data);
			System.out.println(ccp1);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    }

}
