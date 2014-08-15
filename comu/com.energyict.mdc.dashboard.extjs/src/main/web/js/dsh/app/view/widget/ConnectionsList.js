Ext.define('Dsh.view.widget.ConnectionsList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.connections-list',
    requires: [
        'Ext.grid.column.Date',
        'Ext.form.field.ComboBox',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Dsh.view.widget.ActionMenu'
    ],
    itemId: 'connectionslist',
    store: 'Dsh.store.ConnectionTasks',
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'Device',
                text: Uni.I18n.translate('workspace.dataCommunication.connections.device', 'DSH', 'Device'),
                dataIndex: 'device',
                flex: 1,
                renderer: function (val) {
                    return val.name
                }
            },
            {
                itemId: 'currentState',
                text: Uni.I18n.translate('workspace.dataCommunication.connections.currentState', 'DSH', 'Current state'),
                dataIndex: 'currentState',
                flex: 1
            },
            {
                itemId: 'latestStatus',
                text: Uni.I18n.translate('workspace.dataCommunication.connections.latestStatus', 'DSH', 'Latest status'),
                dataIndex: 'latestStatus',
                flex: 1,
                renderer: function (val) {
                    return val.displayValue
                }
            },
            {
                itemId: 'latestResult',
                text: Uni.I18n.translate('workspace.dataCommunication.connections.latestResult', 'DSH', 'Latest result'),
                dataIndex: 'latestResult',
                flex: 1,
                renderer: function (val) {
                    return val.displayValue
                }
            },
            {
                dataIndex: 'taskCount',
                itemId: 'taskCount',
                renderer: function (val) {
                    return '<tpl><span class="fa fa-check fa-lg"></span>' + val.numberOfSuccessfulTasks + '</tpl>' +
                        '<tpl><span class="fa fa-times fa-lg"></span>' + val.numberOfFailedTasks + '</tpl>' +
                        '<tpl><span class="fa fa-ban fa-lg"></span>' + val.numberOfIncompleteTasks + '</tpl>'
                },
                header: Uni.I18n.translate('workspace.dataCommunication.connections.commTasks', 'DSH', 'Communication tasks'),
                flex: 1
            },
            {
                itemId: 'startDateTime',
                text: Uni.I18n.translate('workspace.dataCommunication.connections.latestResult', 'DSH', 'Started on'),
                dataIndex: 'startDateTime',
                xtype: 'datecolumn',
                format: 'm/d/Y h:i:s',
                flex: 2
            },
            {
                itemId: 'endDateTime',
                text: Uni.I18n.translate('workspace.dataCommunication.connections.latestResult', 'DSH', 'Finished on'),
                dataIndex: 'endDateTime',
                xtype: 'datecolumn',
                format: 'm/d/Y h:i:s',
                flex: 2
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn',
                items: 'Dsh.view.widget.ActionMenu'
            }
        ]
    },
    dockedItems: [
        {
            itemId: 'pagingtoolbartop',
            xtype: 'pagingtoolbartop',
            dock: 'top',
            store: 'Dsh.store.ConnectionTasks',
            displayMsg: Uni.I18n.translate('workspace.dataCommunication.connections.displayMsg', 'DDSH', '{0} - {1} of {2} connections'),
            displayMoreMsg: Uni.I18n.translate('workspace.dataCommunication.connections.displayMoreMsg', 'DSH', '{0} - {1} of more than {2} connections'),
            emptyMsg: Uni.I18n.translate('workspace.dataCommunication.connections.emptyMsg', 'DSH', 'There are no connections to display'),
            items: [
                '->',
                {
                    xtype: 'button',
                    text: Uni.I18n.translate('general.title.bulkActions', 'DSH', 'Choose columns'),
                    action: 'bulkchangesissues'
                },
                {
                    xtype: 'button',
                    text: Uni.I18n.translate('general.title.bulkActions', 'DSH', 'Bulk action'),
                    action: 'bulkchangesissues'
                }
            ]
        },
        {
            itemId: 'pagingtoolbarbottom',
            xtype: 'pagingtoolbarbottom',
            store: 'Dsh.store.ConnectionTasks',
            dock: 'bottom',
            itemsPerPageMsg: Uni.I18n.translate('workspace.dataCommunication.connections.itemsPerPage', 'DSH', 'Connections per page')
        }
    ],

    addTooltip: function () {
        var me = this,
            view = me.getView(),
            tip = Ext.create('Ext.tip.ToolTip', {
                target: view.el,
                delegate: view.itemSelector,
                trackMouse: true,
                renderTo: Ext.getBody(),
                listeners: {
                    beforeshow: function updateTipBody(tip) {
                        tip.update('Failed"' + view.getRecord(tip.triggerElement).get('title') + '"');
                    }
                }
            })
    },

    initComponent: function () {
        var me = this;
        me.on('afterrender', me.addTooltip);
        me.callParent(arguments);
    }
});
