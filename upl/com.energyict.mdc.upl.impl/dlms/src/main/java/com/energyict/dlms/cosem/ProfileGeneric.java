/*
 * ProfileGeneric.java
 *
 * Created on 17 augustus 2004, 17:17
 */

package com.energyict.dlms.cosem;

import com.energyict.cbo.NestedIOException;
import com.energyict.dlms.DataContainer;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Integer8;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.protocol.ProtocolException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.energyict.dlms.DLMSCOSEMGlobals.*;

/**
 * @author Koen
 */
public class ProfileGeneric extends AbstractCosemObject implements CosemObject {

    private static final int RESET_LN = 1;
    private static final int RESET_SN = 0x58;

    private List<CapturedObject> captureObjects = null; // of type CaptureObject
    private int capturePeriod = -1;
    private int profileEntries = -1;
    private int entriesInUse = -1;

    private byte[] capturedObjectsResponseData = null;
    private byte[] bufferResponseData = null;

    private UniversalObject[] capturedObjects = null;

    /**
     * Checks if caching for profile buffer data is enabled or disabled (enabled by default)
     */
    private boolean profileCaching = true;

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
        byte[] responseData = getBufferResponseData(fromCalendar, toCalendar);
        DataContainer dataContainer = new DataContainer();
        dataContainer.parseObjectList(responseData, protocolLink.getLogger());
        return dataContainer;
    }

    /**
     * Enable this to use the DSMR4.0 selective access range descriptor.
     * The from/to date will contain a specified DOW, specific hundredths of seconds, a specified timezone deviation.
     */
    public void setDsmr4SelectiveAccessFormat(boolean dsmr4SelectiveAccessFormat) {
        this.dsmr4SelectiveAccessFormat = dsmr4SelectiveAccessFormat;
    }

    public DataContainer getBuffer(long fromCalendar, long toCalendar) throws IOException {
        byte[] responseData = getBufferResponseData(fromCalendar, toCalendar);
        DataContainer dataContainer = new DataContainer();
        dataContainer.parseObjectList(responseData, protocolLink.getLogger());
        return dataContainer;
    }

    public DataContainer getBuffer(int fromEntry, int toEntry, int fromValue, int toValue) throws IOException {
        byte[] responseData = getBufferResponseData(fromEntry, toEntry, fromValue, toValue);
        DataContainer dataContainer = new DataContainer();
        dataContainer.parseObjectList(responseData, protocolLink.getLogger());
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

    public byte[] getBufferData(Calendar fromCalendar, Calendar toCalendar, List<CapturedObject> channels) throws IOException {
        return getBufferResponseData(fromCalendar, toCalendar, channels);
    }

    public byte[] getBufferData(int fromEntry, int toEntry, int fromValue, int toValue) throws IOException {
        return getBufferResponseData(fromEntry, toEntry, fromValue, toValue);
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
        if (bufferResponseData == null || !isProfileCaching()) {
            bufferResponseData = getResponseData(PROFILE_GENERIC_BUFFER, fromCalendar, toCalendar);
        }
        return bufferResponseData;
    }

    private byte[] getBufferResponseData(long fromCalendar, long toCalendar) throws IOException {
        if (bufferResponseData == null || !isProfileCaching()) {
            bufferResponseData = getResponseData(PROFILE_GENERIC_BUFFER, fromCalendar, toCalendar);
        }
        return bufferResponseData;
    }

    private byte[] getBufferResponseData(Calendar fromCalendar, Calendar toCalendar, List<CapturedObject> channels) throws IOException {
        if (bufferResponseData == null || !isProfileCaching()) {
            bufferResponseData = getResponseData(PROFILE_GENERIC_BUFFER, fromCalendar, toCalendar, channels);
        }
        return bufferResponseData;
    }

    private byte[] getBufferResponseData(int fromEntry, int toEntry, int fromValue, int toValue) throws IOException {
        if (bufferResponseData == null || !isProfileCaching()) {
            bufferResponseData = getResponseData(PROFILE_GENERIC_BUFFER, fromEntry, toEntry, fromValue, toValue);
        }
        return bufferResponseData;
    }

    /**
     * Method to parse the CapturedObjects as array of UniversalObject<br/>
     * <b>Note:</b> always keep in mind that the returned UniversalObjects are in the form of 'Captured Objects',
     * thus all further operations should only do 'captured objects' operations (e.g.: method #getLNAco instead of #getLNA).
     * When directly accessing the UniversalObjects 'fields' array, this has also to be taken into account (so the correct array positions are used).
     */
    public UniversalObject[] getCaptureObjectsAsUniversalObjects() throws IOException {
        List<UniversalObject> universalObjects = new ArrayList<UniversalObject>();
        for (CapturedObject capturedObject : getCaptureObjects()) {
            universalObjects.add(UniversalObject.createCaptureObject(capturedObject.getClassId(), capturedObject.getObisCode()));
        }

        return universalObjects.toArray(new UniversalObject[universalObjects.size()]);
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
            String msg = "Could not calculate the number of channels";
            if (e.getMessage() != null) {
                msg += ": ";
                msg += e.getMessage();
            }
            throw new NestedIOException(e, msg);
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

    public void reset() throws IOException {
        if(getObjectReference().isLNReference()){
            invoke(RESET_LN, new Integer8(0).getBEREncodedByteArray());
        } else {
            write(RESET_SN, new Integer8(0).getBEREncodedByteArray());
        }
    }

    public ScalerUnit getScalerUnit() throws IOException {
        return null;
    }

    public String getText() throws IOException {
        return getBuffer(null).print2strDataContainer();
    }

    public long getValue() throws IOException {
        throw new ProtocolException("Data, getValue(), invalid data value type...");
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

    /**
     * This method can be used to enable or disable the caching for (and only for) the profile data (the actual buffer).<br>
     * If caching is disabled, all requests for profile data will go straight to the meter.<br>
     * Caching is enabled by default and can be disabled using the {@link ProfileGeneric#setProfileCaching(boolean)} method.
     *
     * @return true if caching is enabled
     * @see ProfileGeneric#setProfileCaching(boolean)
     */
    public final boolean isProfileCaching() {
        return profileCaching;
    }

    /**
     * Enable or disable the caching for the profile data (enabled by default)
     *
     * @param profileCaching True to enable, false to disable
     * @see ProfileGeneric#isProfileCaching()
     */
    public final void setProfileCaching(final boolean profileCaching) {
        this.profileCaching = profileCaching;
    }

}
