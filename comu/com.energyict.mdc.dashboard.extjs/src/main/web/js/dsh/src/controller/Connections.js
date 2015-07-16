Ext.define('Dsh.controller.Connections', {
    extend: 'Dsh.controller.BaseController',

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
            selector: '#connectionsdetails filter-top-panel'
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
            '#connectionsActionMenu': {
                show: this.initConnectionMenu
            },
            'connections-list #generate-report': {
                click: this.onGenerateReport
            },
            'connections-list #btn-connections-bulk-action': {
                click: this.forwardToBulk
            },
            'connections-details uni-actioncolumn': {
                run: this.connectionRun,
                viewLog: this.viewLog,
                viewHistory: this.viewHistory
            }

        });

        this.callParent(arguments);
    },

    showOverview: function () {
        var widget = Ext.widget('connections-details'),
            store = this.getStore('Dsh.store.ConnectionTasks');

        this.getApplication().fireEvent('changecontentevent', widget);
        this.initFilter();
        store.load();
    },

    onCommunicationSelectionChange: function (grid, selected) {
        var me = this,
            commPanel = me.getCommunicationsPanel(),
            record = selected[0],
            preview = me.getCommunicationPreview(),
            menuItems = [];

        commPanel.show();
        record.data.devConfig = {
            config: record.data.deviceConfiguration,
            devType: record.data.deviceType
        };

        record.data.title = record.data.comTask.name + ' on ' + record.data.device.name;
        preview.setTitle(record.data.title);
        preview.loadRecord(record);
        this.initMenu(record, menuItems);
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
                    text: Ext.String.format(Uni.I18n.translate('connection.widget.details.menuItem', 'MDC', 'View \'{0}\' log'), record.get('comTask').name),
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
            commPanel.setTitle(Uni.I18n.translate('connection.widget.details.communicationTasksOf', 'DSH', 'Communication tasks of') + title);

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
            + '/viewlog' +
            '?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22id%22%3Anull%7D';
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
        fieldsToFilterNameMap['comPortPools'] = 'PORTPOOLNAME';
        fieldsToFilterNameMap['connectionTypes'] = 'CONNECTIONTYPE';

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

        if(router.filter && router.filter.startedBetween){
            var from = router.filter.startedBetween.get('from');
            var to = router.filter.startedBetween.get('to');
            reportFilter['CONNECTIONDATE'] ={
                'from':from && Ext.Date.format(from,"Y-m-d H:i:s"),
                'to':to && Ext.Date.format(to,"Y-m-d H:i:s")
            };
        }



        //handle special startBetween and finishBetween;
        //router.filter.startedBetween
        //router.filter.finishBetween

        router.getRoute('generatereport').forward(null, {
            category: 'MDC',
            subCategory: 'Device Connections',
            filter: reportFilter
        });
    },

    connectionRun: function (record) {
        var me = this;
        record.run(function () {
            me.getApplication().fireEvent('acknowledge',
                Uni.I18n.translate('connection.run.now', 'MDC', 'Run succeeded')
            );
            record.set('nextExecution', new Date());
            me.showOverview();
        });

    },

    viewLog: function (record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('devices/device/connectionmethods/history/viewlog').forward(
            {
                mRID: record.get('device').id,
                connectionMethodId: record.get('id'),
                historyId: record.get('comSessionId')
            });
    },

    viewHistory: function (record) {
        var me = this,
            router = me.getController('Uni.controller.history.Router');

        router.getRoute('devices/device/connectionmethods/history').forward(
            {
                mRID: record.get('device').id,
                connectionMethodId: record.get('id')
            }
        );
    },

    forwardToBulk: function () {
        var router = this.getController('Uni.controller.history.Router');

        router.getRoute('workspace/connections/details/bulk').forward(null, router.queryParams);
    }
});
