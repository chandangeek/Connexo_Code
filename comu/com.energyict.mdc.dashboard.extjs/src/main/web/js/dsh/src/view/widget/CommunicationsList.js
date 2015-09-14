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
                text: Uni.I18n.translate('communication.widget.details.commmunication', 'DSH', 'Communication'),
                dataIndex: 'name',
                flex: 2
            },
            {
                itemId: 'device',
                text: Uni.I18n.translate('general.device', 'DSH', 'Device'),
                dataIndex: 'device',
                flex: 1,
                renderer: function (val) {
                    return val.name ? Ext.String.htmlEncode(val.name) : '';
                }
            },
            {
                itemId: 'currentState',
                text: Uni.I18n.translate('general.currentState', 'DSH', 'Current state'),
                dataIndex: 'currentState',
                flex: 1,
                renderer: function (val) {
                    return val.displayValue ? Ext.String.htmlEncode(val.displayValue) : '';
                }
            },
            {
                itemId: 'latestResult',
                text: Uni.I18n.translate('general.latestResult', 'DSH', 'Latest result'),
                dataIndex: 'latestResult',
                flex: 1,
                renderer: function (val) {
                    return val.displayValue ? Ext.String.htmlEncode(val.displayValue) : '';
                }
            },
            {
                itemId: 'nextCommunication',
                text: Uni.I18n.translate('general.nextCommunication', 'DSH', 'Next communication'),
                dataIndex: 'nextCommunication',
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                },
                flex: 2
            },
            {
                itemId: 'startTime',
                text: Uni.I18n.translate('general.startedOn', 'DSH', 'Started on'),
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
                displayMsg: Uni.I18n.translate('communication.widget.details.displayMsg', 'DSH', '{0} - {1} of {2} communications'),
                displayMoreMsg: Uni.I18n.translate('communication.widget.details.displayMoreMsg', 'DSH', '{0} - {1} of more than {2} communications'),
                emptyMsg: Uni.I18n.translate('communication.widget.details.emptyMsg', 'DSH', 'There are no communications to display'),
                items:[
                    {
                        xtype:'button',
                        itemId:'generate-report',
                        privileges: Yfn.privileges.Yellowfin.view,
                        text:Uni.I18n.translate('generatereport.generateReportButton', 'DSH', 'Generate report')
                    },
                    {
                        xtype:'button',
                        itemId:'btn-communications-bulk-action',
                        privileges: Mdc.privileges.Device.viewOrAdministrateOrOperateDeviceCommunication,
                        text: Uni.I18n.translate('general.bulkAction', 'DSH', 'Bulk action')
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

