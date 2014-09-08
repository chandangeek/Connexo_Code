Ext.define('Dsh.view.widget.CommunicationsList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.communications-list',
    requires: [
        'Ext.grid.column.Date',
        'Ext.form.field.ComboBox',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Dsh.view.widget.ActionMenu'
    ],
//    store: 'Dsh.store.CommunicationTasks',
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'name',
                text: Uni.I18n.translate('communication.widget.details.commTask', 'DSH', 'Communications'),
                dataIndex: 'name',
                flex: 1
            },
            {
                itemId: 'device',
                text: Uni.I18n.translate('communication.widget.details.device', 'DSH', 'Device'),
                dataIndex: 'device',
                flex: 1,
                renderer: function (val) {
                    return val.name
                }
            },
            {
                itemId: 'currentState',
                text: Uni.I18n.translate('communication.widget.details.currentState', 'DSH', 'Current state'),
                dataIndex: 'currentState',
                flex: 1,
                renderer: function (val) {
                    return val.displayValue
                }
            },
            {
                itemId: 'latestResult',
                text: Uni.I18n.translate('communication.widget.details.currentState', 'DSH', 'Latest result'),
                dataIndex: 'latestResult',
                flex: 1
            },
            {
                itemId: 'nextCommunication',
                text: Uni.I18n.translate('communication.widget.details.startedOn', 'DSH', 'Next communication'),
                dataIndex: 'nextCommunication',
                xtype: 'datecolumn',
                format: 'm/d/Y h:i:s',
                flex: 2
            },
            {
                itemId: 'startTime',
                text: Uni.I18n.translate('communication.widget.details.startedOn', 'DSH', 'Started on'),
                dataIndex: 'startTime',
                xtype: 'datecolumn',
                format: 'm/d/Y h:i:s',
                flex: 2
            },
            {
                itemId: 'successfulFinishTime',
                text: Uni.I18n.translate('communication.widget.details.startedOn', 'DSH', 'Finished succesfuly on'),
                dataIndex: 'successfulFinishTime',
                xtype: 'datecolumn',
                format: 'm/d/Y h:i:s',
                flex: 2
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn'
//                ,items: 'Dsh.view.widget.ActionMenu'
            }
        ]
    }
//  ,  dockedItems: [
//        {
//            itemId: 'pagingtoolbartop',
//            xtype: 'pagingtoolbartop',
//            dock: 'top',
//    //        store: 'Dsh.store.CommunicationTasks',
//            displayMsg: Uni.I18n.translate('communication.widget.details.displayMsg', 'DDSH', '{0} - {1} of {2} communications'),
//            displayMoreMsg: Uni.I18n.translate('communication.widget.details.displayMoreMsg', 'DSH', '{0} - {1} of more than {2} communications'),
//            emptyMsg: Uni.I18n.translate('communication.widget.details.emptyMsg', 'DSH', 'There are no communications to display'),
//            items: [
//                '->',
//                {
//                    xtype: 'button',
//                    text: Uni.I18n.translate('general.title.bulkActions', 'DSH', 'Choose columns'),
//                    action: 'bulkchangesissues'
//                },
//                {
//                    xtype: 'button',
//                    text: Uni.I18n.translate('general.title.bulkActions', 'DSH', 'Bulk action'),
//                    action: 'bulkchangesissues'
//                }
//            ]
//        },
//        {
//            itemId: 'pagingtoolbarbottom',
//            xtype: 'pagingtoolbarbottom',
//  //          store: 'Dsh.store.CommunicationTasks',
//            dock: 'bottom',
//            itemsPerPageMsg: Uni.I18n.translate('communication.widget.details.itemsPerPage', 'DSH', 'Connections per page')
//        }
//    ],
});

