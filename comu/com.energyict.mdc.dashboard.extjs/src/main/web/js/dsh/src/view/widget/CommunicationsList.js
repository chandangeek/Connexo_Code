Ext.define('Dsh.view.widget.CommunicationsList', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.communications-list',
    store: 'Dsh.store.CommunicationTasks',
    requires: [
        'Ext.grid.column.Date',
        'Ext.form.field.ComboBox',
        'Ext.grid.column.Template',
        'Uni.grid.column.Action',
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom'
    ],
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'name',
                text: Uni.I18n.translate('communication.widget.details.commmunication', 'DSH', 'Communication'),
                dataIndex: 'name',
                flex: 2
            },
            {
                itemId: 'device',
                text: Uni.I18n.translate('communication.widget.details.device', 'DSH', 'Device'),
                dataIndex: 'device',
                flex: 1,
                renderer: function (val) {
                    return val.name ? val.name : '';
                }
            },
            {
                itemId: 'currentState',
                text: Uni.I18n.translate('communication.widget.details.currentState', 'DSH', 'Current state'),
                dataIndex: 'currentState',
                flex: 1,
                renderer: function (val) {
                    return val.displayValue ? val.displayValue : '';
                }
            },
            {
                itemId: 'latestResult',
                text: Uni.I18n.translate('connection.widget.details.latestResult', 'DSH', 'Latest result'),
                dataIndex: 'latestResult',
                flex: 1,
                renderer: function (val) {
                    return val.displayValue ? val.displayValue : '';
                }
            },
            {
                itemId: 'nextCommunication',
                text: Uni.I18n.translate('communication.widget.details.nextCommunication', 'DSH', 'Next communication'),
                dataIndex: 'nextCommunication',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                },
                flex: 2
            },
            {
                itemId: 'startTime',
                text: Uni.I18n.translate('communication.widget.details.startedOn', 'DSH', 'Started on'),
                dataIndex: 'startTime',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                },
                flex: 2
            },
            {
                itemId: 'successfulFinishTime',
                text: Uni.I18n.translate('communication.widget.details.finishedOn', 'DSH', 'Finished successfully on'),
                dataIndex: 'successfulFinishTime',
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
                displayMsg: Uni.I18n.translate('communication.widget.details.displayMsg', 'DDSH', '{0} - {1} of {2} communications'),
                displayMoreMsg: Uni.I18n.translate('communication.widget.details.displayMoreMsg', 'DSH', '{0} - {1} of more than {2} communications'),
                emptyMsg: Uni.I18n.translate('communication.widget.details.emptyMsg', 'DSH', 'There are no communications to display'),
                items:[
                    {
                        xtype:'button',
                        itemId:'generate-report',
                        hidden: !Uni.Auth.hasAnyPrivilege(['privilege.view.reports']),
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
                itemsPerPageMsg: Uni.I18n.translate('communication.widget.details.itemsPerPage', 'DSH', 'Communications per page')
            }
        ];
        me.callParent(arguments);
    }
});

