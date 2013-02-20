package com.energyict.genericprotocolimpl.rtuplusserver.g3;

import com.energyict.cbo.BusinessException;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.cosem.SAPAssignment;
import com.energyict.dlms.cosem.SAPAssignmentItem;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Copyrights EnergyICT
 * Date: 6/03/12
 * Time: 15:03
 */
public class RtuPlusServer {

    private DlmsSession session;
    private RtuPlusServerProperties properties = new RtuPlusServerProperties();
    private RtuPlusServerInfo info;
    private RtuPlusServerClock clock;
    private RtuPlusServerEventLog eventLog;
    private RtuPlusServerTask task;

    public RtuPlusServer() {

    }

//    public void execute(CommunicationScheduler scheduler, Link link, Logger logger) throws BusinessException, SQLException, IOException {
//        this.session = new DlmsSession(link.getInputStream(), link.getOutputStream(), logger, properties, TimeZone.getDefault());
//        this.task = new RtuPlusServerTask(scheduler, logger);
//        try {
//            this.session.connect();
//            this.info = new RtuPlusServerInfo(session, task);
//            this.clock = new RtuPlusServerClock(session, task);
//            this.eventLog = new RtuPlusServerEventLog(session, task);
//            doProtocol();
//        } finally {
//            this.session.disconnect();
//        }
//        logger.info("Protocol session [" + scheduler.displayString() + "] finished. Storing data.");
//        this.task.getStoreObject().doExecute();
//    }

    public long getTimeDifference() {
        return clock.getTimeDifference();
    }

    public String getVersion() {
        return "$Date$";
    }
//
//    @Override
//    public void addProperties(TypedProperties properties) {
//        addProperties(properties.toStringProperties());
//    }
//
//    @Override
//    public List<PropertySpec> getRequiredProperties() {
//        return PropertySpecFactory.toPropertySpecs(getRequiredKeys());
//    }
//
//    @Override
//    public List<PropertySpec> getOptionalProperties() {
//        return PropertySpecFactory.toPropertySpecs(getOptionalKeys());
//    }

    public void addProperties(Properties properties) {
        this.properties.addProperties(properties);
    }

    public List<String> getRequiredKeys() {
        return this.properties.getRequiredKeys();
    }

    public List<String> getOptionalKeys() {
        return this.properties.getOptionalKeys();
    }

//    private final void doProtocol() throws IOException, BusinessException {
//        this.info.validateSerialNumber();
//        updateEiServerTopology();
//        task.scheduleSlaveDevices();
////        this.clock.validateClock();
////        this.eventLog.readNewEvents();
//    }

    private final void updateEiServerTopology() throws IOException {
        final SAPAssignment sapAssignment = this.session.getCosemObjectFactory().getSAPAssignment();
        final List<SAPAssignmentItem> sapAssignmentList = sapAssignment.getSapAssignmentList();
//        task.updateEIServerTopology(sapAssignmentList);
    }

}
