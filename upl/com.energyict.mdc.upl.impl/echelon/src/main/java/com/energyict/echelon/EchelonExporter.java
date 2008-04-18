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
            gateway = getGateway(rtu);
            if (gateway == null || gateway.getDeviceId() == null || gateway.getDeviceId().length() == 0) {
                throw new BusinessException(String.format(error[3], rtu.getName()));
            }

            // don't add a concentrator twice and
            if (!concentrators.contains(gateway.getDeviceId()) &&
                    gateway.getGateway() == null) {
                concentrators.add(gateway.getDeviceId());
            }

            try {

                checkSentMessages(rtu, sessionId);
                List<RtuMessage> highTasks = exportRtuMessages(rtu, sessionId);
                if (highTasks.size() > 0) {
                    // add the concentrator to the 'high concenctrators' if needed
                    if (concentrators.contains(gateway.getDeviceId()) &&
                            !highConcentrators.contains(gateway.getDeviceId())) {
                        highConcentrators.add(gateway.getDeviceId());
                    }
                    highPriorityTasks.addAll(highTasks);
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

        if (highPriorityTasks.size() > 0) {
            connectConcentratorsHighPriority(sessionId, highConcentrators, highPriorityTasks);
        }

        connectConcentratorsNormalPriority(sessionId, concentrators);

    }

    private Rtu getGateway(Rtu rtu) {
        Rtu gateway;
        gateway = rtu.getGateway();
        // don't mistaken a meter for a concentrator in case of an M-Bus RTU
        if (gateway.getGateway() != null) {
            gateway = gateway.getGateway();
        }
        return gateway;
    }

    private void connectConcentratorsHighPriority(String sessionId, List<String> highConcentrators, List<RtuMessage> highPriorityTasks) throws SQLException, BusinessException {
        if (debug) {
            debug(null, "Handling high priority commands");
        }
        String concentratorName = "";
        Hashtable<String, Boolean> connectedConcentrators = new Hashtable<String, Boolean>(highConcentrators.size());

        // connect to all concentrators so all high priority tasks will be send
        for (String concentrator : highConcentrators) {
            try {
                concentratorName = concentrator;
                connectedConcentrators.put(concentratorName,
                        isSuccess(EchelonSession.getInstance(serverUri).connect(concentrator, sessionId, true)));
            } catch (EchelonException e) {
                if (debug) {
                    debug(null, String.format("Failed to create a high priority connection to concentrator %s.", concentratorName));
                }
            }
        }
        try {
            // poll the tasks till they all are finished, timed out or the general runtime timed out
            pollHighPriorityTasks(sessionId, highPriorityTasks, connectedConcentrators);
            // disconnect all concentrators with a high priority connection.
            for (String concentrator : highConcentrators) {
                if (connectedConcentrators.get(concentrator)) {
                    EchelonSession.getInstance(serverUri).disconnect(concentrator, sessionId);
                }
            }
        } catch (EchelonException e) {
            getLogger().log(Level.SEVERE, error[2] + e.getMessage(), e);
            e.printStackTrace();
        }
        if (debug) {
            debug(null, "Finished high priority commands");
        }
    }

    private void pollHighPriorityTasks(String sessionId, List<RtuMessage> highPriorityTasks,
                                       Hashtable<String, Boolean> connectedConcentrators)
            throws SQLException, BusinessException, EchelonException {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, timeoutOffset);
        Date taskTimeout = cal.getTime();
        Date now = new Date();

        // wait till all tasks are completed (succes or failed) or the timeout has been reached.
        while (now.before(taskTimeout) && highPriorityTasks.size() > 0) {

            highPriorityTasks = checkHighPriorityMessages(sessionId, highPriorityTasks, connectedConcentrators);

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

    private List<RtuMessage> checkHighPriorityMessages(String sessionId, List<RtuMessage> highPriorityTasks,
                                                       Hashtable<String, Boolean> connectedConcentrators)
            throws SQLException, BusinessException, EchelonException {
        List<RtuMessage> remainingTasks = new ArrayList<RtuMessage>();

        Document result;

        try {
            result = EchelonSession.getInstance(serverUri).retrieveCommandHistory(sessionId);
        } catch (EchelonException ex) {
            return highPriorityTasks;
        }

        if (result != null) {
            Rtu gateway;
            for (RtuMessage message : highPriorityTasks) {
                gateway = getGateway(message.getRtu());
                Boolean bool = connectedConcentrators.get(gateway.getDeviceId());
                if (bool != null && bool) {
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
                            if (debug) {
                            }
                        } else if (Constants.CommandHistoryStatus.SUCCESS.equals(taskStatus) &&
                                message.getContents().indexOf(MeterMessaging.READ_REGISTERS_ON_DEMAND) != -1) {
                            if (!fetchReadOnDemand(message.getRtu(), sessionId)) {
                                remainingTasks.add(message);
                            }
                        }
                        updateMessageStatus(message, taskStatus);
                    }
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
            Boolean hasResult = false;
            try {
                ResultId[] ids = toResultId(document);
                for (ResultId id : ids) {
                    hasResult = store(fetchResult(id, sessionId), rtu);
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new BusinessException(e);
            }
            return hasResult;
        }

    }

    /**
     * persist/log debug messages
     *
     * @param rtu the processed RTU
     * @param msg debug message
     */
    private void debug(Rtu rtu, String msg) {
        if (rtu == null) {
            getLogger().info(msg);
            System.out.println(msg);
        } else {
            String logMsg = String.format("%s: %s", rtu.getName(), msg);
            getLogger().info(logMsg);
            System.out.println(logMsg);
        }
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

    private boolean fetchReadOnDemand(Rtu rtu, String sessionId) throws EchelonException, SQLException, BusinessException {
        String query = readOnDemandListQuery(rtu);
        Document resultList;
        resultList = EchelonSession.getInstance(serverUri).retrieveResultList(query, sessionId);
        if (isSuccess(resultList)) {
            return (Boolean) Environment.getDefault().execute(new StoreTransaction(rtu, resultList, sessionId));
        } else {
            getLogger().severe(error[1] + " " + getStatus(resultList));
        }
        return false;
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
            String taskStatus = e.getFirstChild().getNodeValue();

            updateMessageStatus(msg, taskStatus);
        }
        // enforce a continuous delta load profile read, but not on m-bus meters 
        if (rtu.getGateway() != null && rtu.getGateway().getGateway() == null) {
            enforceContinuousDeltaLoadProfileCommand(rtu, sessionId, r);
        }
    }

    private void updateMessageStatus(RtuMessage msg, String taskStatus) throws BusinessException, SQLException {
        RtuMessageState prevState = msg.getState();
        if (Constants.CommandHistoryStatus.SUCCESS.equals(taskStatus)) {
            msg.confirm();
        } else if (Constants.CommandHistoryStatus.FAILURE.equals(taskStatus)) {
            msg.setFailed();
        } else if (Constants.CommandHistoryStatus.CANCELLED.equals(taskStatus)) {
            msg.setFailed();
        } else if (Constants.CommandHistoryStatus.DELETED.equals(taskStatus)) {
            msg.setFailed();
        } else if (Constants.CommandHistoryStatus.IN_PROGRESS.equals(taskStatus)) {
            msg.setSent();
        } else if (Constants.CommandHistoryStatus.WAITING.equals(taskStatus)) {
            msg.setSent();
        }
        if (debug && msg.getState() != prevState) {
            debug(msg.getRtu(), String.format("%s: %s (NES: %s)", msg.getContents(), msg.getState().toString(), Util.getStatusDescription(taskStatus)));
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
            String nesCommand = createNesCommand(rtu, MeterMessaging.CONTINUOUS_DELTA_LOAD_PROFILE);
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
            String cmd = createNesCommand(rtu, contents);

            if (cmd.equals(Constants.DeviceCommands.CONNECT_LOAD)
                    || cmd.equals(Constants.DeviceCommands.DISCONNECT_LOAD)
                    || cmd.equals(Constants.DeviceCommands.READ_BILLING_DATA_ON_DEMAND)) {
                highPriority = true;
                timeout = nextFiveMinutes();
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


    private String createNesCommand(Rtu rtu, String contents) {
        if (contents.indexOf(MeterMessaging.READ_REGISTERS_ON_DEMAND) != -1) {
            if (debug) {
                debug(rtu, "sending read billing data on demand");
            }
            return Constants.DeviceCommands.READ_BILLING_DATA_ON_DEMAND;
        }

        if (contents.indexOf(MeterMessaging.READ_REGISTERS) != -1) {
            if (debug) {
                debug(rtu, "sending read billing data");
            }
            return Constants.DeviceCommands.READ_SELF_BILLING_DATA;
        }

        if (contents.indexOf(MeterMessaging.DISCONNECT_LOAD) != -1) {
            if (debug) {
                debug(rtu, "sending disconnect");
            }
            return Constants.DeviceCommands.REMOTE_METER_DISCONNECT;
        }

        if (contents.indexOf(MeterMessaging.CONNECT_LOAD) != -1) {
            if (debug) {
                debug(rtu, "sending connect");
            }
            return Constants.DeviceCommands.REMOTE_METER_CONNECT;
        }

        if (contents.indexOf(MeterMessaging.READ_EVENTS) != -1) {
            if (debug) {
                debug(rtu, "sending read events");
            }
            return Constants.DeviceCommands.READ_EVENT_LOG;
        }

        if (contents.indexOf(MeterMessaging.CONTINUOUS_DELTA_LOAD_PROFILE) != -1) {
            if (debug) {
                debug(rtu, "sending continuous delta load profile");
            }
            return Constants.DeviceCommands.READ_CONTINUOUS_DELTA_LOAD_PROFILE;
        }

        if (contents.indexOf(MeterMessaging.LOAD_PROFILE_DELTA) != -1) {
            if (debug) {
                debug(rtu, "sending read delta load profile");
            }
            return Constants.DeviceCommands.READ_DELTA_LOAD_PROFILE;
        }

        if (contents.indexOf(MeterMessaging.LOAD_PROFILE) != -1) {
            if (debug) {
                debug(rtu, "sending read full load profile");
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

    private String readOnDemandListQuery(Rtu rtu) {

        DomHelper builder = new DomHelper("PARAMETERS");
        Element resultTypes = builder.addElement("RESULTTYPES");
        Element resultType = builder.addElement(resultTypes, "RESULTTYPE");
        builder.addElement(resultType, "ID", DeviceResultTypes.BILLING_DATA_ON_DEMAND);
        Element devices = builder.addElement("DEVICES");
        Element device = builder.addElement(devices, "DEVICE");
        builder.addElement(device, "ID", rtu.getDeviceId());
        builder.addElement("STARTDATETIME", Util.DATE_FORMAT.format(rtu.getLastReading()));

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

        // don't set the priority on read on demands, as they are by default on high priority
        // and the tag is only supported on M-Bus devices.
        if (highPriority && !Constants.DeviceCommands.READ_BILLING_DATA_ON_DEMAND.equals(cmd)) {
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
    private boolean store(Document document, Rtu rtu) throws Exception {
        Element result = Util.getElementByName(document, Util.RESULT_TAG);
        String resultTypeId = Util.getNodeValue(result, Util.TYPEID_TAG);
        Element device = Util.getElementByName(document, Util.DEVICE_TAG);
        String deviceTypeId = Util.getNodeValue(device, Util.TYPEID_TAG);

        if (Constants.DeviceResultTypes.FULL_LOAD_PROFILE.equals(resultTypeId) ||
                Constants.DeviceResultTypes.DELTA_LOAD_PROFILE.equals(resultTypeId)) {
            return storeProfile(document, rtu);
        } else if (Constants.DeviceResultTypes.END_OF_BILLING_CYCLE_BILLING_DATA.equals(resultTypeId) ||
                Constants.DeviceResultTypes.BILLING_DATA_ON_DEMAND.equals(resultTypeId) ||
                Constants.DeviceResultTypes.SELF_BILLING_DATA.equals(resultTypeId)) {
            if (Constants.DeviceTypes.METER.equals(deviceTypeId)) {
                return storeBilling(document, rtu);
            } else if (Constants.DeviceTypes.MBUS.equals(deviceTypeId)) {
                boolean onDemand = Constants.DeviceResultTypes.BILLING_DATA_ON_DEMAND.equals(resultTypeId);
                return storeMBusBilling(document, rtu, onDemand);
            }
        } else if (Constants.DeviceResultTypes.EVENT_LOG.equals(resultTypeId)) {
            return storeEventLog(document, rtu);
        }
        return false;
    }

    private boolean storeProfile(Document document, Rtu rtu) throws Exception {
        List<Channel> channels = (List<Channel>) rtu.getChannels();
        ProfileData pd = new ProfileParser().toLoadProfile(document, rtu, channels);
        if (pd.getNumberOfIntervals() > 0) {
            rtu.store(pd);
            if (debug) {
                debug(rtu, "Storing load profile " + pd.toString());
            }
            return true;
        }
        return false;
    }

    private boolean storeBilling(Document document, Rtu rtu) throws Exception {
        boolean hasNewValues = false;
        MeterReadingData mr = new MeterReadingData();
        Date readDate = Util.getNodeDate((Element) document.getFirstChild(), Util.DATETIME_TAG);
        Element billingData = Util.getElementByName(document, Util.BILLING_DATA_TAG);
        Date toDate = Util.getNodeDate(billingData, Util.DATETIME_TAG, rtu.getDeviceTimeZone());

        if (toDate.after(new Date())) {
            return false; // don't store stuff from the future
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

            hasNewValues |= storeTierValues(rtu, mr, toDate, readDate, tier, index, FORWARD_ACTIVE);
            hasNewValues |= storeTierValues(rtu, mr, toDate, readDate, tier, index, REVERSE_ACTIVE);
            hasNewValues |= storeTierValues(rtu, mr, toDate, readDate, tier, index, IMPORT_REACTIVE);
            hasNewValues |= storeTierValues(rtu, mr, toDate, readDate, tier, index, EXPORT_REACTIVE);
        }

        if (hasNewValues) {
            rtu.store(mr);
        }
        return hasNewValues;

    }

    private boolean storeTierValues(Rtu rtu, MeterReadingData mr, Date toDate, Date readDate, Element tier, int index, String name) {
        ObisCode obis = getObisCode(index, name);

        if (rtu.getRegister(obis) != null && rtu.getRegister(obis).getReadingAt(readDate) == null) {
            RegisterValue value = toRegisterValue(rtu, obis, getQuantity(tier, name), toDate, readDate);
            mr.add(value);
            if (debug) {
                debug(rtu, "Storing register " + value.toString());
            }
            return true;
        }
        return false;
    }

    private boolean storeMBusBilling(Document document, Rtu rtu, boolean onDemand) throws Exception {
        MeterReadingData mr = new MeterReadingData();
        boolean hasNewValues = false;

        Date readDate;
        if (onDemand) {
            readDate = Util.getNodeDate((Element) document.getFirstChild(), Util.DATETIME_TAG, rtu.getDeviceTimeZone());
        } else {
            readDate = Util.getNodeDate((Element) document.getFirstChild(), Util.DATETIME_TAG);
        }

        Element billingData = Util.getElementByName(document, Util.BILLING_DATA_TAG);
        Date toDate = Util.getNodeDate(billingData, Util.DATETIME_TAG, rtu.getDeviceTimeZone());

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
                ObisCode obis = ObisCode.fromString(obisCode);
                if (rtu.getRegister(obis) != null && rtu.getRegister(obis).getReadingAt(readDate) == null) {
                    RegisterValue value = toRegisterValue(rtu, ObisCode.fromString(obisCode), record.getQuantity(), toDate, readDate);
                    mr.add(value);
                    if (debug) {
                        debug(rtu, "Storing register " + value.toString());
                    }
                    hasNewValues = true;
                }
                break;
            }
        }
        rtu.store(mr);
        return hasNewValues;
    }

    private boolean storeEventLog(Document document, Rtu rtu) throws Exception {
        ProfileData pd = new ProfileParser().toEventProfile(document, rtu);
        if (pd.getNumberOfEvents() > 0) {
            rtu.store(pd);
            if (debug) {
                debug(rtu, "Storing event log: " + pd.toString());
            }
            return true;
        }
        return false;
    }

    public String getDescription() {
        return "Exporter for retrieving profile and billing data from NES.";
    }

    public String getVersion() {
        return "$Date$:";
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
     * @param toDate   The registers capture date.
     * @return the register value.
     */
    private RegisterValue toRegisterValue(Rtu rtu, ObisCode obis, Quantity quantity, Date toDate, Date readDate) {
        int id = rtu.getRegister(obis).getId();
        return new RegisterValue(obis, quantity, null, null, toDate, readDate, id);
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
    private Date nextFiveMinutes() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MINUTE, 5);
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
