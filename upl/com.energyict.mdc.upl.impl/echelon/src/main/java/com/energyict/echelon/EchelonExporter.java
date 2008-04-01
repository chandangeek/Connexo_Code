package com.energyict.echelon;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.cpo.Environment;
import com.energyict.cpo.Transaction;
import com.energyict.echelon.Constants.DeviceResultTypes;
import com.energyict.eisexport.core.AbstractExporter;
import com.energyict.mdw.core.Group;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.core.RtuMessage;
import com.energyict.mdw.shadow.RtuMessageShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.MeterReadingData;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.mbus.core.CIField72h;
import com.energyict.protocolimpl.mbus.core.DataRecord;
import com.energyict.protocolimpl.mbus.core.ValueInformationfieldCoding;
import com.energyict.xml.xmlhelper.DomHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;

/**
 * The Echelon RTU's who's data is to be imported, are in a Group. The members
 * of the Group are enumerated and for every member the NES system is queried.
 * <p/>
 * The members of the Group are uniquely identified by the NeuronId.
 * <p/>
 * The Rtu.lastReading property is used to ensure that readings are only
 * imported once.
 * <p/>
 * Transactional integrity: all store-operations are bundled into 1 transaction
 * per meter.
 *
 * @author fbo
 */

public class EchelonExporter extends AbstractExporter {

    /**
     * property name of debug flag
     */
    private final String DBG = "dbg";
    /**
     * property name of group
     */
    private final String GROUP = "group";
    /**
     * property name of server URI
     */
    private final String URI = "uri";

    private boolean debug;
    private String serverUri;
    private Group group;

    private String error[] = {
            "Unable to run exporter, missing parameter \"" + URI + "\"",
            "NES returned invalid message",
            "Exception occured during export",
            "Echelon gateway not defined for meter %s"
    };

    protected void preExport() throws IOException, BusinessException, SQLException {

        System.setProperty("entityExpansionLimit", "100000");

        debug = getProperty(DBG) != null;

        serverUri = getProperty(URI);
        if (serverUri == null)
            throw new BusinessException(error[0]);
    }


    protected void export() throws IOException, BusinessException, SQLException {
        String sessionId;

        List<String> concentrators = new ArrayList<String>();

        try {
            sessionId = EchelonSession.getInstance(serverUri).getSession();
        } catch (EchelonException ex) {
            throw new IOException("Retrieving a session key from NES failed.");
        }

        Iterator i = getGroup().getMembers().iterator();
        Rtu rtu;
        Rtu gateway;
        while (i.hasNext()) {

            rtu = (Rtu) i.next();
            gateway = (Rtu) rtu.getGateway();
            if (gateway == null || gateway.getDeviceId() == null || gateway.getDeviceId() == "") {
                throw new BusinessException(String.format(error[3], rtu.getName()));
            }

            // don't add a concentrator twice and don't mistaken a meter for a concentrator in case of an M-Bus RTU
            if (!concentrators.contains(gateway.getDeviceId()) &&
                    gateway.getGateway() == null) {
                concentrators.add(rtu.getGateway().getDeviceId());
            }

            try {

                debug("Handling " + rtu.getFullName());

                checkSentMessages(rtu, sessionId);
                exportRtuMessages(rtu, sessionId);

                debug("Echelon exporter: handling readings");

                Document d = fetchResultList(rtu, is24HoursAgo(rtu.getLastReading()), sessionId);

                if (isSuccess(d)) {
                    Environment.getDefault().execute(new StoreTransaction(rtu, d, sessionId));
                } else {
                    getLogger().severe(error[1] + " " + getStatus(d));
                }

            } catch (Throwable e) {
                getLogger().log(Level.SEVERE, error[2] + e.getMessage(), e);
                e.printStackTrace();
            }

        }

        try {
            for (String concentrator : concentrators) {
                EchelonSession.getInstance(serverUri).connect(concentrator, sessionId);
            }
        } catch (EchelonException e) {
            getLogger().log(Level.SEVERE, error[2] + e.getMessage(), e);
            e.printStackTrace();
        }

    }

    class StoreTransaction implements Transaction {

        Rtu rtu;
        Document document;
        String sessionId;

        StoreTransaction(Rtu rtu, Document d, String sessionId) {
            this.rtu = rtu;
            this.document = d;
            this.sessionId = sessionId;
        }

        public Object doExecute() throws BusinessException, SQLException {
            try {
                ResultId[] ids = toResultId(document);
                for (int j = 0; j < ids.length; j++) {
                    store(fetchResult(ids[j], sessionId), rtu);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new BusinessException(e);
            }
            return null;
        }

    }

    /**
     * persist/log debug messages
     */
    private void debug(String msg) {
        if (debug) {
            getLogger().info(msg);
            System.out.println(msg);
        }

    }

    /**
     * fetch list of Results (=Load Profiles)
     */
    private Document fetchResultList(Rtu rtu, boolean fetchDeltaProfile, String sessionId)
            throws RemoteException, ServiceException, Exception, Throwable {

        String query = resultListQuery(rtu, fetchDeltaProfile);
        return EchelonSession.getInstance(serverUri).retrieveResultList(query, sessionId);

    }

    /**
     * fetch Result document
     */
    private Document fetchResult(ResultId id, String sessionId) throws RemoteException, ServiceException, Exception {
        Document doc = EchelonSession.getInstance(serverUri).retrieveResult(id.id, sessionId);
        return doc;
    }

    private void checkSentMessages(Rtu rtu, String sessionId) throws Throwable {

        if (getDeviceId(rtu) == null) return;

        Iterator mi = rtu.getSentMessages().iterator();
        while (mi.hasNext()) {

            RtuMessage msg = (RtuMessage) mi.next();
            if (msg.getTrackingId() == null) continue;

            String taskId = msg.getTrackingId();

            Document r = EchelonSession.getInstance(serverUri).retrieveCommandHistory(taskId, sessionId);
            Element e = Util.xPath(r, "//STATUSTYPEID");
            String taSkStatus = e.getFirstChild().getNodeValue();

            debug(msg.getContents() + ": " + Util.getStatusDescription(taSkStatus));

            if (Constants.CommandHistoryStatus.SUCCESS.equals(taSkStatus))
                msg.confirm();

            if (Constants.CommandHistoryStatus.FAILURE.equals(taSkStatus))
                msg.setFailed();
        }
    }

    /**
     * send translate RtuMessage to the Ness service
     */
    private void exportRtuMessages(Rtu rtu, String sessionId) throws Throwable {

        if (getDeviceId(rtu) == null) return;

        Iterator mi = rtu.getPendingMessages().iterator();
        while (mi.hasNext()) {

            RtuMessage msg = (RtuMessage) mi.next();
            String contents = msg.getContents();
            String cmd = createNesCommand(rtu, contents);


            if (cmd != null) {

                try {
                    String param = commandParam(rtu, cmd, false);
                    Document r = EchelonSession.getInstance(serverUri).performCommand(param, sessionId);
                    Element e = Util.xPath(r, "//TRACKINGID");
                    String tracking = e.getFirstChild().getNodeValue();
                    updateRtuMessage(msg, tracking);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    msg.setFailed();
                }
            }
        }
    }


    private String createNesCommand(Rtu rtu, String contents) {
        if (contents.indexOf(MeterMessaging.READ_REGISTERS_ON_DEMAND) != -1) {
            debug("sending read billing data on demand to " + getDeviceId(rtu));
            return Constants.DeviceCommands.READ_BILLING_DATA_ON_DEMAND;
        }

        if (contents.indexOf(MeterMessaging.READ_REGISTERS) != -1) {
            debug("sending read billing data to " + getDeviceId(rtu));
            return Constants.DeviceCommands.READ_SELF_BILLING_DATA;
        }

        if (contents.indexOf(MeterMessaging.DISCONNECT_LOAD) != -1) {
            debug("sending disconnect to " + getDeviceId(rtu));
            return Constants.DeviceCommands.REMOTE_METER_DISCONNECT;
        }

        if (contents.indexOf(MeterMessaging.CONNECT_LOAD) != -1) {
            debug("sending connect to " + getDeviceId(rtu));
            return Constants.DeviceCommands.REMOTE_METER_CONNECT;
        }

        if (contents.indexOf(MeterMessaging.READ_EVENTS) != -1) {
            debug("sending read events to " + getDeviceId(rtu));
            return Constants.DeviceCommands.READ_EVENT_LOG;
        }

        if (contents.indexOf(MeterMessaging.LOAD_PROFILE_DELTA) != -1) {
            debug("sending read delta load profile to " + getDeviceId(rtu));
            return Constants.DeviceCommands.READ_DELTA_LOAD_PROFILE;
        }

        if (contents.indexOf(MeterMessaging.LOAD_PROFILE) != -1) {
            debug("sending read full load profile to " + getDeviceId(rtu));
            return Constants.DeviceCommands.READ_FULL_LOAD_PROFILE;
        }

        return null;
    }


    private RtuMessageShadow updateRtuMessage(RtuMessage msg, String tracking)
            throws SQLException, BusinessException {

        RtuMessageShadow shadow = msg.getShadow();
        shadow.setTrackingId(tracking);
        msg.update(shadow);
        msg.setSent();
        return shadow;
    }

    /**
     * return true if status element is SUCCESS, false otherwise
     */
    private boolean isSuccess(Document document) {
        String status = getStatus(document);
        return Constants.ExternalServiceReturnCodes.SUCCEEDED.equals(status);

    }

    /**
     * return status element value, null if no status element is present
     */
    private String getStatus(Document document) {
        NodeList nl = (NodeList) document.getElementsByTagName("STATUS");
        if (nl.getLength() == 0)
            return null;
        return nl.item(0).getFirstChild().getNodeValue();
    }

    /**
     * return all ResultIds in document
     */
    private ResultId[] toResultId(Document document) throws ParseException {

        NodeList nl = document.getElementsByTagName("RESULT");
        /* needs to be sorted !!! NES returns from new to old.
         * If new is imported first the lastReading date is set,
         * and old is skipped. */
        TreeSet results = new TreeSet();

        for (int i = 0; i < nl.getLength(); i++) {

            ResultId result = new ResultId();
            Element e = (Element) nl.item(i);
            result.id = Util.getNodeValue(e, "ID");
            result.deviceId = Util.getNodeValue(e, "DEVICEID");
            result.typeId = Util.getNodeValue(e, "TYPEID");
            result.dateTime = Util.getNodeDate(e, "DATETIME");
            result.command = Util.getNodeValue(e, "COMMANDHISTORYID");

            results.add(result);

        }

        return (ResultId[]) results.toArray(new ResultId[0]);

    }

    /**
     * build query parameters for retrieving a ResultList for a given Rtu
     */
    private String resultListQuery(Rtu rtu, boolean fetchDeltaProfile) throws Throwable {

        String deviceId = rtu.getDeviceId();
        Date lastReading = rtu.getLastReading();

        DomHelper builder = new DomHelper("PARAMETERS");

        Element resultTypes = builder.addElement("RESULTTYPES");

        Element resultType = null;
        if (fetchDeltaProfile) {
            resultType = builder.addElement(resultTypes, "RESULTTYPE");
            builder.addElement(resultType, "ID", DeviceResultTypes.DELTA_LOAD_PROFILE);
        }

        resultType = builder.addElement(resultTypes, "RESULTTYPE");
        builder.addElement(resultType, "ID", DeviceResultTypes.FULL_LOAD_PROFILE);

        resultType = builder.addElement(resultTypes, "RESULTTYPE");
        builder.addElement(resultType, "ID", DeviceResultTypes.END_OF_BILLING_CYCLE_BILLING_DATA);

        resultType = builder.addElement(resultTypes, "RESULTTYPE");
        builder.addElement(resultType, "ID", DeviceResultTypes.BILLING_DATA_ON_DEMAND);

        resultType = builder.addElement(resultTypes, "RESULTTYPE");
        builder.addElement(resultType, "ID", DeviceResultTypes.SELF_BILLING_DATA);

        resultType = builder.addElement(resultTypes, "RESULTTYPE");
        builder.addElement(resultType, "ID", DeviceResultTypes.EVENT_LOG);

        builder.addElement("STARTDATETIME", Util.DATE_FORMAT.format(lastReading));

        Element devices = builder.addElement("DEVICES");
        Element device = builder.addElement(devices, "DEVICE");
        builder.addElement(device, "ID", deviceId);

        // debug(Util.scrubHeader(builder.toXmlString()));
        return Util.scrubHeader(builder.toXmlString());

    }

    /**
     * build the parameters for a performCommand service
     */
    private String commandParam(Rtu rtu, String cmd, boolean highPriority)
            throws Throwable {

        String deviceId = getDeviceId(rtu);

        DomHelper builder = new DomHelper("PARAMETERS");

        builder.addElement("DEVICEID", deviceId);
        builder.addElement("COMMANDID", cmd);
        builder.addElement("TIMEOUTDATETIME", Util.DATE_FORMAT.format(nextHour()));

        if (highPriority)
            builder.addElement("PRIORITY", "" + Constants.TaskPriorities.HIGH);

        return Util.scrubHeader(builder.toXmlString());

    }


    private String getDeviceId(Rtu rtu) {
        return rtu.getDeviceId();
    }

    /**
     * return group of rtu's on which this exporter works
     */
    private Group getGroup() {
        if (group == null) {
            String groupId = (String) getProperties().get(GROUP);
            group = MeteringWarehouse.getCurrent().getGroupFactory().findByExternalName(groupId);
        }
        return group;
    }

    /**
     * identify Result type, and dispatch to appropriate store method
     */
    private void store(Document document, Rtu rtu) throws Exception {
        Element result = Util.getElementByName(document, Util.RESULT_TAG);
        String resultTypeId = Util.getNodeValue(result, Util.TYPEID_TAG);
        Element device = Util.getElementByName(document, Util.DEVICE_TAG);
        String deviceTypeId = Util.getNodeValue(device, Util.TYPEID_TAG);

        if (Constants.DeviceResultTypes.FULL_LOAD_PROFILE.equals(resultTypeId) ||
                Constants.DeviceResultTypes.DELTA_LOAD_PROFILE.equals(resultTypeId)) {
            storeProfile(document, rtu);
        } else if (Constants.DeviceResultTypes.END_OF_BILLING_CYCLE_BILLING_DATA.equals(resultTypeId) ||
                Constants.DeviceResultTypes.BILLING_DATA_ON_DEMAND.equals(resultTypeId) ||
                Constants.DeviceResultTypes.SELF_BILLING_DATA.equals(resultTypeId)) {
            if (Constants.DeviceTypes.METER.equals(deviceTypeId)) {
                storeBilling(document, rtu);
            } else if (Constants.DeviceTypes.MBUS.equals(deviceTypeId)) {
                storeMBusBilling(document, rtu);
            }
        } else if (Constants.DeviceResultTypes.EVENT_LOG.equals(resultTypeId)) {
            storeEventLog(document, rtu);
        }

    }

    private void storeProfile(Document document, Rtu rtu) throws Exception {
        ProfileData pd = new ProfileParser().toLoadProfile(document);
        debug("Storing load profile " + pd.toString());
        rtu.store(pd);
    }

    private void storeBilling(Document document, Rtu rtu) throws Exception {

        MeterReadingData mr = new MeterReadingData();
        BigDecimal v = null;
        Element billingData = Util.getElementByName(document, Util.BILLING_DATA_TAG);
        Date d = Util.getNodeDate(billingData, Util.DATETIME_TAG, rtu.getDeviceTimeZone());

        if (readingAlreadyExists(rtu, d, "1.1.1.8.0.255")) {
            v = new BigDecimal(Util.getNodeValue(document, "FORWARDACTIVE"));
            mr.add(toRegisterValue(rtu, "1.1.1.8.0.255", toKwh(v), d));
        }

        if (readingAlreadyExists(rtu, d, "1.1.2.8.0.255")) {
            v = new BigDecimal(Util.getNodeValue(document, "REVERSEACTIVE"));
            mr.add(toRegisterValue(rtu, "1.1.2.8.0.255", toKwh(v), d));
        }

        if (readingAlreadyExists(rtu, d, "1.1.3.8.0.255")) {
            v = new BigDecimal(Util.getNodeValue(document, "IMPORTREACTIVE"));
            mr.add(toRegisterValue(rtu, "1.1.3.8.0.255", toKvarh(v), d));
        }

        if (readingAlreadyExists(rtu, d, "1.1.4.8.0.255")) {
            v = new BigDecimal(Util.getNodeValue(document, "EXPORTREACTIVE"));
            mr.add(toRegisterValue(rtu, "1.1.4.8.0.255", toKvarh(v), d));
        }

        rtu.store(mr);

    }

    private void storeMBusBilling(Document document, Rtu rtu) throws Exception {
        MeterReadingData mr = new MeterReadingData();

        Element billingData = Util.getElementByName(document, Util.BILLING_DATA_TAG);
        Date readTime = Util.getNodeDate(billingData, Util.DATETIME_TAG, rtu.getDeviceTimeZone());

        String rawData = Util.getNodeValue(billingData, Util.RAW_DATA_TAG);
        byte[] mbusDataFrame = convertHexStringToByte(rawData.substring(16));
        CIField72h ciField72h = new CIField72h(rtu.getDeviceTimeZone());
        ciField72h.parse(mbusDataFrame);

        List dataRecords = ciField72h.getDataRecords();
        DataRecord record;
        ValueInformationfieldCoding valueInfo;
        String obisCode;

//    	debug(ciField72h.toString());

        for (int i = 0; i < dataRecords.size(); i++) {

            record = (DataRecord) dataRecords.get(i);
            valueInfo = record.getDataRecordHeader().getValueInformationBlock().getValueInformationfieldCoding();

            if (valueInfo.isTypeUnit() && valueInfo.getDescription().equalsIgnoreCase("volume")) {
                valueInfo.getObisCodeCreator().setA(ciField72h.getDeviceType().getObisA());
                obisCode = valueInfo.getObisCodeCreator().toString();
                mr.add(toRegisterValue(rtu, obisCode, record.getQuantity(), readTime));
                rtu.store(mr);
                break;
            }
        }
    }

    private void storeEventLog(Document document, Rtu rtu) throws Exception {
        ProfileData pd = new ProfileParser().toEventProfile(document);
        debug("Storing event log: " + pd.toString());
        rtu.store(pd);
    }

    /**
     * RtuRegister for obiscode exists, and does not have a value for date d
     */
    private boolean readingAlreadyExists(Rtu rtu, Date d, String os) {
        ObisCode o = toObisCode(os);
        return rtu.getRegister(o) != null &&
                rtu.getRegister(o).getReadingAt(d) == null;
    }

    public String getDescription() {
        return "Exporter for retrieving profile and billing data from NES.";
    }

    public String getVersion() {
        return "$Revision: 1.13 $";
    }

    /* RetrieveResultList service returns list of ResultId 's */
    class ResultId implements Comparable {
        String id;
        String deviceId;
        String typeId;
        Date dateTime;
        String command;

        public int compareTo(Object o) {
            ResultId other = (ResultId) o;
            return dateTime.compareTo(other.dateTime);
        }

        public String toString() {
            return "ResultId [id=" + id + ",deviceId=" + deviceId + "]";
        }

    }

    public List getOptionalKeys() {
        List result = new ArrayList(super.getOptionalKeys());
        result.add(DBG);
        result.add(URI);
        return result;
    }

    public List getRequiredKeys() {
        List result = new ArrayList(super.getRequiredKeys());
        result.add(GROUP);
        return result;
    }

    /**
     * utility method for creating RegisterValue object
     */
    private RegisterValue toRegisterValue(Rtu rtu, String obis, Quantity quantity, Date date) {
        ObisCode code = ObisCode.fromString(obis);
        int id = rtu.getRegister(code).getId();
        return new RegisterValue(code, quantity, null, null, date, date, id);
    }

    /**
     * utility method for creating Quantity object
     */
    private Quantity toKwh(BigDecimal bd) {
        return new Quantity(bd, Unit.get(BaseUnit.WATTHOUR, 0));
    }

    /**
     * utility method for creating Quantity object
     */
    private Quantity toKvarh(BigDecimal bd) {
        return new Quantity(bd, Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, 0));
    }

    /**
     * utility method for creating Date 1 month in the future
     */
    private Date nextHour() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR_OF_DAY, 1);
        return c.getTime();
    }

    /**
     * return true if date is 24 hours ago
     */
    private boolean is24HoursAgo(Date date) {
        return (System.currentTimeMillis() - date.getTime()) > 86400000;
    }

    private ObisCode toObisCode(String obisCode) {
        return ObisCode.fromString(obisCode);
    }

    private byte[] convertHexStringToByte(String hexString) throws Exception {
        if (hexString.length() % 2 != 0) {
            throw new Exception("hex string length should be dividable by 2!");
        }
        byte[] bytes = new byte[hexString.length() / 2];
        String hex;
        for (int i = 0; i < hexString.length(); i += 2) {
            hex = hexString.substring(i, i + 2);
            bytes[i / 2] = (byte) Integer.parseInt(hex, 16);
        }
        return bytes;
    }

}
