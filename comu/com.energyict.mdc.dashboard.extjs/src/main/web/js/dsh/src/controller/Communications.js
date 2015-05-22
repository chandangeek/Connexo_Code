Ext.define('Dsh.controller.Communications', {
    extend: 'Dsh.controller.BaseController',

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
            selector: '#communicationsdetails filter-top-panel'
        },
        {
            ref: 'sideFilterForm',
            selector: '#communicationsdetails #filter-form'
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

    prefix: '#communicationsdetails',

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
        var widget = Ext.widget('communications-details'),
            store = this.getStore('Dsh.store.CommunicationTasks');

        this.getApplication().fireEvent('changecontentevent', widget);
        this.initFilter();
        store.load();
    },

    initMenu: function (record, menuItems, me) {

        this.getCommunicationsGridActionMenu().menu.removeAll();
        this.getCommunicationPreviewActionMenu().menu.removeAll();
        this.getConnectionsPreviewActionBtn().menu.removeAll();

        Ext.suspendLayouts();

        Ext.each(record.get('comTasks'), function (item) {
            if (record.get('sessionId') !== 0) {
                menuItems.push({
                    text: Ext.String.format(Uni.I18n.translate('connection.widget.details.menuItem', 'MDC', 'View \'{0}\' log'), item.name),
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
                    text: Uni.I18n.translate('connection.widget.details.menuItem.run', 'MDC', 'Run'),
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
                text: Uni.I18n.translate('connection.widget.details.menuItem.runNow', 'MDC', 'Run now'),
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
            text: Uni.I18n.translate('connection.widget.details.connectionMenuItem', 'MDC', 'View connection log'),
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

        this.getCommunicationsGridActionMenu().menu.add(menuItems);
        this.getCommunicationPreviewActionMenu().menu.add(menuItems);

        if (record.get('connectionTask').comSessionId !== 0) {
            this.getConnectionsPreviewActionBtn().menu.add(connectionMenuItem);
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
            this.initMenu(record, menuItems, me);
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
            + '/viewlog' +
            '?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22id%22%3Anull%7D';
    },

    viewConnectionLog: function (item) {
        location.href = '#/devices/' + encodeURIComponent(item.action.connection.mRID) + '/connectionmethods/' + item.action.connection.connectionMethodId + '/history/' + item.action.connection.sessionId + '/viewlog' +
            '?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22logTypes%22%3A%5B%22Connections%22%2C%22Communications%22%5D%7D'
    },

    onGenerateReport: function () {
        var me = this;
        var router = this.getController('Uni.controller.history.Router');
        var fieldsToFilterNameMap = {};
        fieldsToFilterNameMap['deviceGroup'] = 'GROUPNAME';
        fieldsToFilterNameMap['currentStates'] = 'STATUS';
        fieldsToFilterNameMap['latestResults'] = null;
        fieldsToFilterNameMap['comSchedules'] = 'SCHEDULENAME';
        fieldsToFilterNameMap['deviceTypes'] = null;
        fieldsToFilterNameMap['comTasks'] = 'COMTASKNAME';

        var reportFilter = false;

        var fields = me.getSideFilterForm().getForm().getFields();
        fields.each(function (field) {
            reportFilter = reportFilter || {};
            var filterName = fieldsToFilterNameMap[field.getName()];
            if (filterName) {
                var fieldValue = field.getRawValue();
                if (field.getXType() == 'side-filter-combo') {
                    fieldValue = Ext.isString(fieldValue) && fieldValue.split(', ') || fieldValue;
                    fieldValue = _.isArray(fieldValue) && _.compact(fieldValue) || fieldValue;
                }
            }
            reportFilter[filterName] = fieldValue;
        });

        //handle special startBetween and finishBetween;
        //router.filter.startedBetween
        //router.filter.finishBetween

        if(router.filter && router.filter.startedBetween){
            var from = router.filter.startedBetween.get('from');
            var to = router.filter.startedBetween.get('to');
            reportFilter['CONNECTIONDATE'] ={
                'from':from && Ext.Date.format(from,"Y-m-d H:i:s"),
                'to':to && Ext.Date.format(to,"Y-m-d H:i:s")
            };
        }

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
                Uni.I18n.translate('device.communication.run.wait', 'MDC', 'Run succeeded')
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
                Uni.I18n.translate('device.communication.run.now', 'MDC', 'Run now succeeded')
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
