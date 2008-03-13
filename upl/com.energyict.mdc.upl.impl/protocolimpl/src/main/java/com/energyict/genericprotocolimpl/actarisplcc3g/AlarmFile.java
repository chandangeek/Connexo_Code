/*
 * AlarmFile.java
 *
 * Created on 10 januari 2008, 10:10
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.genericprotocolimpl.actarisplcc3g;

import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.axrdencoding.util.*;
import java.util.*;
import java.io.*;


/**
 *
 * @author kvds
 */
public class AlarmFile {
    
    byte[] data;
    TimeZone timeZone;
            
    /** Creates a new instance of AlarmFile */
    public AlarmFile(byte[] data,TimeZone timeZone) {
        this.data=data;
        this.timeZone=timeZone;
    }
    
    public List toAlarmEntries() throws IOException {
        List alarmEntries = new ArrayList();
        
        AbstractDataType o = AXDRDecoder.decode(data);
        if (o instanceof Array) {

            Array array = o.getArray();
            int nrOfEntries = array.nrOfDataTypes();
            for(int index=0; index<nrOfEntries; index++) {
                Structure structure = array.getDataType(index).getStructure();
                DateTime dateTime = new DateTime(structure.getDataType(0).getBEREncodedByteArray(), 0, timeZone);
                String serialNumber = structure.getDataType(1).getVisibleString().getStr().trim();
                int id = structure.getDataType(2).getUnsigned16().getValue();
                alarmEntries.add(new AlarmEntry(dateTime.getValue().getTime(), serialNumber, id));
            }
            return alarmEntries;
        }
        else throw new IOException("AlarmFile, toAlarmEntries, no array object!");
        
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try {
//            Array array = new Array();
//            Structure structure = new Structure();
//            
//            DateTime dateTime = new DateTime();
//            dateTime.setValue(Calendar.getInstance());
//            structure.addDataType(dateTime);
//            structure.addDataType(new VisibleString("alarm1",20));
//            structure.addDataType(new Unsigned16(1));
//            array.addDataType(structure);
//            structure = new Structure();
//            structure.addDataType(dateTime);
//            structure.addDataType(new VisibleString("alarm2",20));
//            structure.addDataType(new Unsigned16(2));            
//            array.addDataType(structure);
//            structure = new Structure();
//            structure.addDataType(dateTime);
//            structure.addDataType(new VisibleString("alarm3",20));
//            structure.addDataType(new Unsigned16(3));  
//            array.addDataType(structure);
//            byte[] data =  array.getBEREncodedByteArray();
//            File file = new File("alarmTest");
//            FileOutputStream fos = new FileOutputStream(file);
//            fos.write(data);
//            fos.close();
            
            
            File file = new File("alarmTest");
            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int)file.length()];
            fis.read(data);
            fis.close();
            AlarmFile o = new AlarmFile(data,TimeZone.getTimeZone("ECT"));
            List entries = o.toAlarmEntries();
            Iterator it = entries.iterator();
            while(it.hasNext()) {
                System.out.println(it.next());
            }
            
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        
    }
    
}
