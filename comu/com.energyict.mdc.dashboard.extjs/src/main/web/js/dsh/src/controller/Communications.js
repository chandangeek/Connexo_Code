/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.controller.Communications', {
    extend: 'Ext.app.Controller',

    models: [
        'Dsh.model.ConnectionTask',
        'Dsh.model.CommTasks',
        'Dsh.model.CommunicationTask',
        'Dsh.model.Filter'
    ],

    views: [
        'Dsh.view.Communications',
        'Dsh.view.widget.PreviewCommunication',
        'Dsh.view.widget.PreviewConnection'
    ],

    stores: [
        'Dsh.store.CommunicationTasks',
        'Dsh.store.filter.CommunicationSchedule',
        'Dsh.store.filter.CommunicationTask',
        'Dsh.store.filter.CurrentState',
        'Dsh.store.filter.LatestResult',
        'Dsh.store.filter.ConnectionType',
        'Dsh.store.filter.DeviceType'
    ],

    refs: [
        {
            ref: 'communicationPreview',
            selector: '#communicationsdetails #communicationdetails'
        },
        {
            ref: 'connectionPreview',
            selector: '#communicationsdetails #connectiondetails'
        },
        {
            ref: 'filterPanel',
            selector: '#communicationsdetails dsh-view-widget-communicationstopfilter'
        },
        {
            ref: 'communicationsGrid',
            selector: 'communications-list'
        },
        {
            ref: 'communicationsGridActionMenu',
            selector: '#communicationsGridActionMenu'
        },
        {
            ref: 'communicationPreviewActionMenu',
            selector: '#communicationPreviewActionMenu'
        },
        {
            ref: 'connectionsPreviewActionBtn',
            selector: '#connectionsPreviewActionBtn'
        }
    ],

    init: function () {
        this.control({
            '#communicationsdetails #communicationslist': {
                selectionchange: this.onSelectionChange
            },
            'communications-list #generate-report': {
                click: this.onGenerateReport
            },
            'communications-list #btn-communications-bulk-action': {
                click: this.forwardToBulk
            },
            'communications-action-menu': {
                click: this.chooseAction
            },
            'communications-details connection-action-menu': {
                click: this.onConnectionActionMenuClick
            }
        });

        this.callParent(arguments);
    },

    showOverview: function () {
        var me = this,
            widget = Ext.widget('communications-details'),
            store = me.getStore('Dsh.store.CommunicationTasks');

        me.getApplication().fireEvent('changecontentevent', widget);
        store.load();
    },

    chooseAction: function (menu, item) {
        var me = this;

        switch (item.action) {
            case 'viewLog':
                me.viewCommunicationLog(menu.record);
                break;
            case 'run':
                me.communicationRun(menu.record);
                break;
            case 'runNow':
                me.communicationRunNow(menu.record);
                break;
        }
    },

    onConnectionActionMenuClick: function (menu, item) {
        var me = this;
        switch (item.action) {
            case 'viewLog':
                me.viewConnectionLog(menu.record);
                break;
        }
    },

    initMenu: function (record, menuItems) {
        var me = this;

        me.getConnectionsPreviewActionBtn().menu.removeAll();

        Ext.suspendLayouts();

        var connectionMenuItem = {
            text: Uni.I18n.translate('connection.widget.details.connectionMenuItem', 'DSH', 'View connection log'),
            action: {
                action: 'viewlog',
                connection: {
                    deviceId: record.get('device').name,
                    connectionMethodId: record.get('connectionTask').id,
                    sessionId: record.get('connectionTask').comSessionId

                }
            },
            listeners: {
                click: me.viewConnectionLog
            }
        };


        if (record.get('connectionTask').comSessionId !== 0) {
            me.getConnectionsPreviewActionBtn().menu.add(connectionMenuItem);
        }

        Ext.resumeLayouts(true);
    },

    onSelectionChange: function (grid, selected) {
        var me = this,
            preview = me.getCommunicationPreview(),
            connPreview = me.getConnectionPreview(),
            record = selected[0];


        if (record) {
            if(record.get('connectionTask').comSessionId !== 0) {
                connPreview.down('uni-button-action').setDisabled(false);
            } else {
                connPreview.down('uni-button-action').setDisabled(true);
            }
            preview.down('communications-action-menu').record = record;
            connPreview.down('connection-action-menu').record = record;
            preview.loadRecord(record);
            preview.setTitle(Uni.I18n.translate('general.XonY', 'DSH', '{0} on {1}', [record.get('name'), record.get('device').name]));
            if (record.getData().connectionTask) {
                var conTask = record.getConnectionTask();
                connPreview.setTitle(Uni.I18n.translate('general.XonY', 'DSH', '{0} on {1}', [conTask.get('connectionMethod').name, conTask.get('device').name]));
                connPreview.show();
                connPreview.loadRecord(conTask);
            } else {
                connPreview.hide()
            }
        }
    },

    viewCommunicationLog: function (record) {
        location.href = '#/devices/' +record.get('device').name
        + '/communicationtasks/' + record.get('comTask').id
        + '/history/' + record.get('sessionId')
        + '/viewlog' +
        '?logLevels=Error&logLevels=Warning&logLevels=Information';
    },

    viewConnectionLog: function (record) {
        location.href = '#/devices/' + record.get('device').name + '/connectionmethods/' + record.get('connectionTask').id + '/history/' + record.get('connectionTask').comSessionId + '/viewlog' +
        '?logLevels=Error&logLevels=Warning&logLevels=Information&communications=Connections&communications=Communications'
    },

    onGenerateReport: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            fieldsToFilterNameMap = {},
            reportFilter = false,
            filterName = undefined,
            fieldValue = undefined,
            filters = me.getFilterPanel().filters;

        fieldsToFilterNameMap['deviceGroups'] = 'GROUPNAME';
        fieldsToFilterNameMap['currentStates'] = 'STATUS';
        fieldsToFilterNameMap['latestResults'] = null;
        fieldsToFilterNameMap['comSchedules'] = 'SCHEDULENAME';
        fieldsToFilterNameMap['deviceTypes'] = null;
        fieldsToFilterNameMap['comTasks'] = 'COMTASKNAME';
        fieldsToFilterNameMap['startInterval'] = 'CONNECTIONDATE';
        // TODO Check if finished interval is even supported by the Yellowfin report.
        //fieldsToFilterNameMap['finishInterval'] = 'CONNECTIONDATE-FINISH';

        var reportFilter = {};

        Ext.iterate(me.getFilterPanel().getFilterDisplayParams(), function (filterKey, filterValue) {
            var filterName = fieldsToFilterNameMap[filterKey];

            if (filterName && !Ext.isEmpty(filterValue)) {
                reportFilter[filterName] = filterValue;
            } else if (filterKey === 'startIntervalFrom' || filterKey === 'finishIntervalTo') {
                if (!reportFilter['CONNECTIONDATE']) {
                    reportFilter['CONNECTIONDATE'] = {};
                }
                reportFilter['CONNECTIONDATE'][filterKey === 'startIntervalFrom' ? 'from' : 'to'] = Ext.Date.format(new Date(filterValue), 'Y-m-d H:i:s');
            }
        });

        router.getRoute('workspace/generatereport').forward(null, {
            category: 'MDC',
            subCategory: 'Device Communication',
            filter: Ext.JSON.encode(reportFilter)
        });
    },

    communicationRun: function (record) {
        var me = this;
        record.run(function () {
            me.getApplication().fireEvent('acknowledge',
                Uni.I18n.translate('general.runSucceeded', 'DSH', 'Run succeeded')
            );
            record.set('plannedDate', new Date());
            me.showOverview();
        });
    },

    communicationRunNow: function (record) {
        var me = this;
        record.runNow(function () {
            me.getApplication().fireEvent('acknowledge',
                Uni.I18n.translate('device.communication.run.now', 'DSH', 'Run now succeeded')
            );
            record.set('plannedDate', new Date());
            me.showOverview();
        });
    },

    forwardToBulk: function () {
        location.href = '#/workspace/communications/details/bulk?' + Uni.util.QueryString.getQueryString();
    }
});
