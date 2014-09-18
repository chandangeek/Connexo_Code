package com.energyict.mdc.dashboard.rest.status.impl;

import com.elster.jupiter.nls.Thesaurus;
import com.energyict.mdc.dashboard.ComPortPoolBreakdown;
import com.energyict.mdc.dashboard.ComSessionSuccessIndicatorOverview;
import com.energyict.mdc.dashboard.ConnectionTypeBreakdown;
import com.energyict.mdc.dashboard.DeviceTypeBreakdown;
import com.energyict.mdc.dashboard.TaskStatusOverview;
import com.energyict.mdc.device.data.rest.TaskStatusAdapter;
import java.util.ArrayList;
import java.util.Comparator;
import javax.inject.Inject;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Created by bvn on 9/18/14.
 */
public class ConnectionOverviewInfoFactory {
    public static final Comparator<TaskCounterInfo> TASK_COUNTER_INFO_COMPARATOR = new Comparator<TaskCounterInfo>() {
        @Override
        public int compare(TaskCounterInfo o1, TaskCounterInfo o2) {
            return -Long.valueOf(o1.count).compareTo(o2.count);
        }
    };

    public static final Comparator<TaskBreakdownInfo> TASK_BREAKDOWN_INFO_COMPARATOR = new Comparator<TaskBreakdownInfo>() {
        @Override
        public int compare(TaskBreakdownInfo o1, TaskBreakdownInfo o2) {
            return -Long.valueOf(o1.failedCount).compareTo(o2.failedCount);
        }
    };

    @JsonIgnore
    private static final TaskStatusAdapter taskStatusAdapter = new TaskStatusAdapter();
    @JsonIgnore
    private static final ComSessionSuccessIndicatorAdapter COM_SESSION_SUCCESS_INDICATOR_ADAPTER = new ComSessionSuccessIndicatorAdapter();



    private final BreakdownFactory breakdownFactory;
    private final OverviewFactory overviewFactory;
    private final SummaryInfoFactory summaryInfoFactory;
    private final Thesaurus thesaurus;

    @Inject
    public ConnectionOverviewInfoFactory(BreakdownFactory breakdownFactory, OverviewFactory overviewFactory, SummaryInfoFactory summaryInfoFactory, Thesaurus thesaurus) {
        this.breakdownFactory = breakdownFactory;
        this.overviewFactory = overviewFactory;
        this.summaryInfoFactory = summaryInfoFactory;
        this.thesaurus = thesaurus;
    }

    public ConnectionOverviewInfo from(
            SummaryData summaryData,
            TaskStatusOverview taskStatusOverview,
            ComSessionSuccessIndicatorOverview comSessionSuccessIndicatorOverview,
            ComPortPoolBreakdown comPortPoolBreakdown,
            ConnectionTypeBreakdown connectionTypeBreakdown,
            DeviceTypeBreakdown deviceTypeBreakdown) {

        ConnectionOverviewInfo info = new ConnectionOverviewInfo();
        info.connectionSummary = summaryInfoFactory.from(summaryData);

        info.overviews=new ArrayList<>(2);
        info.overviews.add(overviewFactory.createOverview(thesaurus.getString(MessageSeeds.PER_CURRENT_STATE.getKey(), MessageSeeds.PER_CURRENT_STATE.getDefaultFormat()), taskStatusOverview, FilterOption.currentStates, taskStatusAdapter)); // JP-4278
        info.overviews.add(overviewFactory.createOverview(thesaurus.getString(MessageSeeds.PER_LATEST_RESULT.getKey(), MessageSeeds.PER_LATEST_RESULT.getDefaultFormat()), comSessionSuccessIndicatorOverview, FilterOption.latestResults, COM_SESSION_SUCCESS_INDICATOR_ADAPTER)); // JP-4280
        overviewFactory.sortAllOverviews(info.overviews);

        info.breakdowns=new ArrayList<>(3);
        info.breakdowns.add(breakdownFactory.createBreakdown(thesaurus.getString(MessageSeeds.PER_COMMUNICATION_POOL.getKey(), MessageSeeds.PER_COMMUNICATION_POOL.getDefaultFormat()), comPortPoolBreakdown, FilterOption.comPortPools)); // JP-4281
        info.breakdowns.add(breakdownFactory.createBreakdown(thesaurus.getString(MessageSeeds.PER_CONNECTION_TYPE.getKey(), MessageSeeds.PER_CONNECTION_TYPE.getDefaultFormat()), connectionTypeBreakdown, FilterOption.connectionTypes)); // JP-4283
        info.breakdowns.add(breakdownFactory.createBreakdown(thesaurus.getString(MessageSeeds.PER_DEVICE_TYPE.getKey(), MessageSeeds.PER_DEVICE_TYPE.getDefaultFormat()), deviceTypeBreakdown, FilterOption.deviceTypes)); // JP-4284
        breakdownFactory.sortAllBreakdowns(info.breakdowns);

        return info;
    }

}
