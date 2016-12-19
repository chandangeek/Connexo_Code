Ext.define('Dal.controller.Alarms', {
    extend: 'Isu.controller.IssuesOverview',
    requires: [
        'Dal.view.AlarmFilter',
        'Dal.view.NoAlarmsFoundPanel',
        'Dal.view.Preview',
        'Dal.store.Alarms',
        'Dal.view.Grid'
    ],

    stores: [
        'Dal.store.Alarms',
        'Dal.store.AlarmAssignees',
        'Dal.store.AlarmReasons',
        'Dal.store.AlarmStatuses',
        'Dal.store.AlarmWorkgroupAssignees',
        'Dal.store.ClearStatus',
        'Dal.store.DueDate'
    ],

    models: [
        'Dal.model.ClearStatus',
        'Dal.model.Device',
        'Dal.model.DueDate',
        'Dal.model.AlarmAssignee',
        'Dal.model.AlarmReason',
        'Dal.model.AlarmStatus',
        'Dal.model.Device'
    ],


    constructor: function () {
        var me = this;
        me.refs =
            [
                {
                    ref: 'preview',
                    selector: 'issues-overview #alarm-preview'
                },
                {
                    ref: 'previewStatus',
                    selector: 'issues-overview #alarms-preview #alarm-status'
                },
                {
                    ref: 'filterToolbar',
                    selector: 'issues-overview view-alarms-filter'
                },
                {
                    ref: 'groupingToolbar',
                    selector: 'issues-overview #issues-grouping-toolbar'
                },
                {
                    ref: 'groupGrid',
                    selector: 'issues-overview #issues-group-grid'
                },
                {
                    ref: 'previewContainer',
                    selector: 'issues-overview #issues-preview-container'
                },
                {
                    ref: 'groupingTitle',
                    selector: 'issues-overview issues-grouping-title'
                },
                {
                    ref: 'issuesGrid',
                    selector: 'issues-overview #alarms-grid'
                },
                {
                    ref: 'previewActionMenu',
                    selector: '#alarm-preview alarms-action-menu'
                }
            ]
        me.callParent(arguments);
    },

    init: function () {
        var me = this;
        me.control({
            'issues-overview #alarms-grid': {
                select: me.showPreview
            },
            'issues-overview #alarm-preview #filter-display-button': {
                click: this.setFilterItem
            }
        });
    },

    showOverview: function () {
        var me = this,
            queryString = Uni.util.QueryString.getQueryStringValues(false);

        if (!queryString.sort) {
            queryString.sort = ['dueDate'];
            window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
        } else {
            me.getStore('Isu.store.Clipboard').set('latest-issues-filter', queryString);
            var widget = Ext.widget('issues-overview', {
                router: me.getController('Uni.controller.history.Router'),
                groupingType: queryString.groupingType,
                filter: {
                    xtype: 'view-alarms-filter',
                    itemId: 'view-alarms-filter'
                },
                emptyComponent: {
                    xtype: 'no-alarms-found-panel',
                    itemId: 'no-alarms-found-panel'
                },
                previewComponent: {
                    xtype: 'alarm-preview',
                    itemId: 'alarm-preview',
                    fieldxtype: 'filter-display'
                },
                grid: {
                    store: 'Dal.store.Alarms',
                    xtype: 'alarms-grid',
                    itemId: 'alarms-grid'
                }
            });

            me.setOverviewAlarmComponets(widget);
            me.getApplication().fireEvent('changecontentevent', widget);
        }
    },

    setOverviewAlarmComponets: function (widget) {
        widget.down('#issue-panel').setTitle(Uni.I18n.translate('device.alarms', 'DAL', 'Alarms'));
        widget.down('#issues-grouping-toolbar').setVisible(false);
        widget.down('#issues-group-grid').setVisible(false);
        widget.down('menuseparator').setVisible(false);
        widget.down('#issues-grouping-title').setVisible(false);
    },

    showPreview: function (selectionModel, record) {
        this.callParent(arguments);
        var subEl = new Ext.get('alarm-status-field-sub-tpl');
        subEl.setHTML(record.get('statusDetail'));
    },

    setFilterItem: function (button) {
        var me = this;

        switch (button.filterBy) {
            case 'alarmId':
                button.filterBy = 'id';
                break;
            case 'reasonName':
                button.filterBy = 'reason';
                break;
        }
        me.callParent(arguments);
    }
});
