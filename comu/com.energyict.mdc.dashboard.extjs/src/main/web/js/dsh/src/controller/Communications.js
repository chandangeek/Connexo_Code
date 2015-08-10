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
        },
        {
            ref: 'latestResultFilter',
            selector: 'dsh-view-widget-communicationstopfilter #latest-result-filter'
        },
        {
            ref: 'finishedBetweenFilter',
            selector: 'dsh-view-widget-communicationstopfilter #finish-interval-filter'
        },
        {
            ref: 'comTaskFilter',
            selector: 'dsh-view-widget-communicationstopfilter #com-task-filter'
        },
        {
            ref: 'comScheduleFilter',
            selector: 'dsh-view-widget-communicationstopfilter #com-schedule-filter'
        }
    ],

    init: function () {
        var me = this;
        me.control({
            '#communicationsdetails #communicationslist': {
                selectionchange: me.onSelectionChange
            },
            'communications-list #generate-report': {
                click: me.onGenerateReport
            },
            'communications-list #btn-communications-bulk-action': {
                click: me.forwardToBulk
            },
            // disable the finished between filter if the latest result filter is used:
            'dsh-view-widget-communicationstopfilter #latest-result-filter': {
                change: me.updateFinishedBetweenFilter
            },
            // disable the latest result filter if the finished between filter is used:
            'dsh-view-widget-communicationstopfilter #finish-interval-filter': {
                filterupdate: me.updateLatestResultFilter,
                filtervaluechange: me.updateLatestResultFilter
            },
            // disable the com schedule filter if the com task filter is used:
            'dsh-view-widget-communicationstopfilter #com-task-filter': {
                change: me.updateComScheduleFilter
            },
            // disable the com task filter if the com schedule filter is used:
            'dsh-view-widget-communicationstopfilter #com-schedule-filter': {
                change: me.updateComTaskFilter
            }
        });

        me.callParent(arguments);
    },

    updateFinishedBetweenFilter: function(combo, newValue) {
        this.getFinishedBetweenFilter().getChooseIntervalButton().setDisabled(Ext.isArray(newValue) && newValue.length!==0);
    },

    updateLatestResultFilter: function() {
        var me = this;
        if (me.getLatestResultFilter()) {
            me.getLatestResultFilter().setDisabled(me.getFinishedBetweenFilter().getParamValue() !== undefined);
        } else {
            // Retry until you can perform the above
            Ext.TaskManager.start({
                run: me.updateLatestResultFilter,
                interval: 200,
                repeat: 1,
                scope: me
            });
        }
    },

    updateComScheduleFilter: function(combo, newValue) {
        this.getComScheduleFilter().setDisabled(Ext.isArray(newValue) && newValue.length!==0);
    },

    updateComTaskFilter: function(combo, newValue) {
        this.getComTaskFilter().setDisabled(Ext.isArray(newValue) && newValue.length!==0);
    },

    showOverview: function () {
        var me = this,
            widget = Ext.widget('communications-details'),
            store = me.getStore('Dsh.store.CommunicationTasks');

        me.getApplication().fireEvent('changecontentevent', widget);
        store.load();
    },

    initMenu: function (record, menuItems, me) {
        var me = this;

        me.getCommunicationsGridActionMenu().menu.removeAll();
        me.getCommunicationPreviewActionMenu().menu.removeAll();
        me.getConnectionsPreviewActionBtn().menu.removeAll();

        Ext.suspendLayouts();

        Ext.each(record.get('comTasks'), function (item) {
            if (record.get('sessionId') !== 0) {
                menuItems.push({
                    text: Ext.String.format(Uni.I18n.translate('connection.widget.details.menuItem', 'DSH', 'View \'{0}\' log'), item.name),
                    action: {
                        action: 'viewlog',
                        comTask: {
                            mRID: record.get('device').id,
                            sessionId: record.get('sessionId'),
                            comTaskId: item.id
                        }
                    },
                    listeners: {
                        click: me.viewCommunicationLog
                    }
                });
            }
        });

        if (record.get('connectionTask').connectionStrategy && record.get('connectionTask').connectionStrategy.id) {
            if (record.get('connectionTask').connectionStrategy.id === 'minimizeConnections') {
                menuItems.push({
                    text: Uni.I18n.translate('connection.widget.details.menuItem.run', 'DSH', 'Run'),
                    action: {
                        action: 'run',
                        record: record,
                        me: me
                    },
                    listeners: {
                        click: me.communicationRun
                    }
                });
            }

            menuItems.push({
                text: Uni.I18n.translate('connection.widget.details.menuItem.runNow', 'DSH', 'Run now'),
                action: {
                    action: 'runNow',
                    record: record,
                    me: me
                },
                listeners: {
                    click: me.communicationRunNow
                }
            });

        }

        var connectionMenuItem = {
            text: Uni.I18n.translate('connection.widget.details.connectionMenuItem', 'DSH', 'View connection log'),
            action: {
                action: 'viewlog',
                connection: {
                    mRID: record.get('device').id,
                    connectionMethodId: record.get('connectionTask').id,
                    sessionId: record.get('connectionTask').comSessionId

                }
            },
            listeners: {
                click: me.viewConnectionLog
            }
        };

        me.getCommunicationsGridActionMenu().menu.add(menuItems);
        me.getCommunicationPreviewActionMenu().menu.add(menuItems);

        if (record.get('connectionTask').comSessionId !== 0) {
            me.getConnectionsPreviewActionBtn().menu.add(connectionMenuItem);
        }

        Ext.resumeLayouts(true);
    },

    onSelectionChange: function (grid, selected) {
        var me = this,
            preview = me.getCommunicationPreview(),
            connPreview = me.getConnectionPreview(),
            record = selected[0],
            menuItems = [];

        if (record) {
            me.initMenu(record, menuItems, me);
            preview.loadRecord(record);
            preview.setTitle(record.get('name') + ' on ' + record.get('device').name);
            if (record.getData().connectionTask) {
                var conTask = record.getConnectionTask();
                connPreview.setTitle(conTask.get('connectionMethod').name + ' on ' + conTask.get('device').name);
                connPreview.show();
                connPreview.loadRecord(conTask);
            } else {
                connPreview.hide()
            }
        }
    },

    viewCommunicationLog: function (item) {
        location.href = '#/devices/' + item.action.comTask.mRID
        + '/communicationtasks/' + item.action.comTask.comTaskId
        + '/history/' + item.action.comTask.sessionId
        + '/viewlog'
        + '?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22id%22%3Anull%7D';
    },

    viewConnectionLog: function (item) {
        location.href = '#/devices/' + encodeURIComponent(item.action.connection.mRID) + '/connectionmethods/'
        + item.action.connection.connectionMethodId + '/history/' + item.action.connection.sessionId + '/viewlog'
        + '?logLevels=Error&logLevels=Warning&logLevels=Information&communications=Connections&communications=Communications'
    },

    onGenerateReport: function () {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            fieldsToFilterNameMap = {},
            reportFilter = false,
            filterName = undefined,
            fieldValue = undefined,
            filters = me.getFilterPanel().filters;

        fieldsToFilterNameMap['deviceGroup'] = 'GROUPNAME';
        fieldsToFilterNameMap['currentStates'] = 'STATUS';
        fieldsToFilterNameMap['latestResults'] = null;
        fieldsToFilterNameMap['comSchedules'] = 'SCHEDULENAME';
        fieldsToFilterNameMap['deviceTypes'] = null;
        fieldsToFilterNameMap['comTasks'] = 'COMTASKNAME';
        fieldsToFilterNameMap['startInterval'] = 'CONNECTIONDATE';
        // TODO Check if finished interval is even supported by the Yellowfin report.
        //fieldsToFilterNameMap['finishInterval'] = 'CONNECTIONDATE-FINISH';

        filters.each(function (filter) {
            filterName = fieldsToFilterNameMap[filter.dataIndex];
            reportFilter = reportFilter || {};
            fieldValue = undefined;

            switch (filter.getXType()) {
                case 'uni-grid-filtertop-interval':
                    var fromValue = filter.getFromDateValue(),
                        toValue = filter.getToDateValue();

                    if (Ext.isDefined(fromValue) && Ext.isDefined(toValue)) {
                        fieldValue = {
                            from: Ext.Date.format(fromValue, "Y-m-d H:i:s"),
                            to: Ext.Date.format(toValue, "Y-m-d H:i:s")
                        };
                    }
                    break;
                default:
                    fieldValue = filter.getParamValue();
                    break;
            }

            reportFilter[filterName] = fieldValue;
        }, me);

        router.getRoute('generatereport').forward(null, {
            category: 'MDC',
            subCategory: 'Device Communication',
            filter: reportFilter
        });
    },

    communicationRun: function (item) {
        var me = item.action.me;
        var record = item.action.record;
        record.run(function () {
            me.getApplication().fireEvent('acknowledge',
                Uni.I18n.translate('device.communication.run.wait', 'DSH', 'Run succeeded')
            );
            record.set('plannedDate', new Date());
            me.showOverview();
        });
    },

    communicationRunNow: function (item) {
        var me = item.action.me;
        var record = item.action.record;
        record.run(function () {
            me.getApplication().fireEvent('acknowledge',
                Uni.I18n.translate('device.communication.run.now', 'DSH', 'Run now succeeded')
            );
            record.set('plannedDate', new Date());
            me.showOverview();
        });
    },

    forwardToBulk: function () {
        var router = this.getController('Uni.controller.history.Router');

        router.getRoute('workspace/communications/details/bulk').forward(null, router.queryParams);
    }
});
