package com.elster.jupiter.demo.impl.generators;

import com.elster.jupiter.demo.impl.Store;
import com.elster.jupiter.demo.impl.UnableToCreate;
import com.elster.jupiter.issue.share.entity.CreationRule;
import com.elster.jupiter.issue.share.entity.Issue;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.metering.AmrSystem;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.KnownAmrSystem;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.data.tasks.ConnectionTask;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

public class IssueGenerator {
    private static final String INSERT_INTO_ISSUE = "INSERT INTO ISU_ISSUE_OPEN (ID, DUE_DATE, REASON_ID, STATUS, DEVICE_ID, OVERDUE, RULE_ID, VERSIONCOUNT, CREATETIME, MODTIME, USERNAME)" +
            " VALUES (?, ?, ?, ?, ?, 0, ?, ?, ?, ?, ?)";
    private static final String INSERT_INTO_DC_ISSUE = "Insert into IDC_ISSUE_OPEN (ID,ISSUE,CON_TASK,VERSIONCOUNT,CREATETIME,MODTIME,USERNAME) " +
            "values (?, ?, ?, ?, ?, ? ,?)";
    private static final String ISSUE_REASON_KEY = "reason.connection.failed";
    private static final String ISSUE_STATUS_KEY = IssueStatus.OPEN;
    private static final int ISSUE_VERSION = 1;
    public static final String ISSUE_AUTHOR = "Jupiter installer";
    public static final String ISSUE_SEQUENCE = "ISU_ISSUE_OPENID";

    private final DataModel dataModel;
    private final MeteringService meteringService;
    private final Store store;
    private final IssueService issueService;

    private Device device;
    private Instant dueDate;
    private String issueReason;

    @Inject
    public IssueGenerator(Store store, DataModel dataModel, MeteringService meteringService, IssueService issueService) {
        this.store = store;
        this.dataModel = dataModel;
        this.meteringService = meteringService;
        this.issueService = issueService;
        this.issueReason = ISSUE_REASON_KEY;
    }

    public IssueGenerator withDevice(Device device) {
        this.device = device;
        return this;
    }

    public IssueGenerator withDueDate(Instant dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    public IssueGenerator withIssueReason(String issueReason){
        this.issueReason = issueReason;
        return this;
    }

    public void create() {
        System.out.println("==> Creating issue for device " + device.getmRID());
        try (Connection connection = this.dataModel.getConnection(false)) {
            List<CreationRule> creationRules = store.get(CreationRule.class);
            if (creationRules.isEmpty()){
                throw new UnableToCreate("There is no defined creation rules");
            }
            Instant currentTime = Instant.now();
            List<ConnectionTask<?, ?>> tasks = device.getConnectionTasks();
            if (tasks.isEmpty()){
                throw new UnableToCreate("There is no connection tasks on device " + device.getmRID());
            }
            if (dueDate == null){
                dueDate = currentTime.plus(14, ChronoUnit.DAYS);
            }
            long baseIssueId = getNext(connection, ISSUE_SEQUENCE);
            createBaseIssue(connection, creationRules, currentTime, baseIssueId);
            createDataCollectionIssue(connection, tasks, currentTime, baseIssueId);
            store.add(Issue.class, issueService.findOpenIssue(baseIssueId).get());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createDataCollectionIssue(Connection connection, List<ConnectionTask<?, ?>> tasks, Instant currentTime, long baseIssueId) throws SQLException {
        PreparedStatement statement;
        statement = connection.prepareStatement(INSERT_INTO_DC_ISSUE);
        statement.setLong(1, baseIssueId); /* issue dc id */
        statement.setLong(2, baseIssueId); /* parent issue id */
        statement.setLong(3, tasks.get(0).getId()); /* connection task reference */
        statement.setInt(4, ISSUE_VERSION); /* current version of issue */
        statement.setLong(5, currentTime.toEpochMilli()); /* creation time */
        statement.setLong(6, currentTime.toEpochMilli()); /* modification time */
        statement.setString(7, ISSUE_AUTHOR); /* created by user */
        statement.execute();
    }

    private void createBaseIssue(Connection connection, List<CreationRule> creationRules, Instant currentTime, long baseIssueId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(INSERT_INTO_ISSUE);
        statement.setLong(1, baseIssueId); /* issue id */
        statement.setLong(2, dueDate.toEpochMilli()); /* issue id */
        statement.setString(3, this.issueReason); /* reason reference */
        statement.setString(4, ISSUE_STATUS_KEY); /* status reference */
        statement.setLong(5, getEndDevice().getId()); /* device reference - to the kore meter */
        statement.setLong(6, creationRules.get(0).getId()); /* creation rule reference */
        statement.setInt(7, ISSUE_VERSION); /* current version of issue */
        statement.setLong(8, currentTime.toEpochMilli()); /* creation time */
        statement.setLong(9, currentTime.toEpochMilli()); /* modification time */
        statement.setString(10, ISSUE_AUTHOR); /* created by user */
        statement.execute();
    }

    private long getNext(Connection connection, String sequence) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select " + sequence + ".nextval from dual")) {
            try (ResultSet rs = statement.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private EndDevice getEndDevice() {
        Optional<AmrSystem> amrSystemRef = meteringService.findAmrSystem(KnownAmrSystem.MDC.getId());
        if (amrSystemRef.isPresent()) {
            Optional<Meter> meterRef = amrSystemRef.get().findMeter(device.getmRID());
            if (meterRef.isPresent()) {
                return meterRef.get();
            } else {
                Meter meter = amrSystemRef.get().newMeter(device.getmRID());
                meter.save();
                return meter;
            }
        }
        throw new UnableToCreate("Unable to retrieve the MDC amr system. Unable to create issues");
    }
}
