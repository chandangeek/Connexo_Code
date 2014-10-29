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
        'Dsh.view.widget.PreviewCommunication'
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
            ref: 'connectionsPreviewActionMenu',
            selector: '#connectionsPreviewActionMenu'
        }
    ],

    prefix: '#communicationsdetails',

    init: function () {
        this.control({
            '#communicationsdetails #communicationslist': {
                selectionchange: this.onSelectionChange
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
        this.getConnectionsPreviewActionMenu().menu.removeAll()
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
        if(record.get('connectionTask').comSessionId!==0){
            this.getConnectionsPreviewActionMenu().menu.add(connectionMenuItem);
        }
    },

    onSelectionChange: function (grid, selected) {
        var me = this,
            preview = me.getCommunicationPreview(),
            connPreview = me.getConnectionPreview(),
            record = selected[0],
            menuItems = [];
        if (record ) {
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

    viewCommunicationLog: function(item){
        location.href = '#/devices/' + item.action.comTask.mRID
            + '/communicationtasks/' +  item.action.comTask.comTaskId
            + '/history/' +  item.action.comTask.sessionId
            + '/viewlog' +
            '?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22id%22%3Anull%7D';
    },

    viewConnectionLog: function(item){
        location.href = '#/devices/' + item.action.connection.mRID + '/connectionmethods/' + item.action.connection.connectionMethodId + '/history/' + item.action.connection.sessionId + '/viewlog' +
            '?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22logTypes%22%3A%5B%22connections%22%2C%22communications%22%5D%7D'
    }
});
