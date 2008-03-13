/*
 * AssociationLN.java
 *
 * Created on 7 oktober 2004, 10:41
 */

package com.energyict.dlms.cosem;
import java.io.*;
import java.util.*;

import com.energyict.dlms.*;
import com.energyict.protocolimpl.dlms.*;
import com.energyict.protocol.*;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.UniversalObject;
/**
 *
 * @author  Koen
 */
public class AssociationLN extends AbstractCosemObject {
    public final int DEBUG=0;
    static public final int CLASSID=15;
    
    UniversalObject[] buffer;
    
    /** Creates a new instance of AssociationSN */
    public AssociationLN(ProtocolLink protocolLink,ObjectReference objectReference) {
        super(protocolLink,objectReference);
    }
    
    public UniversalObject[] getBuffer() throws IOException {
        byte[] responseData = getResponseData(ASSOC_SN_ATTR_OBJ_LST);
        
        // KV_DEBUG
        if (DEBUG>=2) {
            File file = new File("responseData.bin");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(responseData);
            fos.close();
        }
        
        buffer = data2UOL(responseData);
        return buffer;
    }
    
    private UniversalObject[] parseResponseData() {
        if (DEBUG>=1) {
            byte[] data=null;
            try {
                File file = new File("responseData.bin");
                FileInputStream fis = new FileInputStream(file);
                data = new byte[(int)file.length()];
                fis.read(data);
                fis.close();
                
                DataContainer dc = new DataContainer();
                dc.parseObjectList(data,protocolLink.getLogger());
                System.out.println(dc.getText(","));
                //System.out.println(dc.print2strDataContainer());
                //buffer = Data2UOL(data);
            }
            catch(FileNotFoundException e) {
                e.printStackTrace();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
        return buffer;
    }
    
    protected int getClassId() {
        return CLASSID;
    }
    
//    public static void main(String[] args) {
//        
//       ProtocolLink pl = new ProtocolLink() {
//            public DLMSConnection getDLMSConnection() {
//                return null;
//            }
//            public DLMSMeterConfig getMeterConfig() {
//                return null;
//            }
//            public TimeZone getTimeZone() {
//                return null;
//            }
//            public boolean isRequestTimeZone() {
//                return false;
//            }
//            public int getRoundTripCorrection() {
//                return 0;
//            }
//            public java.util.logging.Logger getLogger() {
//                return null;
//            }
//            public int getReference() {
//                return ProtocolLink.LN_REFERENCE;
//            }
//            public StoredValues getStoredValues() {
//                return null;
//            }
//            
//        };        
//        
//        AssociationLN aln = new AssociationLN(pl,null);
//        aln.parseResponseData();
//        
//        
//    }
}
