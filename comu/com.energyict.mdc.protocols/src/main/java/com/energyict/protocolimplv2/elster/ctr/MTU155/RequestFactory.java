/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155;

import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AttributeType;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRConnectionException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Address;
import com.energyict.protocolimplv2.elster.ctr.MTU155.frame.field.Data;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.ArrayEventsQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.ArrayQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.IdentificationResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.TableDECFQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.TableDECQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.Trace_CQueryResponseStructure;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.CIA;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Counter_Q;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Identify;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Index_Q;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.NumberOfElements;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.P_Session;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.PeriodTrace;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.PeriodTrace_C;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.ReferenceDate;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.Segment;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.StartDate;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.VF;
import com.energyict.protocolimplv2.elster.ctr.MTU155.structure.field.WriteDataBlock;
import com.energyict.protocolimplv2.elster.ctr.MTU155.util.GasQuality;
import com.energyict.protocolimplv2.elster.ctr.MTU155.util.MeterInfo;

import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

public interface RequestFactory {

    /**
     * Try to get a number of objects from the meter, by using/combining different sources.<br></br>
     * The different sources used:
     * <ul>
     *     <li>Local object cache</li>
     *     <li>The Identification table</li>
     *     <li>The DECF table</li>
     *     <li>The DEC table</li>
     *     <li>Query against multiple register structure</li>
     * </ul>
     *
     * @param objectIDs: the CTR Object's ID's
     * @return a list of CTR objects
     * @throws CTRConnectionException
     */
    public List<AbstractCTRObject> getObjects(String... objectIDs) throws CTRConnectionException;

    /**
     * Try to get a number of objects from the meter, by using/combining different sources.<br></br>
     * The different sources used:
     * <ul>
     *     <li>Local object cache</li>
     *     <li>The Identification table</li>
     *     <li>The DECF table</li>
     *     <li>The DEC table</li>
     *     <li>Query against multiple register structure</li>
     * </ul>
     *
     * @param objectIDs: the CTR Object's ID's
     * @return a list of CTR objects
     * @throws CTRConnectionException
     */
    public List<AbstractCTRObject> getObjects(CTRObjectID... objectIDs) throws CTRConnectionException;

    public AbstractCTRObject queryRegister(String objectID) throws CTRException;

    public List<AbstractCTRObject> queryRegisters(AttributeType attributeType, String... objectId) throws CTRException;

    public List<AbstractCTRObject> queryRegisters(CTRObjectID... objectId) throws CTRException;

    /**
     * Returns a list of requested objects
     * @param attributeType: determines the fields of the objects
     * @param objectId: the id's of the requested objects
     * @return list of requested objects
     * @throws com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRConnectionException
     */
    public List<AbstractCTRObject> queryRegisters(AttributeType attributeType, CTRObjectID... objectId) throws CTRException;

    /**
     * Queries for a decf table. Returns the meter response.
     * @return the meter response, being a decf table
     * @throws CTRException, if the meter's answer was not a decf table
     */
    public TableDECFQueryResponseStructure queryTableDECF() throws CTRException;

    /**
     * Queries for a dec table. Returns the meter response.
     * @return the meter response, being a dec table
     * @throws CTRException, if the meter's answer was not a dec table
     */
    public TableDECQueryResponseStructure queryTableDEC() throws CTRException;

    /**
     * Executes a certain request
     * @param validityDate: the validity date
     * @param wdb: a unique number
     * @param id: the object's id
     * @param data: data for the execute request
     * @return the meter's response (ack or nack)
     * @throws CTRException, when the meter's response was unexpected.
     */
    public Data executeRequest(ReferenceDate validityDate, WriteDataBlock wdb, CTRObjectID id, byte[] data) throws CTRException;

    public Data executeRequest(CTRObjectID id, byte[] data) throws CTRException;

    /**
     * Writes to register(s) in the meter
     *
     * @param validityDate:  the validity date
     * @param wdb:           a unique number
     * @param p_Session:     the session configuration
     * @param attributeType: determines the fields of the objects
     * @param objects:       the objects that should be written in the meter
     * @throws CTRException, if the meter's response was not recognized
     * @return the meter's response (ack or nack)
     */
    public Data writeRegister(ReferenceDate validityDate, WriteDataBlock wdb, P_Session p_Session, AttributeType attributeType, AbstractCTRObject... objects) throws CTRException;

    /**
     *
     * @param attributeType: determines the fields of the objects
     * @param numberOfObjects: the number of objects to write to the device
     * @param rawData: the raw data containing the objects to write
     * @return the meter's response (ack or nack)
     * @throws CTRException, if the meter's response was not recognized
     */
    public Data writeRegister(AttributeType attributeType, int numberOfObjects, byte[] rawData) throws CTRException;

     /**
     * Writes to register(s) in the meter, using the default and most used write parameters
     * DV = today + 14 days
     * WDB = random
     * PSession = 0x00 (open & close)
     * AttributeType = Value and ObjectID
     * @param objects: the objects that should be written in the meter
     * @return the meter's response (ack or nack)
     * @throws CTRException, if the meter's response was not recognized
     */
    public Data writeRegister(AbstractCTRObject... objects) throws CTRException;

    /**
     * Do a trace query
     *
     * @param id:               the id of the object you need interval data from
     * @param period:           the interval period (e.g.: 15 minutes)
     * @param startDate:        the start date
     * @param numberOfElements: the number of interval values returned
     * @throws CTRException, when the meter's response was not recognized
     * @return a list of objects
     */
    public List<AbstractCTRObject> queryTrace(CTRObjectID id, PeriodTrace period, StartDate startDate, NumberOfElements numberOfElements) throws CTRException;

    /**
     * Queries for a number of events
     * @param index_Q: number of events to query for
     * @return the meter's response containing event records
     * @throws CTRException, if the meter's response was not recognized
     */
    public ArrayEventsQueryResponseStructure queryEventArray(int index_Q) throws CTRException;

    /**
     * Queries for a number of events
     * @param index_Q: number of events to query for
     * @return the meter's response containing event records
     * @throws CTRException, if the meter's response was not recognized
     */
    public ArrayEventsQueryResponseStructure queryEventArray(Index_Q index_Q) throws CTRException;

    /**
     * Read elements from a logic vector
     * @param objectID  the object to which the array type structure is associated (each vector element is of this type)
     * @param index_q   index of the first object to read
     * @param counter_q Number of elements to read
     * @return the meter's response containing a number of vector elements.
     * @throws CTRException, if the meter's response was not recognized
     */
    public ArrayQueryResponseStructure queryArray(CTRObjectID objectID, Index_Q index_q, Counter_Q counter_q) throws CTRException;

    /**
     * Do a trace_C query
     * @param id: the id of the requested object
     * @param period: the interval period
     * @param referenceDate: the reference date
     * @return the meter's response
     * @throws CTRException, if the meter's response was not recognized
     */
    public Trace_CQueryResponseStructure queryTrace_C(CTRObjectID id, PeriodTrace_C period, ReferenceDate referenceDate) throws CTRException;

    /**
     * Send an end of session to the device
     */
    public void sendEndOfSession();


    /** ****** SECTION 'FIRMWARE UPGRADING' ****** **/
    /**
     * Abort any previous ongoing download + initialize all download parameters with info of the new firmware image.
     * @param newSoftwareIdentifier Firmware version of the image
     * @param cia                   Type of device used with
     * @param vf                    Manufacturer's software version
     * @param activationDate        Activation date of the image
     * @param size                  Total number of bytes of the image
     * @return
     * @throws CTRException
     */
    public void doInitFirmwareUpgrade(Identify newSoftwareIdentifier, CIA cia, VF vf,Calendar activationDate, int size, boolean useLongFrameFormat) throws CTRException;

     /**
     * Send out 1 segment of the firmware image code. The response is analysed to see if the segment gets acked.
     **/
   public boolean doSendFirmwareSegment(Identify newSoftwareIdentifier, byte[] firmwareUpgradeFile, Segment lastAckedSegment, boolean useLongFrameFormat) throws CTRException;
    /** ****** END OF SECTION 'FIRMWARE UPGRADING' ****** **/

    public CtrConnection getConnection();

     public MTU155Properties getProperties() ;

    public MeterInfo getMeterInfo();

    public void setMeterInfo(MeterInfo meterInfo);

     public Address getAddress() ;

    public TimeZone getTimeZone();

    public WriteDataBlock getNewWriteDataBlock();

    public int getWriteDataBlockID();

    public Logger getLogger();

    public String getIPAddress();

    public boolean isEK155Protocol();

    public IdentificationResponseStructure getIdentificationStructure();

    /**
     * Return the cached DECF table, or read it from the device
     *
     * @return the meter's decf table
     * @throws CTRException, when the meter's response was unexpected
     */
    public TableDECFQueryResponseStructure getTableDECF() throws CTRException;

    /**
     * Return the cached DEC table, or read it from the device
     *
     * @return the meter's dec table
     * @throws CTRException, when the meter's response was unexpected
     */
    public TableDECQueryResponseStructure getTableDEC() throws CTRException;

    /**
     * Get the cached GasQuality object. If not exist yet, create a new one.
     *
     * @return
     */
    public GasQuality getGasQuality() throws CTRException;

}