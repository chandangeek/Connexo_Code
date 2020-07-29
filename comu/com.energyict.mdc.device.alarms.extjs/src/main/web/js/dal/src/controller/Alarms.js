/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dal.controller.Alarms', {
    extend: 'Isu.controller.IssuesOverview',
    requires: [
        'Dal.view.AlarmFilter',
        'Dal.view.Overview',
        'Dal.view.NoAlarmsFoundPanel',
        'Dal.view.Preview',
        'Dal.store.Alarms',
        'Dal.view.Grid',
        'Dal.view.ActionMenu',
        'Dal.view.AlarmSortingToolbar'
    ],

    stores: [
        'Dal.store.Alarms',
        'Dal.store.AlarmAssignees',
        'Dal.store.AlarmReasons',
        'Dal.store.AlarmStatuses',
        'Dal.store.AlarmWorkgroupAssignees',
        'Dal.store.ClearStatus',
        'Dal.store.DueDate',
        'Dal.store.Devices',
        'Dal.store.AlarmAssignees',
        'Dal.store.DeviceGroups'
    ],

    models: [
        'Dal.model.ClearStatus',
        'Dal.model.Device',
        'Dal.model.DueDate',
        'Dal.model.AlarmAssignee',
        'Dal.model.AlarmReason',
        'Dal.model.AlarmStatus',
        'Dal.model.Device',
        'Dal.model.DeviceGroup'
    ],



    constructor: function () {
        var me = this;
        me.refs =
            [
                {
                    ref: 'preview',
                    selector: 'alarm-overview #alarm-preview'
                },
                {
                    ref: 'previewStatus',
                    selector: 'alarm-overview #alarms-preview #alarm-status'
                },
                {
                    ref: 'filterToolbar',
                    selector: 'alarm-overview view-alarms-filter'
                },
                {
                    ref: 'groupingToolbar',
                    selector: 'alarm-overview #issues-grouping-toolbar'
                },
                {
                    ref: 'groupGrid',
                    selector: 'alarm-overview #issues-group-grid'
                },
                {
                    ref: 'previewContainer',
                    selector: 'alarm-overview #issues-preview-container'
                },
                {
                    ref: 'groupingTitle',
                    selector: 'alarm-overview issues-grouping-title'
                },
                {
                    ref: 'issuesGrid',
                    selector: 'alarm-overview #alarms-grid'
                },
                {
                    ref: 'previewActionMenu',
                    selector: '#alarm-preview alarms-action-menu'
                },
                {
                    ref: 'countButton',
                    selector: '#alarms-count-action'
                }
            ];
        me.views = [
            'Dal.view.Overview'
        ];
        me.callParent(arguments);
    },

    init: function () {
        var me = this;
        me.control({
            'alarm-overview #alarms-grid': {
                select: me.showPreview
            },
            'alarm-overview #alarm-preview #filter-display-button': {
                click: this.setFilterItem
            },
            '#alarms-count-action': {
                click: this.countAlarms
             }
        });
    },

    showOverview: function () {
        var me = this,
            queryString = Uni.util.QueryString.getQueryStringValues(false);

        if (_.values(queryString).length == 0){
            var latestQueryString = this.getStore('Isu.store.Clipboard').get('latest-issues-filter');
            if (latestQueryString) {
                queryString = latestQueryString;
                window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
            }
        }
        if (queryString.myopenalarms) {
            me.getStore('Dal.store.AlarmAssignees').load({
                params: {me: true},
                callback: function (records) {
                    queryString.myopenalarms = undefined;
                    queryString.userAssignee = records[0].getId();
                    queryString.sort = ['-priorityTotal'];
                    window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
                }
            });
        } else if (queryString.myworkgroupalarms) {
            Ext.Ajax.request({
                url: '/api/dal/workgroups?myworkgroups=true',
                method: 'GET',
                success: function (response) {
                    var decoded = response.responseText ? Ext.decode(response.responseText, true) : null;
                    if (decoded && decoded.workgroups) {
                        queryString.myworkgroupalarms = undefined;
                        queryString.userAssignee = [-1];
                        queryString.workGroupAssignee = decoded.workgroups.length == 0 ? [-1] : decoded.workgroups.map(function (wg) {
                            return wg.id;
                        });
                        queryString.sort = ['-priorityTotal'];
                        window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
                    }
                }
            });
        } else if (!queryString.userAssignee && !queryString.myworkgroupalarms && !queryString.status) {
            queryString.status = ['status.open', 'status.in.progress'];
            queryString.sort = ['-priorityTotal'];
            window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
        } else if (!queryString.sort) {
            queryString.sort = ['-priorityTotal'];
            window.location.replace(Uni.util.QueryString.buildHrefWithQueryString(queryString, false));
        } else {
            me.getStore('Isu.store.Clipboard').set('latest-issues-filter', queryString);
            var widget = Ext.widget('alarm-overview', {
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
        subEl.setHTML('<div>' + record.get('statusDetailCleared') + '</div>'
            + '<div>' + record.get('statusDetailSnoozed') + '</div>');
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
    },
    countAlarms: function(){
        var me = this;
        me.fireEvent('loadingcount');
        Ext.Ajax.suspendEvent('requestexception');
        me.getCountButton().up('panel').setLoading(true);
        var filters = [];
        var queryStringValues = Uni.util.QueryString.getQueryStringValues(false);
        for (property in queryStringValues){
            if (property !== 'sort'){
                filters.push({'property' : property, value: queryStringValues[property] })
            }
        };
        var queryString = Ext.Object.toQueryString({"filters" : filters}, true);
        Ext.Ajax.request({
            url: Ext.getStore('Dal.store.Alarms').getProxy().url + '/count',
            timeout: 120000,
            params: {
                 filter: JSON.stringify(filters)
            },
            method: 'GET',
            success: function (response) {
                me.getCountButton().setText(response.responseText);
                me.getCountButton().setDisabled(true);
                me.getCountButton().up('panel').setLoading(false);
            },
            failure: function (response, request) {
                var box = Ext.create('Ext.window.MessageBox', {
                    buttons: [
                        {
                            xtype: 'button',
                            text: Uni.I18n.translate('general.close', 'DAL', 'Close'),
                            action: 'close',
                            name: 'close',
                            ui: 'remove',
                            handler: function () {
                                box.close();
                            }
                        }
                    ],
                    listeners: {
                        beforeclose: {
                            fn: function () {
                                me.getCountButton().setDisabled(true);
                                me.getCountButton().up('panel').setLoading(false);
                            }
                        }
                    }
                });

                box.show({
                    title: Uni.I18n.translate('general.timeOut', 'DAL', 'Time out'),
                    msg: Uni.I18n.translate('general.timeOutMessageAlarms', 'DAL', 'Counting the issues took too long.'),
                    modal: false,
                    ui: 'message-error',
                    icon: 'icon-warning2',
                    style: 'font-size: 34px;'
                });
            }
        });
    }
});
