package com.energyict.echelon;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.cpo.Environment;
import com.energyict.cpo.Transaction;
import com.energyict.echelon.Constants.DeviceResultTypes;
import com.energyict.eisexport.core.AbstractExporter;
import com.energyict.mdw.core.*;
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

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.math.BigDecimal;
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
    /**
     * property name of the run task timeout
     */
    private final String RUN_TASK_TIMEOUT = "run task timeout (minutes, default 5)";

    private boolean debug;
    private String serverUri;
    private Group group;
    private int timeoutOffset;

    private String error[] = {
            "Unable to run exporter, missing parameter \"" + URI + "\"",
            "NES returned invalid message",
            "Exception occured during export",
            "Echelon gateway not defined for meter %s"
    };
    private static final String FORWARD_ACTIVE = "FORWARDACTIVE";
    private static final String REVERSE_ACTIVE = "REVERSEACTIVE";
    private static final String IMPORT_REACTIVE = "IMPORTREACTIVE";
    private static final String EXPORT_REACTIVE = "EXPORTREACTIVE";

    protected void preExport() throws IOException, BusinessException, SQLException {

        System.setProperty("entityExpansionLimit", "100000");

        debug = getProperty(DBG) != null;

        serverUri = getProperty(URI);
        if (serverUri == null) {
            throw new BusinessException(error[0]);
        }

        try {
            timeoutOffset = Integer.parseInt(getProperty(RUN_TASK_TIMEOUT));
        } catch (NumberFormatException ex) {
            timeoutOffset = 5;
        }
        if (timeoutOffset == 0) {
            timeoutOffset = 5;
        }

    }


    protected void export() throws IOException, BusinessException, SQLException {
        String sessionId;

        List<String> concentrators = new ArrayList<String>();
        List<String> highConcentrators = new ArrayList<String>();
        List<RtuMessage> highPriorityTasks = new ArrayList<RtuMessage>();

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
            gateway = rtu.getGateway();
            if (gateway == null || gateway.getDeviceId() == null || gateway.getDeviceId().length() == 0) {
                throw new BusinessException(String.format(error[3], rtu.getName()));
            }

            // don't add a concentrator twice and
            // don't mistaken a meter for a concentrator in case of an M-Bus RTU
            if (!concentrators.contains(gateway.getDeviceId()) &&
                    gateway.getGateway() == null) {
                concentrators.add(gateway.getDeviceId());
            }

            try {

                if (debug) {
                    debug("Handling " + rtu.getFullName());
                }

                checkSentMessages(rtu, sessionId);
                List<RtuMessage> highTasks = exportRtuMessages(rtu, sessionId);
                if (highTasks.size() > 0) {
                    if (concentrators.contains(gateway.getDeviceId())) {
                        highConcentrators.add(gateway.getDeviceId());
                        highPriorityTasks.addAll(highTasks);
                    }
                }

                if (debug) {
                    debug("Echelon exporter: handling readings");
                }

                Document d = fetchResultList(rtu, sessionId);

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

        connectConcentratorsHighPriority(sessionId, highConcentrators, highPriorityTasks);

        connectConcentratorsNormalPriority(sessionId, concentrators);

    }

    private void connectConcentratorsHighPriority(String sessionId, List<String> highConcentrators, List<RtuMessage> highPriorityTasks) throws SQLException, BusinessException {
        try {
            // connect to all concentrators so all high priority tasks will be send
            for (String concentrator : highConcentrators) {
                EchelonSession.getInstance(serverUri).connect(concentrator, sessionId, true);
            }
            // poll the tasks till they all are finished, timed out or the general runtime timed out
            pollHighPriorityTasks(sessionId, highPriorityTasks);
            // disconnect all concentrators with a high priority connection.
            for (String concentrator : highConcentrators) {
                EchelonSession.getInstance(serverUri).disconnect(concentrator, sessionId);
            }
        } catch (EchelonException e) {
            getLogger().log(Level.SEVERE, error[2] + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void pollHighPriorityTasks(String sessionId, List<RtuMessage> highPriorityTasks) throws SQLException, BusinessException {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, timeoutOffset);
        Date taskTimeout = cal.getTime();
        Date now = new Date();

        // wait till all tasks are completed (succes or failed) or the timeout has been reached.
        while (now.before(taskTimeout) && highPriorityTasks.size() > 0) {

            highPriorityTasks = checkHighPriorityMessages(sessionId, highPriorityTasks);

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            now = new Date();
        }
    }

    private void connectConcentratorsNormalPriority(String sessionId, List<String> concentrators) {
        try {
            for (String concentrator : concentrators) {
                EchelonSession.getInstance(serverUri).connect(concentrator, sessionId, false);
            }
        } catch (EchelonException e) {
            getLogger().log(Level.SEVERE, error[2] + e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private List<RtuMessage> checkHighPriorityMessages(String sessionId, List<RtuMessage> highPriorityTasks) throws SQLException, BusinessException {
        List<RtuMessage> remainingTasks = new ArrayList<RtuMessage>();

        Document result;

        try {
            result = EchelonSession.getInstance(serverUri).retrieveCommandHistory(sessionId);
        } catch (EchelonException ex) {
            return highPriorityTasks;
        }

        if (result != null) {
            for (RtuMessage message : highPriorityTasks) {
                Element e = null;
                try {
                    e = Util.xPath(result, String.format("//COMMANDHISTORY/STATUSTYPEID[../ID/text() = '%s']", message.getTrackingId()));
                } catch (TransformerException e1) {
                    e1.printStackTrace();
                }
                if (e != null && e.getFirstChild() != null) {
                    String taskStatus = e.getFirstChild().getNodeValue();

                    if (Constants.CommandHistoryStatus.WAITING.equals(taskStatus) ||
                            Constants.CommandHistoryStatus.IN_PROGRESS.equals(taskStatus)) {
                        remainingTasks.add(message);
                    }
                    updateMessageStatus(message, taskStatus);
                }

            }
        }

        return remainingTasks;
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
                for (ResultId id : ids) {
                    store(fetchResult(id, sessionId), rtu);
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
     *
     * @param msg debug message
     */
    private void debug(String msg) {
        getLogger().info(msg);
        System.out.println(msg);
    }

    /**
     * fetch list of Results (=Load Profiles)
     *
     * @param rtu       The processed RTU
     * @param sessionId The sessionId retrieved from NES
     * @return XML document with list of available result sets.
     * @throws EchelonException when a NES error occurred.
     */
    private Document fetchResultList(Rtu rtu, String sessionId) throws EchelonException {
        String query = resultListQuery(rtu);
        return EchelonSession.getInstance(serverUri).retrieveResultList(query, sessionId);
    }

    /**
     * fetch Result document
     *
     * @param id        Result id
     * @param sessionId NES session id
     * @return XML document with the result data retrieved from NES.
     * @throws EchelonException when a NES error occurred.
     */
    private Document fetchResult(ResultId id, String sessionId) throws EchelonException {
        return EchelonSession.getInstance(serverUri).retrieveResult(id.id, sessionId);
    }

    private void checkSentMessages(Rtu rtu, String sessionId) throws Throwable {

        if (rtu.getDeviceId() == null) return;

        Document r = null;
        boolean echelonError = false;
        try {
            r = EchelonSession.getInstance(serverUri).retrieveCommandHistory(sessionId, rtu.getDeviceId());
        } catch (EchelonException ex) {
            echelonError = true;
        }

        for (Object o : rtu.getSentMessages()) {

            RtuMessage msg = (RtuMessage) o;
            if (msg.getTrackingId() == null) {
                continue;
            }

            if (echelonError) {
                msg.setFailed();
                continue;
            }

            String taskId = msg.getTrackingId();
            Element e = Util.xPath(r, String.format("//COMMANDHISTORY/STATUSTYPEID[../ID/text() = '%s']", taskId));
            String taSkStatus = e.getFirstChild().getNodeValue();

            if (debug) {
                debug(msg.getContents() + ": " + Util.getStatusDescription(taSkStatus));
            }

            updateMessageStatus(msg, taSkStatus);
        }
        enforceContinuousDeltaLoadProfileCommand(rtu, sessionId, r);
    }

    private void updateMessageStatus(RtuMessage msg, String taSkStatus) throws BusinessException, SQLException {
        if (Constants.CommandHistoryStatus.SUCCESS.equals(taSkStatus)) {
            msg.confirm();
        } else if (Constants.CommandHistoryStatus.FAILURE.equals(taSkStatus)) {
            msg.setFailed();
        } else if (Constants.CommandHistoryStatus.CANCELLED.equals(taSkStatus)) {
            msg.setFailed();
        } else if (Constants.CommandHistoryStatus.DELETED.equals(taSkStatus)) {
            msg.setFailed();
        } else if (Constants.CommandHistoryStatus.IN_PROGRESS.equals(taSkStatus)) {
            msg.setSent();
        } else if (Constants.CommandHistoryStatus.WAITING.equals(taSkStatus)) {
            msg.setPending();
        }

    }

    private void enforceContinuousDeltaLoadProfileCommand(Rtu rtu, String sessionId, Document r) throws TransformerException, EchelonException {
        // test if a 'continuous delta load profile' is in the command list with status <> failed, completed.
        // if not, add one.

        NodeList nodes = Util.xPathNodeList(r, String.format("//COMMANDHISTORY[COMMANDID= '%s']/STATUSTYPEID", Constants.DeviceCommands.READ_CONTINUOUS_DELTA_LOAD_PROFILE));
        boolean hasActiveContinuousDelta = false;
        Element e;

        // check if one of the continuous delta load commands in the node list is in progress or waiting.
        for (int i = 0; i < nodes.getLength() && !hasActiveContinuousDelta; i++) {
            e = (Element) nodes.item(i);
            String deltaLoadStatus = null;
            if (e != null) {
                deltaLoadStatus = e.getFirstChild().getNodeValue();
            }
            if (deltaLoadStatus != null &&
                    (deltaLoadStatus.equals(Constants.CommandHistoryStatus.IN_PROGRESS) ||
                            deltaLoadStatus.equals(Constants.CommandHistoryStatus.WAITING))) {
                hasActiveContinuousDelta = true;
            }
        }

        // add a cont. delta load command in case there's no active one.
        if (!hasActiveContinuousDelta) {
            String nesCommand = createNesCommand(rtu.getDeviceId(), MeterMessaging.CONTINUOUS_DELTA_LOAD_PROFILE);
            Calendar c = Calendar.getInstance();
            c.add(Calendar.YEAR, 20);
            Date timeout = c.getTime();
            String param = commandParam(rtu, nesCommand, false, timeout);
            EchelonSession.getInstance(serverUri).performCommand(param, sessionId);
        }
    }

    /**
     * send translate RtuMessage to the Ness service
     *
     * @param rtu       The processed RTU.
     * @param sessionId The NES session id.
     * @return List of high priority messages. empty if none.
     * @throws BusinessException when a functional error occurred.
     * @throws SQLException      when a sql operation failed.
     */
    private List<RtuMessage> exportRtuMessages(Rtu rtu, String sessionId) throws BusinessException, SQLException {

        List<RtuMessage> highPriorityTasks = new ArrayList<RtuMessage>();

        if (rtu.getDeviceId() == null) {
            return highPriorityTasks;
        }

        boolean highPriority;
        Date timeout;
        for (Object o : rtu.getPendingMessages()) {

            RtuMessage msg = (RtuMessage) o;
            String contents = msg.getContents();
            String cmd = createNesCommand(rtu.getDeviceId(), contents);

            if (cmd.equals(Constants.DeviceCommands.CONNECT_LOAD)
                    || cmd.equals(Constants.DeviceCommands.DISCONNECT_LOAD)
                    || cmd.equals(Constants.DeviceCommands.READ_BILLING_DATA_ON_DEMAND)) {
                highPriority = true;
                timeout = nextTwoMinutes();
            } else {
                highPriority = false;
                timeout = nextHour();
            }

            if (cmd != null) {

                try {
                    String param = commandParam(rtu, cmd, highPriority, timeout);
                    Document r = EchelonSession.getInstance(serverUri).performCommand(param, sessionId);
                    Element e = Util.xPath(r, "//TRACKINGID");
                    String tracking = e.getFirstChild().getNodeValue();
                    updateRtuMessage(msg, tracking);
                    if (highPriority) {
                        highPriorityTasks.add(msg);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    msg.setFailed();
                }
            }
        }
        return highPriorityTasks;
    }


    private String createNesCommand(String deviceId, String contents) {
        if (contents.indexOf(MeterMessaging.READ_REGISTERS_ON_DEMAND) != -1) {
            if (debug) {
                debug("sending read billing data on demand to " + deviceId);
            }
            return Constants.DeviceCommands.READ_BILLING_DATA_ON_DEMAND;
        }

        if (contents.indexOf(MeterMessaging.READ_REGISTERS) != -1) {
            if (debug) {
                debug("sending read billing data to " + deviceId);
            }
            return Constants.DeviceCommands.READ_SELF_BILLING_DATA;
        }

        if (contents.indexOf(MeterMessaging.DISCONNECT_LOAD) != -1) {
            if (debug) {
                debug("sending disconnect to " + deviceId);
            }
            return Constants.DeviceCommands.REMOTE_METER_DISCONNECT;
        }

        if (contents.indexOf(MeterMessaging.CONNECT_LOAD) != -1) {
            if (debug) {
                debug("sending connect to " + deviceId);
            }
            return Constants.DeviceCommands.REMOTE_METER_CONNECT;
        }

        if (contents.indexOf(MeterMessaging.READ_EVENTS) != -1) {
            if (debug) {
                debug("sending read events to " + deviceId);
            }
            return Constants.DeviceCommands.READ_EVENT_LOG;
        }

        if (contents.indexOf(MeterMessaging.CONTINUOUS_DELTA_LOAD_PROFILE) != 1) {
            if (debug) {
                debug("sending continuous delta load profile to " + deviceId);
            }
            return Constants.DeviceCommands.READ_CONTINUOUS_DELTA_LOAD_PROFILE;
        }

        if (contents.indexOf(MeterMessaging.LOAD_PROFILE_DELTA) != -1) {
            if (debug) {
                debug("sending read delta load profile to " + deviceId);
            }
            return Constants.DeviceCommands.READ_DELTA_LOAD_PROFILE;
        }

        if (contents.indexOf(MeterMessaging.LOAD_PROFILE) != -1) {
            if (debug) {
                debug("sending read full load profile to " + deviceId);
            }
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
     *
     * @param document XML document returned by NES.
     * @return true if the return code == succeeded, false otherwise.
     */
    private boolean isSuccess(Document document) {
        String status = getStatus(document);
        return Constants.ExternalServiceReturnCodes.SUCCEEDED.equals(status);

    }

    /**
     * return status element value
     *
     * @param document XML document returned by NES.
     * @return the status, null if no status is present.
     */
    private String getStatus(Document document) {
        NodeList nl = document.getElementsByTagName("STATUS");
        if (nl.getLength() == 0)
            return null;
        return nl.item(0).getFirstChild().getNodeValue();
    }

    /**
     * return all ResultIds in document
     *
     * @param document XML document returned by NES with all available result sets.
     * @return list of result id objects.
     * @throws java.text.ParseException when the parsing the result id set failed.
     */
    private ResultId[] toResultId(Document document) throws ParseException {

        NodeList nl = document.getElementsByTagName("RESULT");
        /* needs to be sorted !!! NES returns from new to old.
         * If new is imported first the lastReading date is set,
         * and old is skipped. */
        TreeSet<ResultId> results = new TreeSet<ResultId>();

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

        return results.toArray(new ResultId[results.size()]);

    }

    /**
     * build query parameters for retrieving a ResultList for a given Rtu
     *
     * @param rtu The processed RTU.
     * @return Parameter string to retrieve the available resultsets in NES
     */
    private String resultListQuery(Rtu rtu) {

        String deviceId = rtu.getDeviceId();
        Date lastReading = rtu.getLastReading();

        DomHelper builder = new DomHelper("PARAMETERS");

        Element resultTypes = builder.addElement("RESULTTYPES");

        Element resultType = builder.addElement(resultTypes, "RESULTTYPE");
        builder.addElement(resultType, "ID", DeviceResultTypes.DELTA_LOAD_PROFILE);

        resultType = builder.addElement(resultTypes, "RESULTTYPE");
        builder.addElement(resultType, "ID", DeviceResultTypes.FULL_LOAD_PROFILE);

        resultType = builder.addElement(resultTypes, "RESULTTYPE");
        builder.addElement(resultType, "ID", DeviceResultTypes.END_OF_BILLING_CYCLE_BILLING_DATA);

        resultType = builder.addElement(resultTypes, "RESULTTYPE");
        builder.addElement(resultType, "ID", DeviceResultTypes.BILLING_DATA_ON_DEMAND);

        resultType = builder.addElement(resultTypes, "RESULTTYPE");
        builder.addElement(resultType, "ID", DeviceResultTypes.EVENT_LOG);

        builder.addElement("STARTDATETIME", Util.DATE_FORMAT.format(lastReading));

        Element devices = builder.addElement("DEVICES");
        Element device = builder.addElement(devices, "DEVICE");
        builder.addElement(device, "ID", deviceId);

        return Util.scrubHeader(builder.toXmlString());
    }

    /**
     * build the parameters for a performCommand service
     *
     * @param rtu          The processed RTU
     * @param cmd          The NES command being performed.
     * @param highPriority Flag indicating if the message is send with high priority.
     * @param timeout      absolute time when the command should timeout.
     * @return Paramteters for the NES command.
     */
    private String commandParam(Rtu rtu, String cmd, boolean highPriority, Date timeout) {

        String deviceId = rtu.getDeviceId();

        DomHelper builder = new DomHelper("PARAMETERS");

        builder.addElement("DEVICEID", deviceId);
        builder.addElement("COMMANDID", cmd);
        builder.addElement("TIMEOUTDATETIME", Util.DATE_FORMAT.format(timeout));

        if (highPriority) {
            builder.addElement("PRIORITY", "" + Constants.TaskPriorities.HIGH);
        }

        return Util.scrubHeader(builder.toXmlString());
    }

    /**
     * return group of rtu's on which this exporter works
     *
     * @return The GROUP object referenced by the custom property on the exporter.
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
     *
     * @param document XML document returned buy NES.
     * @param rtu      The processed RTU.
     * @throws Exception when something failed storing the values.
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
        List<Channel> channels = (List<Channel>) rtu.getChannels();
        ProfileData pd = new ProfileParser().toLoadProfile(document, rtu.getDeviceTimeZone(), channels);
        if (debug) {
            debug("Storing load profile " + pd.toString());
        }
        rtu.store(pd);
    }

    private void storeBilling(Document document, Rtu rtu) throws Exception {

        MeterReadingData mr = new MeterReadingData();
        Element billingData = Util.getElementByName(document, Util.BILLING_DATA_TAG);
        Date date = Util.getNodeDate(billingData, Util.DATETIME_TAG, rtu.getDeviceTimeZone());

        if (date.after(new Date())) {
            return; // don't store stuff from the future
        }

        NodeList tiers = billingData.getElementsByTagName("TIERS").item(0).getChildNodes();

        for (int i = 0; i < tiers.getLength(); i++) {
            Element tier = (Element) tiers.item(i);

            int index;
            try {
                // SUM OF TIERS HAS NO INDEX ELEMENT
                index = Util.getNodeInt(tier, "INDEX");
            } catch (Exception ex) {
                index = 0;
            }

            storeTierValues(rtu, mr, date, tier, index, FORWARD_ACTIVE);
            storeTierValues(rtu, mr, date, tier, index, REVERSE_ACTIVE);
            storeTierValues(rtu, mr, date, tier, index, IMPORT_REACTIVE);
            storeTierValues(rtu, mr, date, tier, index, EXPORT_REACTIVE);
        }


        rtu.store(mr);

    }

    private void storeTierValues(Rtu rtu, MeterReadingData mr, Date date, Element tier, int index, String name) {
        ObisCode obis = getObisCode(index, name);

        if (rtu.getRegister(obis) != null && rtu.getRegister(obis).getReadingAt(date) == null) {
            mr.add(toRegisterValue(rtu, obis, getQuantity(tier, name), date));
        }
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

        for (Object dataRecord : dataRecords) {

            record = (DataRecord) dataRecord;
            valueInfo = record.getDataRecordHeader().getValueInformationBlock().getValueInformationfieldCoding();

            if (valueInfo.isTypeUnit() && valueInfo.getDescription().equalsIgnoreCase("volume")) {
                valueInfo.getObisCodeCreator().setA(ciField72h.getDeviceType().getObisA());
                obisCode = valueInfo.getObisCodeCreator().toString();
                mr.add(toRegisterValue(rtu, ObisCode.fromString(obisCode), record.getQuantity(), readTime));
                rtu.store(mr);
                break;
            }
        }
    }

    private void storeEventLog(Document document, Rtu rtu) throws Exception {
        ProfileData pd = new ProfileParser().toEventProfile(document);
        if (debug) {
            debug("Storing event log: " + pd.toString());
        }
        rtu.store(pd);
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
        result.add(RUN_TASK_TIMEOUT);
        return result;
    }

    public List getRequiredKeys() {
        List result = new ArrayList(super.getRequiredKeys());
        result.add(GROUP);
        return result;
    }

    /**
     * utility method for creating RegisterValue object
     *
     * @param rtu      The processed RTU.
     * @param obis     The OBIS code of the register value.
     * @param quantity The quantity (=value + unit) to be stored as a register value.
     * @param date     The registers capture date.
     * @return the register value.
     */
    private RegisterValue toRegisterValue(Rtu rtu, ObisCode obis, Quantity quantity, Date date) {
        int id = rtu.getRegister(obis).getId();
        return new RegisterValue(obis, quantity, null, null, date, date, id);
    }

    /**
     * utility method for creating Quantity object
     *
     * @param bd The bigdecimal to be stored as a Watt/Hour quantity
     * @return KWh quantity
     */
    private Quantity toKwh(BigDecimal bd) {
        return new Quantity(bd, Unit.get(BaseUnit.WATTHOUR, 0));
    }

    /**
     * utility method for creating Quantity object
     *
     * @param bd The bigdecimal to be stored as KVar/Hour quantity
     * @return KVarh quantity
     */
    private Quantity toKvarh(BigDecimal bd) {
        return new Quantity(bd, Unit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR, 0));
    }

    /**
     * utility method for creating Date 1 hour in the future
     *
     * @return The current time + 1 hour.
     */
    private Date nextHour() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR_OF_DAY, 1);
        return c.getTime();
    }

    /**
     * utility method for creating Date 1 hour in the future
     *
     * @return The current time + 1 hour.
     */
    private Date nextTwoMinutes() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 2);
        return c.getTime();
    }

    private ObisCode getObisCode(int index, String c_parameter) {
        int c_Code = 0;
        if (c_parameter.equals(FORWARD_ACTIVE)) {
            c_Code = 1;
        } else if (c_parameter.equals(REVERSE_ACTIVE)) {
            c_Code = 2;
        } else if (c_parameter.equals(IMPORT_REACTIVE)) {
            c_Code = 3;
        } else if (c_parameter.equals(EXPORT_REACTIVE)) {
            c_Code = 4;
        }
        return ObisCode.fromString(String.format("1.1.%d.8.%d.255", c_Code, index));
    }

    private Quantity getQuantity(Element tier, String c_parameter) {
        BigDecimal v = new BigDecimal(Util.getNodeValue(tier, c_parameter));
        if (c_parameter.equals(FORWARD_ACTIVE) || c_parameter.equals(REVERSE_ACTIVE)) {
            return toKwh(v);
        } else if (c_parameter.equals(IMPORT_REACTIVE) || c_parameter.equals(EXPORT_REACTIVE)) {
            return toKvarh(v);
        }
        return null;
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
