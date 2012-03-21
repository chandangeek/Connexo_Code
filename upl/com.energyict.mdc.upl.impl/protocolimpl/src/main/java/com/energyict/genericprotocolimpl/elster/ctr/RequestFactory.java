package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Address;
import com.energyict.genericprotocolimpl.elster.ctr.frame.field.Data;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.structure.*;
import com.energyict.genericprotocolimpl.elster.ctr.structure.field.*;
import com.energyict.genericprotocolimpl.elster.ctr.util.GasQuality;
import com.energyict.genericprotocolimpl.elster.ctr.util.MeterInfo;

import java.util.*;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 8/03/12
 * Time: 15:01
 */
public interface RequestFactory {

    /**
     * Requests a number of objects from the meter, via a query.
     * @param attributeType: determines the fields of the objects in the meter's response
     * @param objectId: the id's of the requested objects
     * @return a list of objects, as requested
     * @throws CTRException
     */
    public List<AbstractCTRObject> queryRegisters(AttributeType attributeType, String... objectId) throws CTRException;

    public AbstractCTRObject queryRegister(String objectID) throws CTRException;

    /**
     * Returns a list of requested objects
     * @param attributeType: determines the fields of the objects
     * @param objectId: the id's of the requested objects
     * @return list of requested objects
     * @throws com.energyict.genericprotocolimpl.elster.ctr.exception.CTRConnectionException
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
     * @return: the meter's response (ack or nack)
     */
    public Data writeRegister(ReferenceDate validityDate, WriteDataBlock wdb, P_Session p_Session, AttributeType attributeType, AbstractCTRObject... objects) throws CTRException;

    /**
     *
     * @param attributeType: determines the fields of the objects
     * @param numberOfObjects: the number of objects to write to the device
     * @param rawData: the raw data containing the objects to write
     * @return: the meter's response (ack or nack)
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
     * @return: the meter's response (ack or nack)
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
     * @return: a list of objects
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
     * @param activationDate        Activation date of the image
     * @param size                  Total number of bytes of the image
     * @return
     * @throws CTRException
     */
    public void doInitFirmwareUpgrade(Identify newSoftwareIdentifier, Calendar activationDate, int size) throws CTRException;

     /**
     * Send out 1 segment of the firmware image code. The response is analysed to see if the segment gets acked.
     **/
   public boolean doSendFirmwareSegment(Identify newSoftwareIdentifier, byte[] firmwareUpgradeFile, Segment lastAckedSegment) throws CTRException;
    /** ****** END OF SECTION 'FIRMWARE UPGRADING' ****** **/

    public CtrConnection getConnection();

     public MTU155Properties getProperties() ;

    public MeterInfo getMeterInfo();

     public Address getAddress() ;

    public TimeZone getTimeZone();

    public Logger getLogger();

    public IdentificationResponseStructure getIdentificationStructure();

    public String getIPAddress();

    /**
     * Get the cached GasQuality object. If not exist yet, create a new one.
     *
     * @return
     */
    public GasQuality getGasQuality() throws CTRException;
}