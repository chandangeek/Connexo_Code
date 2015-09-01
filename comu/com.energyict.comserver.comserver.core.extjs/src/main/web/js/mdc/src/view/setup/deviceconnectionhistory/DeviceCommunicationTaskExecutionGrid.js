Ext.define('Mdc.view.setup.deviceconnectionhistory.DeviceCommunicationTaskExecutionGrid', {
    extend: 'Ext.grid.Panel',
    alias: 'widget.deviceCommunicationTaskExecutionGrid',
    itemId: 'deviceCommunicationTaskExecutionGrid',
    requires: [
        'Uni.view.toolbar.PagingTop',
        'Uni.view.toolbar.PagingBottom',
        'Uni.grid.column.Duration'
    ],
    store: 'DeviceCommunicationTaskExecutions',
    columns: {
        defaults: {
            sortable: false,
            menuDisabled: true
        },
        items: [
            {
                itemId: 'name',
                text: Uni.I18n.translate('deviceconnectionhistory.communication', 'MDC', 'Communication task'),
                dataIndex: 'name',
                flex: 2
            },
            {
                itemId: 'device',
                text: Uni.I18n.translate('deviceconnectionhistory.device', 'MDC', 'Device'),
                dataIndex: 'device',
                flex: 2,
                renderer: function(value){
                   return Ext.String.htmlEncode(value.name);
                }
            },
            {
                itemId: 'result',
                text: Uni.I18n.translate('deviceconnectionhistory.result', 'MDC', 'Result'),
                dataIndex: 'result',
                flex: 1
            },
            {
                itemId: 'startTime',
                text: Uni.I18n.translate('deviceconnectionhistory.startedOn', 'MDC', 'Started on'),
                dataIndex: 'startTime',
                flex: 2,
                renderer: function (value) {
                    return value ? Uni.DateTime.formatDateTimeShort(value) : '';
                }
            },
            {
                xtype: 'uni-grid-column-duration',
                itemId: 'durationInSeconds',
                text: Uni.I18n.translate('deviceconnectionhistory.duration', 'MDC', 'Duration'),
                dataIndex: 'durationInSeconds',
                flex: 2,
                usesSeconds: true
            },
            {
                itemId: 'action',
                xtype: 'uni-actioncolumn'
            }
        ]
    },
    initComponent: function(){
        var me=this;
        me.dockedItems = [
            {
                xtype: 'pagingtoolbartop',
                store: me.store,
                dock: 'top',
                displayMsg: Uni.I18n.translate('devicecommunicationhistory.pagingtoolbartop.displayMsg', 'MDC', '{0} - {1} of {2} communications'),
                displayMoreMsg: Uni.I18n.translate('devicecommunicationhistory.pagingtoolbartop.displayMoreMsg', 'MDC', '{0} - {1} of more than {2} communications'),
                emptyMsg: Uni.I18n.translate('devicecommunicationhistory.pagingtoolbartop.emptyMsg', 'MDC', 'There are no communications to display')
            },
            {
                xtype: 'pagingtoolbarbottom',
                store: me.store,
//            params: [
//                {mrid: me.mrid}
//            ],
                deferLoading: true,
                itemsPerPageMsg: Uni.I18n.translate('devicecommunicationhistory.pagingtoolbarbottom.itemsPerPage', 'MDC', 'Communications per page'),
                dock: 'bottom'
            }
        ];
        me.callParent();
    }

})
;
