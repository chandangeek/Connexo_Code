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
            if (record.get('connectionTask').connectionStrategy.id === 'MINIMIZE_CONNECTIONS') {
                menuItems.push({
                    text: Uni.I18n.translate('general.run', 'DSH', 'Run'),
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
                text: Uni.I18n.translate('general.runNow', 'DSH', 'Run now'),
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
            preview.setTitle(Uni.I18n.translate('general.XonY', 'DSH', '{0} on {1}', [record.get('name'), record.get('device').id]));
            if (record.getData().connectionTask) {
                var conTask = record.getConnectionTask();
                connPreview.setTitle(Uni.I18n.translate('general.XonY', 'DSH', '{0} on {1}', [conTask.get('connectionMethod').name, conTask.get('device').id]));
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
        + '/viewlog' +
        '?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22id%22%3Anull%7D';
    },

    viewConnectionLog: function (item) {
        location.href = '#/devices/' + item.action.connection.mRID + '/connectionmethods/' + item.action.connection.connectionMethodId + '/history/' + item.action.connection.sessionId + '/viewlog' +
        '?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22logTypes%22%3A%5B%22Connections%22%2C%22Communications%22%5D%7D'
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

        router.getRoute('generatereport').forward(null, {
            category: 'MDC',
            subCategory: 'Device Communication',
            filter: Ext.JSON.encode(reportFilter)
        });
    },

    communicationRun: function (item) {
        var me = item.action.me;
        var record = item.action.record;
        record.run(function () {
            me.getApplication().fireEvent('acknowledge',
                Uni.I18n.translate('general.runSucceeded', 'DSH', 'Run succeeded')
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
        location.href = '#/workspace/communications/details/bulk?' + Uni.util.QueryString.getQueryString();
    }
});
