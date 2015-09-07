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
        }
    ],

    init: function () {
        this.getStore('Dsh.store.ConnectionTasks').addListener('load', this.onStoreLoad, this);
        this.control({
            'connections-details #connectionsdetails': {
                selectionchange: this.onSelectionChange
            },
            'connections-details #communicationsdetails': {
                selectionchange: this.onCommunicationSelectionChange
            },
            '#connectionsActionMenu': {
                show: this.initConnectionMenu
            },
            'connections-list #generate-report': {
                click: this.onGenerateReport
            },
            'connections-list #btn-connections-bulk-action': {
                click: this.navigateToBulk
            },
            'connections-details uni-actioncolumn': {
                run: this.connectionRun,
                viewLog: this.viewLog,
                viewHistory: this.viewHistory
            }

        });

        this.callParent(arguments);
    },

    onStoreLoad: function(records, operation, success) {
        var commPanel = this.getCommunicationsPanel();

        if (commPanel && success && records.data.length === 0) {
            commPanel.hide();
        }
    },

    showOverview: function () {
        var me = this,
            widget = Ext.widget('connections-details'),
            store = me.getStore('Dsh.store.ConnectionTasks');

        me.getApplication().fireEvent('changecontentevent', widget);
        store.load();
    },

    onCommunicationSelectionChange: function (grid, selected) {
        var me = this,
            commPanel = me.getCommunicationsPanel(),
            record = selected[0],
            preview = me.getCommunicationPreview(),
            menuItems = [];

        if (!_.isEmpty(record)) {
            commPanel.show();
            record.data.devConfig = {
                config: record.data.deviceConfiguration,
                devType: record.data.deviceType
            };

            record.data.title = Ext.String.format(
                Uni.I18n.translate('connection.widget.details.title.methodX.on.deviceY', 'DSH', '{0} on {1}'),
                record.data.comTask.name, record.data.device.name);
            preview.setTitle(record.data.title);
            preview.loadRecord(record);
            me.initMenu(record, menuItems);
        }
    },

    initMenu: function (record, menuItems) {
        var me = this,
            gridActionMenu = this.getCommunicationsGridActionMenu().menu,
            previewActionMenu = this.getCommunicationPreviewActionMenu().menu;

        Ext.suspendLayouts();

        gridActionMenu.removeAll();
        previewActionMenu.removeAll();

        if (record.get('sessionId') !== 0) {
            menuItems.push({
                text: Ext.String.format(Uni.I18n.translate('connection.widget.details.menuItem', 'DSH', 'View \'{0}\' log'), record.get('comTask').name),
                action: {
                    action: 'viewlog',
                    comTask: {
                        mRID: record.get('device').id,
                        sessionId: record.get('id'),
                        comTaskId: record.get('comTask').id
                    }
                },
                listeners: {
                    click: me.viewCommunicationLog
                }
            });
        }

        gridActionMenu.add(menuItems);
        previewActionMenu.add(menuItems);

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

            preview.loadRecord(record);
            preview.setTitle(title);
            commPanel.setTitle(Uni.I18n.translate('connection.widget.details.communicationTasksOfX', 'DSH', 'Communication tasks of {0}',[title]));

            if (id) {
                commStore.setConnectionId(id);
                commStore.load();
                commPanel.hide()
            }
        }
    },

    initConnectionMenu: function (menu) {
        if (menu && menu.record) {
            if (menu.record.get('comSessionId') !== 0) {
                !!menu.down('menuitem[action=viewLog]') && menu.down('menuitem[action=viewLog]').show()
            } else {
                !!menu.down('menuitem[action=viewLog]') && menu.down('menuitem[action=viewLog]').hide()
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
        fieldsToFilterNameMap['comPortPools'] = 'PORTPOOLNAME';
        fieldsToFilterNameMap['connectionTypes'] = 'CONNECTIONTYPE';
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
            subCategory: 'Device Connections',
            filter: reportFilter
        });
    },

    connectionRun: function (record) {
        var me = this;

        record.run(function () {
            me.getApplication().fireEvent('acknowledge', Uni.I18n.translate('general.runSucceeded', 'DSH', 'Run succeeded'));
            record.set('nextExecution', new Date());
            me.showOverview();
        });
    },

    viewLog: function (record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('devices/device/connectionmethods/history/viewlog').forward(
            {
                mRID: encodeURIComponent(record.get('device').id),
                connectionMethodId: record.get('id'),
                historyId: record.get('comSessionId')
            });
    },

    viewHistory: function (record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('devices/device/connectionmethods/history').forward(
            {
                mRID: encodeURIComponent(record.get('device').id),
                connectionMethodId: record.get('id')
            }
        );
    },

    navigateToBulk: function () {
        location.href = '#/workspace/connections/details/bulk?' + Uni.util.QueryString.getQueryString();
    }
});
