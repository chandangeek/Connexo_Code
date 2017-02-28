/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

Ext.define('Dsh.controller.Connections', {
    extend: 'Ext.app.Controller',

    models: [
        'Dsh.model.ConnectionTask',
        'Dsh.model.CommTasks',
        'Dsh.model.CommunicationTask',
        'Dsh.model.Filter'
    ],

    stores: [
        'Dsh.store.ConnectionTasks',
        'Dsh.store.Communications'
    ],

    views: [
        'Dsh.view.Connections',
        'Dsh.view.widget.PreviewConnection',
        'Dsh.view.widget.connection.CommunicationsList',
        'Dsh.view.widget.connection.PreviewCommunication'
    ],

    refs: [
        {
            ref: 'connectionsList',
            selector: '#connectionsdetails'
        },
        {
            ref: 'connectionPreview',
            selector: '#connectionsdetails #connectionpreview'
        },
        {
            ref: 'communicationList',
            selector: '#connectionsdetails #communicationsdetails'
        },
        {
            ref: 'communicationPreview',
            selector: '#connectionsdetails #communicationpreview'
        },
        {
            ref: 'communicationsPanel',
            selector: '#connectionsdetails #communicationspanel'
        },
        {
            ref: 'filterPanel',
            selector: '#connectionsdetails dsh-view-widget-connectionstopfilter'
        },
        {
            ref: 'sideFilterForm',
            selector: '#connectionsdetails #filter-form'
        },
        {
            ref: 'connectionsActionMenu',
            selector: '#connectionsActionMenu'
        },
        {
            ref: 'connectionsPreviewActionMenu',
            selector: '#connectionsPreviewActionBtn #connectionsActionMenu'
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
            ref: 'latestStatusFilter',
            selector: 'dsh-view-widget-connectionstopfilter #latest-state-filter'
        },
        {
            ref: 'finishedBetweenFilter',
            selector: 'dsh-view-widget-connectionstopfilter #finish-interval-filter'
        },
        {
            ref: 'connectionTypeFilter',
            selector: 'dsh-view-widget-connectionstopfilter #connection-type-filter'
        }

    ],

    prefix: '#connectionsdetails',

    init: function () {
        this.control({
            'connections-details #connectionsdetails': {
                selectionchange: this.onSelectionChange
            },
            'connections-details #communicationsdetails': {
                selectionchange: this.onCommunicationSelectionChange
            },
            'connections-details preview_connection #connectionsActionMenu': {
                click: this.chooseAction
            },
            '#connectionsDetailsActionMenu': {
                click: this.chooseAction
            },
            'connections-list #generate-report': {
                click: this.onGenerateReport
            },
            'connections-list #btn-connections-bulk-action': {
                click: this.navigateToBulk
            },
            // disable the finished between filter if in the latest status filter "Not applicable" is selected:
            'dsh-view-widget-connectionstopfilter #latest-state-filter': {
                change: this.updateFinishedBetweenFilter,
                // disable "Not applicable" in case of finish between date from/to is set but not applied
                focus: this.showLatestStatusFilter
            },
            // Remove the option "Not applicable" from the latest status filter if the finished between filter is used:
            'dsh-view-widget-connectionstopfilter #finish-interval-filter': {
                filterupdate: this.updateLatestStatusFilter,
                filtervaluechange: this.updateLatestStatusFilter
            },
            'communication-action-menu': {
                click: this.viewCommunicationLog
            }

        });

        this.callParent(arguments);
    },

    showOverview: function () {
        var widget = Ext.widget('connections-details'),
            store = this.getStore('Dsh.store.ConnectionTasks');

        this.getApplication().fireEvent('changecontentevent', widget);
        store.load();
    },

    onCommunicationSelectionChange: function (grid, selected) {
        var me = this,
            record = selected[0],
            preview = me.getCommunicationPreview();

        record.data.devConfig = {
            config: record.data.deviceConfiguration,
            devType: record.data.deviceType
        };

        preview.loadRecord(record);
        preview.down('communication-action-menu').record = record;
        preview.setTitle(Uni.I18n.translate('general.XonY', 'DSH', '{0} on {1}', [record.get('comTask').name, record.get('device').name]));
        Ext.resumeLayouts(true);
    },

    onSelectionChange: function (grid, selected) {
        var me = this,
            preview = me.getConnectionPreview(),
            commPanel = me.getCommunicationsPanel(),
            commStore = me.getStore('Dsh.store.Communications'),
            record = selected[0];

        if (!_.isEmpty(record)) {
            me.getConnectionsPreviewActionMenu().record = record;
            var id = record.get('id'),
                title = ' ' + record.get('title');

            Ext.suspendLayouts();
            preview.setTitle(title);
            commPanel.setTitle(Uni.I18n.translate('connection.widget.details.communicationTasksOfX', 'DSH', 'Communication tasks of {0}', [title], false));
            preview.loadRecord(record);
            Ext.resumeLayouts(true);

            if (id) {
                commStore.setConnectionId(id);
                commStore.load();
            }
        }
    },

    viewCommunicationLog: function (menu) {
        location.href = '#/devices/' + menu.record.get('device').name
            + '/communicationtasks/' + menu.record.get('comTask').id
            + '/history/' + menu.record.get('id')
            + '/viewlog' +
            '?logLevels=Error&logLevels=Warning&logLevels=Information';
    },
    onGenerateReport: function () {
        var me = this;
        var router = this.getController('Uni.controller.history.Router');
        var fieldsToFilterNameMap = {};
        fieldsToFilterNameMap['deviceGroups'] = 'GROUPNAME';
        fieldsToFilterNameMap['currentStates'] = 'STATUS';
        fieldsToFilterNameMap['latestResults'] = null;
        fieldsToFilterNameMap['comSchedules'] = 'SCHEDULENAME';
        fieldsToFilterNameMap['deviceTypes'] = null;
        fieldsToFilterNameMap['comTasks'] = 'COMTASKNAME';
        fieldsToFilterNameMap['comPortPools'] = 'PORTPOOLNAME';
        fieldsToFilterNameMap['connectionTypes'] = 'CONNECTIONTYPE';

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

        //handle special startBetween and finishBetween;
        //router.filter.startedBetween
        //router.filter.finishBetween

        router.getRoute('workspace/generatereport').forward(null, {
            category: 'MDC',
            subCategory: 'Device Connections',
            filter: !Ext.Object.isEmpty(reportFilter)
                ? Ext.JSON.encode(reportFilter)
                : false
        });
    },

    navigateToBulk: function () {
        location.href = '#/workspace/connections/details/bulk?' + Uni.util.QueryString.getQueryString();
    },

    updateFinishedBetweenFilter: function(combo, newValue) {
        this.getFinishedBetweenFilter().getChooseIntervalButton().setDisabled(Ext.isArray(newValue) && _.contains(newValue, 'NOT_APPLICABLE'));
    },

    showLatestStatusFilter: function() {
        var me = this;
        if(me.getFinishedBetweenFilter().getChooseIntervalButton().down('#fromDate').value != null ||
            me.getFinishedBetweenFilter().getChooseIntervalButton().down('#toDate').value != null) {
            me.getLatestStatusFilter().getStore().filterBy(me.doFilterLatestStatus);
        }
    },

    updateLatestStatusFilter: function() {
        var me = this;
        if (me.getLatestStatusFilter()) {
            var filterStore = me.getLatestStatusFilter().getStore();
            if (me.getFinishedBetweenFilter().getParamValue() !== undefined) {
                filterStore.filterBy(me.doFilterLatestStatus);
                me.getLatestStatusFilter(); // Apparently, needed to visually see the filtering active in the combo box
            } else {
                filterStore.clearFilter();
            }
        } else {
            // Retry until you can perform the above
            Ext.TaskManager.start({
                run: me.updateLatestStatusFilter,
                interval: 200,
                repeat: 1,
                scope: me
            });
        }
    },

    doFilterLatestStatus: function(record, id) {
        return record.get('successIndicator') !== 'NOT_APPLICABLE';
    },

    chooseAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        switch (item.action) {
            case 'run':
                menu.record.run(function () {
                    me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.runSucceeded', 'DSH', 'Run succeeded'));
                    menu.record.set('nextExecution', new Date());
                    me.showOverview();
                });
                break;
            case 'viewLog':
                router.getRoute('devices/device/connectionmethods/history/viewlog').forward(
                    {
                        deviceId: encodeURIComponent(menu.record.get('device').name),
                        connectionMethodId: menu.record.get('id'),
                        historyId: menu.record.get('comSessionId')
                    });
                break;
            case 'viewHistory':
                router.getRoute('devices/device/connectionmethods/history').forward(
                    {
                        deviceId: encodeURIComponent(menu.record.get('device').name),
                        connectionMethodId: menu.record.get('id')
                    }
                );
                break;
            case 'viewCommunicationTasks':
                me.viewCommunicationTasks(menu.record);
                break;
        }
    },

    viewCommunicationTasks: function (record) {
        var connectionType = record.get('connectionType'),
            storeIndex = this.getConnectionTypeFilter().getStore().findExact("name", connectionType),
            connectionTypeRecord = storeIndex!=-1 ? this.getConnectionTypeFilter().getStore().getAt(storeIndex) : undefined;

        location.href = '#/workspace/communications/details?device=' + encodeURIComponent(record.get('device').name)
            + (Ext.isEmpty(connectionTypeRecord) ? '' : '&connectionTypes=' + connectionTypeRecord.get('id'));
    }

});
