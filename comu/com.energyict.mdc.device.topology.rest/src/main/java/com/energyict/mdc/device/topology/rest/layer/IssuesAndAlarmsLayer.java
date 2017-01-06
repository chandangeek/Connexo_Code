package com.energyict.mdc.device.topology.rest.layer;

import com.elster.jupiter.domain.util.Finder;
import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.nls.TranslationKey;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.device.topology.rest.GraphLayer;
import com.energyict.mdc.device.topology.rest.GraphLayerType;
import com.energyict.mdc.device.topology.rest.info.DeviceNodeInfo;
import com.energyict.mdc.device.topology.rest.info.NodeInfo;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionFilter;
import com.energyict.mdc.issue.datacollection.IssueDataCollectionService;
import com.energyict.mdc.issue.datacollection.entity.IssueDataCollection;
import com.elster.jupiter.issue.share.service.IssueService;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Copyrights EnergyICT
 * Date: 5/01/2017
 * Time: 15:05
 */
@Component(name = "com.energyict.mdc.device.topology.IssuesAndAlarmsLayer", service = GraphLayer.class, immediate = true)
@SuppressWarnings("unused")
public class IssuesAndAlarmsLayer  extends AbstractGraphLayer<Device> {

    private IssueService issueService;
    private IssueDataCollectionService issueDataCollectionService;

    private final static String NAME = "topology.GraphLayer.IssuesAndAlarms";

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
            return NAME + ".node." + propertyName;    //topology.graphLayer.deviceInfo.node.xxxx
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
        return NAME;
    }

    @Reference
    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    @Reference
    public void setIssueDataCollectionService(IssueDataCollectionService issueDataCollectionService) {
        this.issueDataCollectionService = issueDataCollectionService;
    }

    private void countIssuesAndAlarms(DeviceNodeInfo info) {
        Device device = info.getDevice();
        IssueDataCollectionFilter filter = new IssueDataCollectionFilter();
        filter.addStatus(issueService.findStatus(IssueStatus.OPEN).get());
        filter.addDevice(device.getCurrentMeterActivation().get().getMeter().get());
        Finder<? extends IssueDataCollection> finder = issueDataCollectionService.findIssues(filter);
        setIssues(finder.stream().count());
//            //Todo
//            setAlarms(-1);

    }

    @Override
    public Map<String, Object> getProperties(NodeInfo<Device> info) {
        countIssuesAndAlarms(((DeviceNodeInfo) info));
        return propertyMap();
    }

    private void setIssues(long count) {
        this.setProperty(PropertyNames.ISSUE_COUNT.getPropertyName(), "" + count);
    }

    private void setAlarms(long count) {
        this.setProperty(PropertyNames.ALARM_COUNT.getPropertyName(), "" + count);
    }

    @Override
    public List<TranslationKey> getKeys() {
        return Arrays.asList(PropertyNames.values());
    }
}
