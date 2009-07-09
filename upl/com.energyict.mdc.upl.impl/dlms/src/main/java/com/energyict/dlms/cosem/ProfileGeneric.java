/*
 * ProfileGeneric.java
 *
 * Created on 17 augustus 2004, 17:17
 */

package com.energyict.dlms.cosem;

import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Unsigned32;
import java.io.*;
import java.util.*;

import com.energyict.dlms.*;
import com.energyict.protocolimpl.dlms.*;
import com.energyict.protocol.*;
import com.energyict.dlms.cosem.AbstractCosemObject;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.CosemObject;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.UniversalObject;

/**
 *
 * @author  Koen
 */
public class ProfileGeneric extends AbstractCosemObject implements CosemObject {
    public final int DEBUG=0;
    static public final int CLASSID=7;
        
    List captureObjects=null; // of type CaptureObject
    int capturePeriod=-1;
    int profileEntries=-1;
    int entriesInUse=-1;
    
    byte[] capturedObjectsResponseData=null;
    byte[] bufferResponseData=null;
    
    /** Creates a new instance of ProfileGeneric */
    public ProfileGeneric(ProtocolLink protocolLink,ObjectReference objectReference) {
        super(protocolLink,objectReference);
    }
  
    
    public DataContainer getBuffer() throws IOException {
        return getBuffer(null,null);
    }
    
    /**
     * Getter for property buffer.
     * @return Value of property buffer.
     */
    public DataContainer getBuffer(Calendar fromCalendar) throws IOException {
        return getBuffer(fromCalendar,null);
    }
    public DataContainer getBuffer(Calendar fromCalendar,Calendar toCalendar) throws IOException {
        DataContainer dataContainer = new DataContainer();
        byte[] responseData = getBufferResponseData(fromCalendar,toCalendar);
        
        // KV_DEBUG
        if (DEBUG>=2) {
            File file = new File("responseData.bin");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(responseData);
            fos.close();
        }
        
        dataContainer.parseObjectList(responseData,protocolLink.getLogger());
        if (DEBUG >= 1) dataContainer.printDataContainer();
        return dataContainer;
    }

    public byte[] getBufferData() throws IOException {
        return getBufferData(null,null);
    }
    public byte[] getBufferData(Calendar fromCalendar) throws IOException {
        return getBufferData(fromCalendar,null);
    }
    public byte[] getBufferData(Calendar fromCalendar,Calendar toCalendar) throws IOException {
        byte[] responseData = getBufferResponseData(fromCalendar,toCalendar);
        return responseData;
    }
    
    public UniversalObject[] getBufferAsUniversalObjects() throws IOException {
        return getBufferAsUniversalObjects(null,null);
    }
    
    /**
     * Getter for property buffer.
     * @return Value of property buffer.
     */
    public UniversalObject[] getBufferAsUniversalObjects(Calendar fromCalendar,Calendar toCalendar) throws IOException {
        return data2UOL(getBufferResponseData(fromCalendar,toCalendar));
    }
    private byte[] getBufferResponseData(Calendar fromCalendar,Calendar toCalendar) throws IOException {
        if (bufferResponseData==null)
            bufferResponseData = getResponseData(PROFILE_GENERIC_BUFFER,fromCalendar,toCalendar);
        return bufferResponseData;
    }
    
    public UniversalObject[] getCaptureObjectsAsUniversalObjects() throws IOException {
        return data2UOL(getCapturedObjectsResponseData());
    }
    public DataContainer getCaptureObjectsAsDataContainer() throws IOException {
        DataContainer dataContainer = new DataContainer();
        dataContainer.parseObjectList(getCapturedObjectsResponseData(),protocolLink.getLogger());
        return dataContainer;
    }

    /**
     * Getter for property captureObjects.
     * @return Value of property captureObjects.
     */
    public java.util.List getCaptureObjects() throws IOException {
        if (captureObjects == null) {
            DataContainer dataContainer = new DataContainer();
            dataContainer.parseObjectList(getCapturedObjectsResponseData(),protocolLink.getLogger());
            
            if (DEBUG >= 1) dataContainer.printDataContainer();
            
            // translate dataContainer into list of captureobjects
            this.captureObjects = new ArrayList();
            
            for (int index=0;index<dataContainer.getRoot().getNrOfElements();index++) {
                if (dataContainer.getRoot().isStructure(index)) {
                    DataStructure dataStructure = dataContainer.getRoot().getStructure(index);
                    final int classId = dataStructure.getInteger(0);
                    final LogicalName logicalName = new LogicalName(dataStructure.getOctetString(1));
                    final int attributeIndex = dataStructure.getInteger(2);
                    final int  dataIndex = dataStructure.getInteger(3);
                    
                    this.captureObjects.add(new CapturedObject(classId,logicalName,attributeIndex,dataIndex));
                }
            }
        }
        
        return captureObjects;
    }
    
    public CapturedObjectsHelper getCaptureObjectsHelper() throws IOException {
    	return new CapturedObjectsHelper(getCaptureObjects());
    }
    
    /**
     * Check whether the generic profile has already read his captured objects
     * @return
     */
    public boolean containsCapturedObjects(){
    	return this.captureObjects==null?false:true;
    }
    
    public int getNumberOfProfileChannels() throws IOException{
    	int count = 0;
    	try {
			for(int i = 0; i < getCaptureObjectsAsUniversalObjects().length; i++){
				if(getCaptureObjectsAsUniversalObjects()[i].isCapturedObjectNotAbstract()){
					count++;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException("Could not calculate the number of channgels");
		}
		return count;
    }
    
    private byte[] getCapturedObjectsResponseData() throws IOException {
        if (capturedObjectsResponseData == null)
             capturedObjectsResponseData = getResponseData(PROFILE_GENERIC_CAPTURE_OBJECTS);
        return capturedObjectsResponseData;        
    }
 
    /**
     * Getter for property capturePeriod.
     * @return Value of property capturePeriod.
     */
    public int getCapturePeriod() throws IOException {
        if (capturePeriod == -1) {
            capturePeriod = (int)getLongData(PROFILE_GENERIC_CAPTURE_PERIOD);
        }
        return capturePeriod;
    }
    
    /**
     * Getter for property profileEntries.
     * @return Value of property profileEntries.
     */
    public int getProfileEntries() throws IOException {
        if (profileEntries==-1) {
           profileEntries = (int)getLongData(PROFILE_GENERIC_PROFILE_ENTRIES);
        }
        return profileEntries;
    }
 
    public int getEntriesInUse() throws IOException {
        if (entriesInUse==-1) {
        	entriesInUse = (int)getLongData(PROFILE_GENERIC_ENTRIES_IN_USE);
         }
         return entriesInUse;
	}
    
    protected int getClassId() {
        return CLASSID;
    }
    
    public Date getBillingDate() {
        return null;
    }    

    public Date getCaptureTime() throws IOException {
        return null;
    }    
    
    public com.energyict.cbo.Quantity getQuantityValue() throws IOException {
        return null;
    }
    
    public int getResetCounter() {
        return 0;
    }
    
    public ScalerUnit getScalerUnit() throws IOException {
        return null;
    }
    
    public String getText() throws IOException {
        return getBuffer(null).print2strDataContainer();
    }
    
    public long getValue() throws IOException {
        throw new IOException("Data, getValue(), invalid data value type...");
    }

    private void parseResponseData() {
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
                //dc.printDataContainer();
            }
            catch(FileNotFoundException e) {
                e.printStackTrace();
            }
            catch(IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public byte[] getData(int attr,Calendar from, Calendar to) throws IOException {
        return getLNResponseData(attr,from,to);
    }
    public byte[] getData(int attr) throws IOException {
        return getLNResponseData(attr);
    }    
    
    public Array readBufferAttr() throws IOException {
        return AXDRDecoder.decode(getLNResponseData(2)).getArray();
    }
    public Array readBufferAttr(Calendar from, Calendar to) throws IOException {
        return AXDRDecoder.decode(getLNResponseData(2,from,to)).getArray();
    }
    public Array readCaptureObjectsAttr() throws IOException {
        return AXDRDecoder.decode(getLNResponseData(3)).getArray();
    }
    public Unsigned32 readCapturePeriodAttr() throws IOException {
        return AXDRDecoder.decode(getLNResponseData(4)).getUnsigned32();
    }
    public void writeCapturePeriodAttr(Unsigned32 val) throws IOException {
        write(4, val.getBEREncodedByteArray());
    }
    
    public void setBufferAttr(Array val) throws IOException {
        write(2, val.getBEREncodedByteArray());
    }
    public void setCaptureObjectsAttr(Array val) throws IOException {
        write(3, val.getBEREncodedByteArray());
    }
    public void setCapturePeriodAttr(Unsigned32 val) throws IOException {
        write(4, val.getBEREncodedByteArray());
    }
    
    
    public static void main(String[] args){
//    	0, 0, 0, -60, 2, -127, 0, 0, 0, 0, 1, 
    	byte[] response = new byte[]{ 0, -126, 1, -58, 1, -126, 2, 2, 7, -40, 11, 11, 4, 20, 4, 44, 0, 0, 0, 0, 0, 0, 0, 13, 0, 0, 0, 0, 2, 12, 9, 5, 7, -40, 11, 11, 4, 9, 5, 20, 4, 44, 0, 0, 9, 5, 0, 0, 0, 0, 0, 9, 5, 13, 0, 0, 0, 0, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 2, 12, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 2, 12, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 2, 12, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 2, 12, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2, 9, 5, 2, 2, 2, 2, 2};
    	DataContainer dc = new DataContainer();
    	try {
			dc.parseObjectList(response, null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	dc.printDataContainer();
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
//        ProfileGeneric pg = new ProfileGeneric(pl,null);
//        pg.parseResponseData();
//    }    
} // public class ProfileGeneric
