/*
 * ProfileGeneric.java
 *
 * Created on 17 augustus 2004, 17:17
 */

package com.energyict.dlms.cosem;

import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.*;

import java.io.*;
import java.util.*;

/**
 * @author Koen
 */
public class ProfileGeneric extends AbstractCosemObject implements CosemObject {

    private final int DEBUG = 0;
    private List<CapturedObject> captureObjects = null; // of type CaptureObject
    private int capturePeriod = -1;
    private int profileEntries = -1;
    private int entriesInUse = -1;

    private byte[] capturedObjectsResponseData = null;
    private byte[] bufferResponseData = null;

    private UniversalObject[] capturedObjects = null;

    /**
     * Creates a new instance of ProfileGeneric
     */
    public ProfileGeneric(ProtocolLink protocolLink, ObjectReference objectReference) {
        super(protocolLink, objectReference);
    }

    public DataContainer getBuffer() throws IOException {
        return getBuffer(null, null);
    }

    /**
     * Getter for property buffer.
     *
     * @return Value of property buffer.
     */
    public DataContainer getBuffer(Calendar fromCalendar) throws IOException {
        return getBuffer(fromCalendar, null);
    }

    public DataContainer getBuffer(Calendar fromCalendar, Calendar toCalendar) throws IOException {
        DataContainer dataContainer = new DataContainer();
        byte[] responseData = getBufferResponseData(fromCalendar, toCalendar);

        // KV_DEBUG
        if (DEBUG >= 2) {
            File file = new File("responseData.bin");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(responseData);
            fos.close();
        }

        dataContainer.parseObjectList(responseData, protocolLink.getLogger());
        if (DEBUG >= 1) {
            dataContainer.printDataContainer();
        }
        return dataContainer;
    }

    public byte[] getBufferData() throws IOException {
        return getBufferData(null, null);
    }

    public byte[] getBufferData(Calendar fromCalendar) throws IOException {
        return getBufferData(fromCalendar, null);
    }

    public byte[] getBufferData(Calendar fromCalendar, Calendar toCalendar) throws IOException {
        return getBufferResponseData(fromCalendar, toCalendar);
    }

    public UniversalObject[] getBufferAsUniversalObjects() throws IOException {
        return getBufferAsUniversalObjects(null, null);
    }

    /**
     * Getter for property buffer.
     *
     * @return Value of property buffer.
     */
    public UniversalObject[] getBufferAsUniversalObjects(Calendar fromCalendar, Calendar toCalendar) throws IOException {
        return data2UOL(getBufferResponseData(fromCalendar, toCalendar));
    }

    private byte[] getBufferResponseData(Calendar fromCalendar, Calendar toCalendar) throws IOException {
        if (bufferResponseData == null) {
            bufferResponseData = getResponseData(PROFILE_GENERIC_BUFFER, fromCalendar, toCalendar);
        }
        return bufferResponseData;
    }

    public UniversalObject[] getCaptureObjectsAsUniversalObjects() throws IOException {
        if (capturedObjects == null) {
            capturedObjects = data2UOL(getCapturedObjectsResponseData());
        }
        return capturedObjects;
    }

    public DataContainer getCaptureObjectsAsDataContainer() throws IOException {
        DataContainer dataContainer = new DataContainer();
        dataContainer.parseObjectList(getCapturedObjectsResponseData(), protocolLink.getLogger());
        return dataContainer;
    }

    /**
     * Getter for property captureObjects.
     *
     * @return Value of property captureObjects.
     */
    public List<CapturedObject> getCaptureObjects() throws IOException {
        if (captureObjects == null) {
            DataContainer dataContainer = new DataContainer();
            dataContainer.parseObjectList(getCapturedObjectsResponseData(), protocolLink.getLogger());

            if (DEBUG >= 1) {
                dataContainer.printDataContainer();
            }
            getCapturedObjectsFromDataContainter(dataContainer);
        }
        return captureObjects;
    }

    /**
     * Create the CapturedObjectList from the given DataContainer
     *
     * @param dataContainer the dataContainer containing the information about the capturedObjects
     * @return a List of <CODE>CapturedObject</CODE>
     */
    public List<CapturedObject> getCapturedObjectsFromDataContainter(DataContainer dataContainer) {
        // translate dataContainer into list of captureobjects
        this.captureObjects = new ArrayList<CapturedObject>();

        for (int index = 0; index < dataContainer.getRoot().getNrOfElements(); index++) {
            if (dataContainer.getRoot().isStructure(index)) {
                DataStructure dataStructure = dataContainer.getRoot().getStructure(index);
                final int classId = dataStructure.getInteger(0);
                final LogicalName logicalName = new LogicalName(dataStructure.getOctetString(1));
                final int attributeIndex = dataStructure.getInteger(2);
                final int dataIndex = dataStructure.getInteger(3);

                this.captureObjects.add(new CapturedObject(classId, logicalName, attributeIndex, dataIndex));
            }
        }
        return this.captureObjects;
    }

    public CapturedObjectsHelper getCaptureObjectsHelper() throws IOException {
        return new CapturedObjectsHelper(getCaptureObjects());
    }

    /**
     * Check whether the generic profile has already read his captured objects
     *
     * @return
     */
    public boolean containsCapturedObjects() {
        return this.captureObjects == null ? false : true;
    }

    public int getNumberOfProfileChannels() throws IOException {
        int count = 0;
        try {
            for (int i = 0; i < getCaptureObjectsAsUniversalObjects().length; i++) {
                if (getCaptureObjectsAsUniversalObjects()[i].isCapturedObjectNotAbstract()) {
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
        if (capturedObjectsResponseData == null) {
            capturedObjectsResponseData = getResponseData(PROFILE_GENERIC_CAPTURE_OBJECTS);
        }
        return capturedObjectsResponseData;
    }

    /**
     * Getter for property capturePeriod.
     *
     * @return Value of property capturePeriod.
     */
    public int getCapturePeriod() throws IOException {
        if (capturePeriod == -1) {
            capturePeriod = (int) getLongData(PROFILE_GENERIC_CAPTURE_PERIOD);
        }
        return capturePeriod;
    }

    /**
     * Getter for property profileEntries.
     *
     * @return Value of property profileEntries.
     */
    public int getProfileEntries() throws IOException {
        if (profileEntries == -1) {
            profileEntries = (int) getLongData(PROFILE_GENERIC_PROFILE_ENTRIES);
        }
        return profileEntries;
    }

    public int getEntriesInUse() throws IOException {
        if (entriesInUse == -1) {
            entriesInUse = (int) getLongData(PROFILE_GENERIC_ENTRIES_IN_USE);
        }
        return entriesInUse;
    }

    protected int getClassId() {
        return DLMSClassId.PROFILE_GENERIC.getClassId();
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
        if (DEBUG >= 1) {
            byte[] data = null;
            try {
                File file = new File("responseData.bin");
                FileInputStream fis = new FileInputStream(file);
                data = new byte[(int) file.length()];
                fis.read(data);
                fis.close();

                DataContainer dc = new DataContainer();
                dc.parseObjectList(data, protocolLink.getLogger());
                //dc.printDataContainer();
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] getData(int attr, Calendar from, Calendar to) throws IOException {
        return getLNResponseData(attr, from, to);
    }

    public byte[] getData(int attr) throws IOException {
        return getLNResponseData(attr);
    }

    public Array readBufferAttr() throws IOException {
        return AXDRDecoder.decode(getLNResponseData(2)).getArray();
    }

    public Array readBufferAttr(Calendar from, Calendar to) throws IOException {
        return AXDRDecoder.decode(getLNResponseData(2, from, to)).getArray();
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

} // public class ProfileGeneric
