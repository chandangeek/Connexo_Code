package com.energyict.mdc.device.topology.rest.layer;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.alarms.DeviceAlarmFilter;
import com.energyict.mdc.device.alarms.DeviceAlarmService;
import com.energyict.mdc.device.alarms.entity.DeviceAlarm;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerType;
import com.energyict.mdc.device.topology.rest.info.DeviceNodeInfo;
import com.energyict.mdc.device.topology.rest.info.NodeInfo;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionFilter;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;
import com.elster.jupiter.issue.share.service.IssueService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.OrmService;
import com.elster.jupiter.orm.UnderlyingSQLFailedException;
import com.elster.jupiter.util.sql.SqlBuilder;
import com.elster.jupiter.util.sql.SqlFragment;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Gathers the issue and alarm information of a DeviceNodeInfo
 * Copyrights EnergyICT
 * Date: 5/01/2017
 * Time: 15:05
 */
@Component(name = "com.energyict.mdc.device.topology.IssuesAndAlarmsLayer", service = GraphLayer.class, immediate = true)
@SuppressWarnings("unused")
public class IssuesAndAlarmsLayer  extends AbstractGraphLayer<Device> {

    private IssueService issueService;
    private IssueDataCollectionService issueDataCollectionService;
    private DeviceAlarmService deviceAlarmService;
    private OrmService ormService;

    public final static String NAME = "topology.GraphLayer.IssuesAndAlarms";

    public enum PropertyNames implements TranslationKey {
        ISSUE_COUNT("issues", "Issues"),
        ALARM_COUNT("alarms", "Alarms");

        private String propertyName;
        private String defaultFormat;

        PropertyNames(String propertyName, String defaultFormat) {
            this.propertyName = propertyName;
            this.defaultFormat = defaultFormat;
        }

        @Override
        public String getKey() {
            return LayerNames.IssuesAndAlarmLayer.fullName() + ".node." + propertyName;    //topology.graphLayer.deviceInfo.node.xxxx
        }

        public String getPropertyName() {
            return propertyName;
        }

        @Override
        public String getDefaultFormat() {
            return defaultFormat;
        }

    }

    @Override
    public GraphLayerType getType() {
        return GraphLayerType.NODE;
    }

    @Override
    public String getName() {
        return LayerNames.IssuesAndAlarmLayer.fullName();
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setIssueDataCollectionService(IssueDataCollectionService issueDataCollectionService) {
        this.issueDataCollectionService = issueDataCollectionService;
    }

    @Reference
    public void setDeviceAlarmService(DeviceAlarmService deviceAlarmService) {
        this.deviceAlarmService = deviceAlarmService;
    }

    @Reference
    public void setOrmService(OrmService ormService) {
        this.ormService = ormService;
    }

    private void countIssuesAndAlarms(DeviceNodeInfo info) {
        Device device = info.getDevice();
        EndDevice meter = device.getCurrentMeterActivation().get().getMeter().get();
        IssueStatus openStatus = issueService.findStatus(IssueStatus.OPEN).get();

        IssueDataCollectionFilter filter = new IssueDataCollectionFilter();
        filter.addStatus(openStatus);
        filter.addDevice(meter);
        setIssues(countIssues(issueDataCollectionService.findIssues(filter)));

        DeviceAlarmFilter alarmFilter = new DeviceAlarmFilter();
        alarmFilter.setDevice(meter);
        alarmFilter.setStatus(openStatus);
        setAlarms(countAlarms(deviceAlarmService.findAlarms(alarmFilter)));
    }

    @Override
    public Map<String, Object> getProperties(NodeInfo<Device> info) {
        countIssuesAndAlarms(((DeviceNodeInfo) info));
        return propertyMap();
    }

    private void setIssues(long count) {
        this.setProperty(PropertyNames.ISSUE_COUNT.getPropertyName(), count);
    }

    private void setAlarms(long count) {
        this.setProperty(PropertyNames.ALARM_COUNT.getPropertyName(), count);
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(PropertyNames.values());
    }

    private long countIssues(Finder<? extends IssueDataCollection> issueFinder) {
        return count(issueFinder, IssueDataCollectionService.COMPONENT_NAME );
    }

    private long countAlarms(Finder<? extends DeviceAlarm> alarmFinder) {
        return count(alarmFinder, DeviceAlarmService.COMPONENT_NAME );
    }

    private SqlFragment asFragment(Finder finder, String... fieldNames) {
        return finder.asFragment(fieldNames);
    }

    private long count(Finder finder, String componentName ){
        Optional<DataModel> dataModel = ormService.getDataModel(componentName);
        if (dataModel.isPresent()) {
            try (Connection connection = dataModel.get().getConnection(false)) {
                SqlBuilder countSqlBuilder = new SqlBuilder();
                countSqlBuilder.add(asFragment(finder, "count(*)"));
                try (PreparedStatement statement = countSqlBuilder.prepare(connection)) {
                    try (ResultSet resultSet = statement.executeQuery()) {
                        resultSet.next();
                        return resultSet.getInt(1);
                    }
                }
            } catch (SQLException e) {
                throw new UnderlyingSQLFailedException(e);
            }
        }
        return finder.find().stream().count();
    }

}
