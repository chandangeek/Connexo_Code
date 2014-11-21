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
        'Dsh.view.widget.CommunicationsList',
        'Dsh.view.widget.PreviewCommunication'
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
            '#connectionsdetails': {
                selectionchange: this.onSelectionChange
            },
            '#communicationsdetails': {
                selectionchange: this.onCommunicationSelectionChange
            },
            '#connectionsActionMenu': {
                click: this.selectAction,
                afterrender: this.initConnectionMenu
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
        record.data.title = record.data.name + ' on ' + record.data.device.name;
        preview.setTitle(record.data.title);
        preview.loadRecord(record);
        this.initMenu(record, menuItems, me);
    },

    initMenu: function (record, menuItems, me) {
        this.getCommunicationsGridActionMenu().menu.removeAll();
        this.getCommunicationPreviewActionMenu().menu.removeAll();
        Ext.each(record.get('comTasks'), function (item) {
            if (record.get('sessionId') !== 0) {
                menuItems.push({
                    text: Ext.String.format(Uni.I18n.translate('connection.widget.details.menuItem', 'MDC', 'View \'{0}\' log'), item.name),
                    action: {
                        action: 'viewlog',
                        comTask: {
                            mRID: record.get('device').id,
                            sessionId: record.get('id'),
                            comTaskId: item.id
                        }
                    },
                    listeners: {
                        click: me.viewCommunicationLog
                    }
                });
            }
        });
        this.getCommunicationsGridActionMenu().menu.add(menuItems);
        this.getCommunicationPreviewActionMenu().menu.add(menuItems);
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
            commPanel.setTitle(Uni.I18n.translate('connection.widget.details.communicationsOf', 'DSH', 'Communications of') + title);
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
                menu.down('menuitem[action=viewLog]').show()
            } else {
                menu.down('menuitem[action=viewLog]').hide()
            }
        }
    },

    selectAction: function (menu, item) {
        var me = this,
            router = me.getController('Uni.controller.history.Router'),
            record = menu.record;
        switch (item.action) {
            case 'viewLog':
                router.getRoute('devices/device/connectionmethods/history/viewlog').forward(
                    {
                        mRID: record.get('device').id,
                        connectionMethodId: record.get('id'),
                        historyId: record.get('comSessionId')
                    }
                );
                break;
            case 'viewHistory':
                router.getRoute('devices/device/connectionmethods/history').forward(
                    {
                        mRID: record.get('device').id,
                        connectionMethodId: record.get('id')
                    }
                );
                break;
        }
    },

    viewCommunicationLog: function (item) {
        location.href = '#/devices/' + item.action.comTask.mRID
            + '/communicationtasks/' + item.action.comTask.comTaskId
            + '/history/' + item.action.comTask.sessionId
            + '/viewlog' +
            '?filter=%7B%22logLevels%22%3A%5B%22Error%22%2C%22Warning%22%2C%22Information%22%5D%2C%22id%22%3Anull%7D';
    }


});
