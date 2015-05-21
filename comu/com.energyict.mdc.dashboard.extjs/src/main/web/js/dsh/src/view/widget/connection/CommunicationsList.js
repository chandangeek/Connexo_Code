Ext.define('Dsh.view.widget.connection.CommunicationsList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.connection-communications-list',
    store: 'Dsh.store.CommunicationTasks',
    requires: [
        'Ext.grid.column.Date',
        'Ext.form.field.ComboBox',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Yfn.privileges.Yellowfin'
    ],
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'name',
                text: Uni.I18n.translate('connection.communication.widget.details.commmunication', 'DSH', 'Communication task'),
                dataIndex: 'comTask',
                renderer: function (val) {
                    return Ext.String.htmlEncode(val.name);
                },
                flex: 2
            },
            {
                itemId: 'device',
                text: Uni.I18n.translate('connection.communication.widget.details.device', 'DSH', 'Device'),
                dataIndex: 'device',
                flex: 1,
                renderer: function (val) {
                    return val.name ? Ext.String.htmlEncode(val.name) : '';
                }
            },
            {
                itemId: 'currentState',
                text: Uni.I18n.translate('connection.communication.widget.details.currentState', 'DSH', 'Current state'),
                dataIndex: 'currentState',
                flex: 1,
                renderer: function (val) {
                    return val.displayValue ? Ext.String.htmlEncode(val.displayValue) : '';
                }
            },
            {
                itemId: 'Result',
                text: Uni.I18n.translate('connection.communication.widget.details.result', 'DSH', 'Result'),
                dataIndex: 'result',
                flex: 1,
                renderer: function (val) {
                    return val.displayValue ? Ext.String.htmlEncode(val.displayValue) : '';
                }
            },
            {
                itemId: 'nextCommunication',
                text: Uni.I18n.translate('connection.communication.widget.details.nextCommunication', 'DSH', 'Next communication'),
                dataIndex: 'nextCommunication',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                },
                flex: 2
            },
            {
                itemId: 'startTime',
                text: Uni.I18n.translate('connection.communication.widget.details.startedOn', 'DSH', 'Started on'),
                dataIndex: 'startTime',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                },
                flex: 2
            },
            {
                itemId: 'stopTime',
                text: Uni.I18n.translate('connection.communication.widget.details.finishedOn', 'DSH', 'Finished on'),
                dataIndex: 'stopTime',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                },
                flex: 2
            },
            {
                itemId: 'communicationsGridActionMenu',
                xtype: 'uni-actioncolumn',
                menu: {
                    //xtype: 'communications-action-menu'
                }
            }
        ]
    },
    initComponent: function () {
        var me = this;
        me.dockedItems = [
            {
                itemId: 'pagingtoolbartop',
                xtype: 'pagingtoolbartop',
                dock: 'top',
                store: me.store,
                displayMsg: Uni.I18n.translate('connection.communication.widget.details.displayMsg', 'DDSH', '{0} - {1} of {2} communication tasks'),
                displayMoreMsg: Uni.I18n.translate('connection.communication.widget.details.displayMoreMsg', 'DSH', '{0} - {1} of more than {2} communication tasks'),
                emptyMsg: Uni.I18n.translate('connection.communication.widget.details.emptyMsg', 'DSH', 'There are no communication task to display'),
                items:[
                    {
                        xtype:'button',
                        itemId:'generate-report',
                        privileges: Yfn.privileges.Yellowfin.view,
                        text:Uni.I18n.translate('generatereport.generateReportButton', 'YFN', 'Generate report')
                    }
                ]
            },
            {
                itemId: 'pagingtoolbarbottom',
                xtype: 'pagingtoolbarbottom',
                store: me.store,
                dock: 'bottom',
                deferLoading: true,
                itemsPerPageMsg: Uni.I18n.translate('connection.communication.widget.details.itemsPerPage', 'DSH', 'Communication tasks per page')
            }
        ];
        me.callParent(arguments);
    }
});

